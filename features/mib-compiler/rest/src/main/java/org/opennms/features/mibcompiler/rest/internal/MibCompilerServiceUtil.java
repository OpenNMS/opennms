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
package org.opennms.features.mibcompiler.rest.internal;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.mibcompiler.rest.model.MibCompilerFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MibCompilerServiceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerServiceUtil.class);

    public static final String DEFAULT_MIB_EXTENSION = ".mib";

    private static final String SHARE_MIBS_DIR = "share" + File.separatorChar + "mibs";
    private static final String PENDING_DIR = "pending";
    private static final String COMPILED_DIR = "compiled";

    private static final File MIBS_ROOT_DIR = new File(ConfigFileConstants.getHome(), SHARE_MIBS_DIR);

    /** The Constant MIBS_PENDING_DIR. */
    private static final File MIBS_PENDING_DIR = new File(MIBS_ROOT_DIR, PENDING_DIR);

    /** The Constant MIBS_COMPILED_DIR. */
    private static final File MIBS_COMPILED_DIR = new File(MIBS_ROOT_DIR, COMPILED_DIR);

    /** Expose compiled dir for consumers that need to configure parsers, etc. */
    public static File getCompiledDir() {
        return MIBS_COMPILED_DIR;
    }

    public static boolean baseNameExistsInPendingOrCompiled(final String baseName) throws Exception {
        final boolean existsPending = baseNameExists(MIBS_PENDING_DIR, baseName);
        final boolean existsCompiled = baseNameExists(MIBS_COMPILED_DIR, baseName);
        final boolean exists = existsPending || existsCompiled;

        LOG.debug("baseNameExistsInPendingOrCompiled(baseName={}): pending={}, compiled={}, exists={}",
                baseName, existsPending, existsCompiled, exists);

        return exists;
    }

    /**
     * Save a file to pending with a normalized name: {baseName}{extension}
     * Example: baseName="IF-MIB", extension=".mib" -> IF-MIB.mib
     */
    public static File saveToPending(final String baseName,
                                     final String extension,
                                     final InputStream content) throws Exception {

        if (isBlank(baseName)) {
            LOG.warn("saveToPending called with blank baseName");
            throw new IllegalArgumentException("baseName must not be blank.");
        }
        if (content == null) {
            LOG.warn("saveToPending(baseName={}) called with null content", baseName);
            throw new IllegalArgumentException("content must not be null.");
        }

        ensureDirExists(MIBS_PENDING_DIR);
        final String ext = normalizeExtension(extension, DEFAULT_MIB_EXTENSION);
        final File target = new File(MIBS_PENDING_DIR, baseName + ext);

        LOG.info("Saving MIB to pending: baseName={}, extension={}, target={}",
                baseName, ext, target.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(target)) {
            copy(content, out);
        }

        LOG.debug("Saved pending MIB: {}", target.getAbsolutePath());
        return target;
    }

    /**
     * Find a single pending file by base name (no parsing/validation here).
     * @return the pending File or null if not found
     * @throws IllegalStateException if multiple matches exist
     */
    public static File findPendingByBaseName(final String baseName) throws Exception {
        if (isBlank(baseName)) {
            throw new IllegalArgumentException("baseName must not be blank.");
        }

        ensureDirExists(MIBS_PENDING_DIR);

        final String normalizedBaseName = stripPathAndExtension(baseName);
        if (isBlank(normalizedBaseName)) {
            throw new IllegalArgumentException("baseName must not be blank.");
        }

        return findSingleByBaseName(MIBS_PENDING_DIR, normalizedBaseName);
    }

    /**
     * Move a pending file to compiled directory, forcing ".mib" extension.
     * This assumes validation/parsing has already happened elsewhere.
     */
    public static File movePendingToCompiled(final File pendingFile, final String baseName) throws Exception {
        if (pendingFile == null) {
            throw new IllegalArgumentException("pendingFile must not be null.");
        }
        if (isBlank(baseName)) {
            throw new IllegalArgumentException("baseName must not be blank.");
        }

        ensureDirExists(MIBS_COMPILED_DIR);

        final String normalizedBaseName = stripPathAndExtension(baseName);
        if (isBlank(normalizedBaseName)) {
            throw new IllegalArgumentException("baseName must not be blank.");
        }

        final File destFile = new File(MIBS_COMPILED_DIR, normalizedBaseName + DEFAULT_MIB_EXTENSION);
        if (destFile.exists()) {
            LOG.warn("Compilation conflict: destination file already exists: {}", destFile.getAbsolutePath());
            throw new IllegalStateException("Compiled file already exists: " + destFile.getName());
        }

        LOG.info("Moving compiled MIB from pending to compiled: from={}, to={}",
                pendingFile.getAbsolutePath(), destFile.getAbsolutePath());

        Files.move(pendingFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

        LOG.info("Moved MIB to compiled: baseName={}, compiledFile={}",
                normalizedBaseName, destFile.getAbsolutePath());

        return destFile;
    }

    public static List<MibCompilerFileInfo> listPendingAndCompiledFiles() throws IOException {
        ensureDirExists(MIBS_PENDING_DIR);
        ensureDirExists(MIBS_COMPILED_DIR);

        final List<MibCompilerFileInfo> results = new ArrayList<MibCompilerFileInfo>();
        results.addAll(listFilesInDir(MIBS_PENDING_DIR, MibCompilerFileInfo.Location.PENDING));
        results.addAll(listFilesInDir(MIBS_COMPILED_DIR, MibCompilerFileInfo.Location.COMPILED));

        results.sort(Comparator
                .comparing(MibCompilerFileInfo::getLocation)
                .thenComparing(MibCompilerFileInfo::getFileName));

        LOG.debug("Listed mib compiler files: total={}, pendingDir={}, compiledDir={}",
                results.size(), MIBS_PENDING_DIR.getAbsolutePath(), MIBS_COMPILED_DIR.getAbsolutePath());

        return results;
    }

    public static boolean deleteFile(final String location, final String fileName) throws IOException {
        if (isBlank(location)) {
            throw new IllegalArgumentException("location must not be blank.");
        }
        if (isBlank(fileName)) {
            throw new IllegalArgumentException("fileName must not be blank.");
        }

        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            throw new IllegalArgumentException("Invalid fileName.");
        }

        final File dir = resolveLocationDir(location);
        ensureDirExists(dir);

        final Path target = new File(dir, fileName).toPath();

        if (!Files.exists(target)) {
            return false; // not found
        }
        if (!Files.isRegularFile(target)) {
            throw new IllegalStateException("Target is not a regular file: " + fileName);
        }

        Files.delete(target);
        LOG.info("Deleted MIB file: location={}, fileName={}", location, fileName);
        return true;
    }

    public static String readTextFile(final String location, final String fileName) throws IOException {
        final File dir = resolveLocationDir(location);
        ensureDirExists(dir);

        final Path target = new File(dir, fileName).toPath();

        if (!Files.exists(target)) {
            return null;
        }
        if (!Files.isRegularFile(target)) {
            throw new IllegalStateException("Target is not a regular file: " + fileName);
        }

        byte[] bytes = Files.readAllBytes(target);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeBinaryFile(final String location, final String fileName, final byte[] contents) throws IOException {
        final File dir = resolveLocationDir(location);
        ensureDirExists(dir);

        final Path target = new File(dir, fileName).toPath();

        if (!Files.exists(target)) {
            throw new IllegalArgumentException("File does not exist: " + target);
        }
        if (!Files.isRegularFile(target)) {
            throw new IllegalArgumentException("Target is not a regular file: " + fileName);
        }

        Files.write(target, contents);
    }

    /** Helper for REST: does file exist at location/fileName? */
    public static boolean exists(final String location, final String fileName) throws IOException {
        final File dir = resolveLocationDir(location);
        ensureDirExists(dir);
        final Path target = new File(dir, fileName).toPath();
        return Files.exists(target) && Files.isRegularFile(target);
    }

    /** Helper for REST: get File handle for location/fileName (no existence guarantee). */
    public static File getFile(final String location, final String fileName) {
        final File dir = resolveLocationDir(location);
        return new File(dir, fileName);
    }

    private static File resolveLocationDir(final String location) {
        if (isBlank(location)) {
            throw new IllegalArgumentException("location must not be blank.");
        }
        switch (location.toLowerCase()) {
            case "pending":
                return MIBS_PENDING_DIR;
            case "compiled":
                return MIBS_COMPILED_DIR;
            default:
                throw new IllegalArgumentException("Invalid location: " + location + ". Must be 'pending' or 'compiled'.");
        }
    }

    private static List<MibCompilerFileInfo> listFilesInDir(File dir, MibCompilerFileInfo.Location location) throws IOException {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        final List<MibCompilerFileInfo> out = new ArrayList<MibCompilerFileInfo>();
        try (Stream<Path> stream = Files.list(dir.toPath())) {
            stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(p -> out.add(new MibCompilerFileInfo(
                            p.getFileName().toString(),
                            location
                    )));
        }
        return out;
    }

    private static File findSingleByBaseName(final File dir, final String baseName) throws Exception {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            LOG.debug("findSingleByBaseName: directory missing or not a directory: dir={}, baseName={}",
                    dir == null ? null : dir.getAbsolutePath(), baseName);
            return null;
        }

        final List<Path> matches = new ArrayList<Path>();
        try (Stream<Path> stream = Files.list(dir.toPath())) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                final String bn = stripPathAndExtension(p.getFileName().toString());
                if (baseName.equals(bn)) {
                    matches.add(p);
                }
            });
        }

        matches.sort(Comparator.comparing(p -> p.getFileName().toString()));

        if (matches.isEmpty()) {
            LOG.debug("findSingleByBaseName: no matches: dir={}, baseName={}", dir.getAbsolutePath(), baseName);
            return null;
        }
        if (matches.size() > 1) {
            LOG.error("findSingleByBaseName: multiple matches: dir={}, baseName={}, matches={}",
                    dir.getAbsolutePath(), baseName, matches);
            throw new IllegalStateException("Multiple pending files found with base name '" + baseName + "': " + matches);
        }

        LOG.debug("findSingleByBaseName: single match found: {}", matches.get(0));
        return matches.get(0).toFile();
    }

    private static void ensureDirExists(final File dir) {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                LOG.error("Path exists but is not a directory: {}", dir.getAbsolutePath());
                throw new IllegalStateException("Path exists but is not a directory: " + dir.getAbsolutePath());
            }
            return;
        }
        if (!dir.mkdirs()) {
            LOG.error("Failed to create directory: {}", dir.getAbsolutePath());
            throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
        }
        LOG.debug("Created directory: {}", dir.getAbsolutePath());
    }

    private static boolean baseNameExists(final File dir, final String baseName) throws Exception {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            LOG.debug("baseNameExists: dir missing or not a directory: dir={}, baseName={}",
                    dir == null ? null : dir.getAbsolutePath(), baseName);
            return false;
        }
        try (Stream<Path> stream = Files.list(dir.toPath())) {
            final boolean found = stream.anyMatch(p -> baseName.equals(stripPathAndExtension(p.getFileName().toString())));
            LOG.debug("baseNameExists: dir={}, baseName={}, found={}", dir.getAbsolutePath(), baseName, found);
            return found;
        }
    }

    public static String stripPathAndExtension(final String filename) {
        if (filename == null) return null;

        String justName = filename;
        int slash = justName.lastIndexOf('/');
        int backslash = justName.lastIndexOf('\\');
        int idx = Math.max(slash, backslash);
        if (idx >= 0 && idx + 1 < justName.length()) {
            justName = justName.substring(idx + 1);
        }

        justName = justName.trim();
        if (justName.isEmpty()) return null;

        int dot = justName.lastIndexOf('.');
        if (dot > 0) {
            return justName.substring(0, dot);
        }
        return justName;
    }

    public static String normalizeExtension(final String extension, final String defaultExt) {
        String ext = extension;
        if (isBlank(ext)) {
            ext = defaultExt;
        }
        ext = ext.trim();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        return ext;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static long copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }
        return total;
    }
}