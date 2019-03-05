/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeMacLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeStpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.service.api.Bridge;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
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
    
    private BridgeElementDao m_bridgeElementDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private BridgeStpLinkDao m_bridgeStpLinkDao;
    private IpNetToMediaDao m_ipNetToMediaDao;

    volatile Map<Integer, Set<BridgeForwardingTableEntry>> m_nodetoBroadcastDomainMap= new HashMap<Integer, Set<BridgeForwardingTableEntry>>();
    volatile Set<BroadcastDomain> m_domains;
    private volatile Set<Integer> m_bridgecollectionsscheduled = new HashSet<>();


    
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
    public void store(BroadcastDomain domain, Date now) throws BridgeTopologyException {
        for (SharedSegment segment : domain.getSharedSegments()) {
            segment.getDesignatedPort();
        }
        for (SharedSegment segment : domain.getSharedSegments()) {
            for (BridgeBridgeLink link : SharedSegment.getBridgeBridgeLinks(segment)) {
                link.setBridgeBridgeLinkLastPollTime(new Date());
                    saveBridgeBridgeLink(link);
            }
            for (BridgeMacLink link : SharedSegment.getBridgeMacLinks(segment)) {
                link.setBridgeMacLinkLastPollTime(new Date());
                saveBridgeMacLink(link);
            }
        }
        
        domain.getForwarding().stream().filter(forward -> forward.getMacs().size() > 0).
            forEach( forward -> {
                for ( BridgeMacLink link : BridgeForwardingTableEntry.create(forward, BridgeMacLinkType.BRIDGE_FORWARDER)) {
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
        new UpsertTemplate<BridgeMacLink, BridgeMacLinkDao>(
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
        new UpsertTemplate<BridgeBridgeLink, BridgeBridgeLinkDao>(
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
        m_domains=getAllPersisted();
        for (BroadcastDomain domain: m_domains) {
            for (Bridge bridge: domain.getBridges()) {
                bridge.clear();
                List<BridgeElement> elems = m_bridgeElementDao.findByNodeId(bridge.getNodeId());
                bridge.getIdentifiers().addAll(Bridge.getIdentifier(elems));
                bridge.setDesignated(Bridge.getDesignated(elems));
            }        
        }
    }

    @Override
    public void delete(int nodeid) throws BridgeTopologyException {
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
        synchronized (m_domains) {
            for (BroadcastDomain domain : m_domains) {
                synchronized (domain) {
                    Bridge bridge = domain.getBridge(nodeId);
                    if (bridge != null) {
                        return domain;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public BroadcastDomain reconcile(BroadcastDomain domain,int nodeId) throws BridgeTopologyException {
        
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
        BroadcastDomain.removeBridge(domain,nodeId);
        store(domain,now);
        LOG.info("reconcileTopologyForDeleteNode: node:[{}], end: save topology for domain",nodeId);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("reconcileTopologyForDeleteNode: node:[{}], resulting domain: {}", nodeId, domain.printTopology());
        }
        
        if (domain.isEmpty()) {
            cleanBroadcastDomains();
            LOG.info("reconcileTopologyForDeleteNode: node:[{}], empty domain",nodeId);
            return domain;
        }
        if (domain.getRootBridge() == null) {
            throw new BridgeTopologyException("reconcileTopologyForDeleteNode: Domain without root", domain);
        }
        return domain;
    }

    public void cleanBroadcastDomains() {
        synchronized (m_domains) {
            m_domains.removeIf(BroadcastDomain::isEmpty);
        }
    }

    @Override
    public void updateBridgeOnDomain(BroadcastDomain domain, Integer nodeId) {
        if (domain == null) {
            return;
        }
        synchronized (domain) {
            for (Bridge bridge: domain.getBridges()) {
                if (bridge.getNodeId().intValue() == nodeId.intValue()) {
                    bridge.clear();
                    List<BridgeElement> elems = m_bridgeElementDao.findByNodeId(nodeId);
                    bridge.getIdentifiers().addAll(Bridge.getIdentifier(elems));
                    bridge.setDesignated(Bridge.getDesignated(elems));
                    break;
                }
            }
        }
    }

    public List<BridgeElement> getBridgeElements(Set<Integer> nodes) {
        List<BridgeElement> elems = new ArrayList<BridgeElement>();
        for (Integer nodeid: nodes)
            elems.addAll(m_bridgeElementDao.findByNodeId(nodeid));
        return elems;
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
        new UpsertTemplate<BridgeElement, BridgeElementDao>(
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
        new UpsertTemplate<BridgeStpLink, BridgeStpLinkDao>(
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
        Set<BridgeForwardingTableEntry> effectiveBFT=new HashSet<BridgeForwardingTableEntry>(); 
        for (BridgeForwardingTableEntry link : bft) {
            link.setNodeId(nodeId);
            effectiveBFT.add(link);
        }
        synchronized (m_nodetoBroadcastDomainMap) {
            m_nodetoBroadcastDomainMap.put(nodeId, effectiveBFT);
        }
    }

    public synchronized Map<Integer,Set<BridgeForwardingTableEntry>> getUpdateBftMap() {
        return m_nodetoBroadcastDomainMap;
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        m_bridgeElementDao.deleteByNodeIdOlderThen(nodeId, now);
        m_bridgeElementDao.flush();

        m_bridgeStpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_bridgeStpLinkDao.flush();
    }

    @Override
    public synchronized Set<BroadcastDomain> findAll() {
        return m_domains;
    }
    
    @Override
    public void add(BroadcastDomain domain) {
        synchronized (m_domains) {
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
        List<SharedSegment> segments = new ArrayList<SharedSegment>();

        BBLDESI: for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findByDesignatedNodeId(nodeid)) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(BridgePort.getFromDesignatedBridgeBridgeLink(link))) {
                    segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
                    continue BBLDESI;
                }
            }
            try {
                segments.add(SharedSegment.create(link));
            } catch (BridgeTopologyException e) {
                LOG.error("getBridgeNodeSharedSegments: cannot create shared segment {}", 
                          e.getMessage(),
                          e);
                return new ArrayList<SharedSegment>();
            }
        }

        Set<BridgePort> designated = new HashSet<BridgePort>();
        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findByNodeId(nodeid)) {
            designated.add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
        }        
        
       for (BridgePort designatedport: designated) {
       BBL: for ( BridgeBridgeLink link : m_bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(designatedport.getNodeId(), 
                                                                                             designatedport.getBridgePort())) {
           for (SharedSegment segment : segments) {
               if (segment.containsPort(BridgePort.getFromDesignatedBridgeBridgeLink(link))) {
                   segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
                   continue BBL;
               }
           }
           try {
               segments.add(SharedSegment.create(link));
           } catch (BridgeTopologyException e) {
               LOG.error("getBridgeSharedSegments: cannot create shared segment {}", 
                  e.getMessage(),
                  e);
               return new ArrayList<SharedSegment>();
           }
           }
       }

        MACLINK:for (BridgeMacLink link : m_bridgeMacLinkDao.findByNodeId(nodeid)) {

            if (link.getLinkType() == BridgeMacLinkType.BRIDGE_FORWARDER) {
                continue;
            }
            for (SharedSegment segment : segments) {
                if (segment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    segment.getMacsOnSegment().add(link.getMacAddress());
                    continue MACLINK;
                }
            }
            try {
                segments.add(SharedSegment.create(link));
            } catch (BridgeTopologyException e) {
                LOG.error("getBridgeSharedSegments: cannot create shared segment {}", e.getMessage(),e);
                return new ArrayList<SharedSegment>();
            }
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
            return SharedSegment.create();
        }

        if (links.size() > 1 ) {
            LOG.error("getHostNodeSharedSegment: more then one segment for mac:{}", mac);
            return SharedSegment.create();
        }

        BridgeMacLink link = links.iterator().next();

        SharedSegment segment = null;

        try {
            for (BridgeBridgeLink bblink: m_bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(link.getNode().getId(), link.getBridgePort())) {
                if (segment == null) {
                        segment = SharedSegment.create(bblink);
                } else {
                    segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(bblink));
                }
            }
        
            for (BridgeMacLink maclink :m_bridgeMacLinkDao.findByNodeIdBridgePort(link.getNode().getId(), link.getBridgePort())) {
                if (segment == null) {
                    segment = SharedSegment.create(maclink);
                } else {
                    segment.getMacsOnSegment().add(maclink.getMacAddress());
                }
            }
        } catch (Exception e) {
            LOG.error("getHostNodeSharedSegment: cannot create shared segment {} for mac {} ", e.getMessage(), mac,e);
            return SharedSegment.create();
        }
 
        return segment;
    }

    public Set<BroadcastDomain> getAllPersisted() {

        List<SharedSegment> bblsegments = new ArrayList<SharedSegment>();
        Map<Integer,Set<Integer>> rootnodetodomainnodemap = new HashMap<Integer,Set<Integer>>();
        Map<Integer,BridgePort> designatebridgemap = new HashMap<Integer,BridgePort>();

        //start bridge bridge link parsing
        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findAll()) {
            boolean  segmentnotfound = true;
            BridgePort bridgeport = BridgePort.getFromBridgeBridgeLink(link);
            designatebridgemap.put(link.getNode().getId(), bridgeport);
            for (SharedSegment bblsegment : bblsegments) {
                if (bblsegment.containsPort(BridgePort.getFromDesignatedBridgeBridgeLink(link))) {
                    bblsegment.getBridgePortsOnSegment().add(bridgeport);
                    segmentnotfound=false;
                    break;
                }
            }
            if (segmentnotfound)  {
                try {
                    bblsegments.add(SharedSegment.create(link));
                } catch (BridgeTopologyException e) {
                    LOG.error("getAllPersisted: cannot create shared segment {}", e.getMessage(),e);
                    return new CopyOnWriteArraySet<BroadcastDomain>();
                }
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
                    rootnodetodomainnodemap.put(link.getDesignatedNode().getId(), new HashSet<Integer>());
                    rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).add(link.getNode().getId());                
                }
            }
        }
        LOG.info("getAllPersisted: bridge topology node set: {}", rootnodetodomainnodemap );
        
        
        //end bridge bridge link parsing
        
        List<SharedSegment> bmlsegments = new ArrayList<SharedSegment>();
        List<BridgeMacLink> forwarders = new ArrayList<BridgeMacLink>();

BML:    for (BridgeMacLink link : m_bridgeMacLinkDao.findAll()) {
            if (link.getLinkType() == BridgeMacLinkType.BRIDGE_FORWARDER) {
                forwarders.add(link);
                continue;
            }
            
            for (SharedSegment bblsegment: bblsegments) {
                if (bblsegment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    bblsegment.getMacsOnSegment().add(link.getMacAddress());
                    continue BML;
                }
            }
            for (SharedSegment bmlsegment: bmlsegments) {
                if (bmlsegment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    bmlsegment.getMacsOnSegment().add(link.getMacAddress());
                    continue BML;
                }
            }
            try {
                bmlsegments.add(SharedSegment.create(link));
            } catch (BridgeTopologyException e) {
                LOG.error("getAllPersisted: cannot create shared segment {}", e.getMessage(), e);
                return new CopyOnWriteArraySet<BroadcastDomain>();
            }
        }
                
        Set<BroadcastDomain> domains = new CopyOnWriteArraySet<BroadcastDomain>();
        for (Integer rootnode : rootnodetodomainnodemap.keySet()) {
            BroadcastDomain domain = new BroadcastDomain();
            Bridge.createRootBridge(domain,rootnode);
            for (Integer bridgenodeId: rootnodetodomainnodemap.get(rootnode)) {
                Bridge.create(domain,
                              bridgenodeId, 
                                               designatebridgemap.get(
                                                                      bridgenodeId).getBridgePort());
            }
            domains.add(domain);
        }
        
        for (SharedSegment segment : bblsegments) {
            for (BroadcastDomain cdomain: domains) {
                if (BroadcastDomain.loadTopologyEntry(cdomain,segment)) {
                    break;
                }
            }
        }

SEG:        for (SharedSegment segment : bmlsegments) {
            for (BroadcastDomain cdomain: domains) {
                if (BroadcastDomain.loadTopologyEntry(cdomain,segment)) {
                    continue SEG;
                }
            }
            BroadcastDomain domain = new BroadcastDomain();
            Bridge.createRootBridge(domain,segment.getDesignatedBridge());
            BroadcastDomain.loadTopologyEntry(domain, segment);
            domains.add(domain);
        }

        for (BridgeMacLink forwarder : forwarders) {
            for (BroadcastDomain domain: domains) {
                Bridge bridge = domain.getBridge(forwarder.getNode().getId());
                if (bridge != null) {
                    domain.addForwarding(BridgePort.getFromBridgeMacLink(forwarder),forwarder.getMacAddress());
                    break;
                }
            }
        }
        return domains;
    }

    public synchronized boolean collectBft(int nodeid, int maxsize) {
        if (getUpdateBftMap().size()+m_bridgecollectionsscheduled.size() >= maxsize )
                return false;
        synchronized (m_bridgecollectionsscheduled) {
                m_bridgecollectionsscheduled.add(nodeid);
                }
        return true;
    }
    
    public synchronized void collectedBft(int nodeid) {
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
                findAll().
                stream().forEach(m -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getMacPorts: parsing: {}",m);
                    }
                    if (m.getNode() != null ) {
                        if (nodeIfindexToMacPortTable.contains(m.getNode().getId(), m.getIfIndex())) {
                            MacPort.merge(m, nodeIfindexToMacPortTable.get(m.getNode().getId(), m.getIfIndex()));
                        } else {
                            nodeIfindexToMacPortTable.put(m.getNode().getId(), m.getIfIndex(), MacPort.create(m));
                        }
                    } else {
                        if (macToMacPortMap.containsKey(m.getPhysAddress())) {
                            MacPort.merge(m, macToMacPortMap.get(m.getPhysAddress()));
                        } else {
                            macToMacPortMap.put(m.getPhysAddress(), MacPort.create(m));
                        }
                    }
                });
       List<MacPort> ports = nodeIfindexToMacPortTable.values().stream().collect(Collectors.toList(
                    ));
       ports.stream().forEach(mp -> {
           mp.getMacPortMap().keySet().stream().filter(mac -> macToMacPortMap.containsKey(mac)).forEach(mac -> {
                   mp.getMacPortMap().get(mac).addAll(macToMacPortMap.remove(mac).getMacPortMap().get(mac));
           });
       });
       ports.addAll(macToMacPortMap.values());
       return ports;
    }

    @Override
    public List<TopologyShared> match() {       
        final List<TopologyShared> links = new ArrayList<>();
        final List<MacPort> macPortMap = getMacPorts();
        
        m_domains.stream().forEach(dm ->{
            if (LOG.isDebugEnabled()) {
                LOG.debug("match: \n{}", dm.printTopology());
            }
            dm.getSharedSegments().stream().forEach( shs -> {
                try {
                    links.add(TopologyShared.of(shs, macPortMap.stream().filter( mp -> 
                    shs.getMacsOnSegment().containsAll(mp.getMacPortMap().keySet())).
                                                collect(Collectors.toList())));
                } catch (BridgeTopologyException e) {
                    LOG.error("{} Cannot add shared segment to topology: {}", e.getMessage(), e.printTopology(),e);
                }
            });
        });
        return links;
    }

    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }
    
}
