 //
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// The code in this file is Copyright (C) 2004 DJ Gregor.
//
// Based on install.pl which was Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

/*
  Big To-dos:
    - Fix all of the XXX items (some coding, some discussion)
    - Change the Exceptions to something more reasonable
    - Do exception handling where it makes sense (give users reasonable error
      messages for common problems)
    - Add a friendly startup script?
    - Javadoc
 */

public class Installer {
    static final float POSTGRES_MIN_VERSION = 7.2f;

    static final String s_version = "$Id$";
    static final int s_fetch_size = 1024;

    boolean m_rpm = false; // XXX only prints out a diagnostic message

    String m_opennms_home = null;

    boolean m_update_database = false;
    boolean m_do_inserts = false;
    boolean m_update_iplike = false;
    boolean m_update_unicode = false;
    boolean m_install_webapp = false;

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
    String m_tomcatserverlibdir = null;
    String m_install_servletdir = null;

    String m_tomcat_serverlibs = null;

    HashMap m_seqmapping = new HashMap();
    LinkedList m_tables = new LinkedList();
    LinkedList m_sequences = new LinkedList();
    // LinkedList m_cfunctions = new LinkedList(); // Unused, not in create.sql
    // LinkedList m_functions = new LinkedList(); // Unused, not in create.sql
    // LinkedList m_languages = new LinkedList(); // Unused, not in create.sql
    LinkedList m_indexes = new LinkedList();
    HashMap m_inserts = new HashMap();
    HashSet m_drops = new HashSet();
    HashSet m_changed = new HashSet();

    float m_pg_version;
    String m_cascade = "";
    LinkedList m_sql_l = new LinkedList();
    String m_sql;

    PrintStream m_out = System.out;

    Properties m_properties = null;
    Connection m_dbconnection;
    Map m_dbtypes = null;

    String m_required_options = "At least one of -d, -i, -s, -U, -y, " +
	"or -T is required.";

    public void install(String[] argv) throws Exception {
	printHeader();
	loadProperties();
	parseArguments(argv);

	if (!m_update_database &&
	    !m_do_inserts &&
	    !m_update_iplike &&
	    !m_update_unicode &&
	    m_tomcat_conf == null &&
	    !m_install_webapp) {
	    throw new Exception("Nothing to do.\n" + m_required_options +
				"\nUse '-h' for help.");
	}
	    
        // Don't bother checking the Java version.  Leave it up to runjava.
	// checkJava();
	// XXX Check Tomcat version?

	if (m_update_database || m_update_iplike || m_update_unicode ||
	    m_do_inserts) {
	    databaseConnect("template1");
	    databaseCheckVersion();
	}

	printDiagnostics();

	verifyFilesAndDirectories();

	if (m_update_database) {
	    readTables();

	    // XXX Check and optionally modify pg_hba.conf

	    if (!databaseUserExists()) {
		databaseAddUser();
	    }
	    if (!databaseDBExists()) {
		databaseAddDB();
	    }
	}

	if (m_update_database || m_update_iplike || m_update_unicode ||
	    m_do_inserts) {
	    databaseDisconnect();
	    
	    databaseConnect(m_database);
	}

	if (m_update_database) {
	    createSequences();
	    createTables();
	    createIndexes();
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

	if (m_update_database) {
	    // XXX should we be using createFunctions and createLanguages
	    //     instead?
	    updatePlPgsql();
       
	    // XXX should we be using createFunctions instead?
	    addStoredProcedures();
	}

	if (m_update_database || m_update_iplike || m_update_unicode ||
	    m_do_inserts) {
	    databaseDisconnect();
	}

	System.out.println();
	System.out.println("Installer completed successfully!");
    }

    public void printHeader() {
	m_out.println("===============================================" +
			   "===============================");
	m_out.println("OpenNMS Installer Version " + s_version);
	m_out.println("===============================================" +
			   "===============================");
	m_out.println("");
	m_out.println("Configures PostgreSQL tables, users, and other " +
			   "miscellaneous settings.");
	m_out.println("");
    }

    public void loadProperties() throws Exception {
	m_properties = new Properties();
	m_properties.load(Installer.class.getResourceAsStream("installer.properties"));

	/* Do this if we want to merge our properties with the system
	   properties...
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
	m_tomcat_serverlibs = fetchProperty("install.tomcat.serverlibs");

	String soext = fetchProperty("build.soext");
	String pg_iplike_dir = fetchProperty("install.postgresql.dir");

	m_pg_iplike = pg_iplike_dir + File.separator + "iplike." + soext;
	m_create_sql = m_sql_dir + File.separator + "create.sql";
    }

    public String fetchProperty(String property) throws Exception {
	String value;

	if ((value = m_properties.getProperty(property)) == null) {
	    throw new Exception("property \"" + property + "\" not set " +
				"from bundled installer.properties file");
	}

	return value;
    }

    public void parseArguments(String[] argv) throws Exception {
	LinkedList args = new LinkedList();

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

		    case 'd':
			m_update_database = true;
			break;
		       
		    case 'i':
			m_do_inserts = true;
			break;

		    case 's':
			m_update_iplike = true;
			break;

		    case 'U':
			m_update_unicode = true;
			break;

		    case 'u':
			i++;
			m_pg_user = getNextArg(argv, i, 'u');
			break;

		    case 'p':
			i++;
			m_pg_pass = getNextArg(argv, i, 'p');
			break;

		    case 'y':
			m_install_webapp = true;
			break;

		    case 'T':
			i++;
			m_tomcat_conf = getNextArg(argv, i, 'T');
			break;

		    case 'w':
			i++;
			m_webappdir = getNextArg(argv, i, 'w');
			break;

		    case 'W':
			i++;
			m_tomcatserverlibdir = getNextArg(argv, i, 'W');
			break;

		    case 'x':
			m_debug = true;
			break;

		    case 'r':
			m_rpm = true;
			break;

		    case 'N':
			m_ignore_notnull = true;
			break;

		    case 'R':
			m_no_revert = true;
			break;

		    default:
			throw new Exception("unknown option '" + c + "'" +
					    ", use '-h' option for usage");
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
	    throw new Exception("no argument provided for '" + letter +
				"' option");
	}
	if (argv[i].charAt(0) == '-') {
	    throw new Exception("argument to '" + letter + "' option looks " +
				"like another option (begins with a dash)");
	}
	return argv[i];
    }

    // Mac OS X: http://developer.apple.com/technotes/tn2002/tn2110.html
    // Don't bother checking.  Leave the check up to "runjava".
   /*	
    public void checkJava() throws Exception {
	String javaVersion = System.getProperty("java.version");
	if (!javaVersion.startsWith("1.4")) {
	    throw new Exception("Need a Java version of 1.4 or higher.  " +
				"You have " + javaVersion);
	}
	// XXX if we only run properly on Sun Java at this time, should
	// XXX we check for that?  Check for specific release verisions?
    }
    */

    public void printDiagnostics() {
	m_out.println("* using '" + m_user + "' as the PostgreSQL " +
			   "user for OpenNMS");
	m_out.println("* using '" + m_pass + "' as the PostgreSQL " +
			   "password for OpenNMS");
	m_out.println("* using '" + m_database + "' as the PostgreSQL " +
			   "database name for OpenNMS");
	if (m_rpm) {
	    m_out.println("* I am being called from an RPM install");
	}
    }

    public void readTables() throws Exception {
	BufferedReader r = new BufferedReader(new FileReader(m_create_sql));
	String line;

	Pattern seqmappingPattern = Pattern.compile("\\s*--#\\s+install:\\s*" +
						    "(\\S+)\\s+(\\S+)\\s+" +
						    "(\\S+)\\s*.*");
	Pattern createPattern = Pattern.compile("(?i)\\s*create\\b.*");
	Pattern insertPattern = Pattern.compile("(?i)INSERT INTO " +
						"[\"']?([\\w_]+)[\"']?.*");
	Pattern dropPattern = Pattern.compile("(?i)DROP TABLE [\"']?" +
					      "([\\w_]+)[\"']?.*");

	while ((line = r.readLine()) != null) {
	    Matcher m;

	    if (line.matches("\\s*") || line.matches("\\s*\\\\.*")) {
		continue;
	    }

	    m = seqmappingPattern.matcher(line);
	    if (m.matches()) {
		String[] a =  { m.group(2), m.group(3) };
		m_seqmapping.put(m.group(1), a);
		continue;
	    }

	    if (line.matches("--.*")) {
		continue;
	    }

	    if (createPattern.matcher(line).matches()) {
		m = Pattern.compile("(?i)\\s*create\\s+((?:unique )?\\w+)" +
				    "\\s+[\"']?(\\w+)[\"']?.*").matcher(line);
		if (m.matches()) {
		    String type = m.group(1);
		    String name = m.group(2).
			replaceAll("^[\"']", "").
			replaceAll("[\"']$", "");
		    
		    if (type.toLowerCase().indexOf("table") != -1) {
			m_tables.add(name);
		    } else if (type.toLowerCase().indexOf("sequence") != -1) {
			m_sequences.add(name);
		    /* -- Not used, nothing in create.sql to get us here
		    } else if (type.toLowerCase().indexOf("function") != -1) {
			if (type.toLowerCase().indexOf("language 'c'") != -1) {
			    m_cfunctions.add(name);
			} else {
			    m_functions.add(name);
			}
		    } else if (type.toLowerCase().indexOf("trusted") != -1) {
			m = Pattern.compile("(?i)\\s*create\\s+trutsed " +
					    "procedural language\\s+[\"']?" +
					    "(\\w+)[\"']?.*").matcher(line);
			if (!m.matches()) {
			    throw new Exception("Could not match name and " +
						"type of the trusted " +
						"procedural language in this" +
						"line: " + line);
			}
			m_languages.add(m.group(1));
		    */
		    } else if (type.toLowerCase().matches(".*\\bindex\\b.*")) {
			m = Pattern.compile("(?i)\\s*create\\s+(?:unique )?" +
					    "index\\s+[\"']?([\\w_]+)" +
					    "[\"']?.*").matcher(line);
			if (!m.matches()) {
			    throw new Exception("Could not match name and " +
						"type of the index " +
						"in this" +
						"line: " + line);
			}
			m_indexes.add(m.group(1));
		    } else {
			throw new Exception("Unknown CREATE encountered: " +
					    "CREATE " + type + " " + name);
		    }
		} else {
		    throw new Exception("Unknown CREATE encountered: " + line);
		}

		m_sql_l.add(line);
		continue;
	    }

	    m = insertPattern.matcher(line);
	    if (m.matches()) {
		String table = m.group(1);
		if (!m_inserts.containsKey(table)) {
		    m_inserts.put(table, new LinkedList());
		}
		((LinkedList) m_inserts.get(table)).add(line);

		continue;
	    }
 
	    if (line.toLowerCase().startsWith("select setval ")) {
		String table = "select_setval";
		if (!m_inserts.containsKey(table)) {
		    m_inserts.put(table, new LinkedList());
		}
		((LinkedList) m_inserts.get(table)).add(line);

		m_sql_l.add(line);
		continue;
	    }

	    m = dropPattern.matcher(line);
	    if (m.matches()) {
		m_drops.add(m.group(1));
		
		m_sql_l.add(line);
		continue;
	    }

	    // XXX should do something here to we can catch what we can't parse
	    // m_out.println("unmatched line: " + line);

	    m_sql_l.add(line);
	}
	r.close();

	m_sql = cleanText(m_sql_l);
    }

    public void databaseConnect(String database) throws Exception {
	Class.forName(m_pg_driver);	
	m_dbconnection = DriverManager.getConnection(m_pg_url + database,
						     m_pg_user,
						     m_pg_pass);
    }

    public void databaseDisconnect() throws Exception {
	if (m_dbconnection != null) {
	    m_dbconnection.close();
	}
    }

    public void databaseCheckVersion() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs = st.executeQuery("SELECT version()");
	if (!rs.next()) {
	    throw new Exception("Database didn't return any rows for " +
				"'SELECT version()'");
	}

	String versionString = rs.getString(1);

	rs.close();
	st.close();

	Matcher m = Pattern.compile("^PostgreSQL (\\d+\\.\\d+)").
	    matcher(versionString);

	if (!m.find()) {
	    throw new Exception("Could not parse version number out of " +
				"version string: " + versionString);
	}
	String version = m.group(1);
	m_pg_version = Float.parseFloat(version);

	if (m_pg_version < POSTGRES_MIN_VERSION) {
	   throw new Exception("Unsupported database version \"" +
		m_pg_version + "\" -- you need at least " +
		POSTGRES_MIN_VERSION);
	}

	if (m_pg_version >= 7.3) {
	    m_cascade = " CASCADE";
	}
    }

    public boolean databaseUserExists() throws Exception {
	boolean exists;

	Statement st = m_dbconnection.createStatement();
	ResultSet rs = st.executeQuery("SELECT usename FROM pg_user WHERE " +
				       "usename = '" + m_user + "'");
	
	exists = rs.next();

	rs.close();
	st.close();

	return exists;
    }

    public void databaseAddUser() throws Exception {
	Statement st = m_dbconnection.createStatement();
	st.execute("CREATE USER " + m_user +
		   " WITH PASSWORD '" + m_pass +
		   "' CREATEDB CREATEUSER");
    }

    public boolean databaseDBExists() throws Exception {
	boolean exists;

	Statement st = m_dbconnection.createStatement();
	ResultSet rs = st.executeQuery("SELECT datname from pg_database " +
				       "WHERE datname = '" + m_database + "'");

	exists = rs.next();

	rs.close();
	st.close();

	return exists;
    }

    public void databaseAddDB() throws Exception {
	Statement st = m_dbconnection.createStatement();
	st.execute("CREATE DATABASE " + m_database +
				  " WITH ENCODING='UNICODE'");
    }

    public void createSequences() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	m_out.println("- creating sequences... ");

	Iterator i = m_sequences.iterator();
	while (i.hasNext()) {
	    String sequence = (String) i.next();
	    if (!m_seqmapping.containsKey(sequence)) {
		throw new Exception("Cannot find sequence mapping for " +
				    sequence);
	    }
	    String[] mapping = (String[]) m_seqmapping.get(sequence);
	}

	i = m_sequences.iterator();
	while (i.hasNext()) {
	    String sequence = (String) i.next();
	    String[] mapping = (String[]) m_seqmapping.get(sequence);
	    int minvalue = 1;
	    boolean remove;

	    m_out.print("  - checking \"" + sequence +
			       "\" minimum value... ");

	    try {
		rs = st.executeQuery("SELECT MAX(" + mapping[0] +
				     ") AS max FROM " + mapping[1]);

		if (rs.next()) {
		    minvalue = rs.getInt(1) + 1;
		}
	    } catch (SQLException e) {
		/*
		  SQL Status codes:
		  42P01: ERROR: relation "%s" does not exist
		  42703: ERROR: column "%s" does not exist
		*/
		if (!e.getSQLState().equals("42P01") &&
		    !e.getSQLState().equals("42703")) {
		    throw e;
		}
	    }

	    m_out.println(Integer.toString(minvalue));
	    
	    m_out.print("  - removing sequence \"" + sequence + "\"... ");

	    rs = st.executeQuery("SELECT relname FROM pg_class " +
				 "WHERE relname = '" +
				 sequence.toLowerCase() + "'");

	    remove = rs.next();
	    if (remove) {
		st.execute("DROP SEQUENCE " + sequence);
		m_out.println("REMOVED");
	    } else {
		m_out.println("CLEAN");
	    }

	    m_out.print("  - creating sequence \"" + sequence + "\"... ");
	    st.execute("CREATE SEQUENCE " + sequence + " minvalue " +
		       minvalue);
	    st.execute("GRANT ALL on " + sequence + " TO " + m_user);
	    m_out.println("OK");
	}

	m_out.println("- creating sequences... DONE");
    }

    public void createTables() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;
	Iterator i = m_tables.iterator();

	m_out.println("- creating tables...");
       
	while (i.hasNext()) {
	    String table = (String) i.next();

	    if (m_force) {
		table = table.toLowerCase();

		String create = getTableFromSQL(table);

		boolean remove;

		rs = st.executeQuery("SELECT relname FROM pg_class " +
				     "WHERE relname = '" + table + "'");

		remove = rs.next();

		m_out.print("  - removing old table... ");
		if (remove) {
		    st.execute("DROP TABLE " + table + m_cascade);
		    m_out.println("REMOVED");
		} else {
		    m_out.println("CLEAN");
		}

		m_out.print("  - creating table \"" + table + "\"... ");
		st.execute("CREATE TABLE " + table + " (" + create + ")");
		m_out.println("CREATED");

		m_out.print("  - giving \"" + m_user +
				 "\" permissions on \"" + table + "\"... ");
		st.execute("GRANT ALL ON " + table + " TO " + m_user);
		m_out.println("GRANTED");
	    } else {
		m_out.print("  - checking table \"" + table + "\"... ");

		table = table.toLowerCase();

		List newColumns = getTableColumnsFromSQL(table);
		List oldColumns = getTableColumnsFromDB(table);

		if (newColumns.equals(oldColumns)) {
		    m_out.println("UPTODATE");
		} else {
		    if (oldColumns.size() == 0) {
			String create = getTableFromSQL(table);
			st.execute("CREATE TABLE " + table +
				   " (" + create + ")");
			st.execute("GRANT ALL ON " + table + " TO " + m_user);
			m_out.println("CREATED");
		    } else {
			changeTable(table, oldColumns, newColumns);
		    }
		}
	    }
	}

	m_out.println("- creating tables... DONE");
    }

    public void createIndexes() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	m_out.println("- creating indexes...");

	Iterator i = m_indexes.iterator();
	while (i.hasNext()) {
	    String index = (String) i.next();
	    boolean exists;

	    m_out.print("  - creating index \"" + index + "\"... ");

	    rs = st.executeQuery("SELECT relname FROM pg_class " +
				 "WHERE relname = '" +
				 index.toLowerCase() + "'");

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
	HashMap m = new HashMap();

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

    /* -- Not used, nothing in create.sql...
    public void createFunctions(List functions) throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;
       
	Iterator i = functions.iterator();
	while (i.hasNext()) {
	    String function = (String) i.next();
	    String functionSql = getFunctionFromSQL(function);
	    Matcher m =
		Pattern.compile("\\s*\\((.+?)\\).*").matcher(functionSql);
	    String columns = m.group(1);

	    if (m_force) {
		// XXX this doesn't check to see if the function exists
		//     before it drops it, so it will fail and throw an
		//     exception if the function doesn't exist.
		m_out.print("- removing function \"" + function +
				 "\" if it exists... ");
		String dropSql = "DROP FUNCTION \"" + function + "\" (" +
		    columns + ");";
		st.execute(dropSql);
		m_out.println("REMOVED");
	    }

	    // XXX this doesn't check to see if the function exists before
	    //     it tries to create it, so it will fail and throw an
	    //     exception if the function does exist.
	    m_out.print("- creating function \"" + function +
			     "\"... ");
	    st.execute("CREATE FUNCTION \"" + function + "\" " + functionSql);
	    m_out.println("OK");
	}
    }

    public void createLanguages() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	Iterator i = m_languages.iterator();
	while (i.hasNext()) {
	    String language = (String) i.next();
	    String languageSql = getLanguageFromSQL(language);

	    // XXX this doesn't check to see if the language exists before
	    //     it tries to create it, so it will fail and throw an
	    //     exception if the language does already exist.
	    m_out.print("- creating language reference \"" + language +
			     "\"... ");
	    st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE '" +
		       language + "' " + languageSql);
	    m_out.println("OK");
	}
    }
    */

    public void fixData() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	st.execute("UPDATE ipinterface SET issnmpprimary='N' " +
		   "WHERE issnmpprimary IS NULL");
	st.execute("UPDATE service SET servicename='SSH' " +
		   "WHERE servicename='OpenSSH'");
	st.execute("UPDATE snmpinterface SET snmpipadentnetmask=NULL");
    }

    // XXX This causes the following Postgres error:
    //      ERROR:  duplicate key violates unique constraint "pk_dpname"
    void insertData() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	for (Iterator i = m_inserts.keySet().iterator(); i.hasNext(); ) {
	    String table = (String) i.next();
	    boolean exists = false;

	    m_out.print("- inserting initial table data for \"" +
			       table + "\"... ");

	    for (Iterator j = ((LinkedList) m_inserts.get(table)).iterator();
		 j.hasNext(); ) {
		try {
		    st.execute((String) j.next());
		} catch (SQLException e) {
		    /*
		      SQL Status codes:
		        23505: ERROR: duplicate key violates unique
			       constraint "%s"
		    */
		    if (e.getSQLState().equals("23505")) {
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

	m_out.print("- checking if database \"" + m_database +
			 "\" is unicode... ");
	
	rs = st.executeQuery("SELECT encoding FROM pg_database WHERE " +
			     "datname='" + m_database.toLowerCase() + "'");
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

	log.println("------------------------------------------------------" +
		    "------------------------");

	m_out.print("  - dumping data to " + dumpFile + "... ");
	String[] cmd1 = {
	    m_pg_bindir + File.separator + "pg_dump",
	    "-U",
	    m_pg_user,
	    "-a",
	    "-D",
	    m_database,
	    "-f",
	    dumpFile
	};
	if ((exitVal = e.exec(cmd1)) != 0) {
	    throw new Exception("Dumping database returned non-zero exit " +
				"value " + exitVal + " while executing " +
				"command '" + join(" ", cmd1) + "', check " +
				logFile);
	}
	m_out.println("OK");

	m_out.print("  - waiting 3s for PostgreSQL to notice " +
			 "that pg_dump has disconnected.");
	Thread.sleep(1000);
	m_out.print(".");
	Thread.sleep(1000);
	m_out.print(".");
	Thread.sleep(1000);
	m_out.println(" OK");

	m_out.print("  - dropping old database... ");
	String[] cmd2 = {
	    m_pg_bindir + File.separator + "dropdb",
	    "-U",
	    m_pg_user,
	    m_database
	};
	if ((exitVal = e.exec(cmd2)) != 0) {
	    throw new Exception("Dropping database returned non-zero exit " +
				"value " + exitVal + " while executing " +
				"command '" + join(" ", cmd2) + "', check " +
				logFile);
	}
	m_out.println("OK");

	m_out.print("  - creating new unicode database... ");
	String[] cmd3 = {
	    m_pg_bindir + File.separator + "createdb",
	    "-U",
	    m_pg_user,
	    "-E",
	    "UNICODE",
	    m_database
	};
	if ((exitVal = e.exec(cmd3)) != 0) {
	    throw new Exception("Creating database returned non-zero exit " +
				"value " + exitVal + " while executing " +
				"command '" + join(" ", cmd3) + "', check " +
				logFile);
	}
	m_out.println("OK");

	m_out.print("  - recreating tables... ");
	String[] cmd4 = {
	    m_pg_bindir + File.separator + "psql",
	    "-U",
	    m_user,
	    "-f",
	    m_sql_dir + File.separator + "create.sql",
	    m_database
	};
	if ((exitVal = e.exec(cmd4)) != 0) {
	    throw new Exception("Recreating tables returned non-zero exit " +
				"value " + exitVal + " while executing " +
				"command '" + join(" ", cmd4) + "', check " +
				logFile);
	}
	m_out.println("OK");

	m_out.print("  - restoring data... ");
	String[] cmd5 = {
	    m_pg_bindir + File.separator + "psql",
	    "-U",
	    m_user,
	    "-f",
	    dumpFile,
	    m_database
	};
	if ((exitVal = e.exec(cmd5)) != 0) {
	    throw new Exception("Restoring data returned non-zero exit " +
				"value " + exitVal + " while executing " +
				"command '" + join(" ", cmd5) + "', check " +
				logFile);
	}
	m_out.println("OK");

	log.close();

	databaseConnect(m_database);
    }

    public void verifyFilesAndDirectories() throws FileNotFoundException {
	if (m_update_database) {
	    verifyFileExists(true,
			     m_sql_dir,
			     "SQL directory",
			     "install.etc.dir property");

	    verifyFileExists(false,
			     m_create_sql,
			     "create.sql",
			     "install.etc.dir property");
	}

	if (m_update_iplike) {
	    verifyFileExists(false,
			     m_pg_iplike,
			     "iplike module",
			     "install.postgresql.dir property");
	}

	if (m_tomcat_conf != null) {
	    verifyFileExists(false,
			     m_tomcat_conf,
			     "Tomcat startup configuration file tomcat4.conf",
			     "-T option");
	}

	if (m_install_webapp) {
	    verifyFileExists(true,
			     m_webappdir,
			     "Top-level web application directory",
			     "-w option");

	    verifyFileExists(true,
			     m_tomcatserverlibdir,
			     "Tomcat server library directory",
			     "-W option");

	    verifyFileExists(true,
			     m_install_servletdir,
			     "OpenNMS servlet directory",
			     "install.servlet.dir property");
	}
    }

    public void verifyFileExists(boolean isDir,
					String file,
					String description,
					String option)
	throws FileNotFoundException {
	File f;

	if (file == null) {
	    throw new FileNotFoundException("The user most provide the " +
					    "location of " + description +
					    ", but this is not specified.  " +
					    "Use the " + option +
					    " to specify this file.");
	}

	m_out.print("- using " + description + "... ");

	f = new File(file);

	if (!f.exists()) {
	    throw new FileNotFoundException(description +
					    " does not exist at \"" + file +
					    "\".  Use the " + option +
					    " to specify another location.");
	}

	if (!isDir) {
	    if (!f.isFile()) {
		throw new FileNotFoundException(description +
						" not a file at \"" + file +
						"\".  Use the " + option +
						" to specify another file.");
	    }
	} else {
	    if (!f.isDirectory()) {
		throw new FileNotFoundException(description +
						" not a directory at \"" +
						file + "\".  Use the " +
						option + " to specify " +
						"another directory.");
	    }
	}

	m_out.println(f.getAbsolutePath());
    }

    public void addStoredProcedures() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	m_out.print("- adding stored procedures... ");

	FileFilter sqlFilter = new FileFilter() {
		public boolean accept(File pathname) {
		    return (pathname.getName().startsWith("get") &&
			    pathname.getName().endsWith(".sql"));
		}
	    };

	File[] list = new File(m_sql_dir).listFiles(sqlFilter);

	for (int i = 0; i < list.length; i++) {
	    LinkedList drop = new LinkedList();
	    StringBuffer create = new StringBuffer();
	    String line;
	    
	    m_out.print("\n  - " + list[i].getName() + "... ");
	    
	    BufferedReader r = new BufferedReader(new FileReader(list[i]));
	    while ((line = r.readLine()) != null) {
		line = line.trim();

		if (line.matches("--.*")) {
		    continue;
		}

		if (line.toLowerCase().startsWith("drop function")) {
		    drop.add(line);
		} else {
		    create.append(line);
		    create.append("\n");
		}
	    }
	    r.close();

	    // XXX install.pl has code to drop the functions, however I think
	    //     it is buggy and it would have ever droopped a function.
	    //     The supposed buggy code is not implemented here.
	
	    Matcher m = Pattern.compile("(?is)\\bCREATE FUNCTION\\s+" +
					"(\\w+)\\s*\\((.+?)\\)\\s+" +
					"RETURNS\\s+(\\S+)\\s+AS\\s+" +
					"(.+? language ['\"]?\\w+['\"]?);").
		matcher(create.toString());

	    if (!m.find()) {
		throw new Exception("Could match \"" + m.pattern().pattern() +
				    "\" in string \"" + create + "\"");
	    }
	    String function = m.group(1);
	    String columns = m.group(2);
	    String returns = m.group(3);
	    String rest = m.group(4);

	    if (functionExists(function, columns, returns)) {
		if (m_force) {
		    st.execute("DROP FUNCTION " +
			       function + "(" + columns + ")");
		    st.execute(create.toString());
		    m_out.print("OK (dropped and re-added)");
		} else {
		    m_out.print("EXISTS");
		}
	    } else {
		st.execute(create.toString());
		m_out.print("OK");
	    }
	}
	m_out.println("");
    }

    public boolean functionExists(String function, String columns,
				  String returnType) throws Exception {
	Map types = getTypesFromDB();

	String[] splitColumns = columns.split(",");
	int[] columnTypes = new int[splitColumns.length];
	Column c;
	for (int j = 0; j < splitColumns.length; j++) {
	    c = new Column();
	    c.parseColumnType(splitColumns[j]);
	    columnTypes[j] = ((Integer)types.get(c.getType())).intValue();
	}

	c = new Column();
	c.parseColumnType(returnType);
	int retType = ((Integer)types.get(c.getType())).intValue();
	
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

	String query = "SELECT oid FROM pg_proc WHERE proname='" +
			     function.toLowerCase() + "' AND " +
			     "prorettype=" + retType + " AND " +
			     "proargtypes='" + ct.toString().trim() + "'";

	rs = st.executeQuery(query);
	return rs.next();
    }

    public void installWebApp() throws Exception {
	String[] jars = m_tomcat_serverlibs.split(File.pathSeparator);

	m_out.println("- Install OpenNMS webapp... ");

	installLink(m_install_servletdir,
		    m_webappdir + File.separator + "opennms",
		    "web application directory", true);

	for (int i = 0; i < jars.length; i++) {
	    String source = ".." + File.separator + ".." + File.separator +
		"webapps" + File.separator + "opennms" +
		File.separator + "WEB-INF" + File.separator +
		"lib" + File.separator + jars[i];
	    String destination = m_tomcatserverlibdir + File.separator +
		jars[i];
	    installLink(source, destination, "jar file " + jars[i], false);
	}

	m_out.println("- Installing OpenNMS webapp... DONE");
    }

    public void installLink(String source, String destination, 
			    String description, boolean recursive)
	throws Exception {

	File f;
	String[] cmd;
	ProcessExec e = new ProcessExec(m_out, m_out);
	int exists;

	if (new File(destination).exists()) {
	    m_out.print("  - " + destination + " exists, removing... ");
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
		throw new Exception("Non-zero exit value returned while " +
				    "removing " + description + ", " +
				    destination + ", using \"" +
				    join(" ", cmd) + "\"");
	    }

	    if (new File(destination).exists()) {
		throw new Exception("Could not delete existing " +
				    description + ": " +
				    destination);
	    }

	    m_out.println("REMOVED");
	}
	
	m_out.print("  - creating link to " + destination + "... ");

	cmd = new String[4];
	cmd[0] = "ln";
	cmd[1] = "-sf";
	cmd[2] = source;
	cmd[3] = destination;
	
	if (e.exec(cmd) != 0) {
	    throw new Exception("Non-zero exit value returned while " +
				"linking " + description + ", " +
				source + " into " + destination);
	}

	m_out.println("DONE");
    }

    public void updateTomcatConf() throws Exception {
	File f = new File(m_tomcat_conf);

	// XXX give the user the option to set the user to something else?
	//     if so, should we chown the appropriate OpenNMS files to the
	//     tomcat user?
	//
	// XXX should we have the option to automatically try to determine
	//     the tomcat user and chown the OpenNMS files to that user?

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

	f.renameTo(new File(m_tomcat_conf + ".before-opennms-" +
			    System.currentTimeMillis()));

	f = new File(m_tomcat_conf);
	PrintWriter w = new PrintWriter(new FileOutputStream(f));

	w.print(b.toString());
	w.close();

	m_out.println("done");
    }

    public void updateIplike() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	m_out.print("- checking for stale iplike references... ");
	try {
	    st.execute("DROP FUNCTION iplike(text,text)");
	    m_out.println("REMOVED");
	} catch (SQLException e) {
	    /*
	      SQL Status code:
	        42883: ERROR: function %s does not exist
	     */
	    if (e.getSQLState().equals("42883")) {
		m_out.println("CLEAN");
	    } else {
		throw e;
	    }
	}

	// XXX This error is generated from Postgres if eventtime(text)
	//     does not exist:
	//        ERROR:  function eventtime(text) does not exist
	m_out.print("- checking for stale eventtime.so references... ");
	try {
	    st.execute("DROP FUNCTION eventtime(text)");
	    m_out.println("REMOVED");
	} catch (SQLException e) {
	    /*
	      SQL Status code:
	        42883: ERROR: function %s does not exist
	     */
	    if (e.getSQLState().equals("42883")) {
		m_out.println("CLEAN");
	    } else {
		throw e;
	    }
	}

	m_out.print("- adding iplike database function... ");
	st.execute("CREATE FUNCTION iplike(text,text) RETURNS bool " +
		   "AS '" + m_pg_iplike +
		   "' LANGUAGE 'c' WITH(isstrict)");
	m_out.println("OK");
    }

    public void updatePlPgsql() throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;

	m_out.print("- adding PL/pgSQL call handler... ");
	rs = st.executeQuery("SELECT oid FROM pg_proc WHERE " +
			     "proname='plpgsql_call_handler' AND " +
			     "proargtypes = ''");
	if (rs.next()) {
	    m_out.println("EXISTS");
	} else {
	    st.execute("CREATE FUNCTION plpgsql_call_handler () " +
		       "RETURNS OPAQUE AS '$libdir/plpgsql.so' LANGUAGE 'c'");
	    m_out.println("OK");
	}

	m_out.print("- adding PL/pgSQL language module... ");
	rs = st.executeQuery("SELECT pg_language.oid FROM " +
			     "pg_language, pg_proc WHERE " +
			     "pg_proc.proname='plpgsql_call_handler' AND " +
			     "pg_proc.proargtypes = '' AND " +
			     "pg_proc.oid = pg_language.lanplcallfoid AND " +
			     "pg_language.lanname = 'plpgsql'");
	if (rs.next()) {
	    m_out.println("EXISTS");
	} else {
	    st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' " +
		       "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
	    m_out.println("OK");
	}
    }

    public Column findColumn(List columns, String column) {
	Column c;

	for (Iterator i = columns.iterator(); i.hasNext(); ) {
	    c = (Column) i.next();
	    if (c.getName().equals(column.toLowerCase())) {
		return c;
	    }
	}

	return null;
    }

    public String getXFromSQL(String item, String regex, int itemGroup,
			      int returnGroup, String description)
	throws Exception {

	item = item.toLowerCase();
	Matcher m = Pattern.compile(regex).matcher(m_sql);

	while (m.find()) {
	    if (m.group(itemGroup).toLowerCase().equals(item)) {
		return m.group(returnGroup);
	    }
	}

	throw new Exception("could not find " + description +
			    " \"" + item + "\"");
    }

    public String getTableFromSQL(String table) throws Exception {
	return getXFromSQL(table,
			   "(?i)\\bcreate table\\s+['\"]?(\\S+)['\"]?" +
			   "\\s+\\((.+?)\\);", 1, 2, "table");
    }
    
    public String getIndexFromSQL(String index) throws Exception {
	return getXFromSQL(index, "(?i)\\b(create (?:unique )?index\\s+" +
			   "['\"]?(\\S+)['\"]?\\s+.+?);", 2, 1, "index");
    }
    
    public String getFunctionFromSQL(String function) throws Exception {
	return getXFromSQL(function, "(?is)\\bcreate function\\s+" +
			   "['\"]?(\\S+)['\"]?\\s+" +
			   "(.+? language ['\"]?\\w+['\"]?);", 1, 2,
			   "function");
    }
    
    public String getLanguageFromSQL(String language) throws Exception {
	return getXFromSQL(language,
			   "(?is)\\bcreate trusted procedural " +
			   "language\\s+['\"]?(\\S+)['\"]?\\s+(.+?);",
			   1, 2, "language");
    }

    public List getTableColumnsFromSQL(String table) throws Exception {
	String create = getTableFromSQL(table);
	LinkedList columns = new LinkedList();
	boolean parens = false;
	StringBuffer accumulator = new StringBuffer();
	Matcher m;

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
		    if (columns.size() == 0) {
			throw new Exception("found constraint with no " +
					    "previous column");
		    }

		    Constraint constraint = new Constraint(a);
		    Column lastcol = (Column) columns.getLast();
		    if (!constraint.getColumn().equals(lastcol.getName())) {
			throw new Exception("constraint does not " +
					    "reference previous column (" +
					    lastcol.getName() + "): " +
					    constraint);
		    }
		    lastcol.addConstraint(constraint);
		} else {
		    Column column = new Column();
		    column.parse(accumulator.toString());
		    columns.add(column);
		}

		accumulator = new StringBuffer();
	    } else {
		accumulator.append(c);
	    }
	}

	return columns;
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

	rs = st.executeQuery("SELECT DISTINCT tablename FROM pg_tables " +
			     "WHERE lower(tablename) = '" +
			     table.toLowerCase() + "'");
	return rs.next();
    }

    public List getTableColumnsFromDB(String table) throws Exception {
	Statement st = m_dbconnection.createStatement();
	ResultSet rs;
	LinkedList r = new LinkedList();

	if (!tableExists(table)) {
	    return r;
	}

	String query = 
	    "SELECT " +
	    "        attname, " +
	    "        format_type(atttypid, atttypmod), " +
	    "        attnotnull " +
	    "FROM " +
	    "        pg_attribute " +
	    "WHERE " +
	    "        attrelid = " +
	    "                (SELECT oid FROM pg_class WHERE relname = '" +
	                          table.toLowerCase() + "') AND " +
	    "        attnum > 0 ";

	if (m_pg_version >= 7.3) {
	    query = query + "AND attisdropped = false ";
	}

	query = query +
	    "ORDER BY " +
	    "        attnum";

	rs = st.executeQuery(query);

	while (rs.next()) {
	    Column c = new Column();
	    c.setName(rs.getString(1));
	    c.parseColumnType(rs.getString(2));
	    c.setNotNull(rs.getBoolean(3));

	    r.add(c);
	}

	if (m_pg_version > 7.3) {
	    // XXX the [1] on conkey and confkey is a hack and assumes that
	    //     we have at most one constrained column and at most one
	    //     referenced foreign column (which is correct with the current
	    //     database layout.
	    query =
		"SELECT " +
		"       c.conname, " +
		"	c.contype, " +
		"	c.confdeltype, " +
		"	a.attname, " +
		"	d.relname, " +
		"	b.attname " +
		"FROM " +
		"	pg_class d RIGHT JOIN " +
		"	  (pg_attribute b RIGHT JOIN " +
		"	    (pg_constraint c JOIN pg_attribute a " +
		"	      ON c.conrelid = a.attrelid AND " +
		"	         a.attnum = c.conkey[1]) " +
		"	    ON c.confrelid = b.attrelid AND " +
		"	       b.attnum = c.confkey[1]) " +
		"	  ON b.attrelid = d.oid " +
		"WHERE " +
		"	a.attrelid = " +
	        "         (SELECT oid FROM pg_class WHERE relname = '" +
		table.toLowerCase() + "');";

	    rs = st.executeQuery(query);

	    while (rs.next()) {
		Constraint constraint;
		if (rs.getString(2).equals("p")) {
		    constraint = new Constraint(rs.getString(1),
						rs.getString(4));
		} else if (rs.getString(2).equals("f")) {
		    constraint = new Constraint(rs.getString(1),
						rs.getString(4),
						rs.getString(5),
						rs.getString(6),
						rs.getString(3));
		} else {
		    throw new Exception("Do not support constraint type \"" +
					rs.getString(2) + "\" in constraint \"" +
					rs.getString(1) + "\"");
		}

		Column c = findColumn(r, constraint.getColumn());
		if (c == null) {
		    throw new Exception("Got a constraint for column \"" +
					constraint.getColumn() + "\" of table " +
					table + ", but could not find column.  " +
					"Constraint: " + constraint);
		}

		c.addConstraint(constraint);
	    }
	} else {
	    query =
		"SELECT " +
		"        c.relname, " +
		"        a.attname " +
		"FROM " +
		"        pg_index i, " +
		"        pg_class c, " + 
		"        pg_attribute a " +
		"WHERE " +
		"        i.indrelid = " +
		"          (SELECT oid FROM pg_class WHERE relname = '" +
		             table.toLowerCase() + "') AND " +
		"        i.indisprimary = 't' AND " +
		"        i.indrelid = a.attrelid AND " +
		"        i.indkey[0] = a.attnum AND " +
		"        i.indexrelid = c.relfilenode";

	    rs = st.executeQuery(query);
	    while (rs.next()) {
		Constraint constraint = 
		    new Constraint(rs.getString(1), rs.getString(2));

		Column c = findColumn(r, constraint.getColumn());
		if (c == null) {
		    throw new Exception("Got a constraint for column \"" +
					constraint.getColumn() + "\" of table " +
					table + ", but could not find column.  " +
					"Constraint: " + constraint);
		}

		c.addConstraint(constraint);
	    }

	    int fkey, fdel;

	    query = "SELECT oid FROM pg_proc WHERE proname = " +
		"          'RI_FKey_check_ins'";
	    rs = st.executeQuery(query);
	    if (!rs.next()) {
		throw new Exception("Could not get OID for RI_FKey_check_ins");
	    }
	    fkey = rs.getInt(1);

	    query = "SELECT oid FROM pg_proc WHERE proname = " +
		"          'RI_FKey_cascade_del'";
	    rs = st.executeQuery(query);
	    if (!rs.next()) {
		throw new Exception("Could not get OID for RI_FKey_cascade_del");
	    }
	    fdel = rs.getInt(1);

	    query =
		"SELECT " +
		"        tgconstrname, " +
		"        tgargs, " + 
		"        tgfoid " + 
		"FROM " +
		"        pg_trigger " +
		"WHERE " +
		"        ( " +
		"          tgrelid = " +
		"            (SELECT oid FROM pg_class WHERE relname = '" +
		               table.toLowerCase() + "') AND " +
		"            tgfoid = " + fkey + " " +
		"        ) OR ( " +
		"          tgconstrrelid = " +
		"            (SELECT oid FROM pg_class WHERE relname = '" +
		               table.toLowerCase() + "') AND " +
		"            tgfoid = " + fdel + " " +
		"        ) ";
	    rs = st.executeQuery(query);

	    while (rs.next()) {
		String name = rs.getString(1);
		String[] args = new String(rs.getBytes(2)).split("\000");

		Constraint constraint = 
		    new Constraint(rs.getString(1), args[4],
				   args[2], args[5],
				   (rs.getInt(3) == fkey) ? "a" : "c");

		Column c = findColumn(r, constraint.getColumn());
		if (c == null) {
		    throw new Exception("Got a constraint for column \"" +
					constraint.getColumn() + "\" of table " +
					table + ", but could not find column.  " +
					"Constraint: " + constraint);
		}

		boolean found = false;
		ListIterator i = c.getConstraints().listIterator();
		while (i.hasNext()) {
		    Constraint constraint_o = (Constraint) i.next();
		    if (constraint.equals(constraint_o, true)) {
			found = true;
			if (constraint.getForeignDelType().equals("c")) {
			    i.set(constraint);
			}
		    }
		}

		if (!found) {
		    c.addConstraint(constraint);
		}
	    }
	}

	return r;
    }

/*  XXX unused
    public static String[] split(String s, String split) {
	int i, j;
	LinkedList l = new LinkedList();

	i = 0;

	while ((j = s.indexOf(split, i)) != -1) {
	    l.add(s.substring(i, j));
	    i = j + split.length();
	}

	if (i < s.length()) {
	    l.add(s.substring(i));
	}
	return (String[]) l.toArray(new String[0]);
    }
*/

    public void changeTable(String table, List oldColumns, List newColumns)
	throws Exception {
	Statement st = m_dbconnection.createStatement();
	TreeMap columnChanges = new TreeMap();
	String[] oldColumnNames = new String[oldColumns.size()];

	int i;
	Iterator j;
      
	if (m_changed.contains(table)) {
	    return;
	}
	m_changed.add(table);

	m_out.println("SCHEMA DOES NOT MATCH");
	m_out.println("    - differences:");

	// XXX This doesn't check for old column rows that don't exist
	//     in newColumns.
	for (j = newColumns.iterator(); j.hasNext(); ) {
	    Column newColumn = (Column) j.next();
	    Column oldColumn = findColumn(oldColumns,
					  newColumn.getName());

	    if (oldColumn == null || !newColumn.equals(oldColumn)) {
		m_out.println("      - column \"" + newColumn.getName() +
				   "\" is different");
		if (m_debug) {
		    m_out.println("        - old column: " +
				  ((oldColumn == null) ?
				   "null" : oldColumn.toString()));
		    m_out.println("        - new column: " + newColumn);
		}
	    }

	    if (!columnChanges.containsKey(newColumn.getName())) {
		columnChanges.put(newColumn.getName(), new ColumnChange());
	    }

	    ColumnChange columnChange = (ColumnChange)
		columnChanges.get(newColumn.getName());
	    columnChange.setColumn(newColumn);

	    /*
	     * If the new column has a NOT NULL constraint, set a null
	     * replace value for the column.  Throw an exception if it
	     * is possible for null data to be inserted into the new
	     * column.  This would happen if there is not a null
	     * replacement and the column either didn't exist before or
	     * it did NOT have the NOT NULL constraint before.
	     */
	    if (newColumn.isNotNull()) {
		if (newColumn.getName().equals("eventsource")) {
		    columnChange.setNullReplace("OpenNMS.Eventd");
		} else if (newColumn.getName().equals("svcregainedeventid")
			   && table.equals("outages")) {
		    columnChange.setNullReplace(new Integer(0));
		} else if (newColumn.getName().equals("eventid") &&
			   table.equals("notifications")) {
		    columnChange.setNullReplace(new Integer(0));
		} else if (oldColumn == null) {
		    String message = "Column " + newColumn.getName() +
			" in new table has NOT NULL " +
			"constraint, however this column " +
			"did not exist before and there is " +
			"no null replacement for this " +
			"column";
		    if (m_ignore_notnull) {
			m_out.println(message + ".  Ignoring due to '-N'");
		    } else {
			throw new Exception(message);
		    }
		} else if (!oldColumn.isNotNull()) {
		    String message = "Column " + newColumn.getName() +
			" in new table has NOT NULL " +
			"constraint, however this column " +
			"did not have the NOT NULL " +
			"constraint before and there is " +
			"no null replacement for this " +
			"column";
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
		ColumnChange columnChange = (ColumnChange)
		    columnChanges.get(oldColumn.getName());
		Column newColumn = (Column) columnChange.getColumn();
		if (newColumn.getType().indexOf("timestamp") != -1) {
		    columnChange.setUpgradeTimestamp(true);
		}
	    } else {
		m_out.println("      * WARNING: column \"" +
				   oldColumn.getName() + "\" exists in the " +
				   "database but is not in the new schema.  " +
				   "REMOVING COLUMN");
	    }
	}

	String tmpTable = table + "_old_" + System.currentTimeMillis();

	try {
	    if (tableExists(tmpTable)) {
		st.execute("DROP TABLE " + tmpTable + m_cascade);
	    }

	    m_out.print("    - creating temporary table... ");
	    st.execute("CREATE TABLE " + tmpTable + " AS SELECT " +
		       join(", ", oldColumnNames) +
		       " FROM " + table);
	    m_out.println("done");

	    st.execute("DROP TABLE " + table + m_cascade);

	    m_out.print("    - creating new '" + table + "' table... ");
	    st.execute("CREATE TABLE " + table + "(" + getTableFromSQL(table) +
		       ")");
	    m_out.println("done");

	    transformData(table, tmpTable, columnChanges, oldColumnNames);

	    st.execute("GRANT ALL ON " + table + " TO " + m_user);

	    m_out.print("    - optimizing table " + table + "... ");
	    st.execute("VACUUM ANALYZE " + table);
	    m_out.println("DONE");
	} catch (Exception e) {
	    if (m_no_revert) {
		m_out.println("FAILED!  Not reverting due to '-R' being " +
			      "passed.  Old data in " + tmpTable);
		throw e;
	    }

	    try {
		m_dbconnection.rollback();
		m_dbconnection.setAutoCommit(true);
	    
		if (tableExists(table)) {
		    st.execute("DROP TABLE " + table + m_cascade);
		}
		st.execute("CREATE TABLE " + table + " AS SELECT " +
			   join(", ", oldColumnNames) +
			   " FROM " + tmpTable);
		st.execute("DROP TABLE " + tmpTable);
	    } catch (SQLException se) {
		throw new Exception("Got SQLException while trying to " +
				    "revert table changes due to original " +
				    "error: " + e + "\n" +
				    "SQLException while reverting table: " +
				    se, e);
	    }
	    m_out.println("FAILED!  Old data restored, however indexes and " +
			  "constraints on this table were not re-added");
	    throw e;
	}

	// We don't care if dropping the tmp table fails since we've
	// completed copying it, so it's outside of the try/catch block above.
	st.execute("DROP TABLE " + tmpTable);

	m_out.println("    - completed updating table... ");
    }

    public void transformData(String table, String oldTable,
			      TreeMap columnChanges, String[] oldColumnNames)
	throws SQLException, ParseException, Exception {
	Statement st = m_dbconnection.createStatement();
	Iterator j;
	int i;

	st.setFetchSize(s_fetch_size);

	String[] columns = (String[])
	    columnChanges.keySet().toArray(new String[0]);
	String[] questionMarks = new String[columns.length];

	for (i = 0; i < oldColumnNames.length; i++) {
	    ColumnChange c = (ColumnChange)
		columnChanges.get(oldColumnNames[i]);
	    if (c != null) {
	        c.setSelectIndex(i + 1);
	    }
	}

	for (i = 0; i < columns.length; i++) {
	    questionMarks[i] = "?";
	    ColumnChange c = (ColumnChange) columnChanges.get(columns[i]);
	    c.setPrepareIndex(i + 1);
	    c.setColumnType(((Column) c.getColumn()).getColumnSqlType());
	}

	/* Pull everything in from the old table and filter it to update
	   the data to any new formats. */

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

	String dbcmd = "SELECT " + join(", ", oldColumnNames) + " FROM " +
	    oldTable + order;
	if (m_debug) {
	    m_out.println("    - performing select: " + dbcmd);
	}
	select = m_dbconnection.prepareStatement(dbcmd);
    select.setFetchSize(s_fetch_size);
	// error =		      "Unable to prepare select from temp";

	dbcmd = "INSERT INTO " + table + " (" + join(", ", columns) +
	    ") values (" + join(", ", questionMarks) + ")";
	if (m_debug) {
	    m_out.println("    - performing insert: " + dbcmd);
	}
	insert = m_dbconnection.prepareStatement(dbcmd);
	// error = 	      "Unable to prepare insert into " + table);

	rs = select.executeQuery();
	m_dbconnection.setAutoCommit(false);

	String name;
	ColumnChange change;
	Object obj;
	SimpleDateFormat dateParser =
	    new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	SimpleDateFormat dateFormatter =
	    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	char spin[] = { '/', '-', '\\', '|' };

	int current_row = 0;

	while (rs.next()) {
	    for (j = columnChanges.keySet().iterator(); j.hasNext(); ) {
		name = (String) j.next();
		change = (ColumnChange) columnChanges.get(name);
		
		if (change.getSelectIndex() > 0) {
		    obj = rs.getObject(change.getSelectIndex());
		    if (rs.wasNull()) {
			obj = null;
		    }
		} else {
		    if (m_debug) {
			m_out.println("      - don't know what to do " +
					   "for \"" +
					   name + "\", prepared column " +
					   change.getPrepareIndex() +
					   ": setting to null");
		    }
		    obj = null;
		}

		if (table.equals("outages") && name.equals("outageid")) {
		    obj = new Integer(current_row + 1);
		}
		if (obj == null && change.isNullReplace()) {
		    obj = change.getNullReplace();
		    if (m_debug) {
			m_out.println("      - " + name +
					   " was NULL but is a " +
					   "requires NULL replacement -- " +
					   "replacing with '" + obj + "'");
		    }
		}

		if (obj != null) {
		    if (change.isUpgradeTimestamp() &&
			!obj.getClass().equals(java.sql.Timestamp.class)) {
			if (m_debug) {
			    m_out.println("      - " + name +
					       " is an old-style timestamp");
			}
			String newObj =
			    dateFormatter.format(dateParser.parse((String)
								  obj));
			if (m_debug) {
			    m_out.println("      - " +
					       obj + " -> " + newObj);
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
		SQLException ex =
		    new SQLException("Statement.execute() threw an " +
				     "SQLException while inserting a row: " +
				     "\"" + insert.toString() + "\".  " +
				     "Original exception: " + e.toString(),
				     e.getSQLState(),
				     e.getErrorCode());
		ex.setNextException(e);
		throw ex;
	    }

	    current_row++;

	    if ((current_row % 20) == 0) {
		System.err.print("    - transforming data into the new " +
				 "table... " +
				 (int)Math.floor((current_row * 100) / num_rows) +
				 "%  [" + spin[(current_row / 20) % spin.length] + "]\r");
	    }
	}

	m_dbconnection.commit();
	m_dbconnection.setAutoCommit(true);


	if (table.equals("events") && num_rows == 0) {
	    st.execute("INSERT INTO events (eventid, eventuei, eventtime, " +
		       "eventsource, eventdpname, eventcreatetime, " +
		       "eventseverity, eventlog, eventdisplay) values " +
		       "(0, 'http://uei.opennms.org/dummyevent', now(), " +
		       "'OpenNMS.Eventd', 'localhost', now(), 1, 'Y', 'Y')");
	}


	m_out.println("    - transforming data into the new table... " +
			   "DONE           ");
    }

    public void printHelp() {
	m_out.println("usage:");
	m_out.println("  java -jar opennms_install.jar -h");
	m_out.println("  java -jar opennms_install.jar " +
		      "[-r] [-x] [-N] [-R] [-c] [-d] [-i] [-s] [-U] [-y]");
	m_out.println("                                " +
		      "[-u <PostgreSQL admin user>]");
	m_out.println("                                " +
		      "[-p <PostgreSQL admin password>]");
	m_out.println("                                " +
		      "[-T <tomcat4.conf>]");
	m_out.println("                                " +
		      "[-w <tomcat webapps directory>");
	m_out.println("                                " +
		      "[-W <tomcat server/lib directory>]");
	m_out.println("");
	m_out.println(m_required_options);
	m_out.println("");
	m_out.println("   -h    this help");
	m_out.println("");
	m_out.println("   -d    perform database actions");
	m_out.println("   -i    insert data into the database");
	m_out.println("   -s    update iplike postgres function");
	m_out.println("   -U    upgrade database to unicode, if needed");
	m_out.println("   -y    install web application (see -w and -W)");
	m_out.println("");
	m_out.println("   -u    username of the PostgreSQL " +
		      "administrator (default: \"" + m_pg_user + "\")");
	m_out.println("   -p    password of the PostgreSQL " +
		      "administrator (default: \"" + m_pg_pass + "\")");
	m_out.println("   -c    drop and recreate tables that already " +
		      "exist");
	m_out.println("");
	m_out.println("   -T    location of tomcat.conf");
	m_out.println("   -w    location of tomcat's webapps directory");
	m_out.println("   -W    location of tomcat's server/lib " +
		      "directory");
	m_out.println("");
	m_out.println("   -r    run as an RPM install (does nothing)");
	m_out.println("   -x    turn on debugging for database data " +
		      "transformation");
	m_out.println("   -N    ignore NOT NULL constraint checks when " +
		      "transforming data");
	m_out.println("         useful after a table is reverted by a " +
		      "previous run of the installer");
	m_out.println("   -R    do not revert a table to the original if " +
		      "an error occurs when");
	m_out.println("         transforming data -- only used for debugging");

	System.exit(0);
    }

    public static void main(String[] argv) throws Exception {
	new Installer().install(argv);
    }

    /**
     * Join all of the elements of a String together into a single
     * string, inserting sep between each element.
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
}
