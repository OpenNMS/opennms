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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class BridgeForwardingTable implements Topology {

    private final Bridge m_bridge;
    private final Set<BridgeForwardingTableEntry> m_entries;
    private Map<String, BridgePort> m_mactoport = new HashMap<>();
    private Map<String, Set<BridgePort>> m_duplicated = new HashMap<>();
    private final Set<BridgePortWithMacs> m_porttomac = new HashSet<>();

    public BridgeForwardingTable(Bridge bridge, Set<BridgeForwardingTableEntry> entries) {
        m_bridge = bridge;
        m_entries = entries;
    }

    public Set<BridgePortWithMacs> getPorttomac() {
        return m_porttomac;
    }

    public BridgePortWithMacs getBridgePortWithMacs(BridgePort port) {
        for (BridgePortWithMacs bpmx: m_porttomac) {
            if (bpmx.getPort().equals(port)) {
                return bpmx;
            }
        }
        return null;
    }
    
    public Map<String, BridgePort> getMactoport() {
        return m_mactoport;
    }


    public void setMactoport(Map<String, BridgePort> mactoport) {
        m_mactoport = mactoport;
    }

    public Map<String, Set<BridgePort>> getDuplicated() {
        return m_duplicated;
    }


    public void setDuplicated(
            Map<String, Set<BridgePort>> duplicated) {
        m_duplicated = duplicated;
    }


    public Set<BridgeForwardingTableEntry> getEntries() {
        return m_entries;
    }


    public int getBftSize() {
        return m_entries.size();
    }
    
    public Set<String> getBftMacs() {
        return m_mactoport.keySet();
    }
    
    public Integer getNodeId() {
        return m_bridge.getNodeId();
    }

    public Set<String> getIdentifiers() {
        return m_bridge.getIdentifiers();
    }

    public Bridge getBridge() {
        return m_bridge;
    }

    public Integer getRootBridgePort() {
        return m_bridge.getRootPort();
    }

    public BridgePort getRootPort() {
        return getPort(m_bridge.getRootPort());
    }

    public BridgePort getPort(Integer bp) {
        BridgePortWithMacs bpwm = 
            m_porttomac.stream().filter(bpm -> Objects.equals(bpm.getPort().getBridgePort(), bp)).iterator().next();
        if (bpwm == null)
            return null;    
        return bpwm.getPort();
    }

    public void setRootPort(Integer rootPort) {
        m_bridge.setRootPort(rootPort);
    }

    public String printTopology() {
        final List<Topology> topologies = new ArrayList<>();
        topologies.add(m_bridge);
        topologies.addAll(m_entries);
        return topologies.stream()
                .map(Topology::printTopology)
                .collect(Collectors.joining("\n"));
    }

}
