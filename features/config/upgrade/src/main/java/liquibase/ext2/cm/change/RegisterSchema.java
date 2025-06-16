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
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/** Used in changelog.xml */
@DatabaseChange(name = "registerSchema", description = "Registers a new schema", priority = ChangeMetaData.PRIORITY_DATABASE)
public class RegisterSchema extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterSchema.class);

    private String id;
    private Boolean allowMultiple = false;

    @Override
    public ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        checkRequiredField(validationErrors, "id", this.id);
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return String.format("Registered new schema with schemaName=%s", this.id);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new GenericCmStatement((ConfigurationManagerService m) -> {
                    LOG.info("Registering new schema with schemaName={}",
                            this.id);
                    try {
                        ConfigDefinition definition = new ConfigDefinition(this.id, this.allowMultiple);
                        m.registerConfigDefinition(id, definition);
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

    public Boolean getAllowMultiple() {
        return defaultIfNull(this.allowMultiple, false);
    }

    public void setAllowMultiple(Boolean allowMultiple) {
        this.allowMultiple = defaultIfNull(allowMultiple, false);
    }
}


