//
//  $Id$
//

package org.opennms.install;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

public class InstallerDBTest extends TestCase {
	private static final String m_constraint = "fk_nodeid6";

	private static final String m_runProperty = "mock.rundbtests";

	private String m_testDatabase;

	private Installer m_installer;

	protected void setUp() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		m_testDatabase = "opennms_test_" + System.currentTimeMillis();

		m_installer = new Installer();
		// Create a ByteArrayOutputSteam to effectively throw away output.
		m_installer.m_out = new PrintStream(new ByteArrayOutputStream());
		m_installer.m_database = m_testDatabase;
		m_installer.m_pg_driver = "org.postgresql.Driver";
		m_installer.m_pg_url = "jdbc:postgresql://localhost:5432/";
		m_installer.m_pg_user = "postgres";
		m_installer.m_pg_pass = "";
		m_installer.m_create_sql = "etc/create.sql";
		m_installer.m_fix_constraint = true;
		m_installer.m_fix_constraint_name = m_constraint;

		// Create test database.
		m_installer.databaseConnect("template1");
		m_installer.databaseAddDB();
		m_installer.databaseDisconnect();

		// Connect to test database.
		m_installer.databaseConnect(m_testDatabase);

		// Read in the table definitions
		m_installer.readTables();
	}

	public void tearDown() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		m_installer.databaseDisconnect();

		/*
		 * Sleep after disconnecting from the database because PostgreSQL
		 * doesn't seem to notice immediately that we have disconnected. Yeah,
		 * it's a hack.
		 */
		Thread.sleep(1000);

		m_installer.databaseConnect("template1");
		destroyDatabase();
		m_installer.databaseDisconnect();

		// Sleep again. Man, I had this.
		Thread.sleep(1000);
	}

	public boolean isDBTestEnabled() {
		String property = System.getProperty(m_runProperty);
		return "true".equals(property);
	}

	public void destroyDatabase() throws SQLException {
		Statement st = m_installer.m_dbconnection.createStatement();
		st.execute("DROP DATABASE " + m_testDatabase);
		st.close();
	}

	/**
	 * Call Installer.checkOldTables, which should *not* throw an exception
	 * because we have not created a table matching "_old_".
	 */
	public void testBug1006NoOldTables() throws SQLException {
		if (!isDBTestEnabled()) {
			return;
		}

		try {
			m_installer.checkOldTables();
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	/**
	 * Call Installer.checkOldTables, which *should* throw an exception because
	 * we have created a table matching "_old_". We check the exception message
	 * to ensure that it is the exception we are expecting, and fail otherwise.
	 */
	public void testBug1006HasOldTables() throws SQLException {
		if (!isDBTestEnabled()) {
			return;
		}

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

	public void executeSQL(String[] commands) throws SQLException {
		if (!isDBTestEnabled()) {
			return;
		}

		Statement st = m_installer.m_dbconnection.createStatement();
		for (int i = 0; i < commands.length; i++) {
			st.execute(commands[i]);
		}
		st.close();
	}

	public void executeSQL(String command) throws SQLException {
		String[] commands = new String[1];
		commands[0] = command;
		executeSQL(commands);
	}

	public void setupBug931(boolean breakConstraint, boolean dropForeignTable)
			throws SQLException {
		final String[] commands = { "CREATE TABLE events ( nodeID integer )",
				"CREATE TABLE node ( nodeID integer )",
				"INSERT INTO events ( nodeID ) VALUES ( 1 )",
				"INSERT INTO node ( nodeID ) VALUES ( 1 )",
				"INSERT INTO events ( nodeID ) VALUES ( 2 )",
				"INSERT INTO node ( nodeID ) VALUES ( 2 )" };

		executeSQL(commands);

		if (breakConstraint) {
			executeSQL("DELETE FROM node where nodeID = 2");
		}

		if (dropForeignTable) {
			executeSQL("DROP TABLE node");
			if (!breakConstraint) {
				executeSQL("UPDATE events SET nodeID = NULL WHERE nodeID IS NOT NULL");
			}
		}
	}

	public void testBug931ConstraintsOkayTwoTables() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		doTestBug931(false, 0, false);
	}

	public void testBug931ConstraintsOkayOneTable() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		doTestBug931(true, 0, false);
	}

	public void testBug931ConstraintsBadTwoTables() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		doTestBug931(false, 1, false);
	}

	public void testBug931ConstraintsBadOneTable() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		doTestBug931(true, 2, false);
	}

	public void testConstraintsFixedNullTwoTables() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		doTestBug931(false, 0, true);
	}

	public void testConstraintsFixedNullOneTable() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		doTestBug931(true, 0, true);
	}

	public void testConstraintsFixedDelTwoTables() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		m_installer.m_fix_constraint_remove_rows = true;
		doTestBug931(false, 0, true);
	}

	public void testConstraintsFixedDelOneTable() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		m_installer.m_fix_constraint_remove_rows = true;
		doTestBug931(true, 0, true);
	}

	public void testBogusConstraintName() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		String constraint = "bogus_test_" + System.currentTimeMillis();
		doTestBogusConstraint(constraint, "Did not find constraint "
				+ constraint + " in the database.");
	}

	public void testBogusConstraintTable() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		String constraint = "fk_nodeid1";
		doTestBogusConstraint(constraint, "Constraint " + constraint
				+ " is on table " + "ipinterface, but table does not exist");
	}

	public void testBogusConstraintColumn() throws Exception {
		if (!isDBTestEnabled()) {
			return;
		}

		String constraint = "fk_dpname";
		doTestBogusConstraint(constraint, "Constraint " + constraint
				+ " is on column "
				+ "dpname of table node, but column does not " + "exist");
	}

	public void doTestBogusConstraint(String constraint, String errorSubstring)
			throws Exception {
		m_installer.m_fix_constraint_name = constraint;

		setupBug931(false, false);

		try {
			m_installer.fixConstraint();
		} catch (Exception e) {
			if (e.getMessage().indexOf(errorSubstring) >= 0) {
				// Received expected error, so the test is successful.
				return;
			} else {
				fail("Expected an exception matching \"" + errorSubstring
						+ "\", but instead received an unexpected Exception: "
						+ e.toString());
			}
		}
	}

	public void doTestBug931(boolean dropForeignTable, int badRows,
			boolean fixConstraint) throws Exception {
		final String errorSubstring = "Table events contains " + badRows
				+ " rows (out of 2) that violate new constraint "
				+ m_constraint;

		setupBug931((badRows != 0) || fixConstraint, dropForeignTable);

		if (fixConstraint) {
			m_installer.fixConstraint();
		}

		try {
			m_installer.checkConstraints();
		} catch (Exception e) {
			if (badRows == 0) {
				fail("Received an unexpected exception: " + e.toString());
			} else {
				if (e.getMessage().indexOf(errorSubstring) >= 0) {
					// Received expected error, so the test is successful.
					return;
				} else {
					fail("Expected an exception matching \""
							+ errorSubstring
							+ "\", but instead received an unexpected Exception: "
							+ e.toString());
				}
			}
		}

		if (badRows != 0) {
			fail("Did not receive expected exception: " + errorSubstring);
		}
	}
}