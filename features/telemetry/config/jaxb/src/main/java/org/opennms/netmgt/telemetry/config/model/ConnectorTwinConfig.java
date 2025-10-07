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
package org.opennms.netmgt.telemetry.config.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConnectorTwinConfig {
    public static String CONNECTOR_KEY = "telemetry.connector.config";

    private List<ConnectorConfig> configurations = new ArrayList<>();

    public ConnectorTwinConfig() {
    }

    @JsonCreator
    public ConnectorTwinConfig(
            @JsonProperty("configurations") List<ConnectorConfig> configurations) {
        this.configurations = configurations != null
                ? Collections.unmodifiableList(new ArrayList<>(configurations))
                : Collections.emptyList();
    }

    @JsonProperty("configurations")
    public List<ConnectorConfig> getConfigurations() {
        return configurations;
    }

    public static class ConnectorConfig {
        private int nodeId;
        private String ipAddress;
        private List<Map<String, String>> parameters;
        private String connectionKey;

        public ConnectorConfig() {
        }

        @JsonCreator
        public ConnectorConfig(
                @JsonProperty("nodeId") int nodeId,
                @JsonProperty("ipAddress") String ipAddress,
                @JsonProperty("connectionKey") String connectionKey,
                @JsonProperty("parameters") List<Map<String, String>> parameters) {

            this.nodeId = nodeId;
            this.ipAddress = ipAddress;
            this.connectionKey = connectionKey;
            this.parameters = parameters != null
                    ? Collections.unmodifiableList(new ArrayList<>(parameters))
                    : Collections.emptyList();
        }

        @JsonProperty("nodeId")
        public int getNodeId() { return nodeId; }

        @JsonProperty("connectionKey")
        public String getConnectionKey() { return connectionKey; }

        @JsonProperty("ipAddress")
        public String getIpAddress() { return ipAddress; }

        @JsonProperty("parameters")
        public List<Map<String, String>> getParameters() { return parameters; }
    }
}