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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

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
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                final String path = file.toString().replace('\\', '/');
                if (!path.endsWith(".java")) {
                    return FileVisitResult.CONTINUE;
                }

                final int sourceRoot = path.lastIndexOf(SOURCE_ROOT);
                if (sourceRoot < 0) {
                    return FileVisitResult.CONTINUE;
                }

                final String relative = path.substring(sourceRoot + SOURCE_ROOT.length());
                if (REST_PACKAGES.stream().noneMatch(relative::startsWith)) {
                    return FileVisitResult.CONTINUE;
                }

                final String module = repoRoot.relativize(Paths.get(path.substring(0, sourceRoot))).toString();
                final String className = relative.substring(0, relative.length() - ".java".length()).replace('/', '.');
                byModule.computeIfAbsent(module, m -> new ArrayList<>()).add(className);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return FileVisitResult.CONTINUE;
            }
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
