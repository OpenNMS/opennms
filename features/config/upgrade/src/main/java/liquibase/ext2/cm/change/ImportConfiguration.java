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

package liquibase.ext2.cm.change;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = "importConfig", description = "Imports a configuration from file.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfiguration extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterSchema.class);

    private String schemaId;
    private String configId;
    private String filePath;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("configId", this.configId);
        validationErrors.checkRequiredField("filePath", this.filePath);

        try {
            File file = ResourceUtils.getFile(this.filePath);
            if(!file.canRead()) {
                validationErrors.addError(String.format("Can not find file %s", this.filePath));
            }
        } catch(Exception e) {
            validationErrors.addError(String.format("Can not find file %s: %s", this.filePath, e.getMessage()));
        }
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return String.format("Imported configuration from %s with id=%s for schema=%s", this.filePath, this.configId, this.schemaId);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new GenericCmStatement((ConfigurationManagerService m) -> {
                    LOG.info("Importing configuration from {} with id={} for schema={}", this.filePath, this.configId, this.schemaId);
                    try {
                        Optional<ConfigSchema<?>> configSchema = m.getRegisteredSchema(this.schemaId);
                        String xmlStr = Files.readString(ResourceUtils.getFile(this.filePath).toPath());
                        JSONObject configObject = new JSONObject(configSchema.get().getConverter().xmlToJson(xmlStr));
                        m.registerConfiguration(this.schemaId, this.configId, configObject);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
        };
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getConfigId() {
        return configId;
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
