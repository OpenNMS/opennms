package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {
    
    volatile List<BroadcastDomain> m_domains = new ArrayList<BroadcastDomain>();
    volatile Map<Integer, List<BridgeMacLink>> m_notYetParsedBFTMap = new HashMap<Integer, List<BridgeMacLink>>();
    volatile Map<Integer, List<BridgeStpLink>> m_notYetParsedSTPMap = new HashMap<Integer, List<BridgeStpLink>>();
    volatile Map<Integer, List<BridgeElement>> m_notYetParsedEleMap = new HashMap<Integer, List<BridgeElement>>();

    @Override
    public synchronized void store(BridgeElement element) {
        Integer nodeid = element.getNode().getId();
        if (!m_notYetParsedEleMap.containsKey(nodeid))
            m_notYetParsedEleMap.put(nodeid, new ArrayList<BridgeElement>());
        m_notYetParsedEleMap.get(nodeid).add(element);
        
    }
    
    @Override
    public synchronized void store(BridgeMacLink maclink) {
        Integer nodeid = maclink.getNode().getId();
        if (!m_notYetParsedBFTMap.containsKey(nodeid))
            m_notYetParsedBFTMap.put(nodeid, new ArrayList<BridgeMacLink>());
        m_notYetParsedBFTMap.get(nodeid).add(maclink);
    }

    @Override
    public synchronized void store(BridgeStpLink stplink) {
        Integer nodeid = stplink.getNode().getId();
        if (!m_notYetParsedSTPMap.containsKey(nodeid))
            m_notYetParsedSTPMap.put(nodeid, new ArrayList<BridgeStpLink>());
        m_notYetParsedSTPMap.get(nodeid).add(stplink);
    }

    @Override
    public synchronized void loadTopology(List<BridgeElement> bridgeelements, 
            List<BridgeMacLink> links,
            List<BridgeBridgeLink> bridgelinks,
            List<BridgeStpLink> stplinks) {
        
        //clear all the topology for all domains
        m_domains.clear();
        
        List<SharedSegment> segments = new ArrayList<SharedSegment>();
        for (BridgeMacLink link: links) {
            for (SharedSegment segment: segments) {
                if (segment.containsMac(link.getMacAddress())) {
                    segment.add(link);
                    break;
                }
                if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                    segment.add(link);
                    break;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segments.add(segment);
        }

        for (BridgeBridgeLink link: bridgelinks) {
            for (SharedSegment segment: segments) {
                if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                    segment.add(link);
                    break;
                }
                if (segment.containsPort(link.getDesignatedNode().getId(), link.getDesignatedPort())) {
                    segment.add(link);
                    break;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segments.add(segment);
        }
        
        // Assign the segment to domain
        for (SharedSegment segment: segments) {
            BroadcastDomain domain = null;
            for (BroadcastDomain curdomain: m_domains) {
                if (curdomain.containsAtleastOne(segment.getBridgeIdsOnSegment())) {
                    domain = curdomain;
                    break;
                }
            }
            if (domain == null) {
                domain = new BroadcastDomain(); 
                m_domains.add(domain);
            }
            domain.loadTopologyEntry(segment);
        }
       
        for (BridgeElement element: bridgeelements) {
            for (BroadcastDomain domain: m_domains) {
                if (domain.containBridgeId(element.getNode().getId())) {
                    domain.addBridgeElement(element);
                    break;
                }
            }
        }
        
        for (BridgeStpLink link: stplinks) {
            for (BroadcastDomain domain: m_domains) {
                if (domain.containBridgeId(link.getNode().getId())) {
                    domain.addSTPEntry(link);
                    break;
                }
            }
        }
    }

    @Override
    public synchronized void update(int nodeid) {
 
        List<BridgeMacLink> bft = m_notYetParsedBFTMap.remove(nodeid);
        Set<String>incomingSet = new HashSet<String>();
        for (BridgeMacLink link: bft)
            incomingSet.add(link.getMacAddress());
        BroadcastDomain bftdomain = null;
        for (BroadcastDomain domain: m_domains) {
            Set<String>retainedSet = new HashSet<String>(domain.getMacsOnDomain());
            retainedSet.retainAll(incomingSet);
            // should contain at list 10 or 10% of the all size
            if (retainedSet.size() < 10 && retainedSet.size() <= incomingSet.size()*0.1 ) {
                domain.removeBridge(nodeid);
                continue;          
            }
            bftdomain = domain;
            break;
        }
        if (bftdomain == null) {
            bftdomain = new BroadcastDomain();
            m_domains.add(bftdomain);
        }
        bftdomain.loadBFT(nodeid,bft,m_notYetParsedSTPMap.remove(nodeid),m_notYetParsedEleMap.remove(nodeid));
        
        List<BroadcastDomain> domains = new ArrayList<BroadcastDomain>();
        for (BroadcastDomain domain: m_domains) {
            if (domain.isEmpty())
                continue;
            domains.add(domain);
        }
        m_domains = domains;

    }

    @Override
    public synchronized void delete(int nodeid) {
        for (BroadcastDomain domain: m_domains) {
            if (domain.containBridgeId(nodeid))
                domain.removeBridge(nodeid);
        }
    }
    
    @Override
    public synchronized BroadcastDomain getBroadcastDomain(int nodeid) {
        for (BroadcastDomain domain: m_domains) {
            if (domain.containBridgeId(nodeid))
                return domain;
        }
        return null;
    }

}
