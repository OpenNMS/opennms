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

package liquibase.ext.opennms.setsequence;

import java.util.List;
import java.util.Map;
import java.util.Random;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

public class SetSequenceGenerator extends AbstractSqlGenerator<SetSequenceStatement> {
    private static final Logger LOG = LogService.getLog(SetSequenceGenerator.class);
    private final String m_tempTableName = "t" + Integer.toHexString(new Random().nextInt());

    @Override
    public boolean supports(final SetSequenceStatement statement, final Database database) {
        return database instanceof PostgresDatabase;
    }

    public String getTempTableName() {
        return m_tempTableName;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ValidationErrors validate(final SetSequenceStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());
        if (statement.getValue() == null) {
            validationErrors.checkRequiredField("table", statement.getTables());
        } else {
            if (statement.getTables().size() > 0) {
                LOG.warning("You have set the sequence to a specific value, but have also provided tables to use for inferring the value.  Using the specific value.");
            }
        }
        return validationErrors;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Sql[] generateSql(final SetSequenceStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT pg_catalog.setval(").append("'").append(statement.getSequenceName()).append("',");
        if (statement.getValue() != null) {
            sb.append(statement.getValue());
        } else {
            sb.append("(SELECT max(").append(m_tempTableName).append(".id)").append("+1").append(" AS id FROM (");

            final List<String> tables = statement.getTables();
            final Map<String,String> columns = statement.getColumns();
            final int tableSize = tables.size();
            for (int i = 0; i < tableSize; i++) {
                // (SELECT max(id) AS id FROM acks LIMIT 1) UNION
                final String tableName = tables.get(i);
                final String columnName = columns.get(tableName);
                sb.append("(SELECT max(").append(columnName).append(") AS id FROM ");
                sb.append(tableName).append(" LIMIT 1)");
                if (i != tableSize - 1) {
                    sb.append(" UNION ");
                }
            }
            sb.append(")");
            sb.append(" AS ").append(m_tempTableName).append(" LIMIT 1)");
        }
        sb.append(",");
        sb.append("true");
        sb.append(");");

        return new Sql[] {
                new UnparsedSql(sb.toString())
        };
    }



}
