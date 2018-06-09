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

package org.opennms.netmgt.model.topology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BridgeForwardingTable implements Topology {
    
    private static final Logger LOG = LoggerFactory.getLogger(BridgeForwardingTable.class);

    public static Set<BridgePortWithMacs> getThroughSet(BridgeForwardingTable bridgeFt, Set<BridgePort> excluded) throws BridgeTopologyException {

        for (BridgePort exclude: excluded) {
            if (exclude.getNodeId().intValue() != bridgeFt.getNodeId().intValue()) {
                throw new BridgeTopologyException("getThroughSet: node mismatch ["
                        + bridgeFt.getNodeId() + "]", exclude);
            }
        }
        Set<BridgePortWithMacs> throughSet= new HashSet<BridgePortWithMacs>();
        bridgeFt.getPorttomac().stream().filter(ptm ->!excluded.contains(ptm.getPort())).forEach(ptm -> throughSet.add(ptm));
        return throughSet;
    }

    public static BridgeForwardingTable create(Bridge bridge, Set<BridgeForwardingTableEntry> entries) throws BridgeTopologyException {
        if (bridge == null) {
            throw new BridgeTopologyException("bridge must not be null");
        }
        if (entries == null) {
            throw new BridgeTopologyException("bridge forwarding table must not be null");
        }
        
        for (BridgeForwardingTableEntry link: entries) {
            if (link.getNodeId().intValue() != bridge.getNodeId().intValue()) {
                throw new BridgeTopologyException("create: bridge:["+ bridge.getNodeId()+ "] and forwarding table must have the same nodeid", link);                
            }
        }
        final BridgeForwardingTable bridgeFt = new BridgeForwardingTable(bridge,entries);

        entries.stream().filter(link -> link.getBridgeDot1qTpFdbStatus() 
                                == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF).
                                forEach(link -> {
            bridgeFt.getIdentifiers().add(link.getMacAddress());
            if (LOG.isDebugEnabled()) {
                LOG.debug("create: bridge:[{}] adding bid {}",
                          bridge.getNodeId(),
                          link.printTopology());
            }
        });
        
        for (BridgeForwardingTableEntry link: entries) {
            if (link.getBridgeDot1qTpFdbStatus() 
                                != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED ) {
                continue;
            }
            if (bridgeFt.getIdentifiers().contains(link.getMacAddress())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] skip bid {}",
                          bridge.getNodeId(),
                          link.printTopology());
                }
                continue;
            }
                
            BridgePort bridgeport = BridgePort.getFromBridgeForwardingTableEntry(link);
                
            BridgePortWithMacs bpwm = bridgeFt.getBridgePortWithMacs(bridgeport);
            if (bpwm == null ) {
                bridgeFt.getPorttomac().add(BridgePortWithMacs.create(bridgeport,new HashSet<String>()));
            }
            bridgeFt.getBridgePortWithMacs(bridgeport).getMacs().add(link.getMacAddress());

            if (bridgeFt.getMactoport().containsKey(link.getMacAddress())) {
                bridgeFt.getDuplicated().put(link.getMacAddress(), new HashSet<BridgePort>());
                bridgeFt.getDuplicated().get(link.getMacAddress()).add(bridgeport);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] duplicated {}",
                              bridge.getNodeId(),
                              link.printTopology());
                }
                continue;
            }
            
            if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] adding {}",
                          bridge.getNodeId(),
                          link.printTopology());
            }
            bridgeFt.getMactoport().put(link.getMacAddress(), bridgeport);
        }

        for (String mac: bridgeFt.getDuplicated().keySet()) {
            BridgePort saved = bridgeFt.getMactoport().remove(mac);            
            if (LOG.isDebugEnabled()) {
                LOG.debug("create: bridge:[{}] remove duplicated [{}] from {}",
                          bridge.getNodeId(),
                          mac,
                          saved.printTopology());
            }

            BridgePortWithMacs savedwithmacs = bridgeFt.getBridgePortWithMacs(saved);
            savedwithmacs.getMacs().remove(mac);
            
            for (BridgePort dupli: bridgeFt.getDuplicated().get(mac)) {
                BridgePortWithMacs dupliwithmacs = bridgeFt.getBridgePortWithMacs(dupli);
                dupliwithmacs.getMacs().remove(mac);
            }
            bridgeFt.getDuplicated().get(mac).add(saved);
        }

        return bridgeFt;
    }
    
    private final Bridge m_bridge;
    private final Set<BridgeForwardingTableEntry> m_entries;
    private Map<String, BridgePort> m_mactoport = new HashMap<String, BridgePort>();
    private Map<String, Set<BridgePort>> m_duplicated = new HashMap<String, Set<BridgePort>>();
    private Set<BridgePortWithMacs> m_porttomac = new HashSet<BridgePortWithMacs>();

    private BridgeForwardingTable(Bridge bridge, Set<BridgeForwardingTableEntry> entries) {
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
            m_porttomac.stream().filter(bpm -> bpm.getPort().getBridgePort() == bp).iterator().next();
        if (bpwm == null)
            return null;    
        return bpwm.getPort();
    }

    public void setRootPort(Integer rootPort) {
        m_bridge.setRootPort(rootPort);
    }

    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append(m_bridge.printTopology());
        strbfr.append("\n");
        boolean rn = false;
        for (BridgeForwardingTableEntry bftentry: m_entries) {
            if (rn) {
                strbfr.append("\n");
            } else {
                rn = true;
            }
            strbfr.append(bftentry.printTopology());
        }
        return strbfr.toString();
    }

}
