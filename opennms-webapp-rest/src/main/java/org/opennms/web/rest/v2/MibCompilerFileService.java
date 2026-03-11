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
package org.opennms.web.rest.v2;

import org.opennms.core.utils.ConfigFileConstants;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class MibCompilerFileService {

    public static final String DEFAULT_MIB_EXTENSION = ".mib";

    private static final String SHARE_MIBS_DIR = "share" + File.separatorChar + "mibs";
    private static final String PENDING_DIR = "pending";
    private static final String COMPILED_DIR = "compiled";

    public File getMibsRootDir() {
        return new File(ConfigFileConstants.getHome(), SHARE_MIBS_DIR);
    }

    public File getPendingDir() {
        return new File(getMibsRootDir(), PENDING_DIR);
    }

    public File getCompiledDir() {
        return new File(getMibsRootDir(), COMPILED_DIR);
    }

    public void ensurePendingDirExists() {
        ensureDirExists(getPendingDir());
    }

    public void ensureCompiledDirExists() {
        ensureDirExists(getCompiledDir());
    }

    public boolean baseNameExistsInPendingOrCompiled(final String baseName) throws Exception {
        return baseNameExists(getPendingDir(), baseName) || baseNameExists(getCompiledDir(), baseName);
    }

    /**
     * Save a file to pending with a normalized name: {baseName}{extension}
     * Example: baseName="IF-MIB", extension=".mib" -> IF-MIB.mib
     */
    public File saveToPending(final String baseName,
                              final String extension,
                              final InputStream content) throws Exception {

        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException("baseName must not be blank.");
        }
        if (content == null) {
            throw new IllegalArgumentException("content must not be null.");
        }

        ensurePendingDirExists();
        final String ext = normalizeExtension(extension, DEFAULT_MIB_EXTENSION);
        final File target = new File(getPendingDir(), baseName + ext);

        try (FileOutputStream out = new FileOutputStream(target)) {
            content.transferTo(out);
        }
        return target;
    }

    /**
     * Optional helper for later endpoints: move a pending file to compiled.
     * This keeps the same filename (including extension).
     */
    public File movePendingToCompiled(final File pendingFile) throws Exception {
        if (pendingFile == null) {
            throw new IllegalArgumentException("pendingFile must not be null.");
        }
        ensureCompiledDirExists();
        final Path source = pendingFile.toPath();
        final Path dest = new File(getCompiledDir(), pendingFile.getName()).toPath();
        Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toFile();
    }

    /**
     * Optional helper for later endpoints: copy a pending file to compiled.
     */
    public File copyPendingToCompiled(final File pendingFile) throws Exception {
        if (pendingFile == null) {
            throw new IllegalArgumentException("pendingFile must not be null.");
        }
        ensureCompiledDirExists();
        final Path source = pendingFile.toPath();
        final Path dest = new File(getCompiledDir(), pendingFile.getName()).toPath();
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toFile();
    }

    private static void ensureDirExists(final File dir) {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IllegalStateException("Path exists but is not a directory: " + dir.getAbsolutePath());
            }
            return;
        }
        if (!dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    /**
     * Checks if any file in {@code dir} has the given basename (basename comparison, not full filename).
     * Requirement: "Base file names (without extension) must not already exist in either compiled or pending."
     */
    private static boolean baseNameExists(final File dir, final String baseName) throws Exception {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }
        try (var stream = Files.list(dir.toPath())) {
            return stream.anyMatch(p -> baseName.equals(stripPathAndExtension(p.getFileName().toString())));
        }
    }

    /**
     * Removes any path segments and strips the extension.
     * Examples:
     *  - "C:\\fakepath\\FOO.mib" -> "FOO"
     *  - "/tmp/FOO.my.mib" -> "FOO.my"
     *  - "FOO" -> "FOO"
     */
    public static String stripPathAndExtension(final String filename) {
        if (filename == null) return null;

        // Strip any path component
        String justName = filename;
        int slash = justName.lastIndexOf('/');
        int backslash = justName.lastIndexOf('\\');
        int idx = Math.max(slash, backslash);
        if (idx >= 0 && idx + 1 < justName.length()) {
            justName = justName.substring(idx + 1);
        }

        justName = justName.trim();
        if (justName.isEmpty()) return null;

        // Strip extension
        int dot = justName.lastIndexOf('.');
        if (dot > 0) {
            return justName.substring(0, dot);
        }
        return justName;
    }

    public static String normalizeExtension(final String extension, final String defaultExt) {
        String ext = extension;
        if (ext == null || ext.isBlank()) {
            ext = defaultExt;
        }
        ext = ext.trim();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        return ext;
    }
}
