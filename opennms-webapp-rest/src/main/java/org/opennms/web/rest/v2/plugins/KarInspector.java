/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opennms.web.rest.v2.plugins.KarInspection.BundleDescriptor;
import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.DependencyInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.FeatureInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads a candidate KAR without the OSGi framework and reports its contents and
 * the outcome of the structural checks. Never writes anything.
 */
public class KarInspector {

    public static final long MAX_SIZE_BYTES = 512L * 1024 * 1024;

    public static final String CHECK_SIZE_LIMIT = "size-limit";
    public static final String CHECK_ZIP_READABLE = "zip-readable";
    public static final String CHECK_MANIFEST_PRESENT = "manifest-present";
    public static final String CHECK_ENTRIES_SAFE = "entries-safe";
    public static final String CHECK_FEATURES_XML_PRESENT = "features-xml-present";
    public static final String CHECK_FEATURES_XML_PARSES = "features-xml-parses";
    public static final String CHECK_FEATURES_DECLARED = "features-declared";
    public static final String CHECK_BUNDLES_RESOLVABLE_JARS = "bundles-resolvable-jars";
    public static final String CHECK_FEATURE_START_FLAG = "feature-start-flag";
    public static final String CHECK_DUPLICATE_KAR = "duplicate-kar";

    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";
    private static final String KARAF_FEATURE_START = "Karaf-Feature-Start";
    private static final String CREATED_BY = "Created-By";
    private static final int MAX_LISTED = 5;

    private final Path deployDir;

    /** @param deployDir the Karaf deploy directory used for the duplicate check; may be null. */
    public KarInspector(final Path deployDir) {
        this.deployDir = deployDir;
    }

    public static String sanitizeKarName(final String fileName) {
        String name = fileName == null ? "" : fileName;
        final int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        if (name.toLowerCase(Locale.ROOT).endsWith(".kar")) {
            name = name.substring(0, name.length() - 4);
        }
        name = name.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^[._-]+", "");
        return name.isEmpty() ? "plugin" : name;
    }

    public KarInspection inspect(final Path file, final String originalFileName) throws IOException {
        return inspect(file, originalFileName, null);
    }

    /** @param karNameOverride already-sanitised name to use instead of the one derived from the file name. */
    public KarInspection inspect(final Path file, final String originalFileName, final String karNameOverride) throws IOException {
        final KarInspection result = new KarInspection();
        final String fileName = originalFileName != null ? originalFileName : file.getFileName().toString();
        result.setFileName(fileName);
        result.setKarName(karNameOverride != null ? karNameOverride : sanitizeKarName(fileName));
        result.setSize(Files.size(file));
        result.setSha256(sha256(file));
        final List<Check> checks = result.getChecks();

        if (result.getSize() > MAX_SIZE_BYTES) {
            checks.add(new Check(CHECK_SIZE_LIMIT, Level.FAIL, String.format("%s exceeds the %d MB limit", humanSize(result.getSize()), MAX_SIZE_BYTES / (1024 * 1024))));
        } else {
            checks.add(new Check(CHECK_SIZE_LIMIT, Level.PASS, String.format("%s is within the %d MB limit", humanSize(result.getSize()), MAX_SIZE_BYTES / (1024 * 1024))));
        }

        try (ZipFile zip = new ZipFile(file.toFile())) {
            checks.add(new Check(CHECK_ZIP_READABLE, Level.PASS, "The file is a readable ZIP archive with " + zip.size() + " entries"));
            inspectEntries(zip, result);
        } catch (final IOException e) {
            checks.add(new Check(CHECK_ZIP_READABLE, Level.FAIL, "The file is not a readable ZIP archive: " + e.getMessage()));
        }

        if (deployDir != null) {
            final Path existing = deployDir.resolve(result.getKarName() + ".kar");
            if (Files.exists(existing)) {
                checks.add(new Check(CHECK_DUPLICATE_KAR, Level.WARN, "deploy/" + existing.getFileName() + " already exists and will be replaced"));
            } else {
                checks.add(new Check(CHECK_DUPLICATE_KAR, Level.PASS, "No deploy/" + existing.getFileName() + " exists yet"));
            }
        }
        return result;
    }

    private void inspectEntries(final ZipFile zip, final KarInspection result) throws IOException {
        final List<Check> checks = result.getChecks();
        final List<String> unsafe = new ArrayList<>();
        final List<String> featureXmlCandidates = new ArrayList<>();
        final List<String> jars = new ArrayList<>();
        ZipEntry manifestEntry = null;
        int count = 0;
        int metadataFiles = 0;

        final Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            count++;
            final String name = entry.getName().replace('\\', '/');
            if (!isSafeEntry(name)) {
                unsafe.add(entry.getName());
                continue;
            }
            if (MANIFEST_ENTRY.equals(name)) {
                manifestEntry = entry;
            } else if (name.startsWith("repository/") && !entry.isDirectory()) {
                final String lower = name.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".xml")) {
                    if (isMavenMetadata(lower) || entry.getSize() == 0) {
                        metadataFiles++;
                    } else {
                        featureXmlCandidates.add(name);
                    }
                } else if (lower.endsWith(".jar")) {
                    jars.add(name);
                }
            }
        }

        if (unsafe.isEmpty()) {
            checks.add(new Check(CHECK_ENTRIES_SAFE, Level.PASS, "All " + count + " entries live under repository/ or META-INF/"));
        } else {
            checks.add(new Check(CHECK_ENTRIES_SAFE, Level.FAIL, "Entries outside repository/ and META-INF/ would be written into OPENNMS_HOME on install: " + listSome(unsafe)));
        }

        if (manifestEntry == null) {
            checks.add(new Check(CHECK_MANIFEST_PRESENT, Level.FAIL, MANIFEST_ENTRY + " is missing"));
            checks.add(new Check(CHECK_FEATURE_START_FLAG, Level.WARN, "No manifest, so " + KARAF_FEATURE_START + " cannot be read; Karaf treats a missing flag as true and starts the features as soon as the KAR is deployed"));
        } else {
            try (InputStream in = zip.getInputStream(manifestEntry)) {
                final Attributes main = new Manifest(in).getMainAttributes();
                for (final Map.Entry<Object, Object> e : main.entrySet()) {
                    result.getManifest().put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
                result.setFeatureStart(main.getValue(KARAF_FEATURE_START));
                result.setCreatedBy(main.getValue(CREATED_BY));
                checks.add(new Check(CHECK_MANIFEST_PRESENT, Level.PASS, MANIFEST_ENTRY + " is present"));
            } catch (final IOException e) {
                checks.add(new Check(CHECK_MANIFEST_PRESENT, Level.FAIL, MANIFEST_ENTRY + " cannot be parsed: " + e.getMessage()));
            }
            // Karaf only skips the automatic feature install when the header is exactly "false".
            final String flag = result.getFeatureStart();
            if (flag == null) {
                checks.add(new Check(CHECK_FEATURE_START_FLAG, Level.WARN, "The manifest does not declare " + KARAF_FEATURE_START + "; Karaf treats this as true and starts the features as soon as the KAR is deployed"));
            } else if ("false".equalsIgnoreCase(flag.trim())) {
                checks.add(new Check(CHECK_FEATURE_START_FLAG, Level.PASS, KARAF_FEATURE_START + " is false; the features start on the next restart through featuresBoot.d"));
            } else {
                checks.add(new Check(CHECK_FEATURE_START_FLAG, Level.PASS, KARAF_FEATURE_START + " is " + flag.trim() + "; Karaf starts the features as soon as the KAR is deployed"));
            }
        }

        inspectFeatureRepositories(zip, featureXmlCandidates, metadataFiles, result);
        inspectBundles(zip, jars, result);
    }

    private void inspectFeatureRepositories(final ZipFile zip, final List<String> candidates, final int metadataFiles, final KarInspection result) {
        final List<Check> checks = result.getChecks();
        final List<String> parseFailures = new ArrayList<>();
        final List<String> otherXmlFailures = new ArrayList<>();
        for (final String name : candidates) {
            try (InputStream in = zip.getInputStream(zip.getEntry(name))) {
                final Document doc = secureDocumentBuilder().parse(in);
                final Element root = doc.getDocumentElement();
                if (root == null || !"features".equals(localName(root))) {
                    continue;
                }
                result.getFeatureRepositories().add(name);
                for (final Element featureEl : childElements(root, "feature")) {
                    result.getFeatures().add(readFeature(featureEl, name));
                }
            } catch (final Exception e) {
                if (isFeaturesFileName(name)) {
                    parseFailures.add(name + " (" + e.getMessage() + ")");
                } else {
                    otherXmlFailures.add(name);
                }
            }
        }

        final String ignored = metadataFiles > 0 ? "; " + metadataFiles + " Maven metadata files ignored" : "";
        if (result.getFeatureRepositories().isEmpty()) {
            checks.add(new Check(CHECK_FEATURES_XML_PRESENT, Level.FAIL, "No feature repository (a features.xml with a <features> root) was found under repository/" + ignored));
        } else {
            checks.add(new Check(CHECK_FEATURES_XML_PRESENT, Level.PASS, "Found " + result.getFeatureRepositories().size() + " feature repositories: " + listSome(result.getFeatureRepositories()) + ignored));
        }
        if (!parseFailures.isEmpty()) {
            checks.add(new Check(CHECK_FEATURES_XML_PARSES, Level.FAIL, "Feature repositories that do not parse: " + listSome(parseFailures)));
        } else if (!otherXmlFailures.isEmpty()) {
            checks.add(new Check(CHECK_FEATURES_XML_PARSES, Level.WARN, "XML files under repository/ that are not feature repositories and do not parse (ignored): " + listSome(otherXmlFailures)));
        } else if (!result.getFeatureRepositories().isEmpty()) {
            checks.add(new Check(CHECK_FEATURES_XML_PARSES, Level.PASS, "All " + result.getFeatureRepositories().size() + " feature repositories parse"));
        }
        if (!result.getFeatureRepositories().isEmpty()) {
            if (result.getFeatures().isEmpty()) {
                checks.add(new Check(CHECK_FEATURES_DECLARED, Level.FAIL, "The KAR declares no features; it is probably a build stub, not a plugin (the real plugin KAR is usually much larger and named after the plugin)"));
            } else {
                result.markTopLevelFeatures();
                final String names = result.getFeatures().stream().map(f -> f.getName() + "/" + f.getVersion() + (f.isTopLevel() ? "" : " (dependency)")).collect(Collectors.joining(", "));
                checks.add(new Check(CHECK_FEATURES_DECLARED, Level.PASS, result.getFeatures().size() + " features declared, " + result.topLevelFeatures().size() + " of them top-level: " + names));
            }
        }
    }

    private FeatureInfo readFeature(final Element featureEl, final String repository) {
        final FeatureInfo info = new FeatureInfo();
        info.setName(featureEl.getAttribute("name"));
        info.setVersion(featureEl.hasAttribute("version") ? featureEl.getAttribute("version") : "0.0.0");
        info.setRepository(repository);
        info.setDescription(featureEl.hasAttribute("description") ? featureEl.getAttribute("description").trim() : null);
        info.setHidden("true".equalsIgnoreCase(featureEl.getAttribute("hidden")));
        for (final Element dep : childElements(featureEl, "feature")) {
            final String version = dep.hasAttribute("version") ? dep.getAttribute("version") : null;
            final boolean dependencyOnly = "true".equalsIgnoreCase(dep.getAttribute("dependency"));
            info.getDependencies().add(new DependencyInfo(dep.getTextContent().trim(), version, dependencyOnly));
        }
        for (final Element bundle : childElements(featureEl, "bundle")) {
            info.getBundles().add(bundle.getTextContent().trim());
        }
        return info;
    }

    private void inspectBundles(final ZipFile zip, final List<String> jars, final KarInspection result) {
        final List<Check> checks = result.getChecks();
        final List<String> unreadable = new ArrayList<>();
        for (final String name : jars) {
            try (InputStream in = zip.getInputStream(zip.getEntry(name))) {
                final Manifest manifest = readNestedManifest(in);
                if (manifest == null) {
                    unreadable.add(name + " (no manifest)");
                    continue;
                }
                final Attributes main = manifest.getMainAttributes();
                final BundleDescriptor bundle = new BundleDescriptor();
                bundle.setPath(name);
                bundle.setSymbolicName(OsgiHeaders.parseSymbolicName(main.getValue("Bundle-SymbolicName")));
                bundle.setVersion(main.getValue("Bundle-Version"));
                bundle.setImports(OsgiHeaders.parseImports(main.getValue("Import-Package")));
                bundle.setExports(OsgiHeaders.parseExports(main.getValue("Export-Package")));
                bundle.setRequiredJavaVersion(OsgiHeaders.parseRequiredJavaVersion(main.getValue("Require-Capability")));
                result.getBundles().add(bundle);
            } catch (final Exception e) {
                unreadable.add(name + " (" + e.getMessage() + ")");
            }
        }
        if (!unreadable.isEmpty()) {
            checks.add(new Check(CHECK_BUNDLES_RESOLVABLE_JARS, Level.FAIL, "These jars under repository/ cannot be read as bundles: " + listSome(unreadable)));
        } else if (jars.isEmpty()) {
            checks.add(new Check(CHECK_BUNDLES_RESOLVABLE_JARS, Level.WARN, "No jars under repository/; the features must resolve their bundles from elsewhere"));
        } else {
            checks.add(new Check(CHECK_BUNDLES_RESOLVABLE_JARS, Level.PASS, "All " + jars.size() + " jars under repository/ open and carry a manifest"));
        }
    }

    /** Scans the nested jar for its manifest so entry order inside the jar does not matter. */
    private static Manifest readNestedManifest(final InputStream in) throws IOException {
        final ZipInputStream nested = new ZipInputStream(in);
        ZipEntry entry;
        boolean any = false;
        while ((entry = nested.getNextEntry()) != null) {
            any = true;
            if (MANIFEST_ENTRY.equals(entry.getName().replace('\\', '/'))) {
                return new Manifest(nested);
            }
        }
        if (!any) {
            throw new IOException("not a jar");
        }
        return null;
    }

    private static String baseName(final String lowerCaseName) {
        return lowerCaseName.substring(lowerCaseName.lastIndexOf('/') + 1);
    }

    static boolean isMavenMetadata(final String lowerCaseName) {
        return baseName(lowerCaseName).startsWith("maven-metadata");
    }

    static boolean isFeaturesFileName(final String name) {
        return baseName(name.toLowerCase(Locale.ROOT)).endsWith("features.xml");
    }

    static boolean isSafeEntry(final String name) {
        if (name.isEmpty() || name.startsWith("/") || name.matches("^[A-Za-z]:.*")) {
            return false;
        }
        for (final String segment : name.split("/")) {
            if ("..".equals(segment)) {
                return false;
            }
        }
        return name.equals("repository") || name.startsWith("repository/") || name.equals("META-INF") || name.startsWith("META-INF/");
    }

    private static DocumentBuilder secureDocumentBuilder() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory.newDocumentBuilder();
    }

    private static String localName(final Node node) {
        final String local = node.getLocalName();
        if (local != null) {
            return local;
        }
        final String name = node.getNodeName();
        final int colon = name.indexOf(':');
        return colon >= 0 ? name.substring(colon + 1) : name;
    }

    private static List<Element> childElements(final Element parent, final String localName) {
        final List<Element> elements = new ArrayList<>();
        final NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(localName(child))) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    static String sha256(final Path file) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
            final byte[] buffer = new byte[64 * 1024];
            while (in.read(buffer) >= 0) {
                // digest updates as a side effect of reading
            }
        }
        final StringBuilder hex = new StringBuilder();
        for (final byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static String listSome(final List<String> items) {
        final String head = items.stream().limit(MAX_LISTED).collect(Collectors.joining(", "));
        return items.size() > MAX_LISTED ? head + " (+" + (items.size() - MAX_LISTED) + " more)" : head;
    }

    private static String humanSize(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
