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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.web.rest.v2.plugins.KarInspection.BundleDescriptor;
import org.opennms.web.rest.v2.plugins.KarInspection.Check;
import org.opennms.web.rest.v2.plugins.KarInspection.FeatureInfo;
import org.opennms.web.rest.v2.plugins.KarInspection.ImportDescriptor;
import org.opennms.web.rest.v2.plugins.KarInspection.Level;

public class KarInspectorTest {

    private static final String FEATURES_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.4.0\" name=\"example-plugin\">\n"
            + "  <feature name=\"example-plugin\" version=\"1.0.0\" description=\"Example\">\n"
            + "    <feature version=\"[1.0,2)\">opennms-integration-api</feature>\n"
            + "    <feature dependency=\"true\">example-plugin-libs</feature>\n"
            + "    <bundle>mvn:org.example/plugin/1.0.0</bundle>\n"
            + "  </feature>\n"
            + "  <feature name=\"example-plugin-libs\" version=\"1.0.0\">\n"
            + "    <bundle>wrap:mvn:org.example/lib/1.0.0</bundle>\n"
            + "  </feature>\n"
            + "</features>\n";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path deployDir;
    private KarInspector inspector;

    @Before
    public void setUp() throws IOException {
        deployDir = folder.newFolder("deploy").toPath();
        inspector = new KarInspector(deployDir);
    }

    @Test
    public void wellFormedKarPassesEveryCheck() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false", "Created-By", "unit test"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0.jar", bundle("org.example.plugin", "1.0.0",
                "org.opennms.integration.api.v1.alarms;org.opennms.integration.api.v1.model;version=\"[1.0,2)\",javax.xml.parsers,org.osgi.framework;version=\"[1.8,2)\",org.opennms.integration.api.v1.extra;version=\"[1.0,2)\";resolution:=optional",
                "org.example.plugin;version=\"1.0.0\"",
                "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=17))\""));
        final Path kar = writeKar("plugin-1.0.0.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "plugin-1.0.0.kar");

        assertEquals("plugin-1.0.0", inspection.getKarName());
        assertEquals("plugin-1.0.0.kar", inspection.getFileName());
        assertEquals(Files.size(kar), inspection.getSize());
        assertEquals(64, inspection.getSha256().length());
        assertEquals("false", inspection.getFeatureStart());
        assertEquals("unit test", inspection.getCreatedBy());
        assertTrue(inspection.checksAt(Level.FAIL).isEmpty());
        assertTrue("unexpected warnings: " + inspection.checksAt(Level.WARN), inspection.checksAt(Level.WARN).isEmpty());
        for (final String id : new String[] { KarInspector.CHECK_SIZE_LIMIT, KarInspector.CHECK_ZIP_READABLE, KarInspector.CHECK_MANIFEST_PRESENT,
                KarInspector.CHECK_ENTRIES_SAFE, KarInspector.CHECK_FEATURES_XML_PRESENT, KarInspector.CHECK_FEATURES_XML_PARSES,
                KarInspector.CHECK_BUNDLES_RESOLVABLE_JARS, KarInspector.CHECK_FEATURE_START_FLAG, KarInspector.CHECK_DUPLICATE_KAR }) {
            assertEquals(id, Level.PASS, check(inspection, id).getLevel());
        }

        assertEquals(1, inspection.getFeatureRepositories().size());
        assertEquals(2, inspection.getFeatures().size());
        final FeatureInfo feature = inspection.getFeatures().get(0);
        assertEquals("example-plugin", feature.getName());
        assertEquals("1.0.0", feature.getVersion());
        assertEquals(2, feature.getDependencies().size());
        assertEquals("opennms-integration-api", feature.getDependencies().get(0).getName());
        assertEquals("[1.0,2)", feature.getDependencies().get(0).getVersion());
        assertFalse(feature.getDependencies().get(0).isDependencyOnly());
        assertTrue(feature.getDependencies().get(1).isDependencyOnly());
        assertEquals(List.of("mvn:org.example/plugin/1.0.0"), feature.getBundles());

        final List<FeatureInfo> topLevel = inspection.topLevelFeatures();
        assertEquals(1, topLevel.size());
        assertEquals("example-plugin", topLevel.get(0).getName());

        assertEquals(1, inspection.getBundles().size());
        final BundleDescriptor bundle = inspection.getBundles().get(0);
        assertEquals("org.example.plugin", bundle.getSymbolicName());
        assertEquals("1.0.0", bundle.getVersion());
        assertEquals("[1.0,2)", bundle.getImports().get("org.opennms.integration.api.v1.alarms").getVersion());
        assertFalse(bundle.getImports().get("org.opennms.integration.api.v1.alarms").isOptional());
        assertEquals("[1.0,2)", bundle.getImports().get("org.opennms.integration.api.v1.model").getVersion());
        assertEquals("", bundle.getImports().get("javax.xml.parsers").getVersion());
        assertEquals("[1.8,2)", bundle.getImports().get("org.osgi.framework").getVersion());
        assertEquals("[1.0,2)", bundle.getImports().get("org.opennms.integration.api.v1.extra").getVersion());
        assertTrue(bundle.getImports().get("org.opennms.integration.api.v1.extra").isOptional());
        assertEquals(List.of("org.example.plugin"), bundle.getExports());
        assertEquals("17", bundle.getRequiredJavaVersion());
    }

    @Test
    public void entriesOutsideRepositoryAndMetaInfFail() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "true"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        entries.put("resources/etc/evil.properties", "x=y".getBytes(StandardCharsets.UTF_8));
        entries.put("repository/../etc/evil.cfg", "x=y".getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("evil.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "evil.kar");

        final Check check = check(inspection, KarInspector.CHECK_ENTRIES_SAFE);
        assertEquals(Level.FAIL, check.getLevel());
        assertTrue(check.getMessage(), check.getMessage().contains("resources/etc/evil.properties"));
        assertTrue(check.getMessage(), check.getMessage().contains("repository/../etc/evil.cfg"));
        assertFalse(inspection.checksAt(Level.FAIL).isEmpty());
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_FEATURE_START_FLAG).getLevel());
    }

    @Test
    public void missingManifestAndFeaturesAreReported() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("repository/readme.txt", "hello".getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("empty.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "empty.kar");

        assertEquals(Level.FAIL, check(inspection, KarInspector.CHECK_MANIFEST_PRESENT).getLevel());
        assertEquals(Level.FAIL, check(inspection, KarInspector.CHECK_FEATURES_XML_PRESENT).getLevel());
        assertEquals(Level.WARN, check(inspection, KarInspector.CHECK_FEATURE_START_FLAG).getLevel());
        assertEquals(Level.WARN, check(inspection, KarInspector.CHECK_BUNDLES_RESOLVABLE_JARS).getLevel());
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_ENTRIES_SAFE).getLevel());
        assertNull(inspection.getFeatureStart());
    }

    @Test
    public void absentFeatureStartFlagWarns() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Created-By", "unit test"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("noflag.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "noflag.kar");

        final Check check = check(inspection, KarInspector.CHECK_FEATURE_START_FLAG);
        assertEquals(Level.WARN, check.getLevel());
        assertTrue(check.getMessage(), check.getMessage().contains("Karaf-Feature-Start"));
    }

    @Test
    public void notAZipFailsReadableCheckOnly() throws IOException {
        final Path file = folder.getRoot().toPath().resolve("garbage.kar");
        Files.write(file, "this is not a zip file".getBytes(StandardCharsets.UTF_8));

        final KarInspection inspection = inspector.inspect(file, "garbage.kar");

        assertEquals(Level.FAIL, check(inspection, KarInspector.CHECK_ZIP_READABLE).getLevel());
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_SIZE_LIMIT).getLevel());
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_DUPLICATE_KAR).getLevel());
        assertNull(find(inspection, KarInspector.CHECK_MANIFEST_PRESENT));
        assertEquals(64, inspection.getSha256().length());
    }

    @Test
    public void unreadableBundleJarFails() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/broken/1.0.0/broken-1.0.0.jar", "not a jar".getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/nomanifest/1.0.0/nomanifest-1.0.0.jar", zipWithoutManifest());
        final Path kar = writeKar("broken.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "broken.kar");

        final Check check = check(inspection, KarInspector.CHECK_BUNDLES_RESOLVABLE_JARS);
        assertEquals(Level.FAIL, check.getLevel());
        assertTrue(check.getMessage(), check.getMessage().contains("broken-1.0.0.jar"));
        assertTrue(check.getMessage(), check.getMessage().contains("nomanifest-1.0.0.jar"));
        assertTrue(inspection.getBundles().isEmpty());
    }

    @Test
    public void unparseableFeaturesXmlFails() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", "<features><feature name='x'>".getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("badxml.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "badxml.kar");

        final Check check = check(inspection, KarInspector.CHECK_FEATURES_XML_PARSES);
        assertEquals(Level.FAIL, check.getLevel());
        assertTrue(check.getMessage(), check.getMessage().contains("plugin-1.0.0-features.xml"));
        assertEquals(Level.FAIL, check(inspection, KarInspector.CHECK_FEATURES_XML_PRESENT).getLevel());
    }

    @Test
    public void mavenMetadataAndEmptyXmlAreIgnored() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/plugin-features/1.0.0-SNAPSHOT/maven-metadata-local.xml", new byte[0]);
        entries.put("repository/org/example/plugin-features/maven-metadata-local.xml", "<metadata><groupId>org.example</groupId></metadata>".getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/other-features/1.0.0/empty.xml", new byte[0]);
        final Path kar = writeKar("metadata.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "metadata.kar");

        assertTrue(inspection.checksAt(Level.FAIL).isEmpty());
        final Check present = check(inspection, KarInspector.CHECK_FEATURES_XML_PRESENT);
        assertEquals(Level.PASS, present.getLevel());
        assertTrue(present.getMessage(), present.getMessage().endsWith("; 3 Maven metadata files ignored"));
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_FEATURES_XML_PARSES).getLevel());
        assertEquals(1, inspection.getFeatureRepositories().size());
    }

    @Test
    public void nonFeaturesXmlIsIgnoredOrWarnsWhenBroken() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0.pom.xml", "<project><artifactId>plugin</artifactId></project>".getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-config.xml", "<config><unclosed>".getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("otherxml.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "otherxml.kar");

        assertTrue(inspection.checksAt(Level.FAIL).isEmpty());
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_FEATURES_XML_PRESENT).getLevel());
        final Check parses = check(inspection, KarInspector.CHECK_FEATURES_XML_PARSES);
        assertEquals(Level.WARN, parses.getLevel());
        assertTrue(parses.getMessage(), parses.getMessage().contains("plugin-1.0.0-config.xml"));
        assertFalse(parses.getMessage(), parses.getMessage().contains("pom.xml"));
        assertEquals(1, inspection.getFeatureRepositories().size());
        assertEquals(2, inspection.getFeatures().size());
    }

    @Test
    public void brokenFeaturesXmlNextToValidOneStillFails() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        entries.put("repository/org/example/extra/1.0.0/extra-1.0.0-features.xml", "<features><feature name='x'>".getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("brokenfeatures.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "brokenfeatures.kar");

        final Check parses = check(inspection, KarInspector.CHECK_FEATURES_XML_PARSES);
        assertEquals(Level.FAIL, parses.getLevel());
        assertTrue(parses.getMessage(), parses.getMessage().contains("extra-1.0.0-features.xml"));
        assertEquals(Level.PASS, check(inspection, KarInspector.CHECK_FEATURES_XML_PRESENT).getLevel());
    }

    @Test
    public void doctypeInFeaturesXmlIsRejected() throws IOException {
        final String xxe = "<?xml version=\"1.0\"?><!DOCTYPE features [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><features name=\"a\"><feature name=\"a\">&x;</feature></features>";
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/a-features.xml", xxe.getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("xxe.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "xxe.kar");

        assertEquals(Level.FAIL, check(inspection, KarInspector.CHECK_FEATURES_XML_PARSES).getLevel());
        assertTrue(inspection.getFeatures().isEmpty());
    }

    @Test
    public void existingDeployedKarWarns() throws IOException {
        Files.write(deployDir.resolve("plugin-1.0.0.kar"), new byte[] { 1, 2, 3 });
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("plugin-1.0.0.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "plugin-1.0.0.kar");

        final Check check = check(inspection, KarInspector.CHECK_DUPLICATE_KAR);
        assertEquals(Level.WARN, check.getLevel());
        assertTrue(check.getMessage(), check.getMessage().contains("plugin-1.0.0.kar"));
    }

    @Test
    public void karNameOverrideAndUploadedFileNameAreHonoured() throws IOException {
        final Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/MANIFEST.MF", manifest("Karaf-Feature-Start", "false"));
        entries.put("repository/org/example/plugin/1.0.0/plugin-1.0.0-features.xml", FEATURES_XML.getBytes(StandardCharsets.UTF_8));
        final Path kar = writeKar("abcdef.kar", entries);

        final KarInspection inspection = inspector.inspect(kar, "My Plugin (1).kar", "renamed");

        assertEquals("renamed", inspection.getKarName());
        assertEquals("My Plugin (1).kar", inspection.getFileName());
    }

    @Test
    public void sanitizesKarNames() {
        assertEquals("My_Plugin__1_", KarInspector.sanitizeKarName("My Plugin (1).kar"));
        assertEquals("evil", KarInspector.sanitizeKarName("../../evil.KAR"));
        assertEquals("plugin", KarInspector.sanitizeKarName(""));
        assertEquals("plugin", KarInspector.sanitizeKarName(null));
        assertEquals("opennms-alec-plugin", KarInspector.sanitizeKarName("/tmp/opennms-alec-plugin.kar"));
        assertEquals("a.b-c_d", KarInspector.sanitizeKarName("a.b-c_d"));
    }

    @Test
    public void entrySafety() {
        assertTrue(KarInspector.isSafeEntry("repository/"));
        assertTrue(KarInspector.isSafeEntry("repository/org/x/y.jar"));
        assertTrue(KarInspector.isSafeEntry("META-INF/MANIFEST.MF"));
        assertFalse(KarInspector.isSafeEntry("resources/etc/x.cfg"));
        assertFalse(KarInspector.isSafeEntry("repository/../etc/x.cfg"));
        assertFalse(KarInspector.isSafeEntry("/repository/x.jar"));
        assertFalse(KarInspector.isSafeEntry("C:/repository/x.jar"));
        assertFalse(KarInspector.isSafeEntry("repositoryx/x.jar"));
        assertFalse(KarInspector.isSafeEntry(""));
    }

    @Test
    public void parsesOsgiHeaders() {
        final Map<String, ImportDescriptor> imports = OsgiHeaders.parseImports("a.b;c.d;version=\"[1,2)\",e.f;resolution:=optional,g.h;version=1.5;resolution:=mandatory,i.j;version=\"1.0\";resolution:=\"optional\"");
        assertEquals("[1,2)", imports.get("a.b").getVersion());
        assertEquals("[1,2)", imports.get("c.d").getVersion());
        assertFalse(imports.get("c.d").isOptional());
        assertEquals("", imports.get("e.f").getVersion());
        assertTrue(imports.get("e.f").isOptional());
        assertEquals("1.5", imports.get("g.h").getVersion());
        assertFalse(imports.get("g.h").isOptional());
        assertEquals("1.0", imports.get("i.j").getVersion());
        assertTrue(imports.get("i.j").isOptional());
        assertEquals(5, imports.size());
        assertEquals("org.example", OsgiHeaders.parseSymbolicName("org.example;singleton:=true"));
        assertEquals("11", OsgiHeaders.parseRequiredJavaVersion("osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=11))\",osgi.service;filter:=\"(objectClass=x)\""));
        assertEquals("1.8", OsgiHeaders.parseRequiredJavaVersion("osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=1.8))\""));
        assertNull(OsgiHeaders.parseRequiredJavaVersion("osgi.service;filter:=\"(objectClass=x)\""));
        assertNull(OsgiHeaders.parseRequiredJavaVersion(null));
        assertEquals(8, OsgiHeaders.javaFeatureVersion("1.8"));
        assertEquals(17, OsgiHeaders.javaFeatureVersion("17"));
        assertEquals(21, OsgiHeaders.javaFeatureVersion("21.0.2"));
        assertEquals(-1, OsgiHeaders.javaFeatureVersion("x"));
    }

    @Test
    public void versionRanges() {
        assertTrue(OsgiHeaders.inRange("[1.0,2)", "1.0.0"));
        assertTrue(OsgiHeaders.inRange("[1.0,2)", "1.9.9.SNAPSHOT"));
        assertFalse(OsgiHeaders.inRange("[1.0,2)", "2.0.0"));
        assertFalse(OsgiHeaders.inRange("(1.0,2]", "1.0.0"));
        assertTrue(OsgiHeaders.inRange("(1.0,2]", "2.0.0"));
        assertTrue(OsgiHeaders.inRange("1.5", "1.5.0"));
        assertTrue(OsgiHeaders.inRange("1.5", "3.0.0"));
        assertFalse(OsgiHeaders.inRange("1.5", "1.4.9"));
        assertTrue(OsgiHeaders.inRange("", "0.0.0"));
        assertTrue(OsgiHeaders.inRange(null, "0.0.0"));
        assertEquals(2, OsgiHeaders.majorVersion("2.0.1"));
        assertEquals(0, OsgiHeaders.compareVersions("1.2.3", "1.2.3"));
        assertTrue(OsgiHeaders.compareVersions("1.2.3", "1.10.0") < 0);
        assertTrue(OsgiHeaders.looksLikeRange("[1,2)"));
        assertFalse(OsgiHeaders.looksLikeRange("1.0.0"));
    }

    // --- fixtures --------------------------------------------------------------

    private Path writeKar(final String name, final Map<String, byte[]> entries) throws IOException {
        final Path kar = folder.getRoot().toPath().resolve(name);
        try (OutputStream out = Files.newOutputStream(kar); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (final Map.Entry<String, byte[]> e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                zip.write(e.getValue());
                zip.closeEntry();
            }
        }
        return kar;
    }

    private static byte[] manifest(final String... keyValues) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            manifest.getMainAttributes().putValue(keyValues[i], keyValues[i + 1]);
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        manifest.write(out);
        return out.toByteArray();
    }

    private static byte[] bundle(final String symbolicName, final String version, final String imports, final String exports, final String requireCapability) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Bundle-ManifestVersion", "2");
        manifest.getMainAttributes().putValue("Bundle-SymbolicName", symbolicName);
        manifest.getMainAttributes().putValue("Bundle-Version", version);
        manifest.getMainAttributes().putValue("Import-Package", imports);
        manifest.getMainAttributes().putValue("Export-Package", exports);
        manifest.getMainAttributes().putValue("Require-Capability", requireCapability);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JarOutputStream jar = new JarOutputStream(out, manifest)) {
            jar.putNextEntry(new ZipEntry("org/example/plugin/Plugin.class"));
            jar.write(new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE });
            jar.closeEntry();
        }
        return out.toByteArray();
    }

    private static byte[] zipWithoutManifest() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("org/example/Foo.class"));
            zip.write(new byte[] { 1 });
            zip.closeEntry();
        }
        return out.toByteArray();
    }

    private static Check check(final KarInspection inspection, final String id) {
        final Check check = find(inspection, id);
        assertNotNull("missing check " + id + " in " + inspection.getChecks(), check);
        return check;
    }

    private static Check find(final KarInspection inspection, final String id) {
        for (final Check c : inspection.getChecks()) {
            if (id.equals(c.getId())) {
                return c;
            }
        }
        return null;
    }
}
