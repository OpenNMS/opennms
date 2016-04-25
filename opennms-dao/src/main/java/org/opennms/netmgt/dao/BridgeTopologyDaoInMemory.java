package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {
    volatile Set<BroadcastDomain> m_domains = new HashSet<BroadcastDomain>();

    @Override
    public synchronized void save(BroadcastDomain domain) {
        m_domains.add(domain);
    }

    @Override
    public synchronized void load(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {
        m_domains=getAllPersisted(bridgeBridgeLinkDao, bridgeMacLinkDao);
    }

    @Override
    public List<SharedSegment> getBridgeNodeSharedSegments(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao, int nodeid) {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();
        /*
        for (BroadcastDomain domain: getAllPersisted(bridgeBridgeLinkDao, bridgeMacLinkDao)) {
            System.out.println("parsing domain:" + domain);
            System.out.println("parsing domain with nodes:" + domain.getBridgeNodesOnDomain());
            System.out.println("parsing domain with macs:" + domain.getMacsOnDomain());
            if (domain.getBridgeNodesOnDomain().contains(nodeid)) {
                System.out.println("got domain with nodeid:" + nodeid);
                for (SharedSegment segment: domain.getTopology()) {
                    System.out.println("parsing segment:" + segment);
                    System.out.println("parsing segment with nodes:" + segment.getBridgeIdsOnSegment());
                    System.out.println("parsing segment with macs:" + segment.getMacsOnSegment());
                    if (segment.getBridgeIdsOnSegment().contains(nodeid)) {
                        segments.add(segment);
                        System.out.println("added segment:" + segment);
                    }
                }
            }
        }*/
        Set<Integer> designated = new HashSet<Integer>();
BRIDGELINK:        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByNodeId(nodeid)) {
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
DBRIDGELINK:        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByDesignatedNodeId(curNodeId)) {
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

MACLINK:        for (BridgeMacLink link : bridgeMacLinkDao.findByNodeId(nodeid)) {
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
    public SharedSegment getHostNodeSharedSegment(BridgeBridgeLinkDao bridgeBridgeLinkDao, BridgeMacLinkDao bridgeMacLinkDao, String mac) {
        
        List<BridgeMacLink> links = bridgeMacLinkDao.findByMacAddress(mac);
        if (links.size() == 0 )
            return new SharedSegment();
        BridgeMacLink link = links.get(0);
        for (SharedSegment segment: getBridgeNodeSharedSegments(bridgeBridgeLinkDao, bridgeMacLinkDao, link.getNode().getId()) ) {
            if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                return segment;
            }
        }
        return new SharedSegment();
    }

    @Override
    public Set<BroadcastDomain> getAllPersisted(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();

BRIDGELINK:        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findAll()) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())
                     || segment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    segment.add(link);
                    continue BRIDGELINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getDesignatedNode().getId());
            segments.add(segment);
        }

MACLINK:  for (BridgeMacLink link : bridgeMacLinkDao.findAll()) {
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

        Set<Set<Integer>> nodelinked = new HashSet<Set<Integer>>();
SHARED:        for (SharedSegment segment: segments) {
            Set<Integer> nodesOnSegment = new HashSet<Integer>(segment.getBridgeIdsOnSegment());
            for (Set<Integer> nodes: nodelinked) {
                for (Integer nodeid: nodesOnSegment) {
                    if (nodes.contains(nodeid)) 
                        continue SHARED;
                }
            }
            nodelinked.add(getNodesOnDomainSet(segments, segment, new HashSet<SharedSegment>(),nodesOnSegment));
        }
        
        Set<BroadcastDomain> domains = new HashSet<BroadcastDomain>();
        for (Set<Integer> nodes : nodelinked) {
            BroadcastDomain domain = new BroadcastDomain();
            for (Integer nodeid: nodes)
                domain.addBridge(new Bridge(nodeid));
            domains.add(domain);
        }
        // Assign the segment to domain and add to single nodes
        for (SharedSegment segment : segments) {
            BroadcastDomain domain = null;
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.containsAtleastOne(segment.getBridgeIdsOnSegment())) {
                    domain = cdomain;
                    break;
                }
            }
            if (domain == null) {
                domain = new BroadcastDomain();
                domains.add(domain);
            }
            domain.loadTopologyEntry(segment);
        }

        return domains;
    }
    
    private Set<Integer> getNodesOnDomainSet(List<SharedSegment> segments, SharedSegment segment, Set<SharedSegment> parsed, Set<Integer> nodesOnDomain) {
        parsed.add(segment);
MAINLOOP:        for (SharedSegment parsing: segments) {
            if (parsed.contains(parsing))
                continue;
            Set<Integer> nodesOnSegment = parsing.getBridgeIdsOnSegment();
            for (Integer nodeid: nodesOnSegment) {
                if (nodesOnDomain.contains(nodeid)) {
                    nodesOnDomain.addAll(nodesOnSegment);
                    getNodesOnDomainSet(segments, parsing, parsed, nodesOnDomain);
                    break MAINLOOP;
                }
            }
        }
        return nodesOnDomain;
    }

    @Override
    public synchronized void delete(BroadcastDomain domain) {
        m_domains.remove(domain);
    }

    @Override
    public synchronized BroadcastDomain get(int nodeid) {
        for (BroadcastDomain domain: m_domains) {
            if (domain.containBridgeId(nodeid))
                return domain;
        }
        return null;
    }

    public synchronized Set<BroadcastDomain> getAll() {
        return m_domains;
    }

    @Override
    public synchronized void clean() {
        Set<BroadcastDomain> empties = new HashSet<BroadcastDomain>();
        for (BroadcastDomain domain: m_domains) {
            if (domain.isEmpty())
                empties.add(domain);
        }
        m_domains.removeAll(empties);
    }
    

}
