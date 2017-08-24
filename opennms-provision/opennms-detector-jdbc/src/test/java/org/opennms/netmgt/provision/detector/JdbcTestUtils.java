/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.jdbc.AbstractJdbcDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcTestUtils.class);
    private static final Pattern HOST_AND_PORT = Pattern.compile(".*://([^:]+):(\\d+)/.*");

    public static DSInfo getDataSourceInfo(final DataSource ds) throws URISyntaxException, UnknownHostException {
        String url = null;
        String username = null;
        String password = "";
        InetAddress host = InetAddressUtils.getLocalHostAddress();
        Integer port = 5432;

        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            url = metaData.getURL();
            username = metaData.getUserName();
            conn.close();
        } catch (final SQLException e) {
            LOG.warn("Failed to get metadata from connection", e);
            if (conn != null) {
                try {
                    conn.close();
                } catch (final SQLException sqe) {
                    LOG.warn("Failed to close connection", sqe);
                }
            }
        }

        final String mockDbUrl = System.getProperty("mock.db.url");
        if (mockDbUrl != null && mockDbUrl.contains("jdbc:postgresql")) {
            //url = mockDbUrl;
            final Matcher m = HOST_AND_PORT.matcher(mockDbUrl);
            if (m.matches()) {
                final String hostName = m.group(1);
                final String portString = m.group(2);

                if (hostName != null && !"".equals(hostName.trim())) {
                    final InetAddress[] addrs = InetAddress.getAllByName(hostName);
                    if (addrs != null && addrs.length > 0) {
                        host = addrs[0];
                    }
                }
                if (portString != null && !"".equals(portString.trim())) {
                    port = Integer.valueOf(portString, 10);
                }
            } else {
                LOG.debug("-Dmock.db.url is set, but hostname and port could not be extracted from '{}'", mockDbUrl);
            }
        }

        final String mockDbAdminUser = System.getProperty("mock.db.adminUser");
        if (mockDbAdminUser != null && !"".equals(mockDbAdminUser.trim())) {
            username = mockDbAdminUser;
        }

        final String mockDbAdminPassword = System.getProperty("mock.db.adminPassword");
        if (mockDbAdminPassword != null) {
            password = mockDbAdminPassword;
        }

        final DSInfo info  = new DSInfo("org.postgresql.Driver", url, host, port, username, password);

        LOG.info("Test DataSource should be: {}", info);
        return info;
    }

    public static void setInfo(final AbstractJdbcDetector detector, final DSInfo info) {
        detector.setDbDriver(info.getDriver());
        detector.setUrl(info.getUrl());
        detector.setPort(info.getPort());
        detector.setUser(info.getUser());
        detector.setPassword(info.getPassword());
    }

    public static class DSInfo {
        private String m_driver;
        private String m_url;
        private InetAddress m_host;
        private Integer m_port;
        private String m_user;
        private String m_password;

        public DSInfo(final String driver, final String url, final InetAddress host, final Integer port, final String user, final String password) {
            m_driver = driver;
            m_url = url;
            m_host = host;
            m_port = port;
            m_user = user;
            m_password = password;
        }

        public String getDriver() {
            return m_driver;
        }

        public String getUrl() {
            return m_url;
        }

        public InetAddress getHost() {
            return m_host;
        }

        public Integer getPort() {
            return m_port;
        }

        public String getUser() {
            return m_user;
        }

        public String getPassword() {
            return m_password;
        }

        @Override
        public String toString() {
            return "DSInfo [driver=" + m_driver + ", url=" + m_url
                    + ", host=" + InetAddressUtils.str(m_host) + ", port=" + m_port
                    + ", user=" + m_user + ", password=" + m_password
                    + "]";
        }
    }
}
