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
