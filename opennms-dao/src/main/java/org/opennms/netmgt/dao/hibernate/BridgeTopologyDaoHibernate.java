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
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.Bridge;
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
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;

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
    public  void saveForwarders(BroadcastDomain domain) {
        for (Integer nodeId: domain.getBridgeNodesOnDomain()) {
            List<BridgeMacLink> forwarders = domain.getForwarders(nodeId);
            if (forwarders == null || forwarders.size() == 0)
                continue;
            for (BridgeMacLink forward: forwarders) {
                forward.setBridgeMacLinkLastPollTime(new Date());
                saveBridgeMacLink(forward);
            }
        }
    }
    
    @Override 
    public void save(BroadcastDomain domain) {
        for (SharedSegment segment : domain.getTopology()) {
            //FIXME why I can have a segment without a designated port?
            if (!segment.hasDesignatedBridgeport())
                continue;
            for (BridgeBridgeLink link : segment.getBridgeBridgeLinks()) {
                link.setBridgeBridgeLinkLastPollTime(new Date());
                saveBridgeBridgeLink(link);
            }
            for (BridgeMacLink link : segment.getBridgeMacLinks()) {
                link.setBridgeMacLinkLastPollTime(new Date());
                saveBridgeMacLink(link);
            }
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
    public void delete(int nodeid) {
        m_bridgeBridgeLinkDao.deleteByDesignatedNodeId(nodeid);
        m_bridgeBridgeLinkDao.deleteByNodeId(nodeid);
        m_bridgeBridgeLinkDao.flush();

        m_bridgeMacLinkDao.deleteByNodeId(nodeid);
        m_bridgeMacLinkDao.flush();

    }

    @Override
    public void deleteOlder(int nodeid, Date now) {
        m_bridgeMacLinkDao.deleteByNodeIdOlderThen(nodeid, now);
        m_bridgeMacLinkDao.flush();
        m_bridgeBridgeLinkDao.deleteByNodeIdOlderThen(nodeid, now);
        m_bridgeBridgeLinkDao.deleteByDesignatedNodeIdOlderThen(nodeid, now);
        m_bridgeBridgeLinkDao.flush();
    }

    
    @Override
    public List<SharedSegment> getBridgeSharedSegments(int nodeid) {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();
        Set<Integer> designated = new HashSet<Integer>();
BRIDGELINK:        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findByNodeId(nodeid)) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())
                     || segment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    segment.add(link);
                    designated.add(link.getDesignatedNode().getId());
                    continue BRIDGELINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getDesignatedNode().getId());
            segments.add(segment);
        }
        
        designated.add(nodeid);
        for (Integer curNodeId: designated) {
DBRIDGELINK:        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findByDesignatedNodeId(curNodeId)) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())
                     || segment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    segment.add(link);
                    continue DBRIDGELINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getDesignatedNode().getId());
            segments.add(segment);
        }
        }

MACLINK:        for (BridgeMacLink link : m_bridgeMacLinkDao.findByNodeId(nodeid)) {
            link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            for (SharedSegment segment : segments) {
                if (segment.containsMac(link.getMacAddress())
                        || segment.containsPort(link.getNode().getId(),
                                                link.getBridgePort())) {
                    segment.add(link);
                    continue MACLINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getNode().getId());
            segments.add(segment);
        }

        return segments;
    }
    
    @Override
    public SharedSegment getHostSharedSegment(String mac) {
        
        List<BridgeMacLink> links = m_bridgeMacLinkDao.findByMacAddress(mac);
        if (links.size() == 0 )
            return new SharedSegment();
        BridgeMacLink link = links.get(0);
        for (SharedSegment segment: getBridgeSharedSegments(link.getNode().getId()) ) {
            if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                return segment;
            }
        }
        return new SharedSegment();
    }

    @Override
    public Set<BroadcastDomain> load() {

        List<SharedSegment> bblsegments = new ArrayList<SharedSegment>();
        Map<Integer,Set<Integer>> rootnodetodomainnodemap = new HashMap<Integer,Set<Integer>>();

        //start bridge bridge link parsing
        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findAll()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: Parsing BridgeBridgeLink: {}", link.printTopology());
            }
            boolean  segmentnotfound = true;
            for (SharedSegment bblsegment : bblsegments) {
                if (bblsegment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    bblsegment.add(link);
                    segmentnotfound=false;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: found Bridge Bridge Link Shared Segment: {}", bblsegment.printTopology());
                    }
                    break;
                }
            }
            if (segmentnotfound)  {
                SharedSegment bblsegment = new SharedSegment();
                bblsegment.add(link);
                bblsegment.setDesignatedBridge(link.getDesignatedNode().getId());
                bblsegments.add(bblsegment);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAllPersisted: created new Bridge Bridge Link Shared Segment: {}", bblsegment.printTopology());
                }
            }
            // set up domains
            if (rootnodetodomainnodemap.containsKey(link.getDesignatedNode().getId())) {
                rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).add(link.getNode().getId());
                if (rootnodetodomainnodemap.containsKey(link.getNode().getId())) {
                    rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).addAll(
                                                                                         rootnodetodomainnodemap.remove(
                                                                                                                     link.getNode().getId()));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAllPersisted: designated {} is root, dependency set: {}",
                              link.getDesignatedNode().getId(),
                              rootnodetodomainnodemap.get(
                                                          link.getDesignatedNode().getId()) );
                }
            } else if (rootnodetodomainnodemap.containsKey(link.getNode().getId())) {
                Set<Integer> dependentsnode= rootnodetodomainnodemap.remove(link.getNode().getId());
                dependentsnode.add(link.getNode().getId());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAllPersisted: node {} is root, dependency set: {}",link.getNode().getId(),dependentsnode );
                }
                Integer rootdesignated=null;
                for (Integer rootid: rootnodetodomainnodemap.keySet()) {
                    if (rootnodetodomainnodemap.get(rootid).contains(link.getDesignatedNode().getId())) {
                        rootdesignated=rootid;
                        break;
                    }
                }
                if (rootdesignated != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: node {} found root: {}",link.getNode().getId(),rootdesignated );
                    }
                    dependentsnode.add(link.getDesignatedNode().getId());
                    rootnodetodomainnodemap.get(rootdesignated).addAll(dependentsnode);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: node {} found root: {}, dependency set: {}",link.getNode().getId(),
                              rootdesignated, 
                              rootnodetodomainnodemap.get(rootdesignated));
                    }
                } else {
                    rootnodetodomainnodemap.put(link.getDesignatedNode().getId(), dependentsnode);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: node {} created root: {}, dependency set: {}",link.getNode().getId(),
                              link.getDesignatedNode().getId(), 
                              rootnodetodomainnodemap.get(link.getDesignatedNode().getId()));
                    }
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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: designatednode {} found root: {}, dependency set: {}",link.getDesignatedNode().getId(),
                              rootdesignated, 
                              rootnodetodomainnodemap.get(rootdesignated));
                    }
                } else {
                    rootnodetodomainnodemap.put(link.getDesignatedNode().getId(), new HashSet<Integer>());
                    rootnodetodomainnodemap.get(link.getDesignatedNode().getId()).add(link.getNode().getId());                
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: designatednode {} : {}",link.getDesignatedNode().getId(),rootnodetodomainnodemap.get(link.getDesignatedNode().getId()) );
                    }// FIXME: Check if node is a child of some other and manage exception :-)
                }
            }
        }
        LOG.debug("getAllPersisted: bridge topology set: {}", rootnodetodomainnodemap );
        
        
        //end bridge bridge link parsing
        
        List<SharedSegment> bmlsegments = new ArrayList<SharedSegment>();

        Map<String,List<BridgeMacLink>> mactobridgeportbbl = new HashMap<String, List<BridgeMacLink>>();
BML:    for (BridgeMacLink link : m_bridgeMacLinkDao.findAll()) {
            link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: Parsing BridgeMacLink: {}", link.printTopology());
            }
            for (SharedSegment bblsegment: bblsegments) {
                if (bblsegment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())) {
                    if (!mactobridgeportbbl.containsKey(link.getMacAddress())) {
                        mactobridgeportbbl.put(link.getMacAddress(), new ArrayList<BridgeMacLink>());
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: Found BridgeBridgeLink Segment: {}", bblsegment.printTopology());
                    }
                    mactobridgeportbbl.get(link.getMacAddress()).add(link);
                    continue BML;
                }
            }
            if (!rootnodetodomainnodemap.containsKey(link.getNode().getId())) {
                boolean norootnodetodomainmapentry=true;
                for (Set<Integer> nodes: rootnodetodomainnodemap.values()) {
                    if (nodes.contains(link.getNode().getId())) {
                        norootnodetodomainmapentry=false;
                        break;
                    }
                }
                if (norootnodetodomainmapentry)   
                    rootnodetodomainnodemap.put(link.getNode().getId(), 
                                                new HashSet<Integer>());
            }
            for (SharedSegment bmlsegment: bmlsegments) {
                if (bmlsegment.containsPort(link.getNode().getId(),
                                            link.getBridgePort())) {
                    bmlsegment.add(link);
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: found Bridge Mac Link Shared Segment: {}", bmlsegment.printTopology());
                    }
                    continue BML;
                }
            }
            SharedSegment bmlsegment = new SharedSegment();
            bmlsegment.add(link);
            bmlsegment.setDesignatedBridge(link.getNode().getId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: created new Bridge Mac Link Shared Segment: {}", bmlsegment.printTopology());
            }
            bmlsegments.add(bmlsegment);
        }

        List<BridgeMacLink> forwarders = new ArrayList<BridgeMacLink>();
        for (String macaddress: mactobridgeportbbl.keySet()) {
            LOG.debug("getAllPersisted: assigning mac {} to Bridge Bridge Link Shared Segment",macaddress);
            
            for (SharedSegment segment : bblsegments) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAllPersisted: parsing Bridge Bridge Link Shared Segment {}",segment.printTopology());
                }
                List<BridgeMacLink> bblfoundonsegment = new ArrayList<BridgeMacLink>();
                for (BridgeMacLink link : mactobridgeportbbl.get(macaddress)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: parsing Bridge Mac Link {}",link.printTopology());
                    }
                    if (segment.containsPort(link.getNode().getId(),
                                                    link.getBridgePort())) {
                        bblfoundonsegment.add(link);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getAllPersisted: adding Bridge Mac Link {}",link.printTopology());
                        }
                   }
                }
                if (bblfoundonsegment.size() == segment.getBridgePortsOnSegment().size()) {
                    for (BridgeMacLink link: bblfoundonsegment)
                        segment.add(link);
                } else {
                    forwarders.addAll(bblfoundonsegment);
                }
            }
        }          
                
        Set<BroadcastDomain> domains = new CopyOnWriteArraySet<BroadcastDomain>();
        for (Integer rootnode : rootnodetodomainnodemap.keySet()) {
            BroadcastDomain domain = new BroadcastDomain();
            domain.addBridge(new Bridge(rootnode));
            for (Integer nodeid: rootnodetodomainnodemap.get(rootnode))
                domain.addBridge(new Bridge(nodeid));
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: created new Broadcast Domain: {}", domain.getBridgeNodesOnDomain());
            }
            domains.add(domain);
        }
        
        for (SharedSegment segment : bblsegments) {
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.containsAtleastOne(segment.getBridgeIdsOnSegment())) {
                    cdomain.loadTopologyEntry(segment);
                    break;
                }
            }
        }

        for (SharedSegment segment : bmlsegments) {
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.containsAtleastOne(segment.getBridgeIdsOnSegment())) {
                    cdomain.loadTopologyEntry(segment);
                    break;
                }
            }
        }

        for (BridgeMacLink forwarder : forwarders) {
            for (BroadcastDomain domain: domains) {
                if (domain.containBridgeId(forwarder.getNode().getId())) {
                    domain.addForwarding(forwarder);
                    break;
                }
            }
        }

        for (BroadcastDomain domain: domains) {
            if (LOG.isDebugEnabled()) {
                LOG.info("getAllPersisted: loading root Broadcast Domain: {}", domain.getBridgeNodesOnDomain());
            }
            domain.loadTopologyRoot();
        }
        return domains;
    }
    

}
