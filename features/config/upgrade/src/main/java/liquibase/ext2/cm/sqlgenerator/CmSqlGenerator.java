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

package liquibase.ext2.cm.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext2.cm.statement.AbstractCmStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

/** NoOps generator since we don't need actual SQL. */
public class CmSqlGenerator extends AbstractSqlGenerator<AbstractCmStatement> {

    @Override
    public ValidationErrors validate(AbstractCmStatement statement, Database database,
                                     SqlGeneratorChain<AbstractCmStatement> sqlGeneratorChain) {
        return null;
    }

    @Override
    public Sql[] generateSql(AbstractCmStatement statement, Database database, SqlGeneratorChain<AbstractCmStatement> sqlGeneratorChain) {
        return new Sql[0];
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return true;
    }
}
