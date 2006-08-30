//
// $Id$
//

package org.opennms.install;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.test.ThrowableAnticipator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class InstallerDBTest extends TestCase {
    private static final String s_constraint = "fk_nodeid6";

    private static final String s_runProperty = "mock.rundbtests";

    private String m_testDatabase;

    private boolean m_leaveDatabase = false;

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
        m_installer.m_user = "opennms";
        
        m_installer.m_create_sql = "../opennms-daemon/src/main/filtered/etc/create.sql";
        
        /*
        URL sql = getClass().getResource("/create.sql");
        assertNotNull("Could not find create.sql", sql);
        m_installer.m_create_sql = sql.getFile();
        */
        
        m_installer.m_sql_dir = "../opennms-daemon/src/main/filtered/etc";
        
        m_installer.m_fix_constraint = true;
        m_installer.m_fix_constraint_name = s_constraint;

        m_installer.m_debug = false;

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
        Thread.sleep(100);

        m_installer.databaseConnect("template1");
        destroyDatabase();
        m_installer.databaseDisconnect();

        // Sleep again. Man, I hate this.
        Thread.sleep(100);
    }

    public boolean isDBTestEnabled() {
        String property = System.getProperty(s_runProperty);
        return "true".equals(property);
    }

    public void destroyDatabase() throws SQLException {
        if (m_leaveDatabase) {
            System.err.println("Not dropping database '" + m_testDatabase
                    + "'");
        } else {
            Statement st = m_installer.m_dbconnection.createStatement();
            st.execute("DROP DATABASE " + m_testDatabase);
            st.close();
        }
    }

    // XXX this should be an integration test
    public void testParseSQLTables() throws Exception {
        Iterator i = m_installer.m_tables.iterator();
        while (i.hasNext()) {
            String table = ((String) i.next()).toLowerCase();
            m_installer.getTableFromSQL(table);
        }
    }

    // XXX this should be an integration test
    public void testCreateSequences() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
    }
    
    // XXX this should be an integration test
    public void testCreateTables() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();
    }

    // XXX this should be an integration test
    public void testCreateTablesTwice() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        // First pass.
        m_installer.createSequences();
        m_installer.createTables();
        
        /*
         * Second pass.
         * We don't care about the output from this, so we clear the
         * ByteArrayOutputStream after we call createSequences().
         * It's important to test the sequence part, and do it first,
         * because the tables depend on sequences for their ID column.
         */ 
        m_installer.createSequences();

        /*
         * Create a new ByteArrayOutputStream so we can look for UPTODATE for
         * every table
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m_installer.m_out = new PrintStream(out);
        m_installer.createTables();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("- creating tables\\.\\.\\.")) {
                continue;
            }
            if (line.matches("  - checking table \"\\S+\"\\.\\.\\. UPTODATE")) {
                continue;
            }
            if (line.matches("- creating tables\\.\\.\\. DONE")) {
                continue;
            }
            fail("Unexpected line output by createTables(): \"" + line + "\"");
        }
    }

    /**
     * Call Installer.checkOldTables, which should *not* throw an exception
     * because we have not created a table matching "_old_".
     */
    public void testBug1006NoOldTables() {
        if (!isDBTestEnabled()) {
            return;
        }

        ThrowableAnticipator ta = new ThrowableAnticipator();

        try {
            m_installer.checkOldTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    /**
     * Call Installer.checkOldTables, which *should* throw an exception
     * because we have created a table matching "_old_". We check the
     * exception message to ensure that it is the exception we are expecting,
     * and fail otherwise.
     */
    public void testBug1006HasOldTables() throws SQLException {
        if (!isDBTestEnabled()) {
            return;
        }

        // final String errorSubstring = "One or more backup tables from a
        // previous install still exists";

        String table = "testBug1006_old_" + System.currentTimeMillis();

        Statement st = m_installer.m_dbconnection.createStatement();
        st.execute("CREATE TABLE " + table + " ( foo integer )");
        st.close();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        LinkedList<String> l = new LinkedList<String>();
        l.add(table);
        ta.anticipate(new BackupTablesFoundException(l));

        try {
            m_installer.checkOldTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void executeSQL(String[] commands) {
        if (!isDBTestEnabled()) {
            return;
        }

        Statement st = null;;
        try {
            st = m_installer.m_dbconnection.createStatement();
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

    public void executeSQL(String command) {
        executeSQL(new String[] { command });
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
        doTestBogusConstraint(
                              constraint,
                              "Constraint "
                                      + constraint
                                      + " is on table "
                                      + "ipinterface, but table does not exist (so fixing this constraint does nothing).");
    }

    public void testBogusConstraintColumn() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String constraint = "fk_dpname";
        doTestBogusConstraint(constraint, "Constraint " + constraint
                + " is on column "
                + "dpname of table node, but column does not "
                + "exist (so fixing this constraint does nothing).");
    }

    public void testConstraintAfterConstrainedColumn() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String s_create_sql = "            create table distPoller (\n"
                + "                    dpName            varchar(12),\n"
                + "                                constraint pk_dpName primary key (dpName),\n"
                + "                    dpIP            varchar(16) not null,\n"
                + "                    dpComment        varchar(256),\n"
                + "                    dpDiscLimit        numeric(5,2),\n"
                + "                    dpLastNodePull        timestamp without time zone,\n"
                + "                    dpLastEventPull        timestamp without time zone,\n"
                + "                    dpLastPackagePush    timestamp without time zone,\n"
                + "                    dpAdminState         integer,\n"
                + "                    dpRunState        integer );\n";

        m_installer.readTables(new StringReader(s_create_sql));
        m_installer.getTableColumnsFromSQL("distpoller");
    }

    public void testConstraintAtEndOfTable() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String s_create_sql = "            create table distPoller (\n"
                + "                    dpName            varchar(12),\n"
                + "                    dpIP            varchar(16) not null,\n"
                + "                    dpComment        varchar(256),\n"
                + "                    dpDiscLimit        numeric(5,2),\n"
                + "                    dpLastNodePull        timestamp without time zone,\n"
                + "                    dpLastEventPull        timestamp without time zone,\n"
                + "                    dpLastPackagePush    timestamp without time zone,\n"
                + "                    dpAdminState         integer,\n"
                + "                    dpRunState        integer,\n"
                + "                                constraint pk_dpName primary key (dpName) );\n";

        m_installer.readTables(new StringReader(s_create_sql));
        m_installer.getTableColumnsFromSQL("distpoller");
    }

    public void testConstraintOnBogusColumn() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String s_create_sql = "            create table distPoller (\n"
                + "                    dpName            varchar(12),\n"
                + "                    dpIP            varchar(16) not null,\n"
                + "                    dpComment        varchar(256),\n"
                + "                    dpDiscLimit        numeric(5,2),\n"
                + "                    dpLastNodePull        timestamp without time zone,\n"
                + "                    dpLastEventPull        timestamp without time zone,\n"
                + "                    dpLastPackagePush    timestamp without time zone,\n"
                + "                    dpAdminState         integer,\n"
                + "                    dpRunState        integer,\n"
                + "                                constraint pk_dpName primary key (dpNameBogus) );\n";

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(
                                    "constraint pk_dpname references column \"dpnamebogus\", which is not a column in the table distpoller"));

        m_installer.readTables(new StringReader(s_create_sql));
        try {
            m_installer.getTableColumnsFromSQL("distpoller");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void doTestBogusConstraint(String constraint,
            String exceptionMessage) throws Exception {
        m_installer.m_fix_constraint_name = constraint;

        setupBug931(false, false);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(exceptionMessage));

        try {
            m_installer.fixConstraint();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void doTestBug931(boolean dropForeignTable, int badRows,
            boolean fixConstraint) throws Exception {
        final String errorSubstring = "Table events contains "
                + badRows
                + " rows (out of 2) that violate new constraint "
                + s_constraint
                + ".  See the install guide for details on how to correct this problem.";

        setupBug931((badRows != 0) || fixConstraint, dropForeignTable);

        if (fixConstraint) {
            m_installer.fixConstraint();
        }

        ThrowableAnticipator ta = new ThrowableAnticipator();
        if (badRows > 0) {
            ta.anticipate(new Exception(errorSubstring));
        }

        try {
            m_installer.checkConstraints();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testParsePrimaryKeyMultipleColumns() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        // Make sure that every table, column, and key ID has at least one
        // upper case character
        final String createSQL = "create table Element (\n"
                + "    mapId           integer,\n"
                + "    elementId       integer,\n"
                + "    somethingElse       varchar(80),\n"
                + "    constraint pk_Element primary key (mapId, elementId)\n"
                + ");";

        m_installer.readTables(new StringReader(createSQL));
        Table table = m_installer.getTableFromSQL("element");

        List<Column> columns = table.getColumns();
        assertNotNull("column list is not null", columns);
        assertEquals("column count", 3, columns.size());
        assertEquals("column zero toString()", "mapid integer(4) NOT NULL",
                     columns.get(0).toString());
        assertEquals("column one toString()",
                     "elementid integer(4) NOT NULL",
                     columns.get(1).toString());
        assertEquals("column two toString()",
                     "somethingelse character varying(80)",
                     columns.get(2).toString());

        List<Constraint> foo = table.getConstraints();

        assertNotNull("constraint list is not null", foo);
        assertEquals("constraint count is one", 1, foo.size());
        Constraint f = foo.get(0);
        assertNotNull("constraint zero is not null", f);
        assertEquals("constraint getTable()", "element", f.getTable());
        assertEquals("constraint zero toString()",
                     "constraint pk_element primary key (mapid, elementid)",
                     f.toString());
    }

    public void testInsertMultipleColumns() throws SQLException {
        if (!isDBTestEnabled()) {
            return;
        }

        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);
    }

    public void testInsertMultipleColumnsGetFromDB() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        m_installer.getTableColumnsFromDB("qrtz_job_details");
    }

    public void testInsertMultipleColumnsGetFromDBCompare() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        Table table = m_installer.getTableFromDB("qrtz_job_details");
        assertNotNull("table not null", table);

        List<Constraint> constraints = table.getConstraints();
        assertNotNull("constraints not null", constraints);
        assertEquals("constraints size equals one", 1, constraints.size());
        assertEquals(
                     "constraint zero toString()",
                     "constraint pk_qrtz_job_details primary key (job_name, job_group)",
                     constraints.get(0).toString());
    }

    public void testGetColumnsFromDB() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        List<Column> columns = m_installer.getColumnsFromDB("qrtz_job_details");
        assertNotNull("column list not null", columns);
        assertEquals("column list size", 2, columns.size());
        assertEquals("column zero toString()",
                     "job_name character varying(80) NOT NULL",
                     columns.get(0).toString());
        assertEquals("column one toString()",
                     "job_group character varying(80) NOT NULL",
                     columns.get(1).toString());
    }

    public void testGetConstraintsFromDB() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        List<Column> columns = m_installer.getColumnsFromDB("qrtz_job_details");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = m_installer.getConstraintsFromDB("qrtz_job_details");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals("constraint zero toString()",
                     "constraint pk_qrtz_job_details primary key (job_name, job_group)",
                     constraints.get(0).toString());
    }
    
    public void testSetEventSourceOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        
        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("events", new String[][] {
                new String[] { "eventSource\\s+varchar\\(\\d+\\) not null,", "" }
            });
        
        executeSQL("INSERT INTO events (eventID, eventUei, eventTime, eventDpName, eventCreateTime, eventSeverity, eventLog, eventDisplay) "
                   + "VALUES ( 1, 'uei.opennms.org/eatmyshorts', now(), 'Duh', now(), 1, 'n', 'n' )");

        m_installer.createTables();

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT eventsource from events");
        int count = 0;
        while (rs.next()) {
            assertEquals("expected events eventsrource", "OpenNMS.Eventd", rs.getString(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
    
    }
    
    public void testSetOutageIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements("outages", new String[][] {
                new String[] { "outageID\\s+integer not null,", "" },
                new String[] { "constraint pk_outageID primary key \\(outageID\\),", "" }
            });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (id, nodeID, ipAddr, ifIndex, serviceID, ipInterfaceId) VALUES ( 1, 1, '1.2.3.4', null, 1, 1 )");
        
        executeSQL("INSERT INTO outages (nodeId, ipAddr, ifLostService, serviceId, ifServiceId ) "
                   + "VALUES ( 1, '1.2.3.4', now(), 1, 1 )");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT outageid from outages");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected outages outageid", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }
    
    public void testSetOutagesSvcRegainedEventIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQL("outages");

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (id, nodeID, ipAddr, ifIndex, serviceID, ipInterfaceId) VALUES ( 1, 1, '1.2.3.4', null, 1, 1 )");

        executeSQL("INSERT INTO outages ( outageID, nodeId, ipAddr, ifLostService, serviceId, ifServiceId ) "
                   + "VALUES ( 1, 1, '1.2.3.4', now(), 1, 1 )");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT svcregainedeventid from outages");
        int count = 0;
        while (rs.next()) {
            assertEquals("expected outages svcregainedeventid", 0, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }
    
    public void testSetNotificationsEventIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();
        
        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements("notifications", new String[][] {
                new String[] { "eventID\\s+integer,", "" },
                new String[] { ",\\s+constraint fk_eventID3 foreign key \\(eventID\\) references events \\(eventID\\) ON DELETE CASCADE", "" }
            });
        
        executeSQL("INSERT INTO notifications (textMsg, notifyID, eventUEI) "
                   + "VALUES ('DJ broke it... it is always his fault', 1, "
                   + "'We ain\\\'t got no UEIs here, no sir.')");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT eventID from notifications");
        int count = 0;
        while (rs.next()) {
            assertEquals("expected notifications eventID", 0, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
    }

    
    public void testSetUsersNotifiedIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQL("notifications");
        addTableFromSQLWithReplacements("usersnotified", new String[][] {
                new String[] { "id\\s+integer not null, ", "" },
                new String[] { "constraint pk_userNotificationID primary key \\(id\\),", "" }
            });
        
        executeSQL("INSERT INTO usersNotified (userID) "
                   + "VALUES ('DJ... it is always his fault')");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from usersnotified");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected usersNotified id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
    }
    
    public void testSetSnmpInterfaceIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] { "(?i)id\\s+INTEGER DEFAULT nextval\\('opennmsNxtId'\\) NOT NULL,", "" },
                new String[] { "(?i)CONSTRAINT snmpinterface_pkey primary key \\(id\\),", "" }
            });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES (1, '1.2.3.4', 1)");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from snmpInterface");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected usersNotified id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
        
    }
    
    public void testCatchSnmpInterfaceNullNodeIdColumnOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] { "(?i)nodeID\\s+integer not null,", "nodeId integer," }
            });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from snmpInterface");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected usersNotified id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
        
    }

    public void testCatchSnmpInterfaceHasNullNodeIdValueOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] { "(?i)nodeID\\s+integer not null,", "nodeId integer," }
            });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr) VALUES ( null, '1.2.3.4' )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Error changing table 'snmpinterface'.  Nested exception: The nodeId column in the snmpInterface table should never be null, but the entry for this row does have a null nodeId.  It needs to be removed or udpated to reflect a valid nodeId."));
        try {
            m_installer.createTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    
    public void testCatchIpInterfaceNullIpAddrColumnOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements("ipinterface", new String[][] {
                new String[] { "(?i)ipAddr\\s+varchar\\(16\\) not null,", "ipAddr varchar(16)," }
            });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr) VALUES ( 1, '1.2.3.4' )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.9', 1 )");

        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from snmpInterface");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected usersNotified id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
        
    }

    public void testCatchIpInterfaceHasNullIpAddrValueOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements("ipinterface", new String[][] {
                new String[] { "(?i)ipAddr\\s+varchar\\(16\\) not null,", "ipAddr varchar(16)," }
            });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr) VALUES ( 1, '1.2.3.4' )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, null, null )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, null, 1 )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Error changing table 'ipinterface'.  Nested exception: The ipAddr column in the ipInterface table should never be null, but the entry for this row does have a null ipAddr.  It needs to be removed or udpated to reflect a valid ipAddr."));
        try {
            m_installer.createTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    public void testIpInterfaceForeignKeySnmpInterfaceIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();
        for (int i = 0; i < 10; i++) {
            executeSQL("SELECT nextval('opennmsNxtId');");
        }
        
        addTableFromSQL("distPoller");
        addTableFromSQL("node");
        
        // No ID column
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] { "(?i)id\\s+INTEGER DEFAULT nextval\\('opennmsNxtId'\\) NOT NULL,", "" },
                new String[] { "(?i)CONSTRAINT snmpinterface_pkey primary key \\(id\\),", "" }
            });
      
        // No snmpInterfaceID column
        addTableFromSQLWithReplacements("ipinterface", new String[][] {
                new String[] { "(?i)snmpInterfaceId\\s+integer,", "" },
                new String[] { "(?i)CONSTRAINT snmpinterface_fkey1 FOREIGN KEY \\(snmpInterfaceId\\) REFERENCES snmpInterface \\(id\\),", "" }
                });
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', null)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.9', 1 )");
        
        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT snmpInterfaceID from ipInterface");
        
        assertTrue("Could not ResultSet.next() to first result entry", rs.next());
        int got = rs.getInt(1);
        assertFalse("first result should not be null, but was null", rs.wasNull());
        assertEquals("ipInterface snmpInterfaceId", 1, got);
        
        assertTrue("Could not ResultSet.next() to second result entry", rs.next());
        assertFalse("second result should be null, but wasn't null", rs.wasNull());

        assertFalse("Too many entries", rs.next());
    }

    public void testIfServicesForeignKeyIpInterfaceIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();
        for (int i = 0; i < 10; i++) {
            executeSQL("SELECT nextval('opennmsNxtId');");
        }
        
        addTableFromSQL("distPoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
      
        // No ID column
        addTableFromSQLWithReplacements("ipinterface", new String[][] {
                new String[] { "(?i)id\\s+integer default nextval\\('opennmsNxtId'\\) not null,", "" },
                new String[] { "(?i)constraint ipinterface_pkey primary key \\(id\\),", "" }
                });
        
        addTableFromSQL("service");
        
        // No ID or ipInterfaceID column
        addTableFromSQLWithReplacements("ifservices", new String[][] {
                new String[] { "(?i)id\\s+integer default nextval\\('opennmsNxtId'\\) not null,", "" },
                new String[] { "(?i)constraint ifServices_pkey primary key \\(id\\),", "" },
                new String[] { "(?i)ipInterfaceID\\s+integer not null,", "" },
                new String[] { "(?i)constraint ipinterface_fkey foreign key \\(ipInterfaceId\\) references ipInterface \\(id\\) ON DELETE CASCADE,", "" }
                });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.9', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.9', 1, 1)");
        
        m_installer.createTables();
        
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from ipInterface");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected ipInterface id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 2, count);

        rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
        count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected ifServices id", expected, rs.getInt(1));
            assertEquals("expected ifServices ipInterfaceId", expected, rs.getInt(2));
            count++;
        }
        assertEquals("expected column count", 2, count);
    }
    
    public void testAddStoredProcedures() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        for (String function : new String[] { "getIfServiceId",
                "getIpInterfaceId", "getSnmpInterfaceId"} ) {
            assertTrue("Function '" + function + "' does not exist",
                       m_installer.functionExists(function, "", "trigger"));
        }

        assertTrue("Trigger setIfServiceIdInOutage does not exist",
                   m_installer.triggerExists("setIfServiceIdInOutage",
                                             "outages",
                                             "getIfServiceId"));

        assertTrue("Trigger setIpInterfaceIdInIfService does not exist",
                   m_installer.triggerExists("setIpInterfaceIdInIfService",
                                             "ifServices",
                                             "getIpInterfaceId"));

        assertTrue("Trigger setSnmpInterfaceIdInIpInterface does not exist",
                   m_installer.triggerExists("setSnmpInterfaceIdInIpInterface",
                                             "ipInterface",
                                             "getSnmpInterfaceId"));
    }
    
    public void testAddStoredProceduresTwice() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();

        m_installer.addStoredProcedures();
        m_installer.addStoredProcedures();

        for (String function : new String[] { "getIfServiceId",
                "getIpInterfaceId", "getSnmpInterfaceId"} ) {
            assertTrue("Function '" + function + "' does not exist",
                       m_installer.functionExists(function, "", "trigger"));
        }
    }

    public void testTriggerSetSnmpInterfaceIdInIpInterface() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected ipInterface id", 2, rs.getInt(1));
        
        int id = rs.getInt(2);
        assertFalse("expected ipInterface snmpInterfaceId to be non-null", rs.wasNull());
        assertEquals("expected ipInterface snmpInterfaceId to be the same", 1, id);
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    public void testTriggerSetSnmpInterfaceIdInIpInterfaceNoSnmpInterfaceEntry() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )': ERROR: IpInterface Trigger Exception: No SnmpInterface found for... nodeid: 1  ipaddr: 1.2.3.4  ifindex: 1"));
        try {
            executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testTriggerSetSnmpInterfaceIdInIpInterfaceNullIfIndex() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("ipInterface id", 2, rs.getInt(1));

        int id = rs.getInt(2);
        assertTrue("ipInterface snmpInterfaceId to be null (was " + id + ")", rs.wasNull());
        assertFalse("ResultSet contains more than one row", rs.next());
    }

    public void testTriggerSetSnmpInterfaceIdInIpInterfaceNullIfIndexNoSnmpInterface() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("ipInterface id", 1, rs.getInt(1));
        
        int id = rs.getInt(2);
        assertTrue("ipInterface snmpInterfaceId to be null (was " + id + ")", rs.wasNull());
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    public void testTriggerSetSnmpInterfaceIdInIpInterfaceLessThanOneIfIndex() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 0 )");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected ipInterface id", 1, rs.getInt(1));
        
        int id = rs.getInt(2);
        assertTrue("expected ipInterface snmpInterfaceId to be null (got " + id + ")", rs.wasNull());
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    public void testTriggerSetSnmpInterfaceIdInIpInterfaceLessThanOneIfIndexWithSnmpInterface() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 0)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 0 )");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("ipInterface id", 2, rs.getInt(1));
        
        int id = rs.getInt(2);
        assertTrue("ipInterface snmpInterfaceId should be null (was " + id + ")", rs.wasNull());
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    public void testTriggerSetIpInterfaceIdInIfService() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected ifServices id", 3, rs.getInt(1));
        assertEquals("expected ifServices ipInterfaceId", 2, rs.getInt(2));
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    public void testTriggerSetIpInterfaceIdInIfServiceNullIfIndexBoth() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected ifServices id", 2, rs.getInt(1));
        assertEquals("expected ifServices ipInterfaceId", 1, rs.getInt(2));
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    
    public void testTriggerSetIpInterfaceIdInIfServiceNullIfIndexInIfServices() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)': ERROR: IfServices Trigger Exception: No IpInterface found for... nodeid: 1  ipaddr: 1.2.3.4  ifindex: <NULL>"));
        try {
            executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTriggerSetIpInterfaceIdInIfServiceNullIfIndexInIpInterface() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)': ERROR: IfServices Trigger Exception: No IpInterface found for... nodeid: 1  ipaddr: 1.2.3.4  ifindex: 1"));
        try {
            executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testTriggerSetIfServiceIdInOutage() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                   + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), 1 )");

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT outageId, ifServiceId from outages");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected outages outageId", 1, rs.getInt(1));
        assertEquals("expected outages ifServiceId", 3, rs.getInt(2));
        assertFalse("ResultSet contains more than one row", rs.next());
    }
    
    public void testTriggerSetIfServiceIdInOutageNullNodeId() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', 1, 1)");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) VALUES ( nextval('outageNxtId'), null, '1.2.3.4', now(), null )': ERROR: Outages Trigger Exception: No service found for... nodeid: 1  ipaddr: <NULL>  serviceid: 1"));
        try {
            executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                       + "VALUES ( nextval('outageNxtId'), null, '1.2.3.4', now(), 1 )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();

        /*
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT outageId, ifServiceId from outages");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected outages outageId", 1, rs.getInt(1));
        assertEquals("expected outages ifServiceId", 3, rs.getInt(2));
        assertFalse("ResultSet contains more than one row", rs.next());
        */
    }
    
    public void testTriggerSetIfServiceIdInOutageNullServiceId() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();
        
        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new AssertionFailedError("Could not execute statement: 'INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), null )': ERROR: Outages Trigger Exception: No service found for... nodeid: 1  ipaddr: 1.2.3.4  serviceid: <NULL>"));
        try {
            executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                       + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), null )");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();

        /*
        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT outageId, ifServiceId from outages");
        assertTrue("could not advance to read first row in ResultSet", rs.next());
        assertEquals("expected outages outageId", 1, rs.getInt(1));
        assertEquals("expected outages ifServiceId", 1, rs.getInt(2));
        assertFalse("ResultSet contains more than one row", rs.next());
        */
    }
    

    public void testSnmpInterfaceNodeIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] { "(?i)nodeID\\s+integer not null,", "nodeId integer," }
            });

        m_installer.createTables();
    }


    public void testSnmpInterfaceSnmpIfIndexColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] { "(?i)snmpIfIndex\\s+integer not null,", "snmpIfIndex integer," }
            });

        m_installer.createTables();
    }

    public void testIpInterfaceNodeIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements("ipinterface", new String[][] {
                new String[] { "(?i)nodeID\\s+integer not null,", "nodeId integer," }
            });

        m_installer.createTables();
    }
    
    public void testIfServicesNodeIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQLWithReplacements("ifservices", new String[][] {
                new String[] { "(?i)nodeID\\s+integer not null,", "nodeId integer," }
            });

        m_installer.createTables();
    }
    
    public void testIfServicesServiceIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQLWithReplacements("ifservices", new String[][] {
                new String[] { "(?i)serviceID\\s+integer not null,", "serviceId integer," }
            });

        m_installer.createTables();
    }

    

    public void testOutagesNodeIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements("outages", new String[][] {
                new String[] { "(?i)nodeID\\s+integer not null,", "nodeId integer," }
            });

        m_installer.createTables();
    }


    public void testOutagesServiceIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        m_installer.createSequences();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements("outages", new String[][] {
                new String[] { "(?i)serviceID\\s+integer not null,", "serviceID integer," }
            });

        m_installer.createTables();
    }

    
    public void addTableFromSQL(String tableName) {
        String partialSQL = null;
        try {
            partialSQL = m_installer.getTableCreateFromSQL(tableName);
        } catch (Exception e) {
            fail("Could not get SQL for table '" + tableName + "'", e);
        }
        
        executeSQL("CREATE TABLE " + tableName + " ( " + partialSQL + " )");
    }
    
    public void addTableFromSQLWithReplacements(String tableName, String[][] replacements) {
        String partialSQL = null;
        try {
            partialSQL = m_installer.getTableCreateFromSQL(tableName);
        } catch (Exception e) {
            fail("Could not get SQL for table '" + tableName + "'", e);
        }
        
        for (String[] replacement : replacements) {
            Pattern p = Pattern.compile(replacement[0]);
            Matcher m = p.matcher(partialSQL);
            if (!m.find()) {
                StringBuffer error = new StringBuffer();
                error.append("Could not find a match for pattern '"
                             + p.toString() + "'");
                if (containsUnescapedParens(p.toString())) {
                    error.append(" (pattern contains unescaped parenthesis--"
                                 + "should they be backslash escaped?)");
                }
                error.append(" in string '" + partialSQL + "'.");
                fail(error.toString());
            }
            partialSQL = partialSQL.replaceFirst(replacement[0], replacement[1]);
        }

        executeSQL("CREATE TABLE " + tableName + " ( " + partialSQL + " )");
    }
    
    public boolean containsUnescapedParens(String str) {
        Pattern p = Pattern.compile("[^\\\\]\\(");
        Matcher m = p.matcher(str);
        return m.find();
    }
    
    public boolean setUpForTriggerTest() throws Exception {
        if (!isDBTestEnabled()) {
            return false;
        }

        m_installer.createSequences();
        m_installer.createTables();

        m_installer.updatePlPgsql();
        
        m_installer.addStoredProcedures();

        return true;
    }
}