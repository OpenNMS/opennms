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
                LOG.warn("getAllNodesWithUpdatedBFTOnDomain: node: [{}], no update bft for node [{}] on domain", getNodeId(), nodeid);
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

    private void verify(BroadcastDomain domain, Integer nodeid) throws BridgeTopologyException {
        if (domain.getBridge(nodeid) != null) {
            LOG.info("verify: node: [{}]. bridge: [{}]. Found on Shared Domain", 
                     getNodeId(),
                     nodeid);
            return;
        }
        BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(nodeid);
        if (olddomain != null) {
            m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, nodeid);
            LOG.info("verify: node: [{}]. bridge: [{}]. Removed from Old Domain", getNodeId());
        }
    }
    
    private BroadcastDomain find(Set<String> setA) throws BridgeTopologyException {

        BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(getNodeId());
        if (olddomain != null &&
                checkMacSets(setA, olddomain.getMacsOnDomain())) {
            LOG.info("find: node: [{}]. Same Domain", getNodeId());
            return olddomain;
        } 
        BroadcastDomain domain = findUsingRetainedSet(setA);
        if ( domain != null ) {
            if (olddomain != null) {
                m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, getNodeId());
                LOG.info("find: node: [{}]. Removed from Old Domain", getNodeId());
            }
            LOG.info("find: node: [{}] Moving to a new Domain", getNodeId());
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
    
    //FIXME check better fit
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
            LOG.info("run: node: [{}]. no updated bft. Return", getNodeId());
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
            synchronized (m_domain) {
                Bridge.create(m_domain, nodeid);
                try {
                    verify(m_domain, nodeid);
                } catch (BridgeTopologyException e) {
                    LOG.warn("run: node: [{}], bridge  node [{}] on domain. {}", 
                              getNodeId(), 
                              nodeid,
                              e.getMessage());
                }
            }
            m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,nodeid);
            sendStartEvent(nodeid);
        }
        
        m_notYetParsedBFTMap.put(getNodeId(), links);
        Bridge.create(m_domain, getNodeId());
        m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,getNodeId());
        sendStartEvent(getNodeId());

        Set<Integer> nodeswithupdatedbft= new HashSet<Integer>(m_notYetParsedBFTMap.keySet());

        synchronized (m_domain) {
            calculate();
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

        for (Integer curNode : nodeswithupdatedbft) {
            if (m_notYetParsedBFTMap.containsKey(curNode)) {
                LOG.error("run: node: [{}], bridge [{}], topology calcutation failed.", 
                          getNodeId(), 
                          curNode);
            } else {
                sendCompletedEvent(curNode);
            }
        }
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
            LOG.debug("getElectedRootBridge: node: [{}], bridge [{}]: max bft size \"{}\" in topology",
                      getNodeId(), bridge.getNodeId(),
                      m_notYetParsedBFTMap.get(bridgeid).size());
            if (size < m_notYetParsedBFTMap.get(bridgeid).size()) {
                elected = bridge;
                size = m_notYetParsedBFTMap.get(bridgeid).size();
            }
        }
        if (elected != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getElectedRootBridge: node: [{}], bridge [{}]: elected root with max bft size \"{}\" in topology",
                          getNodeId(), elected.getNodeId(), size);
            }
        } else {
            elected = m_domain.getBridges().iterator().next();
            if (LOG.isDebugEnabled()) {
        	LOG.debug("getElectedRootBridge: node: [{}], bridge [{}] elected first bridge in topology", 
                       getNodeId(),
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
                LOG.debug("electRootBridge: node: [{}], elected is [root] bridge: {}. no updated bft,",
                    getNodeId(), 
            		electedRoot.printTopology());
            }
            rootBft = m_domain.calculateRootBFT();
        } else if ( rootBft != null ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: node: [{}], elected is [root] bridge: [{}]. updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
            }
            m_domain.clearTopologyForBridge(electedRoot.getNodeId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: node: [{}], cleared topology for new elected [root]: ->\n{}, on domain: ->\n{}",
                      getNodeId(), 
                      electedRoot.printTopology(),
                       m_domain.printTopology());
            }
            if (m_domain.getSharedSegments().isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("electRootBridge: node: [{}], elected [root] is firstbridge: [{}]. Loading first level",
                       getNodeId(), 
                        electedRoot.getNodeId());
                }
                loadFirstLevelSharedSegment(m_domain,rootBft);
                electedRoot.setRootBridge();
           } else {
                goDown(
                  BridgeForwardingTable.create(
                       m_domain.getRootBridge(), 
                       m_domain.calculateRootBFT()),
                  BridgeForwardingTable.create(
                       electedRoot, 
                       rootBft),0);
                addForwarding(rootBft);
                m_domain.hierarchySetUp(electedRoot);
           }
        } else {
           LOG.debug("electRootBridge: node: [{}], elected [root] bridge: [{}], is new, without updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
           m_domain.hierarchySetUp(electedRoot);
           rootBft = m_domain.calculateRootBFT();
        }
        for (Integer xBridgeId: m_notYetParsedBFTMap.keySet()) {
            m_domain.clearTopologyForBridge(xBridgeId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("electRootBridge: node: [{}], Cleaned bridge: [{}] from domain: -> \n{}", 
                        getNodeId(),
                        xBridgeId,
                        m_domain.printTopology());
            }
        }

        return BridgeForwardingTable.create(electedRoot, rootBft);
    }
    
    protected  void calculate() {
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: node: [{}], topology calculation start: ->\n{}.", 
                 getNodeId(),
                 m_domain.printTopology());
        }

        BridgeForwardingTable rootft;
        try {
            rootft = electRootBridge();
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: node: [{}]. {}, topology:\n{}",
                      getNodeId(),
                      e.getMessage(),
                      e.printTopology(),
                      e);
            return;
        }
        
        Set<Integer> nodetobeparsed = new HashSet<Integer>(m_notYetParsedBFTMap.keySet());
        for (Integer xBridgeId: nodetobeparsed) {
            try {
                goDown(rootft, BridgeForwardingTable.create(m_domain.getBridge(xBridgeId), 
                                   new HashSet<BridgeForwardingTableEntry>(m_notYetParsedBFTMap.remove(xBridgeId))),0);
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: node: [{}]. {}, topology:\n{}", 
                          getNodeId(),
                          e.getMessage(),
                          e.printTopology(),
                          e);
                continue;
            }
        }
        m_domain.cleanForwarders();
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: node: [{}], topology calculation end: ->\n{}.", 
                 getNodeId(),
                 m_domain.printTopology());
        }
    }
     
    private void addForwarding(Set<BridgeForwardingTableEntry> bft) {
        for (BridgeForwardingTableEntry maclink: bft) {
            if (m_domain.getMacsOnDomain().contains(maclink.getMacAddress())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("addForwarding: node: [{}]. Skipping forwarding: {}",
                          getNodeId(), 
                          maclink.printTopology());
                }
                continue;                    
            }
            m_domain.addForwarding(maclink);
            if (LOG.isDebugEnabled()) {
                LOG.debug("addForwarding: node: [{}]. Adding forwarding: {}",
                      getNodeId(), 
                      maclink.printTopology());
            }
        }
    }

    private void loadFirstLevelSharedSegment(BroadcastDomain domain,Set<BridgeForwardingTableEntry> electedRootBFT) throws BridgeTopologyException {
        Map<BridgePort, Set<String>> rootleafs = new HashMap<BridgePort, Set<String>>();
        
        for (BridgeForwardingTableEntry link : electedRootBFT) {
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
            SharedSegment segment = SharedSegment.createAndAddToBroadcastDomain(domain,port,rootleafs.get(port));
            if (LOG.isDebugEnabled()) {
                LOG.debug("loadFirstLevelSharedSegment: node: [{}], add shared segment: ->\n{}",
            		 getNodeId(),
                     segment.printTopology());
            }
        }      
   }
    

    // here we assume that rbridge exists in topology
    // while xBridge is to be added
    private boolean goDown(BridgeForwardingTable root,  
            BridgeForwardingTable xBridge, Integer level) {
        
        if (++level == 30) {
            LOG.warn("goDown: node: [{}]: level: {}, bridge: [{}], too many iteration on topology exiting.....",
                        getNodeId(),
                        level,
                        xBridge.getNodeId());
            return false;
        }

        BridgeSimpleConnection rx = new BridgeSimpleConnection(root, xBridge);
        if (rx.findSimpleConnection()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("goDown: node: [{}], level {}, simple connection found: ->\n{}", 
                         getNodeId(),
                         level,
                         rx.printTopology());
            }
        } else {
            LOG.warn("goDown: node: [{}], level {}, cannot found simple connection for bridges: [{},{}]", 
                     getNodeId(),
                     level,
                     root.getNodeId(), 
                     xBridge.getNodeId());
            return false;
        }
        if (level == 1) {
            xBridge.setRootPort(rx.getSecondBridgePort().getBridgePort());
        }
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = m_domain.getSharedSegment(rx.getFirstBridgePort());
        if (topSegment == null) {
            LOG.warn("goDown: node: [{}], level: {}, {}. top segment not found.",
                        getNodeId(),
                        rx.getFirstBridgePort().printTopology());
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: node: [{}], level: {}, bridge: [{}] -> root segment: \n{} ",
                        getNodeId(),
                        level,
                        xBridge.getNodeId(),
                        topSegment.printTopology());
        }

        Set<Integer> portsAdded=new HashSet<Integer>();
        Set<String> macsOnSegment=rx.getSimpleConnectionMacs();
        Map<Integer,Set<BridgeForwardingTableEntry>> bftSets=new HashMap<Integer,Set<BridgeForwardingTableEntry>>();
        Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
        forwarders.addAll(rx.getFirstBridgeForwarders());
        forwarders.addAll(rx.getSecondBridgeForwarders());

        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            try {
                bftSets.put(yBridge.getNodeId(), m_domain.calculateBFT(yBridge));
            } catch (BridgeTopologyException e) {
                LOG.error("goDown: node: [{}]. level: {}. {} topology:\n{}", 
                          getNodeId(),
                          level,
                          e.getMessage(),e.printTopology(),e);
                return false;
            }
        }
        
        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            Integer yBridgeId = yBridge.getNodeId();
            // X is a leaf of top segment: of course
            if (yBridgeId.intValue() == topSegment.getDesignatedBridge().intValue()) {
                continue;
            } 
            Integer yrDesignatedPort = yBridge.getRootPort();
            LOG.debug("goDown: node: [{}], level: {}, bridge: [{}], bridge: [{}, designated port: {}]",
            		getNodeId(),
                     level,
                     xBridge.getNodeId(),
                     yBridgeId,
                     yrDesignatedPort);
            BridgeSimpleConnection yx;
            try {
                yx = new BridgeSimpleConnection(
                               BridgeForwardingTable.create(yBridge,
                                                            bftSets.get(yBridgeId)) ,
                               xBridge);
            } catch (BridgeTopologyException e) {
                LOG.error("goDown: node: [{}]. level: {}. {} topology:\n{}", 
                          getNodeId(),
                          level,
                          e.getMessage(),e.printTopology(),e);
                return false;
            }
            if (!yx.findSimpleConnection()) {;
                LOG.error("goDown: node: [{}]. level: {}, no simple connection:[{}<-->{}]", 
                          getNodeId(),
                          level, 
                          xBridge.getNodeId(),
                          yBridge.getNodeId());
                return false;
            }
            Integer  xyDesignatedPort = yx.getSecondBridgePort().getBridgePort();
            Integer  yxDesignatedPort = yx.getFirstBridgePort().getBridgePort();
            // if X is a leaf of Y then iterate
            if (xyDesignatedPort == rx.getSecondBridgePort().getBridgePort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.debug("goDown: node: [{}]: level: {}, bridge: [{}] is a leaf of bridge: [{}], going one level down",
                		getNodeId(),
                		level,xBridge.getNodeId(),yBridge.getNodeId());
                return goDown(yx.getFirst(), xBridge,level);
            }
            // Y is a leaf of X then 
            // remove Y from topSegment
            // Create shared Segment with X designated bridge 
            // or if exists then retain all common macs on domain
            // Assign Forwarders for Y
            // Clean also topSegment macs.
            if (yxDesignatedPort == yrDesignatedPort && xyDesignatedPort != rx.getSecondBridgePort().getBridgePort()) {
                //create a SharedSegment with root port
                LOG.info("goDown: node: [{}], level: {}, bridge: [{},designated port [{}]]: found level.", 
                          getNodeId(), 
                          level,
                          xBridge.getNodeId(),
                          xyDesignatedPort);
                LOG.debug("goDown: node: [{}], level: {}, bridge: [{},designated port [{}]]: is 'hierarchy up' for bridge: [{}].", 
                		getNodeId(), 
                		level,
                		xBridge.getNodeId(),
                		xyDesignatedPort,
                                yBridge.getNodeId());
                SharedSegment leafSegment = m_domain.getSharedSegment(xBridge.getNodeId(), xyDesignatedPort);
                if (leafSegment == null) {
                    try {
                        leafSegment = SharedSegment.createAndAddToBroadcastDomain(m_domain,
                                                                  yx.getSimpleConnectionPorts(),
                                                                  yx.getSimpleConnectionMacs(),
                                                                  xBridge.getNodeId());
                    } catch (BridgeTopologyException e) {
                        LOG.error("goDown: node: [{}]. level: {}. {} topology:\n{}", 
                                  getNodeId(),
                                  level,
                                  e.getMessage(),e.printTopology(),e);
                        return false;
                    }
                } else {
                    try {
                        leafSegment.retain(yx.getSimpleConnectionMacs(),yx.getFirstBridgePort());
                    } catch (BridgeTopologyException e) {
                        LOG.error("goDown: node: [{}]: level {}: bridge [{}]. Topology mismatch. {}:\n{}",
                                  getNodeId(), 
                                  level,
                                  xBridge.getNodeId(),
                                  e.getMessage(),
                                  e.printTopology(),
                                  e);
                         return false;
                    }
                }
                portsAdded.add(xyDesignatedPort);
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: node: [{}], level: {}, bridge [{}]. Remove bridge [{}] and macs {} from top segment.\n{}", 
                		getNodeId(), 
                         level,
                         xBridge.getNodeId(),
                         yBridge.getNodeId(),
                         topSegment.getMacsOnSegment(),topSegment.printTopology());
                }
                topSegment.getMacsOnSegment().clear();
                try {
                    topSegment.removeBridge(yBridgeId);
                } catch (BridgeTopologyException e) {
                    LOG.error("goDown: node: [{}]. level: {}. {} topology:\n{}", 
                              getNodeId(),
                              level,
                              e.getMessage(),e.printTopology(),e);
                    return false;
                }
            } else if (xyDesignatedPort != rx.getSecondBridgePort().getBridgePort() 
                    && yxDesignatedPort != yrDesignatedPort) {
                LOG.warn("goDown: node: [{}]: level {}: bridge [{}]. Topology mismatch. return",
                		getNodeId(), 
                		level,
                		xBridge.getNodeId());
                return false;
            } else {
                macsOnSegment.retainAll(yx.getSimpleConnectionMacs());                
            }
            forwarders.addAll(yx.getFirstBridgeForwarders());
            forwarders.addAll(yx.getSecondBridgeForwarders());
        }
        // if we are here is because X is NOT a leaf of any bridge found
        // on topSegment so X is connected to top Segment by it's root 
        // port or rx is a direct connection
        try {
            topSegment.assign(macsOnSegment,rx.getSecondBridgePort());
        } catch (BridgeTopologyException e) {
            LOG.error("goDown: node: [{}]: level {}: bridge [{}]. Topology mismatch. {}:\n{}",
                     getNodeId(), 
                     level,
                     xBridge.getNodeId(),
                     e.getMessage(),
                     e.printTopology(),
                     e);
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: node: [{}]: level: {}, {}, ->\nmacs{} assigned ->\n{}", 
                        getNodeId(), 
                        level,
                        rx.getSecondBridgePort().printTopology(),
                        macsOnSegment,
                        topSegment.printTopology());
        }
        for (BridgePort xbridgePort : rx.getSecondBridgeTroughSetBft().keySet()) {
            if (portsAdded.contains(xbridgePort.getBridgePort())) {
                continue;
            }
            SharedSegment xleafSegment;
            try {
                xleafSegment = SharedSegment.createAndAddToBroadcastDomain(m_domain, xbridgePort,
                                                               rx.getSecondBridgeTroughSetBft().get(xbridgePort));
            } catch (BridgeTopologyException e) {
                LOG.error("goDown: node: [{}]. level: {}. {} topology:\n{}", 
                          getNodeId(),
                          level,
                          e.getMessage(),e.printTopology(),e);
                return false;
            }  
            if (LOG.isDebugEnabled()) {
                LOG.debug("goDown: node: [{}]: level: {}, bridge: [{}]. Add shared segment: ->\n{}",
                          getNodeId(), 
                     level,
                     xBridge.getNodeId(),
                     xleafSegment.printTopology());
            }
        }
        addForwarding(forwarders);
        return true;
    }

}

