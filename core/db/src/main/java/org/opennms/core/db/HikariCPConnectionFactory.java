/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * A factory for creating HikariCPConnection objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HikariCPConnectionFactory extends BaseConnectionFactory {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(HikariCPConnectionFactory.class);

    /** The data source. */
    private HikariDataSource m_pool;

    /**
     * Instantiates a new HikariCP connection factory.
     *
     * @param dataSource the data source
     * @throws MarshalException the marshal exception
     * @throws ValidationException the validation exception
     * @throws PropertyVetoException the property veto exception
     * @throws SQLException the SQL exception
     */
    public HikariCPConnectionFactory(final JdbcDataSource dataSource) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
        super(dataSource);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#initializePool(org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource)
     */
    // TODO Enable and configure MetricRegistry
    @Override
    protected void initializePool(final JdbcDataSource dataSource) throws SQLException {
        final Properties properties = new Properties();
        for (final Param parameter : dataSource.getParamCollection()) {
            properties.setProperty(parameter.getName(), parameter.getValue());
        }
        final HikariConfig config = new HikariConfig(properties);
        config.setPoolName(dataSource.getName());
        config.setJdbcUrl(dataSource.getUrl());
        config.setUsername(dataSource.getUserName());
        config.setPassword(dataSource.getPassword());
        config.setDriverClassName(dataSource.getClassName());
        config.setRegisterMbeans(true); // For JMX Monitoring
        config.validate();
        m_pool = new HikariDataSource(config);
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        return m_pool.getConnection();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#getUrl()
     */
    @Override
    public String getUrl() {
        return m_pool.getJdbcUrl();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#setUrl(java.lang.String)
     */
    @Override
    public void setUrl(final String url) {
        validateJdbcUrl(url);
        m_pool.setJdbcUrl(url);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#getUser()
     */
    @Override
    public String getUser() {
        return m_pool.getUsername();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#setUser(java.lang.String)
     */
    @Override
    public void setUser(final String user) {
        m_pool.setUsername(user);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#getDataSource()
     */
    @Override
    public DataSource getDataSource() {
        return m_pool;
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return m_pool.getConnection(username, password);
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return m_pool.getLogWriter();
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        m_pool.setLogWriter(out);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.ClosableDataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        m_pool.setLoginTimeout(seconds);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return m_pool.getLoginTimeout();
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getParentLogger()
     */
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.BaseConnectionFactory#close()
     */
    @Override
    public void close() {
        super.close();
        LOG.info("Closing HikariCP pool.");
        m_pool.close();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.ClosableDataSource#setIdleTimeout(int)
     */
    @Override
    public void setIdleTimeout(final int idleTimeout) {
        m_pool.setIdleTimeout(idleTimeout * 1000L);
    }

    /**
     * Set the maximum lifetime of the connections in the pool (in milliseconds)
     * which forces occasional connection recycling. This will probably only be 
     * used inside tests although it might be a good idea to do it in production
     * as well to reset server-side query caches and metrics.
     * 
     * @param maxLifetimeMs
     */
    public void setMaxLifetime(final int maxLifetimeMs) {
        m_pool.setMaxLifetime(maxLifetimeMs);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.ClosableDataSource#setMinPool(int)
     */
    @Override
    public void setMinPool(final int minPool) {
        LOG.debug("Hikari has no equivalent to setMinPool(). Ignoring.");
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.ClosableDataSource#setMaxPool(int)
     */
    @Override
    public void setMaxPool(final int maxPool) {
        m_pool.setMaximumPoolSize(maxPool);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.db.ClosableDataSource#setMaxSize(int)
     */
    @Override
    public void setMaxSize(final int maxSize) {
        LOG.debug("Hikari has no equivalent to setMaxSize(). Ignoring.");
    }
}
