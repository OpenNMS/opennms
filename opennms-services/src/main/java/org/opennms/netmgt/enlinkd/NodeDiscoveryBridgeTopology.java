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

    Map<Bridge,Set<BridgeForwardingTableEntry>> m_notYetParsedBFTMap;
    BroadcastDomain m_domain;

    public BroadcastDomain getDomain() {
        return m_domain;
    }

    public void setDomain(BroadcastDomain domain) {
        m_domain = domain;
    }

    public Map<Bridge, Set<BridgeForwardingTableEntry>> getNotYetParsedBFTMap() {
        return m_notYetParsedBFTMap;
    }

    public void addUpdatedBFT(Bridge bridge, Set<BridgeForwardingTableEntry> notYetParsedBFT) {
        if (m_notYetParsedBFTMap==null)
            m_notYetParsedBFTMap = new HashMap<Bridge, Set<BridgeForwardingTableEntry>>();
        m_notYetParsedBFTMap.put(bridge, notYetParsedBFT);
    }

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
    }

    private Set<Integer> getAllNodesWithUpdatedBFTOnDomain(Set<String>incomingSet, Map<Integer,Set<BridgeForwardingTableEntry>> nodeBftMap) {
        Set<Integer> nodeswithupdatedbftonbroadcastdomain= new HashSet<Integer>();
        nodeswithupdatedbftonbroadcastdomain.add(getNodeId());

        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. Start", getNodeId());
        synchronized (nodeBftMap) {
            for (Integer curNodeId: nodeBftMap.keySet()) {
                if (curNodeId.intValue() == getNodeId())
                    continue;
                Set<String>retainedSet = new HashSet<String>();
                for (BridgeForwardingTableEntry link: nodeBftMap.get(curNodeId)) {
                    retainedSet.add(link.getMacAddress());
                }
                LOG.debug("run: node: [{}], parsing updated bft node: [{}], macs {}", getNodeId(), curNodeId,retainedSet);
                retainedSet.retainAll(incomingSet);
                LOG.debug("run: node: [{}], node: [{}] - common mac address set: {}", getNodeId(), curNodeId, retainedSet);
                if (retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
                        || retainedSet.size() >= incomingSet.size() * DOMAIN_MATCH_MIN_RATIO) {
                    nodeswithupdatedbftonbroadcastdomain.add(curNodeId);
                    LOG.debug("run: node: [{}], node: [{}] - put on same broadcast domain, common macs: {} ", getNodeId(), 
                             curNodeId,
                             retainedSet);
                }
            }            
        }
        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. End", getNodeId());
        return nodeswithupdatedbftonbroadcastdomain;
    }
    
    @Override
    public void run() {

        if (!m_linkd.getQueryManager().hasUpdatedBft(getNodeId())) {
            LOG.info("run: node: [{}], no updated bft. End Topology Bridge Discovery run", getNodeId());
            return;
        }

    	Set<BridgeForwardingTableEntry> links =  m_linkd.
        		getQueryManager().
        		getBridgeTopologyUpdateBFT(
        				getNodeId());
    	if (links == null || links.size() == 0) {
            LOG.info("run: node: [{}]. no updates macs found.", getNodeId());
            return;
    	}
    	Date now = new Date();
                
        Set<String> incomingSet = new HashSet<String>();
        synchronized (links) {
            for (BridgeForwardingTableEntry link : links) {
                incomingSet.add(link.getMacAddress());
            }            
        }
        LOG.info("run: node: [{}]. macs found: {}", getNodeId(), incomingSet);

        LOG.info("run: node: [{}], getting broadcast domain. Start", getNodeId());
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            synchronized (domain) {
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("run: node: [{}], parsing domain: {}", 
                          getNodeId(),
                          domain.printTopology());
                }
                Set<String>retainedSet = new HashSet<String>(
                                                              domain.getMacsOnDomain());
                retainedSet.retainAll(incomingSet);
                LOG.info("run: node: [{}], retained: {}", getNodeId(), retainedSet);
                // should contain at list 5 or 10% of the all size
                if (retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
                        || retainedSet.size() >= incomingSet.size() * DOMAIN_MATCH_MIN_RATIO) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("run: node: [{}], domain {} found!",getNodeId(), 
                              domain.printTopology());
                    }
                    m_domain = domain;
                    break;
                    // TODO: We should find the *best* domain, instead of using the last match
                }
            }
        }
        if (m_domain == null) {
            LOG.info("run: node: [{}] Creating a new Domain", getNodeId());
            m_domain = new BroadcastDomain();
            m_linkd.getQueryManager().save(m_domain);
        } else if (m_domain.getRootBridge() == null) {
            //FIXME should never be like this? And what do to clear topology or give up!
            LOG.warn("run: node: [{}] Domain without root, clearing topology", getNodeId());
            m_domain.clearTopology();
        }
        LOG.info("run: node: [{}], getting broadcast domain. End", getNodeId());
                
        Map<Integer,Set<BridgeForwardingTableEntry>> nodeBftMap = m_linkd.getQueryManager().getUpdateBftMap();
        //FIXME this should be synchronized?
        Set<Integer> nodeswithupdatedbftonbroadcastdomain = getAllNodesWithUpdatedBFTOnDomain(incomingSet,nodeBftMap);            

        LOG.info("run: node: [{}], clean broadcast domains. Start", getNodeId());
        boolean clean = false;
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
        	if (m_domain == domain)
        		continue;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("run: node [{}]: cleaning broadcast domain {}.",
            		getNodeId(),
                      domain.printTopology());
                }
            for (Integer curNodeId: nodeswithupdatedbftonbroadcastdomain) {
                Bridge bridge = domain.getBridge(curNodeId);
                if (bridge != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("run: node [{}]: node [{}]: removing from broadcast domain {}!",
                    		getNodeId(),
                    		  curNodeId,
                              domain.getBridgeNodesOnDomain());
                    }
                    synchronized (domain) {
                        try {
                            domain.removeBridge(curNodeId);
                            m_linkd.getQueryManager().store(domain,now);
                        } catch (BridgeTopologyException e) {
                            LOG.error("run: node [{}]: node [{}] cannot remove bridge {}",getNodeId(),
                                      curNodeId, e.getMessage(),e);
                        }
                    }
                    clean=true;
                }
            }
        }
        if (clean) {
            m_linkd.getQueryManager().cleanBroadcastDomains();
        }
        LOG.info("run: node: [{}], clean broadcast domains. End", getNodeId());

        synchronized (m_domain) {
            m_notYetParsedBFTMap = new HashMap<Bridge, Set<BridgeForwardingTableEntry>>();
            for (Integer nodeid : nodeswithupdatedbftonbroadcastdomain) {
                sendStartEvent(nodeid);
                Bridge.create(m_domain,nodeid);
                LOG.info("run: node: [{}], added bridge  node [{}] on domain", getNodeId(), nodeid);
                LOG.debug("run: node: [{}], getting update bft for node [{}] on domain", getNodeId(), nodeid);
                Set<BridgeForwardingTableEntry> bft = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(nodeid);
                if (bft == null || bft.isEmpty()) {
                    LOG.debug("run: node: [{}], no update bft for node [{}] on domain", getNodeId(), nodeid);
                    continue;
                }
                m_notYetParsedBFTMap.put(m_domain.getBridge(nodeid), bft);
            }

            Set<Integer> nodetoberemovedondomain = new HashSet<Integer>();
            synchronized (nodeBftMap) {
                for (Integer nodeid : nodeBftMap.keySet()) {
                    if (nodeswithupdatedbftonbroadcastdomain.contains(nodeid))
                        continue;
                    LOG.info("run: node [{}]: bridge [{}] with updated bft. Not even more on broadcast domain {}: clear topology.",
                            getNodeId(),
                            nodeid,
                            m_domain.getBridgeNodesOnDomain());
                    nodetoberemovedondomain.add(nodeid);
                }
            }
            for (Integer nodeid : nodetoberemovedondomain) {
                try {
                    m_domain.removeBridge(nodeid);
                } catch (BridgeTopologyException e) {
                    LOG.error("run: node [{}]: node [{}] cannot remove bridge {}",getNodeId(),
                              nodeid, e.getMessage(),e);
                }
            }
            m_linkd.getQueryManager().cleanBroadcastDomains();

            //FIXME check everything is right
            m_linkd.getQueryManager().updateBridgesOnDomain(m_domain);

            if (m_notYetParsedBFTMap.isEmpty()) {
                LOG.info("run: node: [{}], broadcast domain has no topology updates. No more action is needed.", getNodeId());
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("calculate: node: [{}], start: broadcast domain {} topology calculation.", 
                         getNodeId(),
                         m_domain.printTopology());
                }
                calculate();
                if (LOG.isInfoEnabled()) {
                    LOG.info("calculate: node: [{}], end: broadcast domain {} topology calculation.", 
                         getNodeId(),
                         m_domain.printTopology());
                }
            }

            LOG.info("run: node: [{}], saving Topology.", getNodeId());
            m_linkd.getQueryManager().store(m_domain, now);
            LOG.info("run: node: [{}], saved Topology.", getNodeId());

            for (Integer curNode : nodeswithupdatedbftonbroadcastdomain) {
                sendCompletedEvent(curNode);
            }
        }
    }
            
    @Override
    protected void runCollection() {
    }

    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

    private Bridge getElectedRootBridge() throws BridgeTopologyException {
        
        Bridge electedRoot = m_domain.electRootBridge();
        
        Bridge rootBridge = m_domain.getRootBridge();
        if (electedRoot == null && rootBridge != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], electRootBridge: mantaining old root bridge: {}", 
                    getNodeId(),
                    rootBridge.printTopology());
            }
            electedRoot = m_domain.getRootBridge();        	
        } else if (electedRoot == null) {
	        // no spanning tree root?
	        // why I'm here?
	        // not root bridge defined (this mean no calculation yet done...
	        // so checking the best into not parsed
	        int size=0;
	        
	        for (Bridge bridge:  m_notYetParsedBFTMap.keySet()) {
	            LOG.debug("calculate: node: [{}], bridge [{}]: max bft size \"{}\" in topology",
	                    getNodeId(),
	                    bridge.getNodeId(), 
	                    m_notYetParsedBFTMap.get(bridge).size());
	            if (size < m_notYetParsedBFTMap.get(bridge).size()) {
	                rootBridge = bridge;
	                size = m_notYetParsedBFTMap.get(bridge).size();
	            }
	        }
	        if (rootBridge != null ) {
	            if (LOG.isDebugEnabled()) {
	                LOG.debug("calculate: node: [{}], bridge [{}]: elected root with max bft size \"{}\" in topology",
	                    getNodeId(),
	                    rootBridge.getNodeId(), 
	                    size);
	            }
	            electedRoot = rootBridge;
	        }
        } 
        
        if (electedRoot == null ) {
            electedRoot = m_domain.getBridges().iterator().next();
            if (LOG.isDebugEnabled()) {
        	LOG.debug("calculate: node: [{}], electRootBridge: first root bridge: {}", 
                       getNodeId(),
                       electedRoot.printTopology());
            }
        }

        if (electedRoot.getNodeId() == null) {
            LOG.error("calculate: node: [{}], electedRootBridge must have an id!",
                    getNodeId()
            		);
            throw new BridgeTopologyException("electedRoot bridge id cannot be null", electedRoot);
        }
        return electedRoot;
        
    }

    private Set<BridgeForwardingTableEntry> getRootBridgeForwardingTable(Bridge electedRoot) throws BridgeTopologyException {

        Bridge rootBridge = m_domain.getRootBridge();
        Set<BridgeForwardingTableEntry> rootBft = 
                m_notYetParsedBFTMap.remove(electedRoot);
        
        if (rootBridge != null && rootBridge.getNodeId() == electedRoot.getNodeId() && rootBft == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], no updated bft, elected is equal to old root bridge: {}",
                    getNodeId(), 
            		electedRoot.printTopology());
            }
            rootBft = m_domain.calculateRootBFT();
        } else if ( rootBft != null ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], elected root bridge: [{}], has updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
            }
            m_domain.clearTopologyForBridge(electedRoot);
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], root bridge: [{}], cleared topology:\n{}",
                      getNodeId(), 
                      electedRoot.printTopology(),
                       m_domain.printTopology());
            }
            if (m_domain.getSharedSegments().isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}], new elected root bridge: [{}], is the first bridge in topology. Adding root shared segments",
                       getNodeId(), 
                        electedRoot.getNodeId());
                }
                loadFirstLevelSharedSegment(rootBft);
                electedRoot.setRootBridge();
           } else {
                calculate(
                  BridgeForwardingTable.create(
                       m_domain.getRootBridge(), 
                       m_domain.calculateRootBFT()),
                  BridgeForwardingTable.create(
                       electedRoot, 
                       rootBft));
                addForwarding(m_domain, rootBft);
                m_domain.hierarchySetUp(electedRoot);
           }
        } else {
           LOG.debug("calculate: node: [{}], elected root bridge: [{}], is new, without updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
           m_domain.hierarchySetUp(electedRoot);
           rootBft = m_domain.calculateRootBFT();
        }
        return rootBft;
    }

    protected  void calculate() {
        Bridge electedRoot =null;
        try {
            electedRoot = getElectedRootBridge();
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: node: [{}], no bridge to be elected. Return without changes:\n{}",
                      getNodeId(), 
                      m_domain.printTopology());
            return;
        }
        Set<BridgeForwardingTableEntry> rootBft = null;
        try {
            rootBft = getRootBridgeForwardingTable(electedRoot);
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: node: [{}]. {}, topology:\n{}", getNodeId(),e.getMessage(),e.printTopology(),e);
            return;
        }

        BridgeForwardingTable rootft;
        try {
            rootft = BridgeForwardingTable.create(electedRoot, rootBft);
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: node: [{}]. {}, topology:\n{}", getNodeId(),e.getMessage(),e.printTopology(),e);
            return;
        }

        if (!m_notYetParsedBFTMap.isEmpty()) {
            for (Bridge xBridge: m_notYetParsedBFTMap.keySet()) {
                try {
                    m_domain.clearTopologyForBridge(xBridge);
                } catch (BridgeTopologyException e) {
                    LOG.error("calculate: node: [{}]. {}, topology:\n{}", getNodeId(),e.getMessage(),e.printTopology(),e);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}], Removed bridge: [{}] form domain: {}", 
                		getNodeId(),
                		xBridge.getNodeId(),
                		m_domain.printTopology());
                }
            }
        }

        Set<Bridge> nodetobeparsed = new HashSet<Bridge>(m_notYetParsedBFTMap.keySet());
        for (Bridge xBridge: nodetobeparsed) {
            BridgeForwardingTable xbridgeft;
            try {
                xbridgeft = BridgeForwardingTable.create(xBridge, 
                                   new HashSet<BridgeForwardingTableEntry>(m_notYetParsedBFTMap.remove(xBridge)));
            } catch (BridgeTopologyException e) {
                LOG.error("calculate: node: [{}]. {}, topology:\n{}", getNodeId(),e.getMessage(),e.printTopology(),e);
                return;
            }
            calculate(rootft, xbridgeft);
        }
        m_domain.cleanForwarders();
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}], Print Topology {}",
                    getNodeId(),
                    m_domain.printTopology());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: node: [{}], stop: broadcast domain {} topology calculated.",
                    getNodeId(),
                    m_domain.getBridgeNodesOnDomain());
        }
    }
     
    private void addForwarding(BroadcastDomain domain, Set<BridgeForwardingTableEntry> bft) {
        for (BridgeForwardingTableEntry maclink: bft) {
            if (domain.getMacsOnDomain().contains(maclink.getMacAddress())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}]. Skipping forwarding: {}",
                          getNodeId(), 
                          maclink.printTopology());
                }
                continue;                    
            }
            domain.addForwarding(maclink);
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}]. Adding forwarding: {}",
                      getNodeId(), 
                      maclink.printTopology());
            }
        }
    }

    private void loadFirstLevelSharedSegment(Set<BridgeForwardingTableEntry> electedRootBFT) throws BridgeTopologyException {
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
            SharedSegment segment = SharedSegment.createAndAddToBroadcastDomain(m_domain,port,rootleafs.get(port));
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], add shared segment[designated bridge:[{}],"
            		+ "designated port:{}, macs: {}]",
            		 getNodeId(),
                     segment.getDesignatedBridge(),
                     segment.getDesignatedPort().getBridgePort(),
                     segment.getMacsOnSegment());
            }
        }      
   }
    
    private void calculate(BridgeForwardingTable root,  
            BridgeForwardingTable xBridge) {
        BridgeSimpleConnection rx = new BridgeSimpleConnection(root, xBridge);
        if (rx.findSimpleConnection()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("calculate: node: [{}]. {}, simple connection found:\n{}", rx.printTopology());
            }
        } else {
            LOG.warn("calculate: node: [{}]. {}, no simple connection found:\n{}", rx.printTopology());
            return;
        }
        Integer rxDesignatedPort = rx.getFirstBridgeConnectionPort();
        if (rxDesignatedPort == null) {
            LOG.warn("calculate: node: [{}], cannot found simple connection for bridges: [{},{}]", 
            		getNodeId(),
            		root.getNodeId(), 
            		xBridge.getNodeId());
            m_domain.clearTopology();
            return;
        }
        Integer xrDesignatedPort = rx.getSecondBridgeConnectionPort();
        if (xrDesignatedPort == null) {
             LOG.warn("calculate: node: [{}], cannot found simple connectionfor bridges: [{},{}]",
             		getNodeId(),
             		xBridge.getNodeId(), 
             		root.getNodeId());
             m_domain.clearTopology();
             return;
        }
        LOG.debug("calculate: node: [{}], level: 1, bridge: [{}], root port:[{}] ",
        		getNodeId(),
        		xBridge.getNodeId(),
        		xrDesignatedPort);
        xBridge.setRootPort(xrDesignatedPort);
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = m_domain.getSharedSegment(root.getNodeId(),rxDesignatedPort);
        if (topSegment == null) {
            LOG.warn("calculate: node: [{}], level: 1, nodeid: [{}], port {}. top segment not found.",
            		getNodeId(),
            		m_domain.getRootBridge().getNodeId(),
            		rxDesignatedPort);
            m_domain.clearTopology();
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}], bridge: [{}] -> root segment: \n{} ",
                        getNodeId(),
                        xBridge.getNodeId(),
                        topSegment.printTopology());
        }

        //FIXME manage error should really clear topology?
        if (!findBridgesTopo(rx,topSegment, xBridge,0)) {
            return;
            //m_domain.clearTopology();
        }
    }

    // here we assume that rbridge exists in topology
    // while xBridge is to be added
    private boolean findBridgesTopo(BridgeSimpleConnection rx,
            SharedSegment topSegment, 
            BridgeForwardingTable xBridge, 
            int level) {
        if (topSegment == null) {
            LOG.warn("calculate: node: [{}]: level: {}, bridge: [{}], top segment is null exiting.....",
                     getNodeId(),
                     level,
                     xBridge.getNodeId());
         return false;
        }
        level++;
        if (level == 30) {
            LOG.warn("calculate: node: [{}]: level: {}, bridge: [{}], too many iteration on topology exiting.....",
                        getNodeId(),
                        level,
                        xBridge.getNodeId());
            return false;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}], level: {}, bridge: [{}] -> check if is child of: \n{} ",
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
                LOG.error("calculate: node: [{}]. level: {}. {} topology:\n{}", 
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
            LOG.debug("calculate: node: [{}], level: {}, bridge: [{}], bridge: [{}, designated port: {}]",
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
                LOG.error("calculate: node: [{}]. level: {}. {} topology:\n{}", 
                          getNodeId(),
                          level,
                          e.getMessage(),e.printTopology(),e);
                return false;
            }
            if (!yx.findSimpleConnection()) {;
                LOG.error("calculate: node: [{}]. level: {}. no simple connection:\n{}", 
                          getNodeId(),
                          level, yx.printTopology());
                return false;
            }
            Integer  xyDesignatedPort = yx.getSecondBridgeConnectionPort();
            Integer  yxDesignatedPort = yx.getFirstBridgeConnectionPort();
            // if X is a leaf of Y then iterate
            if (xyDesignatedPort == rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.debug("calculate: node: [{}]: level: {}, bridge: [{}] is a leaf of bridge: [{}], going one level down",
                		getNodeId(),
                		level,xBridge.getNodeId(),yBridge.getNodeId());
                if (!findBridgesTopo(yx,m_domain.getSharedSegment(yBridgeId, yxDesignatedPort), xBridge,level)) {
                	return false;
                }
                return true;
            }
            // Y is a leaf of X then 
            // remove Y from topSegment
            // Create shared Segment with X designated bridge 
            // or if exists then retain all common macs on domain
            // Assign Forwarders for Y
            // Clean also topSegment macs.
            if (yxDesignatedPort == yrDesignatedPort && xyDesignatedPort != rx.getSecondBridgeConnectionPort()) {
                //create a SharedSegment with root port
                LOG.info("calculate: node: [{}], level: {}, bridge: [{},designated port [{}]]: found level.", 
                          getNodeId(), 
                          level,
                          xBridge.getNodeId(),
                          xyDesignatedPort);
                LOG.debug("calculate: node: [{}], level: {}, bridge: [{},designated port [{}]]: is 'hierarchy up' for bridge: [{}].", 
                		getNodeId(), 
                		level,
                		xBridge.getNodeId(),
                		xyDesignatedPort,
                                yBridge.getNodeId());
                SharedSegment leafSegment = m_domain.getSharedSegment(xBridge.getNodeId(), xyDesignatedPort);
                if (leafSegment == null) {
                    try {
                        leafSegment = SharedSegment.createAndAddToBroadcastDomain(m_domain,yx.getSimpleConnection(),yx.getSimpleConnectionMacs(),xBridge.getNodeId());
                    } catch (BridgeTopologyException e) {
                        LOG.error("calculate: node: [{}]. level: {}. {} topology:\n{}", 
                                  getNodeId(),
                                  level,
                                  e.getMessage(),e.printTopology(),e);
                        return false;
                    }
                } else {
                    leafSegment.retain(yx.getSimpleConnectionMacs(),yx.getFirstBridgePort());
                }
                portsAdded.add(xyDesignatedPort);
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}], level: {}, bridge [{}]. Remove bridge [{}] and macs {} from top segment.\n{}", 
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
                    LOG.error("calculate: node: [{}]. level: {}. {} topology:\n{}", 
                              getNodeId(),
                              level,
                              e.getMessage(),e.printTopology(),e);
                    return false;
                }
            } else if (xyDesignatedPort != rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.warn("calculate: node: [{}]: level {}: bridge [{}]. Topology mismatch. Clearing...topology",
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
        topSegment.assign(macsOnSegment,rx.getSecondBridgePort());
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}]: level: {}, bridge: [{}], port[{}], macs{} -> assigned.\n{}", 
                        getNodeId(), 
                 level,xBridge.getNodeId(),rx.getSecondBridgePort().getBridgePort(),macsOnSegment,topSegment.printTopology());
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
                LOG.error("calculate: node: [{}]. level: {}. {} topology:\n{}", 
                          getNodeId(),
                          level,
                          e.getMessage(),e.printTopology(),e);
                return false;
            }  
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}]: level: {}, bridge: [{}]. Add shared segment\n{}",
                          getNodeId(), 
                     level,
                     xBridge.getNodeId(),
                     xleafSegment.printTopology());
            }
        }
        addForwarding(m_domain, forwarders);
        return true;
    }

}

