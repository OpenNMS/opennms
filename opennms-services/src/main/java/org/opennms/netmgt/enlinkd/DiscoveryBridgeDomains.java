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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryBridgeDomains extends Discovery {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryBridgeDomains.class);

    
    public DiscoveryBridgeDomains(EnhancedLinkd linkd) {
        super(linkd, linkd.getInitialSleepTime());
    }
        
    private void clean(BroadcastDomain domain, Set<Integer> nodes) throws BridgeTopologyException {
        for (Integer nodeid:nodes) {
            if (domain.getBridgeNodesOnDomain().contains(nodeid)) {
                continue;
            }
            BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(nodeid);
            if (olddomain != null) {
                m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, nodeid);
                if (LOG.isInfoEnabled()) {
                    LOG.info("clean: node: [{}]. Removed from Old Domain {} ", 
                             nodeid,
                             olddomain.printTopology());
                }
            }
        }
    }
    
    private BroadcastDomain find(Integer nodeid, Set<String> setA) throws BridgeTopologyException {
        BroadcastDomain olddomain = m_linkd.getQueryManager().getBroadcastDomain(nodeid);
        if (olddomain != null &&
                BroadcastDomain.checkMacSets(setA, olddomain.getMacsOnDomain())) {
            LOG.info("find: node: [{}]. node found on previuos Domain", 
                     nodeid);
            return olddomain;
        } 
        if (olddomain != null) {
            m_linkd.getQueryManager().reconcileTopologyForDeleteNode(olddomain, nodeid);
            if (LOG.isInfoEnabled()) {
                LOG.info("find: node: [{}]. Removed from Old Domain {} ", 
                     nodeid, olddomain.printTopology());
            }
        }
        BroadcastDomain domain = null;
        
        for (BroadcastDomain curBDomain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            if (curBDomain.getBridgeNodesOnDomain().contains(nodeid)) {
                continue;
            }
            if (BroadcastDomain.checkMacSets(setA, curBDomain.getMacsOnDomain())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("find: node: [{}], found:\n{}",
                             nodeid, 
                          curBDomain.printTopology());
                }
                return curBDomain;
            }
        }

        LOG.info("find: node: [{}]. No domain found, creating new Domain", nodeid);
        domain = new BroadcastDomain();
        m_linkd.getQueryManager().save(domain);
        return domain;        
    }
        
    @Override
    public void doit() {
        Map<Integer, Map<Integer,Set<BridgeForwardingTableEntry>>> nodeondomainbft = 
                new HashMap<Integer, Map<Integer,Set<BridgeForwardingTableEntry>>>();

        Map<Integer, Set<BridgeForwardingTableEntry>> nodeBft= new HashMap<Integer, Set<BridgeForwardingTableEntry>>();
        Map<Integer, Set<String>> nodeMacs = new HashMap<Integer, Set<String>>();
        
        Set<Integer> nodeids = new HashSet<Integer>(m_linkd.getQueryManager().getUpdateBftMap().keySet());

        LOG.info("run: getting nodes with updated bft on broadcast domains. Start");
        for (Integer nodeid: nodeids) {
            Set<BridgeForwardingTableEntry> links =  m_linkd.
                    getQueryManager().
                    useBridgeTopologyUpdateBFT(
                                    nodeid);
    
            if (links == null || links.size() == 0) {
                LOG.warn("run: node: [{}]. no updated bft. Return", 
                         nodeid);
                continue;
            }
            nodeBft.put(nodeid, links);
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry link : links) {
                macs.add(link.getMacAddress());
            }
            LOG.debug("run: node: [{}]. macs:{}", 
                      nodeid, 
                      macs);
            nodeMacs.put(nodeid, macs);
        }
            
        Set<Integer> parsed = new HashSet<Integer>();

        for (Integer nodeidA: nodeBft.keySet()) {
            if (parsed.contains(nodeidA)) {
                continue;
            }
            nodeondomainbft.put(nodeidA, new HashMap<Integer, Set<BridgeForwardingTableEntry>>());
            nodeondomainbft.get(nodeidA).put(nodeidA, nodeBft.get(nodeidA));
            parsed.add(nodeidA);
            for (Integer nodeidB: nodeBft.keySet()) {
                if (parsed.contains(nodeidB)) {
                    continue;
                }
                if (BroadcastDomain.checkMacSets(nodeMacs.get(nodeidA), 
                                                 nodeMacs.get(nodeidB))) {
                    nodeondomainbft.get(nodeidA).put(nodeidB, nodeBft.get(nodeidB));
                    parsed.add(nodeidB);
                    LOG.info("run: node: [{}], node [{}] are on same domain", 
                         nodeidA, 
                         nodeidB);
                }
            }
        }

    	Map<Integer, BroadcastDomain> nodedomainMap = new HashMap<Integer, BroadcastDomain>();
    	for (Integer nodeid: nodeondomainbft.keySet()) {
            LOG.info("run: node: [{}], getting broadcast domain. Start", nodeid);
            try {
                BroadcastDomain domain = find(nodeid,nodeMacs.get(nodeid));
                if (domain == null) {
                    LOG.error("run: node: [{}], null broadcast domain.", nodeid);
                    continue;
                }                    
                clean(domain, nodeondomainbft.get(nodeid).keySet());
                nodedomainMap.put(nodeid, domain);
            } catch (BridgeTopologyException e) {
                LOG.error("run: node: [{}], getting broadcast domain. Failed {}", nodeid,
                          e.getMessage());
                continue;
            }
            LOG.info("run: node: [{}], getting broadcast domain. End", nodeid);
    	}
    	
    	int n = 5;
    	if (nodedomainMap.size() < 5) {
    	    n=nodedomainMap.size();
    	}
    	
    	if (n == 0 ) {
            LOG.info("run: no Domain to process", n);
    	    return;
    	}

        LOG.info("run: creating executorService with {} Threads", n);
    	ExecutorService executorService = Executors.newFixedThreadPool(n);
        LOG.info("run: created executorService with {} Threads", n);
    	
    	List<Callable<String>> taskList = new ArrayList<Callable<String>>();
    	for (Integer nodeid: nodedomainMap.keySet()) {
    	    
    	    NodeDiscoveryBridgeTopology nodebridgetopology = m_linkd.getNodeBridgeDiscoveryTopology(nodeid);
    	    nodebridgetopology.setDomain(nodedomainMap.get(nodeid));
    	    Map<Integer,Set<BridgeForwardingTableEntry>> notYetParsedBFT = nodeondomainbft.get(nodeid);
    	    for (Integer bridgeId: notYetParsedBFT.keySet()) {
    	        nodebridgetopology.addUpdatedBFT(bridgeId, notYetParsedBFT.get(bridgeId));
    	    }
    	    Callable<String> task = () -> {
    	        nodebridgetopology.doit();
    	        return "Topology calculated: " + nodebridgetopology.getInfo();
    	    };
    	    taskList.add(task);
            LOG.info("run: adding bridge topology discovery Task {}", nodebridgetopology.getInfo());
    	}
    	
        try {
            for(Future<String> future: executorService.invokeAll(taskList)) {
                LOG.info("run: {}" ,future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("run: executing tasks {}", e.getMessage(),e);
        }

        executorService.shutdown(); 
        LOG.info("run: getting nodes with updated bft on broadcast domains. End");
        

    }

    @Override
    public String getName() {
        return "DiscoveryBridgeDomain";
    }
            
}

