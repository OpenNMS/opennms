/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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

package liquibase.ext2.cm.change;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.config.exception.ConfigIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import liquibase.exception.ValidationErrors;
import liquibase.util.file.FilenameUtils;

class ConfigFileUtil {
    public static String OPENNMS_HOME = System.getProperty("opennms.home", "");

    static Path validateAndGetArchiveDir(ValidationErrors validationErrors) {
        Path archivePath = Paths.get(OPENNMS_HOME, "etc_archive");
        try {
            if (!Files.exists(archivePath)) {
                Files.createDirectory(archivePath);
            }
            if (!Files.isDirectory(archivePath)) {
                validationErrors.addError(String.format("Archive directory %s is not a directory.", archivePath));
            }
            if(!Files.isWritable(archivePath)) {
                validationErrors.addError(String.format("Archive directory %s is not writable.", archivePath));
            }
        } catch(Exception e) {
            validationErrors.addError(String.format("Can not find or create archive directory %s: %s", archivePath, e.getMessage()));
        }
        return archivePath;
    }

    static Collection<Resource> findConfigFiles(String fileName) {
        Map<String, Resource> allConfigFiles = new HashMap<>();

        // find config files in defaults
        String resourcePattern = String.format("classpath*:/defaults/%s", fileName);
        allConfigFiles.putAll(findFiles(resourcePattern));

        // find config files in etc (might override default)
        resourcePattern = String.format("file:%s/etc/%s", OPENNMS_HOME, fileName);
        allConfigFiles.putAll(findFiles(resourcePattern));

        return allConfigFiles.values();
    }

    static Map<String, Resource> findFiles(String resourcePattern) {
        Map<String, Resource> configs = new HashMap<>();
        try {
            Resource[] configFiles = new PathMatchingResourcePatternResolver().getResources(resourcePattern);
            for (Resource config : configFiles) {
                if (config.isReadable()) {
                    configs.put(config.getFilename(), config);
                }
            }
        } catch (IOException e) {
            throw new ConfigIOException("An Exception occurred while trying to find files for " + resourcePattern, e);
        }
        return configs;
    }

    static void checkFileType(ValidationErrors validationErrors, Set<String> allowedExtension, String filePath) {
        Objects.requireNonNull(validationErrors);
        Objects.requireNonNull(allowedExtension);

        if (filePath == null) {
            return; // nothing to do
        }

        String fileType = FilenameUtils.getExtension(filePath).toLowerCase();
        if (!allowedExtension.contains(fileType)) {
            validationErrors.addError(String.format("Unknown file type: '%s'", fileType));
        }
    }

    static String asString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
