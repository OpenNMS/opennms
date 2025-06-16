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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class DiscoveryBridgeTopology {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryBridgeTopology.class);

    private final Map<Integer,BridgeForwardingTable> m_bridgeFtMapUpdate = new HashMap<>();
    private final BroadcastDomain m_domain;
    private Set<Integer> m_failed;
    private Set<Integer> m_parsed;

    public static Set<String> getMacs(BridgeForwardingTable xBridge,
                                      BridgeForwardingTable yBridge, BridgeSimpleConnection simple)
            throws BridgeTopologyException {

        if ( simple.getFirstPort() == null) {
            throw new BridgeTopologyException("getMacs: not found simple connection ["
                    + xBridge.getNodeId() + "]", simple);
        }

        if ( simple.getSecondPort() == null) {
            throw new BridgeTopologyException("getMacs: not found simple connection ["
                    + yBridge.getNodeId() + "]", simple);
        }

        if (xBridge.getNodeId().intValue() != simple.getFirstPort().getNodeId().intValue()) {
            throw new BridgeTopologyException("getMacs: node mismatch ["
                    + xBridge.getNodeId() + "] found " , simple.getFirstPort());
        }

        if (yBridge.getNodeId().intValue() != simple.getSecondPort().getNodeId().intValue()) {
            throw new BridgeTopologyException("getMacs: node mismatch ["
                    + yBridge.getNodeId() + "]", simple.getSecondPort());
        }

        Set<String> macsOnSegment = xBridge.getBridgePortWithMacs(simple.getFirstPort()).getMacs();
        macsOnSegment.retainAll(yBridge.getBridgePortWithMacs(simple.getSecondPort()).getMacs());

        return macsOnSegment;
    }

    public static Set<BridgePortWithMacs> getThroughSet(BridgeForwardingTable bridgeFt, Set<BridgePort> excluded) throws BridgeTopologyException {

        for (BridgePort exclude: excluded) {
            if (exclude.getNodeId().intValue() != bridgeFt.getNodeId().intValue()) {
                throw new BridgeTopologyException("getThroughSet: node mismatch ["
                        + bridgeFt.getNodeId() + "]", exclude);
            }
        }
        Set<BridgePortWithMacs> throughSet = new HashSet<>();
        bridgeFt.getPorttomac().stream().filter(ptm -> !excluded.contains(ptm.getPort())).forEach(throughSet::add);
        return throughSet;
    }

    public static BridgeForwardingTable create(Bridge bridge, Set<BridgeForwardingTableEntry> entries) throws BridgeTopologyException {
        if (bridge == null) {
            throw new BridgeTopologyException("bridge must not be null");
        }
        if (entries == null) {
            throw new BridgeTopologyException("bridge forwarding table must not be null");
        }

        for (BridgeForwardingTableEntry link: entries) {
            if (link.getNodeId().intValue() != bridge.getNodeId().intValue()) {
                throw new BridgeTopologyException("create: bridge:[" + bridge.getNodeId() + "] and forwarding table must have the same nodeid", link);
            }
        }
        final BridgeForwardingTable bridgeFt = new BridgeForwardingTable(bridge,entries);

        entries.stream().filter(link -> link.getBridgeDot1qTpFdbStatus()
                                == BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF).
                                forEach(link -> {
            bridgeFt.getIdentifiers().add(link.getMacAddress());
            if (LOG.isDebugEnabled()) {
                LOG.debug("create: bridge:[{}] adding bid {}",
                          bridge.getNodeId(),
                          link.printTopology());
            }
        });

        for (BridgeForwardingTableEntry link : entries) {
            if (link.getBridgeDot1qTpFdbStatus()
                                != BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED ) {
                continue;
            }
            if (bridgeFt.getIdentifiers().contains(link.getMacAddress())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] skip bid {}",
                          bridge.getNodeId(),
                          link.printTopology());
                }
                continue;
            }

            BridgePort bridgeport = getFromBridgeForwardingTableEntry(link);

            BridgePortWithMacs bpwm = bridgeFt.getBridgePortWithMacs(bridgeport);
            if (bpwm == null ) {
                bridgeFt.getPorttomac().add(new BridgePortWithMacs(bridgeport, new HashSet<>()));
            }
            bridgeFt.getBridgePortWithMacs(bridgeport).getMacs().add(link.getMacAddress());

            if (bridgeFt.getMactoport().containsKey(link.getMacAddress())) {
                bridgeFt.getDuplicated().put(link.getMacAddress(), new HashSet<>());
                bridgeFt.getDuplicated().get(link.getMacAddress()).add(bridgeport);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] duplicated {}",
                              bridge.getNodeId(),
                              link.printTopology());
                }
                continue;
            }

            if (LOG.isDebugEnabled()) {
                    LOG.debug("create: bridge:[{}] adding {}",
                          bridge.getNodeId(),
                          link.printTopology());
            }
            bridgeFt.getMactoport().put(link.getMacAddress(), bridgeport);
        }

        for (String mac : bridgeFt.getDuplicated().keySet()) {
            BridgePort saved = bridgeFt.getMactoport().remove(mac);
            if (LOG.isDebugEnabled()) {
                LOG.debug("create: bridge:[{}] remove duplicated [{}] from {}",
                          bridge.getNodeId(),
                          mac,
                          saved.printTopology());
            }

            BridgePortWithMacs savedwithmacs = bridgeFt.getBridgePortWithMacs(saved);
            savedwithmacs.getMacs().remove(mac);

            for (BridgePort dupli: bridgeFt.getDuplicated().get(mac)) {
                BridgePortWithMacs dupliwithmacs = bridgeFt.getBridgePortWithMacs(dupli);
                dupliwithmacs.getMacs().remove(mac);
            }
            bridgeFt.getDuplicated().get(mac).add(saved);
        }

        return bridgeFt;
    }

    public static BridgePort getFromBridgeForwardingTableEntry(BridgeForwardingTableEntry link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNodeId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    public static Bridge create(BroadcastDomain domain, Integer nodeid) {
        Bridge bridge = new Bridge(nodeid);
        domain.getBridges().add(bridge);
        return bridge;
    }

    public static Set<BridgeForwardingTableEntry> calculateBFT(
            BroadcastDomain domain, Bridge bridge)
            throws BridgeTopologyException {

        if ( domain == null ) {
            throw new BridgeTopologyException("calculateBFT: domain cannot be null");
        }

        if ( bridge == null ) {
            throw new BridgeTopologyException("calculateBFT: bridge cannot be null", domain);
        }
        Integer bridgeId = bridge.getNodeId();
        if ( bridgeId == null ) {
            throw new BridgeTopologyException("calculateBFT: bridge Id cannot be null", bridge);
        }
        Map<Integer, Set<String>> bft = new HashMap<>();
        Map<Integer, BridgePort> portifindexmap = new HashMap<>();

        Map<Integer,Integer> upperForwardingBridgePorts = getUpperForwardingBridgePorts(domain, bridge, new HashMap<>(),0);

        Map<Integer,Integer> bridgeIdtobridgePortOnBridge = new HashMap<>();

        for (Integer upperbridgeid: upperForwardingBridgePorts.keySet()) {
            bridgeIdtobridgePortOnBridge.put(upperbridgeid, bridge.getRootPort());
        }

        //
        for (SharedSegment segment : domain.getSharedSegments()) {

           Integer bridgeport;

            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) {
                BridgePort bport = segment.getBridgePort(bridgeId);
                portifindexmap.put(bport.getBridgePort(), bport);
                bridgeport = bport.getBridgePort();
            } else {
                bridgeport = getCalculateBFT(domain, segment, bridge, bridgeIdtobridgePortOnBridge, new HashSet<>(),0);
            }

            if (!bft.containsKey(bridgeport)) {
                bft.put(bridgeport, new HashSet<>());
            }
            bft.get(bridgeport).addAll(segment.getMacsOnSegment());
        }

        List<BridgePortWithMacs> links = new ArrayList<>(domain.getForwarders(bridgeId));

        for (Integer bridgePort : bft.keySet()) {
            links.add(new BridgePortWithMacs(portifindexmap.get(bridgePort),bft.get(bridgePort)));
        }

        Set<BridgeForwardingTableEntry> entries= new HashSet<>();
        links.stream().filter(bfti -> bfti.getMacs().size() > 0).forEach(bfti -> entries.addAll(bfti.getBridgeForwardingTableEntrySet()));
        return entries;
    }

    public static Bridge electRootBridge(BroadcastDomain domain) throws BridgeTopologyException {
        if (domain.getBridges().size() == 1) {
            return domain.getBridges().iterator().next();
        }
        //well only one root bridge should be defined....
        //otherwise we need to skip calculation
        //so here is the place were we can
        //manage multi stp domains...
        //ignoring for the moment....
        for (Bridge electable: domain.getBridges()) {
            if (electable.getDesignated() != null) {
                return getUpperBridge(domain,electable,0);
            }
        }
        return null;
    }

    public static Bridge getUpperBridge(BroadcastDomain domain, Bridge electableroot, int level) throws BridgeTopologyException {
        if (level == BroadcastDomain.maxlevel) {
            throw new BridgeTopologyException("getUpperBridge, too many iterations", electableroot);
        }
        for (Bridge electable: domain.getBridges()) {
            if (electable.getIdentifiers().contains(electableroot.getDesignated())) {
                return getUpperBridge(domain,electable, ++level);
            }
        }
        return electableroot;
    }

    public static Map<Integer,Integer> getUpperForwardingBridgePorts(BroadcastDomain domain, Bridge bridge, Map<Integer,Integer> downports, int level) throws BridgeTopologyException {
        if (level == BroadcastDomain.maxlevel) {
            throw new BridgeTopologyException("getUpperForwardingBridgePorts: too many iteration", bridge);
        }

        if (bridge.isRootBridge()) {
            return downports;
        }

        SharedSegment upSegment = domain.getSharedSegment(bridge.getNodeId(), bridge.getRootPort());
        if (upSegment == null) {
            throw new BridgeTopologyException("getUpperForwardingBridgePorts: no up segment", bridge);
        }

        Bridge upBridge = domain.getBridge(upSegment.getDesignatedBridge());
        if (upBridge == null) {
            throw new BridgeTopologyException("getUpperForwardingBridgePorts: no designated bridge on segment", bridge);
        }
        BridgePort bp = upSegment.getBridgePort(upBridge.getNodeId());
        downports.put(bp.getNodeId(),bp.getBridgePort());
        return getUpperForwardingBridgePorts(domain, upBridge, downports, ++level);
    }

    public static Integer getCalculateBFT(BroadcastDomain domain, SharedSegment segment, Bridge bridge, Map<Integer,Integer> bridgetobridgeport, Set<Integer> downBridgeIds, int level) throws BridgeTopologyException {
        if (level == BroadcastDomain.maxlevel) {
            throw new BridgeTopologyException("getCalculateBFT: too many iteration", domain);
        }

        for (Integer bridgeIdOnsegment: segment.getBridgeIdsOnSegment()) {
            if (bridgetobridgeport.containsKey(bridgeIdOnsegment)) {
                Integer bridgeport = bridgetobridgeport.get(bridgeIdOnsegment);
                for (Integer bridgeidonsegment: downBridgeIds) {
                    bridgetobridgeport.put(bridgeidonsegment, bridgeport);
                }

                return bridgeport;
            }
        }
        // if segment is on the bridge then...
        Integer upBridgeId = segment.getDesignatedBridge();

        if (upBridgeId.intValue() == bridge.getNodeId().intValue()) {
            for (Integer bridgeidonsegment: downBridgeIds) {
                bridgetobridgeport.put(bridgeidonsegment, segment.getDesignatedPort().getBridgePort());
            }
            return segment.getDesignatedPort().getBridgePort();
        }
        // if segment is a root segment add mac on port
        if (upBridgeId.intValue() == domain.getRootBridge().getNodeId().intValue()) {
            for (Integer bridgeidonsegment : downBridgeIds) {
                bridgetobridgeport.put(bridgeidonsegment, bridge.getRootPort());
            }
            return bridge.getRootPort();
        }

        downBridgeIds.addAll(segment.getBridgeIdsOnSegment());

        Bridge upBridge = null;
        for (Bridge cbridge: domain.getBridges()) {
            if (cbridge.getNodeId().intValue() == bridge.getNodeId().intValue())
                continue;
            if (cbridge.getNodeId().intValue() == upBridgeId.intValue()) {
                upBridge = cbridge;
                break;
            }
        }
        if (upBridge == null) {
            throw new BridgeTopologyException("getCalculateBFT: cannot find up bridge on domain", domain);
        }
        SharedSegment up = domain.getSharedSegment(upBridge.getNodeId(), upBridge.getRootPort());
        if (up == null) {
            throw new BridgeTopologyException("getCalculateBFT: cannot find up segment on domain", domain);
        }

        return getCalculateBFT(domain,up, bridge,bridgetobridgeport,downBridgeIds, ++level);
    }

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
            create(m_domain, bridgeid);
        }
        try {
            m_bridgeFtMapUpdate.put(bridgeid, create(m_domain.getBridge(bridgeid), notYetParsedBFT));
        } catch (BridgeTopologyException e) {
            LOG.warn("calculate:  node[{}], {}, topology:\n{}", 
                      bridgeid,
                      e.getMessage(),
                      e.printTopology(),
                      e);
        }
    }

    public DiscoveryBridgeTopology(BroadcastDomain domain) {
        Assert.notNull(domain);
        m_domain=domain;
    }
        
    public String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getName());
        if (m_domain != null) {
            info.append(" domain nodes: ");
            info.append(m_domain.getBridgeNodesOnDomain());
        }
        info.append(", updated bft nodes: ");
        info.append(m_bridgeFtMapUpdate.keySet());
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
        Bridge electedRoot = electRootBridge(m_domain);
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
                        forEach(m_domain::add);
            LOG.debug("calculate: bridge:[{}] elected [root] is first:{}", 
                      rootBft.getNodeId(),
                 m_domain.getBridgeNodesOnDomain());
            return;
        } 
        BridgeForwardingTable oldRootBft = bridgeFtMapCalcul.get(m_domain.getRootBridge().getNodeId());
        BridgeSimpleConnection sp = BridgeSimpleConnection.create(oldRootBft, rootBft);
        sp.findSimpleConnection();
        rootBft.setRootPort(sp.getSecondBridgePort());
        down(oldRootBft,rootBft,sp,bridgeFtMapCalcul,0);
    }

    public  void calculate() {
        Assert.notNull(m_bridgeFtMapUpdate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: domain\n{}", 
                      m_domain.printTopology());
        }
        m_parsed = new HashSet<>();
        m_failed = new HashSet<>();
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
        
        Map<Integer, BridgeForwardingTable> bridgeFtMapCalcul = new HashMap<>();
        
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

                m_domain.clearTopologyForBridge(bridgeId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: bridge:[{}] cleaned ->\n{}",
                              bridgeId,
                              m_domain.printTopology());
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
                                          create(bridge,
                                                                       calculateBFT(m_domain,
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
        
        if (m_domain.getRootBridge() != null && !Objects.equals(m_domain.getRootBridge().getNodeId(), electedRoot.getNodeId())) {
            m_domain.hierarchySetUp(electedRoot);
            LOG.debug("calculate: bridge:[{}] elected is new [root] ->\n{}",
                      electedRoot.getNodeId(),
                      m_domain.printTopology());
        }
        
        Set<Integer> postprocessing = new HashSet<>();
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
            BridgeSimpleConnection upsimpleconn = BridgeSimpleConnection.create(rootBft, bridgeFT);
            try {
                  upsimpleconn.findSimpleConnection();
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

        for (Integer failedbridgeid: new HashSet<>(m_failed)) {
            if (failedbridgeid == null) {
                LOG.error("calculate: bridge:[null], first iteration on failed");
                continue;
            }
            BridgeForwardingTable failedBridgeFT = m_bridgeFtMapUpdate.get(failedbridgeid);
            if (failedBridgeFT == null) {
                LOG.error("calculate: bridge:[{}], first iteration on failed. FT is null",failedbridgeid);
                continue;
            }
            try {
                postprocess(failedBridgeFT, rootBft,bridgeFtMapCalcul, new HashSet<>(m_parsed));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], first iteration on failed. no topology found. {}, \n{}", failedbridgeid, e.getMessage(),e.printTopology());
                continue;
            }
            m_parsed.add(failedbridgeid);
            m_failed.remove(failedbridgeid);
        }        

        for (Integer failedbridgeid: new HashSet<>(m_failed)) {
            if (failedbridgeid == null) {
                LOG.error("calculate: bridge:[null], second iteration on failed");
                continue;
            }
            BridgeForwardingTable failedBridgeFT = m_bridgeFtMapUpdate.get(failedbridgeid);
            if (failedBridgeFT == null) {
                LOG.error("calculate: bridge:[{}], second iteration on failed. FT is null",failedbridgeid);
                continue;
            }
             try {
                postprocess(failedBridgeFT,rootBft, bridgeFtMapCalcul, new HashSet<>(m_parsed));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], second iteration on failed. no topology found. {}, \n{}", failedbridgeid, e.getMessage(),e.printTopology());
                continue;
            }
            m_parsed.add(failedbridgeid);
            m_failed.remove(failedbridgeid);
        }        

        for (Integer postprocessbridgeid: new HashSet<>(postprocessing)) {
            if (postprocessbridgeid == null) {
                LOG.error("calculate: bridge:[null], postprocessbridge");
                continue;
            }            
            BridgeForwardingTable postprocessBridgeFT = m_bridgeFtMapUpdate.get(postprocessbridgeid);
            if (postprocessBridgeFT == null) {
                LOG.error("calculate: bridge:[{}],postprocessbridge. FT is null",postprocessbridgeid);
                continue;
            }
            BridgeSimpleConnection simpleConnection = BridgeSimpleConnection.create(rootBft, postprocessBridgeFT);
            try {
                simpleConnection.findSimpleConnection();
                down(rootBft, postprocessBridgeFT, simpleConnection, bridgeFtMapCalcul,
                     0);
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], postprocessbridge. No topology found for single port node. {}, \n{}", postprocessbridgeid, e.getMessage(),e.printTopology());
                m_failed.add(postprocessbridgeid);
                continue;
            }
            m_parsed.add(postprocessbridgeid);
        }        

        for (Integer failedbridgeid: new HashSet<>(m_failed)) {
            if (failedbridgeid == null) {
                LOG.error("calculate: bridge:[null], third iteration on failed");
                continue;
            }
            BridgeForwardingTable failedBridgeFT = m_bridgeFtMapUpdate.get(failedbridgeid);
            if (failedBridgeFT == null) {
                LOG.error("calculate: bridge:[{}], third iteration on failed. FT is null",failedbridgeid);
                continue;
            }
            try {
                postprocess(failedBridgeFT,rootBft, bridgeFtMapCalcul, new HashSet<>(m_parsed));
            } catch (BridgeTopologyException e) {
                LOG.warn("calculate: bridge:[{}], third iteration on failed. no topology found. {}, \n{}", failedbridgeid, e.getMessage(),e.printTopology());
                continue;
            }
            m_parsed.add(failedbridgeid);
            m_failed.remove(failedbridgeid);
        }        

        m_bridgeFtMapUpdate.values().stream().
            filter(ft -> m_parsed.contains(ft.getNodeId())).
                forEach(m_domain::addforwarders);
        
        bridgeFtMapCalcul.values().
            forEach(m_domain::addforwarders);

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
            
            BridgeSimpleConnection sp = BridgeSimpleConnection.create(parsedBridgeFT,
                    postBridgeFT);

            try {
                sp.findSimpleConnection();
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
                    LOG.debug("postprocess: bridge:[{}] <--> bridge:[{}] topology found.",
                              postbridgeid, parsedbridgeid);
                }
                return;
            }
        }
        BridgeSimpleConnection simpleConnection = BridgeSimpleConnection.create(rootBridgeFT, postBridgeFT);
        try {
            simpleConnection.findSimpleConnection();
            down(rootBridgeFT, postBridgeFT, simpleConnection, bridgeFtMapCalcul,
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

        Set<BridgePort> parsed = new HashSet<>();
        parsed.add(bridgeFT.getRootPort());

        Set<BridgeForwardingTable> checkforwarders = new HashSet<>();
        checkforwarders.add(bridgeUpFT);
        checkforwarders.add(bridgeFT);

        Map<BridgePortWithMacs, Set<BridgePortWithMacs>> splitted 
            = new HashMap<>();
        
        BridgeForwardingTable nextDownBridge = null;
        BridgeSimpleConnection nextDownSP = null;
        boolean levelfound = false;
        
        Set<String> maconupsegment = getMacs(bridgeUpFT, bridgeFT, upsimpleconn);
        
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
                    BridgeSimpleConnection.create(curBridgeFT,
                                       bridgeFT);
            simpleconn.findSimpleConnection();
            if (LOG.isDebugEnabled()) {
                LOG.debug("down: level: {}, bridge:[{}]. {}", 
                         level,
                         bridgeFT.getNodeId(),
                         simpleconn.printTopology());
            }
            if (!Objects.equals(simpleconn.getSecondBridgePort(), bridgeFT.getBridge().getRootPort())
                && !Objects.equals(simpleconn.getFirstBridgePort(), curbridge.getRootPort())) {
                throw new BridgeTopologyException(
                              "down: level: " + level +", bridge:["+bridgeFT.getNodeId()+"], Topology mismatch. NO ROOTS");
            }

            // bridge is a leaf of curbridge
            if (Objects.equals(simpleconn.getSecondBridgePort(), bridgeFT.getRootBridgePort())
                    &&
                    !Objects.equals(simpleconn.getFirstBridgePort(), curbridge.getRootPort())) {
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
            if (Objects.equals(simpleconn.getFirstBridgePort(), curBridgeFT.getRootBridgePort())
                    && !Objects.equals(simpleconn.getSecondBridgePort(), bridgeFT.getRootBridgePort())) {
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
                            new HashSet<>());
                }
                splitted.get(bridgeFT.getBridgePortWithMacs(simpleconn.getSecondPort())).
                    add(curBridgeFT.getBridgePortWithMacs(simpleconn.getFirstPort()));
                parsed.add(simpleconn.getSecondPort());
                continue;
            }
            //here are all the simple connection in which the connection is the root port
            maconupsegment.retainAll(getMacs(curBridgeFT, bridgeFT, simpleconn));
        } // end of loop on up segment bridges
        
        if (nextDownBridge != null) {
            down(nextDownBridge, bridgeFT, nextDownSP,bridgeFtMapCalcul,level);
            return;
        }

        m_domain.merge(     upSegment,
                            splitted,
                            maconupsegment,
                            bridgeFT.getRootPort(),
                            getThroughSet(bridgeFT, parsed));
        checkforwarders.forEach(m_domain::addforwarders);
    }
    
}

