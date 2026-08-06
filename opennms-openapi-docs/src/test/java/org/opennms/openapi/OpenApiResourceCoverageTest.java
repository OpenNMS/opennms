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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * Walks the source tree for JAX-RS root resources and checks each one is on this
 * module's classpath.
 *
 * A module that contributes to org.opennms.web.rest.v1 or .v2 and is not a
 * dependency here still generates a valid document, just one that is missing
 * those endpoints. Nothing else notices.
 */
public class OpenApiResourceCoverageTest {

    private static final Path REPO_ROOT = Paths.get("..").toAbsolutePath().normalize();

    private static final Set<String> PRUNED = Set.of("target", ".git", "node_modules", ".claude");

    /** The generator only registers resources in these packages and below. */
    private static final Pattern REST_PACKAGE =
            Pattern.compile("^\\s*package\\s+(org\\.opennms\\.web\\.rest\\.v[12](?:\\.[\\w.]+)?)\\s*;", Pattern.MULTILINE);

    /** Guards against a parser change quietly reducing this to a no-op. */
    private static final int EXPECTED_AT_LEAST = 50;

    @Test
    public void everyRootResourceIsOnTheClasspath() throws Exception {
        final TreeMap<String, String> missing = new TreeMap<>();
        int rootResources = 0;

        for (final Path source : findRestSources()) {
            final String className = rootResourceClassName(source);
            if (className == null) {
                continue;
            }

            rootResources++;
            try {
                Class.forName(className, false, getClass().getClassLoader());
            } catch (final ClassNotFoundException e) {
                missing.put(className, moduleOf(source));
            }
        }

        assertTrue("found only " + rootResources + " root resources under " + REPO_ROOT
                        + "; the source scan is broken, not the classpath",
                rootResources >= EXPECTED_AT_LEAST);

        assertTrue("these ReST resources are not on this module's classpath, so their endpoints are"
                        + " missing from the generated documents. Add the module as a provided"
                        + " dependency in opennms-openapi-docs/pom.xml:\n"
                        + missing.entrySet().stream()
                                .map(e -> "  " + e.getKey() + " (" + e.getValue() + ")")
                                .collect(Collectors.joining("\n")),
                missing.isEmpty());
    }

    private static List<Path> findRestSources() throws IOException {
        final List<Path> sources = new ArrayList<>();

        Files.walkFileTree(REPO_ROOT, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                return PRUNED.contains(dir.getFileName().toString())
                        ? FileVisitResult.SKIP_SUBTREE
                        : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                final String path = file.toString().replace('\\', '/');
                if (path.endsWith(".java") && path.contains("/src/main/java/org/opennms/web/rest/")) {
                    sources.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });

        return sources;
    }

    /**
     * Returns the class name when the file declares a concrete root resource, meaning
     * a class carrying a type-level {@code @Path}. Sub-resources reached from a
     * locator method carry {@code @Path} only on their methods and are never
     * registered on their own, so they must not be counted.
     */
    private static String rootResourceClassName(final Path source) throws IOException {
        final String fileName = source.getFileName().toString();
        final String simpleName = fileName.substring(0, fileName.length() - ".java".length());
        final String content = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);

        final Matcher packageMatcher = REST_PACKAGE.matcher(content);
        if (!packageMatcher.find()) {
            return null;
        }

        final Matcher declaration = Pattern.compile(
                "^[^\\S\\n]*(?:public\\s+)?(?:(abstract|final)\\s+)?(class|interface|enum)\\s+" + Pattern.quote(simpleName) + "\\b",
                Pattern.MULTILINE).matcher(content);
        if (!declaration.find()) {
            return null;
        }
        if (!"class".equals(declaration.group(2)) || "abstract".equals(declaration.group(1))) {
            return null;
        }

        return annotatedWithPath(content.substring(0, declaration.start()))
                ? packageMatcher.group(1) + "." + simpleName
                : null;
    }

    /** Only the annotation block directly above the declaration counts, not imports or javadoc. */
    private static boolean annotatedWithPath(final String beforeDeclaration) {
        final String[] lines = beforeDeclaration.split("\n");

        for (int i = lines.length - 1; i >= 0; i--) {
            final String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("*") || line.startsWith("/*") || line.startsWith("//")) {
                continue;
            }
            if (!line.startsWith("@")) {
                return false;
            }
            if (line.startsWith("@Path")) {
                return true;
            }
        }

        return false;
    }

    private static String moduleOf(final Path source) {
        for (Path dir = source.getParent(); dir != null && dir.startsWith(REPO_ROOT); dir = dir.getParent()) {
            if (Files.exists(dir.resolve("pom.xml"))) {
                return REPO_ROOT.relativize(dir).toString();
            }
        }
        return "unknown module";
    }
}
