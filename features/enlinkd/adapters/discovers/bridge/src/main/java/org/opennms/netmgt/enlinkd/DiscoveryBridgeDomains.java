/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd;

import java.util.ArrayList;
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

import org.opennms.netmgt.scheduler.Schedulable;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.DiscoveryBridgeTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryBridgeDomains extends Schedulable {

    public static final int DOMAIN_MATCH_MIN_SIZE = 20;
    public static final float DOMAIN_MATCH_MIN_RATIO = 0.5f;

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

    public static boolean checkMacSets(Set<String> setA, Set<String> setB) {
        Set<String> retainedSet = new HashSet<>(setB);
        retainedSet.retainAll(setA);
        // should contain at list 20 or 50% of the all size
        return retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
                || retainedSet.size() > setA.size() * DOMAIN_MATCH_MIN_RATIO
                || retainedSet.size() > setB.size() * DOMAIN_MATCH_MIN_RATIO;
    }

    private BroadcastDomain find(Set<Integer> nodes, Set<String> setA) throws BridgeTopologyException {
        
        BroadcastDomain domain = null;
        
        for (BroadcastDomain curBDomain : m_bridgeTopologyService.findAll()) {
            if (checkMacSets(setA, curBDomain.getMacsOnSegments())) {
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
    public void runSchedulable() {
        LOG.info("run: calculate topology on broadcast domains. Start");
        
        Map<Integer, Map<Integer, Set<BridgeForwardingTableEntry>>> nodeondomainbft 
            = new HashMap<>();

        Map<Integer, Set<BridgeForwardingTableEntry>> nodeBft 
            = new HashMap<>();
        Map<Integer, Set<String>> nodeMacs 
        = new HashMap<>();

        Set<Integer> nodeids 
        = new HashSet<>(
                m_bridgeTopologyService.getUpdateBftMap().keySet());
        
        LOG.debug("run: nodes with updated bft {}", nodeids);

        for (Integer nodeid : nodeids) {
            Set<BridgeForwardingTableEntry> links = m_bridgeTopologyService.useBridgeTopologyUpdateBFT(nodeid);

            if (links == null || links.size() == 0) {
                LOG.warn("run: node:[{}]. no updated bft. Return", nodeid);
                continue;
            }
            nodeBft.put(nodeid, links);
            Set<String> macs = new HashSet<>();
            for (BridgeForwardingTableEntry link : links) {
                macs.add(link.getMacAddress());
            }
            LOG.debug("run: node:[{}]. macs:{}", nodeid, macs);
            nodeMacs.put(nodeid, macs);
        }

        Set<Integer> parsed = new HashSet<>();

        for (Integer nodeidA : nodeBft.keySet()) {
            if (parsed.contains(nodeidA)) {
                continue;
            }
            nodeondomainbft.put(nodeidA,
                    new HashMap<>());
            nodeondomainbft.get(nodeidA).put(nodeidA, nodeBft.get(nodeidA));
            parsed.add(nodeidA);
            for (Integer nodeidB : nodeBft.keySet()) {
                if (parsed.contains(nodeidB)) {
                    continue;
                }
                if (checkMacSets(nodeMacs.get(nodeidA),
                                                 nodeMacs.get(nodeidB))) {
                    nodeondomainbft.get(nodeidA).put(nodeidB,
                                                     nodeBft.get(nodeidB));
                    parsed.add(nodeidB);
                }
            }
        }

        List<Callable<String>> taskList = new ArrayList<>();
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
                        m_bridgeTopologyService.store(domain, now);
                        LOG.debug("run: save end");
                    }
                    return "executed Task: " + nodebridgetopology.getInfo();
                };
                taskList.add(task);
                LOG.info("run: added Task {}", nodebridgetopology.getInfo());
            } catch (BridgeTopologyException e) {
                LOG.error("run: node: [{}], getting broadcast domain. Failed {}",
                          nodeid, e.getMessage());
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

    public void setMaxthreads(int maxthreads) {
        m_maxthreads = maxthreads;
    }

    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }
            
}

