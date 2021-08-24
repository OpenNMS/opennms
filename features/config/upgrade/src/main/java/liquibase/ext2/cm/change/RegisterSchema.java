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

import javax.xml.bind.JAXBException;

import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;

/** Used in changelog.xml */
@DatabaseChange(name = "registerSchema", description = "Registers a new schema", priority = ChangeMetaData.PRIORITY_DATABASE)
public class RegisterSchema extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterSchema.class);

    private String id;
    // The Jaxb class that represents a configuration
    private String entityClassName;
    private Class<?> entityClass;
    private String version;
    private ConfigurationManagerService.Version parsedVersion;

    @Override
    public ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        validationErrors.checkRequiredField("id", this.id);
        validationErrors.checkRequiredField("configClassName", this.entityClassName);
        validationErrors.checkRequiredField("version", this.version);

        try {
            this.entityClass = Class.forName(entityClassName);
        } catch(Exception e) {
            validationErrors.addError(String.format("Can not load class %s: %s", this.entityClassName, e.getMessage()));
        }

        try {
            String[] versions = version.split("\\.");
            parsedVersion = new ConfigurationManagerService.Version(
                    Integer.parseInt(versions[0]),
                    Integer.parseInt(versions[1]),
                    Integer.parseInt(versions[2])
            );
        } catch(Exception e) {
            validationErrors.addError(e.getMessage());
        }
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return String.format("Registered new schema with schemaName=%s, configClassName=%s", this.id, this.entityClassName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new GenericCmStatement((ConfigurationManagerService m) -> {
                    LOG.info("Registering new schema with schemaName={}, entityClass={}", this.id, this.entityClassName);
                    try {
                        m.registerSchema(id, parsedVersion, this.entityClass);
                    } catch (IOException | JAXBException e) {
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

    public String getEntityClass() {
        return entityClassName;
    }

    public void setEntityClass(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}


