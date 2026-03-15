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
package org.opennms.netmgt.poller.passive;

/**
 * Immutable key for passive service status lookup: (nodeLabel, ipAddr, serviceName).
 */
public class PassiveStatusKey {

    private final String m_nodeLabel;
    private final String m_ipAddr;
    private final String m_serviceName;

    public PassiveStatusKey(String nodeLabel, String ipAddr, String serviceName) {
        m_nodeLabel = nodeLabel;
        m_ipAddr = ipAddr;
        m_serviceName = serviceName;
    }

    public String getNodeLabel() { return m_nodeLabel; }
    public String getIpAddr() { return m_ipAddr; }
    public String getServiceName() { return m_serviceName; }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PassiveStatusKey) {
            PassiveStatusKey key = (PassiveStatusKey) o;
            return getNodeLabel().equals(key.getNodeLabel()) &&
                    getIpAddr().equals(key.getIpAddr()) &&
                    getServiceName().equals(key.getServiceName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getNodeLabel().hashCode() ^ getIpAddr().hashCode() ^ getServiceName().hashCode();
    }

    @Override
    public String toString() {
        return getNodeLabel() + ':' + getIpAddr() + ':' + getServiceName();
    }
}
