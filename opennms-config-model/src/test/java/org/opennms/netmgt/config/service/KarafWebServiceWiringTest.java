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
package org.opennms.netmgt.config.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/** Guards against making a Karaf bundle depend on a service owned by the web application. */
public class KarafWebServiceWiringTest {
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Pattern XML_COMMENT = Pattern.compile("<!--[\\s\\S]*?-->");
    private static final Pattern BLUEPRINT_REFERENCE = Pattern.compile("<reference\\b([^>]*)>");
    private static final Pattern BLUEPRINT_SERVICE = Pattern.compile("<service\\b([^>]*)>");
    private static final Pattern ONMS_SERVICE = Pattern.compile("<onmsgi:service\\b([^>]*)>");
    private static final Pattern ONMS_SERVICE_INTERFACES = Pattern.compile(
            "<onmsgi:service\\b[^>]*>[\\s\\S]*?<onmsgi:interfaces>([\\s\\S]*?)</onmsgi:interfaces>[\\s\\S]*?</onmsgi:service>");
    private static final Pattern INTERFACE_VALUE = Pattern.compile("<value>\\s*([^<\\s]+)\\s*</value>");
    private static final Pattern CONTEXT = Pattern.compile(
            "<bean\\b([^>]*class=[\"']org\\.springframework\\.context\\.support\\.ClassPathXmlApplicationContext[\"'][^>]*)>([\\s\\S]*?)</bean>");
    private static final Pattern CONTEXT_RESOURCE = Pattern.compile("<value>\\s*([^<]+?)\\s*</value>");
    private static final Pattern CONTEXT_PARENT = Pattern.compile("<constructor-arg\\s+ref=[\"']([^\"']+)[\"']");
    private static final Pattern WEB_CLASSPATH_RESOURCE = Pattern.compile("classpath\\*?:/?([^\\s<]+\\.xml)");

    @Test
    public void mandatoryBlueprintReferencesAreNotProvidedOnlyByTheWebApplication() throws IOException {
        final List<Path> xmlFiles = sourceXmlFiles();
        final Set<String> webResources = webClasspathResources(xmlFiles);
        final Map<String, ContextDefinition> contexts = contextDefinitions(xmlFiles);
        final Set<String> daemonResources = daemonResources(contexts);

        final Map<String, Set<Path>> mandatoryReferences = new LinkedHashMap<>();
        final Map<String, Set<Path>> springProviders = new LinkedHashMap<>();
        final Map<String, Set<Path>> blueprintProviders = new LinkedHashMap<>();
        final Map<String, Set<Path>> webProviders = new LinkedHashMap<>();

        for (Path xmlFile : xmlFiles) {
            final String xml = withoutComments(Files.readString(xmlFile));
            if (xml.contains("<blueprint")) {
                collectBlueprintReferences(xmlFile, xml, mandatoryReferences);
                collectAttributeValues(xmlFile, xml, BLUEPRINT_SERVICE, "interface", blueprintProviders);
            }

            final Set<String> exportedInterfaces = new LinkedHashSet<>();
            collectAttributeValues(xml, ONMS_SERVICE, "interface", exportedInterfaces);
            collectNestedServiceInterfaces(xml, exportedInterfaces);
            for (String exportedInterface : exportedInterfaces) {
                add(springProviders, exportedInterface, xmlFile);
                if (isWebLoaded(xmlFile, webResources) && !isDaemonLoaded(xmlFile, daemonResources)) {
                    add(webProviders, exportedInterface, xmlFile);
                }
            }
        }

        final List<String> violations = new ArrayList<>();
        for (Map.Entry<String, Set<Path>> entry : mandatoryReferences.entrySet()) {
            final String serviceInterface = entry.getKey();
            final Set<Path> providersLoadedOnlyByWeb = webProviders.get(serviceInterface);
            if (providersLoadedOnlyByWeb == null) {
                continue;
            }

            final Set<Path> otherSpringProviders = new HashSet<>(springProviders.get(serviceInterface));
            otherSpringProviders.removeAll(providersLoadedOnlyByWeb);
            if (otherSpringProviders.isEmpty() && !blueprintProviders.containsKey(serviceInterface)) {
                violations.add(serviceInterface
                        + "\n  mandatory references: " + relativePaths(entry.getValue())
                        + "\n  web-only providers: " + relativePaths(providersLoadedOnlyByWeb));
            }
        }

        if (!violations.isEmpty()) {
            fail("Mandatory Blueprint services must not be supplied only by the web application:\n"
                    + String.join("\n", violations));
        }
    }

    @Test
    public void webAndKarafUseTheSharedDaoContextDirectly() throws IOException {
        final Map<String, ContextDefinition> contexts = contextDefinitions(sourceXmlFiles());

        assertEquals("daoContext", contexts.get("karafDaemonContext").parent);
        assertEquals("daoContext", contexts.get("webContext").parent);
        assertFalse(contexts.containsKey("timeformatContext"));
        assertFalse(contexts.containsKey("measurementsContext"));
    }

    private static void collectBlueprintReferences(final Path file, final String xml,
            final Map<String, Set<Path>> references) {
        final Matcher matcher = BLUEPRINT_REFERENCE.matcher(xml);
        while (matcher.find()) {
            final String serviceInterface = attribute(matcher.group(1), "interface");
            final String availability = attribute(matcher.group(1), "availability");
            if (serviceInterface != null && (availability == null || "mandatory".equals(availability))) {
                add(references, serviceInterface, file);
            }
        }
    }

    private static void collectAttributeValues(final Path file, final String xml, final Pattern element,
            final String attribute, final Map<String, Set<Path>> values) {
        final Set<String> found = new LinkedHashSet<>();
        collectAttributeValues(xml, element, attribute, found);
        for (String value : found) {
            add(values, value, file);
        }
    }

    private static void collectAttributeValues(final String xml, final Pattern element,
            final String attribute, final Set<String> values) {
        final Matcher matcher = element.matcher(xml);
        while (matcher.find()) {
            final String value = attribute(matcher.group(1), attribute);
            if (value != null) {
                values.add(value);
            }
        }
    }

    private static void collectNestedServiceInterfaces(final String xml, final Set<String> values) {
        final Matcher serviceMatcher = ONMS_SERVICE_INTERFACES.matcher(xml);
        while (serviceMatcher.find()) {
            final Matcher valueMatcher = INTERFACE_VALUE.matcher(serviceMatcher.group(1));
            while (valueMatcher.find()) {
                values.add(valueMatcher.group(1));
            }
        }
    }

    private static Map<String, ContextDefinition> contextDefinitions(final List<Path> xmlFiles) throws IOException {
        final Map<String, ContextDefinition> contexts = new HashMap<>();
        for (Path xmlFile : xmlFiles) {
            if (!"beanRefContext.xml".equals(xmlFile.getFileName().toString())) {
                continue;
            }
            final String xml = withoutComments(Files.readString(xmlFile));
            final Matcher contextMatcher = CONTEXT.matcher(xml);
            while (contextMatcher.find()) {
                final String id = attribute(contextMatcher.group(1), "id");
                final List<String> resources = new ArrayList<>();
                final Matcher resourceMatcher = CONTEXT_RESOURCE.matcher(contextMatcher.group(2));
                while (resourceMatcher.find()) {
                    resources.add(normalizeResource(resourceMatcher.group(1)));
                }
                String parent = null;
                final Matcher parentMatcher = CONTEXT_PARENT.matcher(contextMatcher.group(2));
                while (parentMatcher.find()) {
                    parent = parentMatcher.group(1);
                }
                contexts.put(id, new ContextDefinition(parent, resources));
            }
        }
        return contexts;
    }

    private static Set<String> daemonResources(final Map<String, ContextDefinition> contexts) {
        final Set<String> resources = new LinkedHashSet<>();
        final Set<String> visited = new HashSet<>();
        String contextId = "karafDaemonContext";
        while (contextId != null && visited.add(contextId)) {
            final ContextDefinition context = contexts.get(contextId);
            if (context == null) {
                break;
            }
            resources.addAll(context.resources);
            contextId = context.parent;
        }
        return resources;
    }

    private static Set<String> webClasspathResources(final List<Path> xmlFiles) throws IOException {
        final Set<String> resources = new LinkedHashSet<>();
        for (Path xmlFile : xmlFiles) {
            final String relativePath = relativePath(xmlFile);
            if (!relativePath.matches("opennms-webapp(?:-rest)?/src/main/webapp/WEB-INF/web\\.xml")) {
                continue;
            }
            final Matcher matcher = WEB_CLASSPATH_RESOURCE.matcher(withoutComments(Files.readString(xmlFile)));
            while (matcher.find()) {
                resources.add(normalizeResource(matcher.group(1)));
            }
        }
        return resources;
    }

    private static boolean isWebLoaded(final Path file, final Set<String> webResources) {
        final String relativePath = relativePath(file);
        if (relativePath.startsWith("opennms-webapp/src/main/")
                || relativePath.startsWith("opennms-webapp-rest/src/main/")) {
            return true;
        }
        return webResources.stream().anyMatch(resource -> sourcePathEndsWith(relativePath, resource));
    }

    private static boolean isDaemonLoaded(final Path file, final Set<String> daemonResources) {
        final String relativePath = relativePath(file);
        return daemonResources.stream().anyMatch(resource -> sourcePathEndsWith(relativePath, resource));
    }

    private static boolean sourcePathEndsWith(final String sourcePath, final String resource) {
        return sourcePath.endsWith("/src/main/resources/" + resource)
                || sourcePath.endsWith("/src/main/webapp/" + resource);
    }

    private static List<Path> sourceXmlFiles() throws IOException {
        final List<Path> files = new ArrayList<>();
        Files.walkFileTree(REPO_ROOT, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) {
                final String name = directory.getFileName() == null ? "" : directory.getFileName().toString();
                if (Set.of(".git", "target", "node_modules").contains(name)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
                final String relativePath = relativePath(file);
                if (relativePath.contains("/src/main/") && relativePath.endsWith(".xml")) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    private static String attribute(final String attributes, final String name) {
        final Matcher matcher = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*=\\s*[\"']([^\"']+)[\"']")
                .matcher(attributes);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String withoutComments(final String xml) {
        return XML_COMMENT.matcher(xml).replaceAll("");
    }

    private static String normalizeResource(final String resource) {
        return resource.replaceFirst("^classpath\\*?:/?", "").replaceFirst("^/", "");
    }

    private static void add(final Map<String, Set<Path>> values, final String key, final Path file) {
        values.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(file);
    }

    private static String relativePaths(final Set<Path> paths) {
        return paths.stream().map(KarafWebServiceWiringTest::relativePath).sorted().toList().toString();
    }

    private static String relativePath(final Path file) {
        return REPO_ROOT.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    private static final class ContextDefinition {
        private final String parent;
        private final List<String> resources;

        private ContextDefinition(final String parent, final List<String> resources) {
            this.parent = parent;
            this.resources = resources;
        }
    }
}
