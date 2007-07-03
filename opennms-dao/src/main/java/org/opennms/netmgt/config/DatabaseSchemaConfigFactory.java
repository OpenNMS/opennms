//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 03: Use Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.filter.Column;
import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.filter.Join;
import org.opennms.netmgt.config.filter.Table;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * database schemafor the filters from the database-schema xml file.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class DatabaseSchemaConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static DatabaseSchemaConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private DatabaseSchema m_config;

    /**
     * The set of tables that can be joined directly or indirectly to the
     * primary table
     */
    // FIXME: m_joinable is never read
    //private Set m_joinable = null;

    /**
     * A map from a table to the join to use to get 'closer' to the primary
     * table
     */
    private Map<String, Join> m_primaryJoins = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private DatabaseSchemaConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        FileReader cfgIn = new FileReader(configFile);

        parseXML(cfgIn);


        cfgIn.close();
	}
    
    public DatabaseSchemaConfigFactory(Reader reader) throws IOException, MarshalException, ValidationException {
        parseXML(reader);
    }

    private void parseXML(Reader rdr) throws IOException, MarshalException, ValidationException {
    
        m_config = (DatabaseSchema) Unmarshaller.unmarshal(DatabaseSchema.class, rdr);
    
        finishConstruction();
    
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DB_SCHEMA_FILE_NAME);

        m_singleton = new DatabaseSchemaConfigFactory(cfgFile.getPath());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DatabaseSchemaConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    public static synchronized void setInstance(DatabaseSchemaConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * Return the database schema.
     * 
     * @return the database schema
     */
    public synchronized DatabaseSchema getDatabaseSchema() {
        return m_config;
    }

    /**
     * This method is used to find the table that should drive the construction
     * of the join clauses between all table in the from clause. At least one
     * table has to be designated as the driver table.
     * 
     * @return The name of the driver table
     */
    public Table getPrimaryTable() {
        Enumeration e = getDatabaseSchema().enumerateTable();
        while (e.hasMoreElements()) {
            Table t = (Table) e.nextElement();
            if (t.getVisable() == null || t.getVisable().equalsIgnoreCase("true")) {
                if (t.getKey() != null && t.getKey().equals("primary")) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Find a table using its name as the search key.
     * 
     * @param name
     *            the name of the table to find
     * @return the table if it is found, null otherwise.
     */
    public Table getTableByName(String name) {
        Enumeration e = getDatabaseSchema().enumerateTable();
        while (e.hasMoreElements()) {
            Table t = (Table) e.nextElement();
            if (t.getVisable() == null || t.getVisable().equalsIgnoreCase("true")) {
                if (t.getName() != null && t.getName().equals(name)) {
                    return t;
                }
            }
        }
        return null;

    }

    /**
     * Find the table which has a visible column named 'colName'
     * 
     * @param the
     *            name of the column to search for
     * @return the table containing column 'colName', null if colName is not a
     *         valid column or if is not visible.
     * 
     */
    public Table findTableByVisableColumn(String colName) {
        Table table = null;

        Enumeration etbl = getDatabaseSchema().enumerateTable();
        OUTER: while (etbl.hasMoreElements()) {
            Table t = (Table) etbl.nextElement();
            Enumeration ecol = t.enumerateColumn();
            while (ecol.hasMoreElements()) {
                Column col = (Column) ecol.nextElement();
                if (col.getVisable() == null || col.getVisable().equalsIgnoreCase("true")) {
                    if (col.getName().equalsIgnoreCase(colName)) {
                        table = t;
                        break OUTER;
                    }
                }
            }
        }

        return table;
    }

    /**
     * Return a count of the number of tables defined.
     * 
     * @return the number of tables in the schema
     */
    public int getTableCount() {
        return getDatabaseSchema().getTableCount();
    }

    /**
     * Construct a joining expression necessary to join the given table to the
     * primary table.
     * 
     * @param t
     *            the table to create the expression for
     * @return a string representing the joining expression or "" if no
     *         expression is found
     */
    public String constructJoinExprForTable(Table t) {
        StringBuffer buf = new StringBuffer();

        // change this to use getPrimaryJoinsForTable
        Join[] joins = getPrimaryJoinsForTable(t);
        for (int i = 0; i < joins.length; i++) {
            Join j = joins[i];
            if (i != 0)
                buf.append(" AND ");
            buf.append(i == 0 ? t.getName() : joins[i - 1].getTable()).append('.').append(j.getColumn());
            buf.append(" = ");
            buf.append(j.getTable()).append('.').append(j.getTableColumn());
        }
        return buf.toString();
    }

    /**
     * Returns an array of the names of tables involved in a join of the given
     * table and the primary. The tables are listing in order starting with
     * primary and moving 'toward' the given table.
     * 
     * @param t
     *            the Table to join
     * @return an array containing the names of the tables involved. If t is the
     *         primary or there is no join that reaches the primary table from
     *         t, then join list is a zero-length aarray
     */
    public String[] getJoinTablesForTable(Table t) {
        Join[] joins = getPrimaryJoinsForTable(t);
        String[] tables = new String[joins.length + 1];
        tables[joins.length] = t.getName();
        for (int i = 0; i < joins.length; i++) {
            // put these in reverse order so they are from primary toward 't'
            tables[joins.length - 1 - i] = joins[i].getTable();
        }
        return tables;
    }

    /**
     * Get the sequence of joins that are necessary to joint table t to the
     * primary table.
     * 
     * @param t
     *            the table to join
     * @return a list of the join objects for all the tables between the given
     *         table and the primary or a zero-length array if t is the primary
     *         or no join exists.
     */
    public Join[] getPrimaryJoinsForTable(Table t) {
        Table primary = getPrimaryTable();

        Join j = m_primaryJoins.get(t.getName());
        List<Join> joins = new ArrayList<Join>();
        while (j != null && j.getTable() != null && !j.getTable().equals(primary.getName())) {
            joins.add(j);
            j = m_primaryJoins.get(j.getTable());
        }

        if (j != null) {
            joins.add(j);
        }

        return joins.toArray(new Join[joins.size()]);
    }

    /**
     * 
     */
    private void finishConstruction() {
        Table primary = getPrimaryTable();
        Set<String> joinableSet = new HashSet<String>();
        Map<String, Join> primaryJoins = new HashMap<String, Join>();
        joinableSet.add(primary.getName());
        int joinableCount = 0;
        // loop until we stop adding entries to the set
        while (joinableCount < joinableSet.size()) {
            joinableCount = joinableSet.size();
            Set<String> newSet = new HashSet<String>(joinableSet);
            Enumeration e = getDatabaseSchema().enumerateTable();
            // for each table not already in the set
            while (e.hasMoreElements()) {
                Table t = (Table) e.nextElement();
                if (!joinableSet.contains(t.getName()) && (t.getVisable() == null || t.getVisable().equalsIgnoreCase("true"))) {
                    Enumeration ejoin = t.enumerateJoin();
                    // for each join does it join a table in the set?
                    while (ejoin.hasMoreElements()) {
                        Join j = (Join) ejoin.nextElement();
                        if (joinableSet.contains(j.getTable())) {
                            newSet.add(t.getName());
                            primaryJoins.put(t.getName(), j);
                        }
                    }
                }
            }
            joinableSet = newSet;
        }
        // FIXME: m_joinable is never read
        //m_joinable = Collections.synchronizedSet(joinableSet);
        m_primaryJoins = Collections.synchronizedMap(primaryJoins);
    }

}