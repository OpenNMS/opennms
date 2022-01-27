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


