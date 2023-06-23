/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
