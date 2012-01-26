/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.filter.Column;
import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.filter.Join;
import org.opennms.netmgt.config.filter.Table;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * database schema for the filters from the database-schema XML file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 */
public final class DatabaseSchemaConfigFactory {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
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
    private DatabaseSchemaConfigFactory(final String configFile) throws IOException, MarshalException, ValidationException {
        InputStream cfgStream = null;
        try {
            cfgStream = new FileInputStream(configFile);
            m_config = CastorUtils.unmarshal(DatabaseSchema.class, cfgStream);
            finishConstruction();
        } finally {
            IOUtils.closeQuietly(cfgStream);
        }
    }

    /**
     * <p>Constructor for DatabaseSchemaConfigFactory.</p>
     *
     * @param is a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public DatabaseSchemaConfigFactory(final InputStream is) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(DatabaseSchema.class, is);
        finishConstruction();
    }

    public Lock getReadLock() {
        return m_readLock;
    }
    
    public Lock getWriteLock() {
        return m_writeLock;
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

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DB_SCHEMA_FILE_NAME);
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
    public static synchronized void setInstance(final DatabaseSchemaConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * Return the database schema.
     *
     * @return the database schema
     */
    public DatabaseSchema getDatabaseSchema() {
        final Lock lock = getReadLock();
		lock.lock();
        try {
            return m_config;
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method is used to find the table that should drive the construction
     * of the join clauses between all table in the from clause. At least one
     * table has to be designated as the driver table.
     *
     * @return The name of the driver table
     */
    public Table getPrimaryTable() {
        getReadLock().lock();
        try {
            for (final Table t : getDatabaseSchema().getTableCollection()) {
                if (t.getVisible() == null || t.getVisible().equalsIgnoreCase("true")) {
                    if (t.getKey() != null && t.getKey().equals("primary")) {
                        return t;
                    }
                }
            }
            return null;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Construct m_primaryJoins
     */
    private void finishConstruction() {
        Set<String> joinableSet = new HashSet<String>();
        final Map<String, Join> primaryJoins = new ConcurrentHashMap<String, Join>();
        joinableSet.add(getPrimaryTable().getName());
        // loop until we stop adding entries to the set
        int joinableCount = 0;
        while (joinableCount < joinableSet.size()) {
            joinableCount = joinableSet.size();
            final Set<String> newSet = new HashSet<String>(joinableSet);
            for (final Table t : getDatabaseSchema().getTableCollection()) {
                if (!joinableSet.contains(t.getName()) && (t.getVisible() == null || t.getVisible().equalsIgnoreCase("true"))) {
                    for (final Join j : t.getJoinCollection()) {
                        if (joinableSet.contains(j.getTable())) {
                            newSet.add(t.getName());
                            primaryJoins.put(t.getName(), j);
                        }
                    }
                }
            }
            joinableSet = newSet;
        }
        m_primaryJoins = primaryJoins;
    }

    /**
     * Find a table using its name as the search key.
     *
     * @param name
     *            the name of the table to find
     * @return the table if it is found, null otherwise.
     */
    public Table getTableByName(final String name) {
        getReadLock().lock();
        try {
            for (final Table t : getDatabaseSchema().getTableCollection()) {
                if (t.getVisible() == null || t.getVisible().equalsIgnoreCase("true")) {
                    if (t.getName() != null && t.getName().equals(name)) {
                        return t;
                    }
                }
            }
            return null;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Find the table which has a visible column named 'colName'
     *
     * @return the table containing column 'colName', null if colName is not a
     *         valid column or if is not visible.
     * @param colName a {@link java.lang.String} object.
     */
    public Table findTableByVisibleColumn(final String colName) {
        Table table = null;
        getReadLock().lock();
        try {
            OUTER: for (final Table t : getDatabaseSchema().getTableCollection()) {
                for (final Column col : t.getColumnCollection()) {
                    if (col.getVisible() == null || col.getVisible().equalsIgnoreCase("true")) {
                        if (col.getName().equalsIgnoreCase(colName)) {
                            table = t;
                            break OUTER;
                        }
                    }
                }
            }
            return table;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return a count of the number of tables defined.
     *
     * @return the number of tables in the schema
     */
    public int getTableCount() {
        final Lock lock = getReadLock();
		lock.lock();
        try {
            return getDatabaseSchema().getTableCount();
        } finally {
            lock.unlock();
        }
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
    public List<String> getJoinTables(final List<Table> tables) {
        final List<String> joinedTables = new ArrayList<String>();

        getReadLock().lock();
        try {
            for (int i = 0; i < tables.size(); i++) {
                final int insertPosition = joinedTables.size();
                String currentTable = tables.get(i).getName();
                while (currentTable != null && !joinedTables.contains(currentTable)) {
                    joinedTables.add(insertPosition, currentTable);
                    final Join next = m_primaryJoins.get(currentTable);
                    if (next != null) {
                        currentTable = next.getTable();
                    } else {
                        currentTable = null;
                    }
                }
            }
    
            return joinedTables;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Construct a SQL FROM clause joining the given tables to the primary table.
     *
     * @param tables
     *            list of Tables to join
     * @return an SQL FROM clause or "" if no expression is found
     */
    public String constructJoinExprForTables(final List<Table> tables) {
        StringBuffer joinExpr = new StringBuffer();

        getReadLock().lock();
        try {
            final List<String> joinTables = getJoinTables(tables);
            joinExpr.append(joinTables.get(0));
            for (int i = 1; i < joinTables.size(); i++) {
                final Join currentJoin = m_primaryJoins.get(joinTables.get(i));
                if (currentJoin.getType() != null && !currentJoin.getType().equalsIgnoreCase("inner")) {
                  joinExpr.append(" " + currentJoin.getType().toUpperCase());
                }
                joinExpr.append(" JOIN " + joinTables.get(i) + " ON (");
                joinExpr.append(currentJoin.getTable() + "." + currentJoin.getTableColumn() + " = ");
                joinExpr.append(joinTables.get(i) + "." + currentJoin.getColumn() + ")");
            }
    
            if (joinExpr.length() > 0) return "FROM " + joinExpr.toString();
            return "";
        } finally {
            getReadLock().unlock();
        }
    }
}
