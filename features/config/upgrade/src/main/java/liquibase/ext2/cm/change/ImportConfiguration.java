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

package liquibase.ext2.cm.change;

import static liquibase.ext2.cm.change.ConfigFileUtil.OPENNMS_HOME;
import static liquibase.ext2.cm.change.ConfigFileUtil.checkFileType;
import static liquibase.ext2.cm.change.ConfigFileUtil.findConfigFiles;
import static liquibase.ext2.cm.change.ConfigFileUtil.validateAndGetArchiveDir;
import static liquibase.ext2.cm.change.ImportConfigurationUtil.importConfig;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

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

/**
 * Imports an existing configuration. It can either live in {opennms.home}/etc (user defined) or in the class path (default).
 */
@DatabaseChange(name = "importConfig", description = "Imports a configuration from file.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfiguration extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(ImportConfiguration.class);
    private final static Set<String> ALLOWED_EXTENSIONS = Set.of("xml", "cfg");

    private String schemaId;
    private String configId;
    private String filePath;
    private Path archivePath;
    private Resource configResource;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("filePath", this.filePath);
        Optional<Resource> configResource = findConfigFiles(this.filePath).stream().findAny();

        if (configResource.isEmpty() || !configResource.get().isReadable()) {
            validationErrors.addError(String.format("Can not read configuration in file: %s/etc/%s or in classpath: /defaults/%s",
                    OPENNMS_HOME, this.filePath, this.filePath));
        } else {
            this.configResource = configResource.get();
        }

        archivePath = validateAndGetArchiveDir(validationErrors);
        checkFileType(validationErrors, ALLOWED_EXTENSIONS, this.filePath);
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return String.format("Imported configuration from %s with id=%s for schema=%s", this.filePath, getConfigId(), this.schemaId);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new GenericCmStatement((ConfigurationManagerService cm) -> importConfig(cm, this.configResource, new ConfigUpdateInfo(schemaId, getConfigId()), archivePath))
        };
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
