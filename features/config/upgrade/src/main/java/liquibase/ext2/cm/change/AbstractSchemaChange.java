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

import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;
import liquibase.ext2.cm.statement.GenericCmStatement;
import liquibase.statement.SqlStatement;

public abstract class AbstractSchemaChange extends AbstractCmChange {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaChange.class);

    protected String id;
    protected String xsdFileName;
    protected String xsdFileHash;
    protected String rootElement;

    @Override
    public ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors) {
        checkRequiredField(validationErrors, "id", this.id);
        checkRequiredField(validationErrors,"xsdFileName", this.xsdFileName);
        checkRequiredField(validationErrors,"rootElement", this.rootElement);
        checkRequiredField(validationErrors,"xsdFileHash", this.xsdFileHash);
        checkHash(validationErrors,this.xsdFileName, this.xsdFileHash);
        return validationErrors;
    }

    private void checkRequiredField(ValidationErrors validationErrors, String name, String value) {
        if(value == null || value.isBlank()) {
            validationErrors.addError(String.format("Attribute %s is missing", name));
        }
    }

    private void checkHash(ValidationErrors validationErrors, String xsdFileName, String expectedXsdHash) {
        try {
            String actualHash = HashUtil.getHash(xsdFileName);
            if(!actualHash.equals(xsdFileHash)) {
                validationErrors.addError(String.format("The hashes for the schema file %s don't match." +
                        " Expected from changelog: %s, actual: %s.%n", xsdFileName, expectedXsdHash, actualHash));
            }
        } catch (IOException e) {
            validationErrors.addError(String.format("Attribute %s is missing", xsdFileName));
        }
    }

    protected abstract String getChangeName();

    protected abstract RunnableWithException getCmFunction(ConfigurationManagerService m);

    @Override
    public String getConfirmationMessage() {
        return String.format("%sed new schema with schemaName=%s, xsdName=%s, rootElement=%s",
                getChangeName(), this.id, this.xsdFileName, this.rootElement);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
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

    @FunctionalInterface
    protected interface RunnableWithException {
        void doRun() throws Exception;
    }
}
