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

import static liquibase.ext2.cm.change.ConfigFileUtil.checkFileType;
import static liquibase.ext2.cm.change.ConfigFileUtil.validateAndGetArchiveDir;
import static liquibase.ext2.cm.change.ImportConfigurationUtil.importConfig;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.Substring;
import org.springframework.core.io.Resource;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;

/**
 * Imports existing configurations for the same schema. They can either live in  {opennms.home}/etc (user defined) or in the class path (default).
 */
@DatabaseChange(name = "importConfigs", description = "Imports configurations. Only osgi (.cfg) files are supported.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class ImportConfigurations extends AbstractCmChange {

    private final static Set<String> ALLOWED_EXTENSIONS = Set.of("cfg");

    private String schemaId;
    private String filePath;
    private Path archivePath;
    private Collection<Resource> configResources;

    @Override
    public ValidationErrors validate(CmDatabase db, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("schemaId", this.schemaId);
        validationErrors.checkRequiredField("filePath", this.filePath);
        checkFileType(validationErrors, ALLOWED_EXTENSIONS, this.filePath);
        checkForWildcard(validationErrors);
        configResources = ConfigFileUtil.findConfigFiles(this.filePath);
        for(Resource resource : configResources) {
           if(!resource.isReadable()) {
               validationErrors.addError("Found configuration file but can not read it: " + resource.getFilename());
           }
        }
        archivePath = validateAndGetArchiveDir(validationErrors);
        return validationErrors;
    }

    void checkForWildcard(ValidationErrors validationErrors) {

        if (this.filePath == null) {
            return; // nothing to do
        }
        if (!this.filePath.endsWith("-*.cfg")) {
            validationErrors.addError(String.format("filePath doesn't end in '-*.cfg', check your filePath: %s", filePath));
        }
    }

    @Override
    public String getConfirmationMessage() {
        if(this.configResources.isEmpty()) {
            return String.format("No configurations files found with pattern %s for schema=%s.", this.filePath, this.schemaId);
        } else {
            return String.format("Imported configurations with pattern %s for schema=%s:%n%s", this.filePath, this.schemaId,
                    this.configResources.stream()
                            .map(Resource::getFilename)
                            .collect(Collectors.joining("%n")));
        }
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                // this method is called before validate(), therefore we wrap everything in one statement.
                new GenericCmStatement(this::importConfigs)
        };
    }

    private void importConfigs(ConfigurationManagerService cm) {
        for(Resource config : this.configResources) {
            String configId = new Substring(config.getFilename())
                    .getAfterLast("-")
                    .getBeforeLast(".cfg")
                    .toString();
            importConfig(cm, config, new ConfigUpdateInfo(schemaId, configId), archivePath);
        }
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
