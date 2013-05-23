/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.db.install.BackupTablesFoundException;
import org.opennms.core.db.install.Column;
import org.opennms.core.db.install.Constraint;
import org.opennms.core.db.install.InstallerDb;
import org.opennms.core.db.install.Table;
import org.opennms.core.db.install.Trigger;
import org.opennms.core.test.db.TemporaryDatabaseTestCase;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.util.StringUtils;

public class InstallerDbTest extends TemporaryDatabaseTestCase {
    private static final String s_constraint = "fk_nodeid6";

    private InstallerDb m_installerDb;
    private Connection m_connection;

    private ByteArrayOutputStream m_outputStream;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        if (!isEnabled()) {
            return;
        }
        
        m_installerDb = new InstallerDb();

        resetOutputStream();
        getInstallerDb().setDatabaseName(getTestDatabase());
        m_installerDb.setPostgresOpennmsUser("opennms");

        getInstallerDb().setCreateSqlLocation("../../../opennms-base-assembly/src/main/filtered/etc/create.sql");

        getInstallerDb().setStoredProcedureDirectory("../../../opennms-base-assembly/src/main/filtered/etc");

        getInstallerDb().setDebug(true);

        getInstallerDb().readTables();
        
        getInstallerDb().setDataSource(getDataSource());
        
        getInstallerDb().addColumnReplacements();

        m_connection = getInstallerDb().getDataSource().getConnection();
    }

    @Override
    public void tearDown() throws Exception {
        if (isEnabled()) {
            m_installerDb.closeColumnReplacements();
            m_connection.close();
            getInstallerDb().closeConnection();
        }
        super.tearDown();
    }

    // XXX this should be an integration test
    public void testParseSQLTables() throws Exception {
        for (String table : getInstallerDb().getTableNames()) {
            getInstallerDb().getTableFromSQL(table);
        }
    }

    // XXX this should be an integration test
    public void testCreateSequences() throws Exception {
        getInstallerDb().createSequences();
    }

    // XXX this should be an integration test
    public void testCreateTables() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();
    }

    // XXX this should be an integration test
    public void testCreateTablesTwice() throws Exception {
        // First pass.
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        /*
         * Second pass. We don't care about the output from this, so we clear
         * the ByteArrayOutputStream after we call createSequences(). It's
         * important to test the sequence part, and do it first, because the
         * tables depend on sequences for their ID column.
         */
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        /*
         * Create a new ByteArrayOutputStream so we can look for UPTODATE for
         * every table
         */
        resetOutputStream();
        getInstallerDb().createTables();

        assertNoTablesHaveChanged();
    }
    
    public void testInsertCriteria() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        getInstallerDb().insertData();
        
        // try to insert twice
        getInstallerDb().insertData();


    }

    public void testUpgradeRevision3952ToCurrent() throws Exception {
        String newCreate = getInstallerDb().getCreateSqlLocation();

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        getInstallerDb().setCreateSqlLocation(sql.getFile());
        getInstallerDb().readTables();

        // First pass.
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        getInstallerDb().setCreateSqlLocation(newCreate);
        getInstallerDb().readTables();

        // Second pass.
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

    }
    
    public void testUpgradeRevision3952ToCurrentWithData() throws Exception {
        String newCreate = getInstallerDb().getCreateSqlLocation();

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        getInstallerDb().setCreateSqlLocation(sql.getFile());
        getInstallerDb().readTables();

        // First pass.
        getInstallerDb().createSequences();
        getInstallerDb().getTriggerDao().reset();
        getInstallerDb().createTables();
                
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
        executeSQL("INSERT INTO events (eventID, eventUei, eventTime, eventSource, eventDpName, eventCreateTime, eventSeverity, eventLog, eventDisplay) "
                + "VALUES ( nextval('eventsNxtId'), 'uei.opennms.org/foo', now(), 'somewhere', 'rainbow', now(), 0, 'Y', 'Y')");
        
        int eventId = jdbcTemplate.queryForInt("select eventId from events");
        Date eventTime = jdbcTemplate.queryForObject("select eventTime from events where eventId = ?", Date.class, eventId);

        getInstallerDb().setCreateSqlLocation(newCreate);
        getInstallerDb().readTables();

        // Second pass.
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        Date newEventTime = jdbcTemplate.queryForObject("select eventTime from events where eventId = ?", Date.class, eventId);
        String localFailureMessage = "time for eventId " + eventId + " does not match between old and new (in local time zone): " + eventTime + " (" + new Date(eventTime.getTime()) + ") -> " + newEventTime + " (" + new Date(newEventTime.getTime())+ ")";
        assertEquals(localFailureMessage, eventTime, newEventTime);

        jdbcTemplate.getJdbcOperations().execute("SET TIME ZONE 'UTC'");
        
        Date utcEventTime = jdbcTemplate.queryForObject("select eventTime from events where eventId = ?", Date.class, eventId);
        String utcFailureMessage = "time for eventId " + eventId + " does not match between old and new (in UTC): " + eventTime + " (" + new Date(eventTime.getTime()) + ") -> " + utcEventTime + " (" + new Date(utcEventTime.getTime())+ ")";
        assertEquals(utcFailureMessage, eventTime, utcEventTime);
    }

    public void testUpgradeColumnAddNotNullConstraint() throws Exception {
        Table oldTable = new Table();
        oldTable.setName("node");
        oldTable.setConstraints(new LinkedList<Constraint>());
        List<Column> oldColumns = new LinkedList<Column>();
        Column oldColumn = new Column();
        oldColumn.setName("nodeId");
        oldColumn.setType("INTEGER");
        oldColumn.setSize(4);
        oldColumns.add(oldColumn);
        oldTable.setColumns(oldColumns);
        oldTable.setNotNullOnPrimaryKeyColumns();
        
        Table newTable = new Table();
        newTable.setName("node");
        newTable.setConstraints(new LinkedList<Constraint>());
        List<Column> newColumns = new LinkedList<Column>();
        Column newColumn = new Column();
        newColumn.setName("nodeId");
        newColumn.setType("INTEGER");
        newColumn.setSize(4);
        newColumn.setNotNull(true);
        newColumns.add(newColumn);
        newTable.setColumns(newColumns);
        newTable.setNotNullOnPrimaryKeyColumns();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Column nodeid in new table has NOT NULL constraint, however this column did not have the NOT NULL constraint before and there is no change replacement for this column"));
        try { 
            getInstallerDb().changeTable("node", oldTable, newTable);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    /**
     * Call Installer.checkOldTables, which should *not* throw an exception
     * because we have not created a table matching "_old_".
     */
    public void testBug1006NoOldTables() {
        ThrowableAnticipator ta = new ThrowableAnticipator();

        try {
            getInstallerDb().checkOldTables();
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
        // final String errorSubstring = "One or more backup tables from a
        // previous install still exists";

        String table = "testBug1006_old_" + System.currentTimeMillis();

        Statement st = m_connection.createStatement();
        st.execute("CREATE TABLE " + table + " ( foo integer )");
        st.close();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        LinkedList<String> l = new LinkedList<String>();
        l.add(table);
        ta.anticipate(new BackupTablesFoundException(l));

        try {
            getInstallerDb().checkOldTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void setupBug931(boolean breakConstraint, boolean dropForeignTable)
        throws Exception {
        setupBug931(breakConstraint, dropForeignTable, true);
    }

    public void setupBug931(boolean breakConstraint, boolean dropForeignTable, boolean useOwnCreateSql)
    throws Exception {
        final String[] commands = { "CREATE TABLE events ( nodeID integer )",
                "CREATE TABLE node ( nodeID integer )",
                "INSERT INTO events ( nodeID ) VALUES ( 1 )",
                "INSERT INTO node ( nodeID ) VALUES ( 1 )",
                "INSERT INTO events ( nodeID ) VALUES ( 2 )",
                "INSERT INTO node ( nodeID ) VALUES ( 2 )" };
        final String newSql = "CREATE TABLE events ( nodeID integer, "
            + "constraint fk_nodeID6 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE );\n";

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
        
        if (useOwnCreateSql) {
            getInstallerDb().readTables(new StringReader(newSql));
        }
    }

    public void testBug931ConstraintsOkayTwoTables() throws Exception {
        doTestBug931(false, 0, false, false);
    }

    public void testBug931ConstraintsOkayOneTable() throws Exception {
        doTestBug931(true, 0, false, false);
    }

    public void testBug931ConstraintsBadTwoTables() throws Exception {
        doTestBug931(false, 1, false, false);
    }

    public void testBug931ConstraintsBadOneTable() throws Exception {
        doTestBug931(true, 2, false, false);
    }

    public void testConstraintsFixedNullTwoTables() throws Exception {
        doTestBug931(false, 0, true, false);
    }

    public void testConstraintsFixedNullOneTable() throws Exception {
        doTestBug931(true, 0, true, false);
    }

    public void testConstraintsFixedDelTwoTables() throws Exception {
        doTestBug931(false, 0, true, true);
    }

    public void testConstraintsFixedDelOneTable() throws Exception {
        doTestBug931(true, 0, true, true);
    }

    public void testBogusConstraintName() throws Exception {
        String constraint = "bogus_test_" + System.currentTimeMillis();
        doTestBogusConstraint(constraint, "Did not find constraint "
                + constraint + " in the database.");
    }

    public void testBogusConstraintTable() throws Exception {
        String constraint = "fk_nodeid1";
        doTestBogusConstraint(
                              constraint,
                              "Constraint "
                                      + constraint
                                      + " is on table "
                                      + "ipinterface, but table does not exist (so fixing this constraint does nothing).");
    }

    public void testBogusConstraintColumn() throws Exception {
        String constraint = "fk_dpname";
        doTestBogusConstraint(constraint, "Constraint " + constraint
                + " constrains column "
                + "dpname of table node, but column does not "
                + "exist (so fixing this constraint does nothing).");
    }

    public void testConstraintAfterConstrainedColumn() throws Exception {
        String s_create_sql = "            create table distPoller (\n"
                + "                    dpName            varchar(12),\n"
                + "                                constraint pk_dpName primary key (dpName),\n"
                + "                    dpIP            text not null,\n"
                + "                    dpComment        varchar(256),\n"
                + "                    dpDiscLimit        numeric(5,2),\n"
                + "                    dpLastNodePull        timestamp without time zone,\n"
                + "                    dpLastEventPull        timestamp without time zone,\n"
                + "                    dpLastPackagePush    timestamp without time zone,\n"
                + "                    dpAdminState         integer,\n"
                + "                    dpRunState        integer );\n";

        getInstallerDb().readTables(new StringReader(s_create_sql));
        getInstallerDb().getTableColumnsFromSQL("distpoller");
    }

    public void testConstraintAtEndOfTable() throws Exception {
        String s_create_sql = "            create table distPoller (\n"
                + "                    dpName            varchar(12),\n"
                + "                    dpIP            text not null,\n"
                + "                    dpComment        varchar(256),\n"
                + "                    dpDiscLimit        numeric(5,2),\n"
                + "                    dpLastNodePull        timestamp without time zone,\n"
                + "                    dpLastEventPull        timestamp without time zone,\n"
                + "                    dpLastPackagePush    timestamp without time zone,\n"
                + "                    dpAdminState         integer,\n"
                + "                    dpRunState        integer,\n"
                + "                                constraint pk_dpName primary key (dpName) );\n";

        getInstallerDb().readTables(new StringReader(s_create_sql));
        getInstallerDb().getTableColumnsFromSQL("distpoller");
    }

    public void testConstraintIpInterfaceSnmpInterfaceValidData() throws Exception {
        String newCreate = getInstallerDb().getCreateSqlLocation();

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        getInstallerDb().setCreateSqlLocation(sql.getFile());
        getInstallerDb().readTables();

        // First pass.
        getInstallerDb().createSequences();
        getInstallerDb().getTriggerDao().reset();
        //getInstallerDb().updatePlPgsql();
        //getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();
        
        // Data
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.6', -100 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.5', null )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.6', -100 )");

        getInstallerDb().setCreateSqlLocation(newCreate);
        getInstallerDb().readTables();

        // Second pass.
        getInstallerDb().checkConstraints();
        /*
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();
        */
    }
    public void testConstraintIpInterfaceSnmpInterfaceInvalidData() throws Exception {
        String newCreate = getInstallerDb().getCreateSqlLocation();

        URL sql = getClass().getResource("/create.sql-revision-3952");
        assertNotNull("Could not find create.sql", sql);
        getInstallerDb().setCreateSqlLocation(sql.getFile());
        getInstallerDb().readTables();

        // First pass.
        getInstallerDb().createSequences();
        getInstallerDb().getTriggerDao().reset();
        //getInstallerDb().updatePlPgsql();
        //getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();
        
        // Data
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeId, ipAddr, snmpIfIndex) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.4', 1 )");
        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.5', null )");
//        executeSQL("INSERT INTO ipInterface ( nodeId, ipAddr, ifIndex ) VALUES ( 1, '1.2.3.6', -100 )");

        getInstallerDb().setCreateSqlLocation(newCreate);
        getInstallerDb().readTables();

        // Second pass.
        getInstallerDb().checkConstraints();
        /*
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();
        */
    }
    
    public void testConstraintOnBogusColumn() throws Exception {
        String s_create_sql = "            create table distPoller (\n"
                + "                    dpName            varchar(12),\n"
                + "                    dpIP            text not null,\n"
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

        getInstallerDb().readTables(new StringReader(s_create_sql));
        try {
            getInstallerDb().getTableColumnsFromSQL("distpoller");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }


    public void doTestBogusConstraint(String constraint,
            String exceptionMessage) throws Exception {
        //m_installer.m_fix_constraint_name = constraint;

        setupBug931(false, false, false);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(exceptionMessage));

        try {
            getInstallerDb().fixConstraint(constraint, false);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void doTestBug931(boolean dropForeignTable, int badRows,
            boolean fixConstraint, boolean removeRows) throws Exception {
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
            getInstallerDb().fixConstraint(s_constraint, removeRows);
        }

        ThrowableAnticipator ta = new ThrowableAnticipator();
        if (badRows > 0) {
            ta.anticipate(new Exception(errorSubstring));
        }

        try {
            getInstallerDb().checkConstraints();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testParseConstraintWithOnUpdateCascade() throws Exception {
        /*
         * Make sure that every table, column, and key ID listed below has
         * at least one upper case character so that we can test lower casing
         */
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

        getInstallerDb().readTables(new StringReader(createSQL));
        getInstallerDb().getTableFromSQL("a");
        getInstallerDb().getTableFromSQL("b");
    }

    public void testGetFromDbConstraintWithOnUpdateCascade() throws Exception {
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

        List<Column> columns = getInstallerDb().getColumnsFromDB("b");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = getInstallerDb().getConstraintsFromDB("b");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals("constraint zero toString()",
                     "constraint fk_a foreign key (b1) references a (a1) "
                     + "on update cascade",
                     constraints.get(0).toString());
    }

    public void testParseConstraintWithOnDeleteRestrict() throws Exception {
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on delete restrict\n"
                + ");\n";

        getInstallerDb().readTables(new StringReader(createSQL));
        getInstallerDb().getTableFromSQL("a");
        getInstallerDb().getTableFromSQL("b");
    }

    public void testGetFromDbConstraintWithOnDeleteRestrict() throws Exception {
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on delete restrict\n"
                + ");\n";

        executeSQL(createSQL);

        List<Column> columns = getInstallerDb().getColumnsFromDB("b");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = getInstallerDb().getConstraintsFromDB("b");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals("constraint zero toString()",
                     "constraint fk_a foreign key (b1) references a (a1) "
                     + "on delete restrict",
                     constraints.get(0).toString());
    }

    public void testParseConstraintWithOnDeleteSetDefault() throws Exception {
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on delete set default\n"
                + ");\n";

        getInstallerDb().readTables(new StringReader(createSQL));
        getInstallerDb().getTableFromSQL("a");
        getInstallerDb().getTableFromSQL("b");
    }

    public void testGetFromDbConstraintWithOnDeleteSetDefault() throws Exception {
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on delete set default\n"
                + ");\n";

        executeSQL(createSQL);

        List<Column> columns = getInstallerDb().getColumnsFromDB("b");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = getInstallerDb().getConstraintsFromDB("b");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals("constraint zero toString()",
                     "constraint fk_a foreign key (b1) references a (a1) "
                     + "on delete set default",
                     constraints.get(0).toString());
    }


    public void testParseConstraintWithOnDeleteSetNull() throws Exception {
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on delete set null\n"
                + ");\n";

        getInstallerDb().readTables(new StringReader(createSQL));
        getInstallerDb().getTableFromSQL("a");
        getInstallerDb().getTableFromSQL("b");
    }

    public void testGetFromDbConstraintWithOnDeleteSetNull() throws Exception {
        final String createSQL =
            "create table a (\n"
                + "    a1           integer,\n"
                + "    constraint pk_a primary key (a1)\n"
                + ");\n"
                + "create table b (\n"
                + "    b1           integer,\n"
                + "    constraint fk_a foreign key (b1) references a (a1) "
                        + "on delete set null\n"
                + ");\n";

        executeSQL(createSQL);

        List<Column> columns = getInstallerDb().getColumnsFromDB("b");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = getInstallerDb().getConstraintsFromDB("b");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals("constraint zero toString()",
                     "constraint fk_a foreign key (b1) references a (a1) "
                     + "on delete set null",
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

        getInstallerDb().readTables(new StringReader(createSQL));
        Table table = getInstallerDb().getTableFromSQL("element");

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
        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);
    }

    public void testInsertMultipleColumnsGetFromDB() throws Exception {
        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        getInstallerDb().getTableColumnsFromDB("qrtz_job_details");
    }

    public void testInsertMultipleColumnsGetFromDBCompare() throws Exception {
        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        Table table = getInstallerDb().getTableFromDB("qrtz_job_details");
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
        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        List<Column> columns = getInstallerDb().getColumnsFromDB("qrtz_job_details");
        assertNotNull("column list not null", columns);
        assertEquals("column list size", 2, columns.size());
        assertEquals("column zero toString()",
                     "job_name character varying(80) NOT NULL",
                     columns.get(0).toString());
        assertEquals("column one toString()",
                     "job_group character varying(80) NOT NULL",
                     columns.get(1).toString());
    }
    

    public void testGetColumnsFromDBWithDefaultIntegerConstant() throws Exception {
        String command = "CREATE TABLE alarms (\n"
                + "  x733ProbableCause INTEGER DEFAULT 17 NOT NULL\n"
                + ")";
        executeSQL(command);

        List<Column> columns = getInstallerDb().getColumnsFromDB("alarms");
        assertNotNull("column list not null", columns);
        assertEquals("column list size", 1, columns.size());
        assertEquals("column zero toString()",
                     "x733probablecause integer(4) DEFAULT 17 NOT NULL",
                     columns.get(0).toString());
    }

    public void testGetColumnsFromDBWithDefaultTextConstant() throws Exception {
        String command = "CREATE TABLE alarms (\n"
                + "  someColumn VARCHAR(20) DEFAULT 'HeLlO!' NOT NULL\n"
                + ")";
        executeSQL(command);

        List<Column> columns = getInstallerDb().getColumnsFromDB("alarms");
        assertNotNull("column list not null", columns);
        assertEquals("column list size", 1, columns.size());
        assertEquals("column zero toString()",
                     "somecolumn character varying(20) DEFAULT 'HeLlO!' NOT NULL",
                     columns.get(0).toString());
    }

    public void testGetColumnsFromDBWithDefaultNextVal() throws Exception {
        executeSQL("create sequence opennmsNxtId minvalue 1");
        String command = "CREATE TABLE alarms (\n"
                + "  x733ProbableCause INTEGER DEFAULT nextval('opennmsNxtId') NOT NULL\n"
                + ")";
        executeSQL(command);

        List<Column> columns = getInstallerDb().getColumnsFromDB("alarms");
        assertNotNull("column list not null", columns);
        assertEquals("column list size", 1, columns.size());
        assertEquals("column zero toString()",
                     "x733probablecause integer(4) DEFAULT nextval('opennmsnxtid') NOT NULL",
                     columns.get(0).toString());
    }


    public void testGetConstraintsFromDB() throws Exception {
        String command = "CREATE TABLE qrtz_job_details (\n"
                + "  JOB_NAME  VARCHAR(80) NOT NULL,\n"
                + "  JOB_GROUP VARCHAR(80) NOT NULL,\n"
                + "  CONSTRAINT pk_qrtz_job_details PRIMARY KEY (JOB_NAME,JOB_GROUP)\n"
                + ")";
        executeSQL(command);

        List<Column> columns = getInstallerDb().getColumnsFromDB("qrtz_job_details");
        assertNotNull("column list not null", columns);
        List<Constraint> constraints = getInstallerDb().getConstraintsFromDB("qrtz_job_details");
        assertNotNull("constraint list not null", constraints);
        assertEquals("constraint list size", 1, constraints.size());
        assertEquals(
                     "constraint zero toString()",
                     "constraint pk_qrtz_job_details primary key (job_name, job_group)",
                     constraints.get(0).toString());
    }

    public void testSetEventSourceOnUpgrade() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements("events",
                                        new String[][] { new String[] {
                                                "eventSource\\s+varchar\\(\\d+\\) not null,",
                                                "" } }, true);

        executeSQL("INSERT INTO events (eventID, eventUei, eventTime, eventDpName, eventCreateTime, eventSeverity, eventLog, eventDisplay) "
                + "VALUES ( 1, 'uei.opennms.org/eatmyshorts', now(), 'Duh', now(), 1, 'n', 'n' )");

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT eventsource from events");
        int count = 0;
        while (rs.next()) {
            assertEquals("expected events eventsource", "OpenNMS.Eventd", rs.getString(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }

    public void testSetOutageIdOnUpgrade() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT outageid from outages");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected outages outageid", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }

    public void testSomethingUnknown() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
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
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        executeSQL("INSERT INTO usersNotified (userID) VALUES ('DJ... it is always his fault')");

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from usersnotified");
        int count = 0;
        for (int expected = 1; rs.next(); expected++) {
            assertEquals("expected usersNotified id", expected, rs.getInt(1));
            count++;
        }
        assertEquals("expected column count", 1, count);

    }

    public void testSetSnmpInterfaceIdOnUpgrade() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES (1, 1)");

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
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
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id from snmpInterface");

        assertTrue("Could not ResultSet.next() to first result entry",
                   rs.next());
        rs.getInt(1);
        assertFalse("first result should not be null, but was null",
                    rs.wasNull());

        assertFalse("Too many entries", rs.next());
    }

    public void testCatchSnmpInterfaceHasNullNodeIdValueOnUpgrade()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( null, 1 )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(
                                    "Error changing table 'snmpinterface'.  Nested exception: The 'nodeId' column in the 'snmpInterface' table should never be null, but the entry for this row does have a null 'nodeId' column.  It needs to be removed or udpated to reflect a valid 'nodeId' value."));
        try {
            getInstallerDb().createTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testCatchIpInterfaceNullIpAddrColumnOnUpgrade()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] { new String[] {
                                                "(?i)ipAddr\\s+text\\s+not null,",
                                                "ipAddr text," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', null )");

        getInstallerDb().createTables();
    }

    public void testCatchIpInterfaceHasNullIpAddrValueOnUpgrade()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] { new String[] {
                                                "(?i)ipAddr\\s+text\\s+not null,",
                                                "ipAddr text," } });

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1 )");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, null, 1 )");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception(
                                    "Error changing table 'ipinterface'.  Nested exception: The 'ipAddr' column in the 'ipInterface' table should never be null, but the entry for this row does have a null 'ipAddr' column.  It needs to be removed or udpated to reflect a valid 'ipAddr' value."));
        try {
            getInstallerDb().createTables();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    public void testAssetsIdOnUpgrade() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
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
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        verifyTriggers(false);

        getInstallerDb().createTables();

        verifyTriggers(false);
    }

    public void testTriggersAfterUpdateWithChange() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();


        /*
         * getInstallerDb().createTables(); executeSQL("drop table outages");
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
        

        /*
         *  No snmpInterfaceID column, and I need to kill an index that
         *  includes that column, as well.
         */
        getInstallerDb().getIndexDao().remove("ipinterface_snmpInterfaceId_idx");
        addTableFromSQLWithReplacements("ipinterface",
                                        new String[][] {
                                                new String[] {
                                                        "(?i)snmpInterfaceId\\s+integer,",
                                                        "" },
                                                new String[] {
                                                        "(?i)CONSTRAINT snmpinterface_fkey2 FOREIGN KEY \\(snmpInterfaceId\\) REFERENCES snmpInterface \\(id\\) ON DELETE SET NULL,",
                                                        "" } });

        addTableFromSQL("service");
        addTableFromSQL("ifServices");
        addTableFromSQL("events");
        addTableFromSQL("outages");

        verifyTriggers(false);

        getInstallerDb().createTables();

        verifyTriggers(false);
    }

    public void testIpInterfaceForeignKeySnmpInterfaceIdOnUpgrade()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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
                                                        "(?i)CONSTRAINT snmpinterface_fkey2 FOREIGN KEY \\(snmpInterfaceId\\) REFERENCES snmpInterface \\(id\\) ON DELETE SET NULL,",
                                                        "" } }, false);

        addTableFromSQL("service");
        addTableFromSQL("ifServices");
        addTableFromSQL("events");
        addTableFromSQL("outages");

        executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface (nodeId, snmpIfIndex) VALUES ( 1, 1)");
        executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 1, '1.2.3.4', 1 )");

        //      executeSQL("INSERT INTO node (nodeId, nodeCreateTime) VALUES ( 2, now() )");
  //      executeSQL("INSERT INTO ipInterface (nodeId, ipAddr, ifIndex) VALUES ( 2, '1.2.3.9', null )");

        getInstallerDb().createTables();

        verifyTriggers(false);

        Statement st;
        ResultSet rs;

        st = m_connection.createStatement();
        rs = st.executeQuery("SELECT id from snmpInterface ORDER BY nodeId");

        assertTrue("Could not ResultSet.next() to first result entry",
                   rs.next());
        rs.getInt(1);
        assertFalse("first result should not be null, but was null",
                    rs.wasNull());
        // I don't care about the value
        // assertEquals("smmpInterface id", 1, got);

        assertFalse("Too many entries", rs.next());

        st = m_connection.createStatement();
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
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
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
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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
        executeSQL("INSERT INTO snmpInterface (id, nodeId, snmpIfIndex) VALUES ( 1, 1, 1 )");
        executeSQL("INSERT INTO ipInterface (id, nodeId, ipAddr, ifIndex, snmpInterfaceId ) VALUES ( 1, 1, '1.2.3.4', 1, 1 )");
        executeSQL("INSERT INTO service (serviceID, serviceName) VALUES ( 1, 'COFFEE-READY' )");
        executeSQL("INSERT INTO ifServices (nodeID, ipAddr, ifIndex, serviceID, ipInterfaceId) VALUES ( 1, '1.2.3.4', 1, 1, 1)");
        executeSQL("INSERT INTO outages (outageId, nodeId, ipAddr, ifLostService, serviceID ) "
                + "VALUES ( nextval('outageNxtId'), 1, '1.2.3.4', now(), 1 )");

        getInstallerDb().createTables();

        Statement st = m_connection.createStatement();
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
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        getInstallerDb().createTables();

        verifyTriggers(true);
    }

    public void testAddStoredProceduresTwice() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        
        //getInstallerDb().createTables();
        
        getInstallerDb().addStoredProcedures();

        verifyTriggers(true);
    }

    public void testSnmpInterfaceNodeIdColumnConvertToNotNull()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        getInstallerDb().createTables();
    }

    public void testSnmpInterfaceSnmpIfIndexColumnConvertToNotNull()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQLWithReplacements(
                                        "snmpinterface",
                                        new String[][] { new String[] {
                                                "(?i)snmpIfIndex\\s+integer not null,",
                                                "snmpIfIndex integer," } });

        getInstallerDb().createTables();
    }

    public void testIpInterfaceNodeIdColumnConvertToNotNull()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQLWithReplacements(
                                        "ipinterface",
                                        new String[][] { new String[] {
                                                "(?i)nodeID\\s+integer not null,",
                                                "nodeId integer," } });

        getInstallerDb().createTables();
    }

    public void testIfServicesNodeIdColumnConvertToNotNull() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();
    }

    public void testIfServicesIpAddrColumnConvertToNotNull() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        addTableFromSQL("ipinterface");
        addTableFromSQL("service");
        addTableFromSQLWithReplacements(
                                        "ifservices",
                                        new String[][] { new String[] {
                                                "(?i)ipAddr\\s+text\\s+not null,",
                                                "ipAddr text," } });

        getInstallerDb().createTables();
    }

    public void testIfServicesServiceIdColumnConvertToNotNull()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();
    }

    public void testOutagesNodeIdColumnConvertToNotNull() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();
    }

    public void testOutagesServiceIdColumnConvertToNotNull() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();
    }

    public void testOutagesIfServiceIdColumnConvertToNotNull()
            throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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

        getInstallerDb().createTables();
    }
    
    public void testSnmpInterfaceNonUniqueKeys() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        addTableFromSQL("snmpinterface");
        executeSQL("drop index snmpinterface_nodeid_ifindex_unique_idx");
        
        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 1, now() )");

        // One test with identical entries
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        
        // One with different different IPs
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Unique index "
                                    + "'snmpinterface_nodeid_ifindex_unique_idx' "
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
            getInstallerDb().checkIndexUniqueness();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testIpInterfaceNonUniqueKeys() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 2 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 3, 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 3, 2 )");
        
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
            getInstallerDb().checkIndexUniqueness();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();

    }
    
    public void testIfServicesNonUniqueKeys() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

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
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 2 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 3, 1 )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 3, 2 )");
        
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
            getInstallerDb().checkIndexUniqueness();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testCheckIndexUniquenessWithTableButMissingColumnBug2325() throws Exception {
        addTableFromSQL("distpoller");
        
        getInstallerDb().getIndexDao().remove("node_foreign_unique_idx");
        addTableFromSQLWithReplacements("node", new String[][] { new String[] { "foreignSource\\s+varchar\\(\\d+\\),", "" } });
        
        getInstallerDb().readTables();
        
        getInstallerDb().checkIndexUniqueness();
    }
    
    public void testBug1574() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        
        addTableFromSQL("snmpinterface");
        executeSQL("drop index snmpinterface_nodeid_ifindex_unique_idx");
        executeSQL("create index snmpinterface_nodeid_ifindex_idx on snmpinterface(nodeID, snmpIfIndex)");
        getInstallerDb().addIndexesForTable("snmpinterface");
        
        addTableFromSQL("ipinterface");
    }
    
    public void testUpgradeExistingDoNotAddColumnBug1685() throws Exception {
        getInstallerDb().createSequences();
        getInstallerDb().updatePlPgsql();
        getInstallerDb().addStoredProcedures();

        addTableFromSQL("distpoller");
        addTableFromSQL("node");
        
        // Add snmpinterface table with an arbitrary change so it gets upgraded
        addTableFromSQLWithReplacements("snmpinterface", new String[][] {
                new String[] {
                        "snmpIfAlias\\s+varchar\\(\\d+\\),", ""
                } });

        executeSQL("INSERT INTO node ( nodeId, nodeCreateTime ) "
                   + "VALUES ( 1, now() )");
        executeSQL("INSERT INTO snmpInterface ( nodeID, snmpIfIndex ) "
                   + "VALUES ( 1, 1 )");
        
        int id = jdbcTemplate.queryForInt("SELECT id from snmpInterface");
        
        getInstallerDb().createTables();

        int id2 = jdbcTemplate.queryForInt("SELECT id from snmpInterface");

        assertEquals("id before upgrade should equal id after upgrade", id,
                     id2);
    }

    public void testUpgradeColumnToNotNullWithDefault() throws Exception {
        
        final String[] commands = { "CREATE TABLE alarms ( id integer, x733ProbableCause integer )",
                "INSERT INTO alarms ( id, x733ProbableCause ) VALUES ( 1, 1 )",
                "INSERT INTO alarms ( id, x733ProbableCause ) VALUES ( 2, NULL )" };
        final String newSql = "CREATE TABLE alarms ( id integer, x733ProbableCause integer DEFAULT 0 NOT NULL );\n";

        executeSQL(commands);

        getInstallerDb().readTables(new StringReader(newSql));
        getInstallerDb().createTables();
        
        assertEquals("x733ProbableCause for id = 1 should have its original value", 1, jdbcTemplate.queryForInt("SELECT x733ProbableCause FROM alarms WHERE id = 1"));
        assertEquals("x733ProbableCause for id = 2 should have the DEFAULT value", Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT x733ProbableCause FROM alarms WHERE id = 2", Integer.class));

    }
    
   public void testColumnNoChangeWithDefault() throws Exception {
        final String sql = "CREATE TABLE alarms ( id integer, x733ProbableCause integer DEFAULT 0 NOT NULL );\n";

        executeSQL(sql);

        getInstallerDb().readTables(new StringReader(sql));
        getInstallerDb().createTables();
        
        assertNoTablesHaveChanged();
    }

    public void testUpdateIplikePgSql() throws Throwable {
        getInstallerDb().updatePlPgsql();
        getInstallerDb().setPostgresIpLikeLocation(null); // Ensure that we don't try to load the C version
        getInstallerDb().updateIplike();
        getInstallerDb().closeConnection();
    }
    
    public void testCreateTableWithCheckConstraint() throws Throwable {
    	final String cname="setfilter_type_valid";
    	final String checkexpression="(((type >= 0) AND (type <= 2)))";
        final String sql = "create table setFilter ( id integer, type integer, " +
        		"constraint "+cname+" check "+checkexpression+");\n";
        getInstallerDb().readTables(new StringReader(sql));
    	Table table=getInstallerDb().getTableFromSQL("setFilter");
    	List<Constraint> constraints=table.getConstraints();
    	assertTrue(constraints.size()==1);
    	Constraint constraint=constraints.get(0);
    	assertTrue(cname.equals(constraint.getName()));
    	assertTrue(checkexpression.equals("("+constraint.getCheckExpression()+")"));
    }
    
    public void testUpgradeAddCheckConstraint() throws Exception {
       	final String cname="setfilter_type_valid";
    	final String checkexpression="(((type >= 0) AND (type <= 2)))";
        final String sql_start = "create table setFilter ( id integer, type integer);\n";
        executeSQL(sql_start);
        
        final String sql_upgrade = "create table setFilter ( id integer, type integer, " +
						"constraint "+cname+" check "+checkexpression+");\n";
        getInstallerDb().readTables(new StringReader(sql_upgrade));
        getInstallerDb().createTables();
        
        //Check created table
    	Table table=getInstallerDb().getTableFromDB("setFilter");
    	List<Constraint> constraints=table.getConstraints();
    	assertEquals(1, constraints.size());
    	Constraint constraint=constraints.get(0);
    	assertEquals(cname, constraint.getName());
        // postgresql8.2 has quotes in the resulting expression
        // postgresql8.3 has none... remove the quotes (if there are any) before comparing
        assertEquals(checkexpression.replaceAll("\"type\"", "type"), "("+constraint.getCheckExpression().replaceAll("\"type\"", "type")+")");
    }

    public void addTableFromSQL(String tableName) throws SQLException {
        String partialSQL = null;
        try {
            partialSQL = getInstallerDb().getTableCreateFromSQL(tableName);
        } catch (Throwable e) {
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
            partialSQL = getInstallerDb().getTableCreateFromSQL(tableName);
        } catch (Throwable e) {
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
        
        getInstallerDb().addIndexesForTable(table);
        getInstallerDb().addTriggersForTable(table);
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
                   getInstallerDb().functionExists(trigger.getStoredProcedure(),
                                              "", "trigger"));
    }
    
    public void assertTriggerExists(Trigger trigger)
        throws Exception {

        assertTrue("Trigger '" + trigger.getName()
                   + "' does not exist on table '"
                   + trigger.getTable() + "' to execute '"
                   + trigger.getStoredProcedure() + "' function",
                   trigger.isOnDatabase(m_connection));
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
    
    public InstallerDb getInstallerDb() {
        return m_installerDb;
    }
    
    public void resetOutputStream() {
        m_outputStream = new ByteArrayOutputStream();
        getInstallerDb().setOutputStream(new PrintStream(m_outputStream));
    }

    private void assertNoTablesHaveChanged() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(m_outputStream.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String line;
        List<String> unanticipatedOutput = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (line.matches("- creating tables\\.\\.\\.")) {
                continue;
            }
            if (line.matches("  - checking table \"\\S+\"\\.\\.\\. ")) {
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
            
            unanticipatedOutput.add(line);
        }
        
        if (unanticipatedOutput.size() > 0) {
            fail(unanticipatedOutput.size() + "unexpected line(s) output by createTables(): \n\t" + StringUtils.collectionToDelimitedString(unanticipatedOutput, "\n\t") + "\nAll output:\n" + m_outputStream);
        }
    }

}
