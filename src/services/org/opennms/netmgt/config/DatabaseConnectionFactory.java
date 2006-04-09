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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;

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
     * The singleton instance of this factory
     */
    private static DbConnectionFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    private static boolean m_legacy = false;

    /**
     * The database class loaded from the config file
     */
    private Database m_database = null;

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
     * @throws SQLException 
     * @throws PropertyVetoException 
     * 
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException, ClassNotFoundException, PropertyVetoException, SQLException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DB_CONFIG_FILE_NAME);
        try {
            if (!isLegacy()) {
                m_singleton = new C3P0ConnectionFactory(cfgFile.getPath());                
            } else {
                LegacyDatabaseConnectionFactory.init();
                m_singleton = LegacyDatabaseConnectionFactory.getInstance();
            }
        } catch (MarshalException e) {
            throw e;
        } catch (ValidationException e) {
            throw e;
        } catch (FileNotFoundException e) {
            throw e;
        } catch (PropertyVetoException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        }
        m_loaded = true;
    }

    public static boolean isLegacy() {
        return m_legacy;
    }
    
    public static void setLegacy(boolean legacy) {
        m_legacy = legacy;
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
        return m_singleton.getConnection();
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
    public javax.sql.DataSource getDataSource() {
        return m_singleton;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return m_singleton.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        m_singleton.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        m_singleton.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return m_singleton.getLoginTimeout();
    }

    public void initialize() throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
        
    }


}
