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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.sql.DataSource;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A factory for creating HikariCPConnection objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HikariCPConnectionFactory extends BaseConnectionFactory {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(HikariCPConnectionFactory.class);

    private static final boolean DEADLOCK_DETECTION_ENABLED = Boolean.getBoolean("org.opennms.core.db.deadlock.detection");

    /** The data source. */
    private HikariDataSource m_pool;

    /**
     * Instantiates a new HikariCP connection factory.
     *
     * @param dataSource the data source
     * @throws PropertyVetoException the property veto exception
     * @throws SQLException the SQL exception
     */
    public HikariCPConnectionFactory(final JdbcDataSource dataSource) throws PropertyVetoException, SQLException {
        super(dataSource);
        if (DEADLOCK_DETECTION_ENABLED) {
            LOG.error("Deadlock detection is enabled.");
        }
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
        // NMS-9387: Block indefinitely when waiting for a connection
        config.setConnectionTimeout(0);
        config.setRegisterMbeans(true); // For JMX Monitoring
        config.validate();
        m_pool = new HikariDataSource(config);
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (DEADLOCK_DETECTION_ENABLED && TransactionSynchronizationManager.isSynchronizationActive()) {
            LOG.error("Possible database deadlock detected: " +
                    "Attempting to acquire connection in thread while existing transaction active.",
                    new Exception("Possible deadlock"));
        }
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
        m_pool.setMinimumIdle(minPool);
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
