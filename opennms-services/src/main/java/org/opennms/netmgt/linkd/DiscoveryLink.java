//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.linkd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.snmp.FdbTableGet;
import org.opennms.netmgt.linkd.snmp.VlanCollectorEntry;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * This class is designed to discover link among nodes using the collected and
 * the necessary SNMP information. When the class is initially constructed no
 * information is used.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo </a>
 */
public final class DiscoveryLink implements ReadyRunnable {

	private static final int SNMP_IF_TYPE_ETHERNET = 6;

	private static final int SNMP_IF_TYPE_PROP_VIRTUAL = 53;

	private static final int SNMP_IF_TYPE_L2_VLAN = 135;

	private static final int SNMP_IF_TYPE_L3_VLAN = 136;

	private String packageName;
	
	private List<NodeToNodeLink> links = new ArrayList<NodeToNodeLink>();

	private List<MacToNodeLink> maclinks = new ArrayList<MacToNodeLink>();

	private HashMap<Integer,LinkableNode> bridgeNodes = new HashMap<Integer,LinkableNode>();

	private List<LinkableNode> routerNodes = new ArrayList<LinkableNode>();

	private List<LinkableNode> cdpNodes = new ArrayList<LinkableNode>();
	
	private List<LinkableNode> atNodes = new ArrayList<LinkableNode>();

	// this is the list of mac address just parsed by discovery process
	private List<String> macsParsed = new ArrayList<String>();
	
	// this is the list of mac address excluded by discovery process
	private List<String> macsExcluded = new ArrayList<String>();

	// this is tha list of atinterfaces for which to be discovery link
	// here there aren't the bridge identifier becouse they should be discovered
	// by main processes. This is used by addlinks method.
	private Map<String,List<AtInterface>> macToAtinterface = new HashMap<String,List<AtInterface>>();
	
	private boolean enableDownloadDiscovery = false;
	
	private boolean discoveryUsingRoutes = true;
	
	private boolean discoveryUsingCdp = true;
	
	private boolean discoveryUsingBridge = true;

	private boolean suspendCollection = false;

	private boolean isRunned = false;

	private boolean forceIpRouteDiscoveryOnEtherNet = false;
	/**
	 * The scheduler object
	 *  
	 */

	private Scheduler m_scheduler;

	/**
	 * The interval default value 30 min
	 */

	private long snmp_poll_interval = 1800000;

	/**
	 * The interval default value 5 min It is the time in ms after snmp
	 * collection is started
	 *  
	 */

	private long discovery_interval = 300000;

	/**
	 * The initial sleep time default value 10 min
	 */

	private long initial_sleep_time = 600000;

	/**
	 * Constructs a new DiscoveryLink object . The discovery does not occur
	 * until the <code>run</code> method is invoked.
	 */
	public DiscoveryLink() {
		super();
	}
	
	/**
	 * <p>
	 * Performs link discovery for the Nodes and save info in
	 * DatalinkInterface table on DataBase
	 * <p>
	 * No synchronization is performed, so if this is used in a separate thread
	 * context synchronization must be added.
	 * </p>
	 */
	public void run() {

		if (suspendCollection) {
		    LogUtils.warnf(this, "DiscoveryLink.run: Suspended!");
		} else {
			Collection<LinkableNode> all_snmplinknodes = Linkd.getInstance().getLinkableNodesOnPackage(getPackageName());

			LogUtils.debugf(this, "run: LinkableNodes/package found: %d/%s", all_snmplinknodes.size(), getPackageName());
			LogUtils.debugf(this, "run: discoveryUsingBridge/discoveryUsingCdp/discoveryUsingRoutes: %b/%b/%b", discoveryUsingBridge, discoveryUsingCdp, discoveryUsingRoutes);
			LogUtils.debugf(this, "run: enableDownloadDiscovery: %b", enableDownloadDiscovery);
			Iterator<LinkableNode> ite = all_snmplinknodes.iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				LogUtils.debugf(this, "run: Iterating on LinkableNode's found node: %d", curNode.getNodeId());

				if (curNode == null) {
				    LogUtils.errorf(this, "run: null linkable node found");
					continue;
				}

				if (curNode.isBridgeNode && discoveryUsingBridge) {
					bridgeNodes.put(new Integer(curNode.getNodeId()), curNode);
					
				}
				if (curNode.hasCdpInterfaces() && discoveryUsingCdp) {
					cdpNodes.add(curNode);
				}
				if (curNode.hasRouteInterfaces() && discoveryUsingRoutes) {
					routerNodes.add(curNode);
				}

				if (curNode.hasAtInterfaces()) {
					atNodes.add(curNode);
				}
			}

			LogUtils.debugf(this, "run: using atNodes to populate macToAtinterface");

			ite = atNodes.iterator();
			while (ite.hasNext()) {
				Iterator<AtInterface> at_ite = ite.next().getAtInterfaces().iterator();
				while (at_ite.hasNext()) {
					AtInterface at = at_ite.next();
					int nodeid = at.getNodeId();
					String ipaddr = at.getIpAddress();
					String macAddress = at.getMacAddress();
					LogUtils.debugf(this, "Parsing at Interface nodeid/ipaddr/macaddr: %d/%s/%s", nodeid, ipaddr, macAddress);
					if (!Linkd.getInstance().isInterfaceInPackage(at.getIpAddress(), getPackageName())) {
                        LogUtils.infof(this, "run: at interface: %s does not belong to package: %s! Not adding to discoverable atinterface.", ipaddr, getPackageName());
						macsExcluded.add(macAddress);
						continue;
					}
					if (isMacIdentifierOfBridgeNode(macAddress)) {
					    LogUtils.infof(this, "run: at interface %s belongs to bridge node! Not adding to discoverable atinterface.", macAddress);
						macsExcluded.add(macAddress);
						continue;
					}
                    if (macAddress.indexOf("00000c07ac") == 0) {
                        LogUtils.infof(this, "run: at interface %s is cisco hsrp address! Not adding to discoverable atinterface.", macAddress);
                       macsExcluded.add(macAddress); 
                       continue; 
                    }
					List<AtInterface> ats = macToAtinterface.get(macAddress);
					if (ats == null) ats = new ArrayList<AtInterface>();
					LogUtils.infof(this, "parseAtNodes: Adding to discoverable atinterface.");
					ats.add(at);
					macToAtinterface.put(macAddress, ats);
					LogUtils.debugf(this, "parseAtNodes: mac: %s now has atinterface reference: %d", macAddress, ats.size());
				}		
			}

			LogUtils.debugf(this, "run: end populate macToAtinterface");

			//now perform operation to complete
			if (enableDownloadDiscovery) {
			    LogUtils.infof(this, "run: get further unknown mac address snmp bridge table info");
				snmpParseBridgeNodes();
			} else {
			    LogUtils.infof(this, "run: skipping get further unknown mac address snmp bridge table info");
			}

			// First of all use quick methods to get backbone ports for speeding
			// up the link discovery

			LogUtils.debugf(this, "run: finding links among nodes using Cisco Discovery Protocol");

			// Try Cisco Discovery Protocol to found link among all nodes
			// Add CDP info for backbones ports

			ite = cdpNodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				int curCdpNodeId = curNode.getNodeId();
				String curCdpIpAddr = curNode.getSnmpPrimaryIpAddr();

				LogUtils.debugf(this, "run: parsing nodeid %d ip address %s with %d Cdp interfaces.", curCdpNodeId, curCdpIpAddr, curNode.getCdpInterfaces().size());

				Iterator<CdpInterface> sub_ite = curNode.getCdpInterfaces().iterator();
				while (sub_ite.hasNext()) {
					CdpInterface cdpIface = sub_ite.next();

					int cdpIfIndex = cdpIface.getCdpIfIndex();
					
					if (cdpIfIndex < 0) {
					    LogUtils.warnf(this, "run: found not valid CDP IfIndex %d.  Skipping.", cdpIfIndex);
						continue;
					}

					LogUtils.debugf(this, "run: found CDP ifindex %d", cdpIfIndex);

					InetAddress targetIpAddr = cdpIface.getCdpTargetIpAddr();
					
					if (!Linkd.getInstance().isInterfaceInPackage(targetIpAddr.getHostAddress(), getPackageName())) 
					{
					    LogUtils.warnf(this, "run: ip address %s Not in package: %s.  Skipping.", targetIpAddr.getHostAddress(), getPackageName());
					    continue;
					}

					int targetCdpNodeId = cdpIface.getCdpTargetNodeId();

					if (targetCdpNodeId == -1) {
					    LogUtils.warnf(this, "run: no node id found for ip address %s.  Skipping.", targetIpAddr.getHostAddress());
						continue;
					}

					LogUtils.debugf(this, "run: found nodeid/CDP target ipaddress: %d:%s", targetCdpNodeId, targetIpAddr);

					if (targetCdpNodeId == curCdpNodeId) {
					    LogUtils.debugf(this, "run: node id found for ip address %s is itself.  Skipping.", targetIpAddr.getHostAddress());
						continue;
					}

					int cdpDestIfindex = cdpIface.getCdpTargetIfIndex();
					
					if (cdpDestIfindex < 0) {
					    LogUtils.warnf(this, "run: found not valid CDP destination IfIndex %d.  Skipping.", cdpDestIfindex);
						continue;
					}

					LogUtils.debugf(this, "run: found CDP target ifindex %d", cdpDestIfindex);

					LogUtils.debugf(this, "run: parsing CDP link: nodeid=%d ifindex=%d nodeparentid=%d parentifindex=%d", curCdpNodeId, cdpIfIndex, targetCdpNodeId, cdpDestIfindex);

					boolean add = false;
					if (curNode.isBridgeNode() && isBridgeNode(targetCdpNodeId)) {
						LinkableNode targetNode = bridgeNodes.get(new Integer(targetCdpNodeId));
						add = parseCdpLinkOn(curNode, cdpIfIndex,targetNode, cdpDestIfindex);
						LogUtils.debugf(this, "run: both node are bridge nodes! Adding: %b", add);
					} else if (curNode.isBridgeNode) {
					    LogUtils.debugf(this, "run: source node is bridge node, target node is not bridge node! Adding: %b", add);
						add = parseCdpLinkOn(curNode,cdpIfIndex,targetCdpNodeId);
					} else if (isBridgeNode(targetCdpNodeId)) {
					    LogUtils.debugf(this, "run: source node is not bridge node, target node is bridge node! Adding: %b", add);
						LinkableNode targetNode = bridgeNodes.get(new Integer(targetCdpNodeId));
						add = parseCdpLinkOn(targetNode,cdpDestIfindex,curCdpNodeId);
					} else {
					    LogUtils.debugf(this, "run: no node is bridge node! Adding CDP link");
							add = true;
					}

					// now add the cdp link
					if (add) {
						NodeToNodeLink lk = new NodeToNodeLink(targetCdpNodeId,
								cdpDestIfindex);
						lk.setNodeparentid(curCdpNodeId);
						lk.setParentifindex(cdpIfIndex);
						addNodetoNodeLink(lk);
						LogUtils.debugf(this, "run: CDP link added: %s", lk.toString());
					}
				}
			}

			// try get backbone links between switches using STP info
			// and store information in Bridge class
			LogUtils.debugf(this, "run: try to found backbone ethernet links among bridge nodes using Spanning Tree Protocol");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();

				int curNodeId = curNode.getNodeId();
				String cupIpAddr = curNode.getSnmpPrimaryIpAddr();

				LogUtils.debugf(this, "run: parsing bridge nodeid %d ip address %s", curNodeId, cupIpAddr);

				Iterator<Map.Entry<String, List<BridgeStpInterface>>> sub_ite = curNode.getStpInterfaces().entrySet().iterator();

				LogUtils.debugf(this, "run: parsing %d Vlan.", curNode.getStpInterfaces().size());

				while (sub_ite.hasNext()) {
					Map.Entry<String, List<BridgeStpInterface>> me = sub_ite.next();
					String vlan = me.getKey();
					String curBaseBridgeAddress = curNode
							.getBridgeIdentifier(vlan);

					LogUtils.debugf(this, "run: found bridge identifier %s", curBaseBridgeAddress);

					String designatedRoot = null;
					
					if (curNode.hasStpRoot(vlan)) {
						designatedRoot = curNode.getStpRoot(vlan);
					} else {
					    LogUtils.debugf(this, "run: desigated root bridge identifier not found. Skipping %s", curBaseBridgeAddress);
						continue;
					}

					if (designatedRoot == null || designatedRoot.equals("0000000000000000")) {
					    LogUtils.warnf(this, "run: designated root is invalid. Skipping");
						continue;
					}
					// check if designated
					// bridge is it self
					// if bridge is STP root bridge itself exiting
					// searching on linkablesnmpnodes

					if (curNode.isBridgeIdentifier(designatedRoot.substring(4))) {
					    LogUtils.debugf(this, "run: STP designated root is the bridge itself. Skipping");
						continue;
					}

					// Now parse STP bridge port info to get designated bridge
					LogUtils.debugf(this, "run: STP designated root is another bridge. %s Parsing Stp Interface", designatedRoot);

					Iterator<BridgeStpInterface> stp_ite = me.getValue().iterator();
					while (stp_ite.hasNext()) {
						BridgeStpInterface stpIface = stp_ite
								.next();

						// the bridge port number
						int stpbridgeport = stpIface.getBridgeport();
						// if port is a backbone port continue
						if (curNode.isBackBoneBridgePort(stpbridgeport)) {
						    LogUtils.debugf(this, "run: bridge port %d already found.  Skipping.", stpbridgeport);
							continue;
						}

						String stpPortDesignatedPort = stpIface
								.getStpPortDesignatedPort();
						String stpPortDesignatedBridge = stpIface
								.getStpPortDesignatedBridge();

						LogUtils.debugf(this, "run: parsing bridge port %d with stp designated bridge %s and stp designated port %s", stpbridgeport, stpPortDesignatedBridge, stpPortDesignatedPort);

						if (stpPortDesignatedBridge == null || stpPortDesignatedBridge.equals("0000000000000000") || stpPortDesignatedBridge.equals("")) {
						    LogUtils.warnf(this, "run: designated bridge is invalid: %s", stpPortDesignatedBridge);
							continue;
						}

						if (curNode.isBridgeIdentifier(stpPortDesignatedBridge.substring(4))) {
						    LogUtils.debugf(this, "run: designated bridge for port %d is bridge itself", stpbridgeport);
							continue;
						}

						if (stpPortDesignatedPort == null || stpPortDesignatedPort.equals("0000")) {
						    LogUtils.warnf(this, "run: designated port is invalid: %s", stpPortDesignatedPort);
							continue;
						}

						//A Port Identifier shall be encoded as two octets,
						// taken to represent an unsigned binary number. If two
						// Port
						//Identifiers are numerically compared, the lesser
						// number denotes the Port of better priority. The more
						//significant octet of a Port Identifier is a settable
						// priority component that permits the relative priority
						// of Ports
						//on the same Bridge to be managed (17.13.7 and Clause
						// 14). The less significant twelve bits is the Port
						//Number expressed as an unsigned binary number. The
						// value 0 is not used as a Port Number.
						//NOTE -- The number of bits that are considered to be
						// part of the Port Number (12 bits) differs from the
						// 1998 and prior
						//versions of this standard (formerly, the priority
						// component was 8 bits and the Port Number component
						// also 8 bits). This
						//change acknowledged that modern switched LAN
						// infrastructures call for increasingly large numbers
						// of Ports to be
						//supported in a single Bridge. To maintain management
						// compatibility with older implementations, the
						// priority
						//component is still considered, for management
						// purposes, to be an 8-bit value, but the values that
						// it can be set to are
						//restricted to those where the least significant 4
						// bits are zero (i.e., only the most significant 4 bits
						// are settable).
						int designatedbridgeport = Integer.parseInt(
								stpPortDesignatedPort.substring(1), 16);

						// try to see if designated bridge is linkable
						// snmp node

						LinkableNode designatedNode = getNodeFromMacIdentifierOfBridgeNode(stpPortDesignatedBridge
								.substring(4));

						if (designatedNode == null) {
						    LogUtils.warnf(this, "run: no nodeid found for stp bridge address %s.  Nothing to save.", stpPortDesignatedBridge);
							continue; // no saving info if no nodeid
						}
						
						int designatednodeid = designatedNode.getNodeId();

						LogUtils.debugf(this, "run: found designated nodeid %d", designatednodeid);

						// test if there are other bridges between this link
						// USING MAC ADDRESS FORWARDING TABLE

						if (!isNearestBridgeLink(curNode, stpbridgeport, designatedNode, designatedbridgeport)) {
						    LogUtils.debugf(this, "run: other bridge found between nodes. No links to save!");
							continue; // no saving info if no nodeid
						}

						// this is a backbone port so try adding to Bridge class
						// get the ifindex on node

						int curIfIndex = curNode.getIfindex(stpbridgeport);

						if (curIfIndex == -1) {
						    LogUtils.warnf(this, "run: got invalid ifindex");
							continue;
						}

						int designatedifindex = designatedNode.getIfindex(designatedbridgeport);
						
						if (designatedifindex == -1) {
						    LogUtils.warnf(this, "run: got invalid ifindex on designated node");
							continue;
						}

						LogUtils.debugf(this, "run: backbone port found for node %d.  Adding to bridge %d.", curNodeId, stpbridgeport);

						curNode.addBackBoneBridgePorts(stpbridgeport);
						bridgeNodes.put(new Integer(curNodeId), curNode);

						LogUtils.debugf(this, "run: backbone port found for node %d.  Adding to helper class BB port bridge port %d.", designatednodeid, designatedbridgeport);

						designatedNode
								.addBackBoneBridgePorts(designatedbridgeport);
						bridgeNodes.put(new Integer(designatednodeid),
								designatedNode);

						LogUtils.debugf(this, "run: adding links on BB bridge port %d", designatedbridgeport);

						addLinks(getMacsOnBridgeLink(curNode,
								stpbridgeport, designatedNode,
								designatedbridgeport),curNodeId,curIfIndex);

						// writing to db using class
						// DbDAtaLinkInterfaceEntry
						NodeToNodeLink lk = new NodeToNodeLink(curNodeId,
								curIfIndex);
						lk.setNodeparentid(designatednodeid);
						lk.setParentifindex(designatedifindex);
						addNodetoNodeLink(lk);

					}
				}
			}

			// finding links using mac address on ports
			LogUtils.debugf(this, "run: try to found links using Mac Address Forwarding Table");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				int curNodeId = curNode.getNodeId();
				LogUtils.debugf(this, "run: parsing node bridge %d", curNodeId);

				Iterator<Integer> sub_ite = curNode.getPortMacs().keySet().iterator();

				while (sub_ite.hasNext()) {
					Integer intePort = sub_ite.next();
					int curBridgePort = intePort.intValue();

					LogUtils.debugf(this, "run: parsing bridge port %d with mac address %s", curBridgePort, curNode.getMacAddressesOnBridgePort(curBridgePort).toString());

					if (curNode.isBackBoneBridgePort(curBridgePort)) {
					    LogUtils.debugf(this, "run: parsing backbone bridge port %d.  Skipping.", curBridgePort);
						continue;
					}
					
					int curIfIndex = curNode.getIfindex(curBridgePort);
					if (curIfIndex == -1) {
					    LogUtils.warnf(this, "run: got invalid ifindex on bridge port %d", curBridgePort);
						continue;
					}
					// First get the mac addresses on bridge port

					Set<String> macs = curNode.getMacAddressesOnBridgePort(curBridgePort);

					// Then find the bridges whose mac addresses are learned on bridge port
					List<LinkableNode> bridgesOnPort = getBridgesFromMacs(macs);
					
					if (bridgesOnPort.isEmpty()) {
					    LogUtils.debugf(this, "run: no bridge info found on port %d.  Saving MACs.", curBridgePort);
						addLinks(macs, curNodeId, curIfIndex);
					} else {
						// a bridge mac address was found on port so you should analyze what happens
					    LogUtils.debugf(this, "run: bridge info found on port %d.  Finding nearest.", curBridgePort);
						Iterator<LinkableNode> bridge_ite = bridgesOnPort.iterator();
						// one among these bridges should be the node more close to the curnode, curport
						while (bridge_ite.hasNext()) {
							LinkableNode endNode = bridge_ite
									.next();
							
							int endNodeid = endNode.getNodeId();
							
							int endBridgePort = getBridgePortOnEndBridge(curNode, endNode);
							// The bridge port should be valid! This control is not properly done
							if (endBridgePort == -1) {
							    LogUtils.errorf(this, "run: no valid port found on bridge nodeid %d for node bridge identifiers nodeid %d.  Skipping.", endNodeid, curNodeId);
								continue;
							}
							
							// Try to found a new 
							boolean isTargetNode = isNearestBridgeLink(curNode, curBridgePort, endNode, endBridgePort);
							if (!isTargetNode)
									continue;

							int endIfindex = endNode.getIfindex(endBridgePort);
							if (endIfindex == -1) {
							    LogUtils.warnf(this, "run: got invalid ifindex o designated bridge port "
												+ endBridgePort);
								break;
							}

							LogUtils.debugf(this, "run: backbone port found for node "
										+ curNodeId + ". Adding backbone port "
										+ curBridgePort + " to bridge");

							curNode.addBackBoneBridgePorts(curBridgePort);
							bridgeNodes.put(curNodeId, curNode);

							LogUtils.debugf(this, "run: backbone port found for node "
										+ endNodeid
										+ " .Adding to helper class bb port "
										+ " bridge port " + endBridgePort);

							endNode.addBackBoneBridgePorts(endBridgePort);
							bridgeNodes.put(endNodeid, endNode);

							// finding links between two backbone ports
							addLinks(getMacsOnBridgeLink(curNode,
									curBridgePort, endNode, endBridgePort),curNodeId,curIfIndex);

							NodeToNodeLink lk = new NodeToNodeLink(curNodeId,
									curIfIndex);
							lk.setNodeparentid(endNodeid);
							lk.setParentifindex(endIfindex);
							addNodetoNodeLink(lk);
							break;
						}
					}
				}
			}

			// fourth find inter router links,
			// this part could have several special function to get inter router
			// links, but at the moment we worked much on switches.
			// In future we can try to extend this part.
			LogUtils.debugf(this, "run: try to found  not ethernet links on Router nodes");

			ite = routerNodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				int curNodeId = curNode.getNodeId();
				String curIpAddr = curNode.getSnmpPrimaryIpAddr();
				LogUtils.debugf(this, "run: parsing router nodeid " + curNodeId
							+ " ip address " + curIpAddr);

				Iterator<RouterInterface> sub_ite = curNode.getRouteInterfaces().iterator();
				LogUtils.debugf(this, "run: parsing "
							+ curNode.getRouteInterfaces().size()
							+ " Route Interface. ");

				while (sub_ite.hasNext()) {
					RouterInterface routeIface = sub_ite.next();

					LogUtils.debugf(this, "run: parsing RouterInterface: " + routeIface.toString());

					if (routeIface.getMetric() == -1) {
					    LogUtils.infof(this, "run: Router interface has invalid metric "
											+ routeIface.getMetric()
											+ ". Skipping");
						continue;
					}

					if (forceIpRouteDiscoveryOnEtherNet) {
					    LogUtils.infof(this, "run: force ip route discovery not getting SnmpIfType");
					} else {
						int snmpiftype = routeIface.getSnmpiftype();
						LogUtils.infof(this, "run: force ip route discovery getting SnmpIfType: " + snmpiftype);
						
						if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
						    LogUtils.infof(this, "run: Ethernet interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == SNMP_IF_TYPE_PROP_VIRTUAL) {
						    LogUtils.infof(this, "run: PropVirtual interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == SNMP_IF_TYPE_L2_VLAN) {
						    LogUtils.infof(this, "run: Layer2 Vlan interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == SNMP_IF_TYPE_L3_VLAN) {
						    LogUtils.infof(this, "run: Layer3 Vlan interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == -1) {
						    LogUtils.infof(this, "store: interface has unknown snmpiftype "
										+ snmpiftype + " . Skipping ");
							continue;
						} 
					}
					
					InetAddress nexthop = routeIface.getNextHop();

					if (nexthop.getHostAddress().equals("0.0.0.0")) {
					    LogUtils.infof(this, "run: nexthop address is broadcast address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						// FIXME this should be further analized 
						// working on routeDestNet you can find hosts that
						// are directly connected with the dest network
						// This happens when static routing is made like this:
						// route 10.3.2.0 255.255.255.0 Serial0
						// so the router broadcasts on Serial0
						continue;
					}

					if (nexthop.isLoopbackAddress()) {
					    LogUtils.infof(this, "run: nexthop address is localhost address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						continue;
					}

					if (!Linkd.getInstance().isInterfaceInPackage(nexthop.getHostAddress(), getPackageName())) {
					    LogUtils.infof(this, "run: nexthop address is not in package "
											+ nexthop.getHostAddress() + "/"+getPackageName() 
											+ " . Skipping ");
						continue;
					}

					
					int nextHopNodeid = routeIface.getNextHopNodeid();

					if (nextHopNodeid == -1) {
					    LogUtils.infof(this, "run: no node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " , skipping ");
						continue;
					}

					if (nextHopNodeid == curNodeId) {
					    LogUtils.debugf(this, "run: node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " is itself, skipping ");
						continue;
					}

					int ifindex = routeIface.getIfindex();
					
					if (ifindex == 0) {
					    LogUtils.infof(this, "run: route interface has ifindex "
											+ ifindex + " . trying to get ifindex from nextHopNet: " 
											+ routeIface.getNextHopNet());
						ifindex = getIfIndexFromRouter(curNode, routeIface.getNextHopNet());
						if (ifindex == -1 ) {
						    LogUtils.debugf(this, "run: found not correct ifindex "
												+ ifindex + " skipping.");
							continue;
						} else {
						    LogUtils.debugf(this, "run: found correct ifindex "
												+ ifindex + " .");
						}
						
					}
					LogUtils.debugf(this, "run: saving route link");
					
					// Saving link also when ifindex = -1 (not found)
					NodeToNodeLink lk = new NodeToNodeLink(nextHopNodeid,
							routeIface.getNextHopIfindex());
					lk.setNodeparentid(curNodeId);
					lk.setParentifindex(ifindex);
					addNodetoNodeLink(lk);
				}
			}

			bridgeNodes.clear();
			routerNodes.clear();
			cdpNodes.clear();
			macsParsed.clear();
			macsExcluded.clear();
			macToAtinterface.clear();
			atNodes.clear();

			Linkd.getInstance().updateDiscoveryLinkCollection(this);

			links.clear();
			maclinks.clear();
		}
		// rescheduling activities
		isRunned = true;
		reschedule();
	}

	private int getIfIndexFromRouter(LinkableNode parentnode, InetAddress nextHopNet) {

		if (!parentnode.hasRouteInterfaces())
			return -1;
		Iterator<RouterInterface> ite = parentnode.getRouteInterfaces().iterator();
		while (ite.hasNext()) {
			RouterInterface curIface = ite.next();

			if (curIface.getMetric() == -1) {
				continue;
			}

			int ifindex = curIface.getIfindex();

			if (ifindex == 0 || ifindex == -1)
				continue;

			if (curIface.getRouteNet().equals(nextHopNet)) return ifindex;
		}
		return -1;
	}

	/**
	 * 
	 * @param nodeid
	 * @return LinkableSnmpNode or null if not found
	 */

	boolean isBridgeNode(int nodeid) {
	    for (final LinkableNode curNode : bridgeNodes.values()) {
			if (nodeid == curNode.getNodeId())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param nodeid
	 * @return true if found
	 */

	boolean isRouterNode(int nodeid) {
	    for (final LinkableNode curNode : routerNodes) {
			if (nodeid == curNode.getNodeId())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param nodeid
	 * @return true if found
	 */

	boolean isCdpNode(int nodeid) {

		Iterator<LinkableNode> ite = cdpNodes.iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = ite.next();
			if (nodeid == curNode.getNodeId())
				return true;
		}
		return false;
	}

	private boolean isEndBridgePort(LinkableNode bridge, int bridgeport){

		Set<String> macsOnBridge = bridge.getMacAddressesOnBridgePort(bridgeport);

		if (macsOnBridge == null || macsOnBridge.isEmpty())
			return true;

		for (final String macaddr : macsOnBridge) {
			if (isMacIdentifierOfBridgeNode(macaddr)) return false;
		}

		return true;
	}
	
	private boolean isNearestBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		boolean hasbridge2forwardingRule = false;
		Set<String> macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		Set<String> macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

		if (macsOnBridge2 == null || macsOnBridge1 == null)
			return false;

		if (macsOnBridge2.isEmpty() || macsOnBridge1.isEmpty())
			return false;

		for (final String curMacOnBridge1 : macsOnBridge1) {
			// if mac address is bridge identifier of bridge 2 continue
			
			if (bridge2.isBridgeIdentifier(curMacOnBridge1)) {
				hasbridge2forwardingRule = true;
				continue;
			}
			// if mac address is itself identifier of bridge1 continue
			if (bridge1.isBridgeIdentifier(curMacOnBridge1))
				continue;
			// then no identifier of bridge one no identifier of bridge 2
			// bridge 2 contains  
			if (macsOnBridge2.contains(curMacOnBridge1)
					&& isMacIdentifierOfBridgeNode(curMacOnBridge1))
				return false;
		}

		return hasbridge2forwardingRule;
	}

	private Set<String> getMacsOnBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		Set<String> macsOnLink = new HashSet<String>();

    	Set<String> macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

		Set<String> macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		if (macsOnBridge2 == null || macsOnBridge1 == null)
			return null;

		if (macsOnBridge2.isEmpty() || macsOnBridge1.isEmpty())
			return null;

		for (final String curMacOnBridge1 : macsOnBridge1) {
			if (bridge2.isBridgeIdentifier(curMacOnBridge1))
				continue;
			if (macsOnBridge2.contains(curMacOnBridge1))
				macsOnLink.add(curMacOnBridge1);
		}
		return macsOnLink;
	}

	private boolean isMacIdentifierOfBridgeNode(String macAddress) {
	    for (final LinkableNode curNode : bridgeNodes.values()) {
			if (curNode.isBridgeIdentifier(macAddress))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param stpportdesignatedbridge
	 * @return Bridge Bridge Node if found else null
	 */

	private LinkableNode getNodeFromMacIdentifierOfBridgeNode(final String macAddress) {
	    for (final LinkableNode curNode : bridgeNodes.values()) {
			if (curNode.isBridgeIdentifier(macAddress))
				return curNode;
		}
		return null;
	}

	private List<LinkableNode> getBridgesFromMacs(final Set<String> macs) {
		List<LinkableNode> bridges = new ArrayList<LinkableNode>();
		for (final LinkableNode curNode : bridgeNodes.values()) {
		    for (final String curBridgeIdentifier : curNode.getBridgeIdentifiers()) {
				if (macs.contains((curBridgeIdentifier)))
					bridges.add(curNode);
			}
		}
		return bridges;
	}

	private int getBridgePortOnEndBridge(final LinkableNode startBridge, final LinkableNode endBridge) {

		int port = -1;
		for (final String curBridgeIdentifier : startBridge.getBridgeIdentifiers()) {
		    LogUtils.debugf(this, "getBridgePortOnEndBridge: parsing bridge identifier "
								+ curBridgeIdentifier);
			
			if (endBridge.hasMacAddress(curBridgeIdentifier)) {
			    for (final Integer p : endBridge.getBridgePortsFromMac(curBridgeIdentifier)) {
			        port = p;
					if (endBridge.isBackBoneBridgePort(port)) {
					    LogUtils.debugf(this, "getBridgePortOnEndBridge: found backbone bridge port "
											+ port
											+ " .... Skipping");
						continue;
					}
					if (port == -1) {
					    LogUtils.debugf(this, "run: no port found on bridge nodeid "
											+ endBridge.getNodeId()
											+ " for node bridge identifiers nodeid "
											+ startBridge.getNodeId()
											+ " . .....Skipping");
						continue;
					}
					LogUtils.debugf(this, "run: using mac address table found bridge port "
										+ port
										+ " on node "
										+ endBridge.getNodeId());
					return port;
				}
					
			} else {
			    LogUtils.debugf(this, "run: bridge identifier not found on node "
									+ endBridge.getNodeId());
			}
		}
		return -1;
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
	 * @param scheduler a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
	 */
	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}

	/**
	 * This Method is called when DiscoveryLink is initialized
	 */
	public void schedule() {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"schedule: Cannot schedule a service whose scheduler is set to null");

		m_scheduler.schedule(snmp_poll_interval + discovery_interval
				+ initial_sleep_time, this);
	}

	/**
	 * Schedule again the job
	 * 
	 * @return
	 */

	private void reschedule() {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"rescedule: Cannot schedule a service whose scheduler is set to null");
		m_scheduler.schedule(snmp_poll_interval, this);
	}

	/**
	 * <p>getInitialSleepTime</p>
	 *
	 * @return Returns the initial_sleep_time.
	 */
	public long getInitialSleepTime() {
		return initial_sleep_time;
	}

	/**
	 * <p>setInitialSleepTime</p>
	 *
	 * @param initial_sleep_time
	 *            The initial_sleep_timeto set.
	 */
	public void setInitialSleepTime(long initial_sleep_time) {
		this.initial_sleep_time = initial_sleep_time;
	}

	/**
	 * <p>isReady</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReady() {
		return true;
	}

	/**
	 * <p>getDiscoveryInterval</p>
	 *
	 * @return Returns the discovery_link_interval.
	 */
	public long getDiscoveryInterval() {
		return discovery_interval;
	}

	/**
	 * <p>setSnmpPollInterval</p>
	 *
	 * @param interval
	 *            The discovery_link_interval to set.
	 */
	public void setSnmpPollInterval(long interval) {
		this.snmp_poll_interval = interval;
	}

	/**
	 * <p>getSnmpPollInterval</p>
	 *
	 * @return Returns the discovery_link_interval.
	 */
	public long getSnmpPollInterval() {
		return snmp_poll_interval;
	}

	/**
	 * <p>setDiscoveryInterval</p>
	 *
	 * @param interval
	 *            The discovery_link_interval to set.
	 */
	public void setDiscoveryInterval(long interval) {
		this.discovery_interval = interval;
	}

	/**
	 * <p>Getter for the field <code>links</code>.</p>
	 *
	 * @return an array of {@link org.opennms.netmgt.linkd.NodeToNodeLink} objects.
	 */
	public NodeToNodeLink[] getLinks() {
		return links.toArray(new NodeToNodeLink[0]);
	}

	/**
	 * <p>getMacLinks</p>
	 *
	 * @return an array of {@link org.opennms.netmgt.linkd.MacToNodeLink} objects.
	 */
	public MacToNodeLink[] getMacLinks() {
		return maclinks.toArray(new MacToNodeLink[0]);
	}

	/**
	 * <p>isSuspended</p>
	 *
	 * @return Returns the suspendCollection.
	 */
	public boolean isSuspended() {
		return suspendCollection;
	}

	/**
	 * <p>suspend</p>
	 */
	public void suspend() {
		this.suspendCollection = true;
	}

	/**
	 * <p>wakeUp</p>
	 */
	public void wakeUp() {
		this.suspendCollection = false;
	}

	/**
	 * <p>unschedule</p>
	 */
	public void unschedule() {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"rescedule: Cannot schedule a service whose scheduler is set to null");
		if (isRunned) {
			m_scheduler.unschedule(this, snmp_poll_interval);
		} else {
			m_scheduler.unschedule(this, snmp_poll_interval
					+ initial_sleep_time + discovery_interval);
		}
	}
	
	private boolean parseCdpLinkOn(LinkableNode node1,int ifindex1,
								int nodeid2) {

		int bridgeport = node1.getBridgePort(ifindex1);

		if (node1.isBackBoneBridgePort(bridgeport)) {
		    LogUtils.debugf(this, "parseCdpLinkOn: node/backbone bridge port "
						+ node1.getNodeId() +"/" +bridgeport
						+ " already parsed. Skipping");
			return false;
		}

		if (isEndBridgePort(node1, bridgeport)) {

			node1.addBackBoneBridgePorts(bridgeport);
			bridgeNodes.put(node1.getNodeId(), node1);
			
			Set<String> macs = node1.getMacAddressesOnBridgePort(bridgeport);
			addLinks(macs,node1.getNodeId(),ifindex1);
		} else {
		    LogUtils.warnf(this, "parseCdpLinkOn: link cannot be saved. Skipping");
			return false;
		}


		return true;
	}

	private boolean parseCdpLinkOn(LinkableNode node1,int ifindex1,
								LinkableNode node2,int ifindex2) {
		
		int bridgeport1 = node1.getBridgePort(ifindex1);

		if (node1.isBackBoneBridgePort(bridgeport1)) {
		    LogUtils.debugf(this, "parseCdpLinkOn: backbone bridge port "
						+ bridgeport1
						+ " already parsed. Skipping");
			return false;
		}
		
		int bridgeport2 = node2
				.getBridgePort(ifindex2);
		if (node2.isBackBoneBridgePort(bridgeport2)) {
		    LogUtils.debugf(this, "parseCdpLinkOn: backbone bridge port "
						+ bridgeport2
						+ " already parsed. Skipping");
			return false;
		}

		if (isNearestBridgeLink(node1, bridgeport1,
				node2, bridgeport2)) {

			node1.addBackBoneBridgePorts(bridgeport1);
			bridgeNodes.put(node1.getNodeId(), node1);

			node2.addBackBoneBridgePorts(bridgeport2);
			bridgeNodes.put(node2.getNodeId(),node2);

			
			LogUtils.debugf(this, "parseCdpLinkOn: Adding node on links.");
			addLinks(getMacsOnBridgeLink(node1,
					bridgeport1, node2, bridgeport2),node1.getNodeId(),ifindex1);
		} else {
		    LogUtils.debugf(this, "parseCdpLinkOn: link found not on nearest. Skipping");
			return false;
		}
		return true;
	} 	

	private void addNodetoNodeLink(NodeToNodeLink nnlink) {
		if (nnlink == null)
		{
				LogUtils.warnf(this, "addNodetoNodeLink: node link is null.");
				return;
		}
		if (!links.isEmpty()) {
			Iterator<NodeToNodeLink> ite = links.iterator();
			while (ite.hasNext()) {
				NodeToNodeLink curNnLink = ite.next();
				if (curNnLink.equals(nnlink)) {
				    LogUtils.infof(this, "addNodetoNodeLink: link %s exists, not adding", nnlink.toString());
					return;
				}
			}
		}
		
		LogUtils.debugf(this, "addNodetoNodeLink: adding link %s", nnlink.toString());
		links.add(nnlink);
	}

	private void addLinks(Set<String> macs,int nodeid,int ifindex) { 
		if (macs == null || macs.isEmpty()) {
		    LogUtils.debugf(this, "addLinks: mac's list on link is empty.");
		} else {
			Iterator<String> mac_ite = macs.iterator();

			while (mac_ite.hasNext()) {
				String curMacAddress = mac_ite
						.next();
				if (macsParsed.contains(curMacAddress)) {
				    LogUtils.warnf(this, "addLinks: mac address "
									+ curMacAddress
									+ " just found on other bridge port! Skipping...");
					continue;
				}
				
				if (macsExcluded.contains(curMacAddress)) {
				    LogUtils.warnf(this, "addLinks: mac address "
									+ curMacAddress
									+ " is excluded from discovery package! Skipping...");
					continue;
				}
				
				if (macToAtinterface.containsKey(curMacAddress)) {
					List<AtInterface> ats = macToAtinterface.get(curMacAddress);
					Iterator<AtInterface> ite = ats.iterator();
					while (ite.hasNext()) {
						AtInterface at = ite.next();
						NodeToNodeLink lNode = new NodeToNodeLink(at.getNodeId(),at.getIfindex());
						lNode.setNodeparentid(nodeid);
						lNode.setParentifindex(ifindex);
						addNodetoNodeLink(lNode);
					}
				} else {
				    LogUtils.debugf(this, "addLinks: not find nodeid for ethernet mac address %s found on node/ifindex %d/%d", curMacAddress, nodeid, ifindex);
					MacToNodeLink lMac = new MacToNodeLink(curMacAddress);
					lMac.setNodeparentid(nodeid);
					lMac.setParentifindex(ifindex);
					maclinks.add(lMac);
				}
				macsParsed.add(curMacAddress);
			}
		}
	}
	
	/** {@inheritDoc} */
	public boolean equals(ReadyRunnable r) {
		return (r instanceof DiscoveryLink && this.getPackageName().equals(r.getPackageName()));
	}
	
	/**
	 * <p>getInfo</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getInfo() {
		return " Ready Runnable Discovery Link discoveryUsingBridge/discoveryUsingCdp/discoveryUsingRoutes/package: "
		+ discoveryUsingBridge() + "/"
		+ discoveryUsingCdp() + "/"
		+ discoveryUsingRoutes() + "/"+ getPackageName();
	}

	/**
	 * <p>discoveryUsingBridge</p>
	 *
	 * @return a boolean.
	 */
	public boolean discoveryUsingBridge() {
		return discoveryUsingBridge;
	}

	/**
	 * <p>Setter for the field <code>discoveryUsingBridge</code>.</p>
	 *
	 * @param discoveryUsingBridge a boolean.
	 */
	public void setDiscoveryUsingBridge(boolean discoveryUsingBridge) {
		this.discoveryUsingBridge = discoveryUsingBridge;
	}

	/**
	 * <p>discoveryUsingCdp</p>
	 *
	 * @return a boolean.
	 */
	public boolean discoveryUsingCdp() {
		return discoveryUsingCdp;
	}

	/**
	 * <p>Setter for the field <code>discoveryUsingCdp</code>.</p>
	 *
	 * @param discoveryUsingCdp a boolean.
	 */
	public void setDiscoveryUsingCdp(boolean discoveryUsingCdp) {
		this.discoveryUsingCdp = discoveryUsingCdp;
	}

	/**
	 * <p>discoveryUsingRoutes</p>
	 *
	 * @return a boolean.
	 */
	public boolean discoveryUsingRoutes() {
		return discoveryUsingRoutes;
	}

	/**
	 * <p>Setter for the field <code>discoveryUsingRoutes</code>.</p>
	 *
	 * @param discoveryUsingRoutes a boolean.
	 */
	public void setDiscoveryUsingRoutes(boolean discoveryUsingRoutes) {
		this.discoveryUsingRoutes = discoveryUsingRoutes;
	}

	/**
	 * <p>Getter for the field <code>packageName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	/* 
	 * This method is useful to get forwarding table
	 * for switch failed
	 */

	private void snmpParseBridgeNodes() {
	    LogUtils.debugf(this, "parseBridgeNodes: searching bridge port for bridge identifier not yet already found. Iterating on bridge nodes.");
		
		List<LinkableNode> bridgenodeschanged = new ArrayList<LinkableNode>();
		Iterator<LinkableNode> ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = ite.next();
			LogUtils.debugf(this, "parseBridgeNodes: parsing bridge: %d/%s", curNode.getNodeId(), curNode.getSnmpPrimaryIpAddr());

			// get macs
			
			List<String> macs = getNotAlreadyFoundMacsOnNode(curNode);

			if (macs.isEmpty()) continue;

			SnmpAgentConfig agentConfig = null;

			String className = null;
			
			boolean useVlan = Linkd.getInstance().getLinkdConfig().enableVlanDiscovery();
			if (Linkd.getInstance().getLinkdConfig().getPackage(getPackageName()).hasEnableVlanDiscovery()) 
				useVlan = Linkd.getInstance().getLinkdConfig().getPackage(getPackageName()).getEnableVlanDiscovery();
			
			if (useVlan && Linkd.getInstance().getLinkdConfig().hasClassName(curNode.getSysoid())) {
				className = Linkd.getInstance().getLinkdConfig().getVlanClassName(curNode.getSysoid());
			}
			

			try {
				agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(curNode.getSnmpPrimaryIpAddr()));
			} catch (UnknownHostException e) {
			    LogUtils.errorf(this, e, "parseBridgeNodes: Failed to load snmp parameter from snmp configuration file.");
				return;
			}
			
			String community = agentConfig.getReadCommunity();
			
			Iterator<String> mac_ite = macs.iterator();
			
			while (mac_ite.hasNext()) {
				String mac = mac_ite.next();
				LogUtils.debugf(this, "parseBridgeNodes: parsing mac: %s", mac);

				if (className != null && (className.equals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable") 
						|| className.equals("org.opennms.netmgt.linkd.snmp.IntelVlanTable"))){
					Iterator<Vlan> vlan_ite = curNode.getVlans().iterator();
					while (vlan_ite.hasNext()) {
						Vlan vlan = vlan_ite.next();
						if (vlan.getVlanStatus() != VlanCollectorEntry.VLAN_STATUS_OPERATIONAL || vlan.getVlanType() != VlanCollectorEntry.VLAN_TYPE_ETHERNET) {
						    LogUtils.debugf(this, "parseBridgeNodes: skipping vlan: %s", vlan.getVlanName());
							continue;
						}
						agentConfig.setReadCommunity(community+"@"+vlan.getVlanIndex());
						curNode = collectMacAddress(agentConfig, curNode, mac, vlan.getVlanIndex());
						agentConfig.setReadCommunity(community);
					}
				} else {
					int vlan = SnmpCollection.DEFAULT_VLAN_INDEX;
					if (useVlan) vlan = SnmpCollection.TRUNK_VLAN_INDEX;
					curNode = collectMacAddress(agentConfig, curNode, mac, vlan);
				}
			}
			bridgenodeschanged.add(curNode);
		}
		
		ite = bridgenodeschanged.iterator();
		while (ite.hasNext()) {
			LinkableNode node = ite.next();
			bridgeNodes.put(node.getNodeId(), node);
		}

	}
	
	private LinkableNode collectMacAddress(SnmpAgentConfig agentConfig, LinkableNode node,String mac,int vlan) {
		FdbTableGet coll = new FdbTableGet(agentConfig,mac);
		LogUtils.infof(this, "collectMacAddress: finding entry in bridge forwarding table for mac on node: %s/%d", mac, node.getNodeId());
		int bridgeport = coll.getBridgePort();
		if (bridgeport > 0 && coll.getBridgePortStatus() == QueryManager.SNMP_DOT1D_FDB_STATUS_LEARNED) {
			node.addMacAddress(bridgeport, mac, Integer.toString(vlan));
			LogUtils.infof(this, "collectMacAddress: found mac on bridge port: %d", bridgeport);
		} else {
			bridgeport = coll.getQBridgePort();
			if (bridgeport > 0 && coll.getQBridgePortStatus() == QueryManager.SNMP_DOT1D_FDB_STATUS_LEARNED) {
				node.addMacAddress(bridgeport, mac, Integer.toString(vlan));
				LogUtils.infof(this, "collectMacAddress: found mac on bridge port: %d", bridgeport);
			} else {
			    LogUtils.infof(this, "collectMacAddress: mac not found: %d", bridgeport);
			}
		}
		return node;
	}
	
	private List<String> getNotAlreadyFoundMacsOnNode(LinkableNode node){
	    LogUtils.debugf(this, "Searching Not Yet Found Bridge Identifier Occurrence on Node: %d", node.getNodeId());
		List<String> macs = new ArrayList<String>();
		Iterator<LinkableNode> ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = ite.next();
			if (node.getNodeId() == curNode.getNodeId()) continue;
			Iterator<String> mac_ite =curNode.getBridgeIdentifiers().iterator();
			while (mac_ite.hasNext()) {
				String curMac = mac_ite.next();
				if (node.hasMacAddress(curMac)) continue;
				if (macs.contains(curMac)) continue;
				LogUtils.debugf(this, "Found a node/Bridge Identifier %d/%s that was not found in bridge forwarding table for bridge node: %d", curNode.getNodeId(), curMac, node.getNodeId());
				macs.add(curMac);
			}
		}

		LogUtils.debugf(this, "Searching Not Yet Found Mac Address Occurrence on Node: %d", node.getNodeId());

		Iterator<String> mac_ite = macToAtinterface.keySet().iterator();
		while (mac_ite.hasNext()) {
			String curMac = mac_ite.next();
			if (node.hasMacAddress(curMac)) continue;
			if (macs.contains(curMac)) continue;
			LogUtils.debugf(this, "Found a Mac Address %s that was not found in bridge forwarding table for bridge node: %d", curMac, node.getNodeId());
			macs.add(curMac);
		}
		
		return macs;
	}

	/**
	 * <p>isEnableDownloadDiscovery</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEnableDownloadDiscovery() {
		return enableDownloadDiscovery;
	}

	/**
	 * <p>Setter for the field <code>enableDownloadDiscovery</code>.</p>
	 *
	 * @param enableDownloaddiscovery a boolean.
	 */
	public void setEnableDownloadDiscovery(boolean enableDownloaddiscovery) {
		this.enableDownloadDiscovery = enableDownloaddiscovery;
	}

	/**
	 * <p>isForceIpRouteDiscoveryOnEtherNet</p>
	 *
	 * @return a boolean.
	 */
	public boolean isForceIpRouteDiscoveryOnEtherNet() {
		return forceIpRouteDiscoveryOnEtherNet;
	}

	/**
	 * <p>Setter for the field <code>forceIpRouteDiscoveryOnEtherNet</code>.</p>
	 *
	 * @param forceIpRouteDiscoveryOnEtherNet a boolean.
	 */
	public void setForceIpRouteDiscoveryOnEtherNet(
			boolean forceIpRouteDiscoveryOnEtherNet) {
		this.forceIpRouteDiscoveryOnEtherNet = forceIpRouteDiscoveryOnEtherNet;
	}

}
