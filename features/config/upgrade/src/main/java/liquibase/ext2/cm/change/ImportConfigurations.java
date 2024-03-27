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
