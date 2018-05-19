/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BridgeForwardingTable implements Topology {
    
    private static final Logger LOG = LoggerFactory.getLogger(BridgeForwardingTable.class);


    public static BridgeForwardingTable create(Bridge bridge, Set<BridgeForwardingTableEntry> entries) throws BridgeTopologyException {
        if (bridge == null) {
            throw new BridgeTopologyException("bridge must not be null");
        }
        if (entries == null) {
            throw new BridgeTopologyException("bridge forwarding table must not be null");
        }
        BridgeForwardingTable bft = new BridgeForwardingTable(bridge);
        bft.setBFTEntries(new HashSet<BridgeForwardingTableEntry>());
        for (BridgeForwardingTableEntry link: entries) {
            if (link.getNodeId().intValue() != bridge.getNodeId().intValue()) {
                throw new BridgeTopologyException("bridge:["+ bridge.getNodeId()+ "] and forwarding table must have the same nodeid", link);                
            }
            bft.getBFTEntries().add(link);
        }
        return bft;
    }
    
    private final Bridge m_bridge;
    private Set<BridgeForwardingTableEntry> m_entries = new HashSet<BridgeForwardingTableEntry>();

    private BridgeForwardingTable(Bridge bridge) {
        m_bridge = bridge;
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
    
    public Set<BridgeForwardingTableEntry> getBFTEntries() {
        return m_entries;
    }

    public void setBFTEntries(Set<BridgeForwardingTableEntry> entries) {
        m_entries = entries;
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

    public static Map<String, BridgeForwardingTableEntry> getMacMap(BridgeForwardingTable bridgeFt) {
        Map<String, BridgeForwardingTableEntry> mactoport = new HashMap<String, BridgeForwardingTableEntry>();
        for (BridgeForwardingTableEntry link: bridgeFt.getBFTEntries()) {
            if (link.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED ) {
                if (bridgeFt.getIdentifiers().contains(link.getMacAddress())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getMacMap: removing from bft bridge identifier {}",
                                  link.printTopology());
                    }
                    continue;
                }
            }
            mactoport.put(link.getMacAddress(), link);
            
        }
    
        return mactoport;
        
    }

    public static Map<String, Set<BridgeForwardingTableEntry>> deleteDuplicatedMac(BridgeForwardingTable bridgeFt) {
        Map<Integer,Set<String>> bft=new HashMap<Integer, Set<String>>();
        Map<String, Set<BridgeForwardingTableEntry>> duplicated = new HashMap<String, Set<BridgeForwardingTableEntry>>();
        Map<String, BridgeForwardingTableEntry> mactoport = new HashMap<String, BridgeForwardingTableEntry>();
        for (BridgeForwardingTableEntry link: bridgeFt.getBFTEntries()) {
            if (!bft.containsKey(link.getBridgePort())) {
                bft.put(link.getBridgePort(), new HashSet<String>());
            }
            bft.get(link.getBridgePort()).add(link.getMacAddress());
            if (mactoport.containsKey(link.getMacAddress())) {
                duplicated.put(link.getMacAddress(), new HashSet<BridgeForwardingTableEntry>());
                duplicated.get(link.getMacAddress()).add(link);
                duplicated.get(link.getMacAddress()).add(mactoport.remove(link.getMacAddress()));
                continue;
            }
            if (duplicated.containsKey(link.getMacAddress())) {
                duplicated.get(link.getMacAddress()).add(link);
                continue;
            }
            mactoport.put(link.getMacAddress(), link);
        }
        for (String mac: duplicated.keySet()) {
            Set<String> container = new HashSet<String>();
            BridgeForwardingTableEntry saved = null;
            for (BridgeForwardingTableEntry link: duplicated.get(mac)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deleteDuplicatedMac: mac:[{}], duplicated {}",
                          mac,link.printTopology());
                }
                if (container.size() < bft.get(link.getBridgePort()).size()) {
                    saved=link;
                    container = bft.get(link.getBridgePort());
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleteDuplicatedMac: mac:[{}] saved {}",
                      mac,saved.printTopology());
            }
            mactoport.put(mac, saved);
        }
        
        bridgeFt.getBFTEntries().clear();
        bridgeFt.getBFTEntries().addAll(mactoport.values());
        return duplicated;
    }

    public static void updateIdentifiers(BridgeForwardingTable bridgeFt) {
        for (BridgeForwardingTableEntry link: bridgeFt.getBFTEntries()) {
            if (link.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                bridgeFt.getIdentifiers().add(link.getMacAddress());
                if (BridgeSimpleConnection.LOG.isDebugEnabled()) {
                    BridgeSimpleConnection.LOG.debug("doit: adding bridge identifier {}",
                              link.printTopology());
                }
            }
        }
    }

}
