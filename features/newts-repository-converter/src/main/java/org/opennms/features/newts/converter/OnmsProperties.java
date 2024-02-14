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
package org.opennms.features.newts.converter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

/**
 * The Class OnmsProperties.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class OnmsProperties {

    /**
     * Register properties.
     *
     * @param properties the properties
     */
    private static void registerProperties(Properties properties) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            if (System.getProperty(key) == null) {
                System.setProperty(key, properties.getProperty(key));
            }
        }
    }

    /**
     * Load properties.
     *
     * @param properties the properties
     * @param fileName the file name
     * @throws Exception the exception
     */
    private static void loadProperties(Properties properties, String fileName) throws Exception {
        File propertiesFile = ConfigFileConstants.getConfigFileByName(fileName);
        properties.load(new FileInputStream(propertiesFile));
    }

    /**
     * Initialize.
     */
    public static void initialize() {
        try {
            final Properties mainProperties = new Properties();
            loadProperties(mainProperties, "opennms.properties");
            registerProperties(mainProperties);

            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
            final DataSourceConfiguration dsc = JaxbUtils.unmarshal(DataSourceConfiguration.class, cfgFile);

            boolean found = false;
            for (JdbcDataSource jds : dsc.getJdbcDataSourceCollection()) {
                if (jds.getName().equals("opennms")) {
                    SimpleDataSource ds = new SimpleDataSource(jds);
                    DataSourceFactory.setInstance(ds);
                    found = true;
                }
            }
            if (!found) {
                throw NewtsConverterError.create("Can't find OpenNMS database configuration");
            }
        } catch (Exception e) {
            throw NewtsConverterError.create(e, "Can't initialize OpenNMS database connection factory: {}", e.getMessage());
        }
    }
}
