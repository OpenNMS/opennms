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
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.postgresql.xa.PGXADataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class XADataSourceFactory implements XADataSource {

	private static final Logger LOG = LoggerFactory.getLogger(DataSourceFactory.class);

	/**
	 * The singleton instance of this factory
	 */
	private static XADataSource m_singleton = null;

	private static final Map<String, XADataSource> m_dataSources = new ConcurrentHashMap<String, XADataSource>();

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

		final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(cfgFile);
			final JdbcDataSource ds = ConnectionFactoryUtil.marshalDataSourceFromConfig(fileInputStream, dsName);
			PGXADataSource xaDataSource = new PGXADataSource();
			URL url = new URL(ds.getUrl());
			xaDataSource.setServerName(url.getHost());
			xaDataSource.setPortNumber(url.getPort());
			xaDataSource.setDatabaseName(ds.getDatabaseName());
			xaDataSource.setUser(ds.getUserName());
			xaDataSource.setPassword(ds.getPassword());
			setInstance(xaDataSource);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
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
	public static XADataSource getInstance() {
		return getInstance("opennms");
	}

	/**
	 * <p>getInstance</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link javax.sql.DataSource} object.
	 */
	public static XADataSource getInstance(final String name) {
		final XADataSource dataSource = getXADataSource(name);
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
	public XAConnection getXAConnection() throws SQLException {
		return getXAConnection("opennms");
	}

	/**
	 * <p>getConnection</p>
	 *
	 * @param dsName a {@link java.lang.String} object.
	 * @return a {@link java.sql.Connection} object.
	 * @throws java.sql.SQLException if any.
	 */
	public XAConnection getXAConnection(final String dsName) throws SQLException {
		return getXADataSource(dsName).getXAConnection();
	}

	/**
	 * <p>setInstance</p>
	 *
	 * @param singleton a {@link javax.sql.DataSource} object.
	 */
	public static void setInstance(final XADataSource singleton) {
		m_singleton=singleton;
		setInstance("opennms", singleton);
	}

	/**
	 * <p>setInstance</p>
	 *
	 * @param dsName a {@link java.lang.String} object.
	 * @param singleton a {@link javax.sql.DataSource} object.
	 */
	public static synchronized void setInstance(final String dsName, final XADataSource singleton) {
		m_dataSources.put(dsName,singleton);
	}

	/**
	 * Return the datasource configured for the database
	 *
	 * @return the datasource configured for the database
	 */
	public static XADataSource getXADataSource() {
		return getXADataSource("opennms");
	}

	/**
	 * <p>getDataSource</p>
	 *
	 * @param dsName a {@link java.lang.String} object.
	 * @return a {@link javax.sql.DataSource} object.
	 */
	public static synchronized XADataSource getXADataSource(final String dsName) {
		return m_dataSources.get(dsName);
	}

	/** {@inheritDoc} */
	@Override
	public XAConnection getXAConnection(final String username, final String password) throws SQLException {
		return getXAConnection();
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
		return getXADataSource(dsName).getLogWriter();
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
		getXADataSource(dsName).setLogWriter(out);
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
		getXADataSource(dsName).setLoginTimeout(seconds);
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
		return getXADataSource(dsName).getLoginTimeout();
	}

	/** {@inheritDoc} */
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("getParentLogger not supported");
	}

	/**
	 * <p>close</p>
	 *
	 * @throws java.sql.SQLException if any.
	 */
	public static synchronized void close() throws SQLException {
	}
}
