/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.schema.ExistingResourceAccessor;
import org.opennms.core.schema.Migration;
import org.opennms.core.schema.Migrator;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/migratorTest.xml"
})
@JUnitTemporaryDatabase
public class MigratorTest {

    @Autowired
    DataSource m_dataSource;

    @Autowired
    ResourceLoader m_resourceLoader;

    @Autowired
    ApplicationContext m_context;

    private Migration m_migration;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_migration = new Migration();
        m_migration.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        m_migration.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        m_migration.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        m_migration.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        m_migration.setChangeLog("changelog.xml");
    }

    /**
     * This test is a bit fragile because it relies on the fact that the main changelog.xml will
     * be located in the classpath before the schema.a and schema.b test migrations.
     */
    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testUpdate() throws Exception {
        // Make sure there is no databasechangelog table
        Connection connection = m_dataSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM databasechangelog");
            statement.execute();
            Assert.fail("databasechangelog exists");
        } catch (SQLException e) {
        } finally {
            connection.close();
        }

        for (final Resource resource : m_context.getResources("classpath*:/changelog.xml")) {
            LogUtils.infof(this, "=== found resource: " + resource + " ===");
        }

        // Make sure that none of the tables that are added during the migration are present
        Connection conn = null;
        try {
            conn = m_dataSource.getConnection();

            Set<String> tables = new HashSet<String>();
            boolean first = true;
            StringBuffer tableNames = new StringBuffer();
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME").toLowerCase();
                tables.add(tableName);
                if (!first) { 
                    tableNames.append(",\n ");
                }
                tableNames.append(tableName);
                first = false;
            }
            LogUtils.infof(this, "Tables in database before migration:\n %s\n", tableNames);
            assertFalse("must not contain table 'alarms'", tables.contains("alarms"));

            Set<String> procs = new HashSet<String>();
            rs = conn.getMetaData().getProcedures(null, null, "%");
            while (rs.next()) {
                procs.add(rs.getString("PROCEDURE_NAME").toLowerCase());
            }
            LogUtils.infof(this, "procs = %s", procs);
            assertFalse("must not have stored procedure 'setSnmpInterfaceKeysOnUpdate'", procs.contains("setsnmpinterfacekeysonupdate"));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        LogUtils.infof(this, "Running migration on database: %s", m_migration.toString());

        Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.setAdminDataSource(m_dataSource);
        m.setValidateDatabaseVersion(false);
        m.setCreateUser(false);
        m.setCreateDatabase(false);

        m.prepareDatabase(m_migration);
        m.migrate(m_migration);

        // Make sure that the tables were created by the migration properly
        try {
            conn = m_dataSource.getConnection();

            Set<String> tables = new HashSet<String>();
            boolean first = true;
            StringBuffer tableNames = new StringBuffer();
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME").toLowerCase();
                tables.add(tableName);
                if (!first) { 
                    tableNames.append(",\n ");
                }
                tableNames.append(tableName);
                first = false;
            }
            LogUtils.infof(this, "Tables in database after migration:\n %s\n", tableNames);
            assertTrue("must contain table 'alarms'", tables.contains("alarms"));

            Set<String> procs = new HashSet<String>();
            rs = conn.getMetaData().getProcedures(null, null, "%");
            while (rs.next()) {
                procs.add(rs.getString("PROCEDURE_NAME").toLowerCase());
            }
            LogUtils.infof(this, "procs = %s ", procs);
            assertTrue("must have stored procedure 'setSnmpInterfaceKeysOnUpdate'", procs.contains("setsnmpinterfacekeysonupdate"));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testMultipleChangelogs() throws Exception {
        // Make sure there is no databasechangelog table
        Connection connection = m_dataSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM databasechangelog");
            statement.execute();
            Assert.fail("databasechangelog exists");
        } catch (SQLException e) {
        } finally {
            connection.close();
        }

        final Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.setAdminDataSource(m_dataSource);
        m.setValidateDatabaseVersion(false);
        m.setCreateUser(false);
        m.setCreateDatabase(false);

        // Add a resource accessor to the migration so that it will load multiple changelog.xml files
        // from the classpath
        for (final Resource resource : m_context.getResources("classpath*:/changelog.xml")) {
            LogUtils.infof(this, "=== found resource: " + resource + " ===");
            m_migration.setAccessor(new ExistingResourceAccessor(resource));
            m.migrate(m_migration);
        }

        connection = m_dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM databasechangelog");
        assertTrue(statement.execute());
        ResultSet rs = statement.getResultSet();
        List<String> ids = new ArrayList<String>();
        while (rs.next()) {
            ids.add(rs.getString(1));
        }
        
        assertTrue(ids.size() > 0);
        assertTrue(ids.contains("test-api.schema.a"));
        assertTrue(ids.contains("test-api.schema.b"));
    }
    
    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testUpdateTwice() throws Exception {
        final Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.migrate(m_migration);
        m.migrate(m_migration);
    }
}
