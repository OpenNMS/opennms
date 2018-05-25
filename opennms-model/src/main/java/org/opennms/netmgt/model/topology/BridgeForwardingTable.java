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
            if (exclude.getNodeId() != bridgeFt.getNodeId()) {
                throw new BridgeTopologyException("getThroughSet: node mismatch ["
                        + bridgeFt.getNodeId() + "]", exclude);
            }
        }
        Set<BridgePortWithMacs> throughSet= new HashSet<BridgePortWithMacs>();
BFT:    for (BridgePort bp: bridgeFt.getPorttomac().keySet()) {
            for (BridgePort exclude: excluded) {
                    if (bp.getBridgePort() == exclude.getBridgePort()
                    ) {
                    continue BFT;
                }
            }
            throughSet.add(bridgeFt.getPorttomac().get(bp));
        }
        return throughSet;
    }

    public static Set<BridgeForwardingTableEntry> upForwarders(BridgeForwardingTable upBridge, BridgeForwardingTable dwBridge,
                                                                  BridgePort upPort, BridgePort dwPort) throws BridgeTopologyException {
          
          if (upBridge.getNodeId() != upPort.getNodeId()) {
              throw new BridgeTopologyException("upForwarders: node mismatch ["+upBridge.getNodeId()+"]",upPort);
          }

          if (dwBridge.getNodeId() != dwPort.getNodeId()) {
              throw new BridgeTopologyException("upForwarders: node mismatch ["+dwBridge.getNodeId()+"]",dwPort);
          }
          Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
          for (BridgeForwardingTableEntry ylink: dwBridge.getBftEntries()) {
              if (ylink.getBridgePort() == dwPort.getBridgePort() 
                  && upBridge.getMactoport().get(ylink.getMacAddress()) == null 
              ) {
                  forwarders.add(ylink);
              }
          }
          if (LOG.isDebugEnabled()) {
              LOG.debug("upForwarders: -> \n{}", 
                BridgeForwardingTableEntry.printTopology(forwarders));
          }
          return forwarders;
    }

    public static Set<BridgeForwardingTableEntry> downForwarders(BridgeForwardingTable upBridge, BridgeForwardingTable dwBridge,
        BridgePort upPort, BridgePort dwPort) throws BridgeTopologyException {
        
        if (upBridge.getNodeId() != upPort.getNodeId()) {
            throw new BridgeTopologyException("downForwarders: node mismatch ["+upBridge.getNodeId()+"]",upPort);
        }

        if (dwBridge.getNodeId() != dwPort.getNodeId()) {
            throw new BridgeTopologyException("downForwarders: node mismatch ["+dwBridge.getNodeId()+"]",dwPort);
        }

        Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
        for (BridgeForwardingTableEntry xlink: upBridge.getBftEntries()) {
            if (xlink.getBridgePort() == upPort.getBridgePort() 
                && dwBridge.getMactoport().get(xlink.getMacAddress()) == null 
               
            ) {
                forwarders.add(xlink);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("downForwarders: -> \n{}", 
              BridgeForwardingTableEntry.printTopology(forwarders));
        }
        return forwarders;
    }

    public static BridgeForwardingTable create(Bridge bridge, Set<BridgeForwardingTableEntry> entries) throws BridgeTopologyException {
        if (bridge == null) {
            throw new BridgeTopologyException("bridge must not be null");
        }
        if (entries == null) {
            throw new BridgeTopologyException("bridge forwarding table must not be null");
        }
        BridgeForwardingTable bridgeFt = new BridgeForwardingTable(bridge,entries);

        for (BridgeForwardingTableEntry link: entries) {
            if (link.getNodeId().intValue() != bridge.getNodeId().intValue()) {
                throw new BridgeTopologyException("create: bridge:["+ bridge.getNodeId()+ "] and forwarding table must have the same nodeid", link);                
            }
            if (link.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                bridgeFt.getIdentifiers().add(link.getMacAddress());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}]  adding bridge identifier {}",
                              bridge.getNodeId(),
                              link.printTopology());
                }
                continue;
            }
        }
        Map<BridgePort, Set<String>> porttomac = new HashMap<BridgePort, Set<String>>();
        for (BridgeForwardingTableEntry link: entries) {
            if (link.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED ) {
                if (bridgeFt.getIdentifiers().contains(link.getMacAddress())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("create: bridge:[{}] skipping bft bridge identifier {}",
                                  bridge.getNodeId(),
                                  link.printTopology());
                    }
                    continue;
                }
            }

            if (bridgeFt.getMactoport().containsKey(link.getMacAddress())) {
                bridgeFt.getDuplicated().put(link.getMacAddress(), new HashSet<BridgeForwardingTableEntry>());
                bridgeFt.getDuplicated().get(link.getMacAddress()).add(link);
                BridgeForwardingTableEntry firstDuplicated = bridgeFt.getMactoport().remove(link.getMacAddress());
                BridgePort firstduplicatedbridgeport = BridgePort.getFromBridgeForwardingTableEntry(firstDuplicated);
                bridgeFt.getDuplicated().get(link.getMacAddress()).add(firstDuplicated);
                porttomac.get(firstduplicatedbridgeport).
                remove(link.getMacAddress());
                continue;
            }
            if (bridgeFt.getDuplicated().containsKey(link.getMacAddress())) {
                bridgeFt.getDuplicated().get(link.getMacAddress()).add(link);
                continue;
            }
            bridgeFt.getMactoport().put(link.getMacAddress(), link);
            
            BridgePort bridgeport = BridgePort.getFromBridgeForwardingTableEntry(link);
            if (!porttomac.containsKey(bridgeport)) {       
                porttomac.put(bridgeport, new HashSet<String>());
            }
            porttomac.get(bridgeport).add(link.getMacAddress());
        }

        for (String mac: bridgeFt.getDuplicated().keySet()) {
            Set<String> container = new HashSet<String>();
            BridgeForwardingTableEntry saved = null;
            for (BridgeForwardingTableEntry link: bridgeFt.getDuplicated().get(mac)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] mac:[{}], duplicated {}",
                              bridge.getNodeId(),
                              mac,
                              link.printTopology());
                }
                BridgePort bridgeport = BridgePort.getFromBridgeForwardingTableEntry(link);
                if (container.size() < porttomac.get(bridgeport).size()) {
                    saved=link;
                    container = porttomac.get(bridgeport);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("create: bridge:[{}] mac:[{}] saved {}",
                          bridge.getNodeId(),
                          mac,
                          saved.printTopology());
            }
            
            bridgeFt.getMactoport().put(mac, saved);
            
            BridgePort bridgeport = BridgePort.getFromBridgeForwardingTableEntry(saved);
            if (!bridgeFt.getPorttomac().containsKey(bridgeport)) {       
                porttomac.put(bridgeport, new HashSet<String>());
            }
            porttomac.get(bridgeport).add(saved.getMacAddress());

        }
        
        for (BridgePort port: porttomac.keySet()) {
            bridgeFt.getPorttomac().put(port, BridgePortWithMacs.create(port, porttomac.get(port)));
        }
        return bridgeFt;
    }
    
    private final Bridge m_bridge;
    private final Set<BridgeForwardingTableEntry> m_entries;
    private Map<String, BridgeForwardingTableEntry> m_mactoport = new HashMap<String, BridgeForwardingTableEntry>();
    private Map<String, Set<BridgeForwardingTableEntry>> m_duplicated = new HashMap<String, Set<BridgeForwardingTableEntry>>();
    private Map<BridgePort, BridgePortWithMacs> m_porttomac = new HashMap<BridgePort, BridgePortWithMacs>();

    private BridgeForwardingTable(Bridge bridge, Set<BridgeForwardingTableEntry> entries) {
        m_bridge = bridge;
        m_entries = entries;
    }

    public Map<BridgePort,BridgePortWithMacs> getPorttomac() {
        return m_porttomac;
    }

    public BridgePortWithMacs getBridgePortWithMacs(BridgePort port) {
            return m_porttomac.get(port);
    }

    public Set<BridgeForwardingTableEntry> getBftEntries() {
        return new HashSet<BridgeForwardingTableEntry>(m_mactoport.values());
    }
    
    public Map<String, BridgeForwardingTableEntry> getMactoport() {
        return m_mactoport;
    }


    public void setMactoport(Map<String, BridgeForwardingTableEntry> mactoport) {
        m_mactoport = mactoport;
    }

    public Map<String, Set<BridgeForwardingTableEntry>> getDuplicated() {
        return m_duplicated;
    }


    public void setDuplicated(
            Map<String, Set<BridgeForwardingTableEntry>> duplicated) {
        m_duplicated = duplicated;
    }


    public Set<BridgeForwardingTableEntry> getEntries() {
        return m_entries;
    }


    public int getBftSize() {
        return m_mactoport.size();
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
        for (BridgePort bridgeport: m_porttomac.keySet()) {
            if (bridgeport.getBridgePort() == bp) {
                return bridgeport;
            }
        }
        return null;        
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
