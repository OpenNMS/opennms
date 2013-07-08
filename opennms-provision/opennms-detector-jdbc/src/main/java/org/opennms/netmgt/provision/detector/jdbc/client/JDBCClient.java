/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
