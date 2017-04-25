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
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;

/**
 * Helper object to safety access the connection properties.
 *
 * @author mvrueden
 */
public class JmxConnectionConfig {

    private final Map<String, String> properties;
    private final InetAddress ipAddress;

    public JmxConnectionConfig(final InetAddress ipAddress, final Map<String, String> properties) {
        this.properties = Objects.requireNonNull(properties);
        this.ipAddress = Objects.requireNonNull(ipAddress);
    }

    public String getFactory() {
        return ParameterMap.getKeyedString(properties, "factory", "STANDARD");
    }

    public JMXServiceURL createJmxServiceURL() throws MalformedURLException {
        // For backwards compatibility keep old behaviour for now
        if (isLegacyConnection()) {
            if(isRemote()) {
                final String url = String.format("service:jmx:%s:%s:%s://jndi/%s://%s:%s%s",
                        getProtocol(),
                        InetAddressUtils.toUrlIpAddress(ipAddress),
                        getRmiServerPort(),
                        getProtocol(),
                        InetAddressUtils.toUrlIpAddress(ipAddress),
                        getPort(),
                        getUrlPath());
                return new JMXServiceURL(url);
            } else {
                final String url = String.format("service:jmx:%s:///jndi/%s://%s:%s%s",
                        getProtocol(),
                        getProtocol(),
                        InetAddressUtils.toUrlIpAddress(ipAddress),
                        getPort(),
                        getUrlPath());
                return new JMXServiceURL(url);
            }
        } else {
            // Create map to substitute url
            final Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put("ipaddr", InetAddressUtils.str(ipAddress));

            final String url = PropertiesUtils.substitute(getUrl(), propertiesMap);
            return new JMXServiceURL(url);
        }
    }

    public boolean isLocalConnection() {
        Objects.requireNonNull(ipAddress);
        Objects.requireNonNull(getPort());

        // If we're trying to create a connection to a localhost address...
        if (ipAddress.isLoopbackAddress()) {
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
        return ParameterMap.getKeyedBoolean(properties, "sunCacao", false);
    }

    private String getPassword() {
        return properties.get("password");
    }

    private String getUsername() {
        return properties.get("username");
    }

    private String getUrl() {
        return properties.get("url");
    }

    private String getPort() {
        return ParameterMap.getKeyedString(properties, "port", "1099");
    }

    private String getProtocol() {
        return ParameterMap.getKeyedString(properties, "protocol", "rmi");
    }

    private String getUrlPath() {
        return ParameterMap.getKeyedString(properties, "urlPath",  "/jmxrmi");
    }

    private String getRmiServerPort() {
        return  ParameterMap.getKeyedString(properties, "rmiServerport",  "45444");
    }

    private boolean isRemote() {
        return ParameterMap.getKeyedBoolean(properties, "remoteJMX",  false);
    }

    private boolean isLegacyConnection() {
        return getUrl() == null;
    }


}
