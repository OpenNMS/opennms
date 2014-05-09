/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2006-2012 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to discover link among nodes using the collected and
 * the necessary SNMP information. When the class is initially constructed no
 * information is used.
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo </a>
 */
public final class DiscoveryLink implements ReadyRunnable {

    private class BridgeTopologyLinkCandidate {

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

    private class SwitchPort {
        public Integer getIfindex() {
			return ifindex;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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
		private DiscoveryLink getOuterType() {
			return DiscoveryLink.this;
		}
    	
    }
    
    private class BridgeTopologyPort {
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
            result = prime * result + getOuterType().hashCode();
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
            if (!getOuterType().equals(other.getOuterType()))
                return false;
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

        private DiscoveryLink getOuterType() {
            return DiscoveryLink.this;
        }

    }

    private class BridgeTopologyLink {
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

    private class BridgeTopology {

    	
        private List<BridgeTopologyLink> bridgelinks = new ArrayList<BridgeTopologyLink>();
        private Map<String,SwitchPort> bridgeAssociatedMacAddressMap = new HashMap<String, SwitchPort>();
        private List<BridgeTopologyLinkCandidate> bridgeTopologyPortCandidates = new ArrayList<DiscoveryLink.BridgeTopologyLinkCandidate>();
        
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

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryLink.class);

    private String packageName;

    private List<NodeToNodeLink> m_links = new ArrayList<NodeToNodeLink>();

    private boolean discoveryUsingRoutes = true;

    private boolean discoveryUsingCdp = true;

    private boolean discoveryUsingBridge = true;

    private boolean discoveryUsingLldp = true;

    private boolean discoveryUsingOspf = true;

    private boolean discoveryUsingIsis = true;

    private boolean discoveryUsingWifi = true;

    private boolean suspendCollection = false;

    private boolean runned = false;

    /**
     * The scheduler object
     */
    private Scheduler m_scheduler;

    /**
     * The interval default value 30 min
     */
    private long m_interval = 1800000;

    /**
     * The interval default value 5 min It is the time in ms after snmp
     * collection is started
     */
    private long discovery_delay = 300000;

    /**
     * The initial sleep time default value 10 min
     */
    private long m_initial_sleep_time = 600000;

    private Linkd m_linkd;

    /**
     * @param linkd
     *            the linkd to set
     */
    public void setLinkd(Linkd linkd) {
        this.m_linkd = linkd;
    }

    public Linkd getLinkd() {
        return m_linkd;
    }

    /**
     * Constructs a new DiscoveryLink object . The discovery does not occur
     * until the <code>run</code> method is invoked.
     */
    public DiscoveryLink() {
        super();
    }

    private void sendSuspendedEvent() {
        sendEvent(new EventBuilder(
                                   "uei.opennms.org/internal/linkd/linkDiscoverySuspended",
                                   "Linkd"));
    }

    private void sendStartedEvent() {
        sendEvent(new EventBuilder(
                                   "uei.opennms.org/internal/linkd/linkDiscoveryStarted",
                                   "Linkd"));
    }

    private void sendCompletedEvent() {
        sendEvent(new EventBuilder(
                                   "uei.opennms.org/internal/linkd/linkDiscoveryCompleted",
                                   "Linkd"));
    }

    private void sendEvent(EventBuilder builder) {
        builder.addParam("runnable", "discoveryLink/" + getPackageName());
        m_linkd.getEventForwarder().sendNow(builder.getEvent());
    }

    /**
     * <p>
     * Performs link discovery for the Nodes and save info in
     * DatalinkInterface table on DataBase
     * <p>
     * No synchronization is performed, so if this is used in a separate
     * thread context synchronization must be added.
     * </p>
     */
    @Override
    public void run() {
        runned = true;
        LOG.info("run: discoveryUsingRoutes={} on package \"{}\"",
                 discoveryUsingRoutes, getPackageName());
        LOG.info("run: discoveryUsingOspf={} on package \"{}\"",
                 discoveryUsingOspf, getPackageName());
        LOG.info("run: discoveryUsingIsis={} on package \"{}\"",
                 discoveryUsingIsis, getPackageName());
        LOG.info("run: discoveryUsingWifi={} on package \"{}\"",
                 discoveryUsingWifi, getPackageName());
        LOG.info("run: discoveryUsingBridge={} on package \"{}\"",
                 discoveryUsingBridge, getPackageName());
        LOG.info("run: discoveryUsingCdp={} on package \"{}\"",
                 discoveryUsingCdp, getPackageName());
        LOG.info("run: discoveryUsingLldp={} on package \"{}\"",
                 discoveryUsingLldp, getPackageName());
        if (suspendCollection) {
            sendSuspendedEvent();
            LOG.warn("run: linkd collections are suspended!");
        } else {
            sendStartedEvent();
            discoverLinks();
            sendCompletedEvent();
        }
    }

    private void discoverLinks() {
        Collection<LinkableNode> linkableNodes = m_linkd.getLinkableNodesOnPackage(getPackageName());
        LOG.info("run: Found {} LinkableNodes  on package \"{}\"",
                 linkableNodes.size(), getPackageName());

        // this part could have several special function to get inter-router
        // links, but at the moment we worked much on switches.
        // In future we can try to extend this part.
        if (discoveryUsingRoutes)
            getLinksFromRouteTable(linkableNodes);

        if (discoveryUsingOspf)
            getLinksFromOspf(linkableNodes);

        if (discoveryUsingIsis)
            getLinksFromIsis(linkableNodes);

        if (discoveryUsingWifi)
            getLinksFromWifi(linkableNodes);

        // try get backbone links between switches using STP info
        // and store information in Bridge class
        // finding links using MAC address on ports
        if (discoveryUsingBridge) {
            getLinksFromBridge(linkableNodes);
        }

        // Try Link Layer Discovery Protocol to found link among all nodes
        if (discoveryUsingLldp)
            getLinksFromLldp(linkableNodes);

        // Try Cisco Discovery Protocol to found link among all nodes
        if (discoveryUsingCdp)
            getLinksFromCdp(linkableNodes);

        getLinkd().clearPackageSavedData(getPackageName());

        m_linkd.updateDiscoveryLinkCollection(this);

        m_links.clear();
        runned = true;
    }

    private void getLinksFromWifi(Collection<LinkableNode> linkableNodes) {
        List<String> macParsed = new ArrayList<String>();
        for (LinkableNode curNode : linkableNodes) {
            if (curNode.getWifiMacIfIndexMap().isEmpty())
                continue;
            final int curNodeId = curNode.getNodeId();
            LOG.info("getLinksFromWifi: parsing wifi node with ID {} and {} wifi interfaces ",
                     curNodeId, curNode.getWifiMacIfIndexMap().size());
            for (Entry<Integer, Set<String>> wifi : curNode.getWifiMacIfIndexMap().entrySet()) {
                LOG.debug("getLinksFromWifi: parsing wifi node with ID {} wifi interface {} macs {} ",
                          curNodeId, wifi.getKey(), wifi.getValue());
                macParsed = addLinks(macParsed, wifi.getValue(), curNodeId,
                                     wifi.getKey().intValue(),
                                     DiscoveryProtocol.wifi);
            }

        }
    }

    private void getLinksFromBridge(Collection<LinkableNode> linkableNodes) {
        LOG.info("getLinksFromBridge: finding links using Bridge Discovery");
        BridgeTopology topology = new BridgeTopology();
        List<LinkableNode> bridgeNodes = new ArrayList<LinkableNode>();
        for (final LinkableNode curNode : linkableNodes) {
            if (curNode.isBridgeNode()) {
                LOG.debug("getLinksFromBridge: found LinkableNode nodeid/sysoid/ipaddress {}/{}/{}",
                          curNode.getNodeId(), curNode.getSysoid(),
                          str(curNode.getSnmpPrimaryIpAddr()));
                bridgeNodes.add(curNode);
                for (Entry<Integer, String> entry: curNode.getMacIdentifiers().entrySet()) {
                	if (entry.getValue() == null || entry.getValue().equals(""))
                		continue;
                    topology.addBridgeAssociatedMac(curNode.getNodeId(), entry.getKey(),
                                                    entry.getValue());
                }
            }
        }

        for (final LinkableNode curNode : bridgeNodes) {
            final InetAddress curIpAddr = curNode.getSnmpPrimaryIpAddr();
            final Integer curNodeId = curNode.getNodeId();
            if (curNode.getStpInterfaces().size() == 0)
                LOG.info("getLinksFromBridge: no spanning tree info found on bridge with nodeid {} and ip address {}",
                         curNodeId, str(curIpAddr));
            for (final Map.Entry<Integer, List<OnmsStpInterface>> me : curNode.getStpInterfaces().entrySet()) {
                final Integer vlan = me.getKey();
                final String curBaseBridgeAddress = curNode.getBridgeIdentifier(vlan);
                LOG.info("getLinksFromBridge: parsing Spanning Tree Protocol Data:  nodeid {}, bridge identifier {}, VLAN {} with {} stp ports",
                         curNodeId, curBaseBridgeAddress, vlan,
                         curNode.getStpInterfaces().get(vlan).size());
                String designatedRoot = null;
                if (curNode.hasStpRoot(vlan)) {
                    designatedRoot = curNode.getStpRoot(vlan);
                } else {
                    LOG.info("getLinksFromBridge: bridge identifier {}, VLAN {}: stp designated root bridge identifier not found. Skipping.",
                             curBaseBridgeAddress, vlan);
                    continue;
                }
                if (designatedRoot == null
                        || designatedRoot.equals("0000000000000000")) {
                    LOG.warn("getLinksFromBridge: bridge identifier {}, VLAN {}: stp designated root {} is invalid. Skipping: {}",
                             curBaseBridgeAddress, vlan, designatedRoot);
                    continue;
                }
                if (curNode.isBridgeIdentifier(designatedRoot.substring(4))) {
                    LOG.info("getLinksFromBridge: bridge identifier {}, VLAN {}: stp designated root {} is the bridge itself. Skipping.",
                             curBaseBridgeAddress, vlan, designatedRoot);
                    continue;
                }
                LOG.info("getLinksFromBridge: bridge identifier {}, VLAN {}: stp designated root {} is another bridge. {} Parsing stp interfaces.",
                         curBaseBridgeAddress, vlan, designatedRoot);
                for (final OnmsStpInterface stpIface : me.getValue()) {
                    final int stpbridgeport = stpIface.getBridgePort();
                    final String stpPortDesignatedPort = stpIface.getStpPortDesignatedPort();
                    final String stpPortDesignatedBridge = stpIface.getStpPortDesignatedBridge();
                    LOG.info("getLinksFromBridge: bridge identifier {}, VLAN {}: parsing bridge port {} with stp designated bridge {} and stp designated port {}",
                             curBaseBridgeAddress, vlan, stpbridgeport,
                             stpPortDesignatedBridge, stpPortDesignatedPort);
                    if (stpPortDesignatedBridge == null
                            || stpPortDesignatedBridge.equals("0000000000000000")
                            || stpPortDesignatedBridge.equals("")) {
                        LOG.warn("getLinksFromBridge: bridge identifier {}, VLAN {}: designated bridge is invalid, skipping: {}",
                                 curBaseBridgeAddress, vlan,
                                 stpPortDesignatedBridge);
                        continue;
                    }
                    if (curNode.isBridgeIdentifier(stpPortDesignatedBridge.substring(4))) {
                        LOG.info("getLinksFromBridge: bridge identifier {}, VLAN {}: designated bridge for port {} is bridge itself, skipping",
                                 curBaseBridgeAddress, vlan, stpbridgeport);
                        continue;
                    }
                    if (stpPortDesignatedPort == null
                            || stpPortDesignatedPort.equals("0000")) {
                        LOG.warn("getLinksFromBridge: bridge identifier {}, VLAN {}: designated port is invalid: {}. skipping",
                                 curBaseBridgeAddress, vlan,
                                 stpPortDesignatedPort);
                        continue;
                    }
                    int designatedbridgeport = 8191 & Integer.parseInt(stpPortDesignatedPort,
                                                                       16);
                    final LinkableNode designatedNode = getNodeFromMacIdentifierOfBridgeNode(linkableNodes,
                                                                                             stpPortDesignatedBridge.substring(4));
                    if (designatedNode == null) {
                        LOG.debug("getLinksFromBridge: bridge identifier {}, VLAN {}: no nodeid found for stp designated bridge address {}. Nothing to save.",
                                  curBaseBridgeAddress, vlan,
                                  stpPortDesignatedBridge);
                        continue; // no saving info if no nodeid
                    }
                    topology.parseSTPEntry(curNodeId,
                                           stpbridgeport,
                                           curNode.getBridgeForwadingTableOnBridgePort(stpbridgeport),
                                           designatedNode.getNodeId(),
                                           designatedbridgeport,
                                           designatedNode.getBridgeForwadingTableOnBridgePort(designatedbridgeport));
                }
            }

        }

        for (final LinkableNode bridgeNode : bridgeNodes) {
        	topology.addNodeToTopology(bridgeNode);
        }

        List<String> macParsed = new ArrayList<String>();
        for (BridgeTopologyLink link : topology.getTopology()) {
            Integer curNodeId = link.getBridgeTopologyPort().getNodeid();
            Integer curIfIndex = getIfIndexFromNodeidBridgePort(linkableNodes,
                                                                curNodeId,
                                                                link.getBridgeTopologyPort().getBridgePort());
            if (link.getLinkedSwitchPort() != null ) {
            	final NodeToNodeLink lk = new NodeToNodeLink(
                        curNodeId,
                        curIfIndex,
                        DiscoveryProtocol.bridge);
            	lk.setNodeparentid(link.getLinkedSwitchPort().getNodeid());
            	lk.setParentifindex(link.getLinkedSwitchPort().getIfindex());
            	addNodetoNodeLink(lk);
            	LOG.info("getLinksFromBridge: saving bridge link: {}",
                        lk.toString());
            } else {
            	macParsed = addLinks(macParsed, link.getMacs(), curNodeId,
                                 curIfIndex, DiscoveryProtocol.bridge);
	            if (link.getDesignatebridgePort() != null) {
	                Integer endNodeId = link.getDesignatebridgePort().getNodeid();
	                Integer endIfIndex = getIfIndexFromNodeidBridgePort(linkableNodes,
	                                                                    endNodeId,
	                                                                    link.getDesignatebridgePort().getBridgePort());
	                final NodeToNodeLink lk = new NodeToNodeLink(
	                                                             curNodeId,
	                                                             curIfIndex,
	                                                             DiscoveryProtocol.bridge);
	                lk.setNodeparentid(endNodeId);
	                lk.setParentifindex(endIfIndex);
	                addNodetoNodeLink(lk);
	                LOG.info("getLinksFromBridge: saving bridge link: {}",
	                         lk.toString());
	            }
            }
        }
        LOG.info("getLinksFromBridge: done finding links using Bridge Discovery");

    }

    private Integer getIfIndexFromNodeidBridgePort(
            Collection<LinkableNode> linkableNodes, Integer nodeid,
            Integer bridgeport) {
        for (LinkableNode node : linkableNodes) {
            if (node.getNodeId() == nodeid.intValue()) {
                return node.getIfindexFromBridgePort(bridgeport);
            }
        }
        return -1;
    }

    private void getLinksFromRouteTable(Collection<LinkableNode> linkableNodes) {
        LOG.info("getLinksFromRouteTable: adding links using Ipv4 Routing Table");
        int i = 0;
        for (LinkableNode linkableNode : linkableNodes) {
            if (!linkableNode.hasRouteInterfaces())
                continue;
            final int curNodeId = linkableNode.getNodeId();
            InetAddress curIpAddr = linkableNode.getSnmpPrimaryIpAddr();
            LOG.info("getLinksFromRouteTable: parsing router node with ID {} IP address {} and {} router interfaces",
                     curNodeId, str(curIpAddr),
                     linkableNode.getRouteInterfaces().size());

            for (final RouterInterface routeIface : linkableNode.getRouteInterfaces()) {
                LOG.debug("getLinksFromRouteTable: parsing RouterInterface: {}",
                          routeIface.toString());

                final NodeToNodeLink lk = new NodeToNodeLink(
                                                             curNodeId,
                                                             routeIface.getIfindex(),
                                                             DiscoveryProtocol.iproute);
                lk.setNodeparentid(routeIface.getNextHopNodeid());
                lk.setParentifindex(routeIface.getNextHopIfindex());
                LOG.info("getLinksFromRouteTable: saving route link: {}",
                         lk.toString());
                addNodetoNodeLink(lk);
                i++;
            }
            LOG.info("getLinksFromRouteTable: done parsing router node with ID {} IP address {} and {} router interfaces",
                     curNodeId, str(curIpAddr),
                     linkableNode.getRouteInterfaces().size());
        }
        LOG.info("getLinksFromRouteTable: done finding links using Ipv4 Routing Table.Found links # {}.",
                 i);
    }

    private void getLinksFromCdp(Collection<LinkableNode> linkableNodes) {
        LOG.info("getLinksFromCdp: adding links using Cisco Discovery Protocol");
        int i = 0;
        Map<String, LinkableNode> cdpNodes = new HashMap<String, LinkableNode>();
        for (LinkableNode linkableNode : linkableNodes) {
            if (linkableNode.hasCdpInterfaces()) {
                LOG.debug("getLinksFromCdp: adding to CDP node list: node with nodeid/#cdpinterfaces {}/#{}",
                          linkableNode.getNodeId(),
                          linkableNode.getCdpInterfaces().size());
                cdpNodes.put(linkableNode.getCdpDeviceId(), linkableNode);
            }
        }
        LOG.info("getLinksFromCdp: found # {} nodes using Cisco Discovery Protocol",
                 cdpNodes.size());

        for (LinkableNode linknode1 : cdpNodes.values()) {
            LOG.info("getLinksFromCdp: parsing cdp device {} with cdpDeviceId {} using Cisco Discovery Protocol",
                     linknode1.getNodeId(), linknode1.getCdpDeviceId());
            for (CdpInterface cdpiface1 : linknode1.getCdpInterfaces()) {
                if (cdpiface1 == null) {
                    LOG.warn("getLinksFromCdp: cdp interface null found on target device node {} for cdpTargetDeviceId {} ",
                             linknode1.getNodeId());
                    continue;
                }
                LOG.info("getLinksFromCdp: parsing cdpInterface {} ",
                         cdpiface1);
                if (cdpiface1.getCdpTargetDeviceId() != null) {
                    LinkableNode linknode2 = cdpNodes.get(cdpiface1.getCdpTargetDeviceId());
                    if (linknode2 == null) {
                        LOG.info("getLinksFromCdp: no cdpdevice found for cdpDeviceId {} ",
                                 cdpiface1.getCdpTargetDeviceId());
                        continue;
                    }
                    if (linknode1.getNodeId() >= linknode2.getNodeId())
                        continue;
                    LOG.info("getLinksFromCdp: found node {} for cdpTargetDeviceId {} ",
                             linknode2.getNodeId(),
                             cdpiface1.getCdpTargetDeviceId());

                    for (CdpInterface cdpiface2 : linknode2.getCdpInterfaces()) {
                        if (cdpiface2 == null) {
                            LOG.warn("getLinksFromCdp: cdp interface null found on target device node {} for cdpTargetDeviceId {} ",
                                     linknode2.getNodeId(),
                                     cdpiface1.getCdpTargetDeviceId());
                            continue;
                        }
                        LOG.info("getLinksFromCdp: parsing target cdpInterface {} ",
                                 cdpiface2);
                        if (cdpiface2.getCdpTargetDeviceId() != null
                                && cdpiface2.getCdpTargetDeviceId().equals(linknode1.getCdpDeviceId())) {
                            if ((cdpiface1.getCdpIfName() != null && cdpiface1.getCdpIfName().equals(cdpiface2.getCdpTargetIfName()))
                                    || (cdpiface2.getCdpIfName() != null && cdpiface2.getCdpIfName().equals(cdpiface1.getCdpTargetIfName()))) {

                                NodeToNodeLink cdpLink = new NodeToNodeLink(
                                                                            linknode2.getNodeId(),
                                                                            cdpiface2.getCdpIfIndex(),
                                                                            DiscoveryProtocol.cdp);
                                cdpLink.setNodeparentid(linknode1.getNodeId());
                                cdpLink.setParentifindex(cdpiface1.getCdpIfIndex());
                                addNodetoNodeLink(cdpLink);
                                i++;
                            }
                        }
                    }
                } else if (cdpiface1.getCdpTargetNodeId() != null) {
                    LOG.info("getLinksFromCdp: cdpdevice found no snmp target node {} for cdpTargetDeviceId {} ",
                             cdpiface1.getCdpTargetNodeId(),
                             cdpiface1.getCdpTargetDeviceId());
                    NodeToNodeLink link = new NodeToNodeLink(
                                                             cdpiface1.getCdpTargetNodeId(),
                                                             -1,
                                                             DiscoveryProtocol.cdp);
                    link.setNodeparentid(linknode1.getNodeId());
                    link.setParentifindex(cdpiface1.getCdpIfIndex());
                    addNodetoNodeLink(link);
                    i++;
                }
            }
        }
        LOG.info("getLinksFromIsis: done CDP. Found links # {}.", i);

    }

    // We use a simple algoritm
    // to find links.
    // If node1 has a isis IS adj entry for node2
    // then node2 mast have an ospf nbr entry for node1
    // the parent node is that with nodeid1 < nodeid2
    private void getLinksFromIsis(Collection<LinkableNode> linkableNodes) {
        LOG.info("getLinksFromIsis: adding links using ISO IS-IS Routing Protocol");
        List<LinkableNode> m_isisNodes = new ArrayList<LinkableNode>();
        for (LinkableNode linkableNode : linkableNodes) {
            if (linkableNode.getIsisSysId() != null) {
                LOG.debug("getLinksFromIsis: adding to isis node list: node with nodeid/isisSysId/#isisinterface {}/{}/#{}",
                          linkableNode.getNodeId(),
                          linkableNode.getIsisSysId(),
                          linkableNode.getIsisInterfaces().size());
                m_isisNodes.add(linkableNode);
            }
        }
        int i = 0;
        for (LinkableNode linknode1 : m_isisNodes) {
            for (LinkableNode linknode2 : m_isisNodes) {
                if (linknode1.getNodeId() >= linknode2.getNodeId())
                    continue;
                for (NodeToNodeLink isisLink : getIsisLink(linknode1,
                                                           linknode2)) {
                    addNodetoNodeLink(isisLink);
                    i++;
                }
            }
        }
        LOG.info("getLinksFromIsis: done IS-IS. Found links # {}.", i);
    }

    private List<NodeToNodeLink> getIsisLink(LinkableNode linknode1,
            LinkableNode linknode2) {

        LOG.info("getIsisLink: finding IS-IS links between node with id {} and node with id {}.",
                 linknode1.getNodeId(), linknode2.getNodeId());
        List<NodeToNodeLink> links = new ArrayList<NodeToNodeLink>();
        for (IsisISAdjInterface isis1 : linknode1.getIsisInterfaces()) {
            for (IsisISAdjInterface isis2 : linknode2.getIsisInterfaces()) {
                LOG.debug("getIsisLink: first IS-IS element: isisSysId {} isisISAdj {}.",
                          linknode1.getIsisSysId(), isis1);
                LOG.debug("getIsisLink: second IS-IS element: isisSysId {} isisISAdj {}.",
                          linknode2.getIsisSysId(), isis2);
                if (isis1.getIsisISAdjNeighSysId().equals(linknode2.getIsisSysId())
                        && isis2.getIsisISAdjNeighSysId().equals(linknode1.getIsisSysId())
                        && isis1.getIsisISAdjIndex().intValue() == isis2.getIsisISAdjIndex().intValue()) {
                    NodeToNodeLink link = new NodeToNodeLink(
                                                             linknode1.getNodeId(),
                                                             isis1.getIsisLocalIfIndex(),
                                                             DiscoveryProtocol.isis);
                    link.setNodeparentid(linknode2.getNodeId());
                    link.setParentifindex(isis2.getIsisLocalIfIndex());
                    links.add(link);
                }
            }
        }
        return links;
    }

    // We use a simple algoritm
    // to find links.
    // If node1 has a ospf nbr entry for node2
    // then node2 mast have an ospf nbr entry for node1
    // the parent node is that with nodeid1 < nodeid2
    private void getLinksFromOspf(Collection<LinkableNode> linkableNodes) {
        LOG.info("getLinksFromOspf: adding links using Open Short Path First Protocol");
        List<LinkableNode> ospfNodes = new ArrayList<LinkableNode>();
        for (LinkableNode linkableNode : linkableNodes) {
            if (linkableNode.getOspfRouterId() != null
                    && linkableNode.getOspfinterfaces() != null) {
                LOG.debug("getLinksFromOspf: adding to ospf node list: node with nodeid/ospfrouterid/#ospfinterface {}/{}/#{}",
                          linkableNode.getNodeId(),
                          str(linkableNode.getOspfRouterId()),
                          linkableNode.getOspfinterfaces().size());
                ospfNodes.add(linkableNode);
            }
        }
        int i = 0;
        for (LinkableNode linknode1 : ospfNodes) {
            for (LinkableNode linknode2 : ospfNodes) {
                if (linknode1.getNodeId() >= linknode2.getNodeId())
                    continue;
                for (NodeToNodeLink ospfLink : getOspfLink(linknode1,
                                                           linknode2)) {
                    addNodetoNodeLink(ospfLink);
                    i++;
                }
            }
        }
        LOG.info("getLinksFromOspf: done OSPF. Found links # {}.", i);
    }

    private List<NodeToNodeLink> getOspfLink(LinkableNode linknode1,
            LinkableNode linknode2) {
        LOG.info("getLinksFromOspf: finding OSPF links between node with id {} and node with id {}.",
                 linknode1.getNodeId(), linknode2.getNodeId());
        List<NodeToNodeLink> links = new ArrayList<NodeToNodeLink>();
        for (OspfNbrInterface ospf : linknode1.getOspfinterfaces()) {
            for (OspfNbrInterface ospf2 : linknode2.getOspfinterfaces()) {
                if (ospf.getOspfNbrRouterId().equals(linknode2.getOspfRouterId())
                        && ospf.getOspfNbrNodeId() == linknode2.getNodeId()
                        && ospf2.getOspfNbrRouterId().equals(linknode1.getOspfRouterId())
                        && ospf2.getOspfNbrNodeId() == linknode1.getNodeId()) {
                    if (getSubnetAddress(ospf).equals(getSubnetAddress(ospf2))) {
                        NodeToNodeLink link = new NodeToNodeLink(
                                                                 ospf.getOspfNbrNodeId(),
                                                                 ospf.getOspfNbrIfIndex(),
                                                                 DiscoveryProtocol.ospf);
                        link.setNodeparentid(ospf2.getOspfNbrNodeId());
                        link.setParentifindex(ospf2.getOspfNbrIfIndex());
                        links.add(link);
                    }
                }
            }
        }
        return links;
    }

    protected InetAddress getSubnetAddress(OspfNbrInterface ospfinterface) {
        byte[] ip = ospfinterface.getOspfNbrIpAddr().getAddress();
        byte[] nm = ospfinterface.getOspfNbrNetMask().getAddress();
        try {
            return InetAddress.getByAddress(new byte[] {
                    (byte) (ip[0] & nm[0]), (byte) (ip[1] & nm[1]),
                    (byte) (ip[2] & nm[2]), (byte) (ip[3] & nm[3]) });
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    // We use a simple algoritm
    // to find links.
    // If node1 has a lldp rem entry for node2
    // then node2 mast have an lldp rem entry for node1
    // the parent node is that with nodeid1 < nodeid2
    private void getLinksFromLldp(Collection<LinkableNode> linkableNodes) {
        LOG.info("getLinksFromLldp: adding links using Layer Link Discovery Protocol");
        List<LinkableNode> lldpNodes = new ArrayList<LinkableNode>();
        for (LinkableNode linkableNode : linkableNodes) {
            if (linkableNode.getLldpChassisId() != null
                    && linkableNode.getLldpChassisIdSubtype() != null) {
                LOG.debug("getLinksFromLldp: adding to lldp node list: node with nodeid/sysname/chassisid {}/{}/{}",
                          linkableNode.getNodeId(),
                          linkableNode.getLldpSysname(),
                          linkableNode.getLldpChassisId());
                lldpNodes.add(linkableNode);
            }
        }
        int i = 0;
        for (LinkableNode linknode1 : lldpNodes) {
            for (LldpRemInterface lldpremiface : linknode1.getLldpRemInterfaces()) {
                LOG.debug("getLinksFromLldp: found LLDP interface {}",
                          lldpremiface.toString());
                NodeToNodeLink link = new NodeToNodeLink(
                                                         lldpremiface.getLldpRemNodeid(),
                                                         lldpremiface.getLldpRemIfIndex(),
                                                         DiscoveryProtocol.lldp);
                link.setNodeparentid(linknode1.getNodeId());
                link.setParentifindex(lldpremiface.getLldpLocIfIndex());
                addNodetoNodeLink(link);
                i++;
            }
        }

        LOG.info("getLinksFromLldp: done LLDP. Found links # {}.", i);

    }

    private LinkableNode getNodeFromMacIdentifierOfBridgeNode(
            Collection<LinkableNode> linkableNodes, final String macAddress) {
        for (final LinkableNode curNode : linkableNodes) {
            if (curNode.isBridgeNode()
                    && curNode.isBridgeIdentifier(macAddress))
                return curNode;
        }
        return null;
    }

    /**
     * Return the Scheduler
     * 
     * @return a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * Set the Scheduler
     * 
     * @param scheduler
     *            a {@link org.opennms.netmgt.linkd.scheduler.Scheduler}
     *            object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * This Method is called when DiscoveryLink is initialized
     */
    @Override
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "schedule: Cannot schedule a service whose scheduler is set to null");

        if (runned)
            m_scheduler.schedule(m_interval, this);
        else
            m_scheduler.schedule(discovery_delay + m_initial_sleep_time, this);
    }

    /**
     * <p>
     * getInitialSleepTime
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getInitialSleepTime() {
        return m_initial_sleep_time;
    }

    /**
     * <p>
     * setInitialSleepTime
     * </p>
     * 
     * @param initial_sleep_time
     *            The initial_sleep_timeto set.
     */
    public void setInitialSleepTime(long initial_sleep_time) {
        m_initial_sleep_time = initial_sleep_time;
    }

    /**
     * <p>
     * isReady
     * </p>
     * 
     * @return a boolean.
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * <p>
     * getDiscoveryInterval
     * </p>
     * 
     * @return Returns the discovery_link_interval.
     */
    public long getDiscoveryDelay() {
        return discovery_delay;
    }

    /**
     * <p>
     * setSnmpPollInterval
     * </p>
     * 
     * @param interval
     *            The discovery_link_interval to set.
     */
    public void setInterval(long interval) {
        m_interval = interval;
    }

    /**
     * <p>
     * getSnmpPollInterval
     * </p>
     * 
     * @return Returns the discovery_link_interval.
     */
    public long getInterval() {
        return m_interval;
    }

    /**
     * <p>
     * setDiscoveryInterval
     * </p>
     * 
     * @param interval
     *            The discovery_link_interval to set.
     */
    public void setDiscoveryInterval(long interval) {
        this.discovery_delay = interval;
    }

    /**
     * <p>
     * Getter for the field <code>links</code>.
     * </p>
     * 
     * @return an array of {@link org.opennms.netmgt.linkd.NodeToNodeLink}
     *         objects.
     */
    public NodeToNodeLink[] getLinks() {
        return m_links.toArray(new NodeToNodeLink[0]);
    }

    /**
     * <p>
     * isSuspended
     * </p>
     * 
     * @return Returns the suspendCollection.
     */
    @Override
    public boolean isSuspended() {
        return suspendCollection;
    }

    /**
     * <p>
     * suspend
     * </p>
     */
    @Override
    public void suspend() {
        this.suspendCollection = true;
    }

    /**
     * <p>
     * wakeUp
     * </p>
     */
    @Override
    public void wakeUp() {
        this.suspendCollection = false;
    }

    /**
     * <p>
     * unschedule
     * </p>
     */
    @Override
    public void unschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "unschedule: Cannot schedule a service whose scheduler is set to null");
        if (runned) {
            m_scheduler.unschedule(this, m_interval);
        } else {
            m_scheduler.unschedule(this, m_initial_sleep_time
                    + discovery_delay);
        }
    }

    private void addNodetoNodeLink(NodeToNodeLink nnlink) {
        if (nnlink == null) {
            LOG.warn("addNodetoNodeLink: node link is null.");
            return;
        }
        for (NodeToNodeLink curNnLink : m_links) {
            if (curNnLink.equals(nnlink)) {
                LOG.info("addNodetoNodeLink: link {} exists, not adding",
                         nnlink.toString());
                return;
            }
        }
        if (nnlink.getNodeId() == nnlink.getNodeparentid()) {
            LOG.warn("addNodetoNodeLink: link {} is on the same node, not adding",
                     nnlink.toString());
            return;
        }
        LOG.info("addNodetoNodeLink: adding link {}", nnlink.toString());
        m_links.add(nnlink);
    }

    private List<String> addLinks(List<String> macParsed, Set<String> macs,
            int nodeid, int ifindex, DiscoveryProtocol proto) {
        if (macs == null || macs.isEmpty()) {
            LOG.debug("addLinks: MAC address list on link is empty.");
        } else {
            for (String curMacAddress : macs) {
                if (macParsed.contains(curMacAddress)) {
                    LOG.warn("addLinks: MAC address {} just found on other bridge port! Skipping...",
                             curMacAddress);
                    continue;
                }

                if ((curMacAddress.indexOf("00000c07ac") == 0)
                        || (curMacAddress.indexOf("00000c9ff") == 0)) {
                    LOG.warn("addLinks: MAC address {} is excluded from discovery package! Skipping...",
                             curMacAddress);
                    continue;
                }
                final List<AtInterface> ats = m_linkd.getAtInterfaces(getPackageName(),
                                                                      curMacAddress);
                if (!ats.isEmpty()) {
                    for (final AtInterface at : ats) {
                        final NodeToNodeLink lNode = new NodeToNodeLink(
                                                                        at.getNodeid(),
                                                                        at.getIfIndex(),
                                                                        proto);
                        lNode.setNodeparentid(nodeid);
                        lNode.setParentifindex(ifindex);
                        addNodetoNodeLink(lNode);
                    }
                }
                macParsed.add(curMacAddress);
            }
        }
        return macParsed;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object r) {
        return (r instanceof DiscoveryLink && this.getPackageName().equals(((DiscoveryLink) r).getPackageName()));
    }

    /**
     * <p>
     * getInfo
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInfo() {
        return " Ready Runnable DiscoveryLink " + " package="
                + getPackageName() + " sleep=" + getInitialSleepTime()
                + " discovery=" + getDiscoveryDelay() + " interval="
                + getInterval() + " discoveryUsingBridge="
                + discoveryUsingBridge() + " discoveryUsingCdp="
                + discoveryUsingCdp() + " discoveryUsingRoutes="
                + discoveryUsingRoutes() + " discoveryUsingLldp="
                + discoveryUsingLldp() + " discoveryUsingOspf="
                + discoveryUsingOspf() + " discoveryUsingIsis="
                + discoveryUsingIsis() + " discoveryUsingWifi="
                + discoveryUsingWifi();
    }

    /**
     * <p>
     * discoveryUsingBridge
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingBridge() {
        return discoveryUsingBridge;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingBridge</code>.
     * </p>
     * 
     * @param discoveryUsingBridge
     *            a boolean.
     */
    public void setDiscoveryUsingBridge(boolean discoveryUsingBridge) {
        this.discoveryUsingBridge = discoveryUsingBridge;
    }

    /**
     * <p>
     * discoveryUsingLldp
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingOspf() {
        return discoveryUsingOspf;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingOspf</code>.
     * </p>
     * 
     * @param discoveryUsingOspf
     *            a boolean.
     */
    public void setDiscoveryUsingOspf(boolean discoveryUsingOspf) {
        this.discoveryUsingOspf = discoveryUsingOspf;
    }

    /**
     * <p>
     * discoveryUsingIsIs
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingIsis() {
        return discoveryUsingIsis;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingIsIs</code>.
     * </p>
     * 
     * @param discoveryUsingIsIs
     *            a boolean.
     */
    public void setDiscoveryUsingIsIs(boolean discoveryUsingIsIs) {
        this.discoveryUsingIsis = discoveryUsingIsIs;
    }

    /**
     * <p>
     * discoveryUsingLldp
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingLldp() {
        return discoveryUsingLldp;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingLldp</code>.
     * </p>
     * 
     * @param discoveryUsingLldp
     *            a boolean.
     */
    public void setDiscoveryUsingLldp(boolean discoveryUsingLldp) {
        this.discoveryUsingLldp = discoveryUsingLldp;
    }

    /**
     * <p>
     * discoveryUsingCdp
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingCdp() {
        return discoveryUsingCdp;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingCdp</code>.
     * </p>
     * 
     * @param discoveryUsingCdp
     *            a boolean.
     */
    public void setDiscoveryUsingCdp(boolean discoveryUsingCdp) {
        this.discoveryUsingCdp = discoveryUsingCdp;
    }

    /**
     * <p>
     * discoveryUsingWifi
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingWifi() {
        return discoveryUsingWifi;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingWifi</code>.
     * </p>
     * 
     * @param discoveryUsingCdp
     *            a boolean.
     */
    public void setDiscoveryUsingWifi(boolean discoveryUsingWifi) {
        this.discoveryUsingWifi = discoveryUsingWifi;
    }

    /**
     * <p>
     * discoveryUsingRoutes
     * </p>
     * 
     * @return a boolean.
     */
    public boolean discoveryUsingRoutes() {
        return discoveryUsingRoutes;
    }

    /**
     * <p>
     * Setter for the field <code>discoveryUsingRoutes</code>.
     * </p>
     * 
     * @param discoveryUsingRoutes
     *            a boolean.
     */
    public void setDiscoveryUsingRoutes(boolean discoveryUsingRoutes) {
        this.discoveryUsingRoutes = discoveryUsingRoutes;
    }

    /**
     * <p>
     * Getter for the field <code>packageName</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getPackageName() {
        return packageName;
    }

    /** {@inheritDoc} */
    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
