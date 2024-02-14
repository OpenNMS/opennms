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
package org.opennms.netmgt.enlinkd.service.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeMacLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeStpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.service.api.Bridge;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
import org.opennms.netmgt.enlinkd.service.api.TopologyShared;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class BridgeTopologyServiceImpl extends TopologyServiceImpl implements BridgeTopologyService {
    
    private final static Logger LOG = LoggerFactory.getLogger(BridgeTopologyServiceImpl.class);
    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private final Object lock = new Object();
    private BridgeElementDao m_bridgeElementDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private BridgeStpLinkDao m_bridgeStpLinkDao;
    private IpNetToMediaDao m_ipNetToMediaDao;

    private final Map<Integer, Set<BridgeForwardingTableEntry>> m_nodetoBroadcastDomainMap = new HashMap<>();
    private final Set<Integer> m_bridgecollectionsscheduled = new HashSet<>();
    volatile Set<BroadcastDomain> m_domains;

    private MacPort acreate(IpNetToMedia media) {

        Set<InetAddress> ips = new HashSet<>();
        ips.add(media.getNetAddress());

        MacPort port = new MacPort();
        port.setNodeId(media.getNodeId());
        port.setIfIndex(media.getIfIndex());
        port.setMacPortName(media.getPort());
        port.getMacPortMap().put(media.getPhysAddress(), ips);
        return port;
    }

    @Override
    public String getBridgeDesignatedIdentifier(Bridge bridge) {
        for (BridgeElement element : m_bridgeElementDao.findByNodeId(bridge.getNodeId())) {
            if (InetAddressUtils.
                    isValidStpBridgeId(element.getStpDesignatedRoot())
                    && !element.getBaseBridgeAddress().
                    equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
                String designated=InetAddressUtils.
                               getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot());
                if (InetAddressUtils.isValidBridgeAddress(designated)) {
                    return designated;
                }
            }
        }
        return null;
    }

    @Override
    public Set<String> getBridgeIdentifiers(Bridge bridge) {
        Set<String> identifiers = new HashSet<>();
        for (BridgeElement element : m_bridgeElementDao.findByNodeId(bridge.getNodeId())) {
            if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress())) {
                identifiers.add(element.getBaseBridgeAddress());
            }
        }
        return identifiers;
    }

    public BridgeElementDao getBridgeElementDao() {
        return m_bridgeElementDao;
    }

    public void setBridgeElementDao(BridgeElementDao bridgeElementDao) {
        m_bridgeElementDao = bridgeElementDao;
    }

    public BridgeBridgeLinkDao getBridgeBridgeLinkDao() {
        return m_bridgeBridgeLinkDao;
    }

    public void setBridgeBridgeLinkDao(BridgeBridgeLinkDao bridgeBridgeLinkDao) {
        m_bridgeBridgeLinkDao = bridgeBridgeLinkDao;
    }

    public BridgeMacLinkDao getBridgeMacLinkDao() {
        return m_bridgeMacLinkDao;
    }

    public void setBridgeMacLinkDao(BridgeMacLinkDao bridgeMacLinkDao) {
        m_bridgeMacLinkDao = bridgeMacLinkDao;
    }
    
    @Override
    public void store(BroadcastDomain domain, Date now) {
        for (SharedSegment segment : domain.getSharedSegments()) {
            segment.getDesignatedPort();
        }
        for (SharedSegment segment : domain.getSharedSegments()) {
            for (BridgeBridgeLink link : segment.getBridgeBridgeLinks()) {
                link.setBridgeBridgeLinkLastPollTime(new Date());
                    saveBridgeBridgeLink(link);
            }
            for (BridgeMacLink link : segment.getBridgeMacLinks()) {
                link.setBridgeMacLinkLastPollTime(new Date());
                saveBridgeMacLink(link);
            }
        }
        
        domain.getForwarding().stream().filter(forward -> forward.getMacs().size() > 0).
            forEach( forward -> {
                for ( BridgeMacLink link : forward.getBridgeMacLinks()) {
                    link.setBridgeMacLinkLastPollTime(new Date());
                    saveBridgeMacLink(link);
                }
            });
        
        for (Integer nodeid: domain.getBridgeNodesOnDomain()) {
            m_bridgeMacLinkDao.deleteByNodeIdOlderThen(nodeid, now);
            m_bridgeBridgeLinkDao.deleteByNodeIdOlderThen(nodeid, now);
            m_bridgeBridgeLinkDao.deleteByDesignatedNodeIdOlderThen(nodeid, now);
        }
       
        try {
            m_bridgeMacLinkDao.flush();
        } catch (Exception e) {
            LOG.error("BridgeMacLinkDao: {}", e.getMessage(),e );
        }
        try {
            m_bridgeBridgeLinkDao.flush();
        } catch (Exception e) {
            LOG.error("BridgeBridgeLinkDao: {}", e.getMessage(),e );
        }

        updatesAvailable();
    }

    @Transactional
    protected void saveBridgeMacLink(final BridgeMacLink saveMe) {
        new UpsertTemplate<>(
                m_transactionManager,
                m_bridgeMacLinkDao) {

            @Override
            protected BridgeMacLink query() {
                return m_dao.getByNodeIdBridgePortMac(saveMe.getNode().getId(),
                        saveMe.getBridgePort(),
                        saveMe.getMacAddress());
            }

            @Override
            protected BridgeMacLink doUpdate(BridgeMacLink link) {
                link.merge(saveMe);
                m_dao.update(link);
                m_dao.flush();
                return link;
            }

            @Override
            protected BridgeMacLink doInsert() {
                if (saveMe.getBridgeMacLinkLastPollTime() == null)
                    saveMe.setBridgeMacLinkLastPollTime(saveMe.getBridgeMacLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Transactional
    protected void saveBridgeBridgeLink(final BridgeBridgeLink saveMe) {
        new UpsertTemplate<>(
                m_transactionManager,
                m_bridgeBridgeLinkDao) {

            @Override
            protected BridgeBridgeLink query() {
                return m_dao.getByNodeIdBridgePort(saveMe.getNode().getId(),
                        saveMe.getBridgePort());
            }

            @Override
            protected BridgeBridgeLink doUpdate(BridgeBridgeLink link) {
                link.merge(saveMe);
                m_dao.update(link);
                m_dao.flush();
                return link;
            }

            @Override
            protected BridgeBridgeLink doInsert() {
                if (saveMe.getBridgeBridgeLinkLastPollTime() == null)
                    saveMe.setBridgeBridgeLinkLastPollTime(saveMe.getBridgeBridgeLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void load() {
        synchronized (lock) {
            m_domains = getAllPersisted();
            for (BroadcastDomain domain : m_domains) {
                for (Bridge bridge : domain.getBridges()) {
                    bridge.clear();
                    bridge.getIdentifiers().addAll(getBridgeIdentifiers(bridge));
                    bridge.setDesignated(getBridgeDesignatedIdentifier(bridge));
                }
            }
        }
    }

    @Override
    public void delete(int nodeid) {
        m_bridgeElementDao.deleteByNodeId(nodeid);
        m_bridgeElementDao.flush();

        m_bridgeStpLinkDao.deleteByNodeId(nodeid);
        m_bridgeStpLinkDao.flush();

        reconcile(getBroadcastDomain(nodeid), nodeid);
        m_bridgeBridgeLinkDao.deleteByDesignatedNodeId(nodeid);
        m_bridgeBridgeLinkDao.deleteByNodeId(nodeid);
        m_bridgeBridgeLinkDao.flush();

        m_bridgeMacLinkDao.deleteByNodeId(nodeid);
        m_bridgeMacLinkDao.flush();


    }
    
    @Override
    public BroadcastDomain getBroadcastDomain(int nodeId) {
        synchronized (lock) {
            for (BroadcastDomain domain : m_domains) {
                Bridge bridge = domain.getBridge(nodeId);
                if (bridge != null) {
                    return domain;
                }
            }
        }
        return null;
    }

    @Override
    public BroadcastDomain reconcile(BroadcastDomain domain, int nodeId) {
        
        Date now = new Date();
        if (domain == null || domain.isEmpty()) {
            LOG.warn("reconcileTopologyForDeleteNode: node: {}, start: null domain or empty",nodeId);
            return domain;
        }
        if (domain.getBridge(nodeId) == null) {
            LOG.info("reconcileTopologyForDeleteNode: node: {}, not on domain",nodeId);
            return domain;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("reconcileTopologyForDeleteNode: node:[{}], domain:\n{}", nodeId, domain.printTopology());
        }
        
        LOG.info("reconcileTopologyForDeleteNode: node:[{}], start: save topology for domain",nodeId);
        domain.removeBridge(nodeId);
        store(domain,now);
        LOG.info("reconcileTopologyForDeleteNode: node:[{}], end: save topology for domain",nodeId);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("reconcileTopologyForDeleteNode: node:[{}], resulting domain: {}", nodeId, domain.printTopology());
        }

        if (domain.getRootBridge() == null) {
            LOG.info("reconcileTopologyForDeleteNode: {}, domain without root", domain);
        }

        if (domain.isEmpty()) {
            cleanBroadcastDomains();
            LOG.info("reconcileTopologyForDeleteNode: node:[{}], empty domain",nodeId);
        }
        return domain;
    }

    public void cleanBroadcastDomains() {
        synchronized (lock) {
            m_domains.removeIf(BroadcastDomain::isEmpty);
        }
    }

    @Override
    public synchronized void updateBridgeOnDomain(BroadcastDomain domain, Integer nodeId) {
        if (domain == null) {
            return;
        }
        for (Bridge bridge: domain.getBridges()) {
            if (bridge.getNodeId().intValue() == nodeId.intValue()) {
                bridge.clear();
                bridge.getIdentifiers().addAll(getBridgeIdentifiers(bridge));
                bridge.setDesignated(getBridgeDesignatedIdentifier(bridge));
                break;
            }
        }
    }

    @Override
    public void store(int nodeId, BridgeElement bridge) {
        if (bridge == null)
            return;
        saveBridgeElement(nodeId, bridge);
        updatesAvailable();
    }

    @Transactional
    protected void saveBridgeElement(final int nodeId,
            final BridgeElement saveMe) {
        new UpsertTemplate<>(
                m_transactionManager,
                m_bridgeElementDao) {

            @Override
            protected BridgeElement query() {
                return m_dao.getByNodeIdVlan(nodeId, saveMe.getVlan());
            }

            @Override
            protected BridgeElement doUpdate(BridgeElement bridge) {
                bridge.merge(saveMe);
                m_dao.update(bridge);
                m_dao.flush();
                return bridge;
            }

            @Override
            protected BridgeElement doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setBridgeNodeLastPollTime(saveMe.getBridgeNodeCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void store(int nodeId, BridgeStpLink link) {
        if (link == null)
            return;
        saveBridgeStpLink(nodeId, link);
    }

    @Transactional
    protected void saveBridgeStpLink(final int nodeId,
            final BridgeStpLink saveMe) {
        new UpsertTemplate<>(
                m_transactionManager,
                m_bridgeStpLinkDao) {

            @Override
            protected BridgeStpLink query() {
                return m_dao.getByNodeIdBridgePort(nodeId,
                        saveMe.getStpPort());
            }

            @Override
            protected BridgeStpLink doUpdate(BridgeStpLink link) {
                link.merge(saveMe);
                m_dao.update(link);
                m_dao.flush();
                return link;
            }

            @Override
            protected BridgeStpLink doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setBridgeStpLinkLastPollTime(saveMe.getBridgeStpLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void store(int nodeId, List<BridgeForwardingTableEntry> bft) {
        Set<BridgeForwardingTableEntry> effectiveBFT= new HashSet<>();
        for (BridgeForwardingTableEntry link : bft) {
            link.setNodeId(nodeId);
            effectiveBFT.add(link);
        }
        synchronized (m_nodetoBroadcastDomainMap) {
            m_nodetoBroadcastDomainMap.put(nodeId, effectiveBFT);
        }
    }

    public Map<Integer,Set<BridgeForwardingTableEntry>> getUpdateBftMap() {
        synchronized (m_nodetoBroadcastDomainMap) {
            return m_nodetoBroadcastDomainMap;
        }
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        m_bridgeElementDao.deleteByNodeIdOlderThen(nodeId, now);
        m_bridgeElementDao.flush();

        m_bridgeStpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_bridgeStpLinkDao.flush();
    }

    @Override
    public Set<BroadcastDomain> findAll() {
        synchronized (lock) {
            return m_domains;
        }
    }
    
    @Override
    public void add(BroadcastDomain domain) {
        synchronized (lock) {
            m_domains.add(domain);
        }
    }
    
    @Override
    public Set<BridgeForwardingTableEntry> useBridgeTopologyUpdateBFT(int nodeid) {
        synchronized (m_nodetoBroadcastDomainMap) {
            return m_nodetoBroadcastDomainMap.remove(nodeid);
        }
    }

    @Override
    public List<SharedSegment> getSharedSegments(int nodeid) 
        {
        List<SharedSegment> segments = new ArrayList<>();

        BBLDESI: for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findByDesignatedNodeId(nodeid)) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(BridgeTopologyService.getBridgePortFromDesignatedBridgeBridgeLink(link))) {
                    segment.getBridgePortsOnSegment().add(BridgeTopologyService.getBridgePortFromBridgeBridgeLink(link));
                    continue BBLDESI;
                }
            }
            segments.add(BridgeTopologyService.createSharedSegmentFromBridgeBridgeLink(link));
        }

        Set<BridgePort> designated = new HashSet<>();
        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findByNodeId(nodeid)) {
            designated.add(BridgeTopologyService.getBridgePortFromDesignatedBridgeBridgeLink(link));
        }        
        
       for (BridgePort designatedport: designated) {
       BBL: for ( BridgeBridgeLink link : m_bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(designatedport.getNodeId(), 
                                                                                             designatedport.getBridgePort())) {
           for (SharedSegment segment : segments) {
               if (segment.containsPort(BridgeTopologyService.getBridgePortFromDesignatedBridgeBridgeLink(link))) {
                   segment.getBridgePortsOnSegment().add(BridgeTopologyService.getBridgePortFromBridgeBridgeLink(link));
                   continue BBL;
               }
           }
           segments.add(BridgeTopologyService.createSharedSegmentFromBridgeBridgeLink(link));
       }
       }

        MACLINK:for (BridgeMacLink link : m_bridgeMacLinkDao.findByNodeId(nodeid)) {

            if (link.getLinkType() == BridgeMacLinkType.BRIDGE_FORWARDER) {
                continue;
            }
            for (SharedSegment segment : segments) {
                if (segment.containsPort(BridgeTopologyService.getBridgePortFromBridgeMacLink(link))) {
                    segment.getMacsOnSegment().add(link.getMacAddress());
                    continue MACLINK;
                }
            }
            segments.add(BridgeTopologyService.createSharedSegmentFromBridgeMacLink(link));
        }
        return segments;
    }
    
    @Override
    public SharedSegment getSharedSegment(String mac) {

        LOG.debug("getHostNodeSharedSegment: founding segment for mac:{}", mac);

        final List<BridgeMacLink> links =  m_bridgeMacLinkDao.findByMacAddress(mac).
                stream().
                filter(maclink -> maclink.getLinkType() ==  BridgeMacLinkType.BRIDGE_LINK).
                collect(Collectors.toCollection(ArrayList::new));
        
        if (links.size() == 0 ) {
            LOG.info("getHostNodeSharedSegment: no segment found for mac:{}", mac);
            return new SharedSegment();
        }

        if (links.size() > 1 ) {
            LOG.error("getHostNodeSharedSegment: more then one segment for mac:{}", mac);
            return new SharedSegment();
        }

        BridgeMacLink link = links.iterator().next();

        SharedSegment segment = null;

        try {
            for (BridgeBridgeLink bblink: m_bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(link.getNode().getId(), link.getBridgePort())) {
                if (segment == null) {
                        segment = BridgeTopologyService.createSharedSegmentFromBridgeBridgeLink(bblink);
                } else {
                    segment.getBridgePortsOnSegment().add(BridgeTopologyService.getBridgePortFromBridgeBridgeLink(bblink));
                }
            }
        
            for (BridgeMacLink maclink :m_bridgeMacLinkDao.findByNodeIdBridgePort(link.getNode().getId(), link.getBridgePort())) {
                if (segment == null) {
                    segment = BridgeTopologyService.createSharedSegmentFromBridgeMacLink(maclink);
                } else {
                    segment.getMacsOnSegment().add(maclink.getMacAddress());
                }
            }
        } catch (Exception e) {
            LOG.error("getHostNodeSharedSegment: cannot create shared segment {} for mac {} ", e.getMessage(), mac,e);
            return new SharedSegment();
        }
 
        return segment;
    }

    public Set<BroadcastDomain> getAllPersisted() {

        Set<BroadcastDomain> domains = new CopyOnWriteArraySet<>();

        List<SharedSegment> bblsegments = new ArrayList<>();
        Map<Integer,Set<Integer>> rootnodetodomainnodemap = new HashMap<>();
        Map<Integer,BridgePort> designatebridgemap = new HashMap<>();

        //start bridge bridge link parsing
        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findAll()) {
            boolean  segmentnotfound = true;
            BridgePort bridgeport = BridgeTopologyService.getBridgePortFromBridgeBridgeLink(link);
            designatebridgemap.put(link.getNode().getId(), bridgeport);
            for (SharedSegment bblsegment : bblsegments) {
                if (bblsegment.containsPort(BridgeTopologyService.getBridgePortFromDesignatedBridgeBridgeLink(link))) {
                    bblsegment.getBridgePortsOnSegment().add(bridgeport);
                    segmentnotfound=false;
                    break;
                }
            }
            if (segmentnotfound)  {
                bblsegments.add(BridgeTopologyService.createSharedSegmentFromBridgeBridgeLink(link));
            }
            // set up domains
            if (rootnodetodomainnodemap.containsKey(link.getDesignatedNode().getId())) {
                rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).
                    add(link.getNode().getId());
                if (rootnodetodomainnodemap.containsKey(link.getNode().getId())) {
                    rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).addAll(
                                                                                         rootnodetodomainnodemap.remove(
                                                                                                                     link.getNode().getId()));
                }
            } else if (rootnodetodomainnodemap.containsKey(link.getNode().getId())) {
                Set<Integer> dependentsnode= rootnodetodomainnodemap.remove(link.getNode().getId());
                dependentsnode.add(link.getNode().getId());
                Integer rootdesignated=null;
                for (Integer rootid: rootnodetodomainnodemap.keySet()) {
                    if (rootnodetodomainnodemap.get(rootid).contains(link.getDesignatedNode().getId())) {
                        rootdesignated=rootid;
                        break;
                    }
                }
                if (rootdesignated != null) {
                    dependentsnode.add(link.getDesignatedNode().getId());
                    rootnodetodomainnodemap.get(rootdesignated).addAll(dependentsnode);
                } else {
                    rootnodetodomainnodemap.put(link.getDesignatedNode().getId(), dependentsnode);
                }
            } else {
                Integer rootdesignated=null;
                for (Integer rootid: rootnodetodomainnodemap.keySet()) {
                    if (rootnodetodomainnodemap.get(rootid).contains(link.getDesignatedNode().getId())) {
                        rootdesignated=rootid;
                        break;
                    }
                }
                if (rootdesignated != null) {
                    rootnodetodomainnodemap.get(rootdesignated).add(link.getNode().getId());
                } else {
                    rootnodetodomainnodemap.put(link.getDesignatedNode().getId(), new HashSet<>());
                    rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).add(link.getNode().getId());                
                }
            }
        }
        LOG.info("getAllPersisted: bridge topology node set: {}", rootnodetodomainnodemap );
        
        
        //end bridge bridge link parsing
        
        List<SharedSegment> bmlsegments = new ArrayList<>();
        List<BridgeMacLink> forwarders = new ArrayList<>();

BML:    for (BridgeMacLink link : m_bridgeMacLinkDao.findAll()) {
            if (link.getLinkType() == BridgeMacLinkType.BRIDGE_FORWARDER) {
                forwarders.add(link);
                continue;
            }
            
            for (SharedSegment bblsegment: bblsegments) {
                if (bblsegment.containsPort(BridgeTopologyService.getBridgePortFromBridgeMacLink(link))) {
                    bblsegment.getMacsOnSegment().add(link.getMacAddress());
                    continue BML;
                }
            }
            for (SharedSegment bmlsegment: bmlsegments) {
                if (bmlsegment.containsPort(BridgeTopologyService.getBridgePortFromBridgeMacLink(link))) {
                    bmlsegment.getMacsOnSegment().add(link.getMacAddress());
                    continue BML;
                }
            }
            bmlsegments.add(BridgeTopologyService.createSharedSegmentFromBridgeMacLink(link));
    }
                
        for (Integer rootNodeid : rootnodetodomainnodemap.keySet()) {
            BroadcastDomain domain = new BroadcastDomain();
            Bridge bridge = new Bridge(rootNodeid);
            bridge.setRootBridge();
            domain.getBridges().add(bridge);
            for (Integer nodeid : rootnodetodomainnodemap.get(rootNodeid)) {
                Bridge newbridge = new Bridge(nodeid);
                newbridge.setRootPort(designatebridgemap.get(nodeid).getBridgePort());
                domain.getBridges().add(newbridge);
            }
            domains.add(domain);
        }
        
        for (SharedSegment segment : bblsegments) {
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.loadTopologyEntry(segment)) {
                    break;
                }
            }
        }

SEG:        for (SharedSegment segment : bmlsegments) {
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.loadTopologyEntry(segment)) {
                    continue SEG;
                }
            }
            BroadcastDomain domain = new BroadcastDomain();
            Bridge bridge = new Bridge(segment.getDesignatedBridge());
            bridge.setRootBridge();
            domain.getBridges().add(bridge);
            domain.loadTopologyEntry(segment);
            domains.add(domain);
        }

        for (BridgeMacLink forwarder : forwarders) {
            for (BroadcastDomain domain: domains) {
                Bridge bridge = domain.getBridge(forwarder.getNode().getId());
                if (bridge != null) {
                    domain.addForwarding(BridgeTopologyService.getBridgePortFromBridgeMacLink(forwarder),forwarder.getMacAddress());
                    break;
                }
            }
        }

        return domains;
    }

    public boolean collectBft(int nodeid, int maxsize) {
        synchronized (m_bridgecollectionsscheduled) {
            if (getUpdateBftMap().size() + m_bridgecollectionsscheduled.size() >= maxsize)
                return false;
            synchronized (m_bridgecollectionsscheduled) {
                m_bridgecollectionsscheduled.add(nodeid);
            }
            return true;
        }
    }
    
    public void collectedBft(int nodeid) {
        synchronized (m_bridgecollectionsscheduled) {
            m_bridgecollectionsscheduled.remove(nodeid);
        }
    }

    public BridgeStpLinkDao getBridgeStpLinkDao() {
        return m_bridgeStpLinkDao;
    }

    public void setBridgeStpLinkDao(BridgeStpLinkDao bridgeStpLinkDao) {
        m_bridgeStpLinkDao = bridgeStpLinkDao;
    }

    @Override
    public List<MacPort> getMacPorts() {
        final Map<String,MacPort> macToMacPortMap = new HashMap<>();
        final Table<Integer, Integer, MacPort> nodeIfindexToMacPortTable = HashBasedTable.create();
        m_ipNetToMediaDao.
                findAll().forEach(m -> {
                    boolean merge = false;
                    MacPort macport=new MacPort();
                    Set<InetAddress> ips = new HashSet<>();
                    ips.add(m.getNetAddress());
                    macport.setNodeId(m.getNodeId());
                    macport.setIfIndex(m.getIfIndex());
                    macport.setMacPortName(m.getPort());
                    macport.getMacPortMap().put(m.getPhysAddress(), ips);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getMacPorts: parsing: {}",m);
                    }
                    if (m.getNode() != null ) {
                        if (nodeIfindexToMacPortTable.contains(m.getNode().getId(), m.getIfIndex())) {
                            macport = nodeIfindexToMacPortTable.get(m.getNode().getId(), m.getIfIndex());
                            merge = true;
                        } else {
                            nodeIfindexToMacPortTable.put(m.getNode().getId(), m.getIfIndex(), macport);
                        }
                    } else {
                        if (macToMacPortMap.containsKey(m.getPhysAddress())) {
                            macport=macToMacPortMap.get(m.getPhysAddress());
                            merge = true;
                        } else {
                            macToMacPortMap.put(m.getPhysAddress(), macport);
                        }
                    }
                    if (merge) {
                        if (!macport.getMacPortMap().containsKey(m.getPhysAddress())) {
                            macport.getMacPortMap().put(m.getPhysAddress(), new HashSet<>());
                        }
                        macport.getMacPortMap().get(m.getPhysAddress()).add(m.getNetAddress());
                    }
                });
       List<MacPort> ports = new ArrayList<>(nodeIfindexToMacPortTable.values());
       ports.forEach(mp -> mp.getMacPortMap().keySet().stream().filter(macToMacPortMap::containsKey).forEach(mac -> mp.getMacPortMap().get(mac).addAll(macToMacPortMap.remove(mac).getMacPortMap().get(mac))));
       ports.addAll(macToMacPortMap.values());
       return ports;
    }

    @Override
    public void deletePersistedData() {
        m_bridgeElementDao.deleteAll();
        m_bridgeElementDao.flush();

        m_bridgeMacLinkDao.deleteAll();
        m_bridgeMacLinkDao.flush();

        m_bridgeBridgeLinkDao.deleteAll();
        m_bridgeBridgeLinkDao.flush();

        m_bridgeStpLinkDao.deleteAll();
        m_bridgeStpLinkDao.flush();

        m_ipNetToMediaDao.deleteAll();
        m_ipNetToMediaDao.flush();
    }

    @Override
    public List<TopologyShared> match() {       
        final List<TopologyShared> links = new ArrayList<>();
        final List<MacPort> macPortMap = getMacPorts();
        synchronized (lock) {
            m_domains.forEach(dm -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("match: \n{}", dm.printTopology());
                }
                dm.getSharedSegments().forEach(shs -> links.add(TopologyService.of(shs, macPortMap.stream().filter(mp ->
                                shs.getMacsOnSegment().containsAll(mp.getMacPortMap().keySet())).
                        collect(Collectors.toList()))));
            });
        }
        return links;
    }

    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }
    
}
