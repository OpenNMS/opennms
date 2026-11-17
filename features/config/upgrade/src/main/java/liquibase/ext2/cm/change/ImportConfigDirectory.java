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

import static liquibase.ext2.cm.change.ConfigFileUtil.findConfigFiles;
import static liquibase.ext2.cm.change.ConfigFileUtil.validateAndGetArchiveDir;
import static liquibase.ext2.cm.change.ImportConfigurationUtil.importConfig;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.opennms.features.config.dao.api.ConfigDefinition;
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
 * Imports every {@code *.xml} file of a directory below {@code etc/} as a configuration of a schema that allows
 * multiple configurations. The configuration id is the file name without {@code .xml}, so
 * {@code jdbc-datacollection.d/mysql.xml} becomes the configuration {@code mysql}.
 *
 * <p>Files are looked up like {@link ImportConfiguration} does: {@code classpath:/defaults/<dir>/}, overridden by
 * {@code etc/<dir>/}, and only if neither has any file, the files archived by an earlier import in
 * {@code etc_archive/<dir>/}. Files imported from {@code etc/<dir>/} are moved to {@code etc_archive/<dir>/}.
 * An empty or missing directory imports nothing.</p>
 *
 * <p>A file named {@code default.xml} becomes the configuration {@code default.d}, because the id
 * {@code default} belongs to the primary file of the schema. A file that can't be imported (e.g. it doesn't obey
 * the schema, or its name is not a valid configuration id, see {@link #CONFIG_ID}) doesn't stop the upgrade: the error
 * is logged and the file stays where it is.</p>
 */
@DatabaseChange(name = "importConfigDir", description = "Imports all xml configurations of a directory.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfigDirectory extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(ImportConfigDirectory.class);

    private static final String EXTENSION = ".xml";

    /**
     * The ids the REST APIs accept: the id becomes part of a REST path and of the file name in the
     * {@code Content-Disposition} header of a download. Same as {@code JdbcDataCollectionConfigRules.SOURCE_ID},
     * which this module can't depend on; keep the two in step.
     */
    static final Pattern CONFIG_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    /** configId of a file named {@code default.xml}, sorts right after {@code default} */
    static final String RENAMED_DEFAULT_CONFIG_ID = ConfigDefinition.DEFAULT_CONFIG_ID + ".d";

    private String schemaId;
    private String dirPath;
    private Path archivePath;
    /** configId -> file, sorted by configId */
    private Map<String, Resource> configResources = new TreeMap<>();

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("dirPath", this.dirPath);
        if (this.dirPath != null && (this.dirPath.contains("*") || this.dirPath.contains("..") || this.dirPath.startsWith("/"))) {
            validationErrors.addError(String.format("dirPath must be a plain directory relative to etc/: %s", this.dirPath));
            return validationErrors;
        }
        if (this.dirPath != null) {
            configResources = findConfigResources(this.dirPath);
            for (Resource resource : configResources.values()) {
                if (!resource.isReadable()) {
                    validationErrors.addError("Found configuration file but can not read it: " + resource.getFilename());
                }
            }
        }
        archivePath = validateAndGetArchiveDir(validationErrors);
        return validationErrors;
    }

    /**
     * Finds the files to import, one per configuration id. Of several archived versions of a file, the newest is used.
     * {@code default.xml} is mapped to {@code default.d}.
     */
    static Map<String, Resource> findConfigResources(final String dirPath) {
        final Map<String, Resource> resources = new TreeMap<>();
        for (Resource resource : findConfigFiles(stripTrailingSlash(dirPath) + "/*" + EXTENSION)) {
            String configId = toConfigId(resource.getFilename());
            if (ConfigDefinition.DEFAULT_CONFIG_ID.equals(configId)) {
                LOG.warn("The id '{}' is reserved for the primary configuration, {}/{} is imported as configuration '{}'.",
                        ConfigDefinition.DEFAULT_CONFIG_ID, dirPath, resource.getFilename(), RENAMED_DEFAULT_CONFIG_ID);
                configId = RENAMED_DEFAULT_CONFIG_ID;
            }
            if (!CONFIG_ID.matcher(configId).matches()) {
                LOG.error("{}/{} is not imported: the configuration id '{}' must start with a letter or digit and contain only "
                                + "letters, digits, '.', '_' and '-'. The file is left in place and ignored from now on: "
                                + "rename it and store it with the REST API.",
                        dirPath, resource.getFilename(), configId);
                continue;
            }
            resources.merge(configId, resource, (a, b) -> {
                final boolean sameFile = toConfigId(a.getFilename()).equals(toConfigId(b.getFilename()));
                final Resource winner = a.getFilename().compareTo(b.getFilename()) >= 0 ? a : b;
                if (!sameFile) {
                    final Resource loser = winner == a ? b : a;
                    LOG.warn("{}/{} and {}/{} both map to configuration '{}'. {} is imported, {} is ignored.",
                            dirPath, a.getFilename(), dirPath, b.getFilename(), RENAMED_DEFAULT_CONFIG_ID,
                            winner.getFilename(), loser.getFilename());
                }
                return winner;
            });
        }
        return resources;
    }

    /**
     * @param fileName e.g. {@code mysql.xml}, or {@code mysql.xml.20260101120000} for an archived file
     * @return the file name without the archive time stamp and {@code .xml}, e.g. {@code mysql}
     */
    static String toConfigId(final String fileName) {
        String name = fileName;
        if (FilenameUtils.getExtension(name).matches("\\d{14}")) {
            name = FilenameUtils.removeExtension(name);
        }
        return FilenameUtils.removeExtension(name);
    }

    private static String stripTrailingSlash(final String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    @Override
    public String getConfirmationMessage() {
        if (this.configResources.isEmpty()) {
            return String.format("No configuration files found in %s for schema=%s.", this.dirPath, this.schemaId);
        }
        return String.format("Imported configurations %s from %s for schema=%s.", this.configResources.keySet(), this.dirPath, this.schemaId);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                // this method is called before validate(), therefore we wrap everything in one statement.
                new GenericCmStatement(this::importConfigs)
        };
    }

    private void importConfigs(ConfigurationManagerService cm) {
        final String dir = stripTrailingSlash(this.dirPath);
        for (Map.Entry<String, Resource> entry : this.configResources.entrySet()) {
            final String etcRelativePath = dir + "/" + entry.getValue().getFilename();
            try {
                importConfig(cm, entry.getValue(), new ConfigUpdateInfo(schemaId, entry.getKey()), archivePath, etcRelativePath);
            } catch (RuntimeException e) {
                // the old DAO did not validate the files, so a file the collector used to accept must not stop the upgrade
                LOG.error("Configuration {} of schema {} was not imported from {}: {}. The file is left in place and ignored from now on: "
                                + "fix it and store it with the configuration manager REST API.",
                        entry.getKey(), schemaId, etcRelativePath, ImportConfigurationUtil.rootCauseMessage(e), e);
            }
        }
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
}
