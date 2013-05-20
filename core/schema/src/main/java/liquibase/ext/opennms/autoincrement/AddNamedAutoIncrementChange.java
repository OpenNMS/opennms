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

package liquibase.ext.opennms.autoincrement;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.core.AddAutoIncrementChange;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;

public class AddNamedAutoIncrementChange extends AddAutoIncrementChange {

    private String m_sequenceName;

	public AddNamedAutoIncrementChange() {
    	super();
    	setPriority(getChangeMetaData().getPriority() + 1);
    }

    public String getSequenceName() {
    	return m_sequenceName;
    }
    
    public void setSequenceName(final String sequenceName) {
    	m_sequenceName = sequenceName;
    }
    
    @Override
    public SqlStatement[] generateStatements(final Database database) {
    	final List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (database instanceof PostgresDatabase) {
    		String sequenceName = m_sequenceName;
        	if (m_sequenceName == null) {
        		sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
        		statements.add(new CreateSequenceStatement(getSchemaName(), sequenceName));
        	}
        	statements.add(new SetNullableStatement(getSchemaName(), getTableName(), getColumnName(), null, false));
        	statements.add(new AddDefaultValueStatement(getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), new DatabaseFunction("NEXTVAL('"+sequenceName+"')")));
        	return statements.toArray(new SqlStatement[0]);
        } else {
        	return super.generateStatements(database);
        }
    }

}
