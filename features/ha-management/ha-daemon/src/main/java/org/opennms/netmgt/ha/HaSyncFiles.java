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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
 * <p>Manifest wire format is deliberately plain text, one file per line:
 * {@code <sha256> <size> <relative-path>} — binary-safe transfer happens on
 * the separate per-file endpoint, and the standby can parse this without any
 * JSON machinery.
 */
public final class HaSyncFiles {

    /** Files that are never synced regardless of configuration: each node's
     * own HA identity, and transient artifacts. */
    private static final List<String> BUILTIN_EXCLUSIONS =
            List.of("ha-configuration.xml", "examples/");

    private HaSyncFiles() {}

    public record Entry(String relativePath, String sha256, long size) {}

    public static Path etcRoot() {
        String opennmsHome = System.getProperty("opennms.home", ".");
        return Paths.get(opennmsHome, "etc").toAbsolutePath().normalize();
    }

    /** True if {@code relativePath} is excluded from sync (builtin rules plus
     * the operator-extensible list; a trailing "/" excludes a subtree). */
    public static boolean isExcluded(String relativePath, List<String> configuredExcludes) {
        List<String> all = new ArrayList<>(BUILTIN_EXCLUSIONS);
        if (configuredExcludes != null) {
            all.addAll(configuredExcludes);
        }
        return all.stream().anyMatch(e ->
                e.endsWith("/") ? relativePath.startsWith(e) : relativePath.equals(e));
    }

    /**
     * Resolves a manifest-relative path inside {@code root}, rejecting
     * anything that escapes it (absolute paths, {@code ..} traversal).
     */
    public static Path resolveSafe(Path root, String relativePath) throws IOException {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IOException("path escapes sync root: " + relativePath);
        }
        return resolved;
    }

    /** Walks {@code root} and builds manifest entries for every regular,
     * non-excluded file. */
    public static List<Entry> buildManifest(Path root, List<String> configuredExcludes) throws IOException {
        List<Entry> entries = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path p : (Iterable<Path>) walk::iterator) {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                String rel = root.relativize(p).toString().replace('\\', '/');
                if (isExcluded(rel, configuredExcludes)) {
                    continue;
                }
                entries.add(new Entry(rel, sha256(p), Files.size(p)));
            }
        }
        return entries;
    }

    public static String toManifestText(List<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            sb.append(e.sha256()).append(' ').append(e.size()).append(' ')
              .append(e.relativePath()).append('\n');
        }
        return sb.toString();
    }

    /** Parses {@link #toManifestText}; malformed lines are skipped. */
    public static List<Entry> parseManifestText(String text) {
        List<Entry> entries = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (line.isBlank()) continue;
            int firstSpace = line.indexOf(' ');
            int secondSpace = line.indexOf(' ', firstSpace + 1);
            if (firstSpace < 0 || secondSpace < 0) continue;
            try {
                entries.add(new Entry(
                        line.substring(secondSpace + 1),
                        line.substring(0, firstSpace),
                        Long.parseLong(line.substring(firstSpace + 1, secondSpace))));
            } catch (NumberFormatException e) {
                // skip malformed line
            }
        }
        return entries;
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
