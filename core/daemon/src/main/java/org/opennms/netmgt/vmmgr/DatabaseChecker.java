/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

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
 */
public class DatabaseChecker {
	
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseChecker.class);
	
    private static List<String> m_required = new ArrayList<String>();
    private static List<String> m_optional = new ArrayList<String>();
    private Map<String,JdbcDataSource> m_dataSources = new HashMap<String,JdbcDataSource>();

    static {
        m_required.add("opennms");
        m_optional.add("opennms-admin");
    }
    
    /**
     * Protected constructor
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @param configFile a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    protected DatabaseChecker(final String configFile) throws IOException, MarshalException, ValidationException, ClassNotFoundException {
        final DataSourceConfiguration database = CastorUtils.unmarshal(DataSourceConfiguration.class, new FileSystemResource(configFile), false);

        for (final JdbcDataSource dataSource : database.getJdbcDataSourceCollection()) {
            m_dataSources.put(dataSource.getName(), dataSource);
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
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    protected DatabaseChecker() throws IOException, MarshalException, ValidationException, ClassNotFoundException {
    	this(ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME).getPath());
    }

    /**
     * <p>Check whether the data sources in opennms-datasources.xml are valid.</p>
     *
     * @throws MissingDataSourceException A required data source was not found in opennms-datasources.xml.
     * @throws InvalidDataSourceException A required data source could not be connected to.
     */
    public void check() throws MissingDataSourceException, InvalidDataSourceException {

        // First, check to make sure the required datasources are there.
        boolean dataSourcesFound = true;
        for (final String dataSource : m_required) {
            if (!m_dataSources.containsKey(dataSource)) {
            	LOG.error("Required data source '{}' is missing from opennms-datasources.xml", dataSource);
                dataSourcesFound = false;
            }
        }
        if (!dataSourcesFound) {
            throw new MissingDataSourceException("OpenNMS is missing one or more data sources required for startup.");
        }

        // Then, check for the optional ones so we can warn about them going missing.
        for (final String dataSource : m_optional) {
            if (!m_dataSources.containsKey(dataSource)) {
            	LOG.info("Data source '{}' is missing from opennms-datasources.xml", dataSource);
            }
        }
        
        // Finally, try connecting to all data sources, and warn or error as appropriate.
        for (final JdbcDataSource dataSource : m_dataSources.values()) {
            final String name = dataSource.getName();
            if (!m_required.contains(name) && !m_optional.contains(name)) {
            	LOG.warn("Unknown datasource '{}' was found.", name);
            }
            try {
                Class.forName(dataSource.getClassName());
                final Connection connection = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUserName(), dataSource.getPassword());
                connection.close();
            } catch (final Throwable t) {
                final String errorMessage = "Unable to connect to data source '{}' at URL '{}' with username '{}', check opennms-datasources.xml and your database permissions.";
            	LOG.error(errorMessage, name, dataSource.getUrl(), dataSource.getUserName());
                if (m_required.contains(name)) {
                    throw new InvalidDataSourceException("Data source '" + name + "' failed.", t);
                }
            }
        }

    }

    /**
     * <p>main</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(final String[] argv) throws Exception {
        final DatabaseChecker checker = new DatabaseChecker();
    	checker.check();
    }
}
