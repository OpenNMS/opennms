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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
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

/**
 * Imports an existing configuration. It can either live in {opennms.home}/etc (user defined) or in the class path (default).
 */
@DatabaseChange(name = "importConfig", description = "Imports a configuration from file.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfiguration extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterSchema.class);
    private final static String SYSTEM_PROP_OPENNMS_HOME = "opennms.home";

    private String schemaId;
    private String configId;
    private String filePath;
    private Path archivePath;
    private Path configFilePath;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("configId", this.configId);
        validationErrors.checkRequiredField("filePath", this.filePath);

        Optional<Path> configPath = getConfigFile("file:${opennms.home}/etc/");
        if (configPath.isEmpty() || !Files.exists(configPath.get())) {
            // fallback
            configPath = getConfigFile("classpath:defaults/");
        }
        if (configPath.isEmpty() || !Files.exists(configPath.get())) {
            validationErrors.addError(String.format("Cannot find file %s in ${opennms.home}/etc/ or in classpath", this.filePath));
        } else {
            this.configFilePath = configPath.get();
        }
        checkArchiveDir(validationErrors);
        return validationErrors;
    }

    Optional<Path> getConfigFile(String prefix) {
        String path = prefix + this.filePath;
        String opennmsHome = System.getProperty(SYSTEM_PROP_OPENNMS_HOME, "");
        path = path.replace("${"+SYSTEM_PROP_OPENNMS_HOME+"}", opennmsHome);

        try {
            return Optional.of(ResourceUtils.getFile(path).toPath());
        } catch(IOException e) {
            return Optional.empty();
        }
    }

    void checkArchiveDir(ValidationErrors validationErrors) {
        try {
            String opennmsHome = System.getProperty(SYSTEM_PROP_OPENNMS_HOME, "");
            this.archivePath = Paths.get(opennmsHome, "etc_archive");
            if (!Files.exists(this.archivePath)) {
                Files.createDirectory(this.archivePath);
            }
            if(!Files.isWritable(this.archivePath)) {
                validationErrors.addError(String.format("Archive directory %s is not writable.", this.archivePath));
            }
        } catch(Exception e) {
            validationErrors.addError(String.format("Can not find or create archive directory %s: %s", this.archivePath, e.getMessage()));
        }
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
                        String xmlStr = Files.readString(configFilePath);
                        JsonAsString configObject = new JsonAsString(configSchema.get().getConverter().xmlToJson(xmlStr));
                        m.registerConfiguration(this.schemaId, this.configId, configObject);
                        LOG.info("Configuration with id={} imported.", this.configId);
                        if(configFilePath.toAbsolutePath().toString().contains("etc")) {
                            // we imported a user defined config file => move to archive
                            Path archiveFile = Path.of(this.archivePath + "/" + configFilePath.getFileName());
                            Files.move(configFilePath, archiveFile); // move to archive
                            LOG.info("Configuration file {} moved to {}", configFilePath, this.archivePath);
                        }
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
