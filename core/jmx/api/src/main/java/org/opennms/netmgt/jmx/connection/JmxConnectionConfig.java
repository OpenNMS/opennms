/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx.connection;

import static org.opennms.netmgt.jmx.connection.JmxServerConnector.DEFAULT_OPENNMS_JMX_PORT;
import static org.opennms.netmgt.jmx.connection.JmxServerConnector.JMX_PORT_SYSTEM_PROPERTY;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.management.remote.JMXServiceURL;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.PropertiesUtils;

/**
 * Helper object to safety access the connection properties.
 *
 * @author mvrueden
 */
public class JmxConnectionConfig {

    private String factory;
    private InetAddress ipAddress;
    private String url;
    private String password;
    private String username;
    private boolean sunCacao;
    private String port;

    public String getFactory() {
        return factory;
    }

    public boolean isLocalConnection() throws MalformedURLException {
        Objects.requireNonNull(getIpAddress());
        Objects.requireNonNull(getPort());

        // If we're trying to create a connection to a localhost address...
        if (getIpAddress().isLoopbackAddress()) {
            final String jmxPort = System.getProperty(JMX_PORT_SYSTEM_PROPERTY); // returns null if REMOTE JMX is enabled

            // ... and if the port matches the port of the current JVM...
            if (getPort().equals(jmxPort) ||
                    // ... or if remote JMX RMI is disabled and we're attempting to connect
                    // to the default OpenNMS JMX port...
                    (jmxPort == null && DEFAULT_OPENNMS_JMX_PORT.equals(getPort()))) {
                return true;
            }
        }
        return false;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private String getPort() throws MalformedURLException {
        if (port == null && url != null) {
            port = String.valueOf(new JMXServiceURL(getUrl()).getPort());
        }
        return port;
    }

    public boolean hasCredentials() {
        return getUsername() != null && getPassword() != null;
    }

    public String[] getCredentials() {
        if (isSunCacao()) {
            return new String[]{"com.sun.cacao.user\001" + getUsername(), getPassword()};
        }
        return new String[]{getUsername(), getPassword()};
    }

    public PasswordStrategy getPasswordStategy() {
        if ("PASSWORD_CLEAR".equals(getFactory())) {
            return PasswordStrategy.PASSWORD_CLEAR;
        }
        if ("SASL".equals(getFactory())) {
            return PasswordStrategy.SASL;
        }
        return PasswordStrategy.STANDARD;
    }

    private boolean isSunCacao() {
        return sunCacao;
    }

    private String getPassword() {
        return password;
    }

    private String getUsername() {
        return username;
    }

    public String getUrl() throws MalformedURLException {
        final Map<String, Object> propertiesMap = new HashMap<>();
        if (ipAddress != null) {
            // Create map to substitute url
            propertiesMap.put("ipaddr", InetAddressUtils.toUrlIpAddress(getIpAddress()));

            final String theUrl = PropertiesUtils.substitute(url, propertiesMap);
            return theUrl;
        }
        return url;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public InetAddress getIpAddress() throws MalformedURLException {
        if (ipAddress == null && url != null) {
            ipAddress = InetAddressUtils.getInetAddress(new JMXServiceURL(getUrl()).getHost());
        }
        return ipAddress;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSunCacao(boolean sunCacao) {
        this.sunCacao = sunCacao;
    }
}
