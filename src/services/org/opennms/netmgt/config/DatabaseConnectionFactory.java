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
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.jdo.conf.DataSource;
import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.jdo.conf.Driver;
import org.exolab.castor.jdo.conf.Jndi;
import org.exolab.castor.jdo.conf.Mapping;
import org.exolab.castor.jdo.conf.Param;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.xml.sax.InputSource;

/**
 * <p>
 * This is the singleton class used to load the OpenNMS database configuration
 * from the opennms-database.xml. This provides convenience methods to create
 * database connections to the database configured in this default xml
 * </p>
 * 
 * <p>
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods
 * </p>
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class DatabaseConnectionFactory implements DbConnectionFactory {
    /**
     * The maximum age before a connection is closed
     */
    private static final int MAX_AGE = 5 * 60 * 1000;

    /**
     * The singleton instance of this factory
     */
    private static DbConnectionFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The database class loaded from the config file
     */
    private Database m_database = null;

    /**
     * The cached database URL
     */
    private String m_driverUrl;

    /**
     * The cached database user
     */
    private String m_driverUser;

    /**
     * The cached database password.
     */
    private String m_driverPass;

    /**
     * The linked list of cached connections that are reused if the garbage
     * collector has not finalized them yet.
     */
    private LinkedList m_dbcCache = null;

    /**
     * This class is used to represent a cached database connection within this
     * factory. The cached connection may or may not have been collected by the
     * main java garbage collector thread. If the connection still exists and is
     * not in use and does not have any errors then it may be reissued to
     * another thread requestor.
     * 
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org/">OpenNMS </a>
     * 
     */
    static final class CachedConnection implements Connection {
        /**
         * The cached connection
         */
        private final Connection m_delegate;

        /**
         * The owner of the cached connection
         */
        private final DatabaseConnectionFactory m_owner;

        /**
         * This flag is set to true if the connection is currently in use.
         */
        private boolean m_inUse;

        /**
         * This flag is set to true if the connection experienced an error.
         */
        private boolean m_hadError;

        /**
         * This value is used to mark the last time the connection was returned
         * to the pool. If the last use reaches a certian time and the
         * connection is on the bottom of the stack, then it is released. This
         * time is set via the system time.
         * 
         * @see java.lang.System#currentTimeMillis
         */
        private long m_lastUse;

        /**
         * The class constructor used to create a new cached connection for a
         * particular factory.
         * 
         * @param dbc
         *            The database connection to cache.
         * @param owner
         *            The owner of the cached connection.
         * 
         */
        CachedConnection(Connection dbc, DatabaseConnectionFactory owner) {
            m_delegate = dbc;
            m_owner = owner;
            m_inUse = false;
            m_hadError = false;
            m_lastUse = System.currentTimeMillis();
        }

        /**
         * This method checks the access of the connection. If the connection is
         * not marked <em>in use</em> then access is denied via an exception.
         * 
         * @throws java.sql.SQLException
         *             Thrown if access is denied.
         */
        private synchronized void checkAccess() throws SQLException {
            if (!m_inUse) {
                ThreadCategory.getInstance(getClass()).warn("Attempting to used closed connection", new Throwable());
                throw new SQLException("The connection has already been closed");
            }
        }

        /**
         * Returns true if the cached connection is available for use.
         */
        synchronized boolean isAvailable() {
            return !m_inUse;
        }

        /**
         * Marks the connection as in use
         */
        synchronized void markUsed() {
            m_inUse = true;
        }

        /**
         * Returns true if the connection has experienced any errors.
         */
        synchronized boolean isBad() {
            return m_hadError;
        }

        /**
         * Returns the current age of the connection since it was last used in
         * milliseconds. The time is based upon the last time the connection was
         * returned to the database pool. If the connection is kept in use then
         * the value returned by this method is of no value.
         * 
         * @return The time since the connection was last closed.
         */
        synchronized long age() {
            return System.currentTimeMillis() - m_lastUse;
        }

        /**
         * Closes the SQL connection for the owner of the conneciton. The close
         * request may be forwarded to the encapsulate connection if an error
         * has occured or the connection is read-only. Otherwise the connection
         * is returned to the connection pool for reuse.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public synchronized void close() throws SQLException {
            Category log = ThreadCategory.getInstance(getClass());
            boolean isTracing = log.isDebugEnabled();

            if (!m_inUse)
                log.warn("The in use flag was not set but the connection is being closed!", new Throwable());

            try {
                // re-enable auto commit on close
                //
                if (m_delegate.getAutoCommit() == false)
                    m_delegate.setAutoCommit(true);

                // don't reuse read-only connections
                //
                if (m_delegate.isReadOnly()) {
                    if (isTracing)
                        log.debug("connection is read-only, setting bad flag");

                    m_hadError = true;
                }

                // don't reuse closed connections
                //
                if (m_delegate.isClosed()) {
                    if (isTracing)
                        log.debug("connection is closed, setting bad flag");

                    m_hadError = true;
                }
            } catch (SQLException ex) {
                if (isTracing)
                    log.debug("setting bad flag true", ex);

                m_hadError = true;
            }

            // If the connection had errors then close
            // the connection. If the connection is cool
            // then push the conneciton onto the pool
            // stack.
            //
            m_inUse = false;
            if (m_hadError) {
                m_delegate.close();
            } else // m_hadError == false
            {
                synchronized (m_owner.m_dbcCache) {
                    if (isTracing)
                        log.debug("adding connection back into pool [id=" + this + "]");

                    m_owner.m_dbcCache.addFirst(this);
                }
                m_lastUse = System.currentTimeMillis();
            }
        }

        /**
         * Forwards the request to the encapuslated connection, or returns true
         * if the connection is not in use.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public synchronized boolean isClosed() throws SQLException {
            try {
                if (m_inUse)
                    return m_delegate.isClosed();
                else
                    return true;
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public Statement createStatement() throws SQLException {
            checkAccess();
            try {
                return m_delegate.createStatement();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareStatement(sql);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareStatement(sql, autoGeneratedKeys);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareStatement(sql, columnIndexes);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareStatement(sql, columnNames);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public CallableStatement prepareCall(String sql) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareCall(sql);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public String nativeSQL(String sql) throws SQLException {
            checkAccess();
            try {
                return m_delegate.nativeSQL(sql);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            checkAccess();
            try {
                m_delegate.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public boolean getAutoCommit() throws SQLException {
            checkAccess();
            try {
                return m_delegate.getAutoCommit();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void commit() throws SQLException {
            checkAccess();
            try {
                m_delegate.commit();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void rollback() throws SQLException {
            checkAccess();
            try {
                m_delegate.rollback();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            checkAccess();
            try {
                m_delegate.rollback(savepoint);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public DatabaseMetaData getMetaData() throws SQLException {
            checkAccess();
            try {
                return m_delegate.getMetaData();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void setReadOnly(boolean readOnly) throws SQLException {
            checkAccess();
            try {
                m_delegate.setReadOnly(readOnly);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public boolean isReadOnly() throws SQLException {
            checkAccess();
            try {
                return m_delegate.isReadOnly();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void setCatalog(String catalog) throws SQLException {
            checkAccess();
            try {
                m_delegate.setCatalog(catalog);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public String getCatalog() throws SQLException {
            checkAccess();
            try {
                return m_delegate.getCatalog();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void setTransactionIsolation(int level) throws SQLException {
            checkAccess();
            try {
                m_delegate.setTransactionIsolation(level);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public int getTransactionIsolation() throws SQLException {
            checkAccess();
            try {
                return m_delegate.getTransactionIsolation();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public SQLWarning getWarnings() throws SQLException {
            checkAccess();
            try {
                return m_delegate.getWarnings();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void clearWarnings() throws SQLException {
            checkAccess();
            try {
                m_delegate.clearWarnings();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            checkAccess();
            try {
                return m_delegate.createStatement(resultSetType, resultSetConcurrency);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkAccess();
            try {
                return m_delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkAccess();
            try {
                return m_delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public Map getTypeMap() throws SQLException {
            checkAccess();
            try {
                return m_delegate.getTypeMap();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        /**
         * Forwards the request to the encapsulated connection after access to
         * the connection is granted.
         * 
         * @throws java.sql.SQLException
         *             May be thrown by the encapsulated connection
         */
        public void setTypeMap(Map map) throws SQLException {
            checkAccess();
            try {
                m_delegate.setTypeMap(map);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        public String toString() {
            return m_delegate.toString();
        }

        public int getHoldability() throws SQLException {
            try {
                return m_delegate.getHoldability();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        public void setHoldability(int holdability) throws SQLException {
            try {
                m_delegate.setHoldability(holdability);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        public Savepoint setSavepoint() throws SQLException {
            try {
                return m_delegate.setSavepoint();
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            try {
                return m_delegate.setSavepoint(name);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            try {
                m_delegate.releaseSavepoint(savepoint);
            } catch (SQLException ex) {
                m_hadError = true;
                ThreadCategory.getInstance(getClass()).debug("setting bad flag [id=" + this + "]", ex);
                throw ex;
            }
        }
    }

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
    private DatabaseConnectionFactory(String configFile) throws IOException, MarshalException, ValidationException, ClassNotFoundException {
        Class dsc = Database.class;

        // Set the system identifier for the source of the input stream.
        // This is necessary so that any location information can
        // positively identify the source of the error.
        //
        InputSource dbIn = new InputSource(new FileInputStream(configFile));
        dbIn.setSystemId(configFile);

        // Attempt to load the database reference.
        //
        m_database = (Database) Unmarshaller.unmarshal(dsc, dbIn);

        m_dbcCache = new LinkedList();

        Param[] parms = m_database.getDatabaseChoice().getDriver().getParam();
        for (int i = 0; i < parms.length; i++) {
            if (parms[i].getName().equals("user"))
                m_driverUser = parms[i].getValue();
            else if (parms[i].getName().equals("password"))
                m_driverPass = parms[i].getValue();
        }
        m_driverUrl = m_database.getDatabaseChoice().getDriver().getUrl();
        String driverCN = m_database.getDatabaseChoice().getDriver().getClassName();

        Class.forName(driverCN);
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
     * 
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException, ClassNotFoundException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DB_CONFIG_FILE_NAME);
        m_singleton = new DatabaseConnectionFactory(cfgFile.getPath());
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
    private static synchronized void reload() throws IOException, MarshalException, ValidationException, ClassNotFoundException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * <p>
     * Return the singleton instance of this factory. This is the instance of
     * the factory that was last created when the <code>
     * init</code> or
     * <code>reload</code> method was invoked. The instance will not change
     * unless a <code>reload</code> method is invoked.
     * </p>
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DbConnectionFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
	
	public static void setInstance(DbConnectionFactory singleton) {
		m_singleton=singleton;
		m_loaded=true;
	}

    /**
     * Return the database that was configured in the config file
     * 
     * @return the database that was configured in the config file
     */
    public Database getDatabase() {
        return m_database;
    }

    /**
     * Return the datasource configured for the database
     * 
     * @return the datasource configured for the database
     */
    public DataSource getDataSource() {
        return m_database.getDatabaseChoice().getDataSource();
    }

    /**
     * Return the driver configured for the database
     * 
     * @return the driver configured for the database
     */
    public Driver getDriver() {
        return m_database.getDatabaseChoice().getDriver();
    }

    /**
     * Return the engine configured for the database
     * 
     * @return the engine configured for the database
     */
    public String getEngine() {
        return m_database.getEngine();
    }

    /**
     * Return the JNDI configuration for the database
     * 
     * @return the JNDI configuration for the database
     */
    public Jndi getJndi() {
        return m_database.getDatabaseChoice().getJndi();
    }

    /**
     * Return the mapping configured for the database.
     * 
     * @return the mapping configured for the database.
     * 
     */
    public Mapping[] getMapping() {
        return m_database.getMapping();
    }

    /**
     * Return a new database connection to the database configured in the
     * <tt>opennms-database.xml</tt>. The database connection is not managed
     * by the factory and must be release by the caller by using the
     * <code>close</code> method.
     * 
     * @return a new database connection to the database configured in the
     *         <tt>opennms-database.xml</tt>
     * 
     * @throws java.sql.SQLException
     *             Thrown if there is an error opening the connection to the
     *             database.
     */
    public Connection getConnection() throws SQLException {

        // lock the database cache for the open
        //
        synchronized (m_dbcCache) {
            Category log = ThreadCategory.getInstance(getClass());
            boolean isTracing = log.isDebugEnabled();

            // look at each reference, removing those
            // that garbage collection has destroyed
            //
            CachedConnection cdbc = null;
            while (cdbc == null && !m_dbcCache.isEmpty()) {
                cdbc = (CachedConnection) m_dbcCache.removeFirst();
                synchronized (cdbc) {
                    if (cdbc.isAvailable() && cdbc.isBad()) {
                        if (isTracing)
                            log.debug("removed bad connection from pool [id=" + cdbc + "]");
                        continue;
                    } else if (cdbc.isAvailable()) {
                        // mark in use and return
                        // to caller. This will now be a
                        // strongly referenced instance
                        //
                        cdbc.markUsed();
                        if (isTracing)
                            log.debug("reusing previous connection [id=" + cdbc + "]");
                    }
                }

                if (!m_dbcCache.isEmpty()) {
                    long age = ((CachedConnection) m_dbcCache.getLast()).age();
                    if (age >= MAX_AGE) {
                        CachedConnection disconnect = (CachedConnection) m_dbcCache.removeLast();
                        if (isTracing)
                            log.debug("removing expired connection [id=" + disconnect + "]");

                        try {
                            disconnect.m_delegate.close();
                        } catch (SQLException e) {
                            if (isTracing)
                                log.debug("An error occured closing delegate", e);
                        }
                    } else if (isTracing) {
                        log.debug("stack bottom is " + age + "ms old");
                        log.debug("stack size is " + m_dbcCache.size());
                    }
                }
            }

            // unable to find one that was not in
            // use so a new one has been allocated
            //
            if (cdbc == null) {
                cdbc = new CachedConnection(DriverManager.getConnection(m_driverUrl, m_driverUser, m_driverPass), this);
                cdbc.markUsed();

                if (isTracing)
                    log.debug("created new JDBC connection, no previous reference available");
            }

            return cdbc;
        }
    }
}
