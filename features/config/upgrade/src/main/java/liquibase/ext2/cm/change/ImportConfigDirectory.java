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

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
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
 */
@DatabaseChange(name = "importConfigDir", description = "Imports all xml configurations of a directory.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfigDirectory extends AbstractCmChange {

    private static final String EXTENSION = ".xml";

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
     */
    static Map<String, Resource> findConfigResources(final String dirPath) {
        final Map<String, Resource> resources = new TreeMap<>();
        for (Resource resource : findConfigFiles(stripTrailingSlash(dirPath) + "/*" + EXTENSION)) {
            final String configId = toConfigId(resource.getFilename());
            resources.merge(configId, resource, (a, b) -> a.getFilename().compareTo(b.getFilename()) >= 0 ? a : b);
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
            importConfig(cm, entry.getValue(), new ConfigUpdateInfo(schemaId, entry.getKey()), archivePath,
                    dir + "/" + entry.getValue().getFilename());
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
