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
// 2006 Aug 15: Formatting, rganize imports, use generics for Collections, resource type support, validate instance values. - dj@opennms.org
// 2003 Oct 20: Added minval and maxval parameters to mibObj for RRDs.
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
import java.io.IOException;

import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.dao.castor.DefaultDataCollectionConfigDao;
import org.springframework.core.io.FileSystemResource;

/**
 * <p>This class is the main repository for SNMP data collection configuration
 * information used by the SNMP service monitor. When this class is loaded it
 * reads the SNMP data collection configuration into memory.</p>
 * <p>The implementation of DataCollectionConfig interface has been moved to
 * DefaultDataCollectionConfigDao.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class DataCollectionConfigFactory {
	
    /**
     * The singleton instance of this factory
     */
    private static DataCollectionConfig m_singleton = null;

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.DataCollectionConfig} object.
     */
    public static void setInstance(DataCollectionConfig instance) {
        m_singleton = instance;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_singleton == null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
            DefaultDataCollectionConfigDao dataCollectionDao = new DefaultDataCollectionConfigDao();
            dataCollectionDao.setConfigResource(new FileSystemResource(cfgFile));
            dataCollectionDao.afterPropertiesSet();
            DataCollectionConfigFactory.setInstance(dataCollectionDao);
            m_singleton = dataCollectionDao;
        }
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        m_singleton = null;
        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     */
    public static synchronized DataCollectionConfig getInstance() {
        if (m_singleton == null)
            throw new IllegalStateException("The factory has not been initialized");
        return m_singleton;
    }
    
    public static void main(String[] args) {
        try {
            DataCollectionConfigFactory.init();
            DataCollectionConfig config = DataCollectionConfigFactory.getInstance();
            if (config == null) {
                System.err.println("ERROR: can't get a reference to DataCollectionConfig object");
            } else {
                config.getConfiguredResourceTypes();
                System.out.println("OK: no errors found");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}
