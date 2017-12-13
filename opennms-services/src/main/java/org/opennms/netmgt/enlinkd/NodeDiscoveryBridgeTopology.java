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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTable;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryBridgeTopology extends NodeDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridgeTopology.class);


    private static final int DOMAIN_MATCH_MIN_SIZE = 5;
    private static final float DOMAIN_MATCH_MIN_RATIO = 0.1f;

    Map<Integer,Set<BridgeForwardingTableEntry>> m_notYetParsedBFTMap;
    BroadcastDomain m_domain;
    
    public void setDomain(BroadcastDomain domain) {
        m_domain =domain;
    }
    
    public BroadcastDomain getDomain() {
        return m_domain;
    }
    
    public Map<Integer, Set<BridgeForwardingTableEntry>> getNotYetParsedBFTMap() {
        return m_notYetParsedBFTMap;
    }

    public void addUpdatedBFT(Integer bridge, Set<BridgeForwardingTableEntry> notYetParsedBFT) {
        if (m_notYetParsedBFTMap==null)
            m_notYetParsedBFTMap = new HashMap<Integer, Set<BridgeForwardingTableEntry>>();
        m_notYetParsedBFTMap.put(bridge, notYetParsedBFT);
    }

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
    }

    private Map<Integer, Set<BridgeForwardingTableEntry>> 
    getAllNodesWithUpdatedBFTOnDomain(Set<String>setA) 
            throws BridgeTopologyException {
        Map<Integer,Set<BridgeForwardingTableEntry>> nodeswithupdatedbftonbroadcastdomain= 
                new HashMap<Integer,Set<BridgeForwardingTableEntry>>();

        Map<Integer,Set<BridgeForwardingTableEntry>> nodeBftMap = m_linkd.getQueryManager().getUpdateBftMap();

        Set<Integer> nodes = new HashSet<Integer>();
        Set<Integer> delnodes = new HashSet<Integer>();
        synchronized (nodeBftMap) {
            for (Integer curNodeId: nodeBftMap.keySet()) {
                Set<String> setB = new HashSet<String>();
                for (BridgeForwardingTableEntry link: nodeBftMap.get(curNodeId)) {
                    setB.add(link.getMacAddress());
                }
                
                if (checkMacSets(setA, setB)) {
                    nodes.add(curNodeId);
                } else if (m_domain.getBridge(curNodeId) != null) {
                    delnodes.add(curNodeId);
                }
            }            
        }
        for (Integer nodeid : nodes) {
            Set<BridgeForwardingTableEntry> bft = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(nodeid);
            if (bft == null || bft.isEmpty()) {
                LOG.warn("getAllNodesWithUpdatedBFTOnDomain: node: [{}], no update bft for node [{}] on domain", 
                         getNodeId(), 
                         nodeid);
                continue;
            }
            nodeswithupdatedbftonbroadcastdomain.put(nodeid, bft);
            LOG.info("getAllNodesWithUpdatedBFTOnDomain: node: [{}], node: [{}] - common broadcast domain", 
                     getNodeId(), 
                     nodeid);
        }
        for (Integer nodeid : delnodes) {
            m_linkd.getQueryManager().reconcileTopologyForDeleteNode(m_domain, nodeid);
            LOG.info("getAllNodesWithUpdatedBFTOnDomain: node: [{}], node: [{}] with update bft removed from domain", 
                     getNodeId(), 
                     nodeid);
        }
        return nodeswithupdatedbftonbroadcastdomain;
    }
    
    private BroadcastDomain find(Set<String> setA) throws BridgeTopologyException {

        BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(getNodeId());
        if (olddomain != null &&
                checkMacSets(setA, olddomain.getMacsOnDomain())) {
            LOG.info("find: node: [{}]. Same Domain", 
                     getNodeId());
            return olddomain;
        } 
        BroadcastDomain domain = findUsingRetainedSet(setA);
        if ( domain != null ) {
            if (olddomain != null) {
                m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, getNodeId());
                LOG.info("find: node: [{}]. Removed from Old Domain", 
                         getNodeId());
            }
            LOG.info("find: node: [{}] Moving to a new Domain", 
                     getNodeId());
            return domain;
        }
        LOG.info("find: node: [{}] Creating a new Domain", getNodeId());
        domain = new BroadcastDomain();
        m_linkd.getQueryManager().save(domain);
        return domain;        
    }
    
    private BroadcastDomain findUsingRetainedSet(Set<String> incomingSet) {
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            synchronized (domain) {
                
                if (checkMacSets(incomingSet, domain.getMacsOnDomain())) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("findUsingRetainedSet: node: [{}], found:\n{}",
                                 getNodeId(), 
                              domain.printTopology());
                    }
                    return domain;
                }
            }
        }
        return null;
    }
    
    //TODO check better fit
    private boolean checkMacSets(Set<String> setA, Set<String> setB) {
        Set<String>retainedSet = new HashSet<String>(setB);
        retainedSet.retainAll(setA);
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkMacSets: node: [{}], retained Set {}, \n setA {} \n setB {}", 
                  getNodeId(),
                  retainedSet,
                  setA,
                  setB);
        }
        // should contain at list 5 or 10% of the all size
        if (retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
                || retainedSet.size() >= setA.size() * DOMAIN_MATCH_MIN_RATIO) {
            return true;
        }
        return false;
    }
        
    @Override
    public void run() {

    	Set<BridgeForwardingTableEntry> links =  m_linkd.
        		getQueryManager().
        		useBridgeTopologyUpdateBFT(
        				getNodeId());
    	
    	if (links == null || links.size() == 0) {
            LOG.info("run: node: [{}]. no updated bft. Return", 
                     getNodeId());
            return;
    	}
    	Date now = new Date();

    	Set<String> nodemacset = new HashSet<String>();
        for (BridgeForwardingTableEntry link : links) {
            nodemacset.add(link.getMacAddress());
        }            
        LOG.info("run: node: [{}]. macs:{}", 
                 getNodeId(), 
                 nodemacset);
    	
        LOG.info("run: node: [{}], getting broadcast domain. Start", getNodeId());
        try {
            m_domain = find(nodemacset);
        } catch (BridgeTopologyException e) {
            LOG.error("run: node: [{}], getting broadcast domain. Failed {}", getNodeId(),
                      e.getMessage());
            return;
        }
        LOG.info("run: node: [{}], getting broadcast domain. End", getNodeId());
                             
        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. Start", getNodeId());
        try {
            m_notYetParsedBFTMap = getAllNodesWithUpdatedBFTOnDomain(nodemacset);
        } catch (BridgeTopologyException e) {
            LOG.error("run: node: [{}], error on getting bft upadates. {}", 
                      getNodeId(), 
                      e.getMessage());
            return;
        }
        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. End", getNodeId());

        for (Integer nodeid : m_notYetParsedBFTMap.keySet()) {
            if (m_domain.getBridge(nodeid) != null) {
                LOG.info("run: node: [{}]. bridge: [{}]. Found on Shared Domain", 
                         getNodeId(),
                         nodeid);
                continue;
            }
            synchronized (m_domain) {
                Bridge.create(m_domain, nodeid);
            }
            BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(nodeid);
            if (olddomain != null) {
                synchronized (olddomain) {
                try {
                    m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, nodeid);
                    LOG.info("run: node: [{}]. bridge: [{}]. Removed from Old Domain", getNodeId());
                } catch (BridgeTopologyException e) {
                    LOG.warn("run: node: [{}], bridge  node [{}] on domain. {}", 
                              getNodeId(), 
                              nodeid,
                              e.getMessage());
                }
                }
            }
            m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,nodeid);
            sendStartEvent(nodeid);
        }
        
        m_notYetParsedBFTMap.put(getNodeId(), links);
        Bridge.create(m_domain, getNodeId());
        m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,getNodeId());
        sendStartEvent(getNodeId());

        synchronized (m_domain) {
            for (Integer bridgeid : calculate()) {
                sendCompletedEvent(bridgeid);
            }
            for (Integer bridgeid : m_notYetParsedBFTMap.keySet()) {
                LOG.error("run: node: [{}], bridge [{}], topology calcutation failed.", 
                          getNodeId(), 
                          bridgeid);
            }
        }

        LOG.info("run: node: [{}], saving Topology.", getNodeId());
        try {
            m_linkd.getQueryManager().store(m_domain, now);
        } catch (BridgeTopologyException e) {
            LOG.error("run: node: [{}], saving topology failed: {}. {}", 
                      getNodeId(), 
                      e.getMessage(),
                      e.printTopology());
            return;
        }
        LOG.info("run: node: [{}], saved Topology.", getNodeId());

    }
            
    @Override
    protected void runCollection() {
    }

    @Override
    public String getName() {
        return "NodeDiscoveryBridgeTopology";
    }

    private Bridge getElectedRootBridge() {
        // no spanning tree root?
        // why I'm here?
        // not root bridge defined (this mean no calculation yet done...
        // so checking the best into not parsed
        int size = 0;
        Bridge elected = null;
        for (Integer bridgeid : m_notYetParsedBFTMap.keySet()) {
            Bridge bridge = m_domain.getBridge(bridgeid);
            LOG.debug("getElectedRootBridge: bridge [{}]: max bft size \"{}\" in topology",
                      bridge.getNodeId(),
                      m_notYetParsedBFTMap.get(bridgeid).size());
            if (size < m_notYetParsedBFTMap.get(bridgeid).size()) {
                elected = bridge;
                size = m_notYetParsedBFTMap.get(bridgeid).size();
            }
        }
        if (elected != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getElectedRootBridge: bridge [{}]: elected root with max bft size \"{}\" in topology",
                          elected.getNodeId(), size);
            }
        } else {
            elected = m_domain.getBridges().iterator().next();
            if (LOG.isDebugEnabled()) {
        	LOG.debug("getElectedRootBridge: bridge [{}] elected first bridge in topology", 
                       elected.getNodeId());
            }
        }

        return elected;
        
    }
    
    private BridgeForwardingTable electRootBridge() throws BridgeTopologyException {
        Bridge electedRoot = m_domain.electRootBridge();
        Bridge rootBridge = m_domain.getRootBridge();
        if (electedRoot == null) {
            if (rootBridge != null) {
                electedRoot = rootBridge;
            } else {
                electedRoot = getElectedRootBridge();
            }
        }
        if (electedRoot.getNodeId() == null) {
            throw new BridgeTopologyException("elected Root bridge id cannot be null", electedRoot);
        }

        Set<BridgeForwardingTableEntry> rootBft = 
                m_notYetParsedBFTMap.remove(electedRoot.getNodeId());
        
        if (rootBridge != null && rootBridge.getNodeId() == electedRoot.getNodeId() && rootBft == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: elected is [root] bridge: {}. no updated bft,",
            		electedRoot.printTopology());
            }
            rootBft = m_domain.calculateRootBFT();
        } else if ( rootBft != null ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: elected is [root] bridge: [{}]. updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
            }
            m_domain.clearTopologyForBridge(electedRoot.getNodeId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: cleared topology for new elected [root]: ->\n{}, on domain: ->\n{}",
                      electedRoot.printTopology(),
                       m_domain.printTopology());
            }
            if (m_domain.getSharedSegments().isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("electRootBridge: elected [root] is firstbridge: [{}]. Loading first level",
                        electedRoot.getNodeId());
                }
                Map<BridgePort, Set<String>> rootleafs = new HashMap<BridgePort, Set<String>>();
                
                for (BridgeForwardingTableEntry link : rootBft) {
                    if (link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                        continue;
                    }
                    if (!rootleafs.containsKey(BridgePort.getFromBridgeForwardingTableEntry(link))) {
                        rootleafs.put(BridgePort.getFromBridgeForwardingTableEntry(link),
                                      new HashSet<String>());
                    }
                    rootleafs.get(BridgePort.getFromBridgeForwardingTableEntry(link)).add(link.getMacAddress());
                }
                
                for (BridgePort port : rootleafs.keySet()) {
                    SharedSegment segment = SharedSegment.createAndAddToBroadcastDomain(m_domain,port,rootleafs.get(port));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("electRootBridge: add shared segment: ->\n{}",
                             segment.printTopology());
                    }
                }      
                electedRoot.setRootBridge();
           } else {
                goDown(
                  BridgeForwardingTable.create(
                       m_domain.getRootBridge(), 
                       m_domain.calculateRootBFT()),
                  BridgeForwardingTable.create(
                       electedRoot, 
                       rootBft),0);
                m_domain.hierarchySetUp(electedRoot);
           }
        } else {
           LOG.debug("electRootBridge: elected [root] bridge: [{}], is new, without updated bft",
                     electedRoot.getNodeId());
           m_domain.hierarchySetUp(electedRoot);
           rootBft = m_domain.calculateRootBFT();
        }
        for (Integer bridgeId: m_notYetParsedBFTMap.keySet()) {
            m_domain.clearTopologyForBridge(bridgeId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: cleaned topology for bridge: [{}] -> \n{}", 
                        bridgeId,
                        m_domain.printTopology());
            }
        }

        return BridgeForwardingTable.create(electedRoot, rootBft);
    }
    
    protected  Set<Integer> calculate() {
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: topology calculation start: ->\n{}.", 
                 m_domain.printTopology());
        }

        Set<Integer> parsed=new HashSet<Integer>();
        BridgeForwardingTable rootft;
        try {
            rootft = electRootBridge();
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: {}, topology:\n{}",
                      e.getMessage(),
                      e.printTopology(),
                      e);
            return parsed;
        }
        
        Set<Integer> nodetobeparsed = new HashSet<Integer>(m_notYetParsedBFTMap.keySet());
        for (Integer xBridgeId: nodetobeparsed) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("calculate: bridge[{}] find topology", 
                         xBridgeId);
                }
                goDown(rootft, BridgeForwardingTable.create(m_domain.getBridge(xBridgeId), 
                                   new HashSet<BridgeForwardingTableEntry>(m_notYetParsedBFTMap.remove(xBridgeId))),0);
                parsed.add(xBridgeId);
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: {}, topology:\n{}", 
                          e.getMessage(),
                          e.printTopology(),
                          e);
                continue;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: topology before cleaning forwarders: ->\n{}.", 
                 m_domain.printTopology());
        }
        m_domain.cleanForwarders();
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: topology calculation end: ->\n{}.", 
                 m_domain.printTopology());
        }
        return parsed;
    }
     
    private void addForwarding(Set<BridgeForwardingTableEntry> bft) {
        for (BridgeForwardingTableEntry maclink: bft) {
            if (m_domain.getMacsOnDomain().contains(maclink.getMacAddress())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("addForwarding: Skipping forwarding: ->\n{}",
                          maclink.printTopology());
                }
                continue;                    
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("addForwarding: Adding forwarding: ->\n{}",
                      maclink.printTopology());
            }
            m_domain.addForwarding(maclink);
        }
    }
    
    private boolean goDown(BridgeForwardingTable bridgeUpFT,  
            BridgeForwardingTable bridgeFT, Integer level) {
        
        BridgeSimpleConnection bridgeUpBridgeSC = 
                new BridgeSimpleConnection(bridgeUpFT, 
                                             bridgeFT);
        if (bridgeUpBridgeSC.findSimpleConnection()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("goDown: level: 1, bridge:[{}]. simple connection found: ->\n{}", 
                         bridgeFT.getNodeId(),
                         bridgeUpBridgeSC.printTopology());
            }
        } else {
            LOG.warn("goDown: level: 1, bridge:[{}]. cannot found simple connection for bridges: [{},{}]", 
                     bridgeFT.getNodeId(),
                     bridgeUpFT.getNodeId(), 
                     bridgeFT.getNodeId());
            return false;
        }
        bridgeFT.setRootPort(bridgeUpBridgeSC.getSecondBridgePort().getBridgePort());
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: level: 1, bridge:[{}]. set root port:[{}]", 
                     bridgeFT.getNodeId(),
                     bridgeFT.getBridge().getRootPort());
        }
    
        return goDown(bridgeUpBridgeSC, level);
    }        
    
    private boolean goDown(BridgeSimpleConnection bridgeUpBridgeSC, Integer level) {

        if (++level == 30) {
            LOG.warn("goDown: level: {}, bridge:[{}], too many iteration on topology exiting.....",
                        level,
                        bridgeUpBridgeSC.getSecond().getNodeId());
            return false;
        }

        
        SharedSegment upSegment = m_domain.getSharedSegment(bridgeUpBridgeSC.getFirstBridgePort());
        if (upSegment == null) {
            LOG.warn("goDown: level: {}, bridge:[{}]. up segment not found.",
                         level,
                         bridgeUpBridgeSC.getSecond().getNodeId());
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: level: {}, bridge:[{}]. up segment -> \n{} ",
                        level,
                        bridgeUpBridgeSC.getSecond().getNodeId(),
                        upSegment.printTopology());
        }

        Set<BridgePort> bridgeBridgePortsParsed=new HashSet<BridgePort>();
        //Main Loop on the bridge found on up shared segment
        BridgeSimpleConnection bridgeMaybeDownSC = null;
        for (Bridge curbridgeOnUpSegment: m_domain.getBridgeOnSharedSegment(upSegment)) {
            
            // Not considering upbridgeUpBridgeSC.getSecond(): of course
            if (curbridgeOnUpSegment.getNodeId().intValue() == bridgeUpBridgeSC.getFirst().getNodeId().intValue()) {
                continue;
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("goDown: level: {}, bridge: [{}]. ->\n up segment...parsing {}",
                     level,
                     bridgeUpBridgeSC.getSecond().getNodeId(),
                     curbridgeOnUpSegment.printTopology());
            }
            BridgeSimpleConnection curbridgeUpSegmentBridgeSC;
            try {
                curbridgeUpSegmentBridgeSC = new BridgeSimpleConnection(
                               BridgeForwardingTable.create(curbridgeOnUpSegment,
                                                            m_domain.calculateBFT(curbridgeOnUpSegment)) ,
                               bridgeUpBridgeSC.getSecond());
            } catch (BridgeTopologyException e) {
                LOG.error("goDown: level: {}. {} topology:\n{}", 
                          level,
                          e.getMessage(),e.printTopology(),e);
                return false;
            }
            if (!curbridgeUpSegmentBridgeSC.findSimpleConnection()) {;
                LOG.error("goDown: level: {}, no simple connection:[{}<-->{}]", 
                          level, 
                          bridgeUpBridgeSC.getSecond().getNodeId(),
                          curbridgeOnUpSegment.getNodeId());
                return false;
            }
            if (curbridgeUpSegmentBridgeSC.getSecondBridgePort().getBridgePort() != bridgeUpBridgeSC.getSecondBridgePort().getBridgePort() 
                    && curbridgeUpSegmentBridgeSC.getFirstBridgePort().getBridgePort() != curbridgeOnUpSegment.getRootPort()) {
                LOG.warn("goDown: level {}: bridge [{}]. Topology mismatch. return",
                                level,
                                bridgeUpBridgeSC.getSecond().getNodeId());
                return false;
            }

            // bridge is a leaf of curbridgeOnUpSegment
            if (curbridgeUpSegmentBridgeSC.getSecondBridgePort().getBridgePort() == 
                    bridgeUpBridgeSC.getSecondBridgePort().getBridgePort()
                    && curbridgeUpSegmentBridgeSC.getFirstBridgePort().getBridgePort() != curbridgeOnUpSegment.getRootPort()) {
                if (bridgeMaybeDownSC != null) {
                    LOG.warn("goDown: level {}: bridge [{}] cannot be leaf of two. Topology mismatch. return",
                             level,
                             bridgeUpBridgeSC.getSecond().getNodeId());
                    return false;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: level: {}, bridge: [{}, designated port [{}]]: is 'down' in hierarchy for bridge: [{}].", 
                                level,
                                bridgeUpBridgeSC.getSecond().getNodeId(),
                                curbridgeUpSegmentBridgeSC.getSecondBridgePort().getBridgePort(),
                                curbridgeOnUpSegment.getNodeId());
                }
                bridgeMaybeDownSC = curbridgeUpSegmentBridgeSC;
                continue;
            }

            addForwarding(SharedSegment.mergeAndGetForwarders(upSegment,
                                                              curbridgeUpSegmentBridgeSC.getSimpleConnectionMacs(), 
                                                              curbridgeUpSegmentBridgeSC.getSimpleConnectionPorts()));
            addForwarding(curbridgeUpSegmentBridgeSC.getFirstBridgeForwarders());
            addForwarding(curbridgeUpSegmentBridgeSC.getSecondBridgeForwarders());

            if (LOG.isDebugEnabled()) {
                LOG.debug("goDown: level: {}, {}, merged ->\n{}", 
                            level,
                            curbridgeUpSegmentBridgeSC.getSecondBridgePort().printTopology(),
                            upSegment.printTopology());
            }
            
            // curbridgeOnUpSegment is a leaf of bridge
            if (curbridgeUpSegmentBridgeSC.getFirstBridgePort().getBridgePort() == 
                    curbridgeOnUpSegment.getRootPort() 
                    && curbridgeUpSegmentBridgeSC.getSecondBridgePort().getBridgePort() != 
                    bridgeUpBridgeSC.getSecondBridgePort().getBridgePort()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: level: {}, bridge: [{}, designated port [{}]]: is 'up' in hierarchy for bridge: [{}].", 
                		level,
                		bridgeUpBridgeSC.getSecond().getNodeId(),
                		curbridgeUpSegmentBridgeSC.getSecondBridgePort().getBridgePort(),
                                curbridgeOnUpSegment.getNodeId());
                }
                
                if (bridgeBridgePortsParsed.add(curbridgeUpSegmentBridgeSC.getSecondBridgePort())) {
                    SharedSegment.createAndAddToBroadcastDomain(m_domain,
                                                                curbridgeUpSegmentBridgeSC.getSimpleConnectionPorts(),
                                                                curbridgeUpSegmentBridgeSC.getSimpleConnectionMacs(),
                                                                bridgeUpBridgeSC.getSecond().getNodeId());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("goDown: level: {}, {}. created new -> \n{}", 
                             level,
                             bridgeUpBridgeSC.getSecond().getNodeId(),
                             curbridgeUpSegmentBridgeSC.getSecondBridgePort().printTopology(),
                             m_domain.getSharedSegment(curbridgeUpSegmentBridgeSC.getSecondBridgePort()).printTopology());
                    }
                } else {
                   addForwarding(SharedSegment.mergeAndGetForwarders(
                                 m_domain.getSharedSegment(curbridgeUpSegmentBridgeSC.getSecondBridgePort()), 
                                  bridgeUpBridgeSC.getSimpleConnectionMacs(),
                                  bridgeUpBridgeSC.getSimpleConnectionPorts()));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("goDown: level: {}, {}. merged -> \n{}", 
                             level,
                             bridgeUpBridgeSC.getSecond().getNodeId(),
                             curbridgeUpSegmentBridgeSC.getSecondBridgePort().printTopology(),
                             m_domain.getSharedSegment(curbridgeUpSegmentBridgeSC.getSecondBridgePort()).printTopology());
                    }
                }
                SharedSegment.remove(upSegment, 
                                     curbridgeUpSegmentBridgeSC.getSimpleConnectionMacs(), 
                                     curbridgeUpSegmentBridgeSC.getSimpleConnectionPorts());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: level: {}, bridge [{}]. Removed bridge [{}] and macs {} from up segment -> \n{}", 
                         level,
                         bridgeUpBridgeSC.getSecond().getNodeId(),
                         curbridgeOnUpSegment.getNodeId(),
                         curbridgeUpSegmentBridgeSC.getSimpleConnectionMacs(),
                         upSegment.printTopology());
                }
            } 
        } //end of loop on up segment bridges
        
        if (bridgeMaybeDownSC != null) {
            SharedSegment.remove(upSegment, 
                                 bridgeMaybeDownSC.getSimpleConnectionMacs(),
                                 bridgeMaybeDownSC.getSimpleConnectionPorts());
            return goDown(bridgeMaybeDownSC, level);
        }
        addForwarding(SharedSegment.mergeAndGetForwarders(upSegment,
                                                          bridgeUpBridgeSC.getSimpleConnectionMacs(), 
                                                          bridgeUpBridgeSC.getSimpleConnectionPorts()));
        addForwarding(bridgeUpBridgeSC.getSecondBridgeForwarders());
        addForwarding(bridgeUpBridgeSC.getFirstBridgeForwarders());        
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: level: {}, {}, last merged ->\n{}", 
                        level,
                        bridgeUpBridgeSC.getSecondBridgePort().printTopology(),
                        upSegment.printTopology());
        }



        Map<BridgePort,Set<String>> secondbridgeTroughSet = bridgeUpBridgeSC.getSecondBridgeTroughSetBft();
        for (BridgePort xbridgePort : secondbridgeTroughSet.keySet()) {
            if (bridgeBridgePortsParsed.contains(xbridgePort)) {
                continue;
            }
            SharedSegment xleafSegment;
            try {
                xleafSegment = SharedSegment.createAndAddToBroadcastDomain(m_domain, xbridgePort,
                                                                           secondbridgeTroughSet.get(xbridgePort));
            } catch (BridgeTopologyException e) {
                LOG.error("goDown: level: {}. {} topology:\n{}", 
                          level,
                          e.getMessage(),
                          e.printTopology(),
                          e);
                return false;
            }  
            if (LOG.isDebugEnabled()) {
                LOG.debug("goDown: level: {}, bridge: [{}]. Add shared segment: ->\n{}",
                     level,
                     bridgeUpBridgeSC.getSecond().getNodeId(),
                     xleafSegment.printTopology());
            }
        }
        return true;
    }

}

