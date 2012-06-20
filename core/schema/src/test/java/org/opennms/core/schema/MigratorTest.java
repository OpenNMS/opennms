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

package org.opennms.core.schema;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.core.test.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/migratorTest.xml"
})
@JUnitTemporaryDatabase(createSchema=false)
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

    @Test
    public void testUpdate() throws Exception {
        Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.setAdminDataSource(m_dataSource);
        m.setValidateDatabaseVersion(false);
        m.setCreateUser(false);
        m.setCreateDatabase(false);

        m.prepareDatabase(m_migration);
        m.migrate(m_migration);

        Connection conn = null;
        try {
            conn = m_dataSource.getConnection();

            Set<String> tables = new HashSet<String>();
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            assertTrue("must contain table 'alarms'", tables.contains("alarms"));

            Set<String> procs = new HashSet<String>();
            rs = conn.getMetaData().getProcedures(null, null, "%");
            while (rs.next()) {
                procs.add(rs.getString("PROCEDURE_NAME").toLowerCase());
            }
            System.err.println("procs = " + procs);
            assertTrue("must have stored procedure 'setSnmpInterfaceKeysOnUpdate'", procs.contains("setsnmpinterfacekeysonupdate"));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testMultipleChangelogs() throws Exception {
        final Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.setAdminDataSource(m_dataSource);
        m.setValidateDatabaseVersion(false);
        m.setCreateUser(false);
        m.setCreateDatabase(false);

        Migration migration = new Migration();
        migration.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setChangeLog("changelog.xml");

        for (final Resource resource : m_context.getResources("classpath*:/changelog.xml")) {
            System.err.println("=== found resource: " + resource + " ===");
            migration.setAccessor(new ExistingResourceAccessor(resource));
            m.migrate(migration);
        }

        final Connection connection = m_dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT id FROM databasechangelog");
        assertTrue(statement.execute());
        final ResultSet rs = statement.getResultSet();
        final List<String> ids = new ArrayList<String>();
        while (rs.next()) {
            ids.add(rs.getString(1));
        }
        
        assertTrue(ids.size() > 0);
        assertTrue(ids.contains("test-api.schema.a"));
        assertTrue(ids.contains("test-api.schema.b"));
    }
    
    @Test
    @Ignore("takes a long time, just did this to make sure 'upgrades' would not bomb")
    public void testUpdateTwice() throws Exception {
        final Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.migrate(m_migration);
        m.migrate(m_migration);
    }
}
