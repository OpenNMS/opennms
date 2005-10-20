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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.virtual.PassiveStatusConfiguration;
import org.opennms.netmgt.config.virtual.PassiveStatusUei;

/**
 * This is the singleton class used to load the configuration from the
 * passive-status-configuration.xml. This provides convenience methods to get the configured
 * categories and their information, add/delete categories from category groups.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class PassiveStatusConfigFactory implements PassiveStatusConfig {
    /**
     * The singleton instance of this factory
     */
    private static PassiveStatusConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private PassiveStatusConfiguration m_config;

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
     * 
     */
    private PassiveStatusConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        Reader rdr = new InputStreamReader(new FileInputStream(configFile));
        marshallReader(rdr);
        rdr.close();
    }
    
    public PassiveStatusConfigFactory(Reader rdr) throws MarshalException, ValidationException {
        marshallReader(rdr);
    }
    
    private synchronized PassiveStatusConfiguration marshallReader(Reader rdr) throws MarshalException, ValidationException {
        m_config = (PassiveStatusConfiguration) Unmarshaller.unmarshal(PassiveStatusConfiguration.class, rdr);
        return m_config;
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

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CATEGORIES_CONF_FILE_NAME);

        m_singleton = new PassiveStatusConfigFactory(cfgFile.getPath());

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
    public static synchronized PassiveStatusConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("getInstance: The factory has not been initialized");

        return m_singleton;
    }

	public static void setInstance(PassiveStatusConfig singleton) {
		m_singleton=singleton;
		m_loaded=true;
	}
	
    /**
     * Return the PassiveStatus configuration.
     * 
     * @return the PassiveStatus configuration
     */
    public synchronized PassiveStatusConfiguration getConfig() {
        return m_config;
    }

    public List getUEIList() {
        List passiveStatusUeis = getConfig().getPassiveStatusUeiCollection();
        List ueis = new ArrayList();
        for (Iterator it = passiveStatusUeis.iterator(); it.hasNext();) {
            PassiveStatusUei psu = (PassiveStatusUei) it.next();
            ueis.add(psu.getValue());
        }
        return ueis;
    }

}
