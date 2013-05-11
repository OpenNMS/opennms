/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package liquibase.ext.opennms.createtype;

import java.util.List;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

public class CreateTypeGenerator extends AbstractSqlGenerator<CreateTypeStatement> {

        @Override
	public ValidationErrors validate(final CreateTypeStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors errors = new ValidationErrors();
		errors.checkRequiredField("name", statement);
		errors.checkRequiredField("columns", statement);
		return errors;
	}

	// example: CREATE TYPE daily_series AS (ds timestamp without time zone, de timestamp without time zone, dow integer);
        @Override
    public Sql[] generateSql(final CreateTypeStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
    	final StringBuffer sb = new StringBuffer();

    	sb.append("CREATE TYPE " + database.escapeColumnName(null, null, statement.getName()))
    			.append(" AS (");
    	final List<String> columns = statement.getColumns();
		for (int i = 0; i < columns.size(); i++) {
    		final String columnName = columns.get(i);
    		final String columnType = statement.getColumnType(columnName);
    		
    		sb.append(database.escapeColumnName(null, null, columnName));
    		sb.append(" ");
    		sb.append(columnType);
    		if (i < columns.size() - 1) {
    			sb.append(", ");
    		}
    	}
		sb.append(")");
		return new Sql[] {
				new UnparsedSql(sb.toString())
		};
    }
}
