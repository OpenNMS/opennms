/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.test.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;

public class TemporaryDatabasePostgreSQLIT {
    //private static final Logger LOG = LoggerFactory.getLogger(DatabasePopulatorIT.class);

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testOnce() throws Throwable {
        TemporaryDatabasePostgreSQL db = new TemporaryDatabasePostgreSQL();
        db.setupDatabase();
        assertEquals(1, db.countRows("SELECT 1", new Object[0]));
        db.destroyTestDatabase();
    }

    @Test
    public void testOncePopulate() throws Throwable {
        TemporaryDatabasePostgreSQL db = new TemporaryDatabasePostgreSQL();
        db.setPopulateSchema(true);
        db.setupDatabase();
        assertEquals(1, db.countRows("SELECT * FROM monitoringsystems", new Object[0]));
        db.destroyTestDatabase();
    }

    @Test
    public void testRealChangelog() throws Throwable {
        String dbName = TemporaryDatabasePostgreSQL.TEMPLATE_DATABASE_NAME_PREFIX + System.currentTimeMillis();

        TemporaryDatabasePostgreSQL temp = new TemporaryDatabasePostgreSQL();
        temp.setPopulateSchema(true);
        temp.createIntegrationTestTemplateDatabase(dbName);
        temp.setupDatabase();
        assertEquals(1, temp.countRows("SELECT * FROM monitoringsystems", new Object[0]));
    }

    @Test
    public void testGetIntegrationTestDatabaseName() throws Throwable {
        TemporaryDatabasePostgreSQL temp = new TemporaryDatabasePostgreSQL();
        assertNotNull(temp.getIntegrationTestTemplateDatabaseName());
        assertTrue(temp.getIntegrationTestTemplateDatabaseName().startsWith("opennms_it_template_"));
    }

    @Test
    public void testMultipleThreads() throws Throwable {
        doMultipleThreads(false);
    }

    @Test
    public void testMultipleThreadsPopulate() throws Throwable {
        doMultipleThreads(true);
    }

    private void doMultipleThreads(boolean populate) throws Throwable {
        Runnable r = () -> {
            for (int i = 0; i <= 10; i++) {
                try {
                    TemporaryDatabasePostgreSQL db = new TemporaryDatabasePostgreSQL();
                    // System.out.println(Thread.currentThread() + " " + i + " " + db);
                    db.setPopulateSchema(populate);
                    assertEquals(1, db.countRows(populate ? "SELECT * FROM monitoringsystems" : "SELECT 1", new Object[0]));
                    db.setupDatabase();
                    db.destroyTestDatabase();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        Thread t1 = new Thread(r); t1.start();
        Thread t2 = new Thread(r); t2.start();
        Thread t3 = new Thread(r); t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    @Test
    public void testHashesMatch() throws IOException, Exception {
        TemporaryDatabasePostgreSQL temp = new TemporaryDatabasePostgreSQL();

        assertEquals("liquibase configuration hash", temp.generateLiquibaseHash(), temp.generateLiquibaseHash());
    }
}
