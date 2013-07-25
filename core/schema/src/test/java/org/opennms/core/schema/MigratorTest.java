/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.schema;

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
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.test.mock.MockLogAppender;
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

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testUpdate() throws Exception {
        // Make sure there is no databasechangelog table
        assertFalse(changelogExists());

        Resource aResource = null;
        for (final Resource resource : m_context.getResources("classpath*:/changelog.xml")) {
            if (resource.getURI().toString().contains("test-api.schema.a")) {
                aResource = resource;
            }
        }

        Set<String> tables = getTables();
        assertFalse("must not contain table 'schematest'", tables.contains("schematest"));
        
        final Migration migration = new Migration();
        migration.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setChangeLog("changelog.xml");
        migration.setAccessor(new ExistingResourceAccessor(aResource));

        LogUtils.infof(this, "Running migration on database: %s", migration.toString());

        final Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.setAdminDataSource(m_dataSource);
        m.setValidateDatabaseVersion(false);
        m.setCreateUser(false);
        m.setCreateDatabase(false);

        m.prepareDatabase(migration);
        m.migrate(migration);

        LogUtils.infof(this, "Migration complete: %s", migration.toString());

        tables = getTables();
        assertTrue("must contain table 'schematest'", tables.contains("schematest"));
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

        final Migration migration = new Migration();
        migration.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setChangeLog("changelog.xml");

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
            migration.setAccessor(new ExistingResourceAccessor(resource));
            m.migrate(migration);
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
        final Migration migration = new Migration();
        migration.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setChangeLog("changelog.xml");

        final Migrator m = new Migrator();
        m.setDataSource(m_dataSource);
        m.migrate(migration);
        m.migrate(migration);
    }

    protected boolean changelogExists() throws SQLException {
        final Connection connection = m_dataSource.getConnection();
        boolean exists = false;
        try {
            connection.prepareStatement("SELECT id FROM databasechangelog").execute();
            exists = true;
        } catch (SQLException e) {
        } finally {
            connection.close();
        }
        return exists;
    }

    protected Set<String> getStoredProcedures() throws SQLException {
        final Connection connection = m_dataSource.getConnection();
        final Set<String> procs = new HashSet<String>();
        try {
            final ResultSet rs = connection.getMetaData().getProcedures(null, null, "%");
            while (rs.next()) {
                procs.add(rs.getString("PROCEDURE_NAME").toLowerCase());
            }
        } finally {
            connection.close();
        }
        return procs;
    }

    protected Set<String> getTables() throws SQLException {
        final Connection connection = m_dataSource.getConnection();
        final Set<String> tables = new HashSet<String>();
        try {
            final ResultSet rs = connection.getMetaData().getTables(null, null, "%", null);
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME").toLowerCase();
                tables.add(tableName);
            }
        } finally {
            connection.close();
        }
        return tables;
    }
}
