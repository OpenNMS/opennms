/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import static org.testcontainers.utility.MountableFile.forClasspathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.io.ByteSink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opennms.smoketest.stacks.OverlayFile;
import org.opennms.smoketest.stacks.StackModel;
import org.springframework.core.io.FileSystemResource;

import com.google.common.collect.Maps;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.hubspot.jinjava.Jinjava;

/**
 * Utility functions for managing configuration overlays used in
 * OpenNMS/Minion & Sentinel containers.
 *
 * @author jwhite
 */
public class OverlayUtils {

    public static void copyFiles(List<OverlayFile> files, Path overlayRoot) {
        try {
            for (OverlayFile file : files) {
                final Path target = overlayRoot.resolve(file.getTarget());
                final File targetFile = target.toFile();
                java.nio.file.Files.createDirectories(target.getParent());

                final URL sourceUrl = file.getSource();
                final File sourceFile = toFile(sourceUrl);

                if (sourceFile != null) {
                    // The URL points to a local file on disk
                    if (sourceFile.isDirectory()) {
                        // Copy the directory contents
                        java.nio.file.Files.createDirectories(target);
                        FileUtils.copyDirectory(sourceFile, targetFile);
                    } else {
                        // Copy the file
                        FileUtils.copyFile(sourceFile, targetFile);
                    }
                } else {
                    // Copy the bytes from the URL
                    final ByteSink sink = com.google.common.io.Files.asByteSink(targetFile);
                    try (InputStream is = sourceUrl.openStream()) {
                        sink.writeFrom(is);
                    }
                }

                // Update the permissions if any are set
                if (!file.getPermissions().isEmpty()) {
                    java.nio.file.Files.setPosixFilePermissions(target, file.getPermissions());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File toFile(URL url) {
        try {
            // First try to resolve as URL (file:...)
            return Paths.get(url.toURI()).toFile();
        } catch (URISyntaxException|FileSystemNotFoundException e) {
            return null;
        }
    }

    public static void copyAndTemplate(String classpathResource, Path target, StackModel model) {
        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = Maps.newHashMap();
        context.put("model", model);

        try {
            // Copy the files from the class-path to the target directory
            FileUtils.copyDirectory(new File(forClasspathResource(classpathResource).getFilesystemPath()), target.toFile());

            // Render any .j2 files
            try (Stream<Path> walk = java.nio.file.Files.walk(target)) {
                walk.map(Path::toString)
                        .filter(f -> FilenameUtils.isExtension(f, "j2"))
                        .forEach(template -> {
                            try {
                                final String templateSource = Files.asCharSource(new File(template), StandardCharsets.UTF_8).read();
                                final String renderedTemplate = jinjava.render(templateSource, context);
                                final String targetFile = FilenameUtils.removeExtension(template);
                                Files.asCharSink(new File(targetFile), StandardCharsets.UTF_8).write(renderedTemplate);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeProps(Path dest, Map<String, String> propMap) {
        final Properties props = new Properties();
        props.putAll(propMap);

        try (FileOutputStream fos = new FileOutputStream(dest.toFile())) {
            props.store(fos, "Generated");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFeaturesBoot(Path dest, List<String> features) {
        try {
            final CharSink sink = com.google.common.io.Files.asCharSink(dest.toFile(), StandardCharsets.UTF_8);
            sink.writeLines(features);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setOverlayPermissions(Path overlay) throws IOException {
        final Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        java.nio.file.Files.setPosixFilePermissions(overlay, perms);
    }

    public static void setTempPermissions(Path temp) throws IOException {
        final Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        perms.add(PosixFilePermission.OWNER_WRITE);
        java.nio.file.Files.setPosixFilePermissions(temp, perms);
    }
}
