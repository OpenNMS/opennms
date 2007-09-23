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

package org.opennms.netmgt.vmmgr;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.jdo.conf.Param;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
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
public class DatabaseChecker {
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
     * Protected constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    protected DatabaseChecker(String configFile) throws IOException,
						      MarshalException,
						      ValidationException,
						      ClassNotFoundException {
        Class<DataSourceConfiguration> dsc = DataSourceConfiguration.class;

        // Set the system identifier for the source of the input stream.
        // This is necessary so that any location information can
        // positively identify the source of the error.
        //
        InputSource dbIn = new InputSource(new FileInputStream(configFile));
        dbIn.setSystemId(configFile);

        // Attempt to load the database reference.
        //
        DataSourceConfiguration m_database = (DataSourceConfiguration) Unmarshaller.unmarshal(dsc, dbIn);

        /*
        Param[] parms = m_database.getDatabaseChoice().getDriver().getParam();
        for (int i = 0; i < parms.length; i++) {
        	if (parms[i].getName().equals("user")) {
        		m_driverUser = parms[i].getValue();
        	} else if (parms[i].getName().equals("password")) {
        		m_driverPass = parms[i].getValue();
        	} else {
        		throw new ValidationException("Unsupported JDO parameter: " +
        				parms[i].getName());
        	}
        }
        */
        
        Collection<JdbcDataSource> jdbcDataSources = m_database.getJdbcDataSourceCollection();
        for (JdbcDataSource jdbcDataSource : jdbcDataSources) {
			m_driverUrl = jdbcDataSource.getUrl();
			m_driverUser = jdbcDataSource.getUserName();
			m_driverPass = jdbcDataSource.getPassword();
	        String driverCN = jdbcDataSource.getClassName();
	        Class.forName(driverCN);
		}
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
    protected DatabaseChecker() throws IOException, MarshalException, ValidationException, ClassNotFoundException {
    	this(ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME).getPath());
    }

    public void check() throws SQLException {
    	Connection c = DriverManager.getConnection(m_driverUrl, m_driverUser, m_driverPass);
    	c.close();
    }

    public static void main(String[] argv) throws Exception {
    	DatabaseChecker checker = new DatabaseChecker();
    	checker.check();
    }
}
