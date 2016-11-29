/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.db.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class InstallerDb {

    private static final String IPLIKE_SQL_RESOURCE = "iplike.sql";

    public static final float POSTGRES_MIN_VERSION = 9.1f;
    public static final float POSTGRES_MAX_VERSION_PLUS_ONE = 9.9f;

    private static final int s_fetch_size = 1024;
    
    private static Comparator<Constraint> constraintComparator = new Comparator<Constraint>() {

                @Override
		public int compare(final Constraint o1, final Constraint o2) {
			return o1.getName().compareTo(o2.getName());
		}
    	
    }; 
    
    private final IndexDao m_indexDao = new IndexDao();

    private final TriggerDao m_triggerDao = new TriggerDao();

    private DataSource m_dataSource = null;
    
    private DataSource m_adminDataSource = null;
    
    private PrintStream m_out = System.out;

    private boolean m_debug = false;

    private String m_createSqlLocation = null;
    
    private String m_storedProcedureDirectory = null;
    
    private String m_databaseName = null;
    
    private String m_pg_iplike = null;
    
    private String m_pg_plpgsql = null;
    
    private final Map<String, ColumnChangeReplacement> m_columnReplacements = new HashMap<String, ColumnChangeReplacement>();

    private String m_sql;

    private List<String> m_tables = null;

    private List<String> m_sequences = null;

    private final Map<String, List<Insert>> m_inserts = new HashMap<String, List<Insert>>();

    private final Set<String> m_drops = new HashSet<String>();

    private final Set<String> m_changed = new HashSet<String>();
    
    private Map<String, Integer> m_dbtypes = null;
    
    private Map<String, String[]> m_seqmapping = null;
    private Connection m_connection;
    private Connection m_adminConnection;
    private String m_user;
    
    private boolean m_force = false;
    
    private boolean m_ignore_notnull = false;
    
    private boolean m_no_revert = false;

	private final Pattern m_seqmappingPattern = Pattern.compile("\\s*--#\\s+install:\\s*"
	        + "(\\S+)\\s+(\\S+)\\s+(\\S+)\\s*.*");

	private final Pattern m_createPattern = Pattern.compile("(?i)\\s*create\\b.*");

	private final Pattern m_criteriaPattern = Pattern.compile("\\s*--#\\s+criteria:\\s*(.*)");

	private final Pattern m_insertPattern = Pattern.compile("(?i)INSERT INTO "
	        + "[\"']?([\\w_]+)[\"']?.*");

	private final Pattern m_dropPattern = Pattern.compile("(?i)DROP TABLE [\"']?"
	        + "([\\w_]+)[\"']?.*");

	private final Pattern m_emptyLine = Pattern.compile("^\\s*(\\\\.*)?$");

	private final Pattern m_createUnique = Pattern.compile("(?i)\\s*create\\s+((?:unique )?\\w+)\\s+[\"']?(\\w+)[\"']?.*");

	private final Pattern m_createLanguagePattern = Pattern.compile("(?i)\\s*create\\s+trusted procedural language\\s+[\"']?(\\w+)[\"']?.*");

	private final FileFilter m_sqlFilter = new FileFilter() {
            @Override
	    public boolean accept(final File pathname) {
	        return (pathname.getName().startsWith("get") && pathname.getName().endsWith(".sql"))
	             || pathname.getName().endsWith("Trigger.sql");
	    }
	};

	private final Pattern m_createFunction = Pattern.compile("(?is)\\b(CREATE(?: OR REPLACE)? FUNCTION\\s+"
	                                    + "(\\w+)\\s*\\((.*?)\\)\\s+"
	                                    + "RETURNS\\s+(\\S+)\\s+AS\\s+"
	                                    + "(.+? language ['\"]?\\w+['\"]?);)");

	private final SimpleDateFormat m_dateParser = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

	private final SimpleDateFormat m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final char[] m_spins = { '/', '-', '\\', '|' };

    /**
     * <p>Constructor for InstallerDb.</p>
     */
    public InstallerDb() {
        
    }
    
    /**
     * <p>readTables</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void readTables() throws Exception {
        readTables(new InputStreamReader(new FileInputStream(m_createSqlLocation), "UTF-8"));
    }

    /**
     * <p>readTables</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws java.lang.Exception if any.
     */
    public void readTables(final Reader reader) throws Exception {
    	final BufferedReader r = new BufferedReader(reader);
        String line;

        m_tables = new LinkedList<String>();
        m_seqmapping = new HashMap<String, String[]>();
        m_sequences = new LinkedList<String>();
        m_indexDao.reset();

        LinkedList<String> sql_l = new LinkedList<String>();

        String criteria = null;
        while ((line = r.readLine()) != null) {
            Matcher m;

            m = m_emptyLine.matcher(line);
            if (m.matches()) {
                continue;
            }

            m = m_seqmappingPattern.matcher(line);
            if (m.matches()) {
            	final String[] a = { m.group(2), m.group(3) };
                m_seqmapping.put(m.group(1), a);
                continue;
            }
            
            m = m_criteriaPattern.matcher(line);
            if (m.matches()) {
                criteria = m.group(1);
                continue;
            }

            if (line.matches("^--.*$")) {
                continue;
            }

            if (m_createPattern.matcher(line).matches()) {
                m = m_createUnique.matcher(line);
                if (m.matches()) {
                    String type = m.group(1);
                    String name = m.group(2).replaceAll("^[\"']", "").replaceAll("[\"']$",  "");

                    if (type.toLowerCase().indexOf("table") != -1) {
                        m_tables.add(name);
                    } else if (type.toLowerCase().indexOf("sequence") != -1) {
                        m_sequences.add(name);
                    } else if (type.toLowerCase().indexOf("function") != -1) {
                        if (type.toLowerCase().indexOf("language 'c'") != -1) {
                            //m_cfunctions.add(name);
                        } else {
                            //m_functions.add(name);
                        }
                    } else if (type.toLowerCase().indexOf("trusted") != -1) {
                        m = m_createLanguagePattern.matcher(line);
                        if (!m.matches()) {
                            throw new Exception("Could not match name and type of the trusted procedural language in this line: " + line);
                        }
                        //m_languages.add(m.group(1));
                    } else if (type.toLowerCase().matches("^.*\\bindex\\b.*$")) {
                    	final Index i = Index.findIndexInString(line);
                        if (i == null) {
                            throw new Exception("Could not match name and type of the index in this line: " + line);
                        }
                        m_indexDao.add(i);
                    } else {
                        throw new Exception("Unknown CREATE encountered: CREATE " + type + " " + name);
                    }
                } else {
                    throw new Exception("Unknown CREATE encountered: " + line);
                }

                sql_l.add(line);
                continue;
            }

            m = m_insertPattern.matcher(line);
            if (m.matches()) {
            	final String table = m.group(1);
                final Insert insert = new Insert(table, line, criteria);
                criteria = null;
                if (!m_inserts.containsKey(table)) {
                    m_inserts.put(table, new LinkedList<Insert>());
                }
                m_inserts.get(table).add(insert);

                continue;
            }

            if (line.toLowerCase().startsWith("select setval ")) {
            	final String table = "select_setval";
                final Insert insert = new Insert("select_setval", line, null);
                if (!m_inserts.containsKey(table)) {
                    m_inserts.put(table, new LinkedList<Insert>());
                }
                m_inserts.get(table).add(insert);

                sql_l.add(line);
                continue;
            }

            m = m_dropPattern.matcher(line);
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
    

    /**
     * <p>cleanText</p>
     *
     * @param list a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public static String cleanText(final List<String> list) {
    	final StringBuffer s = new StringBuffer();

        for (final String l : list) {
            s.append(l.replaceAll("\\s+", " "));
            if (l.indexOf(';') != -1) {
                s.append('\n');
            }
        }

        return s.toString();
    }
    

    /**
     * <p>createSequences</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void createSequences() throws Exception {
        assertUserSet();
        
        final Statement st = getConnection().createStatement();
        ResultSet rs;

        m_out.println("- creating sequences... ");

        for (final String sequence : getSequenceNames()) {
            if (getSequenceMapping(sequence) == null) {
                throw new Exception("Cannot find sequence mapping for " + sequence + "-- sequence mapping is setup by comments in the create.sql script. Look:--# DO NOT forget to add an \"install\" comment" );
            }
        }

        for (final String sequence : getSequenceNames()) {
            int minvalue = 1;
            boolean alreadyExists;

            m_out.print("  - checking \"" + sequence + "\" sequence... ");

            rs = st.executeQuery("SELECT relname FROM pg_class WHERE relname = '" + sequence.toLowerCase() + "'");
            alreadyExists = rs.next();
            if (alreadyExists) {
                m_out.println("ALREADY EXISTS");
            } else {
                m_out.println("DOES NOT EXIST");
                m_out.print("    - creating sequence \"" + sequence + "\"... ");
                st.execute("CREATE SEQUENCE " + sequence + " minvalue "
                           + minvalue);
                m_out.println("OK");
                grantAccessToObject(sequence, 4);
            }
        }

        m_out.println("- creating sequences... DONE");
    }
    
    /**
     * <p>updatePlPgsql</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void updatePlPgsql() throws Exception {
        final Statement st = getConnection().createStatement();
        ResultSet rs;

        m_out.print("- adding PL/pgSQL call handler... ");
        rs = st.executeQuery("SELECT oid FROM pg_proc WHERE " + "proname='plpgsql_call_handler' AND " + "proargtypes = ''");
        if (rs.next()) {
            m_out.println("EXISTS");
        } else if (isPgPlPgsqlLibPresent()) {
            st.execute("CREATE FUNCTION plpgsql_call_handler () " + "RETURNS OPAQUE AS '" + m_pg_plpgsql + "' LANGUAGE 'c'");
            m_out.println("OK");
        } else {
            m_out.println("SKIPPED (location of PL/pgSQL library not set, will try to continue)");
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
            st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
            m_out.println("OK");
        }
    }
    
    /**
     * <p>isIpLikeUsable</p>
     *
     * @return a boolean.
     */
    public boolean isIpLikeUsable() {
        Statement st = null;
        try {
            m_out.print("- checking if iplike is usable... ");
            st = getConnection().createStatement();
            st.execute("SELECT IPLIKE('127.0.0.1', '*.*.*.*')");
            m_out.println("YES");
        } catch (final SQLException selectException) {
            m_out.println("NO");
            return false;
        } finally {
            closeQuietly(st);
        }
        try {
            m_out.print("- checking if iplike supports IPv6... ");
            st = getConnection().createStatement();
            st.execute("SELECT IPLIKE('fe80:0000:5ab0:35ff:feee:cecd', 'fe80:*::cecd')");
            m_out.println("YES");
        } catch (final SQLException selectException) {
            m_out.println("NO");
            return false;
        } finally {
            closeQuietly(st);
        }
        return true;
    }
    
    /**
     * <p>updateIplike</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void updateIplike() throws Exception {

        boolean insert_iplike = !isIpLikeUsable();

        if (insert_iplike) {
        	dropExistingIpLike();

        	if (!installCIpLike()) {
                setupPlPgsqlIplike();
        	}
        }

        // XXX This error is generated from Postgres if eventtime(text)
        // does not exist:
        // ERROR: function eventtime(text) does not exist
        m_out.print("- checking for stale eventtime.so references... ");
        Statement st = null;
        try {
            st = getConnection().createStatement();
            st.execute("DROP FUNCTION eventtime(text)");
            m_out.println("REMOVED");
        } catch (final SQLException e) {
            /*
             * SQL Status code: 42883: ERROR: function %s does not exist
             */
            if (e.toString().indexOf("does not exist") != -1
                    || "42883".equals(e.getSQLState())) {
                m_out.println("OK");
            } else {
            	m_out.println("FAILED");
                throw e;
            }
        } finally {
            closeQuietly(st);
        }
    }

    private boolean installCIpLike() {
        m_out.print("- inserting C iplike function... ");
        boolean success;
        if (m_pg_iplike == null) {
            success = false;
            
            m_out.println("SKIPPED (location of iplike function not set)");
        } else {
            Statement st = null;
        	try {
                st = getConnection().createStatement();

                st.execute("CREATE FUNCTION iplike(text,text) RETURNS bool " + "AS '" + m_pg_iplike + "' LANGUAGE 'c' WITH(isstrict)");
                
                success = true;
                m_out.println("OK");
        	} catch (final SQLException e) {
                success = false;
        		m_out.println("FAILED (" + e + ")");
            } finally {
                closeQuietly(st);
            }
        }
        return success;
    }

    private void dropExistingIpLike() throws SQLException {
        Statement st = null;
        m_out.print("- removing existing iplike definition (if any)... ");
        try {
            st = getConnection().createStatement();
        	st.execute("DROP FUNCTION iplike(text,text)");
        	m_out.println("OK");
        } catch (final SQLException dropException) {
        	if (dropException.toString().contains("does not exist")
        			|| "42883".equals(dropException.getSQLState())) {
        		m_out.println("OK");
        	} else {
        		m_out.println("FAILED");
        		throw dropException;
        	}
        }
        finally {
            closeQuietly(st);
        }
    }

    /**
     * <p>setupPlPgsqlIplike</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void setupPlPgsqlIplike() throws Exception {
        InputStream sqlfile = null;
        Statement st = null;
        try {
            st = getConnection().createStatement();
            m_out.print("- inserting PL/pgSQL iplike function... ");
            
        	sqlfile = getClass().getResourceAsStream(IPLIKE_SQL_RESOURCE);
        	if (sqlfile == null) {
                String message = "unable to locate " + IPLIKE_SQL_RESOURCE;
                m_out.println("FAILED (" + message + ")");
        		throw new Exception(message);
        	}
        	
        	final BufferedReader in = new BufferedReader(new InputStreamReader(sqlfile, "UTF-8"));
        	final StringBuffer createFunction = new StringBuffer();
        	String line;
        	while ((line = in.readLine()) != null) {
        		createFunction.append(line).append("\n");
        	}
        	st.execute(createFunction.toString());
        	m_out.println("OK");
        } catch (final Throwable e) {
        	m_out.println("FAILED");
        	if (e instanceof Exception) {
        	    throw (Exception)e;
        	}
        	else {
        	    throw new Exception(e);
        	}
        } finally {
            // don't forget to close the statement
            closeQuietly(st);
            // don't forget to close the input stream
            closeQuietly(sqlfile);
        }
    }


    private void closeQuietly(final InputStream sqlfile) {
        try {
            if (sqlfile != null) {
                sqlfile.close();
            }
        } catch(final IOException e) {
            
        }
    }

    private void closeQuietly(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch(final Throwable e) {
        }
    }

    /**
     * <p>addStoredProcedures</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void addStoredProcedures() throws Exception {
        m_triggerDao.reset();

        Statement st = getConnection().createStatement();

        m_out.print("- adding stored procedures... ");

        File[] list = new File(m_storedProcedureDirectory).listFiles(m_sqlFilter);

        for (final File element : list) {
        	final LinkedList<String> drop = new LinkedList<String>();
            final StringBuffer create = new StringBuffer();
            String line;

            m_out.print("\n  - " + element.getName() + "... ");

            final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(element), "UTF-8"));
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
            final Trigger t = Trigger.findTriggerInString(create.toString());
            if (t != null) {
                m_triggerDao.add(t);
            }

            final Matcher m = m_createFunction.matcher(create.toString());

            if (!m.find()) {
                throw new Exception("For stored procedure in file '"
                                    + element.getName()
                                    + "' couldn't match \""
                                    + m.pattern().pattern()
                                    + "\" in string \"" + create + "\"");
            }
            final String createSql = m.group(1);
            final String function = m.group(2);
            final String columns = m.group(3);
            final String returns = m.group(4);
            // final String rest = m.group(5);

            try {
                if (functionExists(function, columns, returns)) {
                    if (t != null && t.isOnDatabase(getConnection())) {
                        t.removeFromDatabase(getConnection());
                        
                    }
                    st.execute("DROP FUNCTION " + function + " (" + columns + ")");
                    st.execute(createSql);
                    m_out.print("OK (dropped and re-added)");
                } else {
                    st.execute(createSql);
                    m_out.print("OK");
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Could not add function: " + function, e);
            }
        }
        m_out.println("");
    }


    /**
     * <p>functionExists</p>
     *
     * @param function a {@link java.lang.String} object.
     * @param columns a {@link java.lang.String} object.
     * @param returnType a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    private boolean functionExists(final String function, String columns, final String returnType) throws Exception {
    	final Map<String, Integer> types = getTypesFromDB();

    	int[] columnTypes = new int[0];
        columns = columns.trim();
        if (columns.length() > 0) {
        	final String[] splitColumns = columns.split("\\s*,\\s*");
            columnTypes = new int[splitColumns.length];
            Column c;
            for (int j = 0; j < splitColumns.length; j++) {
                c = new Column();
                c.parseColumnType(splitColumns[j]);
                columnTypes[j] = (types.get(c.getType())).intValue();
            }
        }

        final Column c = new Column();
        try {
            c.parseColumnType(returnType);
        } catch (final Throwable e) {
            throw new Exception("Could not parse column type '" + returnType + "' for function '" + function + "'.  Nested exception: " + e.getMessage(), e);
        }
        final int retType = (types.get(c.getType())).intValue();

        return functionExists(function, columnTypes, retType);
    }

    /**
     * <p>functionExists</p>
     *
     * @param function a {@link java.lang.String} object.
     * @param columnTypes an array of int.
     * @param retType a int.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    private boolean functionExists(final String function, final int[] columnTypes, final int retType) throws Exception {
    	final Statement st = getConnection().createStatement();
        ResultSet rs;

        final StringBuffer ct = new StringBuffer();
        for (final int columnType : columnTypes) {
            ct.append(" " + columnType);
        }

        final String query = "SELECT oid FROM pg_proc WHERE proname='"
                + function.toLowerCase() + "' AND " + "prorettype=" + retType
                + " AND " + "proargtypes='" + ct.toString().trim() + "'";

        rs = st.executeQuery(query);
        return rs.next();
    }
    
    /**
     * <p>getTypesFromDB</p>
     *
     * @return a {@link java.util.Map} object.
     * @throws java.sql.SQLException if any.
     */
    private Map<String, Integer> getTypesFromDB() throws SQLException {
        if (m_dbtypes != null) {
            return Collections.unmodifiableMap(m_dbtypes);
        }

        final Statement st = getConnection().createStatement();
        ResultSet rs;
        final HashMap<String, Integer> m = new HashMap<String, Integer>();

        rs = st.executeQuery("SELECT oid,typname,typlen FROM pg_type");

        while (rs.next()) {
            try {
                m.put(Column.normalizeColumnType(rs.getString(2),  (rs.getInt(3) < 0)), Integer.valueOf(rs.getInt(1)));
            } catch (final Throwable e) {
                // ignore
            }
        }

        m_dbtypes = m;
        return Collections.unmodifiableMap(m_dbtypes);
    }
    
    /**
     * <p>addTriggersForTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    private void addTriggersForTable(final String table) throws SQLException {
    	final List<Trigger> triggers = m_triggerDao.getTriggersForTable(table.toLowerCase());
        for (final Trigger trigger : triggers) {
            m_out.print("    - checking trigger '" + trigger.getName() + "' on this table... ");
            if (!trigger.isOnDatabase(getConnection())) {
                trigger.addToDatabase(getConnection());
            }
            m_out.println("DONE");
        }
    }

    /**
     * <p>createTables</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void createTables() throws Exception {
        assertUserSet();
        
        final Statement st = getConnection().createStatement();
        ResultSet rs;

        m_out.println("- creating tables...");

        for (String tableName : getTableNames()) {
            if (m_force) {
                tableName = tableName.toLowerCase();

                final String create = getTableCreateFromSQL(tableName);

                boolean remove;

                rs = st.executeQuery("SELECT relname FROM pg_class "
                        + "WHERE relname = '" + tableName + "'");

                remove = rs.next();

                m_out.print("  - removing old table... ");
                if (remove) {
                    st.execute("DROP TABLE " + tableName + " CASCADE");
                    m_out.println("REMOVED");
                } else {
                    m_out.println("CLEAN");
                }

                m_out.print("  - creating table \"" + tableName + "\"... ");
                st.execute("CREATE TABLE " + tableName + " (" + create + ")");
                m_out.println("CREATED");

                addIndexesForTable(tableName);
                addTriggersForTable(tableName);
                grantAccessToObject(tableName, 2);
            } else {
                m_out.println("  - checking table \"" + tableName + "\"... ");

                tableName = tableName.toLowerCase();

                final Table newTable = getTableFromSQL(tableName);
                final Table oldTable = getTableFromDB(tableName);

                if (newTable.equals(oldTable)) {
                    addIndexesForTable(tableName);
                    addTriggersForTable(tableName);
                    m_out.println("  - checking table \"" + tableName  + "\"... UPTODATE");
                } else {
                    if (oldTable == null) {
                        final String create = getTableCreateFromSQL(tableName);
                        final String createSql = "CREATE TABLE " + tableName + " (" + create + ")"; 
                        m_out.print("  - checking table \"" + tableName + "\"... ");
                        st.execute(createSql);
                        m_out.println("CREATED");

                        addIndexesForTable(tableName);
                        addTriggersForTable(tableName);
                        grantAccessToObject(tableName, 2);
                    } else {
                        try {
                            changeTable(tableName, oldTable, newTable);
                        } catch (final Throwable e) {
                            throw new Exception("Error changing table '" + tableName + "'.  Nested exception: " + e.getMessage(), e);
                        }
                    }
                }
            }
        }

        m_out.println("- creating tables... DONE");
    }
    

    /**
     * <p>getTableFromSQL</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link Table} object.
     * @throws java.lang.Exception if any.
     */
    public Table getTableFromSQL(String tableName) throws Exception {
    	final Table table = new Table();

    	final LinkedList<Column> columns = new LinkedList<Column>();
    	final LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        boolean parens = false;
        StringBuffer accumulator = new StringBuffer();

        final String create = getTableCreateFromSQL(tableName);
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
            	final String a = accumulator.toString().trim();

                if (a.toLowerCase().startsWith("constraint ")) {
                    Constraint constraint;
                    try {
                        constraint = new Constraint(tableName, a);
                    } catch (final Throwable e) {
                        throw new Exception("Could not parse constraint for table '" + tableName + "'.  Nested exception: " + e.getMessage(), e);
                    }
                    final List<String> constraintColumns = constraint.getColumns();
                    if (constraint.getType() != Constraint.CHECK) {
                        Assert.state(constraintColumns.size() > 0, "constraint '" + constraint.getName() + "' has no constrained columns");

                        // If the constraint is a foreign key constraint, make sure that the number
                        // of local and foreign columns matches
                        Assert.state(
                            constraint.getType() != Constraint.FOREIGN_KEY ||
                            constraint.getColumns().size() == constraint.getForeignColumns().size(),
                            "number of foreign key constraint columns doesn't match number of foreign columns"
                        );

                    	for (final String constrainedName : constraintColumns) {
                    		final Column constrained = findColumn(columns, constrainedName);
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
                    }
                    constraints.add(constraint);
                } else {
                	final Column column = new Column();
                    try {
                        column.parse(accumulator.toString());
                        columns.add(column);
                    } catch (final Throwable e) {
                        throw new Exception("Could not parse table " + tableName + ".  Chained: " + e.getMessage(), e);
                    }
                }

                accumulator = new StringBuffer();
            } else {
                accumulator.append(c);
            }
        }

        table.setName(tableName);
        table.setColumns(columns);
        Collections.sort(constraints, InstallerDb.constraintComparator);
        table.setConstraints(constraints);
        table.setNotNullOnPrimaryKeyColumns();

        return table;
    }


    /**
     * <p>getXFromSQL</p>
     *
     * @param item a {@link java.lang.String} object.
     * @param regex a {@link java.lang.String} object.
     * @param itemGroup a int.
     * @param returnGroup a int.
     * @param description a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    private String getXFromSQL(String item, final String regex, final int itemGroup, final int returnGroup, final String description) throws Exception {

        item = item.toLowerCase();
        final Matcher m = Pattern.compile(regex).matcher(getSql());

        while (m.find()) {
            if (m.group(itemGroup).equalsIgnoreCase(item)) {
                return m.group(returnGroup);
            }
        }

        throw new Exception("could not find " + description + " \"" + item + "\"");
    }
    

    /**
     * <p>findColumn</p>
     *
     * @param columns a {@link java.util.List} object.
     * @param column a {@link java.lang.String} object.
     * @return a {@link Column} object.
     */
    private Column findColumn(final List<Column> columns, final String column) {
        for (final Column c : columns) {
            if (c.getName().equals(column.toLowerCase())) {
                return c;
            }
        }

        return null;
    }

    /**
     * <p>getTableColumnsFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Column> getTableColumnsFromDB(final String tableName) throws Exception {
        final Table table = getTableFromDB(tableName);
        if (table == null) {
            return null;
        }
        return table.getColumns();
    }

    /**
     * <p>getTableFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link Table} object.
     * @throws java.lang.Exception if any.
     */
    public Table getTableFromDB(final String tableName) throws Exception {
        if (!tableExists(tableName)) {
            return null;
        }

        final Table table = new Table();
        table.setName(tableName.toLowerCase());

        final List<Column> columns = getColumnsFromDB(tableName);
        final List<Constraint> constraints = getConstraintsFromDB(tableName);
        Collections.sort(constraints, InstallerDb.constraintComparator);
        
        table.setColumns(columns);
        table.setConstraints(constraints);
        return table;
    }
    

    /**
     * <p>tableExists</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    private boolean tableExists(final String table) throws SQLException {
    	final Statement st = getConnection().createStatement();
        ResultSet rs;

        rs = st.executeQuery("SELECT DISTINCT tablename FROM pg_tables WHERE lower(tablename) = '" + table.toLowerCase() + "'");
        return rs.next();
    }

    /**
     * <p>getColumnsFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Column> getColumnsFromDB(final String tableName) throws Exception {
    	final LinkedList<Column> columns = new LinkedList<Column>();

    	Statement st = getConnection().createStatement();

    	String query = "SELECT "
                + "        attname, "
                + "        format_type(atttypid, atttypmod), "
                + "        attnotnull "
                + "FROM "
                + "        pg_attribute "
                + "WHERE "
                + "        attrelid = (SELECT oid FROM pg_class WHERE relname = '" + tableName.toLowerCase() + "') "
                + "    AND "
                + "        attnum > 0"
                + "    AND "
                + "        attisdropped = false";

        query = query + " ORDER BY attnum";

        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
        	final Column c = new Column();
            c.setName(rs.getString(1));
            final String columnType = rs.getString(2);
            try {
                c.parseColumnType(columnType);
            } catch (final Throwable e) {
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

        st = getConnection().createStatement();

        query = "SELECT "
                + "        attr.attname, "
                + "        pg_get_expr(def.adbin, def.adrelid) "
                + "FROM "
                + "        pg_attribute attr, "
                + "        pg_attrdef def "
                + "WHERE "
                + "        attr.attrelid = (SELECT oid FROM pg_class WHERE relname = '" + tableName.toLowerCase() + "') "
                + "    AND "
                + "        attr.attnum > 0"
                + "    AND "
                + "        attr.atthasdef = 't' "
                + "    AND "
                + "        attr.attrelid = def.adrelid"
                + "    AND "
                + "        attr.attnum = def.adnum"
                + "    AND "
                + "        attr.attisdropped = false";

        rs = st.executeQuery(query);

        while (rs.next()) {
            Column column = null;
            for (final Column c : columns) {
                if (c.getName().equals(rs.getString(1))) {
                    column = c;
                    break;
                }
            }
            
            if (column == null) {
                throw new Exception("Could not find column '" + rs.getString(1) + "' in original column list when adding default values");
            }
            
            column.setDefaultValue(rs.getString(2).replaceAll("'(.*)'::([a-zA-Z ]+)", "'$1'"));
        }

        rs.close();
        st.close();
        
        return columns;

    }


    /**
     * <p>getConstraintsFromDB</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     * @throws java.lang.Exception if any.
     */
    public List<Constraint> getConstraintsFromDB(final String tableName) throws SQLException, Exception {
        final Statement st = getConnection().createStatement();
        ResultSet rs;

        final LinkedList<Constraint> constraints = new LinkedList<Constraint>();

        final String query = "SELECT c.oid, c.conname, c.contype, c.conrelid, "
            + "c.confrelid, a.relname, c.confupdtype, c.confdeltype, c.consrc from pg_class a "
            + "right join pg_constraint c on c.confrelid = a.oid "
            + "where c.conrelid = (select oid from pg_class where relname = '"
                + tableName.toLowerCase() + "') order by c.oid";

        rs = st.executeQuery(query);

        while (rs.next()) {
        	final int oid = rs.getInt(1);
            final String name = rs.getString(2);
            final String type = rs.getString(3);
            final int conrelid = rs.getInt(4);
            final int confrelid = rs.getInt(5);
            final String ftable = rs.getString(6);
            final String foreignUpdType = rs.getString(7);
            final String foreignDelType = rs.getString(8);
            final String checkExpression = rs.getString(9);
  
            final Constraint constraint;
            if ("p".equals(type)) {
            	final List<String> columns = getConstrainedColumnsFromDBForConstraint(oid, conrelid);
                constraint = new Constraint(tableName.toLowerCase(), name, columns);
            } else if ("f".equals(type)) {
            	final List<String> columns = getConstrainedColumnsFromDBForConstraint(oid, conrelid);
                final List<String> fcolumns = getForeignColumnsFromDBForConstraint(oid, confrelid);
                constraint = new Constraint(tableName.toLowerCase(), name, columns, ftable, fcolumns, foreignUpdType, foreignDelType);
            } else if ("c".equals(type)) {
            	constraint = new Constraint(tableName.toLowerCase(), name, checkExpression);
            } else if ("u".equals(type)) {
                continue; // Ignored....
            } else {
                throw new Exception("Do not support constraint type \"" + type + "\" in constraint \"" + name + "\"");
            }

            constraints.add(constraint);
        }

        return constraints;
    }



    private List<String> getConstrainedColumnsFromDBForConstraint(final int oid, final int conrelid) throws Exception {
    	final Statement st = getConnection().createStatement();
    	final ResultSet rs;

    	final LinkedList<String> columns = new LinkedList<String>();

    	final String query = "select a.attname from pg_attribute a, pg_constraint c where a.attrelid = c.conrelid and a.attnum = ANY (c.conkey) and c.oid = "
                + oid + " and a.attrelid = " + conrelid;
        rs = st.executeQuery(query);

        while (rs.next()) {
            columns.add(rs.getString(1));
        }

        rs.close();
        st.close();

        return columns;
    }

    private List<String> getForeignColumnsFromDBForConstraint(final int oid, final int confrelid) throws Exception {
    	final Statement st = getConnection().createStatement();
    	final ResultSet rs;

    	final LinkedList<String> columns = new LinkedList<String>();

    	final String query = "select a.attname from pg_attribute a, pg_constraint c where a.attrelid = c.confrelid and a.attnum = ANY (c.confkey) and c.oid = "
                + oid + " and a.attrelid = " + confrelid;
        rs = st.executeQuery(query);

        while (rs.next()) {
            columns.add(rs.getString(1));
        }

        rs.close();
        st.close();

        return columns;
    }

    /**
     * <p>changeTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param oldTable a {@link Table} object.
     * @param newTable a {@link Table} object.
     * @throws java.lang.Exception if any.
     */
    private void changeTable(final String table, final Table oldTable, final Table newTable) throws Throwable {
        assertUserSet();
        
        final List<Column> oldColumns = oldTable.getColumns();
        final List<Column> newColumns = newTable.getColumns();

        final Statement st = getConnection().createStatement();
        final TreeMap<String, ColumnChange> columnChanges = new TreeMap<String, ColumnChange>();
        final String[] oldColumnNames = new String[oldColumns.size()];

        int i;

        if (hasTableChanged(table)) {
            return;
        }
        
        tableChanged(table);

        m_out.println("  - checking table \"" + table
                      + "\"... SCHEMA DOES NOT MATCH");

        m_out.println("    - differences:");
        for (final Constraint newConstraint : newTable.getConstraints()) {
            m_out.println("new constraint: " + newConstraint.getTable() + ": " + newConstraint);
        }
        for (final Constraint oldConstraint : oldTable.getConstraints()) {
            m_out.println("old constraint: " + oldConstraint.getTable() + ": " + oldConstraint);
        }

        for (final Column newColumn : newColumns) {
        	final Column oldColumn = findColumn(oldColumns, newColumn.getName());

            if (oldColumn == null || !newColumn.equals(oldColumn)) {
                m_out.println("      - column \"" + newColumn.getName() + "\" is different");
                if (m_debug) {
                    m_out.println("        - old column: " + ((oldColumn == null) ? "null" : oldColumn.toString()));
                    m_out.println("        - new column: " + newColumn);
                }
            }

            if (!columnChanges.containsKey(newColumn.getName())) {
                columnChanges.put(newColumn.getName(), new ColumnChange());
            }

            final ColumnChange columnChange = columnChanges.get(newColumn.getName());
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
                    final String message = "Column " + newColumn.getName()
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
        for (final Column oldColumn : oldColumns) {
            oldColumnNames[i] = oldColumn.getName();

            if (columnChanges.containsKey(oldColumn.getName())) {
                ColumnChange columnChange = columnChanges.get(oldColumn.getName());
                final Column newColumn = columnChange.getColumn();
                if (newColumn.getType().indexOf("timestamp") != -1) {
                    columnChange.setUpgradeTimestamp(true);
                }
            } else {
                m_out.println("      * WARNING: column \""
                        + oldColumn.getName() + "\" exists in the "
                        + "database but is not in the new schema.  "
                        + "REMOVING COLUMN");
            }
            
            i++;
        }

        final String tmpTable = table + "_old_" + System.currentTimeMillis();

        try {
            if (tableExists(tmpTable)) {
                st.execute("DROP TABLE " + tmpTable + " CASCADE");
            }

            m_out.print("    - creating temporary table... ");
            st.execute("CREATE TABLE " + tmpTable + " AS SELECT "
                       + StringUtils.arrayToDelimitedString(oldColumnNames, ", ")
                       + " FROM " + table);
            m_out.println("done");

            st.execute("DROP TABLE " + table + " CASCADE");

            m_out.print("    - creating new '" + table + "' table... ");
            st.execute("CREATE TABLE " + table + " ("
                    + getTableCreateFromSQL(table) + ")");
            m_out.println("done");
            
            addIndexesForTable(table);
            addTriggersForTable(table);

            transformData(table, tmpTable, columnChanges, oldColumnNames);

            grantAccessToObject(table, 4);

            m_out.print("    - optimizing table " + table + "... ");
            st.execute("VACUUM ANALYZE " + table);
            m_out.println("DONE");
        } catch (final Throwable e) {
            if (m_no_revert) {
                m_out.println("FAILED!  Not reverting due to '-R' being "
                        + "passed.  Old data in " + tmpTable);
                throw e;
            }

            try {
                getConnection().rollback();
                getConnection().setAutoCommit(true);

                if (tableExists(table)) {
                    st.execute("DROP TABLE " + table + " CASCADE");
                }
                st.execute("CREATE TABLE " + table + " AS SELECT "
                           + StringUtils.arrayToDelimitedString(oldColumnNames, ", ")
                           + " FROM " + tmpTable);
                st.execute("DROP TABLE " + tmpTable);
            } catch (final SQLException se) {
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


        m_out.println("  - checking table \"" + table
                      + "\"... COMPLETED UPDATING TABLE");
    }

    /*
     * Note: every column has a ColumnChange record for it, which lists
     * the column name, a null replacement, if any, and the indexes for
     * selected rows (for using in ResultSet.getXXX()) and prepared rows
     * (PreparedStatement.setObject()).
     * Monkey.  Make monkey dance.
     */
    /**
     * <p>transformData</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param oldTable a {@link java.lang.String} object.
     * @param columnChanges a {@link java.util.TreeMap} object.
     * @param oldColumnNames an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     * @throws java.text.ParseException if any.
     * @throws java.lang.Exception if any.
     */
    private void transformData(final String table, final String oldTable, final Map<String, ColumnChange> columnChanges, final String[] oldColumnNames) throws SQLException, ParseException,
            Exception {
        final Statement st = getConnection().createStatement();
        int i;

        st.setFetchSize(s_fetch_size);

        for (i = 0; i < oldColumnNames.length; i++) {
            ColumnChange c = columnChanges.get(oldColumnNames[i]);
            if (c != null) {
                c.setSelectIndex(i + 1);
            }
        }
        
        final LinkedList<String> insertColumns = new LinkedList<String>();
        final LinkedList<String> questionMarks = new LinkedList<String>();
        for (final ColumnChange c : columnChanges.values()) {
            c.setColumnType(c.getColumn().getColumnSqlType());
            
            final ColumnChangeReplacement r = c.getColumnReplacement();
            if (r == null || c.getSelectIndex() > 0
                    || r.addColumnIfColumnIsNew()) {
                insertColumns.add(c.getColumn().getName());
                questionMarks.add("?");
                c.setPrepareIndex(questionMarks.size());
            }
        }

        /*
         * Pull everything in from the old table and filter it to update the
         * data to any new formats.
         */

        m_out.print("    - transforming data into the new table...\r");

        ResultSet rs = st.executeQuery("SELECT count(*) FROM " + oldTable);
        rs.next();
        final long num_rows = rs.getLong(1);

        final String order;
        if (table.equals("outages")) {
            order = " ORDER BY iflostservice";
        } else {
            order = "";
        }

        String dbcmd = "SELECT "
            + StringUtils.arrayToDelimitedString(oldColumnNames, ", ")
            + " FROM "
            + oldTable + order;
        if (m_debug) {
            m_out.println("    - performing select: " + dbcmd);
        }

        final PreparedStatement select = getConnection().prepareStatement(dbcmd);
        select.setFetchSize(s_fetch_size);

        dbcmd = "INSERT INTO " + table + " ("
            + StringUtils.collectionToDelimitedString(insertColumns, ", ")
            + ") values ("
            + StringUtils.collectionToDelimitedString(questionMarks, ", ")
            + ")";
        if (m_debug) {
            m_out.println("    - performing insert: " + dbcmd);
        }

        final PreparedStatement insert = getConnection().prepareStatement(dbcmd);

        rs = select.executeQuery();
        getConnection().setAutoCommit(false);

        Object obj;
        int current_row = 0;

        while (rs.next()) {
            for (final ColumnChange change : columnChanges.values()) {
                final String name = change.getColumn().getName();
                
                if (change.getSelectIndex() == 0 &&
                        change.hasColumnReplacement()
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
                            && !obj.getClass().equals(java.sql.Timestamp.class)) {
                        if (m_debug) {
                            m_out.println("      - " + name
                                    + " is an old-style timestamp");
                        }
                        final String newObj = m_dateFormatter.format(m_dateParser.parse((String) obj));
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
            } catch (final SQLException e) {
                final SQLException ex = new SQLException(
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
                        + (int) Math.floor((current_row * 100.0) / num_rows)
                        + "%  [" + m_spins[(current_row / 20) % m_spins.length]
                        + "]\r");
            }
        }
        
        rs.close();
        select.close();
        insert.close();

        getConnection().commit();
        getConnection().setAutoCommit(true);

        if (table.equals("events") && num_rows == 0) {
            st.execute("INSERT INTO events (eventid, eventuei, eventtime, "
                    + "eventsource, eventdpname, eventcreatetime, "
                    + "eventseverity, eventlog, eventdisplay) values "
                    + "(0, 'http://uei.opennms.org/dummyevent', now(), "
                    + "'OpenNMS.Eventd', 'localhost', now(), 1, 'Y', 'Y')");
        }
        
        st.close();

        m_out.println("    - transforming data into the new table... "
                + "DONE           ");
    }

    /**
     * <p>databaseSetUser</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void databaseSetUser() throws SQLException {
        final String[] tableTypes = {"TABLE"};
        final ResultSet rs = getAdminConnection().getMetaData().getTables(null, "public", "%", tableTypes);
        final HashSet<String> objects = new HashSet<String>();
        while (rs.next()) {
            objects.add(rs.getString("TABLE_NAME"));
        }
        final PreparedStatement st = getAdminConnection().prepareStatement("ALTER TABLE ? OWNER TO ?");
        for (final String objName : objects) {
            st.setString(1, objName);
            st.setString(2, m_user);
            st.execute();
        }
        st.close();
    }

    /**
     * <p>databaseRemoveDB</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void databaseRemoveDB() throws SQLException {
        assertUserSet();

        m_out.print("- removing database '" + getDatabaseName() + "'... ");
        final Statement st = getAdminConnection().createStatement();
        st.execute("DROP DATABASE \"" + getDatabaseName() + "\"");
        st.close();
        m_out.print("DONE");
    }

    /**
     * <p>addIndexesForTable</p>
     *
     * @param table a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    private void addIndexesForTable(final String table) throws SQLException {
    	final List<Index> indexes = getIndexDao().getIndexesForTable(table.toLowerCase());
        for (final Index index : indexes) {
            m_out.print("    - checking index '" + index.getName()
                        + "' on this table... ");
            if (!index.isOnDatabase(getConnection())) {
                index.addToDatabase(getConnection());
            }
            m_out.println("DONE");
        }

    }

    /**
     * <p>grantAccessToObject</p>
     *
     * @param object a {@link java.lang.String} object.
     * @param indent a int.
     * @throws java.sql.SQLException if any.
     */
    private void grantAccessToObject(final String object, final int indent) throws SQLException {
        assertUserSet();
        
        for (int i = 0; i < indent; i++) {
            m_out.print(" ");
        }
        
        m_out.print("- granting access to '" + object + "' for user '" + m_user + "'... ");
        
        final Statement st = getConnection().createStatement();
        
        try {
            st.execute("GRANT ALL ON " + object + " TO " + m_user);
        } finally {
            st.close();
        }
        
        m_out.println("DONE");
    }

    // XXX This causes the following Postgres error:
    // ERROR: duplicate key violates unique constraint "pk_dpname"
    /**
     * <p>insertData</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void insertData() throws Exception {
        for (final String table : getInserts().keySet()) {
            insertData(table);
        }
    }

    public void insertData(String table) throws Exception {
        final List<Insert> inserts = getInserts().get(table);
        Status status = Status.OK;

        m_out.print("- inserting initial table data for \"" + table + "\"... ");

        // XXX: criteria are checked for all inserts before
        // any of them are done so inserts don't interfere with
        // other inserts criteria
        final List<Insert> toBeInserted = new LinkedList<Insert>();
        for (final Insert insert : inserts) {
            if (insert.isCriteriaMet()) {
                toBeInserted.add(insert);
            }
        }
        
        for(final Insert insert : toBeInserted) {
            status = status.combine(insert.doInsert());
        }

        m_out.println(status);
    }

    /**
     * <p>checkUnicode</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void checkUnicode() throws Exception {
        assertUserSet();

        m_out.print("- checking if database \"" + getDatabaseName() + "\" is unicode... ");

        Statement st = null;
        ResultSet rs = null;

        try {
            try {
                st = getAdminConnection().createStatement();
                
                try {
                    rs = st.executeQuery("SELECT encoding FROM pg_database WHERE LOWER(datname)='" + getDatabaseName().toLowerCase() + "'");
                    if (rs.next()) {
                        if (rs.getInt(1) == 5 || rs.getInt(1) == 6) {
                            m_out.println("ALREADY UNICODE");
                            return;
                        }
                    }
                    m_out.println("NOT UNICODE");
                    
                    throw new Exception("OpenNMS requires a Unicode database.  Please delete and recreate your\ndatabase and try again.");
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        } finally {
            this.disconnect();
        }
    }

    /**
     * <p>getTableColumnsFromSQL</p>
     *
     * @param tableName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public List<Column> getTableColumnsFromSQL(final String tableName) throws Exception {
        return getTableFromSQL(tableName).getColumns();
    }

    /**
     * <p>getTableCreateFromSQL</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getTableCreateFromSQL(final String table) throws Exception {
        return getXFromSQL(table, "(?i)\\bcreate table\\s+['\"]?(\\S+)['\"]?"
                + "\\s+\\((.+?)\\);", 1, 2, "table");
    }

    private void assertUserSet() {
        Assert.state(m_user != null, "postgresOpennmsUser property has not been set");
    }

    private Connection getConnection() throws SQLException {
        if (m_connection == null) {
            initializeConnection();
        }
        return m_connection;
    }

    private void initializeConnection() throws SQLException {
        Assert.state(m_dataSource != null, "dataSource property has not been set");

        try {
            m_connection = getDataSource().getConnection();
        } catch (final SQLException e) {
            rethrowDatabaseConnectionException(getDataSource(), e, "Could not get a connection to the OpenNMS database.");
        }
    }
    
    /**
     * <p>closeConnection</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void closeConnection() throws SQLException {
        if (m_connection == null) {
            return;
        }
        m_connection.close();
        m_connection = null;
    }

    private Connection getAdminConnection() throws SQLException {
        if (m_adminConnection == null) {
            initializeAdminConnection();
        }
        return m_adminConnection;
    }

    private void initializeAdminConnection() throws SQLException {
        Assert.state(m_adminDataSource != null, "adminDataSource property has not been set");

        try {
            m_adminConnection = getAdminDataSource().getConnection();
        } catch (final SQLException e) {
            rethrowDatabaseConnectionException(getAdminDataSource(), e, "Could not get an administrative connection to the database.");
        }
    }
    
    /**
     * <p>closeAdminConnection</p>
     *
     * @throws java.sql.SQLException if any.
     */
    private void closeAdminConnection() throws SQLException {
        if (m_adminConnection == null) {
            return;
        }
        m_adminConnection.close();
        m_adminConnection = null;
    }

    /**
     * Close all connections to the database.
     *
     * @throws java.sql.SQLException if any.
     */
    public void disconnect() throws SQLException {
        this.closeColumnReplacements();
        this.closeConnection();
        this.closeAdminConnection();
    }

    private void rethrowDatabaseConnectionException(final DataSource ds, final SQLException e, final String msg) throws SQLException {
    	final SQLException newE = new DatabaseConnectionException(msg + "  Is the database running, listening for TCP connections, and allowing us to connect and authenticate from localhost?  Tried connecting to database specified by data source " + ds.toString() + ".  Original error: " + e);
        newE.initCause(e);
        throw newE;
    }

    /**
     * <p>setCreateSqlLocation</p>
     *
     * @param createSqlLocation a {@link java.lang.String} object.
     */
    public void setCreateSqlLocation(final String createSqlLocation) {
        m_createSqlLocation = createSqlLocation;
    }

    /**
     * <p>getCreateSqlLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreateSqlLocation() {
        return m_createSqlLocation;
    }

    /**
     * <p>getTableNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    private List<String> getTableNames() {
        return m_tables;
    }

    /**
     * <p>getSequenceNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    private List<String> getSequenceNames() {
        return m_sequences;
    }

    /**
     * <p>getSequenceMapping</p>
     *
     * @param sequence a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    private String[] getSequenceMapping(final String sequence) {
        return m_seqmapping.get(sequence);
    }

    /**
     * <p>getIndexDao</p>
     *
     * @return a {@link IndexDao} object.
     */
    private IndexDao getIndexDao() {
        return m_indexDao;
    }

    /**
     * <p>getInserts</p>
     *
     * @return a {@link java.util.Map} object.
     */
    private Map<String, List<Insert>> getInserts() {
        return Collections.unmodifiableMap(m_inserts);
    }

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    private String getSql() {
        return m_sql;
    }

    /**
     * <p>hasTableChanged</p>
     *
     * @param table a {@link java.lang.String} object.
     * @return a boolean.
     */
    private boolean hasTableChanged(final String table) {
        return m_changed.contains(table);
    }

    /**
     * <p>tableChanged</p>
     *
     * @param table a {@link java.lang.String} object.
     */
    private void tableChanged(final String table) {
        m_changed.add(table);
    }

    /**
     * <p>setOutputStream</p>
     *
     * @param out a {@link java.io.PrintStream} object.
     */
    public void setOutputStream(final PrintStream out) {
        m_out = out;
    }

    /**
     * <p>setStoredProcedureDirectory</p>
     *
     * @param directory a {@link java.lang.String} object.
     */
    public void setStoredProcedureDirectory(final String directory) {
        m_storedProcedureDirectory = directory;
    }

    /**
     * <p>getStoredProcedureDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStoredProcedureDirectory() {
        return m_storedProcedureDirectory;
    }

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(final DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    private DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setAdminDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setAdminDataSource(final DataSource dataSource) {
        m_adminDataSource = dataSource;
    }
    
    /**
     * <p>getAdminDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    private DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    /**
     * <p>setForce</p>
     *
     * @param force a boolean.
     */
    public void setForce(final boolean force) {
        m_force = force;
    }

    /**
     * <p>setDebug</p>
     *
     * @param debug a boolean.
     */
    public void setDebug(final boolean debug) {
        m_debug = debug;
    }

    /**
     * <p>setIgnoreNotNull</p>
     *
     * @param ignoreNotNull a boolean.
     */
    public void setIgnoreNotNull(final boolean ignoreNotNull) {
        m_ignore_notnull = ignoreNotNull;
    }

    /**
     * <p>getDatabaseName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    private String getDatabaseName() {
        return m_databaseName;
    }

    /**
     * <p>setDatabaseName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setDatabaseName(final String name) {
        m_databaseName = name;
    }

    /**
     * <p>setNoRevert</p>
     *
     * @param noRevert a boolean.
     */
    public void setNoRevert(final boolean noRevert) {
        m_no_revert = noRevert;
    }

    /**
     * <p>setPostgresOpennmsUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setPostgresOpennmsUser(final String user) {
        m_user = user;
    }

    /**
     * <p>setPostgresIpLikeLocation</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    public void setPostgresIpLikeLocation(final String location) {
        if (location != null) {
        	final File iplike = new File(location);
            if (!iplike.exists()) {
                m_out.println("WARNING: missing " + location + ": OpenNMS will use a slower stored procedure if the native library is not available");
            }
        }

        m_pg_iplike = location;
    }

    /**
     * <p>setPostgresPlPgsqlLocation</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    public void setPostgresPlPgsqlLocation(final String location) {
        if (location != null) {
        	final File plpgsql = new File(location);
            if (!plpgsql.exists()) {
                m_out.println("FATAL: missing " + location + ": Unable to set up even the slower IPLIKE stored procedure without PL/PGSQL language support");
            }
        }
        
        m_pg_plpgsql = location;
    }
    
    /**
     * <p>isPgPlPgsqlLibPresent</p>
     *
     * @return a boolean.
     */
    private boolean isPgPlPgsqlLibPresent() {
        if (m_pg_plpgsql == null)
            return false;
        
        final File plpgsqlLib = new File(m_pg_plpgsql);
        if (plpgsqlLib.exists() && plpgsqlLib.canRead())
            return true;
        
        return false;
    }
    
    /**
     * <p>closeColumnReplacements</p>
     *
     * @throws java.sql.SQLException if any.
     */
    private void closeColumnReplacements() throws SQLException {
        for (ColumnChangeReplacement r : m_columnReplacements.values()) {
            r.close();
        }
    }
    
    
    enum Status {
        OK,
        SKIPPED,
        EXISTS;
        
        Status combine(Status s) {
            if (this.ordinal() > s.ordinal()) {
                return this;
            } else {
                return s;
            }
        }
    }
    
    private class Insert {

        private final String m_table;
        private final String m_insertStatement;
        private final String m_criteria;

        public Insert(final String table, final String line, final String criteria) {
            m_table = table;
            m_insertStatement = line;
            m_criteria = criteria;
        }
        
        public String getTable() {
            return m_table;
        }

        public String getCriteria() {
            return m_criteria;
        }

        public String getInsertStatement() {
            return m_insertStatement;
        }

        Status execute() throws SQLException {
            if (isCriteriaMet()) {
                return doInsert();
            } else {
                return Status.SKIPPED;
            }
        }

        private boolean isCriteriaMet() throws SQLException {
            if (getCriteria() == null) {
                return true;
            }
            Statement st = null;
            try {
                st = getConnection().createStatement();
                ResultSet rs = null;
                try {
                    rs = st.executeQuery(getCriteria());
                    // if we find a row the first column must be 't'
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                    // other wise return false
                    return false;
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        }

        private Status doInsert() throws SQLException {
            Statement st = null;
            try {
                st = getConnection().createStatement();
                st.execute(getInsertStatement());
            } catch (final SQLException e) {
                /*
                 * SQL Status codes: 23505: ERROR: duplicate key violates
                 * unique constraint "%s"
                 */
                if (e.toString().indexOf("duplicate key") != -1
                        || "23505".equals(e.getSQLState())) {
                    return Status.EXISTS;
                } else {
                    throw e;
                }
            } finally {
                if (st != null) {
                    st.close();
                }
            }
            return Status.OK;
        }
        


    }

    /**
     * <p>vacuumDatabase</p>
     *
     * @param full a boolean.
     * @throws java.sql.SQLException if any.
     */
    public void vacuumDatabase(final boolean full) throws SQLException {
        final Statement st = getConnection().createStatement();
        m_out.print("- optimizing database (VACUUM ANALYZE)... ");
        st.execute("VACUUM ANALYZE");
        m_out.println("OK");
        
        if (full) {
            m_out.print("- recovering database disk space (VACUUM FULL)... ");
            st.execute("VACUUM FULL");
            m_out.println("OK");
        }
    }
}
