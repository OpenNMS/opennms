package org.opennms.install;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class TemporaryDatabaseTestCase extends TestCase {
    private static final String s_runProperty = "mock.rundbtests";

    private String m_testDatabase;

    private boolean m_leaveDatabase = false;
    private boolean m_leaveDatabaseOnFailure = false;
    private boolean m_failure = false;
    private Throwable m_throwable = null;
    
    private String m_driver;
    private String m_url;
    private String m_adminUser;
    private String m_adminPassword;
    
    private boolean m_toldDisabled = false;
    
    public TemporaryDatabaseTestCase() {
        this("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/",
             "postgres", "");
    }
    
    public TemporaryDatabaseTestCase(String driver, String url,
            String adminUser, String adminPassword) {
        m_driver = driver;
        m_url = url;
        m_adminUser = adminUser;
        m_adminPassword = adminPassword;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        if (!areTestsEnabled()) {
            return;
        }

        m_testDatabase = "opennms_test_" + System.currentTimeMillis();

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
            m_failure = true;
            m_throwable = t;
            throw t;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (areTestsEnabled()) {
            try {
                destroyTestDatabase();
            } catch (Throwable t) {
                if (m_throwable != null) {
                    throw new TestFailureAndTearDownErrorException(m_throwable, t);
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
    
    private boolean areTestsEnabled() {
        String property = System.getProperty(s_runProperty);
        boolean enabled = "true".equals(property);
        if (!enabled && !m_toldDisabled) {
            System.out.println("Test '" + getName() + "' disabled.  Set '"
                               + s_runProperty
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
        if (m_leaveDatabase || (m_leaveDatabaseOnFailure && m_failure)) {
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

        Statement st = adminConnection.createStatement();
        st.execute("DROP DATABASE " + m_testDatabase);
        st.close();

        adminConnection.close();

        /*
         * Sleep after disconnecting from template1, otherwise creating
         * a new test database in future tests may fail. Man, I hate this.
         */
        Thread.sleep(100);
    }

    public void executeSQL(String command) {
        executeSQL(new String[] { command });
    }

    public void executeSQL(String[] commands) {
        Connection connection = null;
        
        try {
            connection = getConnection();
        } catch (Exception e) {
            fail("Could not get connection", e);
        }
        
        try {
            Statement st = null;

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
        
            try {
                st.close();
            } catch (SQLException e) {
                fail("Could not close database connection", e);
            }
        } finally {
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
    
    public class TestFailureAndTearDownErrorException extends Exception {
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
