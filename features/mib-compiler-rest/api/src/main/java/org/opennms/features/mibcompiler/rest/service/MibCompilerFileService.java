package org.opennms.features.mibcompiler.rest.service;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.rest.model.CompileMibResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MibCompilerFileService {

    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerFileService.class);

    private final MibParser mibParser;

    public static final String DEFAULT_MIB_EXTENSION = ".mib";

    private static final String SHARE_MIBS_DIR = "share" + File.separatorChar + "mibs";
    private static final String PENDING_DIR = "pending";
    private static final String COMPILED_DIR = "compiled";

    public MibCompilerFileService(MibParser mibParser) {
        this.mibParser = mibParser;
    }

    public File getMibsRootDir() {
        return new File(getHome(), SHARE_MIBS_DIR);
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
     * Compile (parse/validate) a file that already exists in the pending directory, then move it
     * to the compiled directory, forcing a ".mib" extension.
     *
     * - The file must exist in pending directory.
     * - Caller provides a base name; extension is optional.
     * - There should only be one file in pending with that base name.
     * - Dependencies should already be compiled (present in compiled dir).
     */
    public CompileMibResult compilePendingByBaseName(final String baseName) throws Exception {
        if (baseName == null || baseName.isBlank()) {
            return CompileMibResult.invalidRequest("baseName must not be blank.");
        }

        ensurePendingDirExists();
        ensureCompiledDirExists();

        final String normalizedBaseName = stripPathAndExtension(baseName);
        if (normalizedBaseName == null || normalizedBaseName.isBlank()) {
            return CompileMibResult.invalidRequest("baseName must not be blank.");
        }

        final File pendingFile = findSingleByBaseName(getPendingDir(), normalizedBaseName);
        if (pendingFile == null) {
            return CompileMibResult.notFound("No pending file found with base name '" + normalizedBaseName + "'.");
        }

        // Parse/validate
        final boolean ok = mibParser.parseMib(pendingFile);
        if (!ok) {
            final List<String> missing = safeList(mibParser.getMissingDependencies());
            if (!missing.isEmpty()) {
                return CompileMibResult.missingDependencies("Missing dependencies: " + missing, missing);
            }
            final String errors = mibParser.getFormattedErrors();
            return CompileMibResult.validationFailed("MIB validation failed.", errors);
        }

        // Move to compiled and force .mib extension
        final File destFile = new File(getCompiledDir(), normalizedBaseName + DEFAULT_MIB_EXTENSION);
        if (destFile.exists()) {
            return CompileMibResult.conflict("Compiled file already exists: " + destFile.getName());
        }

        Files.move(pendingFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

        return CompileMibResult.success(pendingFile, destFile);
    }

    private static List<String> safeList(final List<String> l) {
        return l == null ? Collections.<String>emptyList() : l;
    }

    private static File findSingleByBaseName(final File dir, final String baseName) throws Exception {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return null;
        }

        final List<Path> matches = new ArrayList<Path>();
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
            return null;
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple pending files found with base name '" + baseName + "': " + matches);
        }
        return matches.get(0).toFile();
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

    public static String getHome() {
        String home = System.getProperty("opennms.home");
        if (home == null) {
            LOG.debug("The 'opennms.home' property was not set, falling back to /opt/opennms.  This should really only happen in unit tests.");
            home = File.separator + "opt" + File.separator + "opennms";
        }
        // Remove the trailing slash if necessary
        //
        if (home.endsWith("/") || home.endsWith(File.separator))
            home = home.substring(0, home.length() - 1);

        return home;
    }


}
