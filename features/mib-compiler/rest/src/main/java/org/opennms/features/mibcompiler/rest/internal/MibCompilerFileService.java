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
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.rest.model.CompileMibResult;
import org.opennms.features.mibcompiler.rest.model.MibCompilerFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MibCompilerFileService {

    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerFileService.class);

    private final MibParser mibParser;

    public static final String DEFAULT_MIB_EXTENSION = ".mib";

    private static final String SHARE_MIBS_DIR = "share" + File.separatorChar + "mibs";
    private static final String PENDING_DIR = "pending";
    private static final String COMPILED_DIR = "compiled";

    private static final File MIBS_ROOT_DIR = new File(ConfigFileConstants.getHome(), SHARE_MIBS_DIR);

    /** The Constant MIBS_PENDING_DIR. */
    private static final File MIBS_PENDING_DIR = new File(MIBS_ROOT_DIR, PENDING_DIR);

    /** The Constant MIBS_COMPILED_DIR. */
    private static final File MIBS_COMPILED_DIR = new File(MIBS_ROOT_DIR, COMPILED_DIR);

    public MibCompilerFileService(MibParser mibParser) {
        this.mibParser = mibParser;
        mibParser.setMibDirectory(MIBS_COMPILED_DIR);

        LOG.debug("Initialized {} with MIB directories: root={}, pending={}, compiled={}",
                getClass().getSimpleName(),
                MIBS_ROOT_DIR.getAbsolutePath(),
                MIBS_PENDING_DIR.getAbsolutePath(),
                MIBS_COMPILED_DIR.getAbsolutePath());
    }

    public boolean baseNameExistsInPendingOrCompiled(final String baseName) throws Exception {
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
    public File saveToPending(final String baseName,
                              final String extension,
                              final InputStream content) throws Exception {

        if (baseName == null || baseName.isBlank()) {
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
            content.transferTo(out);
        }

        LOG.debug("Saved pending MIB: {}", target.getAbsolutePath());
        return target;
    }

    /**
     * Compile (parse/validate) a file that already exists in the pending directory, then move it
     * to the compiled directory, forcing a ".mib" extension.
     */
    public CompileMibResult compilePendingByBaseName(final String baseName) throws Exception {
        if (baseName == null || baseName.isBlank()) {
            LOG.warn("compilePendingByBaseName called with blank baseName");
            return CompileMibResult.invalidRequest("baseName must not be blank.");
        }

        ensureDirExists(MIBS_PENDING_DIR);
        ensureDirExists(MIBS_COMPILED_DIR);

        final String normalizedBaseName = stripPathAndExtension(baseName);
        if (normalizedBaseName == null || normalizedBaseName.isBlank()) {
            LOG.warn("compilePendingByBaseName(baseName={}) resulted in blank normalized base name", baseName);
            return CompileMibResult.invalidRequest("baseName must not be blank.");
        }

        LOG.info("Compiling pending MIB: requestedBaseName={}, normalizedBaseName={}", baseName, normalizedBaseName);

        final File pendingFile;
        try {
            pendingFile = findSingleByBaseName(MIBS_PENDING_DIR, normalizedBaseName);
        } catch (IllegalStateException e) {
            LOG.error("Multiple pending files found for baseName={} in dir={}: {}",
                    normalizedBaseName, MIBS_PENDING_DIR.getAbsolutePath(), e.getMessage(), e);
            throw e;
        }

        if (pendingFile == null) {
            LOG.warn("No pending file found for baseName={} in dir={}", normalizedBaseName, MIBS_PENDING_DIR.getAbsolutePath());
            return CompileMibResult.notFound("No pending file found with base name '" + normalizedBaseName + "'.");
        }

        LOG.debug("Found pending file to compile: {}", pendingFile.getAbsolutePath());

        final boolean parsed;
        try {
            parsed = mibParser.parseMib(pendingFile);
        } catch (RuntimeException e) {
            // If the parser can throw, treat as unexpected
            LOG.error("Unexpected error while parsing MIB file: {}", pendingFile.getAbsolutePath(), e);
            throw e;
        }

        if (!parsed) {
            final var missingDeps = mibParser.getMissingDependencies();
            if (missingDeps != null && !missingDeps.isEmpty()) {
                LOG.warn("MIB compilation failed due to missing dependencies: baseName={}, file={}, missingDependencies={}",
                        normalizedBaseName, pendingFile.getName(), missingDeps);
                return CompileMibResult.missingDependencies("Missing dependencies: " + missingDeps, missingDeps);
            }
            final String errors = mibParser.getFormattedErrors();
            LOG.warn("MIB validation failed: baseName={}, file={}, errors={}",
                    normalizedBaseName, pendingFile.getName(), errors);
            return CompileMibResult.validationFailed("MIB validation failed.", errors);
        }

        // Move to compiled and force .mib extension
        final File destFile = new File(MIBS_COMPILED_DIR, normalizedBaseName + DEFAULT_MIB_EXTENSION);
        if (destFile.exists()) {
            LOG.warn("Compilation conflict: destination file already exists: {}", destFile.getAbsolutePath());
            return CompileMibResult.conflict("Compiled file already exists: " + destFile.getName());
        }

        LOG.info("Moving compiled MIB from pending to compiled: from={}, to={}",
                pendingFile.getAbsolutePath(), destFile.getAbsolutePath());

        Files.move(pendingFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

        LOG.info("MIB compiled successfully: baseName={}, compiledFile={}",
                normalizedBaseName, destFile.getAbsolutePath());

        return CompileMibResult.success(pendingFile, destFile);
    }

    public List<MibCompilerFileInfo> listPendingAndCompiledFiles() throws IOException {
        ensureDirExists(MIBS_PENDING_DIR);
        ensureDirExists(MIBS_COMPILED_DIR);

        final List<MibCompilerFileInfo> results = new ArrayList<>();
        results.addAll(listFilesInDir(MIBS_PENDING_DIR, MibCompilerFileInfo.Location.PENDING));
        results.addAll(listFilesInDir(MIBS_COMPILED_DIR, MibCompilerFileInfo.Location.COMPILED));

        results.sort(Comparator
                .comparing(MibCompilerFileInfo::getLocation)
                .thenComparing(MibCompilerFileInfo::getFileName));

        LOG.debug("Listed mib compiler files: total={}, pendingDir={}, compiledDir={}",
                results.size(), MIBS_PENDING_DIR.getAbsolutePath(), MIBS_COMPILED_DIR.getAbsolutePath());

        return results;
    }

    public boolean deleteFile(final String location, final String fileName) throws IOException {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("location must not be blank.");
        }
        if (fileName == null || fileName.isBlank()) {
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

    public String readTextFile(final String location, final String fileName) throws java.io.IOException {
        final File dir = resolveLocationDir(location);
        ensureDirExists(dir);

        final Path target = new File(dir, fileName).toPath();

        if (!Files.exists(target)) {
            return null;
        }
        if (!Files.isRegularFile(target)) {
            throw new IllegalStateException("Target is not a regular file: " + fileName);
        }

        return Files.readString(target, StandardCharsets.UTF_8);
    }

    public void writeBinaryFile(final String location, final String fileName, final byte[] contents) throws IOException {
        final File dir = resolveLocationDir(location);
        ensureDirExists(dir);

        final Path target = new File(dir, fileName).toPath();

        if (!Files.exists(target)) {
            throw new IllegalArgumentException("File does not exists: " + target);
        }
        if (!Files.isRegularFile(target)) {
            throw new IllegalArgumentException("Target is not a regular file: " + fileName);
        }

        Files.write(target, contents);
    }

    private static File resolveLocationDir(final String location) {
        if (location == null || location.isBlank()) {
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

        final List<MibCompilerFileInfo> out = new ArrayList<>();
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

        final List<Path> matches = new ArrayList<>();
        try (var stream = Files.list(dir.toPath())) {
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

    /**
     * Checks if any file in {@code dir} has the given basename (basename comparison, not full filename).
     */
    private static boolean baseNameExists(final File dir, final String baseName) throws Exception {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            LOG.debug("baseNameExists: dir missing or not a directory: dir={}, baseName={}",
                    dir == null ? null : dir.getAbsolutePath(), baseName);
            return false;
        }
        try (var stream = Files.list(dir.toPath())) {
            final boolean found = stream.anyMatch(p -> baseName.equals(stripPathAndExtension(p.getFileName().toString())));
            LOG.debug("baseNameExists: dir={}, baseName={}, found={}", dir.getAbsolutePath(), baseName, found);
            return found;
        }
    }

    /**
     * Removes any path segments and strips the extension.
     */
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
