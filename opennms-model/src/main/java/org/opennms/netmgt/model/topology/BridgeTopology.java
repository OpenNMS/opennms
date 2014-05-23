package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BridgeTopology {
	
    private static final Logger LOG = LoggerFactory.getLogger(BridgeTopology.class);

    public class BridgeTopologyLinkCandidate {

        private final BridgeTopologyPort bridgeTopologyPort;
        private Set<String> macs = new HashSet<String>();
        private Set<Integer> targets = new HashSet<Integer>();
        
        public BridgeTopologyLinkCandidate(BridgeTopologyPort btp) {
            bridgeTopologyPort = btp;
        }

        public void removeMacs(Set<String> otherMacs, Integer targettomerge) {
        	targets.add(targettomerge);
        	Set<String> curmacs = new HashSet<String>();
    		for (String mac: getMacs()) {
    			if (otherMacs.contains(mac)) 
    				continue;
    			curmacs.add(mac);
    		}
    		macs = curmacs;
        }
        
        public Set<String> getMacs() {
        	if (macs.isEmpty())
        		return bridgeTopologyPort.getMacs();
        	return macs;
        }
        
        public boolean intersectionNull(BridgeTopologyLinkCandidate portcandidate) {
        	for (String mac: getMacs()) {
        		if (portcandidate.getMacs().contains(mac))
        			return false;
        	}
        	return true;
        }
        
        public void merge(BridgeTopologyLinkCandidate other) {
        	for (String mac: other.macs) {
        		if (bridgeTopologyPort.getMacs().contains(mac))
        				macs.add(mac);
        	}
        }
        
        public boolean strictContained(BridgeTopologyLinkCandidate portcandidate) {
        	for (String mac: getMacs()) {
        		if (!portcandidate.getMacs().contains(mac))
        			return false;
        	}
        	return true;
        }

		public BridgeTopologyPort getBridgeTopologyPort() {
			return bridgeTopologyPort;
		}

		public Set<Integer> getTargets() {
			return targets;
		}

		
		public void addTarget(Integer target) {
			this.targets.add(target);
		}
    }

    public class SwitchPort {
        public Integer getIfindex() {
			return ifindex;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((ifindex == null) ? 0 : ifindex.hashCode());
			result = prime * result
					+ ((nodeid == null) ? 0 : nodeid.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SwitchPort other = (SwitchPort) obj;
			if (ifindex == null) {
				if (other.ifindex != null)
					return false;
			} else if (!ifindex.equals(other.ifindex))
				return false;
			if (nodeid == null) {
				if (other.nodeid != null)
					return false;
			} else if (!nodeid.equals(other.nodeid))
				return false;
			return true;
		}

		public Integer getNodeid() {
			return nodeid;
		}
		
		private final Integer nodeid;
        private final Integer ifindex;
		public SwitchPort(Integer nodeid, Integer ifindex) {
			super();
			this.nodeid = nodeid;
			this.ifindex = ifindex;
		}
    	
    }
    
    public class BridgeTopologyPort {
        private final Integer nodeid;
        private final Integer bridgePort;
        private final Set<String> macs;

        public BridgeTopologyPort(Integer nodeid, Integer bridgePort,
                Set<String> macs) {
            super();
            this.nodeid = nodeid;
            this.bridgePort = bridgePort;
            this.macs = macs;
        }

        public Set<String> getMacs() {
            return macs;
        }

        public Integer getNodeid() {
            return nodeid;
        }

        public Integer getBridgePort() {
            return bridgePort;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((bridgePort == null) ? 0 : bridgePort.hashCode());
            result = prime * result
                    + ((nodeid == null) ? 0 : nodeid.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BridgeTopologyPort other = (BridgeTopologyPort) obj;
            if (bridgePort == null) {
                if (other.bridgePort != null)
                    return false;
            } else if (!bridgePort.equals(other.bridgePort))
                return false;
            if (nodeid == null) {
                if (other.nodeid != null)
                    return false;
            } else if (!nodeid.equals(other.nodeid))
                return false;
            return true;
        }
    }

    public class BridgeTopologyLink {
        public SwitchPort getLinkedSwitchPort() {
			return linkedSwitchPort;
		}

		public void setLinkedSwitchPort(SwitchPort linkedSwitchPort) {
			this.linkedSwitchPort = linkedSwitchPort;
		}

		final private BridgeTopologyPort bridgePort;
        private BridgeTopologyPort designatebridgePort;
        private SwitchPort linkedSwitchPort;

        private Set<String> macs = new HashSet<String>();

        public Set<String> getMacs() {
            return macs;
        }

        public BridgeTopologyPort getBridgeTopologyPort() {
            return bridgePort;
        }

        public BridgeTopologyPort getDesignatebridgePort() {
            return designatebridgePort;
        }

        public BridgeTopologyLink(BridgeTopologyPort bridgeport) {
            super();
            this.bridgePort = bridgeport;
            macs = bridgeport.getMacs();
        }

        public BridgeTopologyLink(BridgeTopologyPort bridgeport,
                BridgeTopologyPort designatedbridgePort) {
            super();
            this.bridgePort = bridgeport;
            this.designatebridgePort = designatedbridgePort;
            for (String mac : bridgeport.getMacs()) {
                if (designatedbridgePort.getMacs().contains(mac))
                    macs.add(mac);
            }
        }

        public boolean contains(BridgeTopologyPort bridgeport) {
            if (this.bridgePort.equals(bridgeport))
                return true;
            if (this.designatebridgePort != null
                    && this.designatebridgePort.equals(bridgeport))
                return true;
            return false;
        }

    }

    private List<BridgeTopologyLink> bridgelinks = new ArrayList<BridgeTopologyLink>();
    private Map<String,SwitchPort> bridgeAssociatedMacAddressMap = new HashMap<String, SwitchPort>();
    private List<BridgeTopologyLinkCandidate> bridgeTopologyPortCandidates = new ArrayList<BridgeTopologyLinkCandidate>();
    
    public void addBridgeAssociatedMac(Integer nodeid, Integer ifindex, String mac) {
        LOG.info("addBridgeAssociatedMac: adding nodeid {}, ifindex {}, mac {}", nodeid, ifindex,mac);
    	SwitchPort swPort = new SwitchPort(nodeid, ifindex);
    	bridgeAssociatedMacAddressMap.put(mac, swPort);
    }

    public void addNodeToTopology(LinkableNode bridgeNode) {
    	Integer nodeid = bridgeNode.getNodeId();
    	 
    	 for (final Entry<Integer,Set<String>> curEntry : bridgeNode.getBridgeForwardingTable().entrySet()) {
             LOG.info("addNodeToTopology: parsing node {}, port {}, macs {}",
                     nodeid, curEntry.getKey(),
                     curEntry.getValue());
             BridgeTopologyPort bridgetopologyport = new BridgeTopologyPort(
                     nodeid,
                     curEntry.getKey(),
                     curEntry.getValue());

             if (parsed(bridgetopologyport)) {
            	 LOG.info("addNodeToTopology: node {}, port {} has been previuosly parsed. Skipping.",nodeid, curEntry.getKey());
            	 continue;
             }

             BridgeTopologyLinkCandidate topologycandidate = new BridgeTopologyLinkCandidate(bridgetopologyport);
             for (String mac : curEntry.getValue()) {
                 if (bridgeAssociatedMacAddressMap.containsKey(mac)) {
                	 SwitchPort swPort = bridgeAssociatedMacAddressMap.get(mac);
    	             LOG.info("addNodeToTopology: parsing node {}, port {}, mac {} found on bridge adding target: targetnodeid {}, targetifindex {}",
    	                     nodeid, curEntry.getKey(),
                             curEntry.getValue(),swPort.getNodeid(),swPort.getIfindex());
                     topologycandidate.addTarget(bridgeAssociatedMacAddressMap.get(mac).getNodeid());
                 }
             }
             bridgeTopologyPortCandidates.add(parseBFTEntry(topologycandidate));
    	 }
    	 mergeTopology();
    }
    
    public void mergeTopology() {
    	Set<BridgeTopologyPort> parsedNode = new HashSet<BridgeTopologyPort>(); 
    	for (BridgeTopologyLinkCandidate candidateA: bridgeTopologyPortCandidates) {
    		if (parsedNode.contains(candidateA.getBridgeTopologyPort()))
    				continue;
    	    parsedNode.add(candidateA.getBridgeTopologyPort());
    		if (candidateA.getTargets().isEmpty()) 
    			continue;
			for (BridgeTopologyLinkCandidate candidateB: bridgeTopologyPortCandidates) {
				if (parsedNode.contains(candidateB.getBridgeTopologyPort()))
					continue;
				if (candidateB.getTargets().isEmpty())
					continue;
				if (candidateA.getBridgeTopologyPort().getNodeid().intValue() == candidateB.getBridgeTopologyPort().getNodeid().intValue())
					continue;
				if (candidateA.getTargets().contains(candidateB.getBridgeTopologyPort().getNodeid()) 
						&& candidateB.getTargets().contains(candidateA.getBridgeTopologyPort().getNodeid())) {
					candidateA.getTargets().clear();
					candidateB.getTargets().clear();
					candidateA.getTargets().add(candidateB.getBridgeTopologyPort().getNodeid());
					candidateB.getTargets().add(candidateA.getBridgeTopologyPort().getNodeid());
					candidateA.merge(candidateB);
					candidateB.merge(candidateA);
					parsedNode.add(candidateB.getBridgeTopologyPort());
				}
			}
    	}
    }
    
	public BridgeTopologyLinkCandidate parseBFTEntry(BridgeTopologyLinkCandidate topologyLinkCandidate) {
        for (BridgeTopologyLinkCandidate linkcandidate: bridgeTopologyPortCandidates) {
        	// regola intersezione nulla -> nessuna azione
        	if (linkcandidate.intersectionNull(topologyLinkCandidate))
        		continue;
        	// regola della dipendenza assoluta direzione avanti
        	if (linkcandidate.strictContained(topologyLinkCandidate)) {
            	topologyLinkCandidate.removeMacs(linkcandidate.getMacs(), linkcandidate.getBridgeTopologyPort().getNodeid());
            	continue;
        	}
        	// regola della dipendenza assoluta direzione dietro
        	if (topologyLinkCandidate.strictContained(linkcandidate)) {
        		linkcandidate.removeMacs(topologyLinkCandidate.getMacs(), topologyLinkCandidate.getBridgeTopologyPort().getNodeid());
        		continue;
        	}
        }
        return topologyLinkCandidate;
	}
	
    private boolean parsed(BridgeTopologyPort bridgePort) {
        for (BridgeTopologyLink link : bridgelinks) {
            if (link.contains(bridgePort))
                return true;
        }
        return false;
    }

    public void parseSTPEntry(Integer nodeid, Integer bridgePort,
            Set<String> macs, Integer designatednodeid,
            Integer designatedport, Set<String> designatedmacs) {

    	BridgeTopologyPort source = new BridgeTopologyPort(
                nodeid,
                bridgePort,
                macs);
    	BridgeTopologyPort designated = new BridgeTopologyPort(
                designatednodeid,
                designatedport,
                designatedmacs);
    	BridgeTopologyLinkCandidate sourceLink = new BridgeTopologyLinkCandidate(source);
    	BridgeTopologyLinkCandidate designatedLink = new BridgeTopologyLinkCandidate(designated);
        BridgeTopologyLink link = new BridgeTopologyLink(source,designated);
        LOG.info("parseSTPEntry: macs on bridge {}.", macs);
        LOG.info("parseSTPEntry: macs on designated bridge {}.",
                 designatedmacs);
        LOG.info("parseSTPEntry: nodeid {}, port {}, designated nodeid {}, designated port {}, macs on link {}.",
                 nodeid, bridgePort, designatednodeid, designatedport,
                 link.getMacs());
    	if (sourceLink.intersectionNull(designatedLink)) {
            bridgelinks.add(link);
    	} else {
        	sourceLink.addTarget(designatednodeid);
        	designatedLink.addTarget(nodeid);
    		bridgeTopologyPortCandidates.add(parseBFTEntry(sourceLink));        	
		    bridgeTopologyPortCandidates.add(parseBFTEntry(designatedLink));
		    mergeTopology();
	    }
    }

    public List<BridgeTopologyLink> getTopology() {
    	Set<BridgeTopologyPort> parsedNode = new HashSet<BridgeTopologyPort>(); 
    	for (BridgeTopologyLinkCandidate candidateA: bridgeTopologyPortCandidates) {
    		if (parsedNode.contains(candidateA.getBridgeTopologyPort()))
				continue;
    		if (candidateA.getTargets().isEmpty()) {
    			continue;
    		}
            LOG.info("getTopology: bridgetobridge discovery: parsing nodeidA {}, portA {}, macsA {}, targetsA {}.",
                    candidateA.getBridgeTopologyPort().getNodeid(), candidateA.getBridgeTopologyPort().getBridgePort(), 
                    candidateA.getMacs(), candidateA.getTargets());
			for (BridgeTopologyLinkCandidate candidateB: bridgeTopologyPortCandidates) {
				if (parsedNode.contains(candidateB.getBridgeTopologyPort()))
					continue;
				if (candidateB.getTargets().isEmpty()) {
					continue;
				}
                LOG.info("getTopology: bridgetobridge discovery: parsing nodeidB {}, portB {}, macsB {}, targetsB {}.",
                        candidateB.getBridgeTopologyPort().getNodeid(), candidateB.getBridgeTopologyPort().getBridgePort(), 
                        candidateB.getMacs(), candidateB.getTargets());
				if (candidateA.getBridgeTopologyPort().getNodeid().intValue() == candidateB.getBridgeTopologyPort().getNodeid().intValue())
					continue;
				if (candidateA.getTargets().contains(candidateB.getBridgeTopologyPort().getNodeid()) 
						&& candidateB.getTargets().contains(candidateA.getBridgeTopologyPort().getNodeid())) {
	        		parsedNode.add(candidateA.getBridgeTopologyPort());
					parsedNode.add(candidateB.getBridgeTopologyPort());
					BridgeTopologyLink link = new BridgeTopologyLink(candidateA.getBridgeTopologyPort(), candidateB.getBridgeTopologyPort());
					LOG.info("getTopology: link found {}", link);
					bridgelinks.add(link);
				}
			}
    	}
    	for (BridgeTopologyLinkCandidate candidate: bridgeTopologyPortCandidates) {
    		if (parsedNode.contains(candidate.getBridgeTopologyPort()))
				continue;
            LOG.info("getTopology: mac discovery: parsing nodeid {}, port {}, macs {}, targets {}.",
                    candidate.getBridgeTopologyPort().getNodeid(), candidate.getBridgeTopologyPort().getBridgePort(), 
                    candidate.getMacs(), candidate.getTargets());
    		BridgeTopologyLink link = new BridgeTopologyLink(new BridgeTopologyPort(candidate.getBridgeTopologyPort().getNodeid(), candidate.getBridgeTopologyPort().getBridgePort(), candidate.getMacs()));
			SwitchPort swPort = null;
    		for (String mac: candidate.getMacs()) {
    			if (!bridgeAssociatedMacAddressMap.containsKey(mac)) {
    				swPort = null;
    				break;
    			}
    			if (swPort != null && !swPort.equals(bridgeAssociatedMacAddressMap.get(mac))) {
    				swPort = null;
    				break;
    			} else {
                    swPort = bridgeAssociatedMacAddressMap.get(mac);
                    LOG.info("getTopology: parsing nodeid {}, port {}: mac {} is associated to switch Node {}, Port {}",
                            candidate.getBridgeTopologyPort().getNodeid(), candidate.getBridgeTopologyPort().getBridgePort(), 
                            mac, swPort.getNodeid(),swPort.getIfindex());        				
    			}
    		}
    		if (swPort != null)
    			link.setLinkedSwitchPort(swPort);
			LOG.info("getTopology: link found {}", link);
    		bridgelinks.add(link);
    	}
        return bridgelinks;
    }

}
