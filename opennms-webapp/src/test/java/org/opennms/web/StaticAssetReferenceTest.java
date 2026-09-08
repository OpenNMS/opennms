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
package org.opennms.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Verifies that every statically written reference to a local <code>images/...</code> file in the
 * webapp resolves to a file that is actually present under <code>src/main/webapp</code>.
 *
 * A dangling image reference in a JSP produces no compile error, no build failure and no
 * server-side error -- the page renders with a broken image or a blank background, and the
 * only symptom is a 404 in the browser. That makes it easy to delete an image that is still
 * in use: NMS-20091 removed 61 unreferenced files from <code>images/</code>, and the one
 * mistake it made (a wallpaper whose only consumers were downstream PoweredBy JSPs) went
 * unnoticed until someone looked at a login page. This test turns that class of mistake
 * into a build failure.
 *
 * Note what this deliberately does <em>not</em> do: it never asserts that an image is
 * referenced. Unused files are a cleanup question for a human, not a test failure.
 */
public class StaticAssetReferenceTest {

    /**
     * Surefire runs with the module directory as the working directory, so the webapp
     * sources are addressable as a relative path. NavBarControllerTest relies on the same
     * convention via {@code new MockServletContext("file:src/main/webapp")}.
     */
    private static final Path WEBAPP_ROOT = Path.of("src", "main", "webapp");

    /** File types that can carry a static reference to an image. */
    private static final Set<String> SCANNED_SUFFIXES =
            Set.of(".jsp", ".jspf", ".tag", ".tagx", ".ftl", ".html", ".htm", ".css", ".js");

    /**
     * Matches a whole URL or path token ending in an image extension. The character class
     * excludes quotes, whitespace and CSS delimiters, so a match is naturally bounded by the
     * attribute quoting or the {@code url(...)} parentheses that surround it, and a path
     * assembled at runtime ({@code "images/" + name + ".png"}) does not match at all.
     *
     * <p>Deciding whether a token is a local webapp image is left to
     * {@link #localWebappImageReference(String)}, which inspects the token's path segments.
     * Matching the token first and classifying it second is what keeps an external URL or a
     * directory merely ending in "images" from being mistaken for a local reference.
     */
    private static final Pattern IMAGE_TOKEN = Pattern.compile(
            "[A-Za-z0-9_@.:/+~%-]*\\.(?:png|jpe?g|gif|svg|ico|webp|bmp)", Pattern.CASE_INSENSITIVE);

    /**
     * Guards against the scan silently finding nothing -- a vacuous pass would be worse than
     * no test at all. The real number is comfortably above this; the floor only has to catch
     * a broken walk, a wrong working directory or a pattern that stops matching.
     */
    private static final int MINIMUM_EXPECTED_REFERENCES = 10;

    @Test
    public void everyStaticImageReferenceResolvesToAFile() throws IOException {
        assertTrue(WEBAPP_ROOT.toAbsolutePath() + " does not exist; is the working directory the "
                + "opennms-webapp module root?", Files.isDirectory(WEBAPP_ROOT));

        // Reference -> the files that use it, so a failure says where to look.
        final Map<String, Set<String>> referenceToSources = new TreeMap<>();

        try (Stream<Path> tree = Files.walk(WEBAPP_ROOT)) {
            final List<Path> scannable = tree
                    .filter(Files::isRegularFile)
                    .filter(StaticAssetReferenceTest::isScannable)
                    .sorted()
                    .toList();

            for (final Path source : scannable) {
                final String contents = Files.readString(source, StandardCharsets.UTF_8);
                final Matcher matcher = IMAGE_TOKEN.matcher(contents);
                while (matcher.find()) {
                    final String reference = localWebappImageReference(matcher.group());
                    if (reference != null) {
                        referenceToSources
                                .computeIfAbsent(reference, key -> new TreeSet<>())
                                .add(WEBAPP_ROOT.relativize(source).toString());
                    }
                }
            }
        }

        assertTrue("Found only " + referenceToSources.size() + " image references under "
                        + WEBAPP_ROOT + ", expected at least " + MINIMUM_EXPECTED_REFERENCES
                        + ". The scan is probably broken rather than the webapp.",
                referenceToSources.size() >= MINIMUM_EXPECTED_REFERENCES);

        final StringBuilder failures = new StringBuilder();
        for (final Map.Entry<String, Set<String>> entry : referenceToSources.entrySet()) {
            final String problem = describeIfUnresolvable(entry.getKey());
            if (problem != null) {
                failures.append(System.lineSeparator())
                        .append("  ").append(problem)
                        .append(System.lineSeparator())
                        .append("      referenced by: ").append(String.join(", ", entry.getValue()));
            }
        }

        if (failures.length() > 0) {
            fail("Image references in the webapp that do not resolve to a file under "
                    + WEBAPP_ROOT + ". Restore the file, or update the reference if the image is "
                    + "genuinely gone -- do not leave the reference dangling, because nothing else "
                    + "in the build will catch it:" + failures);
        }
    }

    @Test
    public void recognizesTheReferenceFormsUsedInTheWebapp() {
        // Bare, as written in a page that lives at the webapp root.
        assertEquals("images/foo.png", localWebappImageReference("images/foo.png"));
        // Server-absolute, and the JSTL <c:url value="..."/> form once the tag is stripped.
        assertEquals("images/foo.png", localWebappImageReference("/opennms/images/foo.png"));
        assertEquals("images/foo.png", localWebappImageReference("/images/foo.png"));
        // Relative, as written in a page in a subdirectory.
        assertEquals("images/foo.png", localWebappImageReference("../images/foo.png"));
        // Nested below images/, which is where the wallpapers live.
        assertEquals("images/wallpapers/bar.jpg",
                localWebappImageReference("images/wallpapers/bar.jpg"));
        // Case is preserved so a mismatch against the file on disk is still reportable.
        assertEquals("Images/foo.png", localWebappImageReference("/opennms/Images/foo.png"));
    }

    @Test
    public void ignoresImagesHostedElsewhere() {
        // An external URL cannot be checked against the local filesystem, and its path
        // happening to contain an images/ segment must not be read as a local reference.
        assertNull(localWebappImageReference("https://cdn.example.com/images/x.png"));
        assertNull(localWebappImageReference("http://cdn.example.com/images/x.png"));
        // Protocol-relative.
        assertNull(localWebappImageReference("//cdn.example.com/images/x.png"));
    }

    @Test
    public void ignoresDirectoriesThatMerelyEndInImages() {
        // "images" has to be a whole path segment. A directory whose name ends in it is a
        // different directory, and resolving these against images/ would invent a file that
        // was never referenced.
        assertNull(localWebappImageReference("/opennms/custom-images/logo.png"));
        assertNull(localWebappImageReference("myimages/logo.png"));
        assertNull(localWebappImageReference("theme_images/logo.png"));
    }

    @Test
    public void ignoresWebAssetsImages() {
        // core/web-assets is served from a different root and is already checked at build
        // time: webpack's file-loader fails the bundle on an unresolvable url().
        assertNull(localWebappImageReference("assets/images/foo.png"));
        assertNull(localWebappImageReference("/opennms/assets/images/foo.png"));
    }

    @Test
    public void ignoresTokensThatAreNotImagePathsAtAll() {
        assertNull(localWebappImageReference("foo.png"));
        assertNull(localWebappImageReference("graph/graph.png"));
        // images/ with nothing after it is not a reference to a file.
        assertNull(localWebappImageReference("images/"));
    }

    private static boolean isScannable(final Path path) {
        final String name = path.getFileName().toString().toLowerCase();
        return SCANNED_SUFFIXES.stream().anyMatch(name::endsWith);
    }

    /**
     * Classifies a matched URL or path token and reduces it to a path relative to the webapp
     * root. References are anchored on the {@code images/} segment, which is how they are
     * served ({@code /opennms/images/...}) regardless of where the referring page sits.
     *
     * @return the reference relative to the webapp root, or null when the token is not a
     *         reference to a local webapp image and therefore cannot be checked here.
     */
    static String localWebappImageReference(final String token) {
        // Anything with a scheme, or protocol-relative, is served by another host.
        if (token.contains("://") || token.startsWith("//")) {
            return null;
        }

        final String[] segments = token.replaceAll("/{2,}", "/").split("/");
        // Stop before the last segment: "images" must be a directory, with a file after it.
        for (int i = 0; i < segments.length - 1; i++) {
            if (!"images".equalsIgnoreCase(segments[i])) {
                continue;
            }
            if (i > 0 && "assets".equalsIgnoreCase(segments[i - 1])) {
                return null;
            }
            return String.join("/", Arrays.copyOfRange(segments, i, segments.length));
        }
        return null;
    }

    /**
     * @return a description of why the reference does not resolve, or null when it is fine.
     */
    private static String describeIfUnresolvable(final String reference) {
        final Path target = WEBAPP_ROOT.resolve(reference);
        if (!Files.isRegularFile(target)) {
            return reference + " -- no such file";
        }
        // macOS and Windows resolve paths case-insensitively while the deployment target does
        // not, so a reference that only differs in case works for the developer who wrote it
        // and 404s in production. Compare against the name as it is actually spelled on disk.
        final String onDisk = actualCaseOf(target);
        if (onDisk != null && !onDisk.equals(reference)) {
            return reference + " -- case does not match the file on disk, which is " + onDisk;
        }
        return null;
    }

    /**
     * @return the reference spelled with the case each path segment actually has on disk, or
     *         null if that cannot be determined.
     */
    private static String actualCaseOf(final Path target) {
        try {
            final Path real = target.toRealPath();
            final Path realRoot = WEBAPP_ROOT.toRealPath();
            if (!real.startsWith(realRoot)) {
                // A symlink pointing outside the webapp; existence is all we can reasonably assert.
                return null;
            }
            return realRoot.relativize(real).toString().replace(File.separatorChar, '/');
        } catch (final IOException e) {
            return null;
        }
    }
}
