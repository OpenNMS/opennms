//
//  $Id$
//

package org.opennms.install;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

public class InstallerTest extends TestCase {
    private String m_testDatabase;
    private Installer m_installer;

    protected void setUp() throws Exception {
	m_testDatabase = "opennms_test_" + System.currentTimeMillis();

	m_installer = new Installer();
	// Create a ByteArrayOutputSteam to effectively throw away output.
	m_installer.m_out = new PrintStream(new ByteArrayOutputStream());
	m_installer.m_database = m_testDatabase;
	m_installer.m_pg_driver = "org.postgresql.Driver";
	m_installer.m_pg_url = "jdbc:postgresql://localhost:5432/";
	m_installer.m_pg_user = "postgres";
	m_installer.m_pg_pass = "";

	// Create test database.
	m_installer.databaseConnect("template1");
	m_installer.databaseAddDB();
	m_installer.databaseDisconnect();

	// Connect to test database.
	m_installer.databaseConnect(m_testDatabase);
    }
	

    public void tearDown() throws Exception {
	m_installer.databaseDisconnect();

	/*
	 * Sleep after disconnecting from the database because PostgreSQL
	 * doesn't seem to notice immediately that we have disconnected.
	 * Yeah, it's a hack.
	 */
	Thread.sleep(1000);

	m_installer.databaseConnect("template1");
	destroyDatabase();
	m_installer.databaseDisconnect();

	// Sleep again.  Man, I had this.
	Thread.sleep(1000);
    }

    public void destroyDatabase() throws SQLException {
	Statement st = m_installer.m_dbconnection.createStatement();
        st.execute("DROP DATABASE " + m_testDatabase);
	st.close();
    }


    /**
     * Call Installer.checkOldTables, which should *not* throw an
     * exception because we have not created a table matching "_old_".
     */
    public void testBug1006False() throws SQLException {
	try {
	    m_installer.checkOldTables();
	} catch (Exception e) {
	    fail(e.toString());
	}
    }

    /**
     * Call Installer.checkOldTables, which *should* throw an
     * exception because we have created a table matching "_old_".
     * We check the exception message to ensure that it is the
     * exception we are expecting, and fail otherwise.
     */
    public void testBug1006True() throws SQLException {
	final String errorSubstring = "One or more backup tables from a previous install still exists";

	String table = "testBug1006_old_" + System.currentTimeMillis();

	Statement st = m_installer.m_dbconnection.createStatement();
	st.execute("CREATE TABLE " + table + " ( foo integer )");
	st.close();

	try {
	    m_installer.checkOldTables();
	} catch (Exception e) {
	    if (e.getMessage().indexOf(errorSubstring) >= 0) {
		// We received the error we expected.
		return;
	    } else {
		fail("Received an unexpected Exception: " + e.toString());
	    }
	}

	fail("Did not receive expected exception: " + errorSubstring);
    }
}
