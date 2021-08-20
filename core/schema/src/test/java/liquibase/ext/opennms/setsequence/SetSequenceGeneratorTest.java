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
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class SetSequenceGeneratorTest {

    protected SetSequenceGenerator generator;
    private Set<Database> allDatabases;

    @Before
    public void setUp() {
        allDatabases = new HashSet<>(DatabaseFactory.getInstance().getImplementedDatabases());
        generator = new SetSequenceGenerator();
    }

    private SetSequenceStatement createSampleSqlStatement() {
    	final SetSequenceStatement statement = new SetSequenceStatement("SEQUENCE_NAME");
    	statement.addTable("TABLE_NAME", "COLUMN1_NAME");
        return statement;
    }

    @Test
    public void testBasicOperation() {
        for (final Database database : allDatabases) {
            if (database instanceof PostgresDatabase) {
            	final SetSequenceStatement statement = new SetSequenceStatement("SEQUENCE_NAME");
            	statement.addTable("TABLE_NAME", "COLUMN1_NAME");
                final String tempTableName = (generator).getTempTableName();
                final Sql[] sql = generator.generateSql(statement, database, null);
                assertEquals(
                        "SELECT pg_catalog.setval('SEQUENCE_NAME',(SELECT max(" + tempTableName + ".id)+1 AS id FROM ((SELECT max(COLUMN1_NAME) AS id FROM TABLE_NAME LIMIT 1)) AS " + tempTableName + " LIMIT 1),true);",
                        sql[0].toSql()
                );
            }
        }
    }

    @Test
    public void testWithMultipleTables() {
        for (final Database database : allDatabases) {
            if (database instanceof PostgresDatabase) {
            	final SetSequenceStatement statement = new SetSequenceStatement("SEQUENCE_NAME");
            	statement.addTable("TABLE1_NAME", "COLUMN1_NAME");
            	statement.addTable("TABLE2_NAME", "COLUMN2_NAME");
                final String tempTableName = generator.getTempTableName();
                final Sql[] sql = generator.generateSql(statement, database, null);
                assertEquals(
                        "SELECT pg_catalog.setval('SEQUENCE_NAME',(SELECT max(" + tempTableName + ".id)+1 AS id FROM ((SELECT max(COLUMN1_NAME) AS id FROM TABLE1_NAME LIMIT 1) UNION (SELECT max(COLUMN2_NAME) AS id FROM TABLE2_NAME LIMIT 1)) AS " + tempTableName + " LIMIT 1),true);",
                        sql[0].toSql()
                );
            }
        }
    }

    @Test
    public void isValid() throws Exception {
        for (Database database : allDatabases) {
            assertFalse("isValid failed against " + database, generator.validate(createSampleSqlStatement(),
                    database,
                    new SqlGeneratorChain<>(new TreeSet<>())).hasErrors());
        }
    }

}
