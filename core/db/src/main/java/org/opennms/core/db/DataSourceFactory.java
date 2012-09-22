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

package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

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
public final class DataSourceFactory implements DataSource {
	private static final Class<?> DEFAULT_FACTORY_CLASS = C3P0ConnectionFactory.class;

	/**
     * The singleton instance of this factory
     */
    private static DataSource m_singleton = null;

    private static final Map<String, DataSource> m_dataSources = new ConcurrentHashMap<String, DataSource>();
    
    private static final List<Runnable> m_closers = new LinkedList<Runnable>();

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
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException, ClassNotFoundException, PropertyVetoException, SQLException {
        if (!isLoaded("opennms")) {
            init("opennms");
        }
    }

    /**
     * <p>init</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     */
    public static synchronized void init(final String dsName) throws IOException, MarshalException, ValidationException, ClassNotFoundException, PropertyVetoException, SQLException {
        if (isLoaded(dsName)) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        String factoryClass = null;
        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
        DataSourceConfiguration dsc = null;
        ConnectionPool connectionPool = null;
    	FileInputStream fileInputStream = null;
    	try {
    		fileInputStream = new FileInputStream(cfgFile);
    		dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, fileInputStream);
    		connectionPool = dsc.getConnectionPool();
    		if (connectionPool != null) {
        		factoryClass = connectionPool.getFactory();
    		}
    	} finally {
    		IOUtils.closeQuietly(fileInputStream);
    	}

    	final String configPath = cfgFile.getPath();
    	ClosableDataSource dataSource = null;
		final String defaultClassName = DEFAULT_FACTORY_CLASS.getName();
    	try {
    		final Class<?> clazz = Class.forName(factoryClass);
    		final Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { String.class, String.class });
    		dataSource = (ClosableDataSource)constructor.newInstance(new Object[] { configPath, dsName });
    	} catch (final Throwable t) {
    		LogUtils.debugf(DataSourceFactory.class, t, "Unable to load %s, falling back to the default dataSource (%s)", factoryClass, defaultClassName);
    		try {
				final Constructor<?> constructor = ((Class<?>) DEFAULT_FACTORY_CLASS).getConstructor(new Class<?>[] { String.class, String.class });
				dataSource = (ClosableDataSource)constructor.newInstance(new Object[] { configPath, dsName });
			} catch (final Throwable cause) {
				LogUtils.errorf(DataSourceFactory.class, cause, "Unable to load %s.", DEFAULT_FACTORY_CLASS.getName());
				throw new SQLException("Unable to load " + defaultClassName + ".", cause);
			}
    	}

    	final ClosableDataSource runnableDs = dataSource;
        m_closers.add(new Runnable() {
            public void run() {
                try {
                    runnableDs.close();
                } catch (final Throwable cause) {
                	LogUtils.infof(DataSourceFactory.class, cause, "Unable to close datasource %s.", dsName);
                }
            }
        });
        
    	if (connectionPool != null) {
    		dataSource.setIdleTimeout(connectionPool.getIdleTimeout());
    		dataSource.setLoginTimeout(connectionPool.getLoginTimeout());
    		dataSource.setMinPool(connectionPool.getMinPool());
    		dataSource.setMaxPool(connectionPool.getMaxPool());
    		dataSource.setMaxSize(connectionPool.getMaxSize());
    	}

    	// Springframework provided proxies that make working with transactions much easier
        final LazyConnectionDataSourceProxy lazyProxy = new LazyConnectionDataSourceProxy(dataSource);
        
        setInstance(dsName, lazyProxy);
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
    public static DataSource getInstance(final String name) {
    	final DataSource dataSource = getDataSource(name);
        if (dataSource == null) {
            throw new IllegalArgumentException("Unable to locate data source named " + name + ".  Does this need to be init'd?");
        } else {
            return dataSource;
        }
    }

    /**
     * Return a new database connection to the database configured in the
     * <tt>opennms-database.xml</tt>. The database connection is not managed
     * by the factory and must be release by the caller by using the
     * <code>close</code> method.
     *
     * @return a new database connection to the database configured in the
     *         <tt>opennms-database.xml</tt>
     * @throws java.sql.SQLException
     *             Thrown if there is an error opening the connection to the
     *             database.
     */
    @Override
    public Connection getConnection() throws SQLException {
        return getConnection("opennms");
    }

    /**
     * <p>getConnection</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public Connection getConnection(final String dsName) throws SQLException {
        return getDataSource(dsName).getConnection();
    }

    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link javax.sql.DataSource} object.
     */
    public static void setInstance(final DataSource singleton) {
        m_singleton=singleton;
        setInstance("opennms", singleton);
    }

    /**
     * <p>setInstance</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @param singleton a {@link javax.sql.DataSource} object.
     */
    public static synchronized void setInstance(final String dsName, final DataSource singleton) {
        m_dataSources.put(dsName,singleton);
    }

    /**
     * Return the datasource configured for the database
     *
     * @return the datasource configured for the database
     */
    public static DataSource getDataSource() {
        return getDataSource("opennms");
    }

    /**
     * <p>getDataSource</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link javax.sql.DataSource} object.
     */
    public static synchronized DataSource getDataSource(final String dsName) {
        return m_dataSources.get(dsName);
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }

    /**
     * <p>getLogWriter</p>
     *
     * @return a {@link java.io.PrintWriter} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return m_singleton.getLogWriter();
    }

    /**
     * <p>getLogWriter</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link java.io.PrintWriter} object.
     * @throws java.sql.SQLException if any.
     */
    public PrintWriter getLogWriter(final String dsName) throws SQLException {
        return getDataSource(dsName).getLogWriter();
    }

    /** {@inheritDoc} */
    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        setLogWriter("opennms", out);
    }

    /**
     * <p>setLogWriter</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @param out a {@link java.io.PrintWriter} object.
     * @throws java.sql.SQLException if any.
     */
    public void setLogWriter(final String dsName, final PrintWriter out) throws SQLException {
        getDataSource(dsName).setLogWriter(out);
    }

    /** {@inheritDoc} */
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        setLoginTimeout("opennms", seconds);
    }

    /**
     * <p>setLoginTimeout</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @param seconds a int.
     * @throws java.sql.SQLException if any.
     */
    public void setLoginTimeout(final String dsName, final int seconds) throws SQLException {
        getDataSource(dsName).setLoginTimeout(seconds);
    }

    /**
     * <p>getLoginTimeout</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return getLoginTimeout("opennms");
    }

    /**
     * <p>getLoginTimeout</p>
     *
     * @param dsName a {@link java.lang.String} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int getLoginTimeout(final String dsName) throws SQLException {
        return getDataSource(dsName).getLoginTimeout();
    }

    /** {@inheritDoc} */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }

    /**
     * <p>close</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public static synchronized void close() throws SQLException {
        
        for(Runnable closer : m_closers) {
            closer.run();
        }
        
        m_closers.clear();
        m_dataSources.clear();

    }

    /**
     * <p>unwrap</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    /**
     * <p>isWrapperFor</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;  //TODO
    }
}
