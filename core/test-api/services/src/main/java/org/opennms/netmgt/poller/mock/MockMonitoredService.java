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
package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;

public class MockMonitoredService implements MonitoredService {
    private final int m_nodeId;
    private String m_nodeLabel;
    private final String m_nodeLocation;
    private final String m_ipAddr;
    private final String m_svcName;
    private InetAddress m_inetAddr;

    public MockMonitoredService(int nodeId, String nodeLabel, InetAddress inetAddress, String svcName) {
        this(nodeId, nodeLabel, null, inetAddress, svcName);
    }

    public MockMonitoredService(int nodeId, String nodeLabel, String nodeLocation, InetAddress inetAddress, String svcName) {
        m_nodeId = nodeId;
        m_nodeLabel = nodeLabel;
        m_nodeLocation = nodeLocation;
        m_inetAddr = inetAddress;
        m_svcName = svcName;
        m_ipAddr = InetAddressUtils.str(m_inetAddr);
    }

    @Override
    public String getSvcName() {
        return m_svcName;
    }

    @Override
    public String getIpAddr() {
        return m_ipAddr;
    }

    @Override
    public int getNodeId() {
        return m_nodeId;
    }

    @Override
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    @Override
    public String getNodeLocation() {
        return m_nodeLocation;
    }

    @Override
    public InetAddress getAddress() {
        return m_inetAddr;
    }
}
