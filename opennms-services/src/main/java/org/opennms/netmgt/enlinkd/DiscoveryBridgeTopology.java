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

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTable;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgePortWithMacs;
import org.opennms.netmgt.model.topology.BridgeSimpleConnection;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class DiscoveryBridgeTopology extends Discovery {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryBridgeTopology.class);

    private Map<Integer,BridgeForwardingTable> m_bridgeFtMapUpdate = new HashMap<Integer, BridgeForwardingTable>();
    private final BroadcastDomain m_domain;
    private Set<Integer> m_failed;
    private Set<Integer> m_parsed;
        
    public BroadcastDomain getDomain() {
        return m_domain;
    }
    
    public void addUpdatedBFT(Integer bridgeid, Set<BridgeForwardingTableEntry> notYetParsedBFT) {
        if (m_domain.getBridge(bridgeid) == null) {
            Bridge.create(m_domain, bridgeid);
        }
        try {
            m_bridgeFtMapUpdate.put(bridgeid, BridgeForwardingTable.create(m_domain.getBridge(bridgeid), notYetParsedBFT));
        } catch (BridgeTopologyException e) {
            LOG.error("calculate:  node[{}], {}, topology:\n{}", 
                      bridgeid,
                      e.getMessage(),
                      e.printTopology(),
                      e);
        }
    }

    public DiscoveryBridgeTopology(EnhancedLinkd linkd,BroadcastDomain domain) {
        super(linkd,linkd.getBridgeTopologyInterval(),0);
        Assert.notNull(domain);
        m_domain=domain;
    }
        
    @Override
    public String getInfo() {
                StringBuffer info = new StringBuffer();
                info.append(getName());
                if (m_domain != null) {
                    info.append(" domain nodes: ");
                    info.append(m_domain.getBridgeNodesOnDomain());  
                }
                if (m_bridgeFtMapUpdate != null) {
                    info.append(", updated bft nodes: ");
                    info.append(m_bridgeFtMapUpdate.keySet());  
                }
                if (m_parsed != null) {
                    info.append(", parsed bft nodes: ");
                    info.append(m_parsed);  
                }
                if (m_failed != null) {
                    info.append(", failed bft nodes: ");
                    info.append(m_failed);  
                }
                return  info.toString();
    }

    @Override
    public void doit() {
        Assert.notNull(m_bridgeFtMapUpdate);
    	Date now = new Date();
                             
        for (Integer bridgeid : m_bridgeFtMapUpdate.keySet()) {
            m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,bridgeid);
            sendStartEvent(bridgeid);
        }
        
        calculate(); 
        for (Integer bridgeid: m_parsed)
        {
                LOG.info("run: bridge:[{}], topology calcutation success.", 
                          bridgeid);
                sendCompletedEvent(bridgeid);
        }
        
        for (Integer bridgeid : m_failed) {
            LOG.error("run: bridge:[{}], topology calcutation failed.", 
                      bridgeid);
        }

        LOG.debug("run: saving Topology.");
        try {
            m_linkd.getQueryManager().store(m_domain, now);
        } catch (BridgeTopologyException e) {
            LOG.error("run: saving topology failed: {}. {}", 
                      e.getMessage(),
                      e.printTopology());
            return;
        } catch (ConcurrentModificationException e) {
            LOG.error("run: bridge:[{}], saving topology failed: {}. {}", 
                      e.getMessage(),
                      m_domain.printTopology());
            
        }
        LOG.debug("run: saved Topology.");
    }
            
    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

    private Bridge calcRootBridge() {
        // no spanning tree root?
        // why I'm here?
        // not root bridge defined (this mean no calculation yet done...
        // so checking the best into not parsed
        int size = 0;
        Bridge elected = null;
        for (Integer bridgeid : m_bridgeFtMapUpdate.keySet()) {
            Bridge bridge = m_domain.getBridge(bridgeid);
            LOG.debug("calculate: bridge:[{}] bft size \"{}\" in topology",
                      bridge.getNodeId(),
                      m_bridgeFtMapUpdate.get(bridgeid).getBftSize());
            if (size < m_bridgeFtMapUpdate.get(bridgeid).getBftSize()) {
                elected = bridge;
                size = m_bridgeFtMapUpdate.get(bridgeid).getBftSize();
            }
        }
        if (elected != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: bridge:[{}] 'elected' root with max bft size \"{}\" in topology",
                          elected.getNodeId(), size);
            }
        } else {
            elected = m_domain.getBridges().iterator().next();
            if (LOG.isDebugEnabled()) {
        	LOG.debug("calculate: bridge:[{}] 'elected' first bridge in topology", 
                       elected.getNodeId());
            }
        }

        return elected;
        
    }

    private Bridge electRootBridge() throws BridgeTopologyException {
        Bridge electedRoot = BroadcastDomain.electRootBridge(m_domain);
        Bridge rootBridge = m_domain.getRootBridge();
        
        if (electedRoot == null) {
            if (rootBridge != null) {
                electedRoot = rootBridge;
            } else {
                electedRoot = calcRootBridge();
            }
        }

        if (electedRoot.getNodeId() == null) {
            throw new BridgeTopologyException("elected Root bridge id cannot be null", electedRoot);
        }

        return electedRoot;
    }
    
    private void cleanTopology() {
        for (Integer bridgeId: m_bridgeFtMapUpdate.keySet()) {
            if (m_domain.getBridge(bridgeId).isNewTopology()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: bridge:[{}] is 'new'. skip clean topology   ", 
                            bridgeId);
                }
                continue;
            }
            
            try {
                BroadcastDomain.clearTopologyForBridge(m_domain,bridgeId);
            } catch (BridgeTopologyException e) {
                LOG.error("calculate: bridge:[{}], {}, \n{}", bridgeId, e.getMessage(),e.printTopology());
                m_failed.add(bridgeId);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: bridge:[{}] cleaned topology -> \n{}", 
                        bridgeId,
                        m_domain.printTopology());
            }
        }
        
    }

    private void root(BridgeForwardingTable rootBft,Map<Integer, BridgeForwardingTable> bridgeFtMapCalcul) throws BridgeTopologyException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: bridge:[{}] elected has updated bft",
                      rootBft.getNodeId());
        }
        if (m_domain.getSharedSegments().isEmpty()) {
            rootBft.getBridge().setRootBridge();
            rootBft.getPorttomac().
                        stream().
                        forEach(ts -> 
                            SharedSegment.createAndAddToBroadcastDomain(m_domain,ts));
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: bridge:[{}] elected [root] is first\n{}", 
                          rootBft.getNodeId(),
                     m_domain.printTopology());
            }

            return;
        } 
        BridgeForwardingTable oldRootBft = bridgeFtMapCalcul.get(m_domain.getRootBridge().getNodeId());
        down(oldRootBft,rootBft,null,bridgeFtMapCalcul,0);
    }

    protected  void calculate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: start\n{}", 
                 m_domain.printTopology());
        }

        m_parsed=new HashSet<Integer>();
        m_failed = new HashSet<Integer>();
        Bridge electedRoot;
        try {
            electedRoot = electRootBridge();
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: {}, topology:\n{}",
                      e.getMessage(),
                      e.printTopology(),
                      e);
            m_failed.addAll(m_bridgeFtMapUpdate.keySet());
            return;
        }

        cleanTopology();

        Map<Integer, BridgeForwardingTable> bridgeFtMapCalcul = new HashMap<Integer, BridgeForwardingTable>();
        for (Bridge bridge: m_domain.getBridges()) {
            if (m_bridgeFtMapUpdate.containsKey(bridge.getNodeId())) {
                continue;
            }
            if (bridge.isNewTopology()) {
                LOG.warn("calculate: bridge:[{}] is new without update bft",
                          bridge.getNodeId());
                continue;
            }
            try {
                bridgeFtMapCalcul.put(bridge.getNodeId(),
                                      BridgeForwardingTable.create(bridge,
                                                                   BroadcastDomain.calculateBFT(m_domain,
                                                                                                bridge)));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}] clear topology. no calculated bft: {} ->\n{}",
                         bridge.getNodeId(), e.getMessage(),
                         e.printTopology());
                m_failed.addAll(m_bridgeFtMapUpdate.keySet());
                m_domain.clearTopology();
                bridgeFtMapCalcul.clear();
                calculate();
            }
        }
        
        BridgeForwardingTable rootBft = 
                m_bridgeFtMapUpdate.get(electedRoot.getNodeId());
        
        if ( rootBft != null ) {
            try {
                root(rootBft, bridgeFtMapCalcul);
                m_parsed.add(rootBft.getNodeId());
            } catch (BridgeTopologyException e) {
                LOG.error("calculate: bridge:[{}], {}, \n{}", rootBft.getNodeId(), e.getMessage(),e.printTopology());
                m_failed.addAll(m_bridgeFtMapUpdate.keySet());
                return;
            }
        } 
        
        if (rootBft == null) {
            rootBft = bridgeFtMapCalcul.get(electedRoot.getNodeId());
        }
        
        if (m_domain.getRootBridge() != null && m_domain.getRootBridge().getNodeId() != electedRoot.getNodeId()) {
            try {
                BroadcastDomain.hierarchySetUp(m_domain,electedRoot);
                LOG.debug("calculate: bridge:[{}] elected is new [root] ->\n{}",
                          electedRoot.getNodeId(), 
                          m_domain.printTopology());
            } catch (BridgeTopologyException e) {
                LOG.error("calculate: bridge:[{}], {}, \n{}", electedRoot.getNodeId(), e.getMessage(),e.printTopology());
                m_failed.addAll(m_bridgeFtMapUpdate.keySet());
                m_failed.add(electedRoot.getNodeId());
                return;
            }
        }
        
        for (Integer bridgeid: m_bridgeFtMapUpdate.keySet()) {
            if (m_parsed.contains(bridgeid) || m_failed.contains(bridgeid)) {
                continue;
            }
            try {
                down(rootBft, m_bridgeFtMapUpdate.get(bridgeid),null,bridgeFtMapCalcul,0);
                m_parsed.add(bridgeid);
            } catch (BridgeTopologyException e) {
                LOG.error("calculate: bridge: [{}], {}, \n{}", bridgeid, e.getMessage(),e.printTopology());
                m_failed.add(bridgeid);
            }                
        }  

        m_bridgeFtMapUpdate.values().stream().
            filter(ft -> m_parsed.contains(ft.getNodeId())).
                forEach(ft -> BroadcastDomain.addforwarders(m_domain, ft));
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: upated forwarders\n{}", 
                 m_domain.printTopology());
        }

        bridgeFtMapCalcul.values().stream().forEach(ft -> BroadcastDomain.addforwarders(m_domain, ft));
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: calculated forwarders\n{}", 
                 m_domain.printTopology());
        }
        m_parsed.stream().forEach( parsed -> m_bridgeFtMapUpdate.remove(parsed));
    }
    
    private void down(BridgeForwardingTable bridgeUpFT,  
            BridgeForwardingTable bridgeFT, BridgeSimpleConnection upsimpleconn, Map<Integer,BridgeForwardingTable> bridgeFtMapCalcul, Integer level) throws BridgeTopologyException {

        if (++level == BroadcastDomain.maxlevel) {
            throw new BridgeTopologyException(
                          "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], too many iteration");
        }

        if (upsimpleconn == null) {
             upsimpleconn = 
                    new BridgeSimpleConnection(bridgeUpFT, 
                                                 bridgeFT);
            if (upsimpleconn.doit()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("down: level: {}, bridge:[{}] -> {}", 
                              level,
                             bridgeFT.getNodeId(),
                             upsimpleconn.printTopology());
                }
            } else {
                throw new BridgeTopologyException(
                      "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], no simple connection", 
                      upsimpleconn);
            }
        }
        if (level == 1) {
            bridgeFT.setRootPort(upsimpleconn.getSecondBridgePort());
            LOG.debug("down: level: {}, bridge:[{}]. set root port:[{}]", 
                      level,
                         bridgeFT.getNodeId(),
                         upsimpleconn.getSecondBridgePort());
        }

        SharedSegment upSegment = m_domain.getSharedSegment(upsimpleconn.getFirstPort());
        if (upSegment == null) {
            throw new BridgeTopologyException(
                          "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], up segment not found");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("down: level: {}, bridge:[{}]. up segment -> \n{} ",
                        level,
                        bridgeFT.getNodeId(),
                        upSegment.printTopology());
        }

        Set<BridgePort> parsed = new HashSet<BridgePort>();
        parsed.add(bridgeFT.getRootPort());

        Set<BridgeForwardingTable> checkforwarders = new HashSet<BridgeForwardingTable>();
        checkforwarders.add(bridgeUpFT);
        checkforwarders.add(bridgeFT);

        Map<BridgePortWithMacs, Set<BridgePortWithMacs>> splitted 
            = new HashMap<BridgePortWithMacs, Set<BridgePortWithMacs>>();
        
        BridgeForwardingTable nextDownBridge = null;
        BridgeSimpleConnection nextDownSP = null;
        boolean levelfound = false;
        
        Set<String> maconupsegment = BridgeSimpleConnection.getMacs(bridgeUpFT, bridgeFT, upsimpleconn);
        
        for (Bridge curbridge : m_domain.getBridgeOnSharedSegment(upSegment)) {
            
            if (curbridge.getNodeId().intValue() == upSegment.getDesignatedBridge().intValue()) {
                continue;
            }
            
            BridgeForwardingTable curBridgeFT = m_bridgeFtMapUpdate.get(curbridge.getNodeId());
            if (curBridgeFT == null) {
                curBridgeFT = bridgeFtMapCalcul.get(curbridge.getNodeId());
            }
            if (curBridgeFT == null) {
                throw new BridgeTopologyException(
                      "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], no bft for: " + curbridge.printTopology());
            }
            checkforwarders.add(curBridgeFT);
            
            BridgeSimpleConnection simpleconn = 
                    new BridgeSimpleConnection(curBridgeFT,
                                       bridgeFT);
            if (simpleconn.doit()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("down: level: {}, bridge:[{}]. {}", 
                             level,
                             bridgeFT.getNodeId(),
                             simpleconn.printTopology());
                }
            } else {
                throw new BridgeTopologyException(
                              "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], no simple connection", 
                              simpleconn);
            }
            if (simpleconn.getSecondBridgePort() != 
                    bridgeFT.getBridge().getRootPort()
                && simpleconn.getFirstBridgePort() != 
                    curbridge.getRootPort()) {
                throw new BridgeTopologyException(
                              "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], Topology mismatch. NO ROOTS");
            }

            // bridge is a leaf of curbridge
            if (simpleconn.getSecondBridgePort() 
                    == bridgeFT.getRootBridgePort()
                    && 
                    simpleconn.getFirstBridgePort() != curbridge.getRootPort()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("down: level: {}, bridge: [{}], is 'down' of -> {}",
                              level,
                              bridgeFT.getNodeId(),
                              simpleconn.getFirstPort().printTopology()
                              );
                }
                if (nextDownBridge != null) {
                    throw new BridgeTopologyException(
                              "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], Topology mismatch. LEAF OF TWO");
                }
                if (levelfound) {
                    throw new BridgeTopologyException(
                              "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], Topology mismatch. LEAF AND LEVEL FOUND");
                }
                nextDownBridge = curBridgeFT;
                nextDownSP = simpleconn;
                continue;
            }
            
            // bridge is up curbridge
            if (simpleconn.getFirstBridgePort() == curBridgeFT.getRootBridgePort()
                    && simpleconn.getSecondBridgePort() != bridgeFT.getRootBridgePort()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("down: level: {}, bridge: [{}], {} is 'up' of -> [{}]",
                              level,
                              bridgeFT.getNodeId(),
                              simpleconn.getSecondPort().printTopology(),
                              curbridge.getNodeId()
                              );
                }
                if (nextDownBridge != null) {
                    throw new BridgeTopologyException(
                      "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], Topology mismatch. LEAF AND LEVEL FOUND");
                }
                levelfound = true;
                if (!splitted.containsKey(bridgeFT.getBridgePortWithMacs(simpleconn.getSecondPort()))) {
                    splitted.put(bridgeFT.getBridgePortWithMacs(simpleconn.getSecondPort()),
                                 new HashSet<BridgePortWithMacs>());
                }
                splitted.get(bridgeFT.getBridgePortWithMacs(simpleconn.getSecondPort())).
                    add(curBridgeFT.getBridgePortWithMacs(simpleconn.getFirstPort()));
                parsed.add(simpleconn.getSecondPort());
                continue;
            }
            //here are all the simple connection in which the connection is the root port
            maconupsegment.retainAll(BridgeSimpleConnection.getMacs(curBridgeFT, bridgeFT, simpleconn));
        } // end of loop on up segment bridges
        
        if (nextDownBridge != null) {
            down(nextDownBridge, bridgeFT, nextDownSP,bridgeFtMapCalcul,level);
            return;
        }
        
        SharedSegment.merge(m_domain, 
                            upSegment, 
                            splitted,
                            maconupsegment,
                            bridgeFT.getRootPort(),
                            BridgeForwardingTable.getThroughSet(bridgeFT, parsed));
        checkforwarders.stream().forEach(ft -> BroadcastDomain.addforwarders(m_domain, ft));
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("down: level: {}, bridge:[{}], merged ->\n{}", 
                      level,
                      bridgeFT.getNodeId(),
                      m_domain.printTopology());
        }        
    }
    
}

