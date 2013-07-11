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

package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.BaseConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

public class TomcatJdbcPoolConnectionFactory extends BaseConnectionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TomcatJdbcPoolConnectionFactory.class);

	private org.apache.tomcat.jdbc.pool.DataSource m_dataSource;

    public TomcatJdbcPoolConnectionFactory(final InputStream stream, final String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
    	super(stream, dsName);
    }

    public TomcatJdbcPoolConnectionFactory(final String configFile, final String dsName) throws IOException, MarshalException, ValidationException, PropertyVetoException, SQLException {
    	super(configFile, dsName);
    }

    @Override
    protected void initializePool(final JdbcDataSource dataSource) throws SQLException {
    	m_dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
    	m_dataSource.setName(dataSource.getName());
    	m_dataSource.setDriverClassName(dataSource.getClassName());
    	m_dataSource.setUrl(dataSource.getUrl());
    	m_dataSource.setUsername(dataSource.getUserName());
    	m_dataSource.setPassword(dataSource.getPassword());

		final Properties properties = new Properties();
        for (final Param parameter : dataSource.getParamCollection()) {
            properties.put(parameter.getName(), parameter.getValue());
        }
        if (!properties.isEmpty()) {
        	m_dataSource.setDbProperties(properties);
        }
        
    	m_dataSource.setAccessToUnderlyingConnectionAllowed(true);
    	m_dataSource.setFairQueue(true);
    }

    @Override
    public Connection getConnection() throws SQLException {
    	return m_dataSource.getConnection();
    }

    @Override
    public String getUrl() {
    	return m_dataSource.getUrl();
    }

    @Override
    public void setUrl(final String url) {
    	validateJdbcUrl(url);
    	m_dataSource.setUrl(url);
    }

    @Override
    public String getUser() {
    	return m_dataSource.getUsername();
    }

    @Override
    public void setUser(final String user) {
    	m_dataSource.setUsername(user);
    }

    @Override
    public DataSource getDataSource() {
    	return m_dataSource;
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
    	return m_dataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
    	return m_dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        m_dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        m_dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return m_dataSource.getLoginTimeout();
    }

    /** {@inheritDoc} */
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }

    @Override
    public void close() throws SQLException {
    	super.close();
    	LOG.info("Closing Tomcat DBCP pool.");
    	m_dataSource.close();
    }

    @Override
	public void setIdleTimeout(final int idleTimeout) {
		LOG.warn("Tomcat DBCP doesn't have the concept of a generic idle timeout.  Ignoring.");
	}

    @Override
	public void setMinPool(final int minPool) {
		m_dataSource.setInitialSize(minPool);
	}

    @Override
	public void setMaxPool(final int maxPool) {
		LOG.warn("Tomcat DBCP doesn't have the concept of a maximum pool.  Ignoring.");
	}

    @Override
	public void setMaxSize(final int maxSize) {
		m_dataSource.setMaxActive(maxSize);
	}
}
