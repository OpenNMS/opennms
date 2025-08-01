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
package org.opennms.netmgt.enlinkd.service.api;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;

public class MacPort implements Topology {

    private Integer m_nodeId;
    private Integer m_macPortIfIndex;
    private String  m_macPortName;
    private final Map<String, Set<InetAddress>> m_macPortMap = new HashMap<>();
    
    public MacPort() {
    }
        
    public String getPortMacInfo() {
        
        final StringBuffer strbfr = new StringBuffer();
        m_macPortMap.keySet().forEach(mac -> {
            strbfr.append("ip:["); 
            m_macPortMap.get(mac).forEach(ip -> {
                strbfr.append(InetAddressUtils.str(ip)); 
                strbfr.append(" ");
            });
            strbfr.append("], mac:[");
            strbfr.append(mac);
            strbfr.append("]");
            
        });

        return strbfr.toString();
    }
    
    public String printTopology() {

        return "nodeid:[" +
                m_nodeId +
                "], port name:[" +
                m_macPortName +
                "], ifindex:[" +
                m_macPortIfIndex +
                "], macPortMap:[" +
                m_macPortMap +
                "]";
    }

    public Integer getIfIndex() {
        return m_macPortIfIndex;
    }
    public void setIfIndex(Integer macPortIfIndex) {
        m_macPortIfIndex = macPortIfIndex;
    }
    public String getMacPortName() {
        return m_macPortName;
    }
    public void setMacPortName(String macPortName) {
        m_macPortName = macPortName;
    }
    public Map<String, Set<InetAddress>> getMacPortMap() {
        return m_macPortMap;
    }
    public Integer getNodeId() {
        return m_nodeId;
    }
    public void setNodeId(Integer nodeId) {
        m_nodeId = nodeId;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_macPortIfIndex == null) ? 0
                                              : m_macPortIfIndex.hashCode());
        result = prime * result
                + m_macPortMap.hashCode();
        result = prime * result
                + ((m_macPortName == null) ? 0 : m_macPortName.hashCode());
        result = prime * result
                + ((m_nodeId == null) ? 0 : m_nodeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MacPort other = (MacPort) obj;
        if (m_macPortIfIndex == null) {
            if (other.m_macPortIfIndex != null)
                return false;
        } else if (!m_macPortIfIndex.equals(other.m_macPortIfIndex))
            return false;
        if (!m_macPortMap.equals(other.m_macPortMap))
            return false;
        if (m_macPortName == null) {
            if (other.m_macPortName != null)
                return false;
        } else if (!m_macPortName.equals(other.m_macPortName))
            return false;
        if (m_nodeId == null) {
            return other.m_nodeId == null;
        } else return m_nodeId.equals(other.m_nodeId);
    }
}
