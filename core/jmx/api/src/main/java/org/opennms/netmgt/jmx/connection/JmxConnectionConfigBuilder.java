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

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;

public class JmxConnectionConfigBuilder {

    public static JmxConnectionConfigBuilder buildFrom(String ipAddress, Map<String, String> properties) {
        InetAddress inetAddress = InetAddressUtils.getInetAddress(ipAddress);
        return buildFrom(inetAddress, properties);
    }

    public static JmxConnectionConfigBuilder buildFrom(InetAddress ipAddress, Map<String, String> properties) {

        final JmxConnectionConfigBuilder builder = new JmxConnectionConfigBuilder();
        builder.withIpAddress(ipAddress);
        builder.withSunCacao(ParameterMap.getKeyedBoolean(properties, "sunCacao", false));
        builder.withUsername(properties.get("username"));
        builder.withPassword(properties.get("password"));
        builder.withFactory(properties.getOrDefault("factory", "STANDARD"));

        // Legacy
        if (properties.get("url") == null) {
            String url = createLegacyUrl(ipAddress, properties);
            builder.withUrl(url);
            builder.withPort(properties.getOrDefault("port", "1099"));
        } else {
            builder.withUrl(properties.get("url"));
        }
        return builder;
    }

    private static String createLegacyUrl(InetAddress ipAddress, Map<String, String> properties) {
        final String ipAddrString = InetAddressUtils.toUrlIpAddress(ipAddress);
        final String port = properties.getOrDefault("port", "1099");
        final String protocol = properties.getOrDefault("protocol", "rmi");
        final String urlPath = properties.getOrDefault("urlPath",  "/jmxrmi");
        final String rmiServerPort = properties.getOrDefault("rmiServerport",  "45444");
        final boolean remote = ParameterMap.getKeyedBoolean(properties, "remoteJMX",  false);

        if(remote) {
            final String url = String.format("service:jmx:%s:%s:%s://jndi/%s://%s:%s%s",
                    protocol,
                    ipAddrString,
                    rmiServerPort,
                    protocol,
                    ipAddrString,
                    port,
                    urlPath);
            return url;
        } else {
            final String url = String.format("service:jmx:%s:///jndi/%s://%s:%s%s",
                    protocol,
                    protocol,
                    ipAddrString,
                    port,
                    urlPath);
            return url;
        }
    }

    private JmxConnectionConfig config = new JmxConnectionConfig();

    public JmxConnectionConfigBuilder withIpAddress(InetAddress ipAddress) {
        config.setIpAddress(ipAddress);
        return this;
    }

    public JmxConnectionConfigBuilder withPassword(String password) {
        config.setPassword(password);
        return this;
    }

    public JmxConnectionConfigBuilder withUsername(String username) {
        config.setUsername(username);
        return this;
    }

    private JmxConnectionConfigBuilder withSunCacao(boolean sunCacao) {
        config.setSunCacao(sunCacao);
        return this;
    }

    public JmxConnectionConfigBuilder withUrl(String url) {
        config.setUrl(url);
        return this;
    }

    public JmxConnectionConfigBuilder withFactory(String factory) {
        config.setFactory(factory);
        return this;
    }

    private JmxConnectionConfigBuilder withPort(String port) {
        config.setPort(port);
        return this;
    }

    public JmxConnectionConfig build() {
        return config;
    }
}
