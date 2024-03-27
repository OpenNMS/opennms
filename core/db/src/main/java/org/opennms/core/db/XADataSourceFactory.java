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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.XADataSource;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
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
public abstract class XADataSourceFactory {

	private static final Logger LOG = LoggerFactory.getLogger(DataSourceFactory.class);

	private static DataSourceConfigurationFactory m_dataSourceConfigFactory;
	
	static {
		// Try to create a DataSourceConfigurationFactory from the default opennms-datasources.xml
		try {
			m_dataSourceConfigFactory = new DataSourceConfigurationFactory(ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME));
		} catch (IOException e) {
			LOG.warn("Could not parse default data source configuration", e);
			m_dataSourceConfigFactory = null;
		}
	}

	private static final Map<String, XADataSource> m_dataSources = new ConcurrentHashMap<String, XADataSource>();

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

		final JdbcDataSource ds = m_dataSourceConfigFactory.getJdbcDataSource(dsName);
		final ConnectionPool pool = ConnectionPool.merge(ds.getConnectionPool(), m_dataSourceConfigFactory.getConnectionPool());
		String urlString = ds.getUrl();
		if (urlString.startsWith("jdbc:")) {
			urlString = urlString.substring("jdbc:".length());
		}
		URI url = URI.create(urlString);
		// TODO: Add support for more XADataSources (hsqldb, derby)
		if ("postgresql".equalsIgnoreCase(url.getScheme())) {
			PGXADataSource xaDataSource = new PGXADataSource();
			xaDataSource.setServerName(url.getHost());
			xaDataSource.setPortNumber(url.getPort());
			xaDataSource.setDatabaseName(ds.getDatabaseName());
			xaDataSource.setUser(ds.getUserName());
			xaDataSource.setPassword(ds.getPassword());

			if (pool != null) {
				if (pool.getLoginTimeout() > 0) {
					xaDataSource.setLoginTimeout(pool.getLoginTimeout());
				}

				if (pool.getIdleTimeout() > 0) {
					// Set the socket timeout so that connections that are stuck reading from
					// the database will be closed after the timeout
					xaDataSource.setSocketTimeout(pool.getIdleTimeout());
				}
			}

			setInstance(dsName, xaDataSource);
		} else {
			throw new UnsupportedOperationException("Data source scheme not supported: " + url.getScheme());
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
	 * <p>setInstance</p>
	 *
	 * @param ds a {@link javax.sql.DataSource} object.
	 */
	public static void setInstance(final XADataSource ds) {
		setInstance("opennms", ds);
	}

	/**
	 * <p>setInstance</p>
	 *
	 * @param dsName a {@link java.lang.String} object.
	 * @param singleton a {@link javax.sql.DataSource} object.
	 */
	public static synchronized void setInstance(final String dsName, final XADataSource singleton) {
		m_dataSources.put(dsName, singleton);
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
		init(dsName);
		return m_dataSources.get(dsName);
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
	}
}
