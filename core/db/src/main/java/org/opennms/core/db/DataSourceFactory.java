/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
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

    // alternatives: AtomikosDataSourceFactory, C3P0ConnectionFactory
    private static final Class<?> DEFAULT_FACTORY_CLASS = HikariCPConnectionFactory.class;

    private static DataSourceConfigurationFactory m_dataSourceConfigFactory;

    private static final Map<String, DataSource> m_dataSources = new ConcurrentHashMap<>();

    private static final Map<DataSource, Closer> m_closers = new ConcurrentHashMap<>();

    protected DataSourceFactory() {
    }

    private static ClosableDataSource parseDataSource(final String dsName) {
        String factoryClass = null;

        ConnectionPool connectionPool = m_dataSourceConfigFactory.getConnectionPool();
        factoryClass = connectionPool.getFactory();

    	ClosableDataSource dataSource = null;
		final String defaultClassName = DEFAULT_FACTORY_CLASS.getName();
    	try {
    		final Class<?> clazz = Class.forName(factoryClass);
    		final Constructor<?> constructor = clazz.getConstructor(JdbcDataSource.class);
    		dataSource = (ClosableDataSource)constructor.newInstance(m_dataSourceConfigFactory.getJdbcDataSource(dsName));
    	} catch (final Exception e) {
    		LOG.debug("Unable to load {}, falling back to the default dataSource ({})", factoryClass, defaultClassName, e);
    		try {
				final Constructor<?> constructor = ((Class<?>) DEFAULT_FACTORY_CLASS).getConstructor(JdbcDataSource.class);
				dataSource = (ClosableDataSource)constructor.newInstance(m_dataSourceConfigFactory.getJdbcDataSource(dsName));
			} catch (final Exception cause) {
			    if (isUnfilteredConfigException(cause)) {
			        throw new IllegalArgumentException("Failed to load " + defaultClassName + " because the configuration is unfiltered. If you see this in a unit/integration test, you can ignore it.");
			    }
				LOG.error("Unable to load {}.", DEFAULT_FACTORY_CLASS.getName(), cause);
				throw new IllegalArgumentException("Unable to load " + defaultClassName + ".", cause);
			}
    	}
    	
		dataSource.setIdleTimeout(connectionPool.getIdleTimeout());
		try {
			dataSource.setLoginTimeout(connectionPool.getLoginTimeout());
		} catch (SQLException e) {
			LOG.warn("Exception thrown while trying to set login timeout on datasource", e);
		}
		dataSource.setMinPool(connectionPool.getMinPool());
		dataSource.setMaxPool(connectionPool.getMaxPool());
		dataSource.setMaxSize(connectionPool.getMaxSize());
    	
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

            m_closers.put(dataSource, new Closer(dsName, dataSource));

            setInstance(dsName, dataSource);
        } catch (final Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("configuration is unfiltered")) {
                LOG.warn(e.getMessage());
                return;
            }
            throw e;
        }
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
        return getInstance("opennms");
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
        setInstance("opennms", ds);
    }

    /**
     * <p>setInstance</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @param ds a {@link javax.sql.DataSource} object.
     */
    public static synchronized void setInstance(final String dsName, final DataSource ds) {
        DataSource oldDs = m_dataSources.put(dsName, ds);
        if (oldDs != null && m_closers.containsKey(oldDs)) {
            m_closers.remove(oldDs).run();
        }
    }

    public static synchronized void setDataSourceConfigurationFactory(final DataSourceConfigurationFactory factory) {
        // Close any existing datasources
        close();
        m_dataSourceConfigFactory = factory;
    }

    public static synchronized void close() {
        
        for(final Runnable closer : m_closers.values()) {
            closer.run();
        }
        
        m_closers.clear();
        m_dataSources.clear();
    }

    public static class Closer implements Runnable {
        final String m_dsName;
        final ClosableDataSource m_dataSource;

        public Closer(final String dsName, final ClosableDataSource dataSource) {
            m_dsName = dsName;
            m_dataSource = dataSource;
        }

        @Override
        public void run() {
            try {
                m_dataSource.close();
            } catch (final Exception cause) {
                LOG.warn("Unable to close datasource {}.", m_dsName, cause);
            }
        }
    }
}
