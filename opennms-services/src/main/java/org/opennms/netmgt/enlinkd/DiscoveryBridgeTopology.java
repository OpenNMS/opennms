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
    
    public Set<Integer> getFailed() {
        return m_failed;
    }

    public Set<Integer> getParsed() {
        return m_parsed;
    }

    public void addUpdatedBFT(Integer bridgeid, Set<BridgeForwardingTableEntry> notYetParsedBFT) {
        if (m_domain.getBridge(bridgeid) == null) {
            Bridge.create(m_domain, bridgeid);
        }
        try {
            m_bridgeFtMapUpdate.put(bridgeid, BridgeForwardingTable.create(m_domain.getBridge(bridgeid), notYetParsedBFT));
        } catch (BridgeTopologyException e) {
            LOG.warn("calculate:  node[{}], {}, topology:\n{}", 
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
    public void runDiscovery() {
        Assert.notNull(m_bridgeFtMapUpdate);
        Date now = new Date();

        synchronized (m_domain) {
            for (Integer bridgeid : m_bridgeFtMapUpdate.keySet()) {
                m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,bridgeid);
            }
            
            LOG.debug("run: calculate start"); 
            calculate(); 
            LOG.debug("run: calculate end"); 
            
            LOG.debug("run: save start");
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
                return;
            }
            LOG.debug("run: save end");
        }
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
            LOG.debug("calculate: bridge:[{}] elected [root] is first:{}", 
                      rootBft.getNodeId(),
                 m_domain.getBridgeNodesOnDomain());
            return;
        } 
        BridgeForwardingTable oldRootBft = bridgeFtMapCalcul.get(m_domain.getRootBridge().getNodeId());
        BridgeSimpleConnection sp = BridgeSimpleConnection.createAndRun(oldRootBft, rootBft);
        rootBft.setRootPort(sp.getSecondBridgePort());
        down(oldRootBft,rootBft,sp,bridgeFtMapCalcul,0);
    }

    protected  void calculate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: domain\n{}", 
                      m_domain.printTopology());
        }
        m_parsed = new HashSet<Integer>();
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
        
        Map<Integer, BridgeForwardingTable> bridgeFtMapCalcul = new HashMap<Integer, BridgeForwardingTable>();
        
        if (m_bridgeFtMapUpdate.keySet().equals(m_domain.getBridgeNodesOnDomain())) {
            m_domain.clearTopology();
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: domain cleaned ->\n{}", 
                          m_domain.printTopology());
            }
        } else {
            for (Integer bridgeId: m_bridgeFtMapUpdate.keySet()) {
                if (m_domain.getBridge(bridgeId).isNewTopology()) {
                    LOG.debug("calculate: bridge:[{}] is 'new'. skip clean topology   ", 
                                bridgeId);
                    continue;
                }
                
                try {
                    BroadcastDomain.clearTopologyForBridge(m_domain,bridgeId);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("calculate: bridge:[{}] cleaned ->\n{}", 
                                  bridgeId,
                                  m_domain.printTopology());
                    }
                } catch (BridgeTopologyException e) {
                    LOG.warn("calculate: bridge:[{}], {}, \n{}", bridgeId, e.getMessage(),e.printTopology());
                    m_failed.add(bridgeId);
                }
            }
            
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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("calculate: bft from domain\n{}", 
                                  bridgeFtMapCalcul.get(bridge.getNodeId()).printTopology());
                    }
                } catch (BridgeTopologyException e) {
                    LOG.warn("calculate: bridge:[{}] clear topology. no calculated bft: {} ->\n{}",
                             bridge.getNodeId(), e.getMessage(),
                             e.printTopology());
                    m_domain.clearTopology();
                    calculate();
                }
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
        
        Set<Integer> postprocessing = new HashSet<Integer>();
        for (Integer bridgeid: m_bridgeFtMapUpdate.keySet()) {
            if (m_parsed.contains(bridgeid) || m_failed.contains(bridgeid)) {
                continue;
            }
            BridgeForwardingTable bridgeFT = m_bridgeFtMapUpdate.get(bridgeid);
            if (bridgeFT.getPorttomac().size() == 1) {
                Integer bridgeFTrootPort = bridgeFT.getPorttomac().iterator().next().getPort().getBridgePort();
                bridgeFT.setRootPort(bridgeFTrootPort);
                postprocessing.add(bridgeid);
                LOG.debug("calculate: bridge:[{}] only one port:[{}] set to root. Postprocessing",
                          bridgeid, bridgeFTrootPort);
                continue;
            }
            BridgeSimpleConnection upsimpleconn;
                    

            try {
                  upsimpleconn= BridgeSimpleConnection.createAndRun(rootBft, bridgeFT);
                  if (LOG.isDebugEnabled()) {
                           LOG.debug("calculate: level: 1, bridge:[{}] -> {}", 
                                    bridgeFT.getNodeId(),
                                    upsimpleconn.printTopology());
                   }
                   bridgeFT.setRootPort(upsimpleconn.getSecondBridgePort());
                   LOG.debug("calculate: level: 1, bridge:[{}]. set root port:[{}]", 
                                bridgeFT.getNodeId(),
                                upsimpleconn.getSecondBridgePort());
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], no root port found. {}, \n{}", bridgeid, e.getMessage(),e.printTopology());
                m_failed.add(bridgeid);
                continue;
            }                
            
            try {
                down(rootBft, m_bridgeFtMapUpdate.get(bridgeid),upsimpleconn,bridgeFtMapCalcul,0);
                m_parsed.add(bridgeid);
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], no topology found. {}, \n{}", bridgeid, e.getMessage(),e.printTopology());
                m_failed.add(bridgeid);
            }                
        }  

        for (Integer failedbridgeid: new HashSet<Integer>(m_failed)) {
            BridgeForwardingTable failedBridgeFT = m_bridgeFtMapUpdate.get(failedbridgeid);
            try {
                postprocess(failedBridgeFT, rootBft,bridgeFtMapCalcul, new HashSet<Integer>(m_parsed));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], first iteration on failed. no topology found. {}, \n{}", failedbridgeid, e.getMessage(),e.printTopology());
                continue;
            }
            m_parsed.add(failedbridgeid);
            m_failed.remove(failedbridgeid);
        }        

        for (Integer failedbridgeid: new HashSet<Integer>(m_failed)) {
            BridgeForwardingTable failedBridgeFT = m_bridgeFtMapUpdate.get(failedbridgeid);
            try {
                postprocess(failedBridgeFT,rootBft, bridgeFtMapCalcul, new HashSet<Integer>(m_parsed));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], second iteration on failed. no topology found. {}, \n{}", failedbridgeid, e.getMessage(),e.printTopology());
                continue;
            }
            m_parsed.add(failedbridgeid);
            m_failed.remove(failedbridgeid);
        }        

        for (Integer postprocessbridgeid: new HashSet<Integer>(postprocessing)) {
            BridgeForwardingTable postprocessBridgeFT = m_bridgeFtMapUpdate.get(postprocessbridgeid);
            try {
                down(rootBft, postprocessBridgeFT, BridgeSimpleConnection.createAndRun(rootBft, postprocessBridgeFT), bridgeFtMapCalcul,
                     0);
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], no topology found for single port node. {}, \n{}", postprocessbridgeid, e.getMessage(),e.printTopology());
                m_failed.add(postprocessbridgeid);
                continue;
            }
            m_parsed.add(postprocessbridgeid);
        }        

        for (Integer failedbridgeid: new HashSet<Integer>(m_failed)) {
            BridgeForwardingTable failedBridgeFT = m_bridgeFtMapUpdate.get(failedbridgeid);
            try {
                postprocess(failedBridgeFT,rootBft, bridgeFtMapCalcul, new HashSet<Integer>(m_parsed));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], third iteration on failed. no topology found. {}, \n{}", failedbridgeid, e.getMessage(),e.printTopology());
                continue;
            }
            m_parsed.add(failedbridgeid);
            m_failed.remove(failedbridgeid);
        }        

        m_bridgeFtMapUpdate.values().stream().
            filter(ft -> m_parsed.contains(ft.getNodeId())).
                forEach(ft -> BroadcastDomain.addforwarders(m_domain, ft));
        
        bridgeFtMapCalcul.values().stream().
            forEach(ft -> BroadcastDomain.addforwarders(m_domain, ft));

        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: domain\n{}", 
                      m_domain.printTopology());
        }
    }
    
    private void postprocess(BridgeForwardingTable postBridgeFT, BridgeForwardingTable rootBridgeFT,Map<Integer,BridgeForwardingTable> bridgeFtMapCalcul, Set<Integer> parsed) throws BridgeTopologyException {
        Integer postbridgeid = postBridgeFT.getBridge().getNodeId();
        for (Integer parsedbridgeid : parsed) {
            if (parsedbridgeid.intValue() == rootBridgeFT.getNodeId().intValue()) {
                continue;
            }
            BridgeForwardingTable parsedBridgeFT = m_bridgeFtMapUpdate.get(parsedbridgeid);
            if (parsedBridgeFT == null) {
                parsedBridgeFT = bridgeFtMapCalcul.get(parsedbridgeid);
            }
            
            BridgeSimpleConnection sp;
            try {
                sp = BridgeSimpleConnection.createAndRun(parsedBridgeFT,
                                                         postBridgeFT);
            } catch (BridgeTopologyException e) {
                LOG.warn("postprocess: bridge:[{}] <--> bridge:[{}] no topology found. {}, \n{}",
                         postbridgeid, parsedbridgeid, e.getMessage(),
                         e.printTopology());
                continue;
            }
            
            if (!parsedBridgeFT.getBridge().isRootBridge()
                    && !parsedBridgeFT.getRootPort().equals(sp.getFirstPort())) {
                if (postBridgeFT.getBridge().isNewTopology()) {
                    postBridgeFT.setRootPort(sp.getSecondBridgePort());
                }
                try {
                    down(parsedBridgeFT, postBridgeFT, sp, bridgeFtMapCalcul,
                         0);
                } catch (BridgeTopologyException e) {
                    LOG.warn("postprocess: bridge:[{}] <--> bridge:[{}] no topology found. {}, \n{}",
                             postbridgeid, parsedbridgeid, e.getMessage(),
                             e.printTopology());
                    continue;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("postprocess: bridge:[{}] <--> bridge:[{}] topology found. \n{}",
                              postbridgeid, parsedbridgeid);
                }
                return;
            }
        }
        try {
            down(rootBridgeFT, postBridgeFT, BridgeSimpleConnection.createAndRun(rootBridgeFT, postBridgeFT), bridgeFtMapCalcul,
                 0);
            return;
        } catch (BridgeTopologyException e) {
            LOG.warn("postprocess: bridge:[{}] <--> bridge:[{}] no topology found. {}, \n{}",
                     postbridgeid,rootBridgeFT.getNodeId(), e.getMessage(),
                     e.printTopology());
        }
        throw new BridgeTopologyException("postprocess: no connection found", postBridgeFT);
    }
    
    private void down(BridgeForwardingTable bridgeUpFT,  
            BridgeForwardingTable bridgeFT, BridgeSimpleConnection upsimpleconn, Map<Integer,BridgeForwardingTable> bridgeFtMapCalcul, Integer level) throws BridgeTopologyException {

        if (++level == BroadcastDomain.maxlevel) {
            throw new BridgeTopologyException(
                          "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], too many iteration");
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
                    BridgeSimpleConnection.createAndRun(curBridgeFT,
                                       bridgeFT);
            if (LOG.isDebugEnabled()) {
                LOG.debug("down: level: {}, bridge:[{}]. {}", 
                         level,
                         bridgeFT.getNodeId(),
                         simpleconn.printTopology());
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
    }
    
}

