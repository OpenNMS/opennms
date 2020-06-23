/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * <p>C3P0ConnectionFactory class.</p>
 */
public class C3P0ConnectionFactory extends BaseConnectionFactory {

	public static final Logger LOG = LoggerFactory.getLogger(C3P0ConnectionFactory.class);

	private ComboPooledDataSource m_pool;

	public C3P0ConnectionFactory(final JdbcDataSource dataSource) throws PropertyVetoException, SQLException {
		super(dataSource);
	}

	@Override
	protected void initializePool(final JdbcDataSource dataSource) throws SQLException {
		m_pool = new ComboPooledDataSource();
		m_pool.setPassword(dataSource.getPassword());
		m_pool.setUser(dataSource.getUserName());
		m_pool.setJdbcUrl(dataSource.getUrl());
		try {
			m_pool.setDriverClass(dataSource.getClassName());
		} catch (final PropertyVetoException e) {
			throw new SQLException("Unable to set driver class.", e);
		}

		final Properties properties = new Properties();
		for (final Param parameter : dataSource.getParamCollection()) {
			properties.put(parameter.getName(), parameter.getValue());
		}
		if (!properties.isEmpty()) {
			m_pool.setProperties(properties);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return m_pool.getConnection();
	}

	@Override
	public String getUrl() {
		return m_pool.getJdbcUrl();
	}

	@Override
	public void setUrl(final String url) {
		validateJdbcUrl(url);
		m_pool.setJdbcUrl(url);
	}

	@Override
	public String getUser() {
		return m_pool.getUser();
	}

	@Override
	public void setUser(final String user) {
		m_pool.setUser(user);
	}

	@Override
	public DataSource getDataSource() {
		return m_pool;
	}

	@Override
	public Connection getConnection(final String username, final String password) throws SQLException {
		return m_pool.getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return m_pool.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		m_pool.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		m_pool.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return m_pool.getLoginTimeout();
	}

	/** {@inheritDoc} */
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("getParentLogger not supported");
	}

	@Override
	public void close() {
		super.close();
		LOG.info("Closing C3P0 pool.");
		m_pool.close();
	}

	@Override
	public void setIdleTimeout(final int idleTimeout) {
		m_pool.setMaxIdleTime(idleTimeout);
	}

	@Override
	public void setMinPool(final int minPool) {
		m_pool.setMinPoolSize(minPool);
	}

	@Override
	public void setMaxPool(final int maxPool) {
		m_pool.setMaxPoolSize(maxPool);
	}

	@Override
	public void setMaxSize(final int maxSize) {
		LOG.debug("C3P0 has no equivalent to setMaxSize(). Ignoring.");
	}
}
