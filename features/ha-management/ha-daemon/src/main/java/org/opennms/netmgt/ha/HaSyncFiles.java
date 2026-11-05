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
package org.opennms.netmgt.ha;

import org.opennms.core.utils.SystemInfoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shared helpers for HA config sync: manifest building, hashing, path
 * safety, and the exclusion rules. Used by the REST endpoints on the ACTIVE
 * node (to serve the manifest and files) and by {@link HaConfigSyncer} on the
 * standby (to diff against local state) — one implementation, so both sides
 * always agree on what is in scope.
 *
 * <p>Manifest wire format is deliberately plain text: {@code #exclude <pattern>}
 * header lines advertising the serving node's exclusions, then one file per
 * line as {@code <sha256> <size> <relative-path>} — binary-safe transfer
 * happens on the separate per-file endpoint, and the standby can parse this
 * without any JSON machinery.
 */
public final class HaSyncFiles {

    /** Files that are never synced regardless of configuration: each node's
     * own HA identity, and transient artifacts. */
    private static final List<String> BUILTIN_EXCLUSIONS =
            List.of("ha-configuration.xml", "examples/");

    private HaSyncFiles() {}

    /** A file in the sync scope. {@code root} is the sync root it belongs to
     * ("etc", "deploy"); {@code relativePath} is relative to that root. */
    public record Entry(String root, String relativePath, String sha256, long size) {}

    /** The only root synced unless the operator adds more. */
    public static final String DEFAULT_ROOT = "etc";

    public static Path home() {
        return Paths.get(System.getProperty("opennms.home", ".")).toAbsolutePath().normalize();
    }

    /** Resolves a sync root under {@code $OPENNMS_HOME}, rejecting anything
     * that is not a plain directory name directly beneath it. */
    public static Path root(String name) throws IOException {
        Path h = home();
        Path r = h.resolve(name).normalize();
        if (!r.getParent().equals(h)) {
            throw new IOException("sync root must be a directory directly under the OpenNMS home: " + name);
        }
        return r;
    }

    public static Path etcRoot() {
        return home().resolve(DEFAULT_ROOT);
    }

    /** True if {@code relativePath} is excluded from sync (builtin rules plus
     * the operator-extensible list; a trailing "/" excludes a subtree). The
     * path is canonicalized first so a non-canonical spelling such as
     * {@code subdir/../ha-configuration.xml} cannot slip past a rule. */
    public static boolean isExcluded(String relativePath, List<String> configuredExcludes) {
        String normalized = normalizeRelative(relativePath);
        List<String> all = new ArrayList<>(BUILTIN_EXCLUSIONS);
        if (configuredExcludes != null) {
            all.addAll(configuredExcludes);
        }
        return all.stream().anyMatch(e ->
                e.endsWith("/") ? normalized.startsWith(e) : normalized.equals(e));
    }

    /** Canonical, forward-slash form of a manifest-relative path ({@code ./},
     * {@code ..} segments collapsed). Escapes are not decided here — that is
     * {@link #resolveSafe}'s job. */
    static String normalizeRelative(String relativePath) {
        return Paths.get(relativePath).normalize().toString().replace('\\', '/');
    }

    /**
     * Resolves a manifest-relative path inside {@code root}, rejecting
     * anything that escapes it: absolute paths, {@code ..} traversal, and
     * symlinks that point outside the root.
     */
    public static Path resolveSafe(Path root, String relativePath) throws IOException {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IOException("path escapes sync root: " + relativePath);
        }
        // Lexical containment is not enough — a symlink inside the root can
        // point anywhere. Verify the deepest existing path element's real
        // location is still inside the root (the tail may not exist yet when
        // resolving a fetch target).
        Path realRoot = root.toRealPath();
        Path existing = resolved;
        while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
            existing = existing.getParent();
        }
        if (existing != null && !existing.toRealPath().startsWith(realRoot)) {
            throw new IOException("path escapes sync root via symlink: " + relativePath);
        }
        return resolved;
    }

    /** Walks {@code root} and builds manifest entries for every regular,
     * non-excluded file. Symlinks are skipped: they are never advertised,
     * served, or deleted — their targets live outside the sync contract. */
    public static List<Entry> buildManifest(List<String> roots, List<String> configuredExcludes) throws IOException {
        List<Entry> entries = new ArrayList<>();
        for (String rootName : roots) {
            Path root = root(rootName);
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(root)) {
                for (Path p : (Iterable<Path>) walk::iterator) {
                    if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                        continue;
                    }
                    String rel = root.relativize(p).toString().replace('\\', '/');
                    if (isExcluded(rel, configuredExcludes)) {
                        continue;
                    }
                    entries.add(new Entry(rootName, rel, sha256(p), Files.size(p)));
                }
            }
        }
        return entries;
    }

    /** Header line prefix advertising one of the serving node's exclusion
     * patterns. The standby unions these with its own list for deletion
     * propagation: a path missing from the manifest only means "deleted on
     * the active" when neither side excludes it. */
    static final String EXCLUDE_HEADER_PREFIX = "#exclude ";

    /** Advertises the serving node's release, so a standby can refuse to sync
     * configuration from a different version. */
    static final String VERSION_HEADER_PREFIX = "#version ";

    /** Introduces the entries belonging to one sync root. */
    static final String ROOT_HEADER_PREFIX = "#root ";

    /** First line of every manifest. Entry lines are permissive enough that
     * arbitrary text can parse as one, so a response without this marker is
     * never treated as a manifest — least of all as authority to delete. */
    public static final String MANIFEST_MARKER = "#ha-manifest 1";

    public static String toManifestText(List<Entry> entries, List<String> configuredExcludes, String version) {
        StringBuilder sb = new StringBuilder();
        sb.append(MANIFEST_MARKER).append('\n');
        if (version != null) {
            sb.append(VERSION_HEADER_PREFIX).append(version).append('\n');
        }
        List<String> all = new ArrayList<>(BUILTIN_EXCLUSIONS);
        if (configuredExcludes != null) {
            all.addAll(configuredExcludes);
        }
        for (String exclude : all) {
            sb.append(EXCLUDE_HEADER_PREFIX).append(exclude).append('\n');
        }
        String currentRoot = null;
        for (Entry e : entries) {
            if (!e.root().equals(currentRoot)) {
                currentRoot = e.root();
                sb.append(ROOT_HEADER_PREFIX).append(currentRoot).append('\n');
            }
            sb.append(e.sha256()).append(' ').append(e.size()).append(' ')
              .append(e.relativePath()).append('\n');
        }
        return sb.toString();
    }

    /** Parses the entry lines of {@link #toManifestText}; header ({@code #})
     * and malformed lines are skipped. Entry paths are canonicalized so every
     * consumer (exclusion matching, fetching, deletion diffing) sees one
     * spelling per file. */
    public static List<Entry> parseManifestText(String text) {
        List<Entry> entries = new ArrayList<>();
        String root = DEFAULT_ROOT;
        for (String line : text.split("\n")) {
            if (line.startsWith(ROOT_HEADER_PREFIX)) {
                root = line.substring(ROOT_HEADER_PREFIX.length()).trim();
                continue;
            }
            if (line.isBlank() || line.startsWith("#")) continue;
            int firstSpace = line.indexOf(' ');
            int secondSpace = line.indexOf(' ', firstSpace + 1);
            if (firstSpace < 0 || secondSpace < 0) continue;
            try {
                entries.add(new Entry(root,
                        normalizeRelative(line.substring(secondSpace + 1)),
                        line.substring(0, firstSpace),
                        Long.parseLong(line.substring(firstSpace + 1, secondSpace))));
            } catch (NumberFormatException e) {
                // skip malformed line
            }
        }
        return entries;
    }

    /** True if {@code text} is a manifest emitted by {@link #toManifestText}. */
    public static boolean isManifest(String text) {
        return text != null && text.startsWith(MANIFEST_MARKER);
    }

    /** The release this node runs, as advertised in and compared against a
     * manifest. */
    public static String localVersion() {
        return new SystemInfoUtils().getVersion();
    }

    /** The release the manifest was served by, or null if it carries none. */
    public static String parseManifestVersion(String text) {
        for (String line : text.split("\n")) {
            if (line.startsWith(VERSION_HEADER_PREFIX)) {
                return line.substring(VERSION_HEADER_PREFIX.length()).trim();
            }
        }
        return null;
    }

    /** Parses the serving node's exclusion patterns from a manifest. */
    public static List<String> parseManifestExcludes(String text) {
        List<String> excludes = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (line.startsWith(EXCLUDE_HEADER_PREFIX)) {
                excludes.add(line.substring(EXCLUDE_HEADER_PREFIX.length()));
            }
        }
        return excludes;
    }

    public static String sha256(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) {
                    md.update(buf, 0, n);
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("sha256 failed for " + file, e);
        }
    }
}
