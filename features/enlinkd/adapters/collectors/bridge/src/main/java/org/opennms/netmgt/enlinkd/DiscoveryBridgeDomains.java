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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
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

import org.opennms.netmgt.enlinkd.common.Discovery;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.DiscoveryBridgeTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryBridgeDomains extends Discovery {

    public static DiscoveryBridgeDomains clone(DiscoveryBridgeDomains dbd) {
        return new DiscoveryBridgeDomains(dbd.getBridgeTopologyService());
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryBridgeDomains.class);
    private int m_maxthreads=1;
    private final BridgeTopologyService m_bridgeTopologyService;

    public DiscoveryBridgeDomains(BridgeTopologyService bridgeTopologyService) {
        super();
        m_bridgeTopologyService = bridgeTopologyService;
    }
            
    private BroadcastDomain find(Set<Integer> nodes, Set<String> setA) throws BridgeTopologyException {
        
        BroadcastDomain domain = null;
        
        for (BroadcastDomain curBDomain : m_bridgeTopologyService.findAll()) {
            if (BroadcastDomain.checkMacSets(setA, curBDomain.getMacsOnSegments())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("find: node:{}, domain:{}",
                             nodes, 
                          curBDomain.getBridgeNodesOnDomain());
                }
                if (domain != null) {
                    throw new BridgeTopologyException("at least two domains found", domain);
                }
                domain = curBDomain;                
            }
        }

        if (domain == null) {
            LOG.debug("find: nodes: [{}]. No domain found, creating new Domain", nodes);
            domain = new BroadcastDomain();
            m_bridgeTopologyService.add(domain);
        }

        for (Integer nodeid: nodes) {
            BroadcastDomain olddomain = m_bridgeTopologyService.getBroadcastDomain(nodeid);
            if (olddomain == null) {
                continue;
            }
            if ( domain == olddomain ) {
                continue;
            }
            m_bridgeTopologyService.reconcile(olddomain, nodeid);
            if (LOG.isDebugEnabled()) {
                LOG.debug("find: node:[{}]. Removed from Old Domain \n{}", 
                     nodeid, olddomain.printTopology());
            }
        }

        return domain;        
    }
    
    @Override
    public void runDiscovery() {
        LOG.info("run: calculate topology on broadcast domains. Start");
        
        Map<Integer, Map<Integer, Set<BridgeForwardingTableEntry>>> nodeondomainbft 
            = new HashMap<Integer, Map<Integer, Set<BridgeForwardingTableEntry>>>();

        Map<Integer, Set<BridgeForwardingTableEntry>> nodeBft 
            = new HashMap<Integer, Set<BridgeForwardingTableEntry>>();
        Map<Integer, Set<String>> nodeMacs 
        = new HashMap<Integer, Set<String>>();

        Set<Integer> nodeids 
        = new HashSet<Integer>(
                m_bridgeTopologyService.getUpdateBftMap().keySet());
        
        LOG.debug("run: nodes with updated bft {}", nodeids);

        for (Integer nodeid : nodeids) {
            Set<BridgeForwardingTableEntry> links = m_bridgeTopologyService.useBridgeTopologyUpdateBFT(nodeid);

            if (links == null || links.size() == 0) {
                LOG.warn("run: node:[{}]. no updated bft. Return", nodeid);
                continue;
            }
            nodeBft.put(nodeid, links);
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry link : links) {
                macs.add(link.getMacAddress());
            }
            LOG.debug("run: node:[{}]. macs:{}", nodeid, macs);
            nodeMacs.put(nodeid, macs);
        }

        Set<Integer> parsed = new HashSet<Integer>();

        for (Integer nodeidA : nodeBft.keySet()) {
            if (parsed.contains(nodeidA)) {
                continue;
            }
            nodeondomainbft.put(nodeidA,
                                new HashMap<Integer, Set<BridgeForwardingTableEntry>>());
            nodeondomainbft.get(nodeidA).put(nodeidA, nodeBft.get(nodeidA));
            parsed.add(nodeidA);
            for (Integer nodeidB : nodeBft.keySet()) {
                if (parsed.contains(nodeidB)) {
                    continue;
                }
                if (BroadcastDomain.checkMacSets(nodeMacs.get(nodeidA),
                                                 nodeMacs.get(nodeidB))) {
                    nodeondomainbft.get(nodeidA).put(nodeidB,
                                                     nodeBft.get(nodeidB));
                    parsed.add(nodeidB);
                }
            }
        }

        List<Callable<String>> taskList = new ArrayList<Callable<String>>();
        for (Integer nodeid : nodeondomainbft.keySet()) {
            LOG.debug("run: nodes are on same domain {}",nodeondomainbft.get(nodeid).keySet());
            try {
                BroadcastDomain domain = find(nodeondomainbft.get(nodeid).keySet(),
                                              nodeMacs.get(nodeid));
                DiscoveryBridgeTopology nodebridgetopology = new DiscoveryBridgeTopology(domain);
                
                synchronized (domain) {
                    for (Integer bridgeId : nodeondomainbft.get(nodeid).keySet()) {
                        nodebridgetopology.addUpdatedBFT(bridgeId,
                                                         nodeondomainbft.get(nodeid).get(bridgeId));
                        m_bridgeTopologyService.updateBridgeOnDomain(domain,bridgeId);
                    }
                }
                                
                Callable<String> task = () -> {
                    synchronized (domain) {
                        
                        Date now = new Date();
                        LOG.debug("run: calculate start"); 
                        nodebridgetopology.calculate();
                        LOG.debug("run: calculate end"); 
                    
                        LOG.debug("run: save start");
                        try {
                            m_bridgeTopologyService.store(domain, now);
                        } catch (BridgeTopologyException e) {
                            LOG.error("run: saving topology failed: {}. {}", 
                                      e.getMessage(),
                                      e.printTopology());
                        } catch (ConcurrentModificationException e) {
                            LOG.error("run: bridge:[{}], saving topology failed: {}. {}",
                                      nodebridgetopology,
                                      e.getMessage(),
                                      domain.printTopology());
                        }
                        LOG.debug("run: save end");
                    }
                    return "executed Task: " + nodebridgetopology.getInfo();
                };
                taskList.add(task);
                LOG.info("run: added Task {}", nodebridgetopology.getInfo());
            } catch (BridgeTopologyException e) {
                LOG.error("run: node: [{}], getting broadcast domain. Failed {}",
                          nodeid, e.getMessage());
                continue;
            }
        }

        int n = taskList.size();
        if (n > m_maxthreads) {
            n = m_maxthreads;
        }

        if (n > 0) {
            LOG.debug("run: creating executorService with {} Threads", n);
            ExecutorService executorService = Executors.newFixedThreadPool(n);
            LOG.debug("run: created executorService with {} Threads", n);

            try {
                for (Future<String> future : executorService.invokeAll(taskList)) {
                    LOG.info("run: {}", future.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("run: executing task {}", e.getMessage(), e);
            }
            executorService.shutdown();
        } else {
            LOG.info("run: no updates on broadcast domains");
        }
        LOG.info("run: calculate topology on broadcast domains. End");

    }

    @Override
    public String getName() {
        return "DiscoveryBridgeDomain";
    }

    public int getMaxthreads() {
        return m_maxthreads;
    }

    public void setMaxthreads(int maxthreads) {
        m_maxthreads = maxthreads;
    }

    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }
            
}

