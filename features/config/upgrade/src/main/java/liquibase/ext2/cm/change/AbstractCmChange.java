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

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.database.CmDatabase;

public abstract class AbstractCmChange extends AbstractChange {

    @Override
    public boolean supports(Database database) {
        return database instanceof CmDatabase;
    }

    @Override
    public boolean generateStatementsVolatile(final Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(final Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);
        return validate((CmDatabase) database, validationErrors);
    }

    protected abstract ValidationErrors validate(CmDatabase database, ValidationErrors validationErrors);

    protected void checkRequiredField(ValidationErrors validationErrors, String name, String value) {
        if (value == null || value.isBlank()) {
            validationErrors.addError(String.format("Attribute %s is missing", name));
        }
    }

    @FunctionalInterface
    protected interface RunnableWithException {
        void doRun() throws Exception;
    }
}
