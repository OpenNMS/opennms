/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.OnmsNode;

public class BroadcastDomain {
    
    volatile Set<Bridge> m_bridges = new HashSet<Bridge>();

    volatile List<SharedSegment> m_topology = new ArrayList<SharedSegment>();    
    
    volatile Map<Integer,List<BridgeMacLink>> m_forwarding = new HashMap<Integer,List<BridgeMacLink>>();

    public void addForwarding(BridgeMacLink forward) {
        Integer bridgeid = forward.getNode().getId();
        if (bridgeid == null)
            return;
        if (!m_forwarding.containsKey(bridgeid))
            m_forwarding.put(bridgeid, new ArrayList<BridgeMacLink>());
        m_forwarding.get(bridgeid).add(forward);
    }
    
    public List<BridgeMacLink> getForwarders(Integer bridgeId) {
        if (!m_forwarding.containsKey(bridgeId))
            m_forwarding.put(bridgeId, new ArrayList<BridgeMacLink>());
        return m_forwarding.get(bridgeId);
    }
    
    public void cleanForwarders(Set<String> macs) {
        Map<Integer, List<BridgeMacLink>> forwadingMap=new HashMap<Integer, List<BridgeMacLink>>();
        for (Integer bridgeId: m_forwarding.keySet()) {
            List<BridgeMacLink> forwarders = new ArrayList<BridgeMacLink>();
            for (BridgeMacLink forward: m_forwarding.get(bridgeId)) {
                if (macs.contains(forward.getMacAddress()))
                    continue;
                forwarders.add(forward);
            }
            if (forwarders.isEmpty())
                continue;
            forwadingMap.put(bridgeId, forwarders);
        }
        m_forwarding = forwadingMap;
        
    }
    
    public Set<String> getBridgeMacAddresses(Integer bridgeid) {
		Set<String> bridgemacaddresses = new HashSet<String>();
		Bridge bridge = getBridge(bridgeid);
		if ( bridge != null ) {
			for (BridgeElement element: bridge.getBridgeElements()) {
				if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress()))
	                bridgemacaddresses.add(element.getBaseBridgeAddress());			}
		}
		return bridgemacaddresses;
	}

    public List<BridgeElement> getBridgeElements() {
    	List<BridgeElement> elements = new ArrayList<BridgeElement>();
    	for (Bridge bridge: m_bridges) {
    		for (BridgeElement element: bridge.getBridgeElements())
    			elements.add(element);
    	}
    	return elements;
    }

    public void setBridgeElements(List<BridgeElement> bridgeelements) {
    	for (Bridge bridge: m_bridges)
    		bridge.clearBridgeElement();
    	
E:    	for (BridgeElement element: bridgeelements) {
			for (Bridge bridge: m_bridges) {
				if (bridge.addBridgeElement(element)) {
					continue E;
				}
			}
		}
    }

    public void clearTopology() {
        m_forwarding.clear();
        m_topology.clear();
    }
    
    public boolean isEmpty() {
        return m_bridges.isEmpty();
    }

    public Set<Integer> getBridgeNodesOnDomain() {
        Set<Integer> bridgeIds = new HashSet<Integer>();
        for (Bridge bridge: m_bridges) 
            bridgeIds.add(bridge.getId());
        return bridgeIds;
    }

    public Set<Bridge> getBridges() {
        return m_bridges;
    }
    
    public List<SharedSegment> getTopology() {
        return m_topology;
    }
        
    public boolean hasRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return true;
        }
        return false;
        
    }
    public Integer getRootBridgeId() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge.getId();
        }
        return null;
    }

    public Bridge getRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge;
        }
        return null;
    }

    public void addBridge(Bridge bridge) {
        if (m_bridges.contains(bridge))
            return;
        m_bridges.add(bridge);
    }

    public Bridge getBridge(int bridgeId) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == bridgeId)
                return bridge;
        }
        return null;
    }

    public Set<String> getMacsOnDomain() {
        Set<String>macs = new HashSet<String>();
        for (SharedSegment segment: m_topology) 
            macs.addAll(segment.getMacsOnSegment());
        return macs;
    }

    public void add(SharedSegment segment) {
        m_topology.add(segment);
    }
    
    public void loadTopologyEntry(SharedSegment segment) {
        segment.setBroadcastDomain(this);
        m_topology.add(segment);
    }
    
    public void loadTopologyRoot() {
        if (m_bridges.size() == 1) {
            hierarchySetUp(m_bridges.iterator().next());
            return;
        }

        Integer designated = null;
        for (SharedSegment segment: m_topology) {
            Set<Integer> children = segment.getBridgeIdsOnSegment();
            if (children.size() == 1)
                continue;
            designated = segment.getDesignatedBridge();
            loadTopologyRoot(designated);
            return;
        }
    }
    
    private void loadTopologyRoot(Integer bridgeId) {
        for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridgeId)) {
            if (segment.getDesignatedBridge().intValue() != bridgeId.intValue()) {
                loadTopologyRoot(segment.getDesignatedBridge());
                return;
            }
        }
        hierarchySetUp(getBridge(bridgeId));
    }

    public boolean containsAtleastOne(Set<Integer> nodeids) {
        for (Bridge bridge: m_bridges) {
            for (Integer nodeid:nodeids) {
                if (bridge.getId().intValue() == nodeid.intValue())
                    return true;
            }
        }
        return false;
    }
    
    public boolean containBridgeId(int nodeid) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeid)
                return true;
        }
        return false;
    }
    
    public synchronized void removeBridge(int bridgeId) {
        Bridge bridge = null;
        for (Bridge curbridge: m_bridges) {
            if (curbridge.getId() == bridgeId) {
                bridge=curbridge;
                break;
            }
        }
        // if not in domain: return
        if (bridge==null)
            return;
        // if last bridge in domain: clear all and return
        if (m_bridges.size() == 1) {
            m_topology.clear();
            m_bridges.clear();
            return;
        }
        
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge cur: m_bridges) {
            if (cur.getId().intValue() == bridgeId) 
                continue;
            bridges.add(cur);
        }
        m_bridges = bridges;            
    }
    
    public List<SharedSegment> getSharedSegmentOnTopologyForBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
        }
        return segmentsOnBridge;
    }

    public List<SharedSegment> removeSharedSegmentOnTopologyForBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        List<SharedSegment> topology = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
            else 
                topology.add(segment);
        }
        m_topology = topology;
        return segmentsOnBridge;
    }
    
    public Set<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        Set<Integer> nodeidsOnSegment = new HashSet<Integer>(segment.getBridgeIdsOnSegment());
        Set<Bridge> bridgesOn = new HashSet<Bridge>();
        for (Bridge bridge: m_bridges) {
            if (nodeidsOnSegment.contains(bridge.getId()))
                bridgesOn.add(bridge);
        }
        return bridgesOn;
    }

    public SharedSegment getSharedSegment(Integer bridgeId, Integer bridgePort) {
        if (bridgeId == null || bridgePort == null)
            return null;
        for (SharedSegment segment: m_topology) {
            if (segment.containsPort(bridgeId, bridgePort)) 
                return segment;
        }
        return null;
    }    
    
    public void hierarchySetUp(Bridge root) {
        if (root== null || root.isRootBridge())
            return;
        root.setRootBridge(true);
        root.setRootPort(null);
        if (m_bridges.size() == 1)
            return;
        for (SharedSegment segment : getSharedSegmentOnTopologyForBridge(root.getId())) {
            segment.setDesignatedBridge(root.getId());
            tier(segment, root.getId(), 0);
        }
    }
    
    private void tier(SharedSegment segment, Integer rootid, int level) {
        if (segment == null)
            return;
        level++;
        if (level == 30)
            return;
        for (Integer bridgeid: segment.getBridgeIdsOnSegment()) {
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            Bridge bridge = getBridge(bridgeid);
            if (bridge == null)
                return;
            bridge.setRootPort(segment.getPortForBridge(bridgeid));
            bridge.setRootBridge(false);
            for (SharedSegment s2: getSharedSegmentOnTopologyForBridge(bridgeid)) {
                if (s2.getDesignatedBridge() != null && s2.getDesignatedBridge().intValue() == rootid.intValue())
                    continue;
                s2.setDesignatedBridge(bridgeid);
                tier(s2,bridgeid,level);
            }
        }
    }
    
    public void clearTopologyForBridge(Integer bridgeId) {
        m_forwarding.remove(bridgeId);
    	Bridge bridge = getBridge(bridgeId);
    	if (bridge == null)
    		return;
        SharedSegment topsegment = getSharedSegment(bridge.getId(), bridge.getRootPort());
        if (bridge.isRootBridge()) {
            for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridgeId)) {
                Integer newRootId = segment.getFirstNoDesignatedBridge();
                if (newRootId == null)
                    continue;
                Bridge newRootBridge=null;
                for (Bridge curBridge: getBridges()) {
                    if (curBridge.getId().intValue() == newRootId.intValue()) {
                        newRootBridge=curBridge;
                        break;
                    }
                }
                if (newRootBridge == null)
                    continue;
                topsegment = getSharedSegment(newRootId,newRootBridge.getRootPort());
                hierarchySetUp(newRootBridge);
                break;
            }
        }
        //all the topology will be merged with the segment for bridge designated port
        if (topsegment != null) {
            topsegment.removeBridge(bridge.getId());
        }

        for (SharedSegment segment: removeSharedSegmentOnTopologyForBridge(bridge.getId())) {
            if (topsegment != null)
                topsegment.mergeBridge(segment,bridge.getId());
        }        

    }

    public List<BridgeMacLink> calculateRootBFT() {
    	Bridge root = getRootBridge();
    	if (root == null)
    		return null;
    	return calculateBFT(root);
    }
    
    public List<BridgeMacLink> calculateBFT(Bridge bridge) {
        Map<Integer,Set<String>> bft = new HashMap<Integer, Set<String>>();
        Map<Integer,BridgePort> portifindexmap = new HashMap<Integer, BridgePort>();
        Integer bridgeId = bridge.getId();
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        OnmsNode node=new OnmsNode();
        node.setId(bridgeId);
        for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridgeId)) {
            Integer bridgeport =segment.getPortForBridge(bridgeId);
            BridgePort bridgeportifIndex = segment.getBridgePort(bridgeId);
            portifindexmap.put(bridgeport, bridgeportifIndex);

        }
        synchronized (m_topology) {
            for (SharedSegment segment: m_topology) {
                
                Set<String> macs = segment.getMacsOnSegment();
                
                if (macs == null || macs.isEmpty())
                    continue;
                Integer bridgeport = goUp(segment,bridge,0);
                if (!bft.containsKey(bridgeport))
                    bft.put(bridgeport, new HashSet<String>());
                bft.get(bridgeport).addAll(macs);
           }
        }
            
        for (Integer bridgePort: bft.keySet()) {
            for (String mac: bft.get(bridgePort)) {
                BridgeMacLink link = new BridgeMacLink();
                link.setNode(node);
                link.setBridgePort(bridgePort);
                link.setBridgePortIfIndex(portifindexmap.get(bridgePort).getBridgePortIfIndex());
                link.setBridgePortIfName(portifindexmap.get(bridgePort).getBridgePortIfName());
                link.setVlan(portifindexmap.get(bridgePort).getVlan());
                link.setMacAddress(mac);
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                links.add(link);
            }
        }
        if (m_forwarding.containsKey(bridgeId))
            links.addAll(m_forwarding.get(bridgeId));
        return links;
    }
    
    private Integer goUp(SharedSegment down,Bridge bridge, int level) {
        if (level == 30) {
            clearTopology();
            return -1;
        }
            Integer upBridgeId = down.getDesignatedBridge();
            // if segment is on the bridge then...
            if (upBridgeId.intValue() == bridge.getId().intValue()) {
                return down.getDesignatedPort();
            }
            // if segment is a root segment add mac on port
            if (upBridgeId.intValue() == getRootBridgeId().intValue()) {
                return bridge.getRootPort();
            }
            // iterate until you got it
            Bridge upBridge = null;
            for (Bridge cbridge: getBridges()) {
                if (cbridge.getId().intValue() == bridge.getId().intValue())
                    continue;
                if (cbridge.getId().intValue() == upBridgeId.intValue()) {
                    upBridge=cbridge;
                    break;
                }
            }
            if (upBridge == null) {
                return null;
            }
            SharedSegment up = getSharedSegment(upBridge.getId(),upBridge.getRootPort());
            if (up == null) {
                return null;
            }
        return goUp(up, bridge,++level);
    }    

    public void clear() {
        m_topology.clear();
        m_bridges.clear();
        m_forwarding.clear();
    }
    
    public String printTopology() {
    	final StringBuilder strbfr = new StringBuilder();
        strbfr.append("\n------broadcast domain-----\n");
        strbfr.append("domain bridges:");
        strbfr.append(getBridgeNodesOnDomain());
        strbfr.append("\n");
    	if (hasRootBridge()) {
    		Set<Integer> rootids = new HashSet<Integer>();
    		rootids.add(getRootBridgeId());
    		strbfr.append("rootbridge: ");
    		strbfr.append(getRootBridgeId());
    		strbfr.append("\n");
    		strbfr.append(printTopologyFromLevel(rootids,0));
    	} else {
    		for (SharedSegment shared: getTopology())
			strbfr.append(shared.printTopology());
    	}
        strbfr.append("------broadcast domain-----");
    	return strbfr.toString();
    }
    
    public String printTopologyFromLevel(Set<Integer> bridgeIds, int level) {
    	Set<Integer> bridgesDownLevel = new HashSet<Integer>();
    	final StringBuilder strbfr = new StringBuilder();
        strbfr.append("------level ");
    	strbfr.append(level);
        strbfr.append(" -----\n");

        strbfr.append("bridges on level:");
        strbfr.append(bridgeIds);
        strbfr.append("\n");
        bridgeIds.stream()
                .map(id -> getBridge(id))
                .filter(bridge -> bridge != null)
                .forEach(bridge -> {
                    strbfr.append(bridge.printTopology());
                    for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridge.getId())) {
                        if (segment.getDesignatedBridge().intValue() == bridge.getId().intValue()) {
                            strbfr.append(segment.printTopology());
                            bridgesDownLevel.addAll(segment.getBridgeIdsOnSegment());
                        }
                    }
                });

        strbfr.append("------level ");
    	strbfr.append(level);
        strbfr.append(" -----\n");
        bridgesDownLevel.removeAll(bridgeIds);
    	if (!bridgesDownLevel.isEmpty())
    		strbfr.append(printTopologyFromLevel(bridgesDownLevel,level+1));
    	return strbfr.toString();
    }
    
    public static String printTopologyBFT(List<BridgeMacLink> bft) {
        final StringBuilder strbfr = new StringBuilder();
        for (BridgeMacLink link: bft) {
            strbfr.append("nodeid:[");
            strbfr.append(link.getNode().getId());
            strbfr.append("]:");
            strbfr.append(link.getMacAddress());
            strbfr.append(":bridgeport:");
            strbfr.append(link.getBridgePort());
            strbfr.append(":ifindex:");
            strbfr.append(link.getBridgePortIfIndex());
            strbfr.append("\n");
    	}
        return strbfr.toString();
    }

    public static String printTopologyLink(BridgeMacLink link) {
        final StringBuilder strbfr = new StringBuilder();
            strbfr.append("nodeid:[");
            strbfr.append(link.getNode().getId());
            strbfr.append("]:");
            strbfr.append(link.getMacAddress());
            strbfr.append(":bridgeport:");
            strbfr.append(link.getBridgePort());
            strbfr.append(":ifindex:");
            strbfr.append(link.getBridgePortIfIndex());
        return strbfr.toString();
    }

    public Bridge electRootBridge() {
        if (getBridges().size() == 1) 
            return getBridges().iterator().next();
        
            //if null try set the stp roots
        Set<String> rootBridgeIds=new HashSet<String>();
        for (Bridge bridge: m_bridges) {
        	for (BridgeElement element: bridge.getBridgeElements() ) {
        		if (InetAddressUtils.
        				isValidStpBridgeId(element.getStpDesignatedRoot()) 
        				&& !element.getBaseBridgeAddress().
        				equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
        			rootBridgeIds.add(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()));
        		}
        	}
        }
        //well only one root bridge should be defined....
        //otherwise we need to skip calculation
        //so here is the place were we can
        //manage multi stp domains...
        //ignoring for the moment....
        for (String rootBridgeId: rootBridgeIds) {
            for (Bridge bridge: m_bridges) {
            	for (BridgeElement element: bridge.getBridgeElements() ) {
            		if (element.getBaseBridgeAddress().equals((rootBridgeId))) {
            			return bridge;
            		}
            	}
            }
        }

        return null;
    }
    
}
