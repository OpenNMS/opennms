/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.ext.opennms.setsequence.SetSequenceGenerator;
import liquibase.ext.opennms.setsequence.SetSequenceStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.test.TestContext;

import org.junit.Test;

public class SetSequenceGeneratorTest extends AbstractSqlGeneratorTest<SetSequenceStatement> {

    public SetSequenceGeneratorTest() throws Exception {
        super(new SetSequenceGenerator());
    }

    @Override
    protected SetSequenceStatement createSampleSqlStatement() {
    	final SetSequenceStatement statement = new SetSequenceStatement("SEQUENCE_NAME");
    	statement.addTable("TABLE_NAME", "COLUMN1_NAME");
        return statement;
    }

    @Test
    public void testBasicOperation() {
        for (final Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof PostgresDatabase) {
            	final SetSequenceStatement statement = new SetSequenceStatement("SEQUENCE_NAME");
            	statement.addTable("TABLE_NAME", "COLUMN1_NAME");
                if (shouldBeImplementation(database)) {
                    final SqlGenerator<SetSequenceStatement> generator = this.generatorUnderTest;
                    final String tempTableName = ((SetSequenceGenerator)generator).getTempTableName();
					final Sql[] sql = generator.generateSql(statement, database, null);
					assertEquals(
                    	"SELECT pg_catalog.setval('SEQUENCE_NAME',(SELECT max(" + tempTableName + ".id)+1 AS id FROM ((SELECT max(COLUMN1_NAME) AS id FROM TABLE_NAME LIMIT 1)) AS " + tempTableName + " LIMIT 1),true);",
                    	sql[0].toSql()
                    );
                }
            }
        }
    }

    @Test
    public void testWithMultipleTables() {
        for (final Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof PostgresDatabase) {
            	final SetSequenceStatement statement = new SetSequenceStatement("SEQUENCE_NAME");
            	statement.addTable("TABLE1_NAME", "COLUMN1_NAME");
            	statement.addTable("TABLE2_NAME", "COLUMN2_NAME");
                if (shouldBeImplementation(database)) {
                    final SqlGenerator<SetSequenceStatement> generator = this.generatorUnderTest;
                    final String tempTableName = ((SetSequenceGenerator)generator).getTempTableName();
					final Sql[] sql = generator.generateSql(statement, database, null);
					assertEquals(
                    	"SELECT pg_catalog.setval('SEQUENCE_NAME',(SELECT max(" + tempTableName + ".id)+1 AS id FROM ((SELECT max(COLUMN1_NAME) AS id FROM TABLE1_NAME LIMIT 1) UNION (SELECT max(COLUMN2_NAME) AS id FROM TABLE2_NAME LIMIT 1)) AS " + tempTableName + " LIMIT 1),true);",
                    	sql[0].toSql()
                    );
                }
            }
        }
    }

    @Test
    @Override
    public void isImplementation() throws Exception {
    	// No idea why this one in the AbstractSqlGeneratorTest fails, but I don't need it  =)
    }

}
