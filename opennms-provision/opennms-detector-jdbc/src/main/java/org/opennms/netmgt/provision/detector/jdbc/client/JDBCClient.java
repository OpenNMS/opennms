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
package org.opennms.netmgt.provision.detector.jdbc.client;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.opennms.core.utils.DBTools;
import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>JDBCClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class JDBCClient implements Client<JDBCRequest, JDBCResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(JDBCClient.class);
    private String m_dbDriver;
    private String m_user;
    private String m_password;
    private String m_url;
    
//    private ResultSet m_result;
    private Connection m_connection;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        if(m_connection != null) {
            try {
                m_connection.close();
            } catch (final SQLException e) {
                LOG.debug("unable to close JDBC connection", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        LOG.info("connecting to JDBC on {}", address);
        LOG.debug("Loading JDBC driver: '{}'", getDbDriver());
        Driver driver = (Driver)Class.forName(getDbDriver()).newInstance();
        LOG.debug("JDBC driver loaded: '{}'", getDbDriver());

        String url = DBTools.constructUrl(getUrl(), address.getCanonicalHostName());
        LOG.debug("Constructed JDBC url: '{}'", url);

        Properties props = new Properties();
        props.setProperty("user", getUser());
        props.setProperty("password", getPassword());
        props.setProperty("timeout", String.valueOf(timeout/1000));
        m_connection = driver.connect(url, props);

        LOG.debug("Got database connection: '{}' ({}, {}, {})", m_connection, url, getUser(), getPassword());
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public JDBCResponse receiveBanner() throws IOException, Exception {
        JDBCResponse response = new JDBCResponse();
        response.receive(m_connection);
        return response;
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public JDBCResponse sendRequest(JDBCRequest request) throws IOException, Exception {
        return request.send(m_connection);
    }

    /**
     * <p>setDbDriver</p>
     *
     * @param dbDriver a {@link java.lang.String} object.
     */
    public void setDbDriver(String dbDriver) {
        m_dbDriver = dbDriver;
    }

    /**
     * <p>getDbDriver</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDbDriver() {
        return m_dbDriver;
    }

    /**
     * <p>setUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(String user) {
        m_user = user;
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return m_user;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setUrl(String url) {
        m_url = url;
    }

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrl() {
        return m_url;
    }
}
