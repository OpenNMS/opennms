package org.opennms.install;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class TemporaryDatabaseTestCase extends TestCase {
    private static final String TEST_DB_NAME_PREFIX = "opennms_test_";
    
    private static final String RUN_PROPERTY = "mock.rundbtests";
    private static final String LEAVE_PROPERTY = "mock.leaveDatabase";
    private static final String LEAVE_ON_FAILURE_PROPERTY =
        "mock.leaveDatabaseOnFailure";
    
    private static final String DRIVER_PROPERTY = "mock.db.driver";
    private static final String URL_PROPERTY = "mock.db.url";
    private static final String ADMIN_USER_PROPERTY = "mock.db.adminUser";
    private static final String ADMIN_PASSWORD_PROPERTY = "mock.db.adminPassword";
    
    private static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DEFAULT_ADMIN_USER = "postgres";
    private static final String DEFAULT_ADMIN_PASSWORD = "";

    private String m_testDatabase;

    private boolean m_leaveDatabase = false;
    private boolean m_leaveDatabaseOnFailure = false;
    private Throwable m_throwable = null;
    
    private String m_driver;
    private String m_url;
    private String m_adminUser;
    private String m_adminPassword;
    
    private boolean m_toldDisabled = false;
    
    public TemporaryDatabaseTestCase() {
        this(System.getProperty(DRIVER_PROPERTY, DEFAULT_DRIVER),
             System.getProperty(URL_PROPERTY, DEFAULT_URL),
             System.getProperty(ADMIN_USER_PROPERTY, DEFAULT_ADMIN_USER),
             System.getProperty(ADMIN_PASSWORD_PROPERTY, DEFAULT_ADMIN_PASSWORD));
    }
    
    public TemporaryDatabaseTestCase(String driver, String url,
            String adminUser, String adminPassword) {
        m_driver = driver;
        m_url = url;
        m_adminUser = adminUser;
        m_adminPassword = adminPassword;
    }

    /*
     * TODO: Should we make this final, and let extending classes override
     * something like afterSetUp() (like the Spring transactional tests do)
     */ 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        // Reset any previous test failures
        setTestFailureThrowable(null);
        
        if (!areTestsEnabled()) {
            return;
        }
        
        m_leaveDatabase = "true".equals(System.getProperty(LEAVE_PROPERTY));
        m_leaveDatabaseOnFailure =
            "true".equals(System.getProperty(LEAVE_ON_FAILURE_PROPERTY));

        m_testDatabase = getTestDatabaseName();

        createTestDatabase();

        // Test connecting to test database.
        Connection connection = getConnection();
        connection.close();
    }
    
    @Override
    protected void runTest() throws Throwable {
        if (!areTestsEnabled()) {
            return;
        }

        try {
            super.runTest();
        } catch (Throwable t) {
            setTestFailureThrowable(t);
            throw t;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (areTestsEnabled()) {
            try {
                destroyTestDatabase();
            } catch (Throwable t) {
                /*
                 * Do some fancy footwork to catch and reasonably report cases
                 * where both the test method and destroyTestDatabase throw
                 * exceptions.  Otherwise, a test that fails in a really
                 * funky way may cause destroyTestDatabase() to throw an
                 * exception, which would mask the root cause, since JUnit
                 * will only report the latter exception.
                 */ 
                if (hasTestFailed()) {
                    throw new TestFailureAndTearDownErrorException(getTestFailureThrowable(), t);
                } else {
                    if (t instanceof Exception) {
                        throw (Exception) t;
                    } else {
                        throw new UndeclaredThrowableException(t);
                    }
                }
            }
        }

        super.tearDown();
    }
    
    protected String getTestDatabaseName() {
        return TEST_DB_NAME_PREFIX + System.currentTimeMillis();
    }

    public String getTestDatabase() {
        return m_testDatabase;
    }
    
    public Connection getConnection() throws Exception {
        return databaseConnect(m_testDatabase);
    }
    
    public Connection getAdminConnection() throws Exception {
        return databaseConnect("template1");
    }
    
    public String getDriver() {
        return m_driver;
    }
    
    public String getUrl() {
        return m_url;
    }
    
    public String getAdminUser() {
        return m_adminUser;
    }
    
    public String getAdminPassword() {
        return m_adminPassword;
    }

    public void setTestFailureThrowable(Throwable t) {
        m_throwable = t;
    }
    
    public Throwable getTestFailureThrowable() {
        return m_throwable;
    }

    public boolean hasTestFailed() {
        return m_throwable != null;
    }

    public boolean areTestsEnabled() {
        String property = System.getProperty(RUN_PROPERTY);
        boolean enabled = "true".equals(property);
        if (!enabled && !m_toldDisabled) {
            System.out.println("Test '" + getName() + "' disabled.  Set '"
                               + RUN_PROPERTY
                               + "' property to 'true' to enable.");
            m_toldDisabled = true;
        }
        return enabled;
    }
    
    private Connection databaseConnect(String database) throws Exception {
        Class.forName(m_driver);
        return DriverManager.getConnection(m_url + database,
                                           m_adminUser, m_adminPassword);
    }

    private void createTestDatabase() throws Exception {
        Connection adminConnection = getAdminConnection();
        Statement st = adminConnection.createStatement();
        st.execute("CREATE DATABASE " + m_testDatabase
                + " WITH ENCODING='UNICODE'");
        adminConnection.close();
    }
    
    private void destroyTestDatabase() throws Exception {
        if (m_leaveDatabase
                || (m_leaveDatabaseOnFailure && hasTestFailed())) {
            System.err.println("Not dropping database '" + m_testDatabase
                    + "' for test '" + getName() + "'");
            return;
        }

        /*
         * Sleep before destroying the test database because PostgreSQL
         * doesn't seem to notice immediately clients have disconnected. Yeah,
         * it's a hack.
         */
        Thread.sleep(100);

        Connection adminConnection = getAdminConnection();

        try {
            Statement st = adminConnection.createStatement();
            st.execute("DROP DATABASE " + m_testDatabase);
            st.close();
        } finally {
            /*
             * Since we are already going to be throwing an exception at this
             * point, print any further errors to stdout so we don't mask
             * the first failure.
             */
            try {
                adminConnection.close();
            } catch (SQLException e) {
                System.out.println("Error closing administrative database "
                                   + "connection after attempting ot drop "
                                   + "test database");
                e.printStackTrace();
            }

            /*
             * Sleep after disconnecting from template1, otherwise creating
             * a new test database in future tests may fail. Man, I hate this.
             */
            Thread.sleep(100);
        }
    }

    public void executeSQL(String command) {
        executeSQL(new String[] { command });
    }

    public void executeSQL(String[] commands) {
        Connection connection = null;
        Statement st = null;

        try {
            connection = getConnection();
        } catch (Exception e) {
            fail("Could not get connection", e);
        }
        
        try {
            try {
                st = connection.createStatement();
            } catch (SQLException e) {
                fail("Could not create statement", e);
            }

            for (String command : commands) {
                try {
                    st.execute(command);
                } catch (SQLException e) {
                    fail("Could not execute statement: '" + command + "'", e);
                }
            }
        } finally {
            /*
             * Since we are already going to be throwing an exception at this
             * point, print any further errors to stdout so we don't mask
             * the first failure.
             */
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    System.out.println("Could not close statement in executeSQL");
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Could not close connection in executeSQL");
                    e.printStackTrace();
                }
            }
        }
    }

    public void fail(String message, Throwable t) throws AssertionFailedError {
        AssertionFailedError e = new AssertionFailedError(message + ": "
                + t.getMessage());
        e.initCause(t);
        throw e;
    }
    
    /**
     * Represents a failure both in a unit test method (e.g.: testFoo) and
     * in the tearDown method.  
     * 
     * @author djgregor
     */
    public class TestFailureAndTearDownErrorException extends Exception {
        private static final long serialVersionUID = -5664844942506660064L;
        private Throwable m_tearDownError;
        
        public TestFailureAndTearDownErrorException(Throwable testFailure,
                Throwable tearDownError) {
            super(testFailure);
            m_tearDownError = tearDownError;
        }
        
        public String toString() {
            return super.toString()
                + "\nAlso received error on tearDown: "
                + m_tearDownError.toString();
        }
    }
}
