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

package org.opennms.core.db.install;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

public class SimpleDataSource implements DataSource {
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
        
        Class.forName(m_driver);
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
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
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
        StringBuffer props = new StringBuffer();
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
