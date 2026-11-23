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

import static liquibase.ext2.cm.change.ConfigFileUtil.checkFileType;
import static liquibase.ext2.cm.change.ConfigFileUtil.findConfigFiles;
import static liquibase.ext2.cm.change.ConfigFileUtil.getOpennmsHome;
import static liquibase.ext2.cm.change.ConfigFileUtil.validateAndGetArchiveDir;
import static liquibase.ext2.cm.change.ImportConfigurationUtil.importConfig;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ConfigConvertUtil;
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

/**
 * Imports an existing configuration. It can either live in {opennms.home}/etc (user defined) or in the class path (default).
 *
 * <p>With {@code failOnError="false"} a file that can't be imported (e.g. it doesn't obey the schema) doesn't stop the
 * upgrade: the error is logged, nothing is stored, and the file stays where it is. A missing file is skipped the same way,
 * like a missing directory is by {@link ImportConfigDirectory}.</p>
 */
@DatabaseChange(name = "importConfig", description = "Imports a configuration from file.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfiguration extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(ImportConfiguration.class);
    private final static Set<String> ALLOWED_EXTENSIONS = Set.of("xml", "cfg");

    private String schemaId;
    private String configId;
    private String filePath;
    private Boolean failOnError;
    private Path archivePath;
    private Resource configResource;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("filePath", this.filePath);
        Optional<Resource> configResource = findConfigFiles(this.filePath).stream().findAny();

        if (configResource.isEmpty() || !configResource.get().isReadable()) {
            this.configResource = null;
            if (Boolean.FALSE.equals(this.failOnError)) {
                LOG.warn("Configuration file {} not found or not readable in {}/etc/, the classpath defaults or etc_archive/. Nothing is imported for configuration {} of schema {}.",
                        this.filePath, getOpennmsHome(), getConfigId(), this.schemaId);
            } else {
                validationErrors.addError(String.format("Can not read configuration in file: %s/etc/%s or in classpath: /defaults/%s",
                        getOpennmsHome(), this.filePath, this.filePath));
            }
        } else {
            this.configResource = configResource.get();
        }

        archivePath = validateAndGetArchiveDir(validationErrors);
        checkFileType(validationErrors, ALLOWED_EXTENSIONS, this.filePath);
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        if (this.configResource == null) {
            return String.format("No configuration file %s found, nothing imported for schema=%s", this.filePath, this.schemaId);
        }
        return String.format("Imported configuration from %s with id=%s for schema=%s", this.filePath, getConfigId(), this.schemaId);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new GenericCmStatement(this::importConfiguration)
        };
    }

    private void importConfiguration(final ConfigurationManagerService cm) {
        if (this.configResource == null) {
            // missing file with failOnError="false", already logged by validate()
            return;
        }
        try {
            importConfig(cm, this.configResource, new ConfigUpdateInfo(schemaId, getConfigId()), archivePath);
        } catch (RuntimeException e) {
            if (!Boolean.FALSE.equals(this.failOnError)) {
                throw e;
            }
            LOG.error("Configuration {} of schema {} was not imported from {}: {}. The file is left in place and ignored from now on: "
                            + "fix it and store it with the configuration manager REST API.",
                    getConfigId(), schemaId, this.filePath, ConfigConvertUtil.rootCauseMessage(e), e);
        }
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getConfigId() {
        return configId == null ? ConfigDefinition.DEFAULT_CONFIG_ID : configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    /**
     * @return the attribute as given, null if not set (means true). Returning the default here would change the
     *         checksum of changesets that were executed before this attribute existed.
     */
    public Boolean getFailOnError() {
        return failOnError;
    }

    public void setFailOnError(Boolean failOnError) {
        this.failOnError = failOnError;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
