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

package liquibase.ext.opennms.createindex;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

@DatabaseChange(name="createIndex", description = "Creates an index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT + 1, appliesTo = "index")
public class CreateIndexWithWhereChange extends liquibase.change.core.CreateIndexChange {
    private static final Logger LOG = LogService.getLog(CreateIndexWithWhereChange.class);
    private String m_where;

    public CreateIndexWithWhereChange() {
        super();
    }

    public String getWhere() {
        return m_where;
    }

    public void setWhere(final String where) {
        m_where = where;
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        final SqlStatement[] superStatements = super.generateStatements(database);
        if (m_where == null) return superStatements;

        if (superStatements.length != 1) {
            LOG.warning("expected 1 create index statement, but got " + superStatements.length);
            return superStatements;
        }

        return new SqlStatement[]{
                new CreateIndexWithWhereStatement((CreateIndexStatement)superStatements[0], m_where)
        };
    }

}
