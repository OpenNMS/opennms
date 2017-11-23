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

package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {
    
    volatile Set<BroadcastDomain> m_domains;
    private final static Logger LOG = LoggerFactory.getLogger(BridgeTopologyDaoInMemory.class);

    @Override
    public void save(BroadcastDomain domain) {
        synchronized (m_domains) {
            m_domains.add(domain);
        }
    }

    @Override
    public synchronized void load(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {
        m_domains=getAllPersisted(bridgeBridgeLinkDao, bridgeMacLinkDao);
    }

    //FIXME check forwarders.....should not be included here..
    @Override
    public List<SharedSegment> getBridgeNodeSharedSegments(
           BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao, int nodeid) 
        {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();
        
        Set<Integer> designated = new HashSet<Integer>();
        designated.add(nodeid);
        
        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByNodeId(nodeid)) {
            designated.add(link.getDesignatedNode().getId());
        }
        
        for (Integer curNodeId: designated) {
            DBRIDGELINK:for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByDesignatedNodeId(curNodeId)) {
                if (link.getNode().getId().intValue() != nodeid
                        && link.getDesignatedNode().getId().intValue() != nodeid) {
                    continue;
                }
                for (SharedSegment segment : segments) {
                    if (segment.containsPort(BridgePort.getFromDesignatedBridgeBridgeLink(link))) {
                        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
                        continue DBRIDGELINK;
                    }
                }
                try {
                    segments.add(SharedSegment.createFrom(link));
                } catch (BridgeTopologyException e) {
                    LOG.error("getBridgeNodeSharedSegments: cannot create shared segment {}", e.getMessage(),e);
                }
            }
        }

        MACLINK:for (BridgeMacLink link : bridgeMacLinkDao.findByNodeId(nodeid)) {
            for (SharedSegment segment : segments) {
                if (segment.containsMac(link.getMacAddress())
                        || segment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    segment.getMacsOnSegment().add(link.getMacAddress());
                    continue MACLINK;
                }
            }
            try {
                segments.add(SharedSegment.createFrom(link));
            } catch (BridgeTopologyException e) {
                LOG.error("getBridgeNodeSharedSegments: cannot create shared segment {}", e.getMessage(),e);
            }
        }
        if (LOG.isDebugEnabled()) {
            for (SharedSegment segment: segments) {
                LOG.debug("getBridgeNodeSharedSegments: node[{}] found:\n{}", nodeid,
                          segment.printTopology());
                
            }
        }

        return segments;
    }
    
    //FIXME what if there are duplicated macs?
    //FIXME check forwarders.....should not be here..
    //FIXME should be revised
    @Override
    public SharedSegment getHostNodeSharedSegment(BridgeBridgeLinkDao bridgeBridgeLinkDao, BridgeMacLinkDao bridgeMacLinkDao, String mac) {
        
        List<BridgeMacLink> links = bridgeMacLinkDao.findByMacAddress(mac);
        if (links.size() == 0 )
            return SharedSegment.create();
        BridgeMacLink link = links.get(0);
        for (SharedSegment segment: getBridgeNodeSharedSegments(bridgeBridgeLinkDao, bridgeMacLinkDao, link.getNode().getId()) ) {
            if (segment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                return segment;
            }
        }
        return SharedSegment.create();
    }

    @Override
    public Set<BroadcastDomain> getAllPersisted(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {

        List<SharedSegment> bblsegments = new ArrayList<SharedSegment>();
        Map<Integer,Set<Integer>> rootnodetodomainnodemap = new HashMap<Integer,Set<Integer>>();
        Map<Integer,BridgePort> designatebridgemap = new HashMap<Integer,BridgePort>();

        //start bridge bridge link parsing
        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findAll()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: Parsing {}", link.printTopology());
            }
            boolean  segmentnotfound = true;
            BridgePort bridgeport = BridgePort.getFromBridgeBridgeLink(link);
            designatebridgemap.put(link.getNode().getId(), bridgeport);
            for (SharedSegment bblsegment : bblsegments) {
                if (bblsegment.containsPort(BridgePort.getFromDesignatedBridgeBridgeLink(link))) {
                    bblsegment.getBridgePortsOnSegment().add(bridgeport);
                    segmentnotfound=false;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: added {}. Shared Segment:\n{}", 
                                  bridgeport.printTopology(), 
                                  bblsegment.printTopology());
                    }
                    break;
                }
            }
            if (segmentnotfound)  {
                try {
                    SharedSegment segment = SharedSegment.createFrom(link); 
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: created Shared Segment:\n{}", 
                                  segment.printTopology());
                    }
                    bblsegments.add(segment);
                } catch (BridgeTopologyException e) {
                    LOG.error("getAllPersisted: cannot create shared segment {}", e.getMessage(),e);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: bridge topology node set: {}", rootnodetodomainnodemap );
            }
        }
        LOG.info("getAllPersisted: bridge topology node set: {}", rootnodetodomainnodemap );
        
        
        //end bridge bridge link parsing
        
        List<SharedSegment> bmlsegments = new ArrayList<SharedSegment>();

        Map<String,List<BridgeMacLink>> mactobridgeportbbl = new HashMap<String, List<BridgeMacLink>>();
BML:    for (BridgeMacLink link : bridgeMacLinkDao.findAll()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: Parsing {}", link.printTopology());
            }
            for (SharedSegment bblsegment: bblsegments) {
                if (bblsegment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    if (!mactobridgeportbbl.containsKey(link.getMacAddress())) {
                        mactobridgeportbbl.put(link.getMacAddress(), new ArrayList<BridgeMacLink>());
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: Found Segment:\n{}", bblsegment.printTopology());
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
                if (bmlsegment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                    bmlsegment.getMacsOnSegment().add(link.getMacAddress());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: added mac {} to Shared Segment:\n{}", 
                                  link.getMacAddress(),
                                  bmlsegment.printTopology());
                    }
                    continue BML;
                }
            }
            try {
                SharedSegment segment = SharedSegment.createFrom(link); 
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAllPersisted: created Shared Segment:\n{}", 
                              segment.printTopology());
                }
                bmlsegments.add(segment);
            } catch (BridgeTopologyException e) {
                LOG.error("getAllPersisted: cannot create shared segment {}", e.getMessage(), e);
            }
        }

        List<BridgeMacLink> forwarders = new ArrayList<BridgeMacLink>();
        for (String macaddress: mactobridgeportbbl.keySet()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: parsing backbone mac {} ",macaddress);
            }
            for (SharedSegment segment : bblsegments) {
                List<BridgeMacLink> bblfoundonsegment = new ArrayList<BridgeMacLink>();
                for (BridgeMacLink link : mactobridgeportbbl.get(macaddress)) {
                    if (segment.containsPort(BridgePort.getFromBridgeMacLink(link))) {
                        bblfoundonsegment.add(link);
                   }
                }
                if (bblfoundonsegment.size() == segment.getBridgePortsOnSegment().size()) {
                    for (BridgeMacLink link: bblfoundonsegment) {
                        segment.getMacsOnSegment().add(link.getMacAddress());
                    }
                } else {
                    forwarders.addAll(bblfoundonsegment);
                }
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
                
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: created new Broadcast Domain:\n{}", domain.printTopology());
            }
            domains.add(domain);
        }
        
        for (SharedSegment segment : bblsegments) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: parsing:\n {}", segment.printTopology());
            }
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.loadTopologyEntry(segment)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: assigned:\n {}", cdomain.printTopology());
                    }
                    break;
                }
            }
        }

        for (SharedSegment segment : bmlsegments) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAllPersisted: parsing:\n {}", segment.printTopology());
            }
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.loadTopologyEntry(segment)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getAllPersisted: assigned:\n {}", cdomain.printTopology());
                    }
                    break;
                }
            }
        }

        for (BridgeMacLink forwarder : forwarders) {
            for (BroadcastDomain domain: domains) {
                Bridge bridge = domain.getBridge(forwarder.getNode().getId());
                if (bridge != null) {
                    domain.addForwarding(BridgeForwardingTableEntry.getFromBridgeMacLink(forwarder));
                    break;
                }
            }
        }
        return domains;
    }
    
    @Override
    public void delete(BroadcastDomain domain) {
        synchronized (m_domains) {
            m_domains.remove(domain);
        }
    }

    @Override
    public BroadcastDomain get(int nodeid) {
        synchronized (m_domains) {
            for (BroadcastDomain domain: m_domains) {
                synchronized (domain) {
                    Bridge bridge = domain.getBridge(nodeid);
                    if (bridge != null) {
                        return domain;
                    }
                }
            }
        }
        return null;
    }

    public synchronized Set<BroadcastDomain> getAll() {
        return m_domains;
    }

    @Override
    public void clean() {
        synchronized (m_domains) {
            m_domains.removeIf(BroadcastDomain::isEmpty);
        }
    }

}
