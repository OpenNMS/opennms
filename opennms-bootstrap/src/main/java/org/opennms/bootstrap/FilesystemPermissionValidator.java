/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class FilesystemPermissionValidator {
    public static boolean VERBOSE = false;
    private static final Random RAND = new Random();

    public static String OPENNMS_HOME = System.getProperty("opennms.home", null);
    public static String DEFAULT_USER = System.getProperty("user.name");

    // directories that should always be fully checked
    public static final String[] FULLDIRS = { "etc", "logs" };

    // directories that could contain loads of user data, or might be empty
    public static final String[] SPOTDIRS = { "data", "instances", "share" };

    // not written by a running OpenNMS: deploy, lib, system

    // the paths (and their contents) that should be skipped
    private static final List<Path> SKIP_PATHS = Arrays.asList(Path.of(".git"), Path.of("lost+found"));

    private static final FileFilter ONLY_DIRECTORIES = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {
            return pathname.isDirectory();
        }
    };

    public boolean validate(final String user, Path path) throws FilesystemPermissionException {
        if (path == null) {
            path = Paths.get(OPENNMS_HOME);
        }

        if (VERBOSE) {
            System.out.println("Validating: user " + user + " can write to " + path + "\n");
        }

        for (final String dir : FULLDIRS) {
            validatePath(user, path.resolve(dir));
        }

        for (final String dir : SPOTDIRS) {
            final Path dirPath = path.resolve(dir);

            if (!dirPath.toFile().exists()) {
                if (VERBOSE) {
                    System.out.println("! expected directory " + dirPath + " was not found. Skipping.");
                }
                continue;
            } else {
                if (VERBOSE) {
                    System.out.println("- spot-checking " + dirPath);
                }
            }

            File deepest = null;
            try {
                deepest = dirPath.toRealPath().toFile();
            } catch (final IOException e) {
                throw new FilesystemPermissionException(dirPath, user, e);
            }
            while (deepest.isDirectory()) {
                if (VERBOSE) {
                    System.out.println("  - " + dirPath + " is a directory");
                }

                if (SKIP_PATHS.stream().anyMatch(dirPath::endsWith)) {
                    deepest = deepest.getParentFile();
                    continue;
                }

                File[] files = deepest.listFiles(ONLY_DIRECTORIES);

                if (files == null) {
                    if (VERBOSE) {
                        System.out.println(deepest + " has no contents");
                    }
                    break;
                }

                if (files.length == 0) {
                    // no more subdirectories, let's just get the files in the current directory and we'll pick one of them
                    files = deepest.listFiles();
                }
                if (files.length == 0) {
                    // we hit a dead-end with nothing in it, let's just stop here
                    break;
                }

                final List<File> remainders = Arrays.asList(files).stream()
                        .filter(file -> SKIP_PATHS.stream().noneMatch(p -> file.toPath().endsWith(p)))
                        .collect(Collectors.toList());

                if (remainders.isEmpty()) {
                    // there's nothing left other than skipped directories
                    break;
                }

                // pick something new from the current list to mark as "deepest"
                deepest = getRandomFile(user, deepest, remainders.toArray(new File[0]));
            }

            if (VERBOSE) {
                System.out.println("  - checking " + deepest);
            }
            validateFile(user, deepest.toPath());
        }

        return true;
    }

    private File getRandomFile(final String user, final File deepest, final File[] files) throws FilesystemPermissionException {
        try {
            return files[RAND.nextInt(files.length)].getCanonicalFile();
        } catch (final IOException e) {
            throw new FilesystemPermissionException(deepest.toPath(), user, e);
        }
    }

    private void validatePath(final String user, final Path dirPath) throws FilesystemPermissionException {
        if (!dirPath.toFile().exists()) {
            System.err.println("! expected directory " + dirPath + " was not found. Skipping.");
            return;
        } else {
            if (VERBOSE) {
                System.out.println("- checking " + dirPath);
            }
        }


        final var visitor = new FileValidatorVisitor(user);

        try {
            Files.walkFileTree(dirPath, Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
        } catch (final IOException e) {
            throw new FilesystemPermissionException(dirPath, user, e);
        }

        visitor.assertValid();
    }

    private void validateFile(final String user, final Path filePath) throws FilesystemPermissionException {
        if (VERBOSE) {
            System.out.println("- validating file " + filePath + " is writable by " + user);
        }
        var file = filePath.toFile();
        if (!file.canRead() || !file.canWrite()) {
            throw new FilesystemPermissionException(filePath, user);
        }
        if (file.isDirectory()) {
            if (!file.canExecute()) {
                throw new FilesystemPermissionException(filePath, user);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        String user = DEFAULT_USER;
        Path path;
        if (args.length >= 1 && args[0] != null) {
            user = args[0];
        }
        if (args.length >= 2 && args[1] != null) {
            path = Paths.get(args[1]);
        } else {
            path = Paths.get(OPENNMS_HOME);
        }
        if (args.length >= 3 && (Objects.equals(args[2], "-v") || Objects.equals(args[2], "--verbose"))) {
            FilesystemPermissionValidator.VERBOSE = true;
        }

        final FilesystemPermissionValidator validator = new FilesystemPermissionValidator();
        try {
            validator.validate(user, path);
        } catch (final FilesystemPermissionException e) {
            System.err.println("\nERROR: at least one file in " + path + " is not writable by " + user + ".");
            System.err.println("Make sure $RUNAS in opennms.conf matches the permissions of " + path +
                    " and its subdirectories.\n");
            e.printStackTrace();
            System.err.println();
        }
    }

    private final class FileValidatorVisitor implements FileVisitor<Path> {
        private final String user;
        private final AtomicReference<FilesystemPermissionException> failure = new AtomicReference<>();

        public FileValidatorVisitor(final String user) {
            this.user = user;
        }

        public void assertValid() throws FilesystemPermissionException {
            if (failure.get() == null) return;
            throw failure.get();
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            if (SKIP_PATHS.contains(dir.getFileName())) {
                return FileVisitResult.SKIP_SUBTREE;
            }

            try {
                validateFile(user, dir);
            } catch (final FilesystemPermissionException e) {
                failure.set(e);
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            try {
                validateFile(user, file);
            } catch (final FilesystemPermissionException e) {
                failure.set(e);
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            if (failure.get() == null) {
                failure.set(new FilesystemPermissionException(file, user));
            }
            return FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
