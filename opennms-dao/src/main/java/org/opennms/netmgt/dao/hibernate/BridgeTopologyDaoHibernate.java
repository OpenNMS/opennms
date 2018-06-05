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

package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class BridgeTopologyDaoHibernate implements BridgeTopologyDao {
    
    private final static Logger LOG = LoggerFactory.getLogger(BridgeTopologyDaoHibernate.class);
    @Autowired
    private PlatformTransactionManager m_transactionManager;
    
    private NodeDao m_nodeDao;
    private BridgeElementDao m_bridgeElementDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;

    
    public BridgeElementDao getBridgeElementDao() {
        return m_bridgeElementDao;
    }

    public void setBridgeElementDao(BridgeElementDao bridgeElementDao) {
        m_bridgeElementDao = bridgeElementDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
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
    public void save(BroadcastDomain domain, Date now) throws BridgeTopologyException {
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
                final OnmsNode node = m_nodeDao.get(saveMe.getNode().getId());
                if (node == null)
                    return null;
                saveMe.setNode(node);
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
                final OnmsNode node = m_nodeDao.get(saveMe.getNode().getId());
                if (node == null)
                    return null;
                saveMe.setNode(node);
                if (saveMe.getBridgeBridgeLinkLastPollTime() == null)
                    saveMe.setBridgeBridgeLinkLastPollTime(saveMe.getBridgeBridgeLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public Set<BroadcastDomain> load() {
        Set<BroadcastDomain> domains=getAllPersisted();
        for (BroadcastDomain domain: domains) {
            for (Bridge bridge: domain.getBridges()) {
                bridge.clear();
                List<BridgeElement> elems = m_bridgeElementDao.findByNodeId(bridge.getNodeId());
                bridge.getIdentifiers().addAll(Bridge.getIdentifier(elems));
                bridge.setDesignated(Bridge.getDesignated(elems));
            }        
        }
        return domains;
    }

    @Override
    public void delete(int nodeid) {
        m_bridgeBridgeLinkDao.deleteByDesignatedNodeId(nodeid);
        m_bridgeBridgeLinkDao.deleteByNodeId(nodeid);
        m_bridgeBridgeLinkDao.flush();

        m_bridgeMacLinkDao.deleteByNodeId(nodeid);
        m_bridgeMacLinkDao.flush();

    }
    
    @Override
    public List<SharedSegment> getBridgeSharedSegments(int nodeid) 
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
    public SharedSegment getHostSharedSegment(String mac) {
        
        List<SharedSegment> segments = new ArrayList<SharedSegment>();

        List<BridgeMacLink> links = m_bridgeMacLinkDao.findByMacAddress(mac);
        if (links.size() == 0 ) {
            return SharedSegment.create();
        }
        
        Set<BridgePort> designated = new HashSet<BridgePort>();
        MACLINK: for (BridgeMacLink link: links) {
            if (link.getLinkType() == BridgeMacLinkType.BRIDGE_FORWARDER) {
                continue;
            }
            designated.add(BridgePort.getFromBridgeMacLink(link));
            for (SharedSegment segment : segments) {
                if (segment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    segment.getMacsOnSegment().add(link.getMacAddress());
                    continue MACLINK;
                }
            }
            try {
                segments.add(SharedSegment.create(link));
            } catch (BridgeTopologyException e) {
                LOG.error("getHostNodeSharedSegment: cannot create shared segment {}", e.getMessage(),e);
                return SharedSegment.create();
            }
        }

        for (BridgePort port: designated) {
            SharedSegment shared = null;
            for (SharedSegment segment : segments) {
                if (segment.containsPort(port)) {
                    shared = segment;
                    break;
                }
            }
            if (shared == null) {
                LOG.error("getHostNodeSharedSegment: cannot found shared segment for port {}", port.printTopology());
                return SharedSegment.create();
            }
            for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(port.getNodeId(), port.getBridgePort())) {
                    shared.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
            }
        }

        if (segments.size() == 0) {
           return SharedSegment.create();
        }

        if (segments.size() > 1) {
            LOG.error("getHostSharedSegment: found {} shared segment for mac {}", 
                      segments.size(),
                      mac);
            return SharedSegment.create();
        }
            return segments.iterator().next();
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
    
}
