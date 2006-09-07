package org.opennms.install;

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
    
    private String m_driver;
    private String m_url;
    private String m_adminUser;
    private String m_adminPassword;
    
    private Connection m_dbConnection;
    
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

    protected void setUp() throws Exception {
        super.setUp();
        m_testDatabase = "opennms_test_" + System.currentTimeMillis();
        
        if (!isDBTestEnabled()) {
            return;
        }

        // Create test database.
        databaseConnect("template1");
        databaseAddDB(m_testDatabase);
        databaseDisconnect();

        // Connect to test database.
        databaseConnect(m_testDatabase);
    }
    
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
            m_failure = true;
            throw t;
        }

    }

    protected void tearDown() throws Exception {

        if (!isDBTestEnabled()) {
            return;
        }

        databaseDisconnect();

        /*
         * Sleep after disconnecting from the database because PostgreSQL
         * doesn't seem to notice immediately that we have disconnected. Yeah,
         * it's a hack.
         */
        Thread.sleep(100);

        databaseConnect("template1");
        destroyDatabase();
        databaseDisconnect();

        // Sleep again. Man, I hate this.
        Thread.sleep(100);

        super.tearDown();
    }
    
    public void destroyDatabase() throws SQLException {
        if (m_leaveDatabase || (m_leaveDatabaseOnFailure && m_failure)) {
            System.err.println("Not dropping database '" + m_testDatabase
                    + "' for test '" + getName() + "'");
        } else {
            Statement st = m_dbConnection.createStatement();
            st.execute("DROP DATABASE " + m_testDatabase);
            st.close();
        }
    }

    public String getTestDatabase() {
        return m_testDatabase;
    }
    
    public Connection getDbConnection() {
        return m_dbConnection;
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
    
    public boolean isDBTestEnabled() {
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
    
    public void databaseConnect(String database) throws Exception {
        Class.forName(m_driver);
        m_dbConnection = DriverManager.getConnection(m_url + database,
                                                     m_adminUser, m_adminPassword);
    }

    public void databaseDisconnect() throws Exception {
        if (m_dbConnection != null) {
            m_dbConnection.close();
        }
    }

    public void databaseAddDB(String database) throws Exception {
        Statement st = m_dbConnection.createStatement();
        st.execute("CREATE DATABASE " + database
                + " WITH ENCODING='UNICODE'");
    }


    public void executeSQL(String command) {
        executeSQL(new String[] { command });
    }

    public void executeSQL(String[] commands) {
        if (!isDBTestEnabled()) {
            return;
        }

        Statement st = null;

        try {
            st = getDbConnection().createStatement();
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
    }

    public void fail(String message, Throwable t) throws AssertionFailedError {
        AssertionFailedError e = new AssertionFailedError(message + ": "
                + t.getMessage());
        e.initCause(t);
        throw e;
    }

    
}
