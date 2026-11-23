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
package org.opennms.openapi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Fails when a module contributing ReST resources is not a dependency of this one.
 *
 * Such a module still produces a valid document, just one missing its endpoints,
 * and nothing else notices.
 *
 * Checked per module rather than per resource class: {@code @Path} is often
 * inherited from an interface in org.opennms.web.rest.v2.api, so reading it off
 * the class declaration in source misses those and understates the coverage.
 */
public class OpenApiResourceCoverageTest {

    /** Overrides the search below; the build does not need to set it. */
    private static final String REPO_ROOT_PROPERTY = "opennms.repoRoot";

    private static final String SOURCE_ROOT = "/src/main/java/";

    private static final List<String> REST_PACKAGES =
            List.of("org/opennms/web/rest/v1/", "org/opennms/web/rest/v2/");

    private static final Set<String> PRUNED = Set.of("target", "node_modules");

    /**
     * Modules whose ReST resources are deliberately not part of the webapp, so their
     * absence from the documents is intended. Empty today.
     */
    private static final Set<String> NOT_IN_THE_WEBAPP = Set.of();

    private static final String BLUEPRINT_NS = "http://www.osgi.org/xmlns/blueprint/v1.0.0";

    private static final String BLUEPRINT_DIR = "/src/main/resources/OSGI-INF/blueprint/";

    /** The Blueprint application-path each document covers. */
    private static final Map<String, OpenApiDocGenerator.Api> API_BY_APPLICATION_PATH = Map.of(
            "/rest", OpenApiDocGenerator.Api.V1,
            "/api/v2", OpenApiDocGenerator.Api.V2);

    /**
     * OSGi ReST services that are published but not yet in OpenApiDocGenerator.Api. When you
     * document one, move it from here to the Api whose path matches its application-path.
     */
    private static final Set<String> NOT_YET_DOCUMENTED = Set.of(
            "org.opennms.core.health.rest.HealthCheckRestService",
            "org.opennms.features.alarms.history.rest.api.AlarmHistoryRestService",
            "org.opennms.features.config.rest.api.ConfigManagerRestService",
            "org.opennms.features.datachoices.web.DataChoiceRestService",
            "org.opennms.features.geocoder.rest.GeocodingRestService",
            "org.opennms.features.reporting.rest.ReportRestService",
            "org.opennms.features.scv.rest.ScvRestService",
            "org.opennms.features.situationfeedback.rest.SituationFeedbackRestService",
            "org.opennms.features.uiextension.api.UIExtensionService",
            "org.opennms.features.zenithconnect.rest.api.ZenithConnectRestService",
            "org.opennms.netmgt.endpoints.grafana.rest.GrafanaEndpointRestService",
            "org.opennms.netmgt.flows.rest.FlowRestService",
            "org.opennms.netmgt.flows.rest.classification.ClassificationRestService",
            "org.opennms.netmgt.graph.rest.api.GraphRestService",
            "org.opennms.netmgt.graph.rest.api.GraphSearchRestService",
            "org.opennms.netmgt.graphml.rest.GraphmlRestService",
            "org.opennms.netmgt.ha.rest.HaRestService",
            "org.opennms.netmgt.search.rest.SearchRestService");

    @Test
    public void everyRestModuleIsOnTheClasspath() throws IOException {
        final Path repoRoot = repoRoot();
        final Map<String, List<String>> restClassesByModule = restClassesByModule(repoRoot);

        assertFalse("no ReST sources found under " + repoRoot + "; the scan is broken, not the classpath",
                restClassesByModule.isEmpty());

        final List<String> missing = restClassesByModule.entrySet().stream()
                .filter(module -> !NOT_IN_THE_WEBAPP.contains(module.getKey()))
                .filter(module -> module.getValue().stream().noneMatch(OpenApiResourceCoverageTest::isOnClasspath))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        assertTrue("these modules contribute ReST resources but are not on this module's classpath,"
                        + " so their endpoints are missing from the generated documents. Add each as a"
                        + " provided dependency in opennms-openapi-docs/pom.xml, or list it in"
                        + " NOT_IN_THE_WEBAPP if it is deliberately not deployed there:\n"
                        + missing.stream().map(m -> "  " + m).collect(Collectors.joining("\n")),
                missing.isEmpty());
    }

    /**
     * Each Blueprint service with an application-path and a type-level @Path must be listed under
     * the Api for that path, or be in NOT_YET_DOCUMENTED. Each listed class and each
     * NOT_YET_DOCUMENTED entry must still be published there.
     */
    @Test
    public void everyOsgiRestServiceIsListedUnderItsApi() throws Exception {
        final Path repoRoot = repoRoot();
        final Map<String, String> published = publishedOsgiResources(repoRoot);

        assertFalse("no Blueprint ReST services found under " + repoRoot + "; the scan is broken",
                published.isEmpty());

        final Map<String, OpenApiDocGenerator.Api> listed = new HashMap<>();
        for (final OpenApiDocGenerator.Api api : OpenApiDocGenerator.Api.values()) {
            api.getOsgiResourceClasses().forEach(clazz -> listed.put(clazz.getName(), api));
        }

        final List<String> errors = new ArrayList<>();
        published.forEach((service, applicationPath) -> {
            final OpenApiDocGenerator.Api expected = API_BY_APPLICATION_PATH.get(applicationPath);
            if (expected == null) {
                errors.add(service + " is published at application-path " + applicationPath
                        + ", which no OpenAPI document covers");
            } else if (listed.containsKey(service)) {
                if (listed.get(service) != expected) {
                    errors.add(service + " is listed under Api." + listed.get(service) + ", but its Blueprint"
                            + " publishes it at " + applicationPath + ", so it belongs under Api." + expected);
                }
                if (NOT_YET_DOCUMENTED.contains(service)) {
                    errors.add(service + " is listed in an Api, so remove it from NOT_YET_DOCUMENTED");
                }
            } else if (!NOT_YET_DOCUMENTED.contains(service)) {
                errors.add(service + " is published at " + applicationPath + " but is not in the OpenAPI"
                        + " documents. Add it to Api." + expected + " in OpenApiDocGenerator, or to"
                        + " NOT_YET_DOCUMENTED in this test");
            }
        });
        listed.keySet().stream()
                .filter(service -> !published.containsKey(service))
                .forEach(service -> errors.add(service + " is listed in OpenApiDocGenerator.Api, but no"
                        + " Blueprint publishes it with an application-path"));
        NOT_YET_DOCUMENTED.stream()
                .filter(service -> !published.containsKey(service))
                .forEach(service -> errors.add(service + " is in NOT_YET_DOCUMENTED, but no Blueprint"
                        + " publishes it with an application-path; remove it"));

        assertTrue("the OSGi ReST services and OpenApiDocGenerator.Api do not agree:\n"
                        + errors.stream().sorted().map(e -> "  " + e).collect(Collectors.joining("\n")),
                errors.isEmpty());
    }

    /** Maps each OSGi ReST service with a type-level @Path to its application-path. */
    private static Map<String, String> publishedOsgiResources(final Path repoRoot) throws Exception {
        final List<Path> blueprints = new ArrayList<>();
        final Map<String, Path> sources = new HashMap<>();
        walk(repoRoot, file -> {
            final String path = file.toString().replace('\\', '/');
            if (path.endsWith(".xml") && path.contains(BLUEPRINT_DIR)) {
                blueprints.add(file);
            } else if (path.endsWith(".java")) {
                final int sourceRoot = path.lastIndexOf(SOURCE_ROOT);
                if (sourceRoot >= 0) {
                    final String relative = path.substring(sourceRoot + SOURCE_ROOT.length());
                    sources.put(relative.substring(0, relative.length() - ".java".length()).replace('/', '.'), file);
                }
            }
        });

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Map<String, String> published = new TreeMap<>();
        for (final Path blueprint : blueprints) {
            final NodeList services = builder.parse(blueprint.toFile()).getElementsByTagNameNS(BLUEPRINT_NS, "service");
            for (int i = 0; i < services.getLength(); i++) {
                final Element service = (Element) services.item(i);
                final String applicationPath = applicationPath(service);
                final String type = service.getAttribute("interface");
                if (applicationPath == null || type.isEmpty()) {
                    continue;
                }
                final Path source = sources.get(type);
                if (source == null) {
                    throw new IllegalStateException(blueprint + " publishes " + type + " at " + applicationPath
                            + ", but its source is not in this repository");
                }
                if (hasTypeLevelPath(source, type)) {
                    published.put(type, applicationPath);
                }
            }
        }
        return published;
    }

    private static String applicationPath(final Element service) {
        final NodeList entries = service.getElementsByTagNameNS(BLUEPRINT_NS, "entry");
        for (int i = 0; i < entries.getLength(); i++) {
            final Element entry = (Element) entries.item(i);
            if ("application-path".equals(entry.getAttribute("key"))) {
                return entry.getAttribute("value");
            }
        }
        return null;
    }

    /** True if a @Path annotation comes before the declaration of the type. */
    private static boolean hasTypeLevelPath(final Path source, final String type) throws IOException {
        final String text = Files.readString(source);
        final String simpleName = type.substring(type.lastIndexOf('.') + 1);
        final Matcher declaration = Pattern.compile("(?m)^\\s*public\\s+(?:abstract\\s+)?(?:interface|class)\\s+"
                + Pattern.quote(simpleName) + "\\b").matcher(text);
        if (!declaration.find()) {
            throw new IllegalStateException("no declaration of " + simpleName + " in " + source);
        }
        return Pattern.compile("(?m)^\\s*@Path\\(").matcher(text.substring(0, declaration.start())).find();
    }

    private interface FileVisitor {
        void visit(Path file) throws IOException;
    }

    private static void walk(final Path repoRoot, final FileVisitor visitor) throws IOException {
        Files.walkFileTree(repoRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                final String name = dir.getFileName().toString();
                // Dot directories cover .git and any nested checkout a developer keeps in the tree.
                return PRUNED.contains(name) || (name.startsWith(".") && !dir.equals(repoRoot))
                        ? FileVisitResult.SKIP_SUBTREE
                        : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                visitor.visit(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean isOnClasspath(final String className) {
        try {
            Class.forName(className, false, OpenApiResourceCoverageTest.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    /** Maps each module holding ReST sources to the classes it contributes. */
    private static Map<String, List<String>> restClassesByModule(final Path repoRoot) throws IOException {
        final Map<String, List<String>> byModule = new TreeMap<>();

        walk(repoRoot, file -> {
            final String path = file.toString().replace('\\', '/');
            if (!path.endsWith(".java")) {
                return;
            }

            final int sourceRoot = path.lastIndexOf(SOURCE_ROOT);
            if (sourceRoot < 0) {
                return;
            }

            final String relative = path.substring(sourceRoot + SOURCE_ROOT.length());
            if (REST_PACKAGES.stream().noneMatch(relative::startsWith)) {
                return;
            }

            final String module = repoRoot.relativize(Paths.get(path.substring(0, sourceRoot))).toString();
            final String className = relative.substring(0, relative.length() - ".java".length()).replace('/', '.');
            byModule.computeIfAbsent(module, m -> new ArrayList<>()).add(className);
        });

        return byModule;
    }

    /**
     * Walks up for the marker rather than assuming a fixed depth, so moving this
     * module does not quietly break the scan.
     */
    private static Path repoRoot() {
        final String configured = System.getProperty(REPO_ROOT_PROPERTY);
        if (configured != null && !configured.isBlank() && !configured.startsWith("${")) {
            final Path candidate = Paths.get(configured).toAbsolutePath().normalize();
            if (isRepoRoot(candidate)) {
                return candidate;
            }
        }

        for (Path dir = Paths.get("").toAbsolutePath().normalize(); dir != null; dir = dir.getParent()) {
            if (isRepoRoot(dir)) {
                return dir;
            }
        }

        throw new IllegalStateException("no repository root above " + Paths.get("").toAbsolutePath()
                + "; pass -D" + REPO_ROOT_PROPERTY + "=<path>");
    }

    private static boolean isRepoRoot(final Path dir) {
        return Files.isRegularFile(dir.resolve("compile.pl")) && Files.isRegularFile(dir.resolve("pom.xml"));
    }
}
