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
// 2008 Jan 08: Use JOIN rather than WHERE to join tables, to allow outer joins
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.filter.Column;
import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.filter.Join;
import org.opennms.netmgt.config.filter.Table;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.springframework.core.io.Resource;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * database schema for the filters from the database-schema XML file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
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
     * @throws ValidationException 
     * @throws MarshalException 
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private DatabaseSchemaConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgStream = null;
        try {
            cfgStream = new FileInputStream(configFile);
            m_config = CastorUtils.unmarshal(DatabaseSchema.class, cfgStream, CastorUtils.PRESERVE_WHITESPACE);
            finishConstruction();
        } finally {
            if (cfgStream != null) {
                IOUtils.closeQuietly(cfgStream);
            }
        }
    }

    /**
     * <p>Constructor for DatabaseSchemaConfigFactory.</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Deprecated
    public DatabaseSchemaConfigFactory(Reader reader) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(DatabaseSchema.class, reader, CastorUtils.PRESERVE_WHITESPACE);
        finishConstruction();
    }

    /**
     * <p>Constructor for DatabaseSchemaConfigFactory.</p>
     *
     * @param is a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public DatabaseSchemaConfigFactory(InputStream is) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(DatabaseSchema.class, is, CastorUtils.PRESERVE_WHITESPACE);
        finishConstruction();
    }

    public DatabaseSchemaConfigFactory(final Resource rs) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(DatabaseSchema.class, rs.getInputStream(), CastorUtils.PRESERVE_WHITESPACE);
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
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DatabaseSchemaConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.DatabaseSchemaConfigFactory} object.
     */
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
        Enumeration<Table> e = getDatabaseSchema().enumerateTable();
        while (e.hasMoreElements()) {
            Table t = e.nextElement();
            if (t.getVisible() == null || t.getVisible().equalsIgnoreCase("true")) {
                if (t.getKey() != null && t.getKey().equals("primary")) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Construct m_primaryJoins
     */
    private void finishConstruction() {
        Set<String> joinableSet = new HashSet<String>();
        Map<String, Join> primaryJoins = new HashMap<String, Join>();
        joinableSet.add(getPrimaryTable().getName());
        // loop until we stop adding entries to the set
        int joinableCount = 0;
        while (joinableCount < joinableSet.size()) {
            joinableCount = joinableSet.size();
            Set<String> newSet = new HashSet<String>(joinableSet);
            Enumeration<Table> e = getDatabaseSchema().enumerateTable();
            // for each table not already in the set
            while (e.hasMoreElements()) {
                Table t = e.nextElement();
                if (!joinableSet.contains(t.getName()) && (t.getVisible() == null || t.getVisible().equalsIgnoreCase("true"))) {
                    Enumeration<Join> ejoin = t.enumerateJoin();
                    // for each join does it join a table in the set?
                    while (ejoin.hasMoreElements()) {
                        Join j = ejoin.nextElement();
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

    /**
     * Find a table using its name as the search key.
     *
     * @param name
     *            the name of the table to find
     * @return the table if it is found, null otherwise.
     */
    public Table getTableByName(String name) {
        Enumeration<Table> e = getDatabaseSchema().enumerateTable();
        while (e.hasMoreElements()) {
            Table t = e.nextElement();
            if (t.getVisible() == null || t.getVisible().equalsIgnoreCase("true")) {
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
     * @return the table containing column 'colName', null if colName is not a
     *         valid column or if is not visible.
     * @param colName a {@link java.lang.String} object.
     */
    public Table findTableByVisibleColumn(String colName) {
        Table table = null;

        Enumeration<Table> etbl = getDatabaseSchema().enumerateTable();
        OUTER: while (etbl.hasMoreElements()) {
            Table t = etbl.nextElement();
            Enumeration<Column> ecol = t.enumerateColumn();
            while (ecol.hasMoreElements()) {
                Column col = ecol.nextElement();
                if (col.getVisible() == null || col.getVisible().equalsIgnoreCase("true")) {
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
     * Return the sequence of tables necessary to join the primary table to the
     * given tables.
     *
     * @param tables
     *            list of Tables to join
     * @return a list of table names, starting with the primary table, going
     *         to each of the given tables, or a zero-length array if no join
     *         exists or only the primary table was specified
     */
    public List<String> getJoinTables(List<Table> tables) {
        List<String> joinedTables = new ArrayList<String>();

        for (int i = 0; i < tables.size(); i++) {
            int insertPosition = joinedTables.size();
            String currentTable = tables.get(i).getName();
            while (currentTable != null && !joinedTables.contains(currentTable)) {
                joinedTables.add(insertPosition, currentTable);
                Join next = m_primaryJoins.get(currentTable);
                if (next != null) {
                    currentTable = next.getTable();
                } else {
                    currentTable = null;
                }
            }
        }

        return joinedTables;
    }

    /**
     * Construct a SQL FROM clause joining the given tables to the primary table.
     *
     * @param tables
     *            list of Tables to join
     * @return an SQL FROM clause or "" if no expression is found
     */
    public String constructJoinExprForTables(List<Table> tables) {
        StringBuffer joinExpr = new StringBuffer();

        List<String> joinTables = getJoinTables(tables);
        joinExpr.append(joinTables.get(0));
        for (int i = 1; i < joinTables.size(); i++) {
            Join currentJoin = m_primaryJoins.get(joinTables.get(i));
            if (currentJoin.getType() != null && !currentJoin.getType().equalsIgnoreCase("inner")) {
              joinExpr.append(" " + currentJoin.getType().toUpperCase());
            }
            joinExpr.append(" JOIN " + joinTables.get(i) + " ON (");
            joinExpr.append(currentJoin.getTable() + "." + currentJoin.getTableColumn() + " = ");
            joinExpr.append(joinTables.get(i) + "." + currentJoin.getColumn() + ")");
        }

        if (joinExpr.length() > 0)
            return "FROM " + joinExpr.toString();
        return "";
    }
}
