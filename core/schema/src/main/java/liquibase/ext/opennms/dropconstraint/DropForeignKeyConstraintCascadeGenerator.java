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

package liquibase.ext.opennms.dropconstraint;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.DatabaseObject;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.DropForeignKeyConstraintGenerator;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintCascadeGenerator extends DropForeignKeyConstraintGenerator {

	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}

        @Override
    public Sql[] generateSql(final DropForeignKeyConstraintStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
		final Sql[] superSql = super.generateSql(statement, database, sqlGeneratorChain);
		if (statement instanceof DropForeignKeyConstraintCascadeStatement) {
			Boolean cascade = ((DropForeignKeyConstraintCascadeStatement)statement).getCascade();
			if (cascade != null && cascade && database instanceof PostgresDatabase) {
	    		return new Sql[] {
	    				new UnparsedSql(superSql[0].toSql() + " CASCADE", superSql[0].getEndDelimiter(), superSql[0].getAffectedDatabaseObjects().toArray(new DatabaseObject[0]))
	    		};
	    	} else {
	    		return superSql;
	    	}
		} else {
			return superSql;
		}
    }

}
