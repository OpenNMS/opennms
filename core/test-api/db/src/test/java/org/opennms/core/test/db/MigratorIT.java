/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.schema.MigrationException;
import org.opennms.core.schema.Migrator;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Joiner;

/**
 * Note that this test may have some issues inside Eclipse related to
 * Spring's ability to classload the changelog.xml files by using the
 * "classpath*:/changelog.xml" resource identifier. Run it outside Eclipse
 * using mvn if you run into problems.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/migratorTest.xml"
})
@JUnitTemporaryDatabase(createSchema = false)
public class MigratorIT implements TemporaryDatabaseAware<TemporaryDatabase> {
    private static final Logger LOG = LoggerFactory.getLogger(MigratorIT.class);

    @Autowired
    DataSource m_dataSource;

    @Autowired
    ApplicationContext m_context;

    TemporaryDatabase m_database;

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        m_database = database;
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "INFO");
        MockLogAppender.setupLogging();
    }

    @Test
    public void testClasspathResources() throws Exception {
        List<Resource> resources = getTestResources();
        assertEquals(2, resources.size());

        resources = getRealChangelog();
        assertEquals(1, resources.size());
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testUpdate() throws Exception {
        // Make sure there is no databasechangelog table
        assertFalse(changelogExists());

        Resource aResource = null;
        for (final Resource resource : getTestResources()) {
            URI uri = resource.getURI();
            if (uri.getScheme().equals("file") && uri.toString().contains("test-api/schema/a")) {
                aResource = resource;
                break;
            }
            if (uri.getScheme().equals("jar") && uri.toString().contains("test-api.schema.a")) {
                aResource = resource;
                break;
            }
        }
        assertNotNull("aResource must not be null", aResource);

        Set<String> tables = getTables();
        assertFalse("must not contain table 'schematest'", tables.contains("schematest"));

        final Migrator migrator = new Migrator();
        migrator.setApplicationContext(m_context);
        migrator.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migrator.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migrator.setDataSource(m_dataSource);
        migrator.setAdminDataSource(m_dataSource);
        migrator.setValidateDatabaseVersion(false);
        migrator.setCreateUser(false);
        migrator.setCreateDatabase(false);

        LOG.info("Running migration on database: {}", migrator.getDatabaseName());

        migrator.prepareDatabase();
        migrator.migrate(aResource);

        LOG.info("Migration complete: {}", migrator.getDatabaseName());

        tables = getTables();
        assertTrue("must contain table 'schematest'", tables.contains("schematest"));
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testMultipleChangelogs() throws Exception {
        assertFalse(changelogExists());

        final Migrator migrator = new Migrator();
        migrator.setApplicationContext(m_context);

        migrator.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migrator.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));

        migrator.setDataSource(m_dataSource);
        migrator.setAdminDataSource(m_dataSource);
        migrator.setValidateDatabaseVersion(false);
        migrator.setCreateUser(false);
        migrator.setCreateDatabase(false);

        migrator.setLiquibaseChangelogFilter(createTestApiLiquibaseChangelogFilter());

        // this doesn't work as an alternative to the for loop below and I haven't had a chance to dig too deep yet -- dj@
        //migrator.setDatabaseName(m_database.getTestDatabase());
        //migrator.setupDatabase(true, false, false, false, m_context);

        for (final Resource resource : getTestResources()) {
            URI uri = resource.getURI();
            if (uri.getScheme().equals("jar") && !uri.toString().contains("test-api.schema")) continue;
            if (uri.getScheme().equals("file") && !uri.toString().contains("test-api/schema")) continue;
            LOG.info("=== found resource: {} ===", resource);
            migrator.migrate(resource);
        }

        final List<ChangelogEntry> ids = getChangelogEntries();

        assertTrue(ids.size() > 0);
        assertEquals("test-api.schema.a", ids.get(0).getId());
        assertEquals("test-api.schema.b", ids.get(1).getId());
    }

    public static final Predicate<Resource> createTestApiLiquibaseChangelogFilter() {
        return r -> {
            try {
                URI uri = r.getURI();
                final String scheme = uri.getScheme();
                final String uriString = uri.toString();
                return (scheme.equals("file") && uriString.contains("test-api/schema")) ||
                        (scheme.equals("jar") && uriString.contains("test-api.schema"));
            } catch (IOException e) {
                throw new IllegalStateException("Resource is not a URI", e);
            }
        };
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testRealChangelogs() throws Exception {

        assertFalse(changelogExists());

        final Migrator migrator = new Migrator();
        migrator.setApplicationContext(m_context);

        migrator.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migrator.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));

        migrator.setDataSource(m_dataSource);
        migrator.setAdminDataSource(m_dataSource);
        migrator.setValidateDatabaseVersion(false);
        migrator.setCreateUser(false);
        migrator.setCreateDatabase(false);

        for (final Resource resource : getRealChangelog()) {
            LOG.info("=== found resource: {} ===", resource);
            migrator.migrate(resource);
        }

        final List<ChangelogEntry> ids = getChangelogEntries();
        assertTrue(ids.size() > 0);
        // Check to make sure some of the changelogs ran
        assertTrue(ids.stream().anyMatch(id -> "17.0.0-remove-legacy-ipinterface-composite-key-fields".equals(id.getId())));
        assertTrue(ids.stream().anyMatch(id -> "17.0.0-remove-legacy-outages-composite-key-fields".equals(id.getId())));
    }

    @Test
    @JUnitTemporaryDatabase(createSchema=false)
    public void testUpdateTwice() throws Exception {
        assertFalse(changelogExists());

        doMigration();
        assertTrue(changelogExists());
        doMigration();
        assertTrue(changelogExists());
    }

    private void doMigration() throws MigrationException, IOException {
        final Migrator migrator = new Migrator();
        migrator.setApplicationContext(m_context);

        migrator.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migrator.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migrator.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));

        migrator.setDataSource(m_dataSource);

        for (final Resource resource : getTestResources()) {
            migrator.migrate(resource);
        }
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
        final Set<String> procs = new HashSet<>();
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
        final Set<String> tables = new HashSet<>();
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

    private List<ChangelogEntry> getChangelogEntries() throws SQLException {
        final Connection connection = m_dataSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id, md5sum FROM databasechangelog order by id");
            assertTrue(statement.execute());
            ResultSet rs = statement.getResultSet();
            final List<ChangelogEntry> entries = new ArrayList<>();
            while (rs.next()) {
                entries.add(new ChangelogEntry(rs.getString(1), rs.getString(2)));
            }
            return entries;
        } finally {
            connection.close();
        }
    }

    private static class ChangelogEntry {
        private final String m_id;
        private final String m_md5sum;

        public ChangelogEntry(final String id, final String md5sum) {
            m_id = id;
            m_md5sum = md5sum;
        }

        public String getId() {
            return m_id;
        }

        @SuppressWarnings("unused")
        public String getMd5sum() {
            return m_md5sum;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
            result = prime * result + ((m_md5sum == null) ? 0 : m_md5sum.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final ChangelogEntry other = (ChangelogEntry) obj;
            if (m_id == null) {
                if (other.m_id != null) return false;
            } else if (!m_id.equals(other.m_id)) {
                return false;
            }
            if (m_md5sum == null) {
                if (other.m_md5sum != null) return false;
            } else if (!m_md5sum.equals(other.m_md5sum)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return m_id + "=" + m_md5sum;
        }
    }

    private List<Resource> getTestResources() throws IOException {
        return getChangelogs("test-api/schema");
    }

    private List<Resource> getRealChangelog() throws IOException {
        return getChangelogs("core/schema");
    }

    private List<Resource> getChangelogs(String fileMatch) throws IOException {
        String jarMatch = fileMatch.replace('/', '.');
        final List<Resource> resources = new ArrayList<>();
        for (final Resource resource : m_context.getResources(Migrator.LIQUIBASE_CHANGELOG_LOCATION_PATTERN)) {
            URI uri = resource.getURI();
            if (uri.getScheme().equals("file") && !uri.toString().contains(fileMatch)) continue;
            if (uri.getScheme().equals("jar") && !uri.toString().contains(jarMatch)) continue;
            resources.add(resource);
        }

        if (resources.isEmpty() ) {
            fail("Couldn't find changelog.xml in this ApplicationContext ClassLoader hierarchy:\n" +
                    Joiner.on("\n").join(getClassLoaderUrls(m_context)));
        }

        return resources;
    }

    private static Collection<String> getClassLoaderUrls(ApplicationContext context) {
        List<String> urls = new LinkedList<String>();
        for (ApplicationContext c = context; c != null; c = c.getParent()) {
            for (ClassLoader l = c.getClassLoader(); l != null; l = l.getParent()) {
                if (l instanceof URLClassLoader) {
                    for (URL u : ((URLClassLoader)l).getURLs()) {
                        urls.add(u.toString());
                    }
                } else {
                    throw new RuntimeException("Couldn't get URLs for classloader " + l + ", not an instance of URLClassLoader. (This shouldn't happen.)");
                }
            }
        }
        return urls;
    }

}
