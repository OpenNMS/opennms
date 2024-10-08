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
package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the rrd configuration data.
 */
public abstract class RrdConfig {
    private static final Logger LOG = LoggerFactory.getLogger(RrdConfig.class);

    private static Properties m_properties = null;

    /**
     * This loads the configuration file.
     *
     * @return a Properties object representing the configuration properties
     * @throws java.io.IOException if any.
     */
    public static Properties getProperties() throws IOException {
        if (m_properties == null) {
            m_properties = new Properties(System.getProperties());
            InputStream in = null;
            String configFileName = null;
            // Merge the config file contents into these properties (if the file exists)
            try {
                configFileName = ConfigFileConstants.getFileName(ConfigFileConstants.RRD_CONFIG_FILE_NAME);
                File configFile = ConfigFileConstants.getFile(ConfigFileConstants.RRD_CONFIG_FILE_NAME);
                in = new FileInputStream(configFile);
                m_properties.load(in);
            } catch (FileNotFoundException e) {
                LOG.info("{} not found, loading RRD configuration solely from system properties", configFileName);
            } finally {
                if (in != null) { 
                    in.close(); 
                }
            }
        }
        return m_properties;
    }
}
