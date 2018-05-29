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
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.model.topology.Bridge;
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
    public void load(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao, BridgeElementDao bridgeElementDao) {
        m_domains=getAllPersisted(bridgeBridgeLinkDao, bridgeMacLinkDao);
        for (BroadcastDomain domain: m_domains) {
            for (Bridge bridge: domain.getBridges()) {
                bridge.clear();
                List<BridgeElement> elems = bridgeElementDao.findByNodeId(bridge.getNodeId());
                bridge.getIdentifiers().addAll(Bridge.getIdentifier(elems));
                bridge.setDesignated(Bridge.getDesignated(elems));
            }        
        }
    }

    @Override
    public List<SharedSegment> getBridgeNodeSharedSegments(
           BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao, int nodeid) 
        {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();

        BBLDESI: for (BridgeBridgeLink link: bridgeBridgeLinkDao.findByDesignatedNodeId(nodeid)) {
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
        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByNodeId(nodeid)) {
            designated.add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
        }        
        
       for (BridgePort designatedport: designated) {
       BBL: for ( BridgeBridgeLink link : bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(designatedport.getNodeId(), 
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
               LOG.error("getBridgeNodeSharedSegments: cannot create shared segment {}", 
                  e.getMessage(),
                  e);
               return new ArrayList<SharedSegment>();
           }
           }
       }

        MACLINK:for (BridgeMacLink link : bridgeMacLinkDao.findByNodeId(nodeid)) {

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
                LOG.error("getBridgeNodeSharedSegments: cannot create shared segment {}", e.getMessage(),e);
                return new ArrayList<SharedSegment>();
            }
        }
        return segments;
    }
    
    @Override
    public SharedSegment getHostNodeSharedSegment(BridgeBridgeLinkDao bridgeBridgeLinkDao, BridgeMacLinkDao bridgeMacLinkDao, String mac) {
        
        List<SharedSegment> segments = new ArrayList<SharedSegment>();

        List<BridgeMacLink> links = bridgeMacLinkDao.findByMacAddress(mac);
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
            for (BridgeBridgeLink link : bridgeBridgeLinkDao.getByDesignatedNodeIdBridgePort(port.getNodeId(), port.getBridgePort())) {
                    shared.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
            }
        }

        if (segments.size() == 0) {
           return SharedSegment.create();
        }

        if (segments.size() > 1) {
            LOG.error("getHostNodeSharedSegment: found {} shared segment for mac {}", 
                      segments.size(),
                      mac);
            return SharedSegment.create();
        }
            return segments.iterator().next();
    }

    @Override
    public Set<BroadcastDomain> getAllPersisted(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {

        List<SharedSegment> bblsegments = new ArrayList<SharedSegment>();
        Map<Integer,Set<Integer>> rootnodetodomainnodemap = new HashMap<Integer,Set<Integer>>();
        Map<Integer,BridgePort> designatebridgemap = new HashMap<Integer,BridgePort>();

        //start bridge bridge link parsing
        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findAll()) {
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

BML:    for (BridgeMacLink link : bridgeMacLinkDao.findAll()) {
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
