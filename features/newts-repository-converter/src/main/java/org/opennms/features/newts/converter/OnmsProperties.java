/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
