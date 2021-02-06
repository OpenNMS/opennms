/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.opennms.core.db.install.SimpleDataSource;
import org.opennms.core.schema.ExistingResourceAccessor;
import org.opennms.core.schema.MigrationException;
import org.opennms.core.schema.Migrator;
import org.opennms.core.test.ConfigurationTestUtils;
import org.postgresql.xa.PGXADataSource;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;

import com.google.common.base.Joiner;

import liquibase.Contexts;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TemporaryDatabasePostgreSQL implements TemporaryDatabase {
    private static final String OPENNMS_UNIT_TEST_PROPERTY = "opennms.unit.test";
    private static final String SAMPLE_CHANGELOG_ID = "17.0.0-remove-legacy-ipinterface-composite-key-fields";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TemporaryDatabasePostgreSQL.class);

    protected static final int MAX_DATABASE_DROP_ATTEMPTS = 10;

    public static final String TEMPLATE_DATABASE_NAME_PREFIX = "opennms_it_template_";
    public static final String LIQUIBASE_RELATIVE_PATH = "core/schema/src/main/liquibase/";

    private static final Object DATABASE_CREATION_MUTEX = new Object();

    private static Set<TemporaryDatabasePostgreSQL> s_toDestroy = new HashSet<>();
    private static boolean s_shutdownHookInstalled = false;
    private static String s_templateDatabaseName = null;

    private final Migrator m_migrator = new Migrator();
    private final String m_testDatabase;
    private final String m_driver;
    private final String m_urlBase;
    private final boolean m_useExisting;

    private final JdbcTemplate m_jdbcTemplate;

    private boolean m_populateSchema;
    private boolean m_plpgsqlIplike;

    private DataSource m_dataSource;
    private DataSource m_adminDataSource;
    private PGXADataSource m_xaDataSource;
    private PGXADataSource m_adminXaDataSource;

    private String m_className = "?";
    private String m_methodName = "?";
    private String m_testDetails = "?";
    private String m_blame = null;

    private boolean m_destroyed = false;

    public TemporaryDatabasePostgreSQL() throws Exception {
        this(null);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase) throws Exception {
        this(testDatabase, false);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase, boolean useExisting) throws Exception {
        this(testDatabase,
                System.getProperty(DRIVER_PROPERTY, DEFAULT_DRIVER),
                System.getProperty(URL_PROPERTY, DEFAULT_URL),
                System.getProperty(ADMIN_USER_PROPERTY, DEFAULT_ADMIN_USER),
                System.getProperty(ADMIN_PASSWORD_PROPERTY, DEFAULT_ADMIN_PASSWORD),
                useExisting);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase, String driver, String url,
            String adminUser, String adminPassword) throws Exception {
        this(testDatabase, driver, url, adminUser, adminPassword, false);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase, String driver, String urlBase,
            String adminUser, String adminPassword, boolean useExisting) throws TemporaryDatabaseException {
        // Append the current object's hashcode to make this value truly unique
        m_testDatabase = testDatabase != null ? testDatabase : getDatabaseName(this);
        m_driver = driver;
        m_urlBase = urlBase;
        m_useExisting = useExisting;

        m_jdbcTemplate = new JdbcTemplate(this);

        m_migrator.setAdminUser(adminUser);
        m_migrator.setAdminPassword(adminPassword);
        m_migrator.setDatabaseUser(adminUser);
        m_migrator.setDatabasePassword(adminPassword);

        m_migrator.setValidateDatabaseVersion(true);
        m_migrator.setCreateUser(false);
        m_migrator.setCreateDatabase(true);

        m_migrator.setApplicationContext(new StaticApplicationContext());

        try {
            m_dataSource = new SimpleDataSource(m_driver, m_urlBase + getTestDatabase(), m_migrator.getAdminUser(), m_migrator.getAdminPassword());
            m_adminDataSource = new SimpleDataSource(m_driver, m_urlBase + DEFAULT_ADMIN_USER, m_migrator.getAdminUser(), m_migrator.getAdminPassword());
            m_xaDataSource = createPgXADataSource(m_urlBase + getTestDatabase(), m_migrator.getAdminUser(), m_migrator.getAdminPassword());
            m_adminXaDataSource = createPgXADataSource(m_urlBase + DEFAULT_ADMIN_USER, m_migrator.getAdminUser(), m_migrator.getAdminPassword());
        } catch (final ClassNotFoundException e) {
            throw new TemporaryDatabaseException("Failed to initialize driver " + m_driver + ": " + e.getMessage(), e);
        }

        m_migrator.setDataSource(m_dataSource);
        m_migrator.setAdminDataSource(m_adminDataSource);

        ensureLiquibaseFilesInClassPath();
    }

    private PGXADataSource createPgXADataSource(String url, String adminUser, String adminPassword) {
        PGXADataSource xaDataSource = new PGXADataSource();
        xaDataSource.setUrl(url);
        xaDataSource.setUser(adminUser);
        xaDataSource.setPassword(adminPassword);
        return xaDataSource;
    }

    @Override
    public void setPlpgsqlIplike(final boolean iplike) {
        m_plpgsqlIplike = iplike;
    }

    @Override
    public void setPopulateSchema(boolean populateSchema) {
        m_populateSchema = populateSchema;
    }

    @Override
    public void setClassName(String className) {
        m_className = className;
    }

    @Override
    public void setMethodName(String methodName) {
        m_methodName = methodName;
    }

    @Override
    public void setTestDetails(String testDetails) {
        m_testDetails = testDetails;
    }

    @Override
    public void create() throws TemporaryDatabaseException {
        failIfUnitTest();

        setupDatabase();
    }

    @Override
    public void drop() throws TemporaryDatabaseException {
        if (!m_useExisting) {
            destroyTestDatabase();
            s_toDestroy.remove(this);
        }
    }

    public JdbcTemplate getJdbcTemplate() {
        return m_jdbcTemplate;
    }

    @Override
    public int countRows(String sql, Object... values) {
        RowCountCallbackHandler counter = new RowCountCallbackHandler();
        getJdbcTemplate().query(sql, values, counter);
        return counter.getRowCount();
    }

    @Override
    public String getTestDatabase() {
        return m_testDatabase;
    }

    public synchronized String getIntegrationTestTemplateDatabaseName() throws Throwable {
        if (s_templateDatabaseName != null) {
            return s_templateDatabaseName;
        }

        String dbName = TEMPLATE_DATABASE_NAME_PREFIX + generateLiquibaseHash();
        m_migrator.setDatabaseName(dbName);

        if (!m_migrator.databaseExists()) {
            LOG.debug("Template database did not already exist.");
            createIntegrationTestTemplateDatabase(dbName);
        } else {
            LOG.debug("Template database already exists.");
        }

        s_templateDatabaseName = m_migrator.getDatabaseName();

        return s_templateDatabaseName;
    }

    protected static void failIfUnitTest() throws TemporaryDatabaseException {
        if ("true".equals(System.getProperty(OPENNMS_UNIT_TEST_PROPERTY))) {
            throw new TemporaryDatabaseException("The '" + OPENNMS_UNIT_TEST_PROPERTY + "' property is set to true, " +
                    "however this class is only suitable for integration tests, not unit tests. " +
                    "Please refactor to not use a database or move to an integration test (name your test class *IT.java). " +
                    "See http://wiki.opennms.org/wiki/Test_conventions for details");
        }
    }

    protected static String getDatabaseName(Object hashMe) {
        // Append the current object's hashcode to make this value truly unique
        return String.format("opennms_test_%s_%06d_%s", System.currentTimeMillis(), System.nanoTime(), Math.abs(hashMe.hashCode()));
    }


    public void setupDatabase() throws TemporaryDatabaseException {
        if (!m_useExisting) {
            // Synchronize around a static mutex to prevent multiple connections
            // to the template1 database
            synchronized(DATABASE_CREATION_MUTEX) {
                createTestDatabase();
            }
        }

        // Test connecting to test database and ensuring we can do a basic query
        try {
            getJdbcTemplate().queryForObject("SELECT now()", Date.class);
        } catch (DataAccessException e) {
            throw new TemporaryDatabaseException("Error occurred while testing database is connectable: " + e.getMessage(), e);
        }

        if (!m_useExisting) {
            setupBlame(getJdbcTemplate(), getBlame());
        }
    }

    protected static void setupBlame(JdbcTemplate jdbcTemplate, String blame) {
        jdbcTemplate.update("CREATE TABLE blame (blame TEXT)");
        jdbcTemplate.update("INSERT INTO blame VALUES (?)", blame);
    }

    private String getBlame() {
        if (m_blame == null) {
            m_blame = m_className + "." + m_methodName + ": " + m_testDetails;
        }
        return m_blame;
    }

    private void createTestDatabase() throws TemporaryDatabaseException {
        Connection adminConnection;
        try {
            adminConnection = getAdminDataSource().getConnection();
        } catch (final SQLException e) {
            throw new TemporaryDatabaseException("Failed to get admin connection: " + e.getMessage(), e);
        }

        String dbSource;
        String create;
        if (m_populateSchema) {
            try {
                dbSource = getIntegrationTestTemplateDatabaseName();
            } catch (Throwable e) {
                throw new TemporaryDatabaseException("Failed to get integration test template database name: " + e.getMessage(), e);
            }
            create = "CREATE DATABASE " + getTestDatabase() + " WITH TEMPLATE " + dbSource + " OWNER opennms";
            LOG.debug("Populating schema from template database.");
        } else {
            dbSource = "template1";
            create = "CREATE DATABASE " + getTestDatabase() + " WITH ENCODING='UNICODE'";
            LOG.debug("Populating schema.");
        }

        Statement st = null;
        try {
            st = adminConnection.createStatement();
            st.execute(create);
            registerDestruction();
        } catch (final Throwable e) {
            try {
                st = adminConnection.createStatement();
                String query = "SELECT pid,usename,query,usename FROM pg_stat_activity where datname = '" + dbSource + "'";
                ResultSet rs = st.executeQuery(query);
                System.err.println("*** database activity immediately after exception '" + e + "' ***");
                System.err.println("*** query: '" + query + "' ***");
                while (rs.next()) {
                    System.err.println("pg_stat_activity: " + rs.getInt(1) + ", " + rs.getString(2) + ", " + rs.getString(3) + ", " + rs.getString(4));
                }
                System.err.println("*** end database activity ***");
            } catch (SQLException sqlE) {
                System.err.println("Got an exception while trying to run pg_stat_activity query after a previous exception: " + sqlE);
                sqlE.printStackTrace();
            }
            throw new TemporaryDatabaseException("Failed to create test database " + getTestDatabase() + ": " + e, e);
        } finally {
            SQLException failed = null;
            if (st != null) {
                try {
                    st.close();
                } catch (final SQLException e) {
                    failed = e;
                }
            }
            try {
                adminConnection.close();
            } catch (final SQLException e) {
                if (failed == null) {
                    failed = e;
                }
            }
            if (failed != null) {
                throw new TemporaryDatabaseException("Failed while cleaning up database resources: " + failed, failed);
            }
        }

        if (m_plpgsqlIplike) {
            try {
                m_migrator.dropExistingIpLike();
                m_migrator.createLangPlPgsql();
                m_migrator.setupPlPgsqlIplike();
            } catch (MigrationException e) {
                throw new TemporaryDatabaseException("Failed to load PL/pgSQL iplike function: " + e.getMessage(), e);
            }
        }
    }

    private void registerDestruction() {
        s_toDestroy.add(this);

        if (s_shutdownHookInstalled) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Running " + TemporaryDatabasePostgreSQL.class + " shutdown hook for " + s_toDestroy.size() + " temporary databases");

                if (s_toDestroy.isEmpty()) {
                    return;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (TemporaryDatabasePostgreSQL db : s_toDestroy) {
                    try {
                        System.err.println("Blame for temporary database being removed late: " + db.getBlame());
                        db.destroyTestDatabase();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        s_shutdownHookInstalled = true;
    }

    void destroyTestDatabase() throws TemporaryDatabaseException {
        if (m_useExisting) {
            return;
        }

        if (m_destroyed) {
            System.err.println("Database '" + getTestDatabase() + "' already destroyed");
            // database already destroyed
            return;
        }

        /*
         * Sleep before destroying the test database because PostgreSQL doesn't
         * seem to notice immediately clients have disconnected. Yeah, it's a
         * hack.
         */
        try {
            Thread.sleep(100);
        } catch (final InterruptedException e) {
            throw new TemporaryDatabaseException("Interrupted while waiting for database to shut down.", e);
        }

        Connection adminConnection = null;
        try {
            adminConnection = getAdminDataSource().getConnection();
        } catch (final SQLException e) {
            throw new TemporaryDatabaseException("Failed to get admin database connection: " + e.getMessage(), e);
        }

        try {
            for (int dropAttempt = 0; dropAttempt < MAX_DATABASE_DROP_ATTEMPTS; dropAttempt++) {
                Statement st = null;

                try {
                    st = adminConnection.createStatement();

                    // Attempt to forcefully terminate all connections to the test database
                    // before we issue the DROP DATABASE command
                    try {
                        st.execute("SELECT pg_terminate_backend(procpid) FROM pg_stat_activity WHERE datname = '" + getTestDatabase() + "'");
                    } catch (final SQLException e) {
                        // Eat the exception; this version of the call only works on PostgreSQL 9.1 and earlier
                    }
                    try {
                        st.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '" + getTestDatabase() + "'");
                    } catch (final SQLException e) {
                        // Eat the exception; this version of the call only works on PostgreSQL 9.2 and later
                    }
                    st.execute("DROP DATABASE " + getTestDatabase());
                    break;
                } catch (final SQLException e) {
                    if ((dropAttempt + 1) >= MAX_DATABASE_DROP_ATTEMPTS) {
                        final String message = "Failed to drop test database on last attempt " + (dropAttempt + 1) + ": " + e;
                        System.err.println(new Date().toString() + ": " + message);
                        dumpThreads();

                        TemporaryDatabaseException newException = new TemporaryDatabaseException(message);
                        newException.initCause(e);
                        throw newException;
                    } else {
                        System.err.println(new Date().toString() + ": Failed to drop test database on attempt " + (dropAttempt + 1) + ": " + e);
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException inter) {
                            throw new TemporaryDatabaseException("Interrupted while waiting for next drop attempt.", inter);
                        }
                    }
                } finally {
                    if (st != null) {
                        try {
                            st.close();
                        } catch (final SQLException e) {
                            throw new TemporaryDatabaseException("Error while closing down database statement: " + e.getMessage(), e);
                        }
                        st = null;
                    }
                }
            }
        } finally {
            /*
             * Since we are already going to be throwing an exception at this
             * point, print any further errors to stdout so we don't mask the
             * first failure.
             */
            try {
                adminConnection.close();
            } catch (SQLException e) {
                throw new TemporaryDatabaseException("Error closing administrative database connection after attempting to drop test database: " + e.getMessage(), e);
            }

            /*
             * Sleep after disconnecting from template1, otherwise creating a
             * new test database in future tests may fail. Man, I hate this.
             */
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                throw new TemporaryDatabaseException("Interrupted while waiting for disconnection to complete: " + e.getMessage(), e);
            }
        }

        m_destroyed = true;
        //System.err.println("Database '" + getTestDatabase() + "' destroyed");
    }

    public static void dumpThreads() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        int daemons = 0;
        for (Thread t : threads.keySet()) {
            if (t.isDaemon()) {
                daemons++;
            }
        }
        System.err.println("Thread dump of " + threads.size() + " threads (" + daemons + " daemons):");
        Map<Thread, StackTraceElement[]> sortedThreads = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            @Override
            public int compare(final Thread t1, final Thread t2) {
                return Long.valueOf(t1.getId()).compareTo(Long.valueOf(t2.getId()));
            }
        });
        sortedThreads.putAll(threads);

        for (Entry<Thread, StackTraceElement[]> entry : sortedThreads.entrySet()) {
            Thread thread = entry.getKey();
            System.err.println("Thread " + thread.getId() + (thread.isDaemon() ? " (daemon)" : "") + ": " + thread + " (state: " + thread.getState() + ")");
            for (StackTraceElement e : entry.getValue()) {
                System.err.println("\t" + e);
            }
        }
        System.err.println("Thread dump completed.");
    }


    @Override
    public Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
    }

    public void update(String stmt, Object... values) {
        //        StringBuffer buf = new StringBuffer("[");
        //        for(int i = 0; i < values.length; i++) {
        //            if (i != 0)
        //                buf.append(", ");
        //            buf.append(values[i]);
        //        }
        //        buf.append("]");
        //        MockUtil.println("Executing "+stmt+" with values "+buf);

        getJdbcTemplate().update(stmt, values);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return m_dataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return m_dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        m_dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        m_dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return m_dataSource.getLoginTimeout();
    }

    /** {@inheritDoc} */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }

    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    public XADataSource getAdminXADataSource() {
        return m_adminXaDataSource;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public XADataSource getXADataSource() {
        return m_xaDataSource;
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * <p/>
     * If the receiver implements the interface then the result is the receiver
     * or a proxy for the receiver. If the receiver is a wrapper
     * and the wrapped object implements the interface then the result is the
     * wrapped object or a proxy for the wrapped object. Otherwise return the
     * the result of calling <code>unwrap</code> recursively on the wrapped object
     * or a proxy for that result. If the receiver is not a
     * wrapper and does not implement the interface, then an <code>SQLException</code> is thrown.
     *
     * @param iface A Class defining an interface that the result must implement.
     * @return an object that implements the interface. May be a proxy for the actual implementing object.
     * @throws java.sql.SQLException If no object found that implements the interface
     * @since 1.6
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    /**
     * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
     * for an object that does. Returns false otherwise. If this implements the interface then return true,
     * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
     * object. If this does not implement the interface and is not a wrapper, return false.
     * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
     * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should succeed.
     *
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly wraps an object that does.
     * @throws java.sql.SQLException if an error occurs while determining whether this is a wrapper
     *                               for an object with the given interface.
     * @since 1.6
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //TODO
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return m_xaDataSource.getXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return m_xaDataSource.getXAConnection(user, password);
    }

    public String generateLiquibaseHash()
            throws NoSuchAlgorithmException, IOException, Exception, ChangeLogParseException, LiquibaseException {
        final long start = System.currentTimeMillis();

        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        changeLogParameters.setContexts(new Contexts(StringUtils.splitAndTrim(Migrator.getLiquibaseContexts(), ",")));

        MessageDigest md = MessageDigest.getInstance("MD5");
        List<URI> seenChangeLogs = new LinkedList<>();

        for (Resource resource : m_migrator.getLiquibaseChangelogs(true)) {
            seenChangeLogs.add(resource.getURI());
            DigestUtils.updateDigest(md, resource.getInputStream());

            ResourceAccessor accessor = new ExistingResourceAccessor(resource);
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(Migrator.LIQUIBASE_CHANGELOG_FILENAME, accessor).parse(Migrator.LIQUIBASE_CHANGELOG_FILENAME, changeLogParameters, accessor);

            for (ChangeSet c : changeLog.getChangeSets()) {
                URI uri = resource.createRelative(c.getFilePath()).getURI();
                if (!seenChangeLogs.contains(uri)) {
                    seenChangeLogs.add(uri);
                    for (InputStream s : accessor.getResourcesAsStream(c.getFilePath())) {
                        DigestUtils.updateDigest(md, s);
                    }
                }
            }
        }

        if (seenChangeLogs.isEmpty()) {
            throw new AssertionError("No change logs were found. ClassPath: " + m_migrator.getContextClassLoaderUrls());
        }

        String hash = Hex.encodeHexString(md.digest());

        final long end = System.currentTimeMillis();
        LOG.info("Computed Liquibase schema hash {} in {} seconds on change logs: {}.", hash, (float) (end - start) / 1000, Joiner.on(", ").join(seenChangeLogs));

        return hash;
    }

    protected void createIntegrationTestTemplateDatabase(String dbName)
            throws ClassNotFoundException, MigrationException, Throwable, SQLException {
        m_migrator.setDatabaseName(dbName);

        /*
        // if it's hashed based on liquibase, shouldn't we not need to drop it?
        if (m_migrator.databaseExists()) {
            m_migrator.dropDatabase();
        }
        */

        try {
            // We temporarily use a new data source for the template database with the Migrator
            DataSource templateDataSource;
            try {
                templateDataSource = new SimpleDataSource(m_driver, m_urlBase + m_migrator.getDatabaseName(), m_migrator.getAdminUser(), m_migrator.getAdminPassword());
            } catch (final ClassNotFoundException e) {
                throw new TemporaryDatabaseException("Failed to initialize driver " + m_driver + ": " + e.getMessage(), e);
            }

            m_migrator.setDataSource(templateDataSource);

            m_migrator.setupDatabase(true, true, true, true, false);

            Integer count = new JdbcTemplate(templateDataSource).queryForObject("SELECT count(id) FROM databasechangelog WHERE id = '" + SAMPLE_CHANGELOG_ID + "'", Integer.class);
            assertTrue("couldn't find a sample databasechangelog entry after setting up template database. Looking for ID " + SAMPLE_CHANGELOG_ID, count == 1);
        } catch (Throwable t) {
            if (m_migrator.databaseExists()) {
                try {
                    LOG.warn("Got an exception while setting up database, so removing");
                    m_migrator.dropDatabase();
                } catch (MigrationException e) {
                    System.err.println("Got an exception while setting up database and then got another exception while removing database: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
            throw t;
        } finally {
            m_migrator.setDataSource(m_dataSource);
        }
    }

    private void ensureLiquibaseFilesInClassPath() throws TemporaryDatabaseException {
        try {
            if (!m_migrator.getLiquibaseChangelogs(false).isEmpty()) {
                return;
            }
            
            String migratorClass = "/" + Migrator.class.getName().replace('.', '/') + ".class";
            URL migratorUrl = Migrator.class.getResource(migratorClass);
            if (migratorUrl == null) {
                throw new TemporaryDatabaseException("Could not find resource for Migrator.class anywhere in the classpath with " + migratorClass);
            }

            GenericApplicationContext newContext = new GenericApplicationContext(m_migrator.getApplicationContext());
            m_migrator.setApplicationContext(newContext);

            if ("file".equals(migratorUrl.getProtocol()) && migratorUrl.getPath().endsWith("core/schema/target/classes" + migratorClass)) {
                URL[] urls = {new URL(migratorUrl.getProtocol(), migratorUrl.getHost(), migratorUrl.getFile().replaceFirst("core/schema/target/classes/.*$", LIQUIBASE_RELATIVE_PATH))};
                newContext.setClassLoader(new URLClassLoader(urls, newContext.getClassLoader()));
            }
            if (!m_migrator.getLiquibaseChangelogs(false).isEmpty()) {
                return;
            }

            File liquibase = new File(ConfigurationTestUtils.getTopProjectDirectory(), LIQUIBASE_RELATIVE_PATH);
            if (!liquibase.exists()) {
                throw new TemporaryDatabaseException("Could we find liquibase files where we expected: " + liquibase.getAbsolutePath());
            }

            URL[] urls = {liquibase.toURI().toURL()};
            newContext.setClassLoader(new URLClassLoader(urls, newContext.getClassLoader()));

            m_migrator.getLiquibaseChangelogs(true);
        } catch (Exception e) {
            throw new TemporaryDatabaseException(e.getMessage(), e);
        }

    }
}
