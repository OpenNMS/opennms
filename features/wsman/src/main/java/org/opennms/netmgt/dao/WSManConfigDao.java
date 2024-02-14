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
package org.opennms.netmgt.dao;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.opennms.netmgt.config.wsman.credentials.WsmanConfig;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.credentials.WsmanAgentConfig;

public interface WSManConfigDao {
    static final String DEFAULT_PROTOCOL = "http";
    static final String DEFAULT_PATH = "/wsman";

    WsmanConfig getConfig();
    Definition getAgentConfig(InetAddress agentInetAddress);
    WSManEndpoint getEndpoint(InetAddress agentInetAddress);

    static WSManEndpoint getEndpoint(WsmanAgentConfig agentConfig, InetAddress agentInetAddress) {
        Objects.requireNonNull(agentConfig, "agentConfig argument");
        Objects.requireNonNull(agentInetAddress, "agentInetAddress argument");
        URL url;
        try {
            String protocol = DEFAULT_PROTOCOL;
            if (agentConfig.isSsl()!= null) {
                protocol = agentConfig.isSsl() ? "https" : "http";
            }

            String port = "";
            if (agentConfig.getPort() != null) {
                port = String.format(":%d", agentConfig.getPort());
            }

            String path = DEFAULT_PATH;
            if (agentConfig.getPath() != null) {
                path = agentConfig.getPath();
            }
            // Prepend a forward slash if missing
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            String host = agentInetAddress.getHostAddress();
            if (agentConfig.isGssAuth()!=null && agentConfig.isGssAuth()) {
                // Always use the canonical host name when using GSS authentication
                host = agentInetAddress.getCanonicalHostName();
            }

            url = new URL(String.format("%s://%s%s%s", protocol, host, port, path));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid endpoint URL: " + e.getMessage());
        }

        final WSManEndpoint.Builder builder = new WSManEndpoint.Builder(url)
                .withServerVersion(WSManVersion.WSMAN_1_0);
        if (agentConfig.getUsername() != null && agentConfig.getPassword() != null) {
            builder.withBasicAuth(agentConfig.getUsername(), agentConfig.getPassword());
        }
        if (agentConfig.isGssAuth() != null && agentConfig.isGssAuth()) {
            builder.withGSSAuth();
        }
        if (agentConfig.getMaxElements() != null) {
            builder.withMaxElements(agentConfig.getMaxElements());
        }
        if (agentConfig.isStrictSsl() != null) {
            builder.withStrictSSL(false);
        }
        if (agentConfig.getTimeout() != null) {
            builder.withConnectionTimeout(agentConfig.getTimeout())
                   .withReceiveTimeout(agentConfig.getTimeout());
        }
        return builder.build();
    }
}
