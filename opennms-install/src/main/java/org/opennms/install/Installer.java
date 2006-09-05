//
// // This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code
// and modified
// code that was published under the GNU General Public License. Copyrights
// for
// modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// The code in this file is Copyright (C) 2004 DJ Gregor.
//
// Based on install.pl which was Copyright (C) 1999-2001 Oculan Corp. All
// rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.ProcessExec;

/*
 * Big To-dos: - Fix all of the XXX items (some coding, some discussion) -
 * Change the Exceptions to something more reasonable - Do exception handling
 * where it makes sense (give users reasonable error messages for common
 * problems) - Add a friendly startup script? - Javadoc
 */

public class Installer {
    static final float POSTGRES_MIN_VERSION = 7.3f;

    static final String s_version = "$Id$";

    static final int s_fetch_size = 1024;

    String m_opennms_home = null;

    boolean m_update_database = false;

    boolean m_do_inserts = false;

    boolean m_skip_constraints = false;

    boolean m_update_iplike = false;

    boolean m_update_unicode = false;

    boolean m_install_webapp = false;

    boolean m_fix_constraint = false;

    boolean m_force = false;

    boolean m_debug = false;

    boolean m_ignore_notnull = false;

    boolean m_no_revert = false;

    String m_pg_driver = null;

    String m_pg_url = null;

    String m_pg_user = "postgres";

    String m_pg_pass = "";

    String m_pg_bindir = null;

    String m_user = null;

    String m_pass = null;

    String m_database = null;

    String m_sql_dir = null;

    String m_create_sql = null;

    String m_pg_iplike = null;

    String m_tomcat_conf = null;

    String m_webappdir = null;

    String m_install_servletdir = null;

    String m_fix_constraint_name = null;

    boolean m_fix_constraint_remove_rows = false;

    HashMap<String, String[]> m_seqmapping = null;

    LinkedList<String> m_tables = null;

    LinkedList<String> m_sequences = null;

    // LinkedList m_cfunctions = new LinkedList(); // Unused, not in
    // create.sql
    // LinkedList m_functions = new LinkedList(); // Unused, not in create.sql
    // LinkedList m_languages = new LinkedList(); // Unused, not in create.sql
    LinkedList<String> m_indexes = new LinkedList<String>();

    HashMap<String, List<String>> m_inserts = new HashMap<String, List<String>>();

    HashSet<String> m_drops = new HashSet<String>();

    HashSet<String> m_changed = new HashSet<String>();

    float m_pg_version;

    String m_cascade = " CASCADE";

    String m_sql;

    PrintStream m_out = System.out;

    Properties m_properties = null;

    Connection m_dbconnection;

    Map m_dbtypes = null;
    
    Map<String, ColumnChangeReplacement> m_columnReplacements = new HashMap<String, ColumnChangeReplacement>();

    String m_required_options = "At least one of -d, -i, -s, -U, -y, "
            + "-C, or -T is required.";

    protected TriggerDao m_triggerDao;
    
    protected IndexDao m_indexDao;
    
    public Installer() throws SQLException {
        // The DEFAULT value for these columns will take care of these primary keys
        m_columnReplacements.put("snmpinterface.id", new DoNotAddColumn());
        m_columnReplacements.put("ipinterface.id", new DoNotAddColumn());
        m_columnReplacements.put("ifservices.id", new DoNotAddColumn());
        m_columnReplacements.put("assets.id", new DoNotAddColumn());

        // Triggers will take care of these surrogate foreign keys
        m_columnReplacements.put("ipinterface.snmpinterfaceid",
                                 new DoNotAddColumn());
        m_columnReplacements.put("ifservices.ipinterfaceid",
                                 new DoNotAddColumn());
        m_columnReplacements.put("outages.ifserviceid", new DoNotAddColumn());
        
        m_columnReplacements.put("events.eventsource",
                                 new EventSourceReplacement());
        
        m_columnReplacements.put("outages.outageid",
                                 new AutoInteger(1));
        
        m_columnReplacements.put("snmpinterface.nodeid",
                                 new RowHasBogusData("snmpInterface",
                                                     "nodeId"));
        
        m_columnReplacements.put("snmpinterface.snmpifindex",
                                 new RowHasBogusData("snmpInterface",
                                                     "snmpIfIndex"));

        m_columnReplacements.put("ipinterface.nodeid",
                                 new RowHasBogusData("ipInterface", "nodeId"));

        m_columnReplacements.put("ipinterface.ipaddr",
                                 new RowHasBogusData("ipInterface", "ipAddr"));

        m_columnReplacements.put("ifservices.nodeid",
                                 new RowHasBogusData("ifservices", "nodeId"));

        m_columnReplacements.put("ifservices.ipaddr",
                                 new RowHasBogusData("ifservices", "ipaddr"));

        m_columnReplacements.put("ifservices.serviceid",
                                 new RowHasBogusData("ifservices",
                                                     "serviceId"));

        m_columnReplacements.put("outages.nodeid",
                                 new RowHasBogusData("outages", "nodeId"));
        
        m_columnReplacements.put("outages.serviceid",
                                 new RowHasBogusData("outages", "serviceId"));
        
        /*
         * This is totally bogus.  outages.svcregainedeventid is a foreign
         * key that points at events.eventid, and a fixed replacement of zero
         * will break, because there should never be an event with an ID of
         * zero.  I don't think it ever got executed before due to the
         * null replacement only being performed if a column was marked as
         * NOT NULL.
         */
        /*
        m_columnReplacements.put("outages.svcregainedeventid",
                                 new FixedIntegerReplacement(0));
                                 */
        
        // Disabled for the same reason as above
        /*
        m_columnReplacements.put("notifications.eventid",
                                 new FixedIntegerReplacement(0));
                                 */
        
        m_columnReplacements.put("usersnotified.id",
                                 new NextValReplacement("userNotifNxtId"));
        
    }
    
    public void install(String[] argv) throws Exception {
        printHeader();
        loadProperties();
        parseArguments(argv);

        if (!m_update_database && !m_do_inserts && !m_update_iplike
                && !m_update_unicode && m_tomcat_conf == null
                && !m_install_webapp && !m_fix_constraint) {
            throw new Exception("Nothing to do.\n" + m_required_options
                    + "\nUse '-h' for help.");
        }

        // Don't bother checking the Java version. Leave it up to runjava.
        // checkJava();
        // XXX Check Tomcat version?

        if (m_update_database || m_update_iplike || m_update_unicode
                || m_do_inserts || m_fix_constraint) {
            databaseConnect("template1");
            databaseCheckVersion();
            databaseCheckLanguage();
        }

        printDiagnostics();

        verifyFilesAndDirectories();

        if (m_install_webapp) {
            checkWebappOldOpennmsDir();
            checkServerXmlOldOpennmsContext();
        }

        if (m_update_database || m_fix_constraint) {
            readTables();
        }

        if (m_update_database) {
            // XXX Check and optionally modify pg_hba.conf

            if (!databaseUserExists()) {
                databaseAddUser();
            }
            if (!databaseDBExists()) {
                databaseAddDB();
            }
        }

        if (m_update_database || m_update_iplike || m_update_unicode
                || m_do_inserts || m_fix_constraint) {
            databaseDisconnect();

            databaseConnect(m_database);
        }

        if (m_fix_constraint) {
            fixConstraint();
        }

        if (m_update_database) {
            checkOldTables();
            if (!m_skip_constraints) {
                checkConstraints();
            }
            createSequences();

            // XXX should we be using createFunctions and createLanguages
            // instead?
            updatePlPgsql();

            // XXX should we be using createFunctions instead?
            addStoredProcedures();

            createTables();
            //createIndexes();
            // createFunctions(m_cfunctions); // Unused, not in create.sql
            // createLanguages(); // Unused, not in create.sql
            // createFunctions(m_functions); // Unused, not in create.sql

            fixData();
        }

        if (m_do_inserts) {
            insertData();
        }

        if (m_update_unicode) {
            checkUnicode();
        }

        if (m_install_webapp) {
            installWebApp();
        }

        if (m_tomcat_conf != null) {
            updateTomcatConf();
        }

        if (m_update_iplike) {
            updateIplike();
        }

        if (m_update_database || m_update_iplike || m_update_unicode
                || m_do_inserts) {
            databaseDisconnect();
        }

        if (m_update_database) {
            createConfiguredFile();
        }

        System.out.println();
        System.out.println("Installer completed successfully!");
    }

    public void createConfiguredFile() throws IOException {
        File f = new File(m_opennms_home + File.separator + "etc"
                + File.separator + "configured");
        f.createNewFile();
    }

    public void printHeader() {
        m_out.println("==============================================="
                + "===============================");
        m_out.println("OpenNMS Installer Version " + s_version);
        m_out.println("==============================================="
                + "===============================");
        m_out.println("");
        m_out.println("Configures PostgreSQL tables, users, and other "
                + "miscellaneous settings.");
        m_out.println("");
    }

    public void loadProperties() throws Exception {
        m_properties = new Properties();
        m_properties.load(Installer.class.getResourceAsStream("installer.properties"));

        /*
         * Do this if we want to merge our properties with the system
         * properties...
         */
        Properties sys = System.getProperties();
        m_properties.putAll(sys);

        m_opennms_home = fetchProperty("install.dir");
        m_database = fetchProperty("install.database.name");
        m_user = fetchProperty("install.database.user");
        m_pass = fetchProperty("install.database.password");
        m_pg_driver = fetchProperty("install.database.driver");
        m_pg_url = fetchProperty("install.database.url");
        m_pg_bindir = fetchProperty("install.database.bindir");
        m_sql_dir = fetchProperty("install.etc.dir");
        m_install_servletdir = fetchProperty("install.servlet.dir");

        String soext = fetchProperty("build.soext");
        String pg_iplike_dir = fetchProperty("install.postgresql.dir");

        m_pg_iplike = pg_iplike_dir + File.separator + "iplike." + soext;
        m_create_sql = m_sql_dir + File.separator + "create.sql";
    }

    public String fetchProperty(String property) throws Exception {
        String value;

        if ((value = m_properties.getProperty(property)) == null) {
            throw new Exception("property \"" + property + "\" not set "
                    + "from bundled installer.properties file");
        }

        return value;
    }

    public void parseArguments(String[] argv) throws Exception {
        LinkedList<String> args = new LinkedList<String>();

        for (int i = 0; i < argv.length; i++) {
            StringBuffer b = new StringBuffer(argv[i]);
            boolean is_arg = false;

            while (b.length() > 0 && b.charAt(0) == '-') {
                is_arg = true;
                b.deleteCharAt(0);
            }

            if (is_arg) {
                while (b.length() > 0) {
                    char c = b.charAt(0);
                    b.deleteCharAt(0);

                    switch (c) {
                    case 'h':
                        printHelp();
                        break;

                    case 'c':
                        m_force = true;
                        break;

                    case 'C':
                        i++;
                        m_fix_constraint = true;
                        m_fix_constraint_name = getNextArg(argv, i, 'C');
                        break;

                    case 'd':
                        m_update_database = true;
                        break;

                    case 'i':
                        m_do_inserts = true;
                        break;

                    case 'n':
                        m_skip_constraints = true;

                    case 'N':
                        m_ignore_notnull = true;
                        break;

                    case 'p':
                        i++;
                        m_pg_pass = getNextArg(argv, i, 'p');
                        break;

                    case 'R':
                        m_no_revert = true;
                        break;

                    case 's':
                        m_update_iplike = true;
                        break;

                    case 'T':
                        i++;
                        m_tomcat_conf = getNextArg(argv, i, 'T');
                        break;

                    case 'u':
                        i++;
                        m_pg_user = getNextArg(argv, i, 'u');
                        break;

                    case 'U':
                        m_update_unicode = true;
                        break;

                    case 'w':
                        i++;
                        m_webappdir = getNextArg(argv, i, 'w');
                        break;

                    case 'x':
                        m_debug = true;
                        break;

                    case 'X':
                        m_fix_constraint_remove_rows = true;
                        break;

                    case 'y':
                        m_install_webapp = true;
                        break;

                    default:
                        throw new Exception("unknown option '" + c + "'"
                                + ", use '-h' option for usage");
                    }
                }
            } else {
                args.add(argv[i]);
            }
        }

        if (args.size() != 0) {
            throw new Exception("too many command-line arguments specified");
        }
    }

    public String getNextArg(String[] argv, int i, char letter)
            throws Exception {
        if (i >= argv.length) {
            throw new Exception("no argument provided for '" + letter
                    + "' option");
        }
        if (argv[i].charAt(0) == '-') {
            throw new Exception("argument to '" + letter + "' option looks "
                    + "like another option (begins with a dash): \""
                    + argv[i] + "\"");
        }
        return argv[i];
    }

    public void printDiagnostics() {
        m_out.println("* using '" + m_user + "' as the PostgreSQL "
                + "user for OpenNMS");
        m_out.println("* using '" + m_pass + "' as the PostgreSQL "
                + "password for OpenNMS");
        m_out.println("* using '" + m_database + "' as the PostgreSQL "
                + "database name for OpenNMS");
    }

    public void readTables() throws Exception {
        readTables(new FileReader(m_create_sql));
    }

    public void readTables(Reader reader) throws Exception {
        BufferedReader r = new BufferedReader(reader);
        String line;

        m_tables = new LinkedList<String>();
        m_seqmapping = new HashMap<String, String[]>();
        m_sequences = new LinkedList<String>();
        m_indexDao = new IndexDao();

        LinkedList<String> sql_l = new LinkedList<String>();

        Pattern seqmappingPattern = Pattern.compile("\\s*--#\\s+install:\\s*"
                + "(\\S+)\\s+(\\S+)\\s+" + "(\\S+)\\s*.*");
        Pattern createPattern = Pattern.compile("(?i)\\s*create\\b.*");
        Pattern insertPattern = Pattern.compile("(?i)INSERT INTO "
                + "[\"']?([\\w_]+)[\"']?.*");
        Pattern dropPattern = Pattern.compile("(?i)DROP TABLE [\"']?"
                + "([\\w_]+)[\"']?.*");

        while ((line = r.readLine()) != null) {
            Matcher m;

            if (line.matches("\\s*") || line.matches("\\s*\\\\.*")) {
                continue;
            }

            m = seqmappingPattern.matcher(line);
            if (m.matches()) {
                String[] a = { m.group(2), m.group(3) };
                m_seqmapping.put(m.group(1), a);
                continue;
            }

            if (line.matches("--.*")) {
                continue;
            }

            if (createPattern.matcher(line).matches()) {
                m = Pattern.compile(
                                    "(?i)\\s*create\\s+((?:unique )?\\w+)"
                                            + "\\s+[\"']?(\\w+)[\"']?.*").matcher(
                                                                                  line);
                if (m.matches()) {
                    String type = m.group(1);
                    String name = m.group(2).replaceAll("^[\"']", "").replaceAll(
                                                                                 "[\"']$",
                                                                                 "");

                    if (type.toLowerCase().indexOf("table") != -1) {
                        m_tables.add(name);
                    } else if (type.toLowerCase().indexOf("sequence") != -1) {
                        m_sequences.add(name);
                        /*
                         * -- Not used, nothing in create.sql to get us here }
                         * else if (type.toLowerCase().indexOf("function") !=
                         * -1) { if (type.toLowerCase().indexOf("language
                         * 'c'") != -1) { m_cfunctions.add(name); } else {
                         * m_functions.add(name); } } else if
                         * (type.toLowerCase().indexOf("trusted") != -1) { m =
                         * Pattern.compile("(?i)\\s*create\\s+trutsed " +
                         * "procedural language\\s+[\"']?" +
                         * "(\\w+)[\"']?.*").matcher(line); if (!m.matches()) {
                         * throw new Exception("Could not match name and " +
                         * "type of the trusted " + "procedural language in
                         * this" + "line: " + line); }
                         * m_languages.add(m.group(1));
                         */
                    } else if (type.toLowerCase().matches(".*\\bindex\\b.*")) {
                        /*
                        m = Pattern.compile(
                                            "(?i)\\s*create\\s+(?:unique )?"
                                                    + "index\\s+[\"']?([\\w_]+)"
                                                    + "[\"']?.*").matcher(
                                                                          line);
                                                                          */
                        Index i = Index.findIndexInString(line);
                        if (i == null) {
                            throw new Exception("Could not match name and "
                                    + "type of the index " + "in this"
                                    + "line: " + line);
                        }
                        m_indexDao.add(i);
                        //m_indexes.add(m.group(1));
                    } else {
                        throw new Exception("Unknown CREATE encountered: "
                                + "CREATE " + type + " " + name);
                    }
                } else {
                    throw new Exception("Unknown CREATE encountered: " + line);
                }

                sql_l.add(line);
                continue;
            }

            m = insertPattern.matcher(line);
            if (m.matches()) {
                String table = m.group(1);
                if (!m_inserts.containsKey(table)) {
                    m_inserts.put(table, new LinkedList<String>());
                }
                m_inserts.get(table).add(line);

                continue;
            }

            if (line.toLowerCase().startsWith("select setval ")) {
                String table = "select_setval";
                if (!m_inserts.containsKey(table)) {
                    m_inserts.put(table, new LinkedList<String>());
                }
                m_inserts.get(table).add(line);

                sql_l.add(line);
                continue;
            }

            m = dropPattern.matcher(line);
            if (m.matches()) {
                m_drops.add(m.group(1));

                sql_l.add(line);
                continue;
            }

            // XXX should do something here so we can catch what we can't
            // parse
            // m_out.println("unmatched line: " + line);

            sql_l.add(line);
        }
        r.close();

        m_sql = cleanText(sql_l);
    }

    public void databaseConnect(String database) throws Exception {
        Class.forName(m_pg_driver);
        m_dbconnection = DriverManager.getConnection(m_pg_url + database,
                                                     m_pg_user, m_pg_pass);
    }

    public void databaseDisconnect() throws Exception {
        if (m_dbconnection != null) {
            m_dbconnection.close();
        }
    }

    public void databaseCheckVersion() throws Exception {
        m_out.print("- checking database version... ");

        Statement st = m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT version()");
        if (!rs.next()) {
            throw new Exception("Database didn't return any rows for "
                    + "'SELECT version()'");
        }

        String versionString = rs.getString(1);

        rs.close();
        st.close();

        Matcher m = Pattern.compile("^PostgreSQL (\\d+\\.\\d+)").matcher(
                                                                         versionString);

        if (!m.find()) {
            throw new Exception("Could not parse version number out of "
                    + "version string: " + versionString);
        }
        m_pg_version = Float.parseFloat(m.group(1));

        if (m_pg_version < POSTGRES_MIN_VERSION) {
            throw new Exception("Unsupported database version \""
                    + m_pg_version + "\" -- you need at least "
                    + POSTGRES_MIN_VERSION);
        }

        // doesn't matter since we now require 7.3
        /*
         * if (m_pg_version >= 7.3) { m_cascade = " CASCADE"; }
         */

        m_out.println(Float.toString(m_pg_version));
        m_out.println("  - Full version string: " + versionString);
    }

    public void databaseCheckLanguage() throws Exception {
        /*
         * Don't bother checking if the database version is 7.4 or greater and
         * just return without throwing an exception. We can (and do) use SQL
         * state checks instead of matching on the exception text, so the
         * language of server error messages does not matter.
         */
        if (m_pg_version >= 7.4) {
            return;
        }

        /*
         * Use column names that should never exist and also encode the
         * current time, in hopes that this should never actually succeed.
         */
        String timestamp = Long.toString(System.currentTimeMillis());
        String bogus_query = "SELECT bogus_column_" + timestamp + " "
                + "FROM bogus_table_" + timestamp + " "
                + "WHERE another_bogus_column_" + timestamp + " IS NULL";

        // Expected error: "ERROR: relation "bogus_table" does not exist"
        try {
            Statement st = m_dbconnection.createStatement();
            st.executeQuery(bogus_query);
        } catch (SQLException e) {
            if (e.toString().indexOf("does not exist") != -1) {
                /*
                 * Everything is fine, since we matched the error. We should
                 * be safe to assume that all of the other error messages we
                 * need to check for are in English.
                 */
                return;
            }
            throw new Exception("The database server's error messages "
                    + "are not in English, however the installer "
                    + "requires them to be in English when using "
                    + "PostgreSQL earlier than 7.4.  You either "
                    + "need to set \"lc_messages = 'C'\" in your "
                    + "postgresql.conf file and restart "
                    + "PostgreSQL or upgrade to PostgreSQL 7.4 or "
                    + "later.  The installer executed the query " + "\""
                    + bogus_query + "\" and expected "
                    + "\"does not exist\" in the error message, "
                    + "but this exception was received instead: " + e, e);
        }

        /*
         * We should not get here, as the above command should always throw an
         * exception, so complain and throw an exception about not getting the
         * exception we were expecting. Are you lost yet? Good!
         */
        throw new Exception("Expected an SQLException when executing a "
                + "bogus query to test for the server's error "
                + "message language, however the query succeeded "
                + "unexpectedly.  SQL query: \"" + bogus_query + "\".");

    }

    public void checkOldTables() throws SQLException,
            BackupTablesFoundException {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT relname FROM pg_class "
                + "WHERE relkind = 'r' AND " + "relname LIKE '%_old_%'");
        LinkedList<String> oldTables = new LinkedList<String>();

        m_out.print("- checking database for old backup tables... ");

        while (rs.next()) {
            oldTables.add(rs.getString(1));
        }

        rs.close();
        st.close();

        if (oldTables.size() == 0) {
            // No problems, so just print "NONE" and return.
            m_out.println("NONE");
            return;
        }

        throw new BackupTablesFoundException(oldTables);
    }

    public List<Constraint> getForeignKeyConstraints() throws Exception {
        LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        for (String table : m_tables) {
            String tableLower = table.toLowerCase();
            for (Constraint constraint : getTableFromSQL(tableLower).getConstraints()) {
                if (constraint.getType() == Constraint.FOREIGN_KEY) {
                    constraints.add(constraint);
                }
            }
        }

        return constraints;
    }

    public void checkConstraints() throws Exception {
        List<Constraint> constraints = getForeignKeyConstraints();

        m_out.println("- checking for rows that violate constraints...");

        for (Constraint constraint : constraints) {
            m_out.print("  - checking for rows that violate constraint '"
                        + constraint.getName() + "'... ");
            checkConstraint(constraint);
            m_out.println("DONE");
        }
        
        m_out.println("- checking for rows that violate constraints... DONE");
    }
    
    public void checkConstraint(Constraint constraint) throws Exception {
        String name = constraint.getName();
        String table = constraint.getTable();
        List<String> columns = constraint.getColumns();
        String ftable = constraint.getForeignTable();
        List<String> fcolumns = constraint.getForeignColumns();

        if (!tableExists(table)) {
            // The constrained table does not exist
            return;
        }
        for (String column : columns) {
            if (!tableColumnExists(table, column)) {
                // This constrained column does not exist
                return;
            }
        }

        // XXX Not sure if it's okay to leave this out
        /*
         * if (table.equals("usersNotified") && column.equals("id")) { //
         * m_out.print("Skipping usersNotified.id"); continue; }
         */

        String query = "SELECT count(*) FROM " + table + " WHERE "
                + getForeignConstraintWhere(table, columns, ftable, fcolumns);

        Statement st = m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery(query);

        rs.next();
        int count = rs.getInt(1);
        rs.close();

        if (count != 0) {
            rs = st.executeQuery("SELECT count(*) FROM " + table);
            rs.next();
            int total = rs.getInt(1);
            rs.close();
            st.close();

            throw new Exception("Table " + table + " contains " + count
                    + " rows " + "(out of " + total
                    + ") that violate new constraint " + name + ".  "
                    + "See the install guide for details "
                    + "on how to correct this problem.");
        }

        st.close();

    }

    public String getForeignConstraintWhere(String table, List<String> columns,
            String ftable, List<String> fcolumns) throws Exception {
        String notNulls = notNullWhereClause(table, columns);
 
        if (!tableExists(ftable)) {
            return notNulls;
        }
        for (String fcolumn : fcolumns) {
            if (!tableColumnExists(ftable, fcolumn)) {
                return notNulls;
            }
        }

        return notNulls + " AND ( "
            + join(", ", tableColumnList(table, columns))
            + " ) NOT IN (SELECT "
            + join(", ", tableColumnList(ftable, fcolumns))
            + " FROM " + ftable + ")";
    }

    public String notNullWhereClause(String table, List<String> columns) {
        List<String> isNotNulls = new ArrayList<String>(columns.size());
        
        for (String column : columns) {
            isNotNulls.add(table + "." + column + " IS NOT NULL");
        }
        
        return join(" AND ", isNotNulls);
    }
    
    public List<String> tableColumnList(String table, List<String> columns) {
        List<String> tableColumns = new ArrayList<String>(columns.size());
        
        for (String column : columns) {
            tableColumns.add(table + "." + column);
        }
        
        return tableColumns;
    }


    public void fixConstraint() throws Exception {
        List<Constraint> constraints = getForeignKeyConstraints();

        m_out.print("- fixing rows that violate constraint "
                + m_fix_constraint_name + "... ");

        for (Constraint c : constraints) {
            if (m_fix_constraint_name.equals(c.getName())) {
                m_out.println(fixConstraint(c));
                return;
            }
        }

        throw new Exception("Did not find constraint "
                            + m_fix_constraint_name + " in the database.");
    }
    
    public String fixConstraint(Constraint constraint) throws Exception {
        String table = constraint.getTable();
        List<String> columns = constraint.getColumns();
        String ftable = constraint.getForeignTable();
        List<String> fcolumns = constraint.getForeignColumns();

        if (!tableExists(table)) {
            throw new Exception("Constraint " + m_fix_constraint_name
                    + " is on table " + table + ", but table does "
                    + "not exist (so fixing this constraint does "
                    + "nothing).");
        }

        for (String column : columns) {
            if (!tableColumnExists(table, column)) {
                throw new Exception("Constraint " + m_fix_constraint_name
                                    + " constrains column " + column
                                    + " of table " + table
                                    + ", but column does "
                                    + "not exist (so fixing this constraint "
                                    + "does nothing).");
            }
        }

        String where = getForeignConstraintWhere(table, columns, ftable,
                                                 fcolumns);

        String query;
        String change_text;

        if (m_fix_constraint_remove_rows) {
            query = "DELETE FROM " + table + " WHERE " + where;
            change_text = "DELETED";
        } else {
            List<String> sets = new ArrayList<String>(columns.size());
            for (String column : columns) {
                sets.add(column + " = NULL");
            }
            
            query = "UPDATE " + table + " SET " + join(", ", sets) + " "
                + "WHERE " + where;
            change_text = "UPDATED";
        }

        Statement st = m_dbconnection.createStatement();
        int num = st.executeUpdate(query);

        return change_text + " " + num + (num == 1 ? " ROW" : " ROWS");
    }

    public boolean databaseUserExists() throws SQLException {
        boolean exists;

        Statement st = m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT usename FROM pg_user WHERE "
                + "usename = '" + m_user + "'");

        exists = rs.next();

        rs.close();
        st.close();

        return exists;
    }

    public void databaseAddUser() throws SQLException {
        Statement st = m_dbconnection.createStatement();
        st.execute("CREATE USER " + m_user + " WITH PASSWORD '" + m_pass
                + "' CREATEDB CREATEUSER");
    }

    public boolean databaseDBExists() throws SQLException {
        boolean exists;

        Statement st = m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT datname from pg_database "
                + "WHERE datname = '" + m_database + "'");

        exists = rs.next();

        rs.close();
        st.close();

        return exists;
    }

    public void databaseAddDB() throws Exception {
        Statement st = m_dbconnection.createStatement();
        st.execute("CREATE DATABASE " + m_database
                + " WITH ENCODING='UNICODE'");
    }

    public void createSequences() throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        m_out.println("- creating sequences... ");

        Iterator i = m_sequences.iterator();
        while (i.hasNext()) {
            String sequence = (String) i.next();
            if (!m_seqmapping.containsKey(sequence)) {
                throw new Exception("Cannot find sequence mapping for "
                        + sequence);
            }
        }

        i = m_sequences.iterator();
        while (i.hasNext()) {
            String sequence = (String) i.next();
//            String[] mapping = (String[]) m_seqmapping.get(sequence);
            int minvalue = 1;
            boolean alreadyExists;

            m_out.print("  - checking \"" + sequence + "\" sequence... ");

            rs = st.executeQuery("SELECT relname FROM pg_class "
                    + "WHERE relname = '" + sequence.toLowerCase() + "'");
            alreadyExists = rs.next();
            if (alreadyExists) {
                m_out.println("ALREADY EXISTS");
            } else {
                m_out.println("DOES NOT EXIST");
                m_out.print("    - creating sequence \"" + sequence + "\"... ");
                st.execute("CREATE SEQUENCE " + sequence + " minvalue "
                           + minvalue);
                st.execute("GRANT ALL on " + sequence + " TO " + m_user);
                m_out.println("OK");
            }
        }

        m_out.println("- creating sequences... DONE");
    }

    public void createTables() throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        m_out.println("- creating tables...");

        for (String tableName : m_tables) {
            if (m_force) {
                tableName = tableName.toLowerCase();

                String create = getTableCreateFromSQL(tableName);

                boolean remove;

                rs = st.executeQuery("SELECT relname FROM pg_class "
                        + "WHERE relname = '" + tableName + "'");

                remove = rs.next();

                m_out.print("  - removing old table... ");
                if (remove) {
                    st.execute("DROP TABLE " + tableName + m_cascade);
                    m_out.println("REMOVED");
                } else {
                    m_out.println("CLEAN");
                }

                m_out.print("  - creating table \"" + tableName + "\"... ");
                st.execute("CREATE TABLE " + tableName + " (" + create + ")");
                m_out.println("CREATED");

                addIndexesForTable(tableName);
                addTriggersForTable(tableName);

                m_out.print("  - giving \"" + m_user + "\" permissions on \""
                        + tableName + "\"... ");
                st.execute("GRANT ALL ON " + tableName + " TO " + m_user);
                m_out.println("GRANTED");
            } else {
                m_out.print("  - checking table \"" + tableName + "\"... ");

                tableName = tableName.toLowerCase();

                Table newTable = getTableFromSQL(tableName);
                Table oldTable = getTableFromDB(tableName);

                if (newTable.equals(oldTable)) {
                    m_out.println("UPTODATE");
                    addIndexesForTable(tableName);
                    addTriggersForTable(tableName);
                } else {
                    if (oldTable == null) {
                        String create = getTableCreateFromSQL(tableName);
                        String createSql = "CREATE TABLE " + tableName + " ("
                            + create + ")"; 
                        st.execute(createSql);
                        
                        addIndexesForTable(tableName);
                        addTriggersForTable(tableName);
                        st.execute("GRANT ALL ON " + tableName + " TO "
                                + m_user);
                        m_out.println("CREATED");
                    } else {
                        try {
                            changeTable(tableName, oldTable, newTable);
                        } catch (Exception e) {
                            throw new Exception("Error changing table '"
                                                + tableName
                                                + "'.  Nested exception: "
                                                + e.getMessage(), e);
                        }
                    }
                }
            }
        }

        m_out.println("- creating tables... DONE");
    }
    
    public void addTriggersForTable(String table) throws SQLException {
        List<Trigger> triggers =
            m_triggerDao.getTriggersForTable(table.toLowerCase());
        for (Trigger trigger : triggers) {
            m_out.print("    - checking trigger '" + trigger.getName()
                        + "' on this table... ");
            if (!trigger.isOnDatabase(m_dbconnection)) {
                trigger.addToDatabase(m_dbconnection);
            }
            m_out.println("DONE");
        }
    }
    
    public void addIndexesForTable(String table) throws SQLException {
        List<Index> indexes =
            m_indexDao.getIndexesForTable(table.toLowerCase());
        for (Index index : indexes) {
            m_out.print("    - checking index '" + index.getName()
                        + "' on this table... ");
            if (!index.isOnDatabase(m_dbconnection)) {
                index.addToDatabase(m_dbconnection);
            }
            m_out.println("DONE");
        }

    }

    public void createIndexes() throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        m_out.println("- creating indexes...");

        for (String index : m_indexes) {
            boolean exists;

            m_out.print("  - creating index \"" + index + "\"... ");

            rs = st.executeQuery("SELECT relname FROM pg_class "
                    + "WHERE relname = '" + index.toLowerCase() + "'");

            exists = rs.next();

            if (exists) {
                m_out.println("EXISTS");
            } else {
                st.execute(getIndexFromSQL(index));
                m_out.println("OK");
            }
        }

        m_out.println("- creating indexes... DONE");
    }

    public Map getTypesFromDB() throws SQLException {
        if (m_dbtypes != null) {
            return m_dbtypes;
        }

        Statement st = m_dbconnection.createStatement();
        ResultSet rs;
        HashMap<String, Integer> m = new HashMap<String, Integer>();

        rs = st.executeQuery("SELECT oid,typname,typlen FROM pg_type");

        while (rs.next()) {
            try {
                m.put(Column.normalizeColumnType(rs.getString(2),
                                                 (rs.getInt(3) < 0)),
                      new Integer(rs.getInt(1)));
            } catch (Exception e) {
                // ignore
            }
        }

        m_dbtypes = m;
        return m_dbtypes;
    }

    /*
     * -- Not used, nothing in create.sql... public void createFunctions(List
     * functions) throws Exception { Statement st =
     * m_dbconnection.createStatement(); ResultSet rs; Iterator i =
     * functions.iterator(); while (i.hasNext()) { String function = (String)
     * i.next(); String functionSql = getFunctionFromSQL(function); Matcher m =
     * Pattern.compile("\\s*\\((.+?)\\).*").matcher(functionSql); String
     * columns = m.group(1); if (m_force) { // XXX this doesn't check to see
     * if the function exists // before it drops it, so it will fail and throw
     * an // exception if the function doesn't exist. m_out.print("- removing
     * function \"" + function + "\" if it exists... "); String dropSql =
     * "DROP FUNCTION \"" + function + "\" (" + columns + ");";
     * st.execute(dropSql); m_out.println("REMOVED"); } // XXX this doesn't
     * check to see if the function exists before // it tries to create it, so
     * it will fail and throw an // exception if the function does exist.
     * m_out.print("- creating function \"" + function + "\"... ");
     * st.execute("CREATE FUNCTION \"" + function + "\" " + functionSql);
     * m_out.println("OK"); } } public void createLanguages() throws Exception {
     * Statement st = m_dbconnection.createStatement(); ResultSet rs; Iterator
     * i = m_languages.iterator(); while (i.hasNext()) { String language =
     * (String) i.next(); String languageSql = getLanguageFromSQL(language); //
     * XXX this doesn't check to see if the language exists before // it tries
     * to create it, so it will fail and throw an // exception if the language
     * does already exist. m_out.print("- creating language reference \"" +
     * language + "\"... "); st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE '" +
     * language + "' " + languageSql); m_out.println("OK"); } }
     */

    public void fixData() throws Exception {
        Statement st = m_dbconnection.createStatement();

        st.execute("UPDATE ipinterface SET issnmpprimary='N' "
                + "WHERE issnmpprimary IS NULL");
        st.execute("UPDATE service SET servicename='SSH' "
                + "WHERE servicename='OpenSSH'");
        st.execute("UPDATE snmpinterface SET snmpipadentnetmask=NULL");
    }

    // XXX This causes the following Postgres error:
    // ERROR: duplicate key violates unique constraint "pk_dpname"
    void insertData() throws Exception {
        Statement st = m_dbconnection.createStatement();

        for (Iterator i = m_inserts.keySet().iterator(); i.hasNext();) {
            String table = (String) i.next();
            boolean exists = false;

            m_out.print("- inserting initial table data for \"" + table
                    + "\"... ");

            for (Iterator j = ((LinkedList) m_inserts.get(table)).iterator(); j.hasNext();) {
                try {
                    st.execute((String) j.next());
                } catch (SQLException e) {
                    /*
                     * SQL Status codes: 23505: ERROR: duplicate key violates
                     * unique constraint "%s"
                     */
                    if (e.toString().indexOf("duplicate key") != -1
                            || "23505".equals(e.getSQLState())) {
                        exists = true;
                    } else {
                        throw e;
                    }
                }
            }

            if (exists) {
                m_out.println("EXISTS");
            } else {
                m_out.println("OK");
            }
        }
    }

    public void checkUnicode() throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        m_out.print("- checking if database \"" + m_database
                + "\" is unicode... ");

        rs = st.executeQuery("SELECT encoding FROM pg_database WHERE "
                + "datname='" + m_database.toLowerCase() + "'");
        rs.next();
        if (rs.getInt(1) == 5 || rs.getInt(1) == 6) {
            m_out.println("ALREADY UNICODE");
            return;
        }

        m_out.println("NOT UNICODE, CONVERTING");

        databaseDisconnect();

        String dumpFile = "/tmp/pg_dump-" + m_database;
        String logFile = "/tmp/unicode-convert.log";
        PrintStream log = new PrintStream(new FileOutputStream(logFile, true));
        ProcessExec e = new ProcessExec(log, log);

        int exitVal;

        log.println("------------------------------------------------------"
                + "------------------------");

        m_out.print("  - dumping data to " + dumpFile + "... ");
        String[] cmd1 = { m_pg_bindir + File.separator + "pg_dump", "-U",
                m_pg_user, "-a", m_database, "-f", dumpFile };
        if ((exitVal = e.exec(cmd1)) != 0) {
            throw new Exception("Dumping database returned non-zero exit "
                    + "value " + exitVal + " while executing " + "command '"
                    + join(" ", cmd1) + "', check " + logFile);
        }
        m_out.println("OK");

        m_out.print("  - waiting 3s for PostgreSQL to notice "
                + "that pg_dump has disconnected.");
        Thread.sleep(1000);
        m_out.print(".");
        Thread.sleep(1000);
        m_out.print(".");
        Thread.sleep(1000);
        m_out.println(" OK");

        m_out.print("  - dropping old database... ");
        String[] cmd2 = { m_pg_bindir + File.separator + "dropdb", "-U",
                m_pg_user, m_database };
        if ((exitVal = e.exec(cmd2)) != 0) {
            throw new Exception("Dropping database returned non-zero exit "
                    + "value " + exitVal + " while executing " + "command '"
                    + join(" ", cmd2) + "', check " + logFile);
        }
        m_out.println("OK");

        m_out.print("  - creating new unicode database... ");
        String[] cmd3 = { m_pg_bindir + File.separator + "createdb", "-U",
                m_pg_user, "-E", "UNICODE", m_database };
        if ((exitVal = e.exec(cmd3)) != 0) {
            throw new Exception("Creating database returned non-zero exit "
                    + "value " + exitVal + " while executing " + "command '"
                    + join(" ", cmd3) + "', check " + logFile);
        }
        m_out.println("OK");

        m_out.print("  - recreating tables... ");
        String[] cmd4 = { m_pg_bindir + File.separator + "psql", "-U",
                m_user, "-f", m_sql_dir + File.separator + "create.sql",
                m_database };
        if ((exitVal = e.exec(cmd4)) != 0) {
            throw new Exception("Recreating tables returned non-zero exit "
                    + "value " + exitVal + " while executing " + "command '"
                    + join(" ", cmd4) + "', check " + logFile);
        }
        m_out.println("OK");

        m_out.print("  - restoring data... ");
        String[] cmd5 = { m_pg_bindir + File.separator + "psql", "-U",
                m_user, "-f", dumpFile, m_database };
        if ((exitVal = e.exec(cmd5)) != 0) {
            throw new Exception("Restoring data returned non-zero exit "
                    + "value " + exitVal + " while executing " + "command '"
                    + join(" ", cmd5) + "', check " + logFile);
        }
        m_out.println("OK");

        log.close();

        databaseConnect(m_database);
    }

    public void verifyFilesAndDirectories() throws FileNotFoundException {
        if (m_update_database) {
            verifyFileExists(true, m_sql_dir, "SQL directory",
                             "install.etc.dir property");

            verifyFileExists(false, m_create_sql, "create.sql",
                             "install.etc.dir property");
        }

        if (m_update_iplike) {
            verifyFileExists(false, m_pg_iplike, "iplike module",
                             "install.postgresql.dir property");
        }

        if (m_tomcat_conf != null) {
            verifyFileExists(
                             false,
                             m_tomcat_conf,
                             "Tomcat startup configuration file tomcat4.conf",
                             "-T option");
        }

        if (m_install_webapp) {
            verifyFileExists(true, m_webappdir, "Tomcat context directory",
                             "-w option");

            verifyFileExists(true, m_install_servletdir,
                             "OpenNMS servlet directory",
                             "install.servlet.dir property");
        }
    }

    public void verifyFileExists(boolean isDir, String file,
            String description, String option) throws FileNotFoundException {
        File f;

        if (file == null) {
            throw new FileNotFoundException("The user most provide the "
                    + "location of " + description
                    + ", but this is not specified.  " + "Use the " + option
                    + " to specify this file.");
        }

        m_out.print("- using " + description + "... ");

        f = new File(file);

        if (!f.exists()) {
            throw new FileNotFoundException(description
                    + " does not exist at \"" + file + "\".  Use the "
                    + option + " to specify another location.");
        }

        if (!isDir) {
            if (!f.isFile()) {
                throw new FileNotFoundException(description
                        + " not a file at \"" + file + "\".  Use the "
                        + option + " to specify another file.");
            }
        } else {
            if (!f.isDirectory()) {
                throw new FileNotFoundException(description
                        + " not a directory at \"" + file + "\".  Use the "
                        + option + " to specify " + "another directory.");
            }
        }

        m_out.println(f.getAbsolutePath());
    }

    public void addStoredProcedures() throws Exception {
        m_triggerDao = new TriggerDao();

        Statement st = m_dbconnection.createStatement();

        m_out.print("- adding stored procedures... ");

        FileFilter sqlFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.getName().startsWith("get") && pathname.getName().endsWith(".sql"))
                     || pathname.getName().endsWith("Trigger.sql");
            }
        };

        File[] list = new File(m_sql_dir).listFiles(sqlFilter);

        for (int i = 0; i < list.length; i++) {
            LinkedList<String> drop = new LinkedList<String>();
            StringBuffer create = new StringBuffer();
            String line;

            m_out.print("\n  - " + list[i].getName() + "... ");

            BufferedReader r = new BufferedReader(new FileReader(list[i]));
            while ((line = r.readLine()) != null) {
                line = line.trim();

                if (line.matches("--.*")) {
                    continue;
                }

                if (line.toLowerCase().startsWith("drop function")
                    || line.toLowerCase().startsWith("drop trigger")) {
                    drop.add(line);
                } else {
                    create.append(line);
                    create.append("\n");
                }
            }
            r.close();
            
            /*
             * Find the trigger first, because if there is a trigger that
             * uses this stored procedure on this table, we'll need to drop
             * it first.
             */
            Trigger t = Trigger.findTriggerInString(create.toString());
            if (t != null) {
                m_triggerDao.add(t);
            }

            Matcher m = Pattern.compile("(?is)\\b(CREATE(?: OR REPLACE)? FUNCTION\\s+"
                                                + "(\\w+)\\s*\\((.*?)\\)\\s+"
                                                + "RETURNS\\s+(\\S+)\\s+AS\\s+"
                                                + "(.+? language ['\"]?\\w+['\"]?);)").matcher(
                                                                                              create.toString());

            if (!m.find()) {
                throw new Exception("For stored procedure in file '"
                                    + list[i].getName()
                                    + "' couldn't match \""
                                    + m.pattern().pattern()
                                    + "\" in string \"" + create + "\"");
            }
            String createSql = m.group(1);
            String function = m.group(2);
            String columns = m.group(3);
            String returns = m.group(4);
            // String rest = m.group(5);

            
            if (functionExists(function, columns, returns)) {
                if (t != null && t.isOnDatabase(m_dbconnection)) {
                    t.removeFromDatabase(m_dbconnection);
                    
                }
                st.execute("DROP FUNCTION " + function + "(" + columns + ")");
                st.execute(createSql);
                m_out.print("OK (dropped and re-added)");
            } else {
                st.execute(createSql);
                m_out.print("OK");
            }
        }
        m_out.println("");
    }

    public boolean functionExists(String function, String columns,
            String returnType) throws Exception {
        Map types = getTypesFromDB();

        int[] columnTypes = new int[0];
        columns = columns.trim();
        if (columns.length() > 0) {
            String[] splitColumns = columns.split("\\s*,\\s*");
            columnTypes = new int[splitColumns.length];
            Column c;
            for (int j = 0; j < splitColumns.length; j++) {
                c = new Column();
                c.parseColumnType(splitColumns[j]);
                columnTypes[j] = ((Integer) types.get(c.getType())).intValue();
            }
        }

        Column c = new Column();
        try {
            c.parseColumnType(returnType);
        } catch (Exception e) {
            throw new Exception("Could not parse column type '" + returnType + "' for function '" + function + "'.  Nested exception: " + e.getMessage(), e);
        }
        int retType = ((Integer) types.get(c.getType())).intValue();

        return functionExists(function, columnTypes, retType);
    }

    public boolean functionExists(String function, int[] columnTypes,
            int retType) throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        StringBuffer ct = new StringBuffer();
        for (int j = 0; j < columnTypes.length; j++) {
            ct.append(" " + columnTypes[j]);
        }

        String query = "SELECT oid FROM pg_proc WHERE proname='"
                + function.toLowerCase() + "' AND " + "prorettype=" + retType
                + " AND " + "proargtypes='" + ct.toString().trim() + "'";

        rs = st.executeQuery(query);
        return rs.next();
    }

    public void checkWebappOldOpennmsDir() throws Exception {
        File f = new File(m_webappdir + File.separator + "opennms");

        m_out.print("- Checking for old opennms webapp directory in "
                + f.getAbsolutePath() + "... ");

        if (f.exists()) {
            throw new Exception("Old OpenNMS web application exists: "
                    + f.getAbsolutePath() + ".  You need to remove this "
                    + "before continuing.");
        }

        m_out.println("OK");
    }

    public void checkServerXmlOldOpennmsContext() throws Exception {
        String search_regexp = "(?ms).*<Context\\s+path=\"/opennms\".*";
        StringBuffer b = new StringBuffer();

        File f = new File(m_webappdir + File.separator + ".."
                + File.separator + "conf" + File.separator + "server.xml");

        m_out.print("- Checking for old opennms context in "
                + f.getAbsolutePath() + "... ");

        if (!f.exists()) {
            m_out.println("DID NOT CHECK (file does not exist)");
            return;
        }

        BufferedReader r = new BufferedReader(new FileReader(f));
        String line;

        while ((line = r.readLine()) != null) {
            b.append(line);
            b.append("\n");
        }
        r.close();

        if (b.toString().matches(search_regexp)) {
            throw new Exception(
                                "Old OpenNMS context found in "
                                        + f.getAbsolutePath()
                                        + ".  "
                                        + "You must remove this context from server.xml and re-run the "
                                        + "installer.");
        }

        m_out.println("OK");

        return;
    }

    public void installWebApp() throws Exception {
        m_out.println("- Install OpenNMS webapp... ");

        installLink(m_install_servletdir + File.separator + "META-INF"
                + File.separator + "context.xml", m_webappdir
                + File.separator + "opennms.xml", "web application context",
                    false);

        m_out.println("- Installing OpenNMS webapp... DONE");
    }

    public void installLink(String source, String destination,
            String description, boolean recursive) throws Exception {

        String[] cmd;
        ProcessExec e = new ProcessExec(m_out, m_out);

        if (new File(destination).exists()) {
            m_out.print("  - " + destination + " exists, removing... ");
            removeFile(destination, description, recursive);
            m_out.println("REMOVED");
        }

        m_out.print("  - creating link to " + destination + "... ");

        cmd = new String[4];
        cmd[0] = "ln";
        cmd[1] = "-sf";
        cmd[2] = source;
        cmd[3] = destination;

        if (e.exec(cmd) != 0) {
            throw new Exception("Non-zero exit value returned while "
                    + "linking " + description + ", " + source + " into "
                    + destination);
        }

        m_out.println("DONE");
    }

    public void updateTomcatConf() throws Exception {
        File f = new File(m_tomcat_conf);

        // XXX give the user the option to set the user to something else?
        // if so, should we chown the appropriate OpenNMS files to the
        // tomcat user?
        //
        // XXX should we have the option to automatically try to determine
        // the tomcat user and chown the OpenNMS files to that user?

        m_out.print("- setting tomcat4 user to 'root'... ");

        BufferedReader r = new BufferedReader(new FileReader(f));
        StringBuffer b = new StringBuffer();
        String line;

        while ((line = r.readLine()) != null) {
            if (line.startsWith("TOMCAT_USER=")) {
                b.append("TOMCAT_USER=\"root\"\n");
            } else {
                b.append(line);
                b.append("\n");
            }
        }
        r.close();

        f.renameTo(new File(m_tomcat_conf + ".before-opennms-"
                + System.currentTimeMillis()));

        f = new File(m_tomcat_conf);
        PrintWriter w = new PrintWriter(new FileOutputStream(f));

        w.print(b.toString());
        w.close();

        m_out.println("done");
    }

    public void removeFile(String destination, String description,
            boolean recursive) throws IOException, InterruptedException,
            Exception {
        String[] cmd;
        ProcessExec e = new ProcessExec(m_out, m_out);

        if (recursive) {
            cmd = new String[3];
            cmd[0] = "rm";
            cmd[1] = "-r";
            cmd[2] = destination;
        } else {
            cmd = new String[2];
            cmd[0] = "rm";
            cmd[1] = destination;
        }
        if (e.exec(cmd) != 0) {
            throw new Exception("Non-zero exit value returned while "
                    + "removing " + description + ", " + destination
                    + ", using \"" + join(" ", cmd) + "\"");
        }

        if (new File(destination).exists()) {
            throw new Exception("Could not delete existing " + description
                    + ": " + destination);
        }
    }

    public void updateIplike() throws Exception {
        Statement st = m_dbconnection.createStatement();

        m_out.print("- checking for stale iplike references... ");
        try {
            st.execute("DROP FUNCTION iplike(text,text)");
            m_out.println("REMOVED");
        } catch (SQLException e) {
            /*
             * SQL Status code: 42883: ERROR: function %s does not exist
             */
            if (e.toString().indexOf("does not exist") != -1
                    || "42883".equals("42883")) {
                m_out.println("CLEAN");
            } else {
                throw e;
            }
        }

        // XXX This error is generated from Postgres if eventtime(text)
        // does not exist:
        // ERROR: function eventtime(text) does not exist
        m_out.print("- checking for stale eventtime.so references... ");
        try {
            st.execute("DROP FUNCTION eventtime(text)");
            m_out.println("REMOVED");
        } catch (SQLException e) {
            /*
             * SQL Status code: 42883: ERROR: function %s does not exist
             */
            if (e.toString().indexOf("does not exist") != -1
                    || "42883".equals(e.getSQLState())) {
                m_out.println("CLEAN");
            } else {
                throw e;
            }
        }

        m_out.print("- adding iplike database function... ");
        st.execute("CREATE FUNCTION iplike(text,text) RETURNS bool " + "AS '"
                + m_pg_iplike + "' LANGUAGE 'c' WITH(isstrict)");
        m_out.println("OK");
    }

    public void updatePlPgsql() throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        m_out.print("- adding PL/pgSQL call handler... ");
        rs = st.executeQuery("SELECT oid FROM pg_proc WHERE "
                + "proname='plpgsql_call_handler' AND " + "proargtypes = ''");
        if (rs.next()) {
            m_out.println("EXISTS");
        } else {
            st.execute("CREATE FUNCTION plpgsql_call_handler () "
                    + "RETURNS OPAQUE AS '$libdir/plpgsql.so' LANGUAGE 'c'");
            m_out.println("OK");
        }

        m_out.print("- adding PL/pgSQL language module... ");
        rs = st.executeQuery("SELECT pg_language.oid FROM "
                + "pg_language, pg_proc WHERE "
                + "pg_proc.proname='plpgsql_call_handler' AND "
                + "pg_proc.proargtypes = '' AND "
                + "pg_proc.oid = pg_language.lanplcallfoid AND "
                + "pg_language.lanname = 'plpgsql'");
        if (rs.next()) {
            m_out.println("EXISTS");
        } else {
            st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' "
                    + "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
            m_out.println("OK");
        }
    }

    public Column findColumn(List columns, String column) {
        Column c;

        for (Iterator i = columns.iterator(); i.hasNext();) {
            c = (Column) i.next();
            if (c.getName().equals(column.toLowerCase())) {
                return c;
            }
        }

        return null;
    }

    public String getXFromSQL(String item, String regex, int itemGroup,
            int returnGroup, String description) throws Exception {

        item = item.toLowerCase();
        Matcher m = Pattern.compile(regex).matcher(m_sql);

        while (m.find()) {
            if (m.group(itemGroup).toLowerCase().equals(item)) {
                return m.group(returnGroup);
            }
        }

        throw new Exception("could not find " + description + " \"" + item
                + "\"");
    }

    public String getTableCreateFromSQL(String table) throws Exception {
        return getXFromSQL(table, "(?i)\\bcreate table\\s+['\"]?(\\S+)['\"]?"
                + "\\s+\\((.+?)\\);", 1, 2, "table");
    }

    public String getIndexFromSQL(String index) throws Exception {
        return getXFromSQL(index, "(?i)\\b(create (?:unique )?index\\s+"
                + "['\"]?(\\S+)['\"]?\\s+.+?);", 2, 1, "index");
    }

    public String getFunctionFromSQL(String function) throws Exception {
        return getXFromSQL(function, "(?is)\\bcreate function\\s+"
                + "['\"]?(\\S+)['\"]?\\s+"
                + "(.+? language ['\"]?\\w+['\"]?);", 1, 2, "function");
    }

    public String getLanguageFromSQL(String language) throws Exception {
        return getXFromSQL(language, "(?is)\\bcreate trusted procedural "
                + "language\\s+['\"]?(\\S+)['\"]?\\s+(.+?);", 1, 2,
                           "language");
    }

    public List<Column> getTableColumnsFromSQL(String tableName)
            throws Exception {
        return getTableFromSQL(tableName).getColumns();
    }

    public Table getTableFromSQL(String tableName) throws Exception {
        Table table = new Table();

        LinkedList<Column> columns = new LinkedList<Column>();
        LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        boolean parens = false;
        StringBuffer accumulator = new StringBuffer();

        String create = getTableCreateFromSQL(tableName);
        for (int i = 0; i <= create.length(); i++) {
            char c = ' ';

            if (i < create.length()) {
                c = create.charAt(i);

                if (c == '(' || c == ')') {
                    parens = (c == '(');
                    accumulator.append(c);
                    continue;
                }
            }

            if (((c == ',') && !parens) || i == create.length()) {
                String a = accumulator.toString().trim();

                if (a.toLowerCase().startsWith("constraint ")) {
                    Constraint constraint;
                    try {
                        constraint = new Constraint(tableName, a);
                    } catch (Exception e) {
                        throw new Exception("Could not parse constraint for table '" + tableName + "'.  Nested exception: " + e.getMessage(), e);
                    }
                    List<String> constraintColumns = constraint.getColumns();
                    if (constraintColumns.size() == 0) {
                        throw new IllegalStateException(
                                                        "constraint with no constrained columns");
                    }

                    for (String constrainedName : constraintColumns) {
                        Column constrained = findColumn(columns,
                                                        constrainedName);
                        if (constrained == null) {
                            throw new Exception(
                                                "constraint "
                                                        + constraint.getName()
                                                        + " references column \""
                                                        + constrainedName
                                                        + "\", which is not a column in the table "
                                                        + tableName);
                        }
                    }
                    constraints.add(constraint);
                } else {
                    Column column = new Column();
                    try {
                        column.parse(accumulator.toString());
                        columns.add(column);
                    } catch (Exception e) {
                        throw new Exception("Could not parse table " + tableName
                                + ".  Chained: " + e.getMessage(), e);
                    }
                }

                accumulator = new StringBuffer();
            } else {
                accumulator.append(c);
            }
        }

        table.setName(tableName);
        table.setColumns(columns);
        table.setConstraints(constraints);
        table.setNotNullOnPrimaryKeyColumns();

        return table;
    }

    public static String cleanText(List list) {
        StringBuffer s = new StringBuffer();
        Iterator i = list.iterator();

        while (i.hasNext()) {
            String l = (String) i.next();

            s.append(l.replaceAll("\\s+", " "));
            if (l.indexOf(';') != -1) {
                s.append('\n');
            }
        }

        return s.toString();
    }

    public boolean tableExists(String table) throws SQLException {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        rs = st.executeQuery("SELECT DISTINCT tablename FROM pg_tables "
                + "WHERE lower(tablename) = '" + table.toLowerCase() + "'");
        return rs.next();
    }

    public boolean tableColumnExists(String table, String column)
            throws Exception {
        return (findColumn(getTableColumnsFromDB(table), column) != null);
    }

    public List<Column> getTableColumnsFromDB(String tableName)
            throws Exception {
        Table table = getTableFromDB(tableName);
        if (table == null) {
            return null;
        }
        return table.getColumns();
    }

    public List<Column> getColumnsFromDB(String tableName) throws Exception {
        LinkedList<Column> columns = new LinkedList<Column>();

        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        String query = "SELECT "
                + "        attname, "
                + "        format_type(atttypid, atttypmod), "
                + "        attnotnull "
                + "FROM "
                + "        pg_attribute "
                + "WHERE "
                + "        attrelid = "
                + "                (SELECT oid FROM pg_class WHERE relname = '"
                + tableName.toLowerCase() + "') AND " + "        attnum > 0";

        if (m_pg_version >= 7.3) {
            query = query + " AND attisdropped = false";
        }

        query = query + " ORDER BY " + "        attnum";

        rs = st.executeQuery(query);

        while (rs.next()) {
            Column c = new Column();
            c.setName(rs.getString(1));
            String columnType = rs.getString(2);
            try {
                c.parseColumnType(columnType);
            } catch (Exception e) {
                throw new Exception("Error parsing column type '"
                        + columnType + "' for column '" + rs.getString(1)
                        + "' in table '" + tableName + "'.  Nested: "
                        + e.getMessage(), e);
            }
            c.setNotNull(rs.getBoolean(3));

            columns.add(c);
        }

        rs.close();
        st.close();

        return columns;

    }

    public Table getTableFromDB(String tableName) throws Exception {
        if (!tableExists(tableName)) {
            return null;
        }

        Table table = new Table();
        table.setName(tableName.toLowerCase());

        List<Column> columns = getColumnsFromDB(tableName);
        List<Constraint> constraints = getConstraintsFromDB(tableName);

        table.setColumns(columns);
        table.setConstraints(constraints);
        return table;
    }

    public List<Constraint> getConstraintsFromDB(String tableName)
            throws SQLException, Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        String query = "SELECT c.oid, c.conname, c.contype, c.conrelid, "
            + "c.confrelid, a.relname, c.confupdtype, c.confdeltype from pg_class a "
            + "right join pg_constraint c on c.confrelid = a.oid "
            + "where c.conrelid = (select oid from pg_class where relname = '"
                + tableName.toLowerCase() + "') order by c.oid";

        rs = st.executeQuery(query);

        while (rs.next()) {
            int oid = rs.getInt(1);
            String name = rs.getString(2);
            String type = rs.getString(3);
            int conrelid = rs.getInt(4);
            int confrelid = rs.getInt(5);
            String ftable = rs.getString(6);
            String foreignUpdType = rs.getString(7);
            String foreignDelType = rs.getString(8);
  
            Constraint constraint;
            if ("p".equals(type)) {
                List<String> columns = getConstrainedColumnsFromDBForConstraint(
                                                                                oid,
                                                                                conrelid);
                constraint = new Constraint(tableName.toLowerCase(), name,
                                            columns);
            } else if ("f".equals(type)) {
                List<String> columns = getConstrainedColumnsFromDBForConstraint(
                                                                                oid,
                                                                                conrelid);
                List<String> fcolumns = getForeignColumnsFromDBForConstraint(
                                                                             oid,
                                                                             confrelid);
                constraint = new Constraint(tableName.toLowerCase(), name,
                                            columns, ftable, fcolumns,
                                            foreignUpdType, foreignDelType);
            } else {
                throw new Exception("Do not support constraint type \""
                        + type + "\" in constraint \"" + name + "\"");
            }

            constraints.add(constraint);
        }

        return constraints;
    }

    private List<String> getConstrainedColumnsFromDBForConstraint(int oid,
            int conrelid) throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        LinkedList<String> columns = new LinkedList<String>();

        String query = "select a.attname from pg_attribute a, pg_constraint c where a.attrelid = c.conrelid and a.attnum = ANY (c.conkey) and c.oid = "
                + oid + " and a.attrelid = " + conrelid;
        rs = st.executeQuery(query);

        while (rs.next()) {
            columns.add(rs.getString(1));
        }

        rs.close();
        st.close();

        return columns;
    }

    private List<String> getForeignColumnsFromDBForConstraint(int oid,
            int confrelid) throws Exception {
        Statement st = m_dbconnection.createStatement();
        ResultSet rs;

        LinkedList<String> columns = new LinkedList<String>();

        String query = "select a.attname from pg_attribute a, pg_constraint c where a.attrelid = c.confrelid and a.attnum = ANY (c.confkey) and c.oid = "
                + oid + " and a.attrelid = " + confrelid;
        rs = st.executeQuery(query);

        while (rs.next()) {
            columns.add(rs.getString(1));
        }

        rs.close();
        st.close();

        return columns;
    }

    public void changeTable(String table, Table oldTable, Table newTable)
            throws Exception {
        List<Column> oldColumns = oldTable.getColumns();
        List<Column> newColumns = newTable.getColumns();

        Statement st = m_dbconnection.createStatement();
        TreeMap<String, ColumnChange> columnChanges = new TreeMap<String, ColumnChange>();
        String[] oldColumnNames = new String[oldColumns.size()];

        int i;
        Iterator j;

        if (m_changed.contains(table)) {
            return;
        }
        m_changed.add(table);

        m_out.println("SCHEMA DOES NOT MATCH");
        m_out.println("    - differences:");
        for (Constraint newConstraint : newTable.getConstraints()) {
            m_out.println("new constraint: " + newConstraint.getTable()
                    + ": " + newConstraint);
        }
        for (Constraint oldConstraint : oldTable.getConstraints()) {
            m_out.println("old constraint: " + oldConstraint.getTable()
                    + ": " + oldConstraint);
        }

        /*
         * XXX This doesn't check for old column rows that don't exist
         * in newColumns.
         */
        for (Column newColumn : newColumns) {
            Column oldColumn = findColumn(oldColumns, newColumn.getName());

            if (oldColumn == null || !newColumn.equals(oldColumn)) {
                m_out.println("      - column \"" + newColumn.getName()
                        + "\" is different");
                if (m_debug) {
                    m_out.println("        - old column: "
                            + ((oldColumn == null) ? "null"
                                                  : oldColumn.toString()));
                    m_out.println("        - new column: " + newColumn);
                }
            }

            if (!columnChanges.containsKey(newColumn.getName())) {
                columnChanges.put(newColumn.getName(), new ColumnChange());
            }

            ColumnChange columnChange = (ColumnChange) columnChanges.get(newColumn.getName());
            columnChange.setColumn(newColumn);

            /*
             * If the new column has a NOT NULL constraint, set a null replace
             * value for the column. Throw an exception if it is possible for
             * null data to be inserted into the new column. This would happen
             * if there is not a null replacement and the column either didn't
             * exist before or it did NOT have the NOT NULL constraint before.
             */
            if (m_columnReplacements.containsKey(table + "." + newColumn.getName())) {
                columnChange.setColumnReplacement(m_columnReplacements.get(table + "." + newColumn.getName()));
            }
            if (newColumn.isNotNull() && columnChange.getColumnReplacement() == null) {
                if (oldColumn == null) {
                    String message = "Column " + newColumn.getName()
                            + " in new table has NOT NULL "
                            + "constraint, however this column "
                            + "did not exist before and there is "
                            + "no change replacement for this column";
                    if (m_ignore_notnull) {
                        m_out.println(message + ".  Ignoring due to '-N'");
                    } else {
                        throw new Exception(message);
                    }
                } else if (!oldColumn.isNotNull()) {
                    String message = "Column " + newColumn.getName()
                            + " in new table has NOT NULL "
                            + "constraint, however this column "
                            + "did not have the NOT NULL "
                            + "constraint before and there is "
                            + "no change replacement for this column";
                    if (m_ignore_notnull) {
                        m_out.println(message + ".  Ignoring due to '-N'");
                    } else {
                        throw new Exception(message);
                    }
                }
            }
        }

        i = 0;
        for (j = oldColumns.iterator(); j.hasNext(); i++) {
            Column oldColumn = (Column) j.next();

            oldColumnNames[i] = oldColumn.getName();

            if (columnChanges.containsKey(oldColumn.getName())) {
                ColumnChange columnChange = (ColumnChange) columnChanges.get(oldColumn.getName());
                Column newColumn = (Column) columnChange.getColumn();
                if (newColumn.getType().indexOf("timestamp") != -1) {
                    columnChange.setUpgradeTimestamp(true);
                }
            } else {
                m_out.println("      * WARNING: column \""
                        + oldColumn.getName() + "\" exists in the "
                        + "database but is not in the new schema.  "
                        + "REMOVING COLUMN");
            }
        }

        String tmpTable = table + "_old_" + System.currentTimeMillis();

        try {
            if (tableExists(tmpTable)) {
                st.execute("DROP TABLE " + tmpTable + m_cascade);
            }

            m_out.print("    - creating temporary table... ");
            st.execute("CREATE TABLE " + tmpTable + " AS SELECT "
                    + join(", ", oldColumnNames) + " FROM " + table);
            m_out.println("done");

            st.execute("DROP TABLE " + table + m_cascade);

            m_out.print("    - creating new '" + table + "' table... ");
            st.execute("CREATE TABLE " + table + "("
                    + getTableCreateFromSQL(table) + ")");
            m_out.println("done");
            
            addIndexesForTable(table);
            addTriggersForTable(table);

            transformData(table, tmpTable, columnChanges, oldColumnNames);

            st.execute("GRANT ALL ON " + table + " TO " + m_user);

            m_out.print("    - optimizing table " + table + "... ");
            st.execute("VACUUM ANALYZE " + table);
            m_out.println("DONE");
        } catch (Exception e) {
            if (m_no_revert) {
                m_out.println("FAILED!  Not reverting due to '-R' being "
                        + "passed.  Old data in " + tmpTable);
                throw e;
            }

            try {
                m_dbconnection.rollback();
                m_dbconnection.setAutoCommit(true);

                if (tableExists(table)) {
                    st.execute("DROP TABLE " + table + m_cascade);
                }
                st.execute("CREATE TABLE " + table + " AS SELECT "
                        + join(", ", oldColumnNames) + " FROM " + tmpTable);
                st.execute("DROP TABLE " + tmpTable);
            } catch (SQLException se) {
                throw new Exception("Got SQLException while trying to "
                        + "revert table changes due to original " + "error: "
                        + e + "\n" + "SQLException while reverting table: "
                        + se, e);
            }
            m_out.println("FAILED!  Old data restored, however indexes and "
                    + "constraints on this table were not re-added");
            throw e;
        }

        // We don't care if dropping the tmp table fails since we've
        // completed copying it, so it's outside of the try/catch block above.
        st.execute("DROP TABLE " + tmpTable);

        m_out.println("    - completed updating table... ");
    }

    /*
     * Note: every column has a ColumnChange record for it, which lists
     * the column name, a null replacement, if any, and the indexes for
     * selected rows (for using in ResultSet.getXXX()) and prepared rows
     * (PreparedStatement.setObject()).
     * Monkey.  Make monkey dance.
     */
    public void transformData(String table, String oldTable,
            TreeMap<String, ColumnChange> columnChanges,
            String[] oldColumnNames) throws SQLException, ParseException,
            Exception {
        Statement st = m_dbconnection.createStatement();
        int i;

        st.setFetchSize(s_fetch_size);

//        String[] columns = columnChanges.keySet().toArray(new String[0]);
//        String[] questionMarks = new String[columns.length];

        for (i = 0; i < oldColumnNames.length; i++) {
            ColumnChange c = columnChanges.get(oldColumnNames[i]);
            if (c != null) {
                c.setSelectIndex(i + 1);
            }
        }
        
        LinkedList<String> insertColumns = new LinkedList<String>();
        LinkedList<String> questionMarks = new LinkedList<String>();
        for (ColumnChange c : columnChanges.values()) {
            c.setColumnType(c.getColumn().getColumnSqlType());
            
            ColumnChangeReplacement r = c.getColumnReplacement();
            if (r == null || r.addColumnIfColumnIsNew()) {
                insertColumns.add(c.getColumn().getName());
                questionMarks.add("?");
                c.setPrepareIndex(questionMarks.size());
            }
        }

        /*
        for (i = 0; i < columns.length; i++) {
            questionMarks[i] = "?";
            ColumnChange c = columnChanges.get(columns[i]);
            c.setPrepareIndex(i + 1);
            c.setColumnType(((Column) c.getColumn()).getColumnSqlType());
        }
        */

        /*
         * Pull everything in from the old table and filter it to update the
         * data to any new formats.
         */

        m_out.print("    - transforming data into the new table...\r");

        ResultSet rs = st.executeQuery("SELECT count(*) FROM " + oldTable);
        rs.next();
        long num_rows = rs.getLong(1);

        PreparedStatement select = null;
        PreparedStatement insert = null;
        String order;
        if (table.equals("outages")) {
            order = " ORDER BY iflostservice";
        } else {
            order = "";
        }

        String dbcmd = "SELECT " + join(", ", oldColumnNames) + " FROM "
                + oldTable + order;
        if (m_debug) {
            m_out.println("    - performing select: " + dbcmd);
        }
        select = m_dbconnection.prepareStatement(dbcmd);
        select.setFetchSize(s_fetch_size);

        dbcmd = "INSERT INTO " + table + " (" + join(", ", insertColumns)
                + ") values (" + join(", ", questionMarks) + ")";
        if (m_debug) {
            m_out.println("    - performing insert: " + dbcmd);
        }
        insert = m_dbconnection.prepareStatement(dbcmd);

        rs = select.executeQuery();
        m_dbconnection.setAutoCommit(false);

//        ColumnChange change;
        Object obj;
        SimpleDateFormat dateParser = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        char spin[] = { '/', '-', '\\', '|' };

        int current_row = 0;

        while (rs.next()) {
            /*
            for (String name : columnChanges.keySet()) {
                ColumnChange change = columnChanges.get(name);
                */
            for (ColumnChange change : columnChanges.values()) {
                String name = change.getColumn().getName();
                
                if (change.hasColumnReplacement()
                        && !change.getColumnReplacement().addColumnIfColumnIsNew()) {
                    continue;
                }

                if (change.getSelectIndex() > 0) {
                    obj = rs.getObject(change.getSelectIndex());
                    if (rs.wasNull()) {
                        obj = null;
                    }
                } else {
                    if (m_debug) {
                        m_out.println("      - don't know what to do "
                                + "for \"" + name + "\", prepared column "
                                + change.getPrepareIndex()
                                + ": setting to null");
                    }
                    obj = null;
                }

                if (obj == null && change.hasColumnReplacement()) {
                    ColumnChangeReplacement replacement =
                        change.getColumnReplacement();
                    obj = replacement.getColumnReplacement(rs, columnChanges);
                    if (m_debug) {
                        m_out.println("      - " + name
                                + " was NULL but is a "
                                + "requires NULL replacement -- "
                                + "replacing with '" + obj + "'");
                    }
                }

                if (obj != null) {
                    if (change.isUpgradeTimestamp()
                            && !obj.getClass().equals(
                                                      java.sql.Timestamp.class)) {
                        if (m_debug) {
                            m_out.println("      - " + name
                                    + " is an old-style timestamp");
                        }
                        String newObj = dateFormatter.format(dateParser.parse((String) obj));
                        if (m_debug) {
                            m_out.println("      - " + obj + " -> " + newObj);
                        }

                        obj = newObj;
                    }
                    if (m_debug) {
                        m_out.println("      - " + name + " = " + obj);
                    }
                } else {
                    if (m_debug) {
                        m_out.println("      - " + name + " = undefined");
                    }
                }

                if (obj == null) {
                    insert.setNull(change.getPrepareIndex(),
                                   change.getColumnType());
                } else {
                    insert.setObject(change.getPrepareIndex(), obj);
                }
            }

            try {
                insert.execute();
            } catch (SQLException e) {
                SQLException ex = new SQLException(
                                                   "Statement.execute() threw an "
                                                           + "SQLException while inserting a row: "
                                                           + "\""
                                                           + insert.toString()
                                                           + "\".  "
                                                           + "Original exception: "
                                                           + e.toString(),
                                                   e.getSQLState(),
                                                   e.getErrorCode());
                ex.setNextException(e);
                throw ex;
            }

            current_row++;

            if ((current_row % 20) == 0) {
                System.err.print("    - transforming data into the new "
                        + "table... "
                        + (int) Math.floor((current_row * 100) / num_rows)
                        + "%  [" + spin[(current_row / 20) % spin.length]
                        + "]\r");
            }
        }

        m_dbconnection.commit();
        m_dbconnection.setAutoCommit(true);

        if (table.equals("events") && num_rows == 0) {
            st.execute("INSERT INTO events (eventid, eventuei, eventtime, "
                    + "eventsource, eventdpname, eventcreatetime, "
                    + "eventseverity, eventlog, eventdisplay) values "
                    + "(0, 'http://uei.opennms.org/dummyevent', now(), "
                    + "'OpenNMS.Eventd', 'localhost', now(), 1, 'Y', 'Y')");
        }

        m_out.println("    - transforming data into the new table... "
                + "DONE           ");
    }

    public void printHelp() {
        m_out.println("usage:");
        m_out.println("      $OPENNMS_HOME/bin/install -h");
        m_out.println("      $OPENNMS_HOME/bin/install "
                + "[-r] [-x] [-N] [-R] [-c] [-d] [-i] [-s] [-U]");
        m_out.println("                                [-y] [-X]");
        m_out.println("                                "
                + "[-u <PostgreSQL admin user>]");
        m_out.println("                                "
                + "[-p <PostgreSQL admin password>]");
        m_out.println("                                "
                + "[-T <tomcat4.conf>]");
        m_out.println("                                "
                + "[-w <tomcat context directory>");
        m_out.println("                                "
                + "[-C <constraint>]");
        m_out.println("");
        m_out.println(m_required_options);
        m_out.println("");
        m_out.println("   -h    this help");
        m_out.println("");
        m_out.println("   -d    perform database actions");
        m_out.println("   -i    insert data into the database");
        m_out.println("   -s    update iplike postgres function");
        m_out.println("   -U    upgrade database to unicode, if needed");
        m_out.println("   -y    install web application (see -w)");
        m_out.println("");
        m_out.println("   -u    username of the PostgreSQL "
                + "administrator (default: \"" + m_pg_user + "\")");
        m_out.println("   -p    password of the PostgreSQL "
                + "administrator (default: \"" + m_pg_pass + "\")");
        m_out.println("   -c    drop and recreate tables that already "
                + "exist");
        m_out.println("");
        m_out.println("   -T    location of tomcat.conf");
        m_out.println("   -w    location of tomcat's contcxt directory");
        m_out.println("         (usually under conf/Catalina/localhost)");
        m_out.println("");
        m_out.println("   -r    run as an RPM install (does nothing)");
        m_out.println("   -x    turn on debugging for database data "
                + "transformation");
        m_out.println("   -N    ignore NOT NULL constraint checks when "
                + "transforming data");
        m_out.println("         useful after a table is reverted by a "
                + "previous run of the installer");
        m_out.println("   -R    do not revert a table to the original if "
                + "an error occurs when");
        m_out.println("         transforming data -- only used for debugging");
        m_out.println("   -C    fix rows that violate the specified "
                + "constraint -- sets key column in");
        m_out.println("         affected rows to NULL by default");
        m_out.println("   -X    drop rows that violate constraint instead of marking key column in");
        m_out.println("         affected rows to NULL (used with \"-C\")");

        System.exit(0);
    }

    public static void main(String[] argv) throws Exception {
        new Installer().install(argv);
    }

    /**
     * Join all of the elements of a String together into a single string,
     * inserting sep between each element.
     */
    public static String join(String sep, String[] array) {
        StringBuffer sb = new StringBuffer();

        if (array.length > 0) {
            sb.append(array[0]);
        }

        for (int i = 1; i < array.length; i++) {
            sb.append(sep + array[i]);
        }

        return sb.toString();
    }

    public static String join(String sep, List<String> list) {
        StringBuffer sb = new StringBuffer();

        Iterator i = list.iterator();

        if (i.hasNext()) {
            sb.append(i.next());
        }

        while (i.hasNext()) {
            sb.append(sep + i.next());
        }

        return sb.toString();
    }
    
    public static String join(String sep, Object[] array) {
        StringBuffer sb = new StringBuffer();

        if (array.length > 0) {
            sb.append(array[0].toString());
        }

        for (int i = 1; i < array.length; i++) {
            if (array[i] == null) {
                sb.append(sep + "(null)");
            } else {
                sb.append(sep + array[i].toString());
            }
        }

        return sb.toString();
    }


    public String checkServerVersion() throws IOException {
        File catalinaHome = new File(m_webappdir).getParentFile();
        String readmeVersion = getTomcatVersion(new File(catalinaHome,
                                                         "README.txt"));
        String runningVersion = getTomcatVersion(new File(catalinaHome,
                                                          "RUNNING.txt"));

        if (readmeVersion == null && runningVersion == null) {
            return null;
        } else if (readmeVersion != null && runningVersion != null) {
            return readmeVersion; // XXX what should be done here?
        } else if (readmeVersion != null && runningVersion == null) {
            return readmeVersion;
        } else {
            return runningVersion;
        }
    }

    public String getTomcatVersion(File file) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }
        Pattern p = Pattern.compile("The Tomcat (\\S+) Servlet/JSP Container");
        BufferedReader in = new BufferedReader(new FileReader(file));
        for (int i = 0; i < 5; i++) {
            String line = in.readLine();
            if (line == null) { // EOF
                in.close();
                return null;
            }
            Matcher m = p.matcher(line);
            if (m.find()) {
                in.close();
                return m.group(1);
            }
        }

        in.close();
        return null;
    }
    
    public class AutoInteger implements ColumnChangeReplacement {
        private int m_value;
        
        public AutoInteger(int initialValue) {
            m_value = initialValue;
        }
        
        public int getInt() {
            return m_value++;
        }
        
        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) {
            return getInt();
        }

        public boolean addColumnIfColumnIsNew() {
            return true;
        }
    }
    
    public class AutoIntegerIdMapStore implements ColumnChangeReplacement {
        private int m_value;
        private String[] m_indexColumns;
        private Map<MultiColumnKey, Integer> m_idMap =
            new HashMap<MultiColumnKey, Integer>();
        
        public AutoIntegerIdMapStore(int initialValue, String[] indexColumns) {
            m_value = initialValue;
            m_indexColumns = indexColumns;
        }
        
        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            MultiColumnKey key = getKeyForColumns(rs, columnChanges, m_indexColumns);
            Integer newInteger = m_value++;
            m_idMap.put(key, newInteger);
            return newInteger;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        public Integer getIntegerForColumns(ResultSet rs, Map<String, ColumnChange> columnChanges, String[] columns, boolean noMatchOkay) throws SQLException {
            MultiColumnKey key = getKeyForColumns(rs, columnChanges, columns);

            Integer oldInteger = m_idMap.get(key);
            if (oldInteger == null && !noMatchOkay) {
                throw new IllegalArgumentException("No entry in the map for " + key);
            }
            
            return oldInteger;
        }
        
        private MultiColumnKey getKeyForColumns(ResultSet rs, Map<String, ColumnChange> columnChanges, String[] columns) throws SQLException {
            Object[] objects = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) { 
                String indexColumn = columns[i];
                
                ColumnChange columnChange = columnChanges.get(indexColumn);
                if (columnChange == null) {
                    throw new IllegalArgumentException("No ColumnChange entry for '" + indexColumn + "'");
                }
                
                int index = columnChange.getSelectIndex();
                if (index == 0) {
                    throw new IllegalArgumentException("ColumnChange entry for '" + indexColumn + "' has no select index");
                }
                
                objects[i] = rs.getObject(index);
            }

            return new MultiColumnKey(objects);
        }
        
        public class MultiColumnKey {
            private Object[] m_keys;
            
            public MultiColumnKey(Object[] keys) {
                m_keys = keys;
            }
            
            @Override
            public boolean equals(Object otherObject) {
                if (!(otherObject instanceof MultiColumnKey)) {
                    return false;
                }
                MultiColumnKey other = (MultiColumnKey) otherObject;
                
                if (m_keys.length != other.m_keys.length) {
                    return false;
                }
                
                for (int i = 0; i < m_keys.length; i++) {
                    if (m_keys[i] == null && other.m_keys[i] == null) {
                        continue;
                    }
                    if (m_keys[i] == null || other.m_keys[i] == null) {
                        return false;
                    }
                    if (!m_keys[i].equals(other.m_keys[i])) {
                        return false;
                    }
                }
                
                return true;
            }
            
            @Override
            public String toString() {
                return join(", ", m_keys);
            }
            
            @Override
            public int hashCode() {
                int value = 1;
                for (Object o : m_keys) {
                    if (o != null) {
                        // not the other way around, since 1 ^ anything == 1
                        value = o.hashCode() ^ value;
                    }
                }
                return value;
            }
        }
    }
    
    public class MapStoreIdGetter implements ColumnChangeReplacement {
        private AutoIntegerIdMapStore m_storeFoo;
        private String[] m_indexColumns;
        private boolean m_noMatchOkay;
        
        public MapStoreIdGetter(AutoIntegerIdMapStore storeFoo,
                String[] columns, boolean noMatchOkay) {
            m_storeFoo = storeFoo;
            m_indexColumns = columns;
            m_noMatchOkay = noMatchOkay;
        }

        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return m_storeFoo.getIntegerForColumns(rs, columnChanges, m_indexColumns, m_noMatchOkay);
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
    }
    
    public class EventSourceReplacement implements ColumnChangeReplacement {
        private static final String m_replacement = "OpenNMS.Eventd";
        
        public EventSourceReplacement() {
            // we do nothing!
        }

        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return m_replacement;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
    }
    
    public class FixedIntegerReplacement implements ColumnChangeReplacement {
        private Integer m_replacement;
        
        public FixedIntegerReplacement(int value) {
            m_replacement = value;
        }

        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return m_replacement;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
    }
    
    public class RowHasBogusData implements ColumnChangeReplacement {
        private String m_table;
        private String m_column;
        
        public RowHasBogusData(String table, String column) {
            m_table = table;
            m_column = column;
        }
        
        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            throw new IllegalArgumentException("The '" + m_column
                                               + "' column in the '"
                                               + m_table
                                               + "' table should never be "
                                               + "null, but the entry for this "
                                               + "row does have a null '"
                                               + m_column + "'column.  "
                                               + "It needs to be "
                                               + "removed or udpated to "
                                               + "reflect a valid '"
                                               + m_column + "' value.");
        }

        public boolean addColumnIfColumnIsNew() {
            return true;
        }
    }
    
    public class NullReplacement implements ColumnChangeReplacement {
        public NullReplacement() {
            // do nothing
        }
        
        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return null;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
    }
    
    public class DoNotAddColumn implements ColumnChangeReplacement {
        public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            return null;
        }

        public boolean addColumnIfColumnIsNew() {
            return false;
        }

    }
    
    public class NextValReplacement implements ColumnChangeReplacement {
        String m_sequence;
        PreparedStatement m_statement;
        
        public NextValReplacement(String sequence) throws SQLException {
            m_sequence = sequence;
        }
        
        private PreparedStatement getStatement() throws SQLException {
            if (m_statement == null) {
                m_statement = m_dbconnection.prepareStatement("SELECT nextval('" + m_sequence + "')");
            }
            return m_statement;
        }
        
        public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
            ResultSet r = getStatement().executeQuery();
            
            if (!r.next()) {
                r.close();
                throw new SQLException("Query for next value of sequence did not return any rows.");
            }
            
            int i = r.getInt(1);
            r.close();
            return i;
        }
        
        public boolean addColumnIfColumnIsNew() {
            return true;
        }
        
        protected void finalize() throws SQLException {
            if (m_statement != null) {
                m_statement.close();
            }
        }
    }
    
    public boolean triggerExists(String name, String table,
            String storedProcedure) throws Exception {
        
        Statement st = m_dbconnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT oid FROM pg_trigger WHERE tgname = '"
                                       + name.toLowerCase()
                                       + "' AND tgrelid = (SELECT oid FROM pg_class WHERE relname = '"
                                       + table.toLowerCase()
                                       + "' ) AND tgfoid = (SELECT oid FROM pg_proc WHERE proname = '"
                                       + storedProcedure.toLowerCase() + "')");
        
        return rs.next();
    }
}
