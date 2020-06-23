/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package liquibase.ext.opennms.dropconstraint;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="dropForeignKeyConstraint", description = "Drops an existing foreign key", priority = ChangeMetaData.PRIORITY_DEFAULT + 1, appliesTo = "foreignKey")
public class DropForeignKeyConstraintCascadeChange extends DropForeignKeyConstraintChange {

    private String m_cascade = "false";

    public DropForeignKeyConstraintCascadeChange() {
        super();
    }

    public String getCascade() {
        return m_cascade;
    }

    public void setCascade(final String cascade) {
        m_cascade = cascade;
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        return DropForeignKeyConstraintCascadeStatement.createFromSqlStatements(super.generateStatements(database), Boolean.valueOf(m_cascade));
    }

}
