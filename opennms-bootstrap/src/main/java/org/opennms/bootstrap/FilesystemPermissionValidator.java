/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class FilesystemPermissionValidator {
    public static boolean VERBOSE = false;
    public static String OPENNMS_HOME = System.getProperty("opennms.home", null);
    public static String DEFAULT_USER = System.getProperty("user.name");

    // directories that should be checked in full
    public String[] FULLDIRS = { "bin", "contrib", "etc", "jetty-webapps", "lib", "logs", "system" };

    // directories that could contain loads of user data, and should be spot checked
    public String[] SPOTDIRS = { "data", "deploy", "share" };

    private static FileFilter ONLY_DIRECTORIES = new FileFilter() {
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
            System.out.println("Validating: user " + user + " owns " + path + "\n");
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
                File[] files = deepest.listFiles(ONLY_DIRECTORIES);
                if (files.length == 0) {
                    // no more subdirectories, let's just get the files in the current directory and we'll pick one of them
                    files = deepest.listFiles();
                }
                if (files.length == 0) {
                    // we hit a dead-end with nothing in it, let's just stop here
                    break;
                }
                // pick something from the current list to mark as "deepest"
                try {
                    deepest = files[new Random().nextInt(files.length)].getCanonicalFile();
                } catch (final IOException e) {
                    throw new FilesystemPermissionException(deepest.toPath(), user, e);
                }
            };

            if (VERBOSE) {
                System.out.println("  - checking " + deepest);
            }
            validateFile(user, deepest.toPath());
        }

        return true;
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
        final List<Path> failures;
        try {
            failures = Files.walk(dirPath, FileVisitOption.FOLLOW_LINKS).filter(file -> {
                try {
                    validateFile(user, file);
                } catch (final FilesystemPermissionException e) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        } catch (final IOException e) {
            throw new FilesystemPermissionException(dirPath, user, e);
        }
        if (failures.size() > 0) {
            throw new FilesystemPermissionException(failures.get(0), user);
        }
    }

    private void validateFile(final String user, final Path filePath) throws FilesystemPermissionException {
        if (VERBOSE) {
            System.out.println("- validating file " + filePath + " is owned by " + user);
        }
        try {
            final UserPrincipal owner = Files.getOwner(filePath);
            if (VERBOSE) {
                System.out.println("  - " + filePath + " is owned by " + owner);
            }
            if (!Objects.equals(owner.getName(), user)) {
                throw new FilesystemPermissionException(filePath, user);
            }
        } catch (final IOException e) {
            throw new FilesystemPermissionException(filePath, user, e);
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
            System.err.println("\nERROR: at least one file in " + path + " is not owned by " + user + ".");
            System.err.println("Make sure $RUNAS in opennms.conf matches the permissions of " + path +
                    " and its subdirectories.\n");
            e.printStackTrace();
            System.err.println();
        }
    }
}
