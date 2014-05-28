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

	public enum BridgePortRole{BACKBONE,DIRECT};
    
    public class BridgeTopologyLinkCandidate {

        private final BridgeTopologyPort bridgeTopologyPort;
        private Set<String> macs = new HashSet<String>();
        private Set<Integer> targets = new HashSet<Integer>();
        private BridgePortRole role;
        private BridgeTopologyPort linkportcandidate;
        private Set<SwitchPort> linkedswitchports = new HashSet<BridgeTopology.SwitchPort>(); 
        
        public BridgeTopologyLinkCandidate(BridgeTopologyPort btp) {
            bridgeTopologyPort = btp;
        }

        public void removeMacs(Set<String> otherMacs) {
        	Set<String> curmacs = new HashSet<String>();
    		for (String mac: getMacs()) {
    			if (otherMacs.contains(mac)) 
    				continue;
    			curmacs.add(mac);
    		}
    		role = BridgePortRole.BACKBONE;
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

		public BridgePortRole getRole() {
			return role;
		}

		public void setRole(BridgePortRole role) {
			this.role = role;
		}

		public BridgeTopologyPort getLinkPortCandidate() {
			return linkportcandidate;
		}

		public void setLinkPortCandidate(BridgeTopologyPort linkportcandidate) {
			this.linkportcandidate =linkportcandidate;
		}

		public Set<SwitchPort> getLinkedSwitchPorts() {
			return linkedswitchports;
		}

		public void addLinkedSwitchPort(SwitchPort linkedswitchport) {
			linkedswitchports.add(linkedswitchport);
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
		final private BridgeTopologyPort bridgePort;
        private BridgeTopologyPort designatebridgePort;
        private SwitchPort linkedSwitchPort;

        private Set<String> macs = new HashSet<String>();

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

        public Set<String> getMacs() {
            return macs;
        }

        public BridgeTopologyPort getBridgeTopologyPort() {
            return bridgePort;
        }

        public BridgeTopologyPort getDesignatebridgePort() {
            return designatebridgePort;
        }

        public SwitchPort getLinkedSwitchPort() {
			return linkedSwitchPort;
		}

		public void setLinkedSwitchPort(SwitchPort linkedSwitchPort) {
			this.linkedSwitchPort = linkedSwitchPort;
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
    private Map<String,Set<SwitchPort>> bridgeAssociatedMacAddressMap = new HashMap<String, Set<SwitchPort>>();
    private List<BridgeTopologyLinkCandidate> bridgeTopologyPortCandidates = new ArrayList<BridgeTopologyLinkCandidate>();
    
    public void addBridgeAssociatedMac(Integer nodeid, Integer ifindex, String mac) {
        LOG.info("addBridgeAssociatedMac: adding nodeid {}, ifindex {}, mac {}", nodeid, ifindex,mac);
    	if (bridgeAssociatedMacAddressMap.containsKey(mac))
    		bridgeAssociatedMacAddressMap.get(mac).add(new SwitchPort(nodeid, ifindex));
    	else {
    		Set<SwitchPort> ports = new HashSet<BridgeTopology.SwitchPort>();
    		ports.add(new SwitchPort(nodeid, ifindex));
    		bridgeAssociatedMacAddressMap.put(mac, ports);
    	}
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
                	 for (SwitchPort swPort : bridgeAssociatedMacAddressMap.get(mac)) {
                		 if (swPort.getNodeid().intValue() == bridgeNode.getNodeId())
                			 continue;
                		 LOG.info("addNodeToTopology: parsing node {}, port {}: mac {} found on bridge adding target: targetnodeid {}, targetifindex {}",
    	                     nodeid, curEntry.getKey(),
                             mac,swPort.getNodeid(),swPort.getIfindex());
                		 topologycandidate.addLinkedSwitchPort(swPort);
                		 topologycandidate.addTarget(swPort.getNodeid());
                	 }
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
        LOG.info("parseBFTEntry: parsing node {}, port {}: mac {}",topologyLinkCandidate.getBridgeTopologyPort().getNodeid(),topologyLinkCandidate.getBridgeTopologyPort().getBridgePort(), topologyLinkCandidate.getMacs());
        for (BridgeTopologyLinkCandidate linkcandidate: bridgeTopologyPortCandidates) {
            LOG.info("parseBFTEntry: checking node {}, port {}: mac {}",linkcandidate.getBridgeTopologyPort().getNodeid(),linkcandidate.getBridgeTopologyPort().getBridgePort(), linkcandidate.getMacs());
            // regola intersezione nulla non faccio niente
        	if (linkcandidate.intersectionNull(topologyLinkCandidate)) {
        		continue;
        	} 
            // regola della dipendenza assoluta direzione avanti
        	if (linkcandidate.strictContained(topologyLinkCandidate)) {
                LOG.info("parseBFTEntry: adding target {} to node {}, port {}", linkcandidate.getBridgeTopologyPort().getNodeid(),
                		topologyLinkCandidate.getBridgeTopologyPort().getNodeid(),topologyLinkCandidate.getBridgeTopologyPort().getBridgePort());
            	topologyLinkCandidate.removeMacs(linkcandidate.getMacs());
            	topologyLinkCandidate.addTarget(linkcandidate.getBridgeTopologyPort().getNodeid());
            	topologyLinkCandidate.setRole(BridgePortRole.BACKBONE);
            	linkcandidate.setRole(BridgePortRole.DIRECT);
            	continue;
        	} 
        	// regola della dipendenza assoluta direzione dietro
        	if (topologyLinkCandidate.strictContained(linkcandidate)) {
                LOG.info("parseBFTEntry: adding target {} to node {}, port {}", topologyLinkCandidate.getBridgeTopologyPort().getNodeid(),
                		linkcandidate.getBridgeTopologyPort().getNodeid(),linkcandidate.getBridgeTopologyPort().getBridgePort());
        		linkcandidate.removeMacs(topologyLinkCandidate.getMacs());
            	linkcandidate.addTarget(topologyLinkCandidate.getBridgeTopologyPort().getNodeid());
            	linkcandidate.setRole(BridgePortRole.BACKBONE);
            	topologyLinkCandidate.setRole(BridgePortRole.DIRECT);
        		continue;
        	}
        	/* 
        	 * This class is designed to get the topology on one bridge forwarding table at a time
        	 * so this means that the rules are written considering port1 belonging 
        	 * always to the same bridge.
        	 * 
        	 * 
        	 * We assume the following:
        	 * 
        	 * 1) there where no loops into the network (so there is a hierarchy)
        	 * 
        	 * Corollary 1
        	 * 
        	 * If exists there is only one backbone port from sw1 and sw2
        	 * If exists there is only one backbone port from sw2 and sw1
        	 * 
        	 * Corollary 2
        	 * There is only one "pseudo device" containing the bridge
        	 * 
        	 * Corollary 3
        	 * on a backbone port two different mac address must belong to the same pseudo device
        	 * 
        	 */
        	
        	if (linkcandidate.getRole() == BridgePortRole.BACKBONE) {
        		linkcandidate.removeMacs(topologyLinkCandidate.getMacs());
            	topologyLinkCandidate.setRole(BridgePortRole.DIRECT);
            	continue;
        	}

        	if (topologyLinkCandidate.getRole() == BridgePortRole.BACKBONE) {
        		topologyLinkCandidate.removeMacs(linkcandidate.getMacs());
            	linkcandidate.setRole(BridgePortRole.DIRECT);
            	continue;
        	}

        	if (linkcandidate.getLinkPortCandidate() == null) {
        		linkcandidate.setLinkPortCandidate(topologyLinkCandidate.getBridgeTopologyPort());
        		topologyLinkCandidate.setLinkPortCandidate(linkcandidate.getBridgeTopologyPort());
        		continue;
        	} 

        	if (topologyLinkCandidate.getBridgeTopologyPort().getNodeid().intValue() == linkcandidate.getLinkPortCandidate().getNodeid().intValue()  
        			&& topologyLinkCandidate.getBridgeTopologyPort().getBridgePort().intValue() != linkcandidate.getLinkPortCandidate().getBridgePort().intValue()) {
        		linkcandidate.removeMacs(linkcandidate.getLinkPortCandidate().getMacs());
        		linkcandidate.removeMacs(topologyLinkCandidate.getMacs());
            	topologyLinkCandidate.setRole(BridgePortRole.DIRECT);
            	linkcandidate.addTarget(topologyLinkCandidate.getBridgeTopologyPort().getNodeid());
            	linkcandidate.setRole(BridgePortRole.BACKBONE);
            	continue;
        	}
        	
        	if (linkcandidate.getBridgeTopologyPort().getNodeid().intValue() == topologyLinkCandidate.getLinkPortCandidate().getNodeid().intValue()
        			&& linkcandidate.getBridgeTopologyPort().getBridgePort().intValue() != topologyLinkCandidate.getLinkPortCandidate().getBridgePort().intValue()) {
        		topologyLinkCandidate.removeMacs(topologyLinkCandidate.getLinkPortCandidate().getMacs());
        		topologyLinkCandidate.removeMacs(linkcandidate.getMacs());
        		linkcandidate.setRole(BridgePortRole.DIRECT);
        		topologyLinkCandidate.addTarget(linkcandidate.getBridgeTopologyPort().getNodeid());
        		topologyLinkCandidate.setRole(BridgePortRole.BACKBONE);
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
    	Set<BridgeTopologyPort> foundedOnBridgeLink = new HashSet<BridgeTopologyPort>(); 
    	
    	for (BridgeTopologyLinkCandidate candidateA: bridgeTopologyPortCandidates) {
    		if (foundedOnBridgeLink.contains(candidateA.getBridgeTopologyPort()))
				continue;
    		if (candidateA.getTargets().isEmpty()) {
    			continue;
    		}
            LOG.info("getTopology: bridgetobridge discovery: parsing nodeidA {}, portA {}, targetsA {}.",
                    candidateA.getBridgeTopologyPort().getNodeid(), candidateA.getBridgeTopologyPort().getBridgePort(), 
                    candidateA.getTargets());
			for (BridgeTopologyLinkCandidate candidateB: bridgeTopologyPortCandidates) {
				if (foundedOnBridgeLink.contains(candidateB.getBridgeTopologyPort()))
					continue;
				if (candidateB.getTargets().isEmpty())
					continue;
				if (candidateA.getBridgeTopologyPort().getNodeid().intValue() == candidateB.getBridgeTopologyPort().getNodeid().intValue())
					continue;
                LOG.info("getTopology: bridgetobridge discovery: parsing nodeidB {}, portB {}, targetsB {}.",
                        candidateB.getBridgeTopologyPort().getNodeid(), candidateB.getBridgeTopologyPort().getBridgePort(), 
                        candidateB.getTargets());
				if (candidateA.getTargets().contains(candidateB.getBridgeTopologyPort().getNodeid()) 
						&& candidateB.getTargets().contains(candidateA.getBridgeTopologyPort().getNodeid())) {
					foundedOnBridgeLink.add(candidateB.getBridgeTopologyPort());
		    		foundedOnBridgeLink.add(candidateA.getBridgeTopologyPort());
					BridgeTopologyLink link = new BridgeTopologyLink(candidateA.getBridgeTopologyPort(), candidateB.getBridgeTopologyPort());
					LOG.info("getTopology: bridgetobridge discovery: link found {}", link);
					bridgelinks.add(link);
				}
			}
    	}
    	for (BridgeTopologyLinkCandidate candidate: bridgeTopologyPortCandidates) {
    		if (foundedOnBridgeLink.contains(candidate.getBridgeTopologyPort()))
				continue;
            LOG.info("getTopology: mac discovery: parsing nodeid {}, port {}, macs {}, targets {}.",
                    candidate.getBridgeTopologyPort().getNodeid(), candidate.getBridgeTopologyPort().getBridgePort(), 
                    candidate.getMacs(), candidate.getTargets());
    		if (candidate.getLinkedSwitchPorts().isEmpty()) {
        		BridgeTopologyLink link = new BridgeTopologyLink(new BridgeTopologyPort(candidate.getBridgeTopologyPort().getNodeid(), candidate.getBridgeTopologyPort().getBridgePort(), candidate.getMacs()));
    			LOG.info("getTopology: link found {}", link);
        		bridgelinks.add(link);
        		continue;
    		} 
    		for (SwitchPort swport: candidate.getLinkedSwitchPorts()) {
        		BridgeTopologyLink link = new BridgeTopologyLink(new BridgeTopologyPort(candidate.getBridgeTopologyPort().getNodeid(), candidate.getBridgeTopologyPort().getBridgePort(), candidate.getMacs()));
    			link.setLinkedSwitchPort(swport);
    			LOG.info("getTopology: link found {}", link);
        		bridgelinks.add(link);
    		}
    	}
        return bridgelinks;
    }

}
