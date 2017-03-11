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
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;

public class BroadcastDomain {
    
    volatile Set<Bridge> m_bridges = new HashSet<Bridge>();

    volatile List<SharedSegment> m_topology = new ArrayList<SharedSegment>();    
    
    boolean m_lock = false;

    Object m_locker;
    
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
    
    public synchronized boolean getLock(Object locker) {
        if (m_lock)
            return false;
        if (locker == null)
            return false;
        m_lock=true;
        m_locker=locker;
        return true;
    }

    public synchronized boolean releaseLock(Object locker) {
        if (locker == null)
            return false;
        if (!m_lock )
            return false;
        if (!m_locker.equals(locker))
            return false;
        m_locker = null;
        m_lock=false; 
        return true;
                
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
        if (root.isRootBridge())
            return;
        root.setRootBridge(true);
        root.setRootPort(null);
        if (m_bridges.size() == 1)
            return;
        for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(root.getId())) {
            segment.setDesignatedBridge(root.getId());
            tier(segment, root.getId());
        }
    }
    
    private void tier(SharedSegment segment, Integer rootid) {
        for (Integer bridgeid: segment.getBridgeIdsOnSegment()) {
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            Bridge bridge = getBridge(bridgeid);
            bridge.setRootPort(segment.getPortForBridge(bridgeid));
            bridge.setRootBridge(false);
            for (SharedSegment s2: getSharedSegmentOnTopologyForBridge(bridgeid)) {
                if (s2.getDesignatedBridge() != null && s2.getDesignatedBridge().intValue() == rootid.intValue())
                    continue;
                s2.setDesignatedBridge(bridgeid);
                tier(s2,bridgeid);
            }
        }
    }
    
    public void clearTopologyForBridge(Integer bridgeId) {
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
                        System.out.println("root bridge" +newRootBridge.getId());
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
        Integer bridgeId = bridge.getId();
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        OnmsNode node=new OnmsNode();
        node.setId(bridgeId);
        for (SharedSegment segment: getTopology()) {
            
            Set<String> macs = segment.getMacsOnSegment();
            
            if (macs == null || macs.isEmpty())
                continue;
            Integer bridgeport = goUp(segment,bridge,0);
            if (!bft.containsKey(bridgeport))
                bft.put(bridgeport, new HashSet<String>());
            bft.get(bridgeport).addAll(macs);
       }
            
        for (Integer bridgePort: bft.keySet()) {
            for (String mac: bft.get(bridgePort)) {
                BridgeMacLink link = new BridgeMacLink();
                link.setNode(node);
                link.setBridgePort(bridgePort);
                link.setMacAddress(mac);
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                links.add(link);
            }
        }
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
    }
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("\n------broadcast domain-----\n");
        strbfr.append("domain bridges:");
        strbfr.append(getBridgeNodesOnDomain());
        strbfr.append("\n");
        strbfr.append("domain macs: ");
        strbfr.append(getMacsOnDomain());
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
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("------level ");
    	strbfr.append(level);
        strbfr.append(" -----\n");

        strbfr.append("bridges on level:");
        strbfr.append(bridgeIds);
        strbfr.append("\n");
        for (Integer bridgeid : bridgeIds) {
        	strbfr.append(getBridge(bridgeid).printTopology());
        	for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridgeid)) {
        		if (segment.getDesignatedBridge().intValue() == bridgeid.intValue()) {
        			strbfr.append(segment.printTopology());
        			bridgesDownLevel.addAll(segment.getBridgeIdsOnSegment());
        		}
        	}
        }
        
        strbfr.append("------level ");
    	strbfr.append(level);
        strbfr.append(" -----\n");
        bridgesDownLevel.removeAll(bridgeIds);
    	if (!bridgesDownLevel.isEmpty())
    		strbfr.append(printTopologyFromLevel(bridgesDownLevel,level+1));
    	return strbfr.toString();
    }
    
    public static String printTopologyBFT(List<BridgeMacLink> bft) {
    	StringBuffer strbfr = new StringBuffer();
    	for (BridgeMacLink link: bft) {
            strbfr.append("nodeid:[");
            strbfr.append(link.getNode().getId());
            strbfr.append("]:");
            strbfr.append(link.getMacAddress());
            strbfr.append(":bridgeport:");
            strbfr.append(link.getBridgePort());
            strbfr.append("\n");
    	}
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
