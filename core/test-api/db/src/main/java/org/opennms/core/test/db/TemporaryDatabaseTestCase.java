/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.sql.DataSource;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.opennms.core.db.install.SimpleDataSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <p>For each unit test method, creates a temporary database before the unit
 * test is run and destroys the database after each test (optionally leaving
 * around the test database, either always or on a test failure).  Tests do
 * get run by default, but the system property "mock.rundbtests" can be set
 * to "false" to disable this (and the database won't be touched).</p>
 * 
 * <p>If you get errors about not being able to delete a database because
 * it is in use, make sure that your tests always close their database
 * connections (even in case of failures).</p>
 * 
 * @author djgregor
 */
public class TemporaryDatabaseTestCase extends TestCase {
    
    protected SimpleJdbcTemplate jdbcTemplate;

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

    private static final int MAX_DATABASE_DROP_ATTEMPTS = 10;

    private String m_testDatabase;

    private boolean m_leaveDatabase = false;
    private boolean m_leaveDatabaseOnFailure = false;
    private Throwable m_throwable = null;
    
    private boolean m_destroyed = false;
    
    private String m_driver;
    private String m_url;
    private String m_adminUser;
    private String m_adminPassword;
    
    private DataSource m_dataSource;
    private DataSource m_adminDataSource;

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
        
        if (!isEnabled()) {
            return;
        }
        
        m_leaveDatabase = "true".equals(System.getProperty(LEAVE_PROPERTY));
        m_leaveDatabaseOnFailure =
            "true".equals(System.getProperty(LEAVE_ON_FAILURE_PROPERTY));

        setTestDatabase(getTestDatabaseName());
        
        setDataSource(new SimpleDataSource(m_driver, m_url + getTestDatabase(),
                                           m_adminUser, m_adminPassword));
        setAdminDataSource(new SimpleDataSource(m_driver, m_url + "template1",
                                           m_adminUser, m_adminPassword));

        createTestDatabase();

        // Test connecting to test database.
        Connection connection = getConnection();
        connection.close();
    }

    private void setTestDatabase(String testDatabase) {
        m_testDatabase = testDatabase; 
    }

    @Override
    protected void runTest() throws Throwable {
        if (!isEnabled()) {
            notifyTestDisabled(getName());
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
        if (isEnabled()) {
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
    
    public void testNothing() {
    }

    protected String getTestDatabaseName() {
        return TEST_DB_NAME_PREFIX + System.currentTimeMillis();
    }

    public String getTestDatabase() {
        return m_testDatabase;
    }
    
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }
    
    public DataSource getDataSource() {
        return m_dataSource;
    }
    
    private void setAdminDataSource(DataSource dataSource) {
        m_adminDataSource = dataSource;
    }

    protected DataSource getAdminDataSource() {
        return m_adminDataSource;
    }
    
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
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
    
    /**
     * Defaults to true.
     * 
     * @return w00t
     */
    public static boolean isEnabled() {
        String property = System.getProperty(RUN_PROPERTY, "true");
        return "true".equals(property);
    }

    public static void notifyTestDisabled(String testMethodName) {
        System.out.println("Test '" + testMethodName
                           + "' disabled.  Set '"
                           + RUN_PROPERTY
                           + "' property from 'false' to 'true' to enable.");
    }

    private void createTestDatabase() throws Exception {
        Connection adminConnection = getAdminDataSource().getConnection();
        Statement st = null;
        try {
            st = adminConnection.createStatement();
            st.execute("CREATE DATABASE " + getTestDatabase()
                    + " WITH ENCODING='UNICODE'");
        } finally {
            if (st != null) {
                st.close();
            }
            adminConnection.close();
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    destroyTestDatabase();
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
            
        });
    }
    
    private void destroyTestDatabase() throws Exception {
        if (m_destroyed) {
            // database already destroyed
            return;
        }
        
        if (m_leaveDatabase
                || (m_leaveDatabaseOnFailure && hasTestFailed())) {
            System.err.println("Not dropping database '" + getTestDatabase()
                    + "' for test '" + getName() + "'");
            return;
        }

        /*
         * Sleep before destroying the test database because PostgreSQL
         * doesn't seem to notice immediately clients have disconnected. Yeah,
         * it's a hack.
         */
        Thread.sleep(100);

        Connection adminConnection = getAdminDataSource().getConnection();

        try {
            for (int dropAttempt = 0; dropAttempt < MAX_DATABASE_DROP_ATTEMPTS; dropAttempt++) {
                Statement st = null;
            
                try {
                    st = adminConnection.createStatement();
                    st.execute("DROP DATABASE " + getTestDatabase());
                    break;
                } catch (SQLException e) {
                    if ((dropAttempt + 1) >= MAX_DATABASE_DROP_ATTEMPTS) {
                        final String message = "Failed to drop test database on last attempt " + (dropAttempt + 1) + ": " + e;
                        System.err.println(new Date().toString() + ": " + message);
                        TemporaryDatabase.dumpThreads();

                        SQLException newException = new SQLException(message);
                        newException.initCause(e);
                        throw newException;
                    } else {
                        System.err.println(new Date().toString() + ": Failed to drop test database on attempt " + (dropAttempt + 1) + ": " + e);
                        Thread.sleep(1000);
                    }
                } finally {
                    if (st != null) {
                        st.close();
                        st = null;
                    }
                }
            }
        } finally {
            /*
             * Since we are already going to be throwing an exception at this
             * point, print any further errors to stdout so we don't mask
             * the first failure.
             */
            try {
                adminConnection.close();
            } catch (SQLException e) {
                System.err.println("Error closing administrative database "
                                   + "connection after attempting to drop "
                                   + "test database");
                e.printStackTrace();
            }

            /*
             * Sleep after disconnecting from template1, otherwise creating
             * a new test database in future tests may fail. Man, I hate this.
             */
            Thread.sleep(100);
        }
        
        m_destroyed = true;
    }

    public void executeSQL(String command) {
        executeSQL(new String[] { command });
    }

    public void executeSQL(String[] commands) {
        Connection connection = null;
        Statement st = null;

        try {
            connection = getConnection();
        } catch (Throwable e) {
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
                    System.err.println("Could not close statement in executeSQL");
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Could not close connection in executeSQL");
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
    
    public SimpleJdbcTemplate getJdbcTemplate() {
    	return jdbcTemplate;
    }
    
}
