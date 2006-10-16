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

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
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
public final class DatabaseConnectionFactory {

    /**
     * The singleton instance of this factory
     */
    private static DbConnectionFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Use the old Legacy code or the C3P0 based classes
     */
    private static boolean m_legacy = Boolean.getBoolean("opennms.db.legacy");


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
        
        FileInputStream in = null;
        Database database = null;
        try {
        	File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DB_CONFIG_FILE_NAME);
        	String configFile = cfgFile.getPath();
        	Class dsc = Database.class;

        	in = new FileInputStream(configFile);
        	// Set the system identifier for the source of the input stream.
        	// This is necessary so that any location information can
        	// positively identify the source of the error.
        	//
        	InputSource dbIn = new InputSource(in);
        	dbIn.setSystemId(configFile);

        	// Attempt to load the database reference.
        	//
        	database = (Database) Unmarshaller.unmarshal(dsc, dbIn);
        	
        	DbConfiguration dbConfig = new DbConfiguration(database);

            m_singleton = (m_legacy 
                    ? new LegacyDbConnectionFactory(dbConfig)
                    : new C3P0DbConnectionFactory(dbConfig)
                    );
            m_loaded = true;

        } finally {
        	if (in != null) in.close();
        }
		
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


}
