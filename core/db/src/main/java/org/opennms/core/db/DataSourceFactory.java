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
package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This is the singleton class used to load the OpenNMS database configuration
 * from the opennms-datasources.xml. This provides convenience methods to create
 * database connections to the database configured in this default XML.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public abstract class DataSourceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceFactory.class);

    //private static final Class<?> DEFAULT_FACTORY_CLASS = AtomikosDataSourceFactory.class;
    //private static final Class<?> DEFAULT_FACTORY_CLASS = C3P0ConnectionFactory.class;
    private static final Class<?> DEFAULT_FACTORY_CLASS = HikariCPConnectionFactory.class;

    private static final String DEFAULT_DS_NAME = "opennms";

    private static DataSourceConfigurationFactory m_dataSourceConfigFactory;

    private static final Map<String, DataSource> m_dataSources = new ConcurrentHashMap<>();

    private static final List<Runnable> m_closers = new LinkedList<>();

    private static ClosableDataSource parseDataSource(final String dsName) {
        final var jdbcDataSource = m_dataSourceConfigFactory.getJdbcDataSource(dsName);

        final var connectionPool = ConnectionPool.merge(jdbcDataSource.getConnectionPool(), m_dataSourceConfigFactory.getConnectionPool());
        final var factoryClass = connectionPool.getFactory();

    	ClosableDataSource dataSource = null;
		final String defaultClassName = DEFAULT_FACTORY_CLASS.getName();
        try {
    		final Class<?> clazz = Class.forName(factoryClass);
    		final Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { JdbcDataSource.class });
    		dataSource = (ClosableDataSource)constructor.newInstance(new Object[] {jdbcDataSource});
    	} catch (final Throwable t) {
    		LOG.debug("Unable to load {}, falling back to the default dataSource ({})", factoryClass, defaultClassName, t);
    		try {
				final Constructor<?> constructor = ((Class<?>) DEFAULT_FACTORY_CLASS).getConstructor(new Class<?>[] { JdbcDataSource.class });
				dataSource = (ClosableDataSource)constructor.newInstance(new Object[] {jdbcDataSource});
			} catch (final Throwable cause) {
			    if (isUnfilteredConfigException(cause)) {
			        throw new IllegalArgumentException("Failed to load " + defaultClassName + " because the configuration is unfiltered. If you see this in a unit/integration test, you can ignore it.");
			    }
				LOG.error("Unable to load {}.", DEFAULT_FACTORY_CLASS.getName(), cause);
				throw new IllegalArgumentException("Unable to load " + defaultClassName + ".", cause);
			}
    	}
    	
    	if (connectionPool != null) {
    		dataSource.setIdleTimeout(connectionPool.getIdleTimeout());
    		try {
    			dataSource.setLoginTimeout(connectionPool.getLoginTimeout());
    		} catch (SQLException e) {
    			LOG.warn("Exception thrown while trying to set login timeout on datasource", e);
    		}
    		dataSource.setMinPool(connectionPool.getMinPool());
    		dataSource.setMaxPool(connectionPool.getMaxPool());
    		dataSource.setMaxSize(connectionPool.getMaxSize());
    	}

    	return dataSource;
    }

    private static boolean isUnfilteredConfigException(final Throwable cause) {
        if (cause.getCause() == null) {
            return cause.getMessage() != null && cause.getMessage().contains("${install.database.driver}");
        }
        if (cause.getMessage() != null && cause.getMessage().contains("${install.database.driver}")) {
            return true;
        }
        return isUnfilteredConfigException(cause.getCause());
    }

    /**
     * @deprecated This function is no longer necessary for DataSourceFactory initialization
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws PropertyVetoException
     * @throws SQLException
     */
    public static synchronized void init() throws IOException, ClassNotFoundException, PropertyVetoException, SQLException {
    }

    /**
     * <p>init</p>
     *
     * @param dsName a {@link java.lang.String} object.
     */
    public static synchronized void init(final String dsName) {
        if (isLoaded(dsName)) {
            // init already called, return
            return;
        }

        // If a data source configuration factory wasn't set, try to load the default settings
        if (m_dataSourceConfigFactory == null) {
            try {
                m_dataSourceConfigFactory = new DataSourceConfigurationFactory(ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME));
            } catch (IOException e) {
                LOG.warn("Could not parse default data source configuration", e);
                m_dataSourceConfigFactory = null;
            }
        }

        try {
            final ClosableDataSource dataSource = parseDataSource(dsName);

            m_closers.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        dataSource.close();
                    } catch (final Throwable cause) {
                            LOG.info("Unable to close datasource {}.", dsName, cause);
                    }
                }
            });

            setInstance(dsName, dataSource);
        } catch (final Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("configuration is unfiltered")) {
                LOG.warn(e.getMessage());
                return;
            }
            throw e;
        }
    }

    /**
     * @return true if the default datasource is loaded, false otherwise
     */
    public static synchronized boolean isDefaultDsLoaded() {
        return isLoaded(DEFAULT_DS_NAME);
    }

    private static synchronized boolean isLoaded(final String dsName) {
        return m_dataSources.containsKey(dsName);			
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
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static DataSource getInstance() {
        return getInstance(DEFAULT_DS_NAME);
    }

    /**
     * <p>getInstance</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link javax.sql.DataSource} object.
     */
    public static synchronized DataSource getInstance(final String name) {
        init(name);
        return m_dataSources.get(name);
    }

    /**
     * <p>setInstance</p>
     *
     * @param ds a {@link javax.sql.DataSource} object.
     */
    public static void setInstance(final DataSource ds) {
        setInstance(DEFAULT_DS_NAME, ds);
    }

    /**
     * <p>setInstance</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @param ds a {@link javax.sql.DataSource} object.
     */
    public static synchronized void setInstance(final String dsName, final DataSource ds) {
        DataSource oldDs = m_dataSources.put(dsName, ds);
        if (oldDs != null) {
            // TODO: Call the closer Runnable?
        }
    }

    /**
     */
    public static synchronized void setDataSourceConfigurationFactory(final DataSourceConfigurationFactory factory) {
        // Close any existing datasources
        close();
        m_dataSourceConfigFactory = factory;
    }

    /**
     * <p>close</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public static synchronized void close() {
        
        for(Runnable closer : m_closers) {
            closer.run();
        }
        
        m_closers.clear();
        m_dataSources.clear();
    }
}
