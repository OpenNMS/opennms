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
        final var addr = Objects.requireNonNull(getIpAddress());
        final var jvmPort = Objects.requireNonNull(getPort());

        // If we're trying to create a connection to a localhost address...
        if (addr.isLoopbackAddress()) {
            final String jmxPort = System.getProperty(JMX_PORT_SYSTEM_PROPERTY); // returns null if REMOTE JMX is enabled

            // ... and if the port matches the port of the current JVM...
            if (jvmPort.equals(jmxPort)) {
                return true;
            }
            if (jmxPort == null && DEFAULT_OPENNMS_JMX_PORT.equals(jvmPort)) {
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
        if ("PASSWORD-CLEAR".equals(getFactory())) {
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
