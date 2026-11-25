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

import static liquibase.ext2.cm.change.ConfigFileUtil.findFiles;
import static liquibase.ext2.cm.change.ConfigFileUtil.getOpennmsHome;
import static liquibase.ext2.cm.change.ConfigFileUtil.validateAndGetArchiveDir;
import static liquibase.ext2.cm.change.ImportConfigurationUtil.importConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.file.FilenameUtils;

/**
 * Imports every xml file of a configuration directory, e.g. {@code etc/xml-datacollection/*.xml}, as one
 * configuration of a schema that allows multiple configurations. The file name without its extension becomes
 * the configId.
 *
 * <p>Files are looked up in {@code classpath*:/defaults/<directory>/} and in {@code {opennms.home}/etc/<directory>/},
 * the latter overriding the former. When neither holds a file, the most recent {@code etc_archive/<directory>.<timestamp>}
 * left behind by an earlier import is used instead, e.g. after a database reset. A directory imported from
 * {@code etc} is moved as a whole to {@code etc_archive/<directory>.<timestamp>}. A missing directory is not an
 * error: there is simply nothing to import.</p>
 */
@DatabaseChange(name = "importConfigDirectory", description = "Imports all xml configurations of a directory.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfigDirectory extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(ImportConfigDirectory.class);
    private static final String EXTENSION = "xml";

    private String schemaId;
    private String directory;
    private Path archivePath;
    /** configId -> file */
    private Map<String, Resource> configResources = new TreeMap<>();
    private Path etcDirectory;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("directory", this.directory);
        if (this.directory == null) {
            return validationErrors;
        }
        if (this.directory.contains("/") || this.directory.contains("\\") || this.directory.contains("..")) {
            validationErrors.addError(String.format("directory must be a plain directory name below etc: %s", this.directory));
            return validationErrors;
        }
        archivePath = validateAndGetArchiveDir(validationErrors);
        etcDirectory = Paths.get(getOpennmsHome(), "etc", this.directory);
        configResources = findConfigFiles();
        for (Resource resource : configResources.values()) {
            if (!resource.isReadable()) {
                validationErrors.addError("Found configuration file but can not read it: " + resource.getFilename());
            }
        }
        return validationErrors;
    }

    private Map<String, Resource> findConfigFiles() {
        final Map<String, Resource> files = new TreeMap<>();
        files.putAll(findFiles(String.format("classpath*:/defaults/%s/*.%s", this.directory, EXTENSION)));
        files.putAll(findFiles(String.format("file:%s/*.%s", etcDirectory, EXTENSION)));
        if (files.isEmpty()) {
            latestArchive().ifPresent(archive -> {
                LOG.info("Configuration directory '{}' not found in etc/ or classpath defaults, using archived version {}", this.directory, archive);
                files.putAll(findFiles(String.format("file:%s/*.%s", archive, EXTENSION)));
            });
        }
        return files.entrySet().stream().collect(Collectors.toMap(
                e -> FilenameUtils.removeExtension(e.getKey()),
                Map.Entry::getValue,
                (a, b) -> b,
                TreeMap::new));
    }

    private Optional<Path> latestArchive() {
        if (archivePath == null || !Files.isDirectory(archivePath)) {
            return Optional.empty();
        }
        try (Stream<Path> entries = Files.list(archivePath)) {
            return entries
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().matches(java.util.regex.Pattern.quote(this.directory) + "\\.\\d{14}"))
                    .max(Comparator.comparing(p -> p.getFileName().toString()));
        } catch (IOException e) {
            LOG.warn("Can not list archive directory {}", archivePath, e);
            return Optional.empty();
        }
    }

    @Override
    public String getConfirmationMessage() {
        if (this.configResources.isEmpty()) {
            return String.format("No configuration files found in directory %s for schema=%s.", this.directory, this.schemaId);
        }
        return String.format("Imported configurations from directory %s for schema=%s: %s", this.directory, this.schemaId,
                String.join(", ", this.configResources.keySet()));
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                // this method is called before validate(), therefore we wrap everything in one statement.
                new GenericCmStatement(this::importConfigs)
        };
    }

    private void importConfigs(ConfigurationManagerService cm) {
        for (Map.Entry<String, Resource> config : this.configResources.entrySet()) {
            importConfig(cm, config.getValue(), new ConfigUpdateInfo(schemaId, config.getKey()), archivePath);
        }
        archiveEtcDirectory();
    }

    private void archiveEtcDirectory() {
        if (etcDirectory == null || !Files.isDirectory(etcDirectory)) {
            return;
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        final Path target = archivePath.resolve(this.directory + "." + formatter.format(LocalDateTime.now()));
        try {
            Files.move(etcDirectory, target);
            LOG.info("Configuration directory {} moved to {}", etcDirectory, target);
        } catch (IOException e) {
            throw new ConfigRuntimeException(String.format("Can not move configuration directory %s to %s", etcDirectory, target), e);
        }
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
