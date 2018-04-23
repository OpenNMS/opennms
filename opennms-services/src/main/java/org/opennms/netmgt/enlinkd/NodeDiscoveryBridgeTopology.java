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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTable;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.opennms.netmgt.model.topology.BroadcastDomain.calculateBFT;
import static org.opennms.netmgt.model.topology.BroadcastDomain.calculateRootBFT;
import static org.opennms.netmgt.model.topology.BroadcastDomain.hierarchySetUp;
import static org.opennms.netmgt.model.topology.BroadcastDomain.clearTopologyForBridge;
import static org.opennms.netmgt.model.topology.BroadcastDomain.electRootBridge;

public class NodeDiscoveryBridgeTopology extends NodeDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridgeTopology.class);

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
    getUpdated(Set<String>setA) 
            throws BridgeTopologyException {
        Map<Integer,Set<BridgeForwardingTableEntry>> nodeswithupdatedbftonbroadcastdomain= 
                new HashMap<Integer,Set<BridgeForwardingTableEntry>>();

        Map<Integer,Set<BridgeForwardingTableEntry>> nodeBftMap = m_linkd.getQueryManager().getUpdateBftMap();

        Set<Integer> nodes = new HashSet<Integer>();
        Set<Integer> delnodes = new HashSet<Integer>();
        synchronized (nodeBftMap) {
            for (Integer curNodeId: nodeBftMap.keySet()) {
                if (curNodeId.intValue() == getNodeId()) {
                    continue;
                }
                Set<String> setB = new HashSet<String>();
                for (BridgeForwardingTableEntry link: nodeBftMap.get(curNodeId)) {
                    setB.add(link.getMacAddress());
                }
                LOG.debug("getUpdated: node:[{}], Node:[{}], macs {}", getNodeId(), curNodeId, setB);
                
                if (BroadcastDomain.checkMacSets(setA, setB)) {
                    nodes.add(curNodeId);
                    LOG.info("getUpdated: node: [{}], node [{}] with updated bft is on domain", 
                             getNodeId(), 
                             curNodeId);
                } else if (m_domain.getBridge(curNodeId) != null) {
                    delnodes.add(curNodeId);
                    LOG.info("getUpdated: node: [{}], node [{}] with updated bft must be removed from domain", 
                             getNodeId(), 
                             curNodeId);
                }
            }            
        }
        for (Integer nodeid : nodes) {
            Set<BridgeForwardingTableEntry> bft = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(nodeid);
            if (bft == null || bft.isEmpty()) {
                LOG.warn("getUpdated: node: [{}], no update bft for node [{}] on domain", 
                         getNodeId(), 
                         nodeid);
                continue;
            }
            nodeswithupdatedbftonbroadcastdomain.put(nodeid, bft);
            if (m_domain.getBridgeNodesOnDomain().contains(nodeid)) {
                continue;
            }
            BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(nodeid);
            if (olddomain != null) {
                synchronized (olddomain) {
                    m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, nodeid);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("getUpdated: node: [{}]. bridge: [{}]. Removed from Old Domain {} ", 
                                 getNodeId(),
                                 olddomain.printTopology());
                    }
                }
            }

        }
        for (Integer nodeid : delnodes) {
            m_linkd.getQueryManager().reconcileTopologyForDeleteNode(m_domain, nodeid);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getUpdated: node: [{}], node: [{}] with update bft removed from domain {}", 
                     getNodeId(), 
                     nodeid, m_domain.printTopology());
            }
        }
        return nodeswithupdatedbftonbroadcastdomain;
    }
    
    private BroadcastDomain find(Set<String> setA) throws BridgeTopologyException {
        BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(getNodeId());
        if (olddomain != null &&
                BroadcastDomain.checkMacSets(setA, olddomain.getMacsOnDomain())) {
            LOG.info("find: node: [{}]. node found on previuos Domain", 
                     getNodeId());
            return olddomain;
        } 
        BroadcastDomain domain = null;
        for (BroadcastDomain curBDomain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            if (curBDomain.getBridgeNodesOnDomain().contains(getNodeId())) {
                continue;
            }
            synchronized (curBDomain) {
                if (BroadcastDomain.checkMacSets(setA, curBDomain.getMacsOnDomain())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("find: node: [{}], found:\n{}",
                                 getNodeId(), 
                              curBDomain.printTopology());
                    }
                    domain=curBDomain;
                    break;
                }
            }
        }

        if ( domain != null ) {
            if (olddomain != null) {
                m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, getNodeId());
                if (LOG.isInfoEnabled()) {
                    LOG.info("find: node: [{}]. Removed from Old Domain {} ", 
                         getNodeId(), olddomain.printTopology());
                }
            }
            LOG.info("find: node: [{}], moving from old to new Domain", 
                     getNodeId());
            return domain;
        }
        LOG.info("find: node: [{}]. No domain found, creating new Domain", getNodeId());
        domain = new BroadcastDomain();
        m_linkd.getQueryManager().save(domain);
        return domain;        
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
            m_notYetParsedBFTMap = getUpdated(nodemacset);
        } catch (BridgeTopologyException e) {
            LOG.error("run: node: [{}], error on getting bft upadates. {}", 
                      getNodeId(), 
                      e.getMessage());
            return;
        }
        m_notYetParsedBFTMap.put(getNodeId(), links);
        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. End", getNodeId());

        for (Integer nodeid : m_notYetParsedBFTMap.keySet()) {
            synchronized (m_domain) {
                if (m_domain.getBridge(nodeid) == null) {
                    Bridge.create(m_domain, nodeid);
                }
                m_linkd.getQueryManager().updateBridgeOnDomain(m_domain,nodeid);
            }
            sendStartEvent(nodeid);
        }
        
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

    private Bridge getRootBridge() {
        // no spanning tree root?
        // why I'm here?
        // not root bridge defined (this mean no calculation yet done...
        // so checking the best into not parsed
        int size = 0;
        Bridge elected = null;
        for (Integer bridgeid : m_notYetParsedBFTMap.keySet()) {
            Bridge bridge = m_domain.getBridge(bridgeid);
            LOG.debug("getRootBridge: bridge [{}]: max bft size \"{}\" in topology",
                      bridge.getNodeId(),
                      m_notYetParsedBFTMap.get(bridgeid).size());
            if (size < m_notYetParsedBFTMap.get(bridgeid).size()) {
                elected = bridge;
                size = m_notYetParsedBFTMap.get(bridgeid).size();
            }
        }
        if (elected != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRootBridge: bridge [{}]: elected root with max bft size \"{}\" in topology",
                          elected.getNodeId(), size);
            }
        } else {
            elected = m_domain.getBridges().iterator().next();
            if (LOG.isDebugEnabled()) {
        	LOG.debug("getRootBridge: bridge [{}] elected first bridge in topology", 
                       elected.getNodeId());
            }
        }

        return elected;
        
    }

    private BridgeForwardingTable calculateRootBridge() throws BridgeTopologyException {
        Bridge electedRoot = electRootBridge(m_domain);
        Bridge rootBridge = m_domain.getRootBridge();
        if (electedRoot == null) {
            if (rootBridge != null) {
                electedRoot = rootBridge;
            } else {
                electedRoot = getRootBridge();
            }
        }
        if (electedRoot.getNodeId() == null) {
            throw new BridgeTopologyException("elected Root bridge id cannot be null", electedRoot);
        }

        Set<BridgeForwardingTableEntry> rootBft = 
                m_notYetParsedBFTMap.remove(electedRoot.getNodeId());
        
        if (rootBridge != null && rootBridge.getNodeId() == electedRoot.getNodeId() && rootBft == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: bridge:[{}] -> elected is [root] with no updated bft,",
            		electedRoot.printTopology());
            }
            rootBft = calculateRootBFT(m_domain);
        } else if ( rootBft != null ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: bridge:[{}] elected has updated bft",
                     electedRoot.getNodeId());
            }
            if (!electedRoot.isNewTopology()) {
                clearTopologyForBridge(m_domain,electedRoot.getNodeId());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: {} cleared topology: ->\n{}",
                          electedRoot.printTopology(),
                           m_domain.printTopology());
                }
            }
            if (m_domain.getSharedSegments().isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: bridge:[{}] elected [root] is first.",
                        electedRoot.getNodeId());
                }
                goDown(BridgeForwardingTableEntry.getThroughSet(rootBft, 
                                                                new HashSet<BridgePort>()),0);
                electedRoot.setRootBridge();
           } else {
                goDown(
                  BridgeForwardingTable.create(
                       m_domain.getRootBridge(), 
                       calculateRootBFT(m_domain)),
                  BridgeForwardingTable.create(
                       electedRoot, 
                       rootBft));
                hierarchySetUp(m_domain,electedRoot);
           }
        } else {
           LOG.debug("calculate: bridge:[{}] elected is new [root], no updated bft",
                     electedRoot.getNodeId());
           hierarchySetUp(m_domain,electedRoot);
           rootBft = calculateRootBFT(m_domain);
        }
        for (Integer bridgeId: m_notYetParsedBFTMap.keySet()) {
            if (m_domain.getBridge(bridgeId).isNewTopology()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: bridge: [{}] is 'new'. skip clean topology   ", 
                            bridgeId);
                }
                continue;
            }
            clearTopologyForBridge(m_domain,bridgeId);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: cleaned topology for bridge: [{}] -> \n{}", 
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
            rootft = calculateRootBridge();
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: {}, topology:\n{}",
                      e.getMessage(),
                      e.printTopology(),
                      e);
            return parsed;
        }
        
        Set<Integer> nodetobeparsed = new HashSet<Integer>(m_notYetParsedBFTMap.keySet());
        for (Integer xBridgeId: nodetobeparsed) {
            if (LOG.isInfoEnabled()) {
                LOG.info("calculate: bridge[{}] find topology", 
                     xBridgeId);
            }
            BridgeForwardingTable bridgeft = null;
            try {
                bridgeft = BridgeForwardingTable.create(m_domain.getBridge(xBridgeId), 
                    new HashSet<BridgeForwardingTableEntry>(m_notYetParsedBFTMap.remove(xBridgeId)));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: {}, topology:\n{}", 
                          e.getMessage(),
                          e.printTopology(),
                          e);
                continue;
            }
            if (goDown(rootft, bridgeft)) {
                parsed.add(xBridgeId);
            }
        }  
        
        m_domain.cleanForwarders();
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: topology calculation end: ->\n{}.", 
                 m_domain.printTopology());
        }
        return parsed;
    }
         
    private boolean goDown(BridgeForwardingTable bridgeUpFT,  
            BridgeForwardingTable bridgeFT) {
        
        BridgeSimpleConnection upsimpleconn = 
                new BridgeSimpleConnection(bridgeUpFT, 
                                             bridgeFT);
        if (upsimpleconn.findSimpleConnection()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("goDown: level: 1, bridge:[{}]. simple connection found: ->\n{}", 
                         bridgeFT.getNodeId(),
                         upsimpleconn.printTopology());
            }
        } else {
            LOG.warn("goDown: level: 1, bridge:[{}]. cannot found simple connection for bridges: [{},{}]", 
                     bridgeFT.getNodeId(),
                     bridgeUpFT.getNodeId(), 
                     bridgeFT.getNodeId());
            return false;
        }
        bridgeFT.setRootPort(upsimpleconn.getSecondBridgePort().getBridgePort());
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: level: 1, bridge:[{}]. set root port:[{}]", 
                     bridgeFT.getNodeId(),
                     bridgeFT.getBridge().getRootPort());
        }
    
        return goDown(upsimpleconn, new HashSet<String>(), 0);
    }        
    
    private boolean goDown(BridgeSimpleConnection upsimpleconn, Set<String> throughSetMac, Integer level) {

        if (++level == 30) {
            LOG.warn("goDown: level: {}, bridge:[{}], too many iteration on topology exiting.....",
                        level,
                        upsimpleconn.getSecond().getNodeId());
            return false;
        }
        
        SharedSegment upSegment = m_domain.getSharedSegment(upsimpleconn.getFirstBridgePort());
        if (upSegment == null) {
            LOG.warn("goDown: level: {}, bridge:[{}]. up segment not found.",
                         level,
                         upsimpleconn.getSecond().getNodeId());
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: level: {}, bridge:[{}]. up segment -> \n{} ",
                        level,
                        upsimpleconn.getSecond().getNodeId(),
                        upSegment.printTopology());
        }
        
        for (Set<String> excluded: upsimpleconn.getExcluded().values()) {
            throughSetMac.addAll(excluded);
        }

        Map<BridgePort, List<BridgeSimpleConnection>> splitted = new HashMap<BridgePort, List<BridgeSimpleConnection>>();
        List<BridgeSimpleConnection> merged = new ArrayList<BridgeSimpleConnection>();
        // Main Loop on the bridge found on up shared segment
        BridgeSimpleConnection bridgeMaybeDownSC = null;
        boolean levelfound = false;
        for (Bridge curbridge : m_domain.getBridgeOnSharedSegment(upSegment)) {
            // not parsing designated bridge of upasegment
            if (curbridge.getNodeId().intValue() == upSegment.getDesignatedBridge().intValue()) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("goDown: level: {}, bridge: [{}]. ->\n up segment...parsing {}",
                          level, upsimpleconn.getSecond().getNodeId(),
                          curbridge.printTopology());
            }
            BridgeForwardingTable curBridgeFT = null;
            try {
                curBridgeFT = BridgeForwardingTable.create(curbridge,calculateBFT(m_domain,curbridge));
            } catch (BridgeTopologyException e) {
                LOG.warn("goDown: level: {}. {} topology:\n{}", level,
                         e.getMessage(), e.printTopology(), e);
                return false;
            }
            BridgeSimpleConnection bridgesimpleconn = 
                    new BridgeSimpleConnection(curBridgeFT,
                                       upsimpleconn.getSecond());
            if (bridgesimpleconn.findSimpleConnection()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("goDown: level: {}, bridge:[{}]. simple connection found: ->\n{}", 
                             level,
                             bridgesimpleconn.getSecond().getNodeId(),
                             bridgesimpleconn.printTopology());
                }
            } else {
                LOG.warn("goDown: level: {}, no simple connection:[{}<-->{}]",
                         level, upsimpleconn.getSecond().getNodeId(),
                         curbridge.getNodeId());
                return false;
            }
            if (bridgesimpleconn.getSecondBridgePort().getBridgePort() != 
                    upsimpleconn.getSecondBridgePort().getBridgePort()
                && bridgesimpleconn.getFirstBridgePort().getBridgePort() != 
                    curbridge.getRootPort()) {
                LOG.warn("goDown: level {}: bridge [{}]. Topology mismatch. return",
                         level, upsimpleconn.getSecond().getNodeId());
                return false;
            }

            // bridge is a leaf of curbridgeOnUpSegment
            if (bridgesimpleconn.getSecondBridgePort().getBridgePort() == upsimpleconn.getSecond().getBridge().getRootPort()
                    && bridgesimpleconn.getFirstBridgePort().getBridgePort() != bridgesimpleconn.getFirst().getBridge().getRootPort()) {
                if (bridgeMaybeDownSC != null) {
                    LOG.warn("goDown: level {}: bridge [{}] cannot be leaf of two. Topology mismatch. return",
                             level, upsimpleconn.getSecond().getNodeId());
                    return false;
                }
                if (levelfound) {
                    LOG.warn("goDown: level {}: bridge [{}] cannot be leaf while is a leaf of another. Topology mismatch. return",
                             level, upsimpleconn.getSecond().getNodeId());
                    return false;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: level: {}, bridge: [{}, designated port [{}]]: is 'down' in hierarchy for bridge: [{}].",
                              level,
                              upsimpleconn.getSecond().getNodeId(),
                              bridgesimpleconn.getSecondBridgePort().getBridgePort(),
                              curbridge.getNodeId());
                }
                bridgeMaybeDownSC = bridgesimpleconn;
                continue;
            }
            
            // curbridgeOnUpSegment is a leaf of bridge
            if (bridgesimpleconn.getFirstBridgePort().getBridgePort() == bridgesimpleconn.getFirst().getBridge().getRootPort()
                    && bridgesimpleconn.getSecondBridgePort().getBridgePort() != upsimpleconn.getSecond().getBridge().getRootPort()) {
                if (bridgeMaybeDownSC != null) {
                    LOG.warn("goDown: level {}: bridge [{}] cannot be leaf while is a leaf of another. Topology mismatch. return",
                             level, upsimpleconn.getSecond().getNodeId());
                    return false;
                }
                levelfound = true;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: level: {}, bridge: [{}, designated port [{}]]: is 'up' in hierarchy for bridge: [{}].",
                              level,
                              upsimpleconn.getSecond().getNodeId(),
                              bridgesimpleconn.getSecondBridgePort().getBridgePort(),
                              curbridge.getNodeId());
                }
                if (!splitted.containsKey(bridgesimpleconn.getSecondBridgePort())) {
                    splitted.put(bridgesimpleconn.getSecondBridgePort(),
                                 new ArrayList<BridgeSimpleConnection>());
                }
                splitted.get(bridgesimpleconn.getSecondBridgePort()).add(bridgesimpleconn);
                continue;
            }

            // both on upSegment
            for (Set<String> excluded: bridgesimpleconn.getExcluded().values()) {
                throughSetMac.addAll(excluded);
            }
            merged.add(bridgesimpleconn);
        } // end of loop on up segment bridges
        
        if (bridgeMaybeDownSC != null) {
            return goDown(bridgeMaybeDownSC, throughSetMac, level);
        }

 
        
        Map<BridgePort, Set<String>> troughSet = upsimpleconn.getTroughSet();
        for (BridgePort port : splitted.keySet()) {
            troughSet.remove(port);
            Set<BridgePort> ports = new HashSet<BridgePort>();
            Set<String> macs = null;
            for (BridgeSimpleConnection simpleconn : splitted.get(port)) {
               Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
               for (BridgeForwardingTableEntry forward: simpleconn.getForwarders()) {
                    if (throughSetMac.contains(forward.getMacAddress())) {
                        continue;
                    }
                    forwarders.add(forward);
                }
                ports.addAll(simpleconn.getSimpleConnectionPorts());
                if (macs == null) {
                    macs = new HashSet<String>(
                                               simpleconn.getSimpleConnectionMacs());
                } else {
                    macs.retainAll(simpleconn.getSimpleConnectionMacs());
                }
                SharedSegment splitseg = SharedSegment.split(m_domain,
                                                             upSegment, macs,
                                                             ports,
                                                             forwarders,
                                                             port.getNodeId());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("goDown: level: {}, {}, splitted: ->\n -up -> {}\n -splitted ->\n{}",
                              level,
                              upsimpleconn.getSecondBridgePort().printTopology(),
                              upSegment.printTopology(),
                              splitseg.printTopology());
                }
            }
        }
        SharedSegment.merge(m_domain, upSegment,
                            upsimpleconn.getSimpleConnectionMacs(),
                            upsimpleconn.getSimpleConnectionPorts(),
                            upsimpleconn.getForwarders());
        for (BridgeSimpleConnection simpleconn : merged) {
            SharedSegment.merge(m_domain, upSegment,
                                simpleconn.getSimpleConnectionMacs(),
                                simpleconn.getSimpleConnectionPorts(),
                                simpleconn.getForwarders());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("goDown: level: {}, {}, merged ->\n{}", level,
                      upsimpleconn.getSecondBridgePort().printTopology(),
                      upSegment.printTopology());
        }
        //save trough set
        return goDown(troughSet,level);
    }
    
    private boolean goDown(Map<BridgePort, Set<String>> throughSet, Integer level) {
        for (BridgePort port : throughSet.keySet()) {
            SharedSegment segment = SharedSegment.createAndAddToBroadcastDomain(m_domain,port,throughSet.get(port));
            if (LOG.isDebugEnabled()) {
                LOG.debug("goDown: level: {}, added shared segment: ->\n{}",
                      level,
                     segment.printTopology());
            }
        }
        return true;
    }
    


}

