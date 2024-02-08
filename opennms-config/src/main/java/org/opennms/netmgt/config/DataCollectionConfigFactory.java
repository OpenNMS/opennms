/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
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
public abstract class DataCollectionConfigFactory {
	
    /**
     * The singleton instance of this factory
     */
    private static DataCollectionConfigDao m_singleton = null;

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.api.DataCollectionConfigDao} object.
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
        // In theory, this method should not be called, as the factory will be initialized by Spring.
        // The file container inside DefaultDataCollectionConfigDao should handle the configuration reloading.
        if (m_singleton == null) {
            m_singleton = BeanUtils.getBean("daoContext", "dataCollectionConfigDao", DataCollectionConfigDao.class);
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
        if (m_singleton == null)
            throw new IllegalStateException("The factory has not been initialized");
        m_singleton.reload();
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
            // Because DataCollectionConfigFactory.init() requires Spring initialization, it is better to instantiate a local copy
            // for testing the data collection configuration.
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
            DefaultDataCollectionConfigDao config = new DefaultDataCollectionConfigDao();
            config.setConfigResource(new FileSystemResource(cfgFile));
            config.afterPropertiesSet();
            config.getConfiguredResourceTypes();
            System.out.println("OK: no errors found");
        } catch (Throwable e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}
