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
// TODO : Patrick: this is the wrong copyright header but I copied it in here so our checks will work...

package liquibase.sqlgenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.test.TestContext;

// TODO: Patrick: find a better solution:
//  I copied this class from the liquibase source since it is not available as jar in the Maven repo:
// in 3.6.3 we had: https://repo1.maven.org/maven2/org/liquibase/liquibase-core/3.6.3/liquibase-core-3.6.3-tests.jar
// in 4.4.3 it is missing: https://repo1.maven.org/maven2/org/liquibase/liquibase-core/4.4.3/liquibase-core-4.4.3-tests.jar doesnt exist
public abstract class AbstractSqlGeneratorTest<T extends SqlStatement> {

    protected SqlGenerator<T> generatorUnderTest;

    public AbstractSqlGeneratorTest(SqlGenerator<T> generatorUnderTest) throws Exception {
        this.generatorUnderTest = generatorUnderTest;
    }

    protected abstract T createSampleSqlStatement();

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, DatabaseException {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    @Test
    public void isImplementation() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            boolean isImpl = generatorUnderTest.supports(createSampleSqlStatement(), database);
            if (shouldBeImplementation(database)) {
                assertTrue("Unexpected false supports for " + database.getShortName(), isImpl);
            } else {
                assertFalse("Unexpected true supports for " + database.getShortName(), isImpl);
            }
        }
    }

    @Test
    public void isValid() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (shouldBeImplementation(database)) {
                if (waitForException(database)) {
                    assertTrue("The validation should be failed for " + database, generatorUnderTest.validate(createSampleSqlStatement(), database, new MockSqlGeneratorChain()).hasErrors());
                } else {
                    assertFalse("isValid failed against " + database, generatorUnderTest.validate(createSampleSqlStatement(), database, new MockSqlGeneratorChain()).hasErrors());
                }

            }
        }
    }

    @Test
    public void checkExpectedGenerator() {
        assertEquals(this.getClass().getName().replaceFirst("Test$", ""), generatorUnderTest.getClass().getName());
    }

    protected boolean waitForException(Database database) {
        return false;
    }

    protected boolean shouldBeImplementation(Database database) {
        return true;
    }



}
