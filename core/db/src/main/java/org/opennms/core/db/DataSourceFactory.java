/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

    private static DataSourceConfigurationFactory m_dataSourceConfigFactory;

    private static final Map<String, DataSource> m_dataSources = new ConcurrentHashMap<>();

    private static final List<Runnable> m_closers = new LinkedList<>();

    private static ClosableDataSource parseDataSource(final String dsName) {
        String factoryClass = null;

        ConnectionPool connectionPool = m_dataSourceConfigFactory.getConnectionPool();
        factoryClass = connectionPool.getFactory();

    	ClosableDataSource dataSource = null;
		final String defaultClassName = DEFAULT_FACTORY_CLASS.getName();
    	try {
    		final Class<?> clazz = Class.forName(factoryClass);
    		final Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { JdbcDataSource.class });
    		dataSource = (ClosableDataSource)constructor.newInstance(new Object[] { m_dataSourceConfigFactory.getJdbcDataSource(dsName) });
    	} catch (final Throwable t) {
    		LOG.debug("Unable to load {}, falling back to the default dataSource ({})", factoryClass, defaultClassName, t);
    		try {
				final Constructor<?> constructor = ((Class<?>) DEFAULT_FACTORY_CLASS).getConstructor(new Class<?>[] { JdbcDataSource.class });
				dataSource = (ClosableDataSource)constructor.newInstance(new Object[] { m_dataSourceConfigFactory.getJdbcDataSource(dsName) });
			} catch (final Throwable cause) {
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
