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

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Used in changelog.xml */
@DatabaseChange(name = "importSchemaFromXsd", description = "Imports a schema from a xsd file.", priority = ChangeMetaData.PRIORITY_DATABASE)
public class UpgradeSchema extends AbstractSchemaChange {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaChange.class);

    protected String id;
    protected String xsdFileName;
    protected String xsdFileHash;
    protected String rootElement;

    @Override
    public ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        checkRequiredField(validationErrors, "id", this.id);
        checkRequiredField(validationErrors, "xsdFileName", this.xsdFileName);
        checkRequiredField(validationErrors, "rootElement", this.rootElement);
        checkRequiredField(validationErrors, "xsdFileHash", this.xsdFileHash);
        checkHash(validationErrors, this.xsdFileName, this.xsdFileHash);
        return validationErrors;
    }

    private void checkHash(ValidationErrors validationErrors, String xsdFileName, String expectedXsdHash) {
        try {
            String actualHash = HashUtil.getHash(xsdFileName);
            if (!actualHash.equals(xsdFileHash)) {
                validationErrors.addError(String.format("The hashes for the schema file %s don't match." +
                        " Expected from changelog: %s, actual: %s.%n", xsdFileName, expectedXsdHash, actualHash));
            }
        } catch (Exception e) {
            validationErrors.addError(
                    String.format("Cannot compare hashes for the schema file %s." +
                            " Expected from changelog: %s.%n%s", xsdFileName, expectedXsdHash, e.getMessage())
            );
        }
    }

    protected String getChangeName() {
        return "Upgrad";
    }

    protected RunnableWithException getCmFunction(ConfigurationManagerService m) {
        return () -> {
            m.changeConfigDefinition(id, XsdHelper.buildConfigDefinition(id, xsdFileName, rootElement,
                    ConfigurationManagerService.BASE_PATH));
        };
    }

    @Override
    public String getConfirmationMessage() {
        return String.format("%sed new schema with schemaName=%s, xsdName=%s, rootElement=%s",
                getChangeName(), this.id, this.xsdFileName, this.rootElement);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new GenericCmStatement((ConfigurationManagerService m) -> {
                    LOG.info("{}ing new schema with schemaName={}, xsdFileName={}, xsdHash={}, rootElement={}",
                            this.getChangeName(),
                            this.id,
                            this.xsdFileName,
                            this.xsdFileHash,
                            this.rootElement);
                    try {
                        getCmFunction(m).doRun();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                })
        };
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getXsdFileName() {
        return xsdFileName;
    }

    public void setXsdFileName(String xsdFileName) {
        this.xsdFileName = xsdFileName;
    }

    public String getXsdFileHash() {
        return xsdFileHash;
    }

    public void setXsdFileHash(String xsdHash) {
        this.xsdFileHash = xsdHash;
    }

    public String getRootElement() {
        return rootElement;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

}
