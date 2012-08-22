/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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
import java.io.IOException;

import org.opennms.core.utils.ConfigFileConstants;
import org.springframework.core.io.FileSystemResource;

/**
 * <p>This class is the main repository for SNMP data collection configuration
 * information used by the SNMP service monitor. When this class is loaded it
 * reads the SNMP data collection configuration into memory.</p>
 * <p>The implementation of DataCollectionConfig interface has been moved to
 * DefaultDataCollectionConfigDao.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 */
public final class DataCollectionConfigFactory {
	
    /**
     * The singleton instance of this factory
     */
    private static DataCollectionConfigDao m_singleton = null;

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.DataCollectionConfigDao} object.
     */
    public static void setInstance(DataCollectionConfigDao instance) {
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
    public static synchronized DataCollectionConfigDao getInstance() {
        if (m_singleton == null)
            throw new IllegalStateException("The factory has not been initialized");
        return m_singleton;
    }
    
    public static void main(String[] args) {
        try {
            DataCollectionConfigFactory.init();
            DataCollectionConfigDao config = DataCollectionConfigFactory.getInstance();
            if (config == null) {
                System.err.println("ERROR: can't get a reference to DataCollectionConfig object");
            } else {
                config.getConfiguredResourceTypes();
                System.out.println("OK: no errors found");
            }
        } catch (Throwable e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}
