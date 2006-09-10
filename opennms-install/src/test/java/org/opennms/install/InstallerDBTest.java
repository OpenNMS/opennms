//
// $Id: InstallerDBTest.java 4189 2006-08-31 18:42:53 +0000 (Thu, 31 Aug 2006)
// djgregor $
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

public class InstallerDBTest extends TemporaryDatabaseTestCase {
    private static final String s_constraint = "fk_nodeid6";

    private Installer m_installer;

    protected void setUp() throws Exception {
        super.setUp();
        
        m_installer = new Installer();

        // Create a ByteArrayOutputSteam to effectively throw away output.
        m_installer.m_out = new PrintStream(new ByteArrayOutputStream());
        m_installer.m_database = getTestDatabase();
        m_installer.m_pg_driver = getDriver();
        m_installer.m_pg_url = getUrl();
        m_installer.m_pg_user = getAdminUser();
        m_installer.m_pg_pass = getAdminPassword();
        m_installer.m_user = "opennms";

        m_installer.m_create_sql =
            "../opennms-daemon/src/main/filtered/etc/create.sql";

        /*
         * URL sql = getClass().getResource("/create.sql");
         * assertNotNull("Could not find create.sql", sql);
         * m_installer.m_create_sql = sql.getFile();
         */

        m_installer.m_sql_dir = "../opennms-daemon/src/main/filtered/etc";

        m_installer.m_fix_constraint = true;
        m_installer.m_fix_constraint_name = s_constraint;

        m_installer.m_debug = false;

        // Read in the table definitions
        m_installer.readTables();
        
        m_installer.m_dbconnection = getDbConnection();

        /*
        if (!isDBTestEnabled()) {
            return;
        }

        // Create test database.
        m_installer.databaseConnect("template1");
        m_installer.databaseAddDB();
        m_installer.databaseDisconnect();

        // Connect to test database.
        m_installer.databaseConnect(m_testDatabase);
        */
    }

    public void tearDown() throws Exception {
        super.tearDown();
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
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();
    }

    // XXX this should be an integration test
    public void testCreateTablesTwice() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        // First pass.
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();

        /*
         * Second pass. We don't care about the output from this, so we clear
         * the ByteArrayOutputStream after we call createSequences(). It's
         * important to test the sequence part, and do it first, because the
         * tables depend on sequences for their ID column.
         */
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

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
            if (line.matches("    - checking trigger '\\S+' on this table\\.\\.\\. DONE")) {
                continue;
            }
            if (line.matches("    - checking trigger '\\S+' on this table\\.\\.\\. DONE")) {
                continue;
            }
            if (line.matches("    - checking index '\\S+' on this table\\.\\.\\. DONE")) {
                continue;
            }
            if (line.matches("- creating tables\\.\\.\\. DONE")) {
                continue;
            }
            fail("Unexpected line output by createTables(): \"" + line + "\"");
        }
    }

    public void testUpgradeRevision3952ToCurrent() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String newCreate = m_installer.m_create_sql;

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        m_installer.m_create_sql = sql.getFile();

        // First pass.
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();

        m_installer.m_create_sql = newCreate;

        // Second pass.
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();

    }
    
    public void testUpgradeRevision3952ToCurrentWithData() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        String newCreate = m_installer.m_create_sql;

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        m_installer.m_create_sql = sql.getFile();
        m_installer.readTables();

        // First pass.
        m_installer.createSequences();
        m_installer.m_triggerDao = new TriggerDao();
        //m_installer.updatePlPgsql();
        //m_installer.addStoredProcedures();

        m_installer.createTables();
        
        
        // Data
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.6', -100 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.5', null )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.6', -100 )");
        executeSQL("INSERT INTO service ( serviceID, serviceName ) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO service ( serviceID, serviceName ) VALUES ( 2, 'TEA-READY' )");
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 1, '1.2.3.4', 1, 1 )");
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 1, '1.2.3.5', null, 1 )");
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 1, '1.2.3.6', -100, 1 )");
//        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 1, '1.2.3.6', null, 2 )");
        executeSQL("INSERT INTO outages ( outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                   + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), 1 )");
        executeSQL("INSERT INTO outages ( outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                   + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.5', now(), 1 )");
        executeSQL("INSERT INTO outages ( outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                   + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.6', now(), 1 )");
//        executeSQL("INSERT INTO outages ( outageId, nodeId, ipAddr, ifLostService, serviceID ) "
//                   + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.6', now(), 2 )");


        m_installer.m_create_sql = newCreate;
        m_installer.readTables();

        // Second pass.
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();

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
                + " constrains column "
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

    public void testConstraintIpInterfaceSnmpInterfaceValidData() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        String newCreate = m_installer.m_create_sql;

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        m_installer.m_create_sql = sql.getFile();
        m_installer.readTables();

        // First pass.
        m_installer.createSequences();
        m_installer.m_triggerDao = new TriggerDao();
        //m_installer.updatePlPgsql();
        //m_installer.addStoredProcedures();

        m_installer.createTables();
        
        // Data
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.6', -100 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.5', null )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.6', -100 )");

        m_installer.m_create_sql = newCreate;
        m_installer.readTables();

        // Second pass.
        m_installer.checkConstraints();
        /*
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();
        */
    }
    public void testConstraintIpInterfaceSnmpInterfaceInvalidData() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }
        
        String newCreate = m_installer.m_create_sql;

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        m_installer.m_create_sql = sql.getFile();
        m_installer.readTables();

        // First pass.
        m_installer.createSequences();
        m_installer.m_triggerDao = new TriggerDao();
        //m_installer.updatePlPgsql();
        //m_installer.addStoredProcedures();

        m_installer.createTables();
        
        // Data
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.5', null )");
//        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.6', -100 )");

        m_installer.m_create_sql = newCreate;
        m_installer.readTables();

        // Second pass.
        m_installer.checkConstraints();
        /*
        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();
        */
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
        final String errorSubstring;
        if (dropForeignTable) {
            errorSubstring = "Table events contains "
                + badRows
                + " rows (out of 2) that violate new constraint "
                + s_constraint
                + ".  See the install guide for details on how to correct this "
                + "problem.  You can execute this SQL query to see a list of "
                + "the rows that violate the constraint:\n"
                + "SELECT * FROM events WHERE events.nodeid IS NOT NULL";
        } else {
            errorSubstring = "Table events contains "
                + badRows
                + " rows (out of 2) that violate new constraint "
                + s_constraint
                + ".  See the install guide for details on how to correct this "
                + "problem.  You can execute this SQL query to see a list of "
                + "the rows that violate the constraint:\n"
                + "SELECT * FROM events LEFT JOIN node ON (events.nodeid = "
                + "node.nodeid) WHERE node.nodeid is NULL AND events.nodeid "
                + "IS NOT NULL";
        }

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
    
    public void testParseConstraintWithOnUpdateCascade() throws Exception {
        // Make sure that every table, column, and key ID has at least one
        // upper case character
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on update cascade\n"
                + ");\n";

        m_installer.readTables(new StringReader(createSQL));
        Table a = m_installer.getTableFromSQL("a");
        Table b = m_installer.getTableFromSQL("b");

        /*
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
        */
    }

    public void testGetFromDbConstraintWithOnUpdateCascade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on update cascade\n"
                + ");\n";

        executeSQL(createSQL);

        List<Column> columns = m_installer.getColumnsFromDB("b");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = m_installer.getConstraintsFromDB("b");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals("constraint zero toString()",
                     "constraint fk_a foreign key (b1) references a (a1) "
                     + "on update cascade",
                     constraints.get(0).toString());
    }

    public void testParsePrimaryKeyMultipleColumns() throws Exception {
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
        assertEquals(
                     "constraint zero toString()",
                     "constraint pk_qrtz_job_details primary key (job_name, job_group)",
                     constraints.get(0).toString());
    }

    public void testSetEventSourceOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("events",
                                        new String[][] { new String[] {
                                                "eventSource\\s+varchar\\(\\d+\\) not null,",
                                                "" } }, true);

        executeSQL("INSERT INTO events (eventID, eventUei, eventTime, eventDpName, eventCreateTime, eventSeverity, eventLog, eventDisplay) "
                + "VALUES ( 1, 'uei.opennms.org/eatmyshorts', now(), 'Duh', now(), 1, 'n', 'n' )");

        m_installer.createTables();

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT eventsource from events");
        int count = 0;
        while (rs.next()) {
            assertEquals("expected events eventsrource", "OpenNMS.Eventd",
                         rs.getString(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }

    public void XXXtestSetOutageIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements("outages", new String[][] {
                new String[] { "outageID\\s+integer not null,", "" },
                new String[] {
                        "constraint pk_outageID primary key \\(outageID\\),",
                        "" } }, true);

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (id, nodeID, ipAddr, ifIndex, serviceID, ipInterfaceId) VALUES ( 1, 1, '1.2.3.4', null, 1, 1 )");

        executeSQL("INSERT INTO outages (nodeId, ipAddr, ifLostService, serviceId, ifServiceId ) "
                + "VALUES ( 1, '1.2.3.4', now(), 1, 1 )");

        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

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

    public void j() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

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
            assertEquals("expected outages svcregainedeventid", 0,
                         rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }

    public void testSetUsersNotifiedIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQL("notifications");
        addTableFromSQLWithReplacements(
                                        "usersnotified",
                                        new String[][] {
                                                new String[] {
                                                        "id\\s+integer not null, ",
                                                        "" },
                                                new String[] {
                                                        "constraint pk_userNotificationID primary key \\(id\\),",
                                                        "" } });

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
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)id\\s+INTEGER DEFAULT nextval\\('opennmsNxtId'\\) NOT NULL,",
                                                        "" },
                                                new String[] {
                                                        "(?i)CONSTRAINT snmpinterface_pkey primary key \\(id\\),",
                                                        "" } });

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

    public void testCatchSnmpInterfaceNullNodeIdColumnOnUpgrade()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");

        m_installer.createTables();

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from snmpInterface");

        assertTrue("Could not ResultSet.next() to first result entry",
                   rs.next());
        rs.getInt(1);
        assertFalse("first result should not be null, but was null",
                    rs.wasNull());

        // Don't care about the specific ID, it just needs to be non-null
        // assertEquals("snmpInterface id", 2, rs.getInt(1));

        assertFalse("Too many entries", rs.next());
    }

    public void testCatchSnmpInterfaceHasNullNodeIdValueOnUpgrade()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( null, '1.2.3.4', 1 )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(
                                    "Error changing table 'snmpinterface'.  Nested exception: The 'nodeId' column in the 'snmpInterface' table should never be null, but the entry for this row does have a null 'nodeId'column.  It needs to be removed or udpated to reflect a valid 'nodeId' value."));
        try {
            m_installer.createTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testCatchIpInterfaceNullIpAddrColumnOnUpgrade()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        Statement st;
        ResultSet rs;

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] { new String[] {
                                                "(?i)ipAddr\\s+varchar\\(16\\) not null,",
                                                "ipAddr varchar(16)," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        m_installer.createTables();
    }

    public void testCatchIpInterfaceHasNullIpAddrValueOnUpgrade()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] { new String[] {
                                                "(?i)ipAddr\\s+varchar\\(16\\) not null,",
                                                "ipAddr varchar(16)," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, null, 1 )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(
                                    "Error changing table 'ipinterface'.  Nested exception: The 'ipAddr' column in the 'ipInterface' table should never be null, but the entry for this row does have a null 'ipAddr'column.  It needs to be removed or udpated to reflect a valid 'ipAddr' value."));
        try {
            m_installer.createTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAssetsIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "assets",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)id\\s+INTEGER DEFAULT nextval\\('opennmsNxtId'\\) NOT NULL,",
                                                        "" },
                                                new String[] {
                                                        "(?i)constraint pk_assetID primary key \\(id\\),",
                                                        "" } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");

        executeSQL("INSERT INTO assets (nodeId, category, userLastModified, lastModifiedDate) VALUES (1, 'some category', 'dgregor broke it', now())");

        m_installer.createTables();

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from assets");

        assertTrue("Could not ResultSet.next() to first result entry",
                   rs.next());
        int got = rs.getInt(1);
        assertFalse("first result should not be null, but was null",
                    rs.wasNull());
        assertEquals("assets id", 1, got);

        assertFalse("Too many entries", rs.next());

    }

    public void testTriggersAfterUpdate() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();

        verifyTriggers(false);

        m_installer.createTables();

        verifyTriggers(false);
    }

    public void testTriggersAfterUpdateWithChange() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();


        /*
         * m_installer.createTables(); executeSQL("drop table outages");
         */

        addTableFromSQL("distPoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");

        /*
         * // No ID column addTableFromSQLWithReplacements("snmpinterface",
         * new String[][] { new String[] { "(?i)id\\s+INTEGER DEFAULT
         * nextval\\('opennmsNxtId'\\) NOT NULL,", "" }, new String[] {
         * "(?i)CONSTRAINT snmpinterface_pkey primary key \\(id\\),", "" } });
         */

        // No snmpInterfaceID column
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)snmpInterfaceId\\s+integer,",
                                                        "" },
                                                new String[] {
                                                        "(?i)CONSTRAINT snmpinterface_fkey2 FOREIGN KEY \\(snmpInterfaceId\\) REFERENCES snmpInterface \\(id\\) ON DELETE CASCADE,",
                                                        "" } });

        // addTableFromSQL("ipinterface");

        addTableFromSQL("service");
        addTableFromSQL("ifServices");
        addTableFromSQL("events");
        addTableFromSQL("outages");

        verifyTriggers(false);

        m_installer.createTables();

        verifyTriggers(false);
    }

    public void testIpInterfaceForeignKeySnmpInterfaceIdOnUpgrade()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distPoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpInterface");

        /*
        // No ID column
        addTableFromSQLWithReplacements("snmpinterface",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)id\\s+INTEGER DEFAULT nextval\\('opennmsNxtId'\\) NOT NULL,",
                                                        "" },
                                                new String[] {
                                                        "(?i)CONSTRAINT snmpinterface_pkey primary key \\(id\\),",
                                                        "" } });
                                                        */

        // No snmpInterfaceID column
        addTableFromSQLWithReplacements("ipinterface",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)snmpInterfaceId\\s+integer,",
                                                        "" },
                                                new String[] {
                                                        "(?i)CONSTRAINT snmpinterface_fkey2 FOREIGN KEY \\(snmpInterfaceId\\) REFERENCES snmpInterface \\(id\\) ON DELETE CASCADE,",
                                                        "" } }, false);

        addTableFromSQL("service");
        addTableFromSQL("ifServices");
        addTableFromSQL("events");
        addTableFromSQL("outages");

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");

        //      executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 2, now() )");
  //      executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 2, '1.2.3.9', null )");

        m_installer.createTables();

        verifyTriggers(false);

        Statement st;
        ResultSet rs;

        st = m_installer.m_dbconnection.createStatement();
        rs = st.executeQuery("SELECT id from snmpInterface ORDER BY nodeId");

        assertTrue("Could not ResultSet.next() to first result entry",
                   rs.next());
        rs.getInt(1);
        assertFalse("first result should not be null, but was null",
                    rs.wasNull());
        // I don't care about the value
        // assertEquals("smmpInterface id", 1, got);

        assertFalse("Too many entries", rs.next());

        st = m_installer.m_dbconnection.createStatement();
        rs = st.executeQuery("SELECT id, snmpInterfaceID from ipInterface ORDER BY nodeId");

        assertTrue("Could not ResultSet.next() to first result entry",
                   rs.next());
        rs.getInt(1);
        assertFalse("ipInterface.id in first result should not be null, but was null",
                    rs.wasNull());
        rs.getInt(2);
        assertFalse("ipInterface.snmpInterfaceId in first result should not be null, but was null",
                    rs.wasNull());
//        assertEquals("ipInterface snmpInterfaceId", 1, rs.getInt(1));
        assertFalse("More than one entry was found", rs.next());

        /*
        assertTrue("Could not ResultSet.next() to second result entry",
                   rs.next());
        rs.getInt(1);
        assertFalse("ipInterface.id in second result should not be null, but was null",
                    rs.wasNull());
        int got = rs.getInt(2);
        assertFalse("ipInterface.snmpInterfaceId in second result should be null, but wasn't null (was "
                + got + ")", rs.wasNull());

        assertFalse("Too many entries", rs.next());
        */
    }

    public void testIfServicesForeignKeyIpInterfaceIdOnUpgrade()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distPoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");

        // No ID column
        addTableFromSQLWithReplacements("ipinterface",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)id\\s+integer default nextval\\('opennmsNxtId'\\) not null,",
                                                        "" },
                                                new String[] {
                                                        "(?i)constraint ipinterface_pkey primary key \\(id\\),",
                                                        "" } });

        addTableFromSQL("service");

        // No ID or ipInterfaceID column
        addTableFromSQLWithReplacements("ifservices",
                                        new String[][] {
                /*
                                                new String[] {
                                                        "(?i)id\\s+integer default nextval\\('opennmsNxtId'\\) not null,",
                                                        "" },
                                                new String[] {
                                                        "(?i)constraint ifServices_pkey primary key \\(id\\),",
                                                        "" },
                                                        */
                                                new String[] {
                                                        "(?i)ipInterfaceID\\s+integer not null,",
                                                        "" },
                                                new String[] {
                                                        "(?i)constraint ipinterface_fkey foreign key \\(ipInterfaceId\\) references ipInterface \\(id\\) ON DELETE CASCADE,",
                                                        "" } }, false);

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID) VALUES ( 1, '1.2.3.4', null, 1)");

        m_installer.createTables();

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from ipInterface");

        assertTrue("could not advance results to first row", rs.next());
        rs.getInt(1);
        assertFalse("ipInterface.id should not be null", rs.wasNull());
        // Don't care about what it is, just that it's not null
        // assertEquals("ipInterface id", expected, rs.getInt(1));
        assertFalse("too many rows: only expecting one", rs.next());
        

        rs = st.executeQuery("SELECT id, ipInterfaceID from ifServices");

        assertTrue("could not advance results to first row", rs.next());

        rs.getInt(1);
        assertFalse("ifServices.id should not be null", rs.wasNull());
        rs.getInt(2);
        assertFalse("ifServices.interfaceId should not be null",
                    rs.wasNull());
        
        // Don't care about the actual values, just that they are not null
        // assertEquals("ifServices id", expected, rs.getInt(1));
        // assertEquals("ifServices ipInterfaceId", expected,
        //             rs.getInt(2));
        assertFalse("too many rows: only expecting one", rs.next());
    }

    public void testOutagesForeignKeyIfServiceIdOnUpgrade() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distPoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");

        /*
         * // No ID column addTableFromSQLWithReplacements("ipinterface", new
         * String[][] { new String[] { "(?i)id\\s+integer default
         * nextval\\('opennmsNxtId'\\) not null,", "" }, new String[] {
         * "(?i)constraint ipinterface_pkey primary key \\(id\\),", "" } });
         */

        addTableFromSQL("events");

        addTableFromSQL("service");

        // No ID column
        addTableFromSQLWithReplacements("ifservices",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)id\\s+integer default nextval\\('opennmsNxtId'\\) not null,",
                                                        "" },
                                                new String[] {
                                                        "(?i)constraint ifServices_pkey primary key \\(id\\),",
                                                        "" } });

        // No ifServiceId column
        addTableFromSQLWithReplacements(
                                        "outages",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)ifServiceId\\s+INTEGER not null,",
                                                        "" },
                                                new String[] {
                                                        "(?i),\\s+CONSTRAINT ifServices_fkey2 FOREIGN KEY \\(ifServiceId\\) REFERENCES ifServices \\(id\\) ON DELETE CASCADE",
                                                        "" } }, false);

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (id, nodeId, ipAddr, snmpIfIndex) VALUES ( 1, 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex, snmpInterfaceId ) VALUES ( 1, 1, '1.2.3.4', 1, 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID, ipInterfaceId) VALUES ( 1, '1.2.3.4', 1, 1, 1)");
        executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), 1 )");

        m_installer.createTables();

        Statement st = m_installer.m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from ifServices");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("ifServices id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("column count", 1, count);

        rs = st.executeQuery("SELECT ifServiceId from outages");
        count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("outages ifServiceId", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);
    }

    public void testAddStoredProcedures() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        m_installer.createTables();

        verifyTriggers(true);
    }

    public void testAddStoredProceduresTwice() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        
        //m_installer.createTables();
        
        m_installer.addStoredProcedures();

        verifyTriggers(true);
    }

    public void testSnmpInterfaceNodeIdColumnConvertToNotNull()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        m_installer.createTables();
    }

    public void testSnmpInterfaceSnmpIfIndexColumnConvertToNotNull()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)snmpIfIndex\\s+integer not null,",
                                                "snmpIfIndex integer," } });

        m_installer.createTables();
    }

    public void testIpInterfaceNodeIdColumnConvertToNotNull()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        m_installer.createTables();
    }

    public void testIfServicesNodeIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQLWithReplacements(
                                        "ifservices",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        m_installer.createTables();
    }

    public void testIfServicesIpAddrColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQLWithReplacements(
                                        "ifservices",
                                        new String[][] { new String[] {
                                                "(?i)ipAddr\\s+varchar\\(16\\) not null,",
                                                "ipAddr varchar(16)," } });

        m_installer.createTables();
    }

    public void testIfServicesServiceIdColumnConvertToNotNull()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQLWithReplacements(
                                        "ifservices",
                                        new String[][] { new String[] {
                                                "(?i)serviceID\\s+integer not null,",
                                                "serviceId integer," } });

        m_installer.createTables();
    }

    public void testOutagesNodeIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements(
                                        "outages",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        m_installer.createTables();
    }

    public void testOutagesServiceIdColumnConvertToNotNull() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements(
                                        "outages",
                                        new String[][] { new String[] {
                                                "(?i)serviceID\\s+integer not null,",
                                                "serviceID integer," } });

        m_installer.createTables();
    }

    public void testOutagesIfServiceIdColumnConvertToNotNull()
            throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        addTableFromSQL("events");
        addTableFromSQLWithReplacements("outages",
                                        new String[][] { new String[] {
                                                "(?i)ifServiceID\\s+integer not null,",
                                                "ifServiceId integer," } });

        m_installer.createTables();
    }
    
    public void testSnmpInterfaceNonUniqueKeys() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        executeSQL("drop index snmpinterface_nodeid_ifindex_idx");
        
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 1, now() )");

        // One test with identical entries
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 1 )");
        
        // One with different different IPs
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.1', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.2', 1 )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Unique index "
                                    + "'snmpinterface_nodeid_ifindex_idx' "
                                    + "cannot be added to table "
                                    + "'snmpinterface' because 4 rows are not "
                                    + "unique.  See the install guide for "
                                    + "details on how to correct this "
                                    + "problem.  You can use the following SQL "
                                    + "to see which rows are not unique:\n"
                                    + "SELECT * FROM snmpinterface WHERE ( "
                                    + "nodeID, snmpIfIndex ) IN ( SELECT "
                                    + "nodeID, snmpIfIndex FROM snmpinterface "
                                    + "GROUP BY nodeID, snmpIfIndex HAVING "
                                    + "count(nodeID) > 1 ORDER BY nodeID, "
                                    + "snmpIfIndex ) ORDER BY nodeID, "
                                    + "snmpIfIndex"));
        try {
            m_installer.checkIndexUniqueness();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testIpInterfaceNonUniqueKeys() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        executeSQL("drop index ipinterface_nodeid_ipaddr_notzero_idx");
        
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 1, now() )");
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 2, now() )");
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 3, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 2 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 2 )");
        
        // These aren't dups because their ipaddr = 0.0.0.0
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 2 )");

        // dups with ifIndex = null (which we don't care about)
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 2, '1.1.1.1', null )");
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 2, '1.1.1.1', null )");
        
        // dups with ifIndex != null (which we also don't care about)
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 2 )");

        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Unique index "
                                    + "'ipinterface_nodeid_ipaddr_notzero_idx' "
                                    + "cannot be added to table "
                                    + "'ipInterface' because 4 rows are not "
                                    + "unique.  See the install guide for "
                                    + "details on how to correct this "
                                    + "problem.  You can use the following SQL "
                                    + "to see which rows are not unique:\n"
                                    + "SELECT * FROM ipInterface WHERE ( "
                                    + "nodeID, ipAddr ) IN ( SELECT nodeID, "
                                    + "ipAddr FROM ipInterface GROUP BY "
                                    + "nodeID, ipAddr HAVING count(nodeID) > 1 "
                                    + "AND ( ipAddr != '0.0.0.0' ) ORDER BY "
                                    + "nodeID, ipAddr ) ORDER BY nodeID, "
                                    + "ipAddr"));
        try {
            m_installer.checkIndexUniqueness();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();

    }
    
    public void testIfServicesNonUniqueKeys() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("events");
        addTableFromSQL("service");
        addTableFromSQL("ifservices");
        executeSQL("drop index ifservices_nodeid_ipaddr_svc_unique");
        
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 1, now() )");
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 2, now() )");
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 3, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 2 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, ipAddr, snmpIfIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 2 )");
        
        // These aren't dups because their ipaddr = 0.0.0.0
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 1, '0.0.0.0', 2 )");

        // dups with ifIndex = null (which we don't care about)
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 2, '1.1.1.1', null )");
        //executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
        //         + "VALUES ( 2, '1.1.1.1', null )");
        
        // dups with ifIndex != null (which we also don't care about)
        executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
                   + "VALUES ( 3, '1.1.1.1', 1 )");
        //executeSQL("INSERT INTO ipInterface ( nodeID, ipAddr, ifIndex ) "
        //           + "VALUES ( 3, '1.1.1.1', 2 )");

        executeSQL("INSERT INTO service ( serviceID, serviceName ) "
                   + "VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO service ( serviceID, serviceName ) "
                   + "VALUES ( 2, 'TEA-READY' )");
        executeSQL("INSERT INTO service ( serviceID, serviceName ) "
                   + "VALUES ( 3, 'SODA-ICE-COLD' )");
        
        
//        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 1, '0.0.0.0', 1, 1 )");
        
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 2, '1.1.1.1', null, 1 )");
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 2, '1.1.1.1', null, 2 )");
        
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 2, '1.1.1.1', null, 3 )");
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 2, '1.1.1.1', null, 3 )");
        
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 3, '1.1.1.1', null, 3 )");
        executeSQL("INSERT INTO ifServices ( nodeID, ipAddr, ifIndex, serviceID ) VALUES ( 3, '1.1.1.1', -100, 3 )");


        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Unique index 'ifservices_nodeid_ipaddr_svc_unique' cannot be added to table 'ifservices' because 4 rows are not unique.  See the install guide for details on how to correct this problem.  You can use the following SQL to see which rows are not unique:\n"
                                    + "SELECT * FROM ifservices WHERE ( nodeID, ipAddr, serviceId ) IN ( SELECT nodeID, ipAddr, serviceId FROM ifservices GROUP BY nodeID, ipAddr, serviceId HAVING count(nodeID) > 1 ORDER BY nodeID, ipAddr, serviceId ) ORDER BY nodeID, ipAddr, serviceId"));
        try {
            m_installer.checkIndexUniqueness();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testBug1574() throws Exception {
        if (!isDBTestEnabled()) {
            return;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        
        addTableFromSQL("snmpinterface");
        executeSQL("drop index snmpinterface_nodeid_ifindex_unique_idx");
        executeSQL("create index snmpinterface_nodeid_ifindex_idx on snmpinterface(nodeID, snmpIfIndex)");
        m_installer.addIndexesForTable("snmpinterface");
        
        addTableFromSQL("ipinterface");
    }

    public void addTableFromSQL(String tableName) throws SQLException {
        String partialSQL = null;
        try {
            partialSQL = m_installer.getTableCreateFromSQL(tableName);
        } catch (Exception e) {
            fail("Could not get SQL for table '" + tableName + "'", e);
        }

        addTableWithSQL(tableName, partialSQL, true);
    }

    public void addTableFromSQLWithReplacements(String tableName,
            String[][] replacements) throws SQLException {
        addTableFromSQLWithReplacements(tableName, replacements, true);
    }

    public void addTableFromSQLWithReplacements(String tableName,
                String[][] replacements, boolean addTriggers) throws SQLException {
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
            partialSQL = partialSQL.replaceFirst(replacement[0],
                                                 replacement[1]);
        }

        addTableWithSQL(tableName, partialSQL, addTriggers);
    }

    
    private void addTableWithSQL(String table, String sql, boolean addTriggers)
    throws SQLException {
        executeSQL("CREATE TABLE " + table + " ( " + sql + " )");
        
        if (!addTriggers) {
            return;
        }
        
        m_installer.addIndexesForTable(table);
        m_installer.addTriggersForTable(table);
    }

    public boolean containsUnescapedParens(String str) {
        Pattern p = Pattern.compile("[^\\\\]\\(");
        Matcher m = p.matcher(str);
        return m.find();
    }
    

    public void assertStoredProcedureForTriggerExists(Trigger trigger)
        throws Exception {

        assertTrue("Function '" + trigger.getStoredProcedure()
                   + "' does not exist",
                   m_installer.functionExists(trigger.getStoredProcedure(),
                                              "", "trigger"));
    }
    
    public void assertTriggerExists(Trigger trigger)
        throws Exception {

        assertTrue("Trigger '" + trigger.getName()
                   + "' does not exist on table '"
                   + trigger.getTable() + "' to execute '"
                   + trigger.getStoredProcedure() + "' function",
                   trigger.isOnDatabase(m_installer.m_dbconnection));
    }

    public void verifyTriggers(boolean onlyStoredProcedures) throws Exception {
        Trigger[] triggers = new Trigger[] {
                new Trigger("setIfServiceKeysOnInsertTrigger",
                            "outages",
                            "setIfServiceKeysOnInsert", "NO SQL"),
                new Trigger("setIfServiceKeysOnUpdateTrigger",
                            "outages",
                            "setIfServiceKeysOnUpdate", "NO SQL"),
                new Trigger("setIpInterfaceKeysOnInsertTrigger",
                            "ifServices",
                            "setIpInterfaceKeysOnInsert", "NO SQL"),
                new Trigger("setIpInterfaceKeysOnUpdateTrigger",
                            "ifServices",
                            "setIpInterfaceKeysOnUpdate", "NO SQL"),
                new Trigger("setSnmpInterfaceKeysOnInsertTrigger",
                            "ipInterface",
                            "setSnmpInterfaceKeysOnInsert", "NO SQL"),
                new Trigger("setSnmpInterfaceKeysOnUpdateTrigger",
                            "ipInterface",
                            "setSnmpInterfaceKeysOnUpdate", "NO SQL")
        };
        
        for (Trigger trigger : triggers) {
            assertStoredProcedureForTriggerExists(trigger);
            if (!onlyStoredProcedures) {
                assertTriggerExists(trigger);
            }
        }
    }


    public boolean setUpForTriggerTest() throws Exception {
        if (!isDBTestEnabled()) {
            return false;
        }

        m_installer.createSequences();
        m_installer.updatePlPgsql();
        m_installer.addStoredProcedures();
        
        m_installer.createTables();

        return true;
    }
}