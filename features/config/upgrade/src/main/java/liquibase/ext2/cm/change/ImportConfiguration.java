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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.change.converter.PropertiesToJson;
import liquibase.ext2.cm.change.converter.XmlToJson;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.file.FilenameUtils;

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
    private Path etcFile = null; // user defined file
    private Resource configResource;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("configId", this.configId);
        validationErrors.checkRequiredField("filePath", this.filePath);

        String opennmsHome = System.getProperty(SYSTEM_PROP_OPENNMS_HOME, "");
        this.etcFile = Path.of(opennmsHome + "/etc/"+this.filePath);
        configResource = new FileSystemResource(etcFile.toString()); // check etc dir first
        if (!configResource.isReadable()) {
            configResource = new ClassPathResource("/defaults/"+this.filePath); // fallback: default config
            this.etcFile = null;
        }
        if (!configResource.isReadable()) {
            validationErrors.addError(String.format("Cannot read configuration in file: %s/etc/%s or in classpath: /defaults/%s",
                    opennmsHome, this.filePath, this.filePath));
        }
        checkArchiveDir(validationErrors);
        checkFileType(validationErrors);
        return validationErrors;
    }

    void checkArchiveDir(ValidationErrors validationErrors) {
        try {
            String opennmsHome = System.getProperty(SYSTEM_PROP_OPENNMS_HOME, "");
            this.archivePath = Paths.get(opennmsHome, "etc_archive");
            if (!Files.exists(this.archivePath)) {
                Files.createDirectory(this.archivePath);
            }
            if (!Files.isDirectory(this.archivePath)) {
                validationErrors.addError(String.format("Archive directory %s is not a directory.", this.archivePath));
            }
            if(!Files.isWritable(this.archivePath)) {
                validationErrors.addError(String.format("Archive directory %s is not writable.", this.archivePath));
            }
        } catch(Exception e) {
            validationErrors.addError(String.format("Can not find or create archive directory %s: %s", this.archivePath, e.getMessage()));
        }
    }

    void checkFileType(ValidationErrors validationErrors) {

        if(this.filePath == null) return; // nothing to do

        String fileType = FilenameUtils.getExtension(this.filePath);
        if (!"xml".equalsIgnoreCase(fileType) && !"cfg".equalsIgnoreCase(fileType)) {
            validationErrors.addError(String.format("Unknown file type: '%s'", fileType));
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

                        String fileType = FilenameUtils.getExtension(this.filePath);
                        JsonAsString configObject;
                        if("xml".equalsIgnoreCase(fileType)) {
                            configObject = new XmlToJson(asString(this.configResource), configSchema.get()).getJson();
                        } else if("cfg".equalsIgnoreCase(fileType)) {
                            configObject = new PropertiesToJson(this.configResource.getInputStream()).getJson();
                        } else {
                            throw new IllegalArgumentException(String.format("Unknown file type: '%s'", fileType));
                        }
                        m.registerConfiguration(this.schemaId, this.configId, configObject);
                        LOG.info("Configuration with id={} imported.", this.configId);
                        if(etcFile != null) {
                            // we imported a user defined config file => move to archive
                            Path archiveFile = Path.of(this.archivePath + "/" + etcFile.getFileName());
                            Files.move(etcFile, archiveFile); // move to archive
                            LOG.info("Configuration file {} moved to {}", etcFile, this.archivePath);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
        };
    }

    private static String asString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
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
