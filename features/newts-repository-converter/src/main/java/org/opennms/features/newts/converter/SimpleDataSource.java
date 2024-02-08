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
package org.opennms.features.newts.converter;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

/**
 * The Class SimpleDataSource.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class SimpleDataSource implements DataSource {
    
    /** The m_driver. */
    private String m_driver;
    
    /** The m_url. */
    private String m_url;
    
    /** The m_properties. */
    private Properties m_properties = new Properties();
    
    /** The m_timeout. */
    private Integer m_timeout = null;

    /**
     * Instantiates a new simple data source.
     *
     * @param ds the ds
     * @throws ClassNotFoundException the class not found exception
     */
    public SimpleDataSource(JdbcDataSource ds) throws ClassNotFoundException {
        m_driver = ds.getClassName();
        m_url = ds.getUrl();
        m_properties.put("user", ds.getUserName());
        m_properties.put("password", ds.getPassword());
        Class.forName(m_driver);
        for (Param param : ds.getParamCollection()) {
            m_properties.put(param.getName(), param.getValue());
        }
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection()
     */
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

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return m_url;
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("getConnection(String, String) not implemented");
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter() not implemented");
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {
        return m_timeout == null ? -1 : m_timeout;
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter(PrintWriter) not implemented");
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        m_timeout = seconds;
    }

    /* (non-Javadoc)
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    /* (non-Javadoc)
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getParentLogger()
     */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }
}

