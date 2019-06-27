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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.db.install.SimpleDataSource;
import org.opennms.core.schema.ExistingResourceAccessor;
import org.opennms.core.schema.Migration;
import org.opennms.core.schema.MigrationException;
import org.opennms.core.schema.Migrator;
import org.opennms.core.test.ConfigurationTestUtils;
import org.postgresql.xa.PGXADataSource;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TemporaryDatabasePostgreSQL implements TemporaryDatabase {
    public static class ChangelogEntry {
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

    private static final String OPENNMS_UNIT_TEST_PROPERTY = "opennms.unit.test";

    protected static final int MAX_DATABASE_DROP_ATTEMPTS = 10;

    private static final Object TEMPLATE1_MUTEX = new Object();

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TemporaryDatabasePostgreSQL.class);

    private final String m_testDatabase;

    private final String m_driver;
    private final String m_url;
    private final String m_adminUser;
    private final String m_adminPassword;
    private final boolean m_useExisting;

    private DataSource m_dataSource;
    private DataSource m_adminDataSource;
    private PGXADataSource m_xaDataSource;
    private PGXADataSource m_adminXaDataSource;

    private boolean m_populateSchema = false;

    private boolean m_destroyed = false;

    private JdbcTemplate jdbcTemplate; // this does not have a m_ per our naming conventions to make it similar to Spring.

    private static Set<TemporaryDatabasePostgreSQL> s_toDestroy = new HashSet<>();

    private static boolean s_shutdownHookInstalled = false;

    private static String s_templateDatabaseName;

    private String m_className = "?";

    private String m_methodName = "?";

    private String m_testDetails = "?";

    private String m_blame = null;

    private boolean m_plpgsqlIplike = false;

    public static final String TEMPLATE_DATABASE_NAME_PREFIX = "opennms_it_template_";

    private static final String ADMIN_DATABASE = "postgres";

    public TemporaryDatabasePostgreSQL() throws Exception {
        this(null);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase) throws Exception {
        this(testDatabase, false);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase, boolean useExisting) throws Exception {
        this(testDatabase, System.getProperty(DRIVER_PROPERTY, DEFAULT_DRIVER),
                System.getProperty(URL_PROPERTY, DEFAULT_URL),
                System.getProperty(ADMIN_USER_PROPERTY, DEFAULT_ADMIN_USER),
                System.getProperty(ADMIN_PASSWORD_PROPERTY, DEFAULT_ADMIN_PASSWORD), useExisting);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase, String driver, String url,
            String adminUser, String adminPassword) throws Exception {
        this(testDatabase, driver, url, adminUser, adminPassword, false);
    }

    public TemporaryDatabasePostgreSQL(String testDatabase, String driver, String url,
            String adminUser, String adminPassword, boolean useExisting) {
        // Append the current object's hashcode to make this value truly unique
        m_testDatabase = testDatabase != null ? testDatabase : getDatabaseName(this);
        m_driver = driver;
        m_url = url;
        m_adminUser = adminUser;
        m_adminPassword = adminPassword;
        m_useExisting = useExisting;
    }

    public synchronized static String getIntegrationTestDatabaseName() throws Throwable {
        if (s_templateDatabaseName != null) {
            return s_templateDatabaseName;
        }
//            GenericApplicationContext context = (GenericApplicationContext) BeanUtils.getBeanFactory("daoContext");
//            context = Migrator.ensureLiquibaseFilesInClassPath(context);
//            System.err.println("******* from getIntegrationTestDatabaseName: " + Migrator.getClassLoaderUrls(TemporaryDatabasePostgreSQL.class.getClassLoader()));
        StaticApplicationContext staticContext = new StaticApplicationContext();
        staticContext.setClassLoader(TemporaryDatabasePostgreSQL.class.getClassLoader());
        GenericApplicationContext context = ensureLiquibaseFilesInClassPath(staticContext);

        String hash = generateLiquibaseHash(context);

        String dbName = TEMPLATE_DATABASE_NAME_PREFIX + hash;

        final Migration migration = createMigration(dbName);

        DataSource dataSource = new SimpleDataSource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/" + migration.getDatabaseName(), migration.getDatabaseUser(), migration.getDatabasePassword());
        DataSource adminDataSource = new SimpleDataSource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/" + ADMIN_DATABASE, migration.getDatabaseUser(), migration.getDatabasePassword());

        final Migrator migrator = createMigrator(dataSource, adminDataSource);

        if (!migrator.databaseExists(migration)) {
            createIntegrationTestDatabase(context, migration, migrator, dataSource);
        }

        s_templateDatabaseName = dbName;

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

    public void setPopulateSchema(boolean populateSchema) {
        m_populateSchema = populateSchema;
    }

    public void create() throws TemporaryDatabaseException {
        failIfUnitTest();

        setupDatabase();
    }

    protected String getStoredProcDirectory() {
        return ConfigurationTestUtils.getFileForConfigFile("getOutageTimeInWindow.sql").getParentFile().getAbsolutePath();
    }

    public void setupDatabase() throws TemporaryDatabaseException {

        try {
            setDataSource(new SimpleDataSource(m_driver, m_url + getTestDatabase(), m_adminUser, m_adminPassword));
            setAdminDataSource(new SimpleDataSource(m_driver, m_url + ADMIN_DATABASE, m_adminUser, m_adminPassword));
            m_xaDataSource = new PGXADataSource();
            m_xaDataSource.setServerName("localhost");
            m_xaDataSource.setDatabaseName(getTestDatabase());
            m_xaDataSource.setUser(m_adminUser);
            m_xaDataSource.setPassword(m_adminPassword);
            m_adminXaDataSource = new PGXADataSource();
            m_adminXaDataSource.setServerName("localhost");
            m_adminXaDataSource.setDatabaseName(ADMIN_DATABASE);
            m_adminXaDataSource.setUser(m_adminUser);
            m_adminXaDataSource.setPassword(m_adminPassword);
        } catch (final ClassNotFoundException e) {
            throw new TemporaryDatabaseException("Failed to initialize driver " + m_driver + ": " + e.getMessage(), e);
        }

        if (!m_useExisting) {
            // Synchronize around a static mutex to prevent multiple connections
            // to the template1 database
            synchronized(TEMPLATE1_MUTEX) {
                createTestDatabase();
            }
        }

        setJdbcTemplate(new JdbcTemplate(this));

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
            m_blame = getClassName() + "." + getMethodName() + ": " + getTestDetails();
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
        Statement st = null;
        String dbSource = null;
        try {
            st = adminConnection.createStatement();
            String create;
            if (m_populateSchema) {
                dbSource = getIntegrationTestDatabaseName();
                create = "CREATE DATABASE " + getTestDatabase() + " WITH TEMPLATE " + dbSource + " OWNER opennms";
            } else {
                dbSource = "template1";
                create = "CREATE DATABASE " + getTestDatabase() + " WITH ENCODING='UNICODE'";
            }
            st.execute(create);

            if (m_plpgsqlIplike) {
                final Migrator m = createMigrator(m_dataSource, m_adminDataSource);
                m.dropExistingIpLike();
                m.createLangPlPgsql();
            }
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
            throw new TemporaryDatabaseException("Failed to create Unicode test database " + getTestDatabase() + ": " + e, e);
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
                if (failed == null) failed = e;
            }
            if (failed != null) {
                throw new TemporaryDatabaseException("Failed while cleaning up database resources: " + failed, failed);
            }
        }

        setupShutdownHook();
        s_toDestroy.add(this);
    }

    private static void setupShutdownHook() {
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

    public void drop() throws TemporaryDatabaseException {
        if (!m_useExisting) {
            destroyTestDatabase();
            s_toDestroy.remove(this);
        }
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

    public int countRows(String sql, Object... values) {
        RowCountCallbackHandler counter = new RowCountCallbackHandler();
        getJdbcTemplate().query(sql, values, counter);
        return counter.getRowCount();
    }

    public String getNextSequenceValStatement(String seqName) {
        return "select nextval('" + seqName + "')";
    }

    protected Integer getNextId(String nxtIdStmt) {
        return getJdbcTemplate().queryForObject(nxtIdStmt, Integer.class);
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

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    public XADataSource getAdminXADataSource() {
        return m_adminXaDataSource;
    }

    public void setAdminDataSource(DataSource adminDataSource) {
        m_adminDataSource = adminDataSource;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public XADataSource getXADataSource() {
        return m_xaDataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public String getTestDatabase() {
        return m_testDatabase;
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

    public String getDriver() {
        return m_driver;
    }

    public String getUrl() {
        return m_url;
    }

    public String getClassName() {
        return m_className;
    }

    public void setClassName(String className) {
        m_className = className;
    }

    public String getMethodName() {
        return m_methodName;
    }

    public void setMethodName(String methodName) {
        m_methodName = methodName;
    }

    public String getTestDetails() {
        return m_testDetails;
    }

    public void setTestDetails(String testDetails) {
        m_testDetails = testDetails;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("driver", m_driver)
                .append("url", m_url)
                .append("testDatabase", m_testDatabase)
                .append("useExisting", m_useExisting)
                //            .append("setupIpLike", m_setupIpLike)
                .append("populateSchema", m_populateSchema)
                .append("dataSource", m_dataSource)
                .append("adminDataSource", m_adminDataSource)
                .append("adminUser", m_adminUser)
                .toString();
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return m_xaDataSource.getXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return m_xaDataSource.getXAConnection(user, password);
    }

    public static List<TemporaryDatabasePostgreSQL.ChangelogEntry> getChangelogEntries(DataSource dataSource) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id, md5sum FROM databasechangelog order by id");
            assertTrue(statement.execute());
            ResultSet rs = statement.getResultSet();
            final List<TemporaryDatabasePostgreSQL.ChangelogEntry> entries = new ArrayList<TemporaryDatabasePostgreSQL.ChangelogEntry>();
            while (rs.next()) {
                entries.add(new TemporaryDatabasePostgreSQL.ChangelogEntry(rs.getString(1), rs.getString(2)));
            }
            return entries;
        } catch (final SQLException e) {
            LOG.warn("Failed to query changelog entries.", e);
            return Collections.emptyList();
        } finally {
            connection.close();
        }
    }

    public static String generateLiquibaseHash(ApplicationContext context)
            throws NoSuchAlgorithmException, IOException, Exception, ChangeLogParseException, LiquibaseException {
        final long start = System.currentTimeMillis();

        final String contexts = System.getProperty("opennms.contexts", "production");
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        changeLogParameters.setContexts(new Contexts(StringUtils.splitAndTrim(contexts, ",")));

        MessageDigest md = MessageDigest.getInstance("MD5");
        List<URI> seenChangeLogs = new LinkedList<>();

        for (Resource resource : Migrator.validateLiquibaseChangelog(context)) {
            if (!createProductionLiquibaseChangelogFilter().test(resource)) {
                LOG.info("skipping {} because it didn't pass the changelog filter", resource);
                continue;
            }

            seenChangeLogs.add(resource.getURI());
            DigestUtils.updateDigest(md, resource.getInputStream());

            ResourceAccessor accessor = new ExistingResourceAccessor(resource);
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(Migration.LIQUIBASE_CHANGELOG_FILENAME, accessor).parse(Migration.LIQUIBASE_CHANGELOG_FILENAME, changeLogParameters, accessor);

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
            throw new AssertionError("No change logs were found. ClassPath: " + Migrator.getContextClassLoaderUrls(context));
        }

        String hash = Hex.encodeHexString(md.digest());

        final long end = System.currentTimeMillis();
        LOG.info("Computed Liquibase schema hash {} in {} seconds on change logs: {}.", hash, (float) (end - start) / 1000, Joiner.on(", ").join(seenChangeLogs));

        return hash;
    }

    protected static void createIntegrationTestDatabase(ApplicationContext context, Migration  migration, Migrator migrator, DataSource dataSource)
            throws ClassNotFoundException, MigrationException, Throwable, SQLException {
        if (migrator.databaseExists(migration)) {
            migrator.databaseRemoveDB(migration);
        }

        migrator.setLiquibaseChangelogFilter(createProductionLiquibaseChangelogFilter());

        try {
            migrator.setupDatabase(migration, true, true, true, true, context);
        } catch (Throwable t) {
            if (migrator.databaseExists(migration)) {
                try {
                    LOG.warn("Got an exception while setting up database, so removing");
                    migrator.databaseRemoveDB(migration);
                } catch (MigrationException e) {
                    System.err.println("Got an exception while setting up database and then got another exception while removing database: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
            throw t;
        }

        final List<TemporaryDatabasePostgreSQL.ChangelogEntry> ids = TemporaryDatabasePostgreSQL.getChangelogEntries(dataSource);
        assertTrue("changelog entries were expected in the newly created database", ids.size() > 0);

        // Check to make sure some of the changelogs ran
        assertTrue(ids.stream().anyMatch(id -> "17.0.0-remove-legacy-ipinterface-composite-key-fields".equals(id.getId())));
        assertTrue(ids.stream().anyMatch(id -> "17.0.0-remove-legacy-outages-composite-key-fields".equals(id.getId())));
    }

    protected static Migrator createMigrator(DataSource dataSource, DataSource adminDataSource) {
        final Migrator m = new Migrator();
        m.setDataSource(dataSource);
        m.setAdminDataSource(adminDataSource);
        m.setValidateDatabaseVersion(true);
        m.setCreateUser(false);
        m.setCreateDatabase(true);
        return m;
    }

    protected static Migration createMigration(String integrationTestTemplateDatabaseName) {
        final Migration migration = new Migration();
        migration.setAdminUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setAdminPassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setDatabaseUser(System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER));
        migration.setDatabasePassword(System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD));
        migration.setDatabaseName(integrationTestTemplateDatabaseName);
        return migration;
    }

    private static Predicate<Resource> createProductionLiquibaseChangelogFilter() {
        return r -> {
            try {
                URI uri = r.getURI();
                return (uri.getScheme().equals("file") && uri.toString().contains("core/schema")) ||
                        (uri.getScheme().equals("jar") && uri.toString().contains("core.schema"));
            } catch (IOException e) {
                return false;
            }
        };
    }

    public static GenericApplicationContext ensureLiquibaseFilesInClassPath(GenericApplicationContext context)
            throws MalformedURLException, IOException, Exception {
        try {
            Migrator.validateLiquibaseChangelog(context);
        } catch (MigrationException e) {
            String liquibaseRelativePath = "core/schema/src/main/liquibase/";

            String migratorClass = "/" + Migrator.class.getName().replace('.', '/') + ".class";
            URL migratorUrl = Migrator.class.getResource(migratorClass);
            assert migratorUrl != null : "Could not find resource for Migrator.class anywhere in the classpath with " + migratorClass;
            if ("file".equals(migratorUrl.getProtocol()) && migratorUrl.getPath().endsWith("core/schema/target/classes" + migratorClass)) {
                URL[] urls = {new URL(migratorUrl.getProtocol(), migratorUrl.getHost(), migratorUrl.getFile().replaceFirst("core/schema/target/classes/.*$", liquibaseRelativePath))};
                context.setClassLoader(new URLClassLoader(urls, context.getClassLoader()));
            }

            try {
                Migrator.validateLiquibaseChangelog(context);
            } catch (MigrationException e2) {
                File buildTop = findTopOpenNmsBuildDir();
                if (buildTop != null) {
                    File liquibase = new File(buildTop, liquibaseRelativePath);
                    if (!liquibase.exists()) {
                        throw new MigrationException(e2.getMessage() + ", nor could we find liquibase files where we expected: " + liquibase.getAbsolutePath());
                    }

                    URL[] urls = {liquibase.toURI().toURL()};
                    context.setClassLoader(new URLClassLoader(urls, context.getClassLoader()));
                    Migrator.validateLiquibaseChangelog(context);

                    return context;
                }

                throw e;
            }
        }

        return context;
    }

    public static File findTopOpenNmsBuildDir() {
        for (File dir = new File("").getAbsoluteFile(); dir != null; dir = dir.getParentFile()) {
            File compilePl = new File(dir, "compile.pl");
            if (compilePl.exists()) {
                return dir;
            }
        }
        return null;
    }
    
    public void setPlpgsqlIplike(final boolean iplike) {
        m_plpgsqlIplike = iplike;
    }
}
