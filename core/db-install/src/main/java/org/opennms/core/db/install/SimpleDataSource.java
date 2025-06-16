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
package org.opennms.core.db.install;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDataSource implements DataSource {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleDataSource.class);

    private String m_driver;
    private String m_url;
    private Properties m_properties = new Properties();
    private Integer m_timeout = null;

    /**
     * <p>Constructor for SimpleDataSource.</p>
     *
     * @param driver a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param password a {@link java.lang.String} object.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public SimpleDataSource(String driver, String url, String user, String password) throws ClassNotFoundException {
        m_driver = driver;
        m_url = url;
        
        m_properties.put("user", user);
        m_properties.put("password", password);
        
        Class<?> driverClass = Class.forName(m_driver);
        
        // If the PostgreSQL driver is in use and has deregistered itself
        // (due to tests using the OSGi lifecycle) then reregister it
        if (org.postgresql.Driver.class.getName().equals(m_driver)) {
            try {
                boolean isRegistered = (boolean)driverClass.getMethod("isRegistered").invoke(null, (Object[])null);
                if (!isRegistered) {
                    LOG.info(org.postgresql.Driver.class.getName() + " is not registered, reregistering...");
                    driverClass.getMethod("register").invoke(null, (Object[])null);
                    LOG.info(org.postgresql.Driver.class.getName() + " is registered");
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOG.warn("Exception while trying to check the registration on the " + org.postgresql.Driver.class.getName() + " driver", e);
            }
        }
    }
    
    /**
     * <p>Constructor for SimpleDataSource.</p>
     *
     * @param ds a {@link org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource} object.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public SimpleDataSource(JdbcDataSource ds) throws ClassNotFoundException {
        this(ds.getClassName(), ds.getUrl(), ds.getUserName(), ds.getPassword());
        
        for (Param param : ds.getParamCollection()) {
            m_properties.put(param.getName(), param.getValue());
        }
    }

    /**
     * <p>getConnection</p>
     *
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (m_timeout == null) {
            return DriverManager.getConnection(m_url, m_properties);
        } else {
            int oldTimeout = DriverManager.getLoginTimeout();
            DriverManager.setLoginTimeout(m_timeout);
            Connection conn = DriverManager.getConnection(m_url, m_properties);
            DriverManager.setLoginTimeout(oldTimeout);
            return conn;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("getConnection(String, String) not implemented");
    }

    /**
     * <p>getLogWriter</p>
     *
     * @return a {@link java.io.PrintWriter} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter() not implemented");
    }

    /**
     * <p>getLoginTimeout</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return m_timeout == null ? -1 : m_timeout;
    }

    /** {@inheritDoc} */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter(PrintWriter) not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        m_timeout = seconds;
    }

    /** {@inheritDoc} */
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
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
    public <T> T unwrap(Class<T> iface) throws SQLException {
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
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //TODO
    }

    /**
     * <p>getDriver</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDriver() {
        return m_driver;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_properties.getProperty("password");
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getTimeout() {
        return m_timeout;
    }

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrl() {
        return m_url;
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return m_properties.getProperty("user");
    }
    
    /**
     * <p>getProperties</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getProperties() {
        return m_properties;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder props = new StringBuilder();
        if (m_properties.isEmpty()) {
            props.append(" none");
        } else {
            boolean first = true;
            for (Entry<Object, Object> entry : m_properties.entrySet()) {
                if (!first) {
                    props.append(",");
                }
                props.append(" ");
                props.append(entry.getKey());
                props.append("='");
                props.append(entry.getValue());
                props.append("'");
                
                first = false;
            }
        }
        return "SimpleDataSource[URL='" + getUrl() + "', driver class='" + getDriver() + "', properties:" + props + "]";
    }
}
