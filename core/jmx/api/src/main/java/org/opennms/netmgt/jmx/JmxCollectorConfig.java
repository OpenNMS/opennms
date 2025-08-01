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
package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.jmx.connection.JmxConnectors;

import java.util.Map;

public class JmxCollectorConfig {

    private JmxConnectors connectionName;

    private String agentAddress;

    private int retries;

    private Map<String, String> serviceProperties;

    private JmxCollection jmxCollection;

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public Map<String, String> getServiceProperties() {
        return serviceProperties;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public JmxConnectors getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(JmxConnectors connectionName) {
        this.connectionName = connectionName;
    }

    public void setServiceProperties(Map<String, String> serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    public void setJmxCollection(JmxCollection jmxCollection) {
        this.jmxCollection = jmxCollection;
    }

    public JmxCollection getJmxCollection() {
        return jmxCollection;
    }
}
