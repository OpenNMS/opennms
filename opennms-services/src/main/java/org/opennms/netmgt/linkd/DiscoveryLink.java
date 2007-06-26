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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;

/**
 * This class is designed to discover link among nodes using the collected and
 * the necessary SNMP information. When the class is initially constructed no
 * information is used.
 * 
 * @author <a href="mailto:rssntn67@yahoo.it">Antonio Russo </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 *  
 */

final class DiscoveryLink implements ReadyRunnable {

	private static final int SNMP_IF_TYPE_ETHERNET = 6;

	private static final int SNMP_IF_TYPE_PROP_VIRTUAL = 53;

	private static final int SNMP_IF_TYPE_L2_VLAN = 135;

	private static final int SNMP_IF_TYPE_L3_VLAN = 136;

	private List<LinkableNode> activenode = new ArrayList<LinkableNode>();

	private List<NodeToNodeLink> links = new ArrayList<NodeToNodeLink>();

	private List<MacToNodeLink> maclinks = new ArrayList<MacToNodeLink>();

	private HashMap<Integer,LinkableNode> bridgeNodes = new HashMap<Integer,LinkableNode>();

	private List<LinkableNode> routerNodes = new ArrayList<LinkableNode>();

	private List<LinkableNode> cdpNodes = new ArrayList<LinkableNode>();

	// this is the list of mac address just parsed by discovery process
	private List<String> macsParsed = new ArrayList<String>();
	
	// this is tha list of atinterfaces for which to be discovery link
	// here there aren't the bridge identifier becouese they should be discovered
	// by main processes. This is used by addlinks method.
	private Map<String,List<AtInterface>> macToAtinterface = new HashMap<String,List<AtInterface>>();
	
	private Map<Integer,Set<String>> nodeToMac = new HashMap<Integer,Set<String>>();
	


	private boolean suspendCollection = false;

	private boolean isRunned = false;

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
	DiscoveryLink() {
		super();
	}

	/**
	 * <p>
	 * Performs the discovery for the Linkable Nodes and save info in
	 * DatalinkInterface table on DataBase
	 * <p>
	 * No synchronization is performed, so if this is used in a separate thread
	 * context synchornization must be added.
	 * </p>
	 *  
	 */

	public void run() {
		Category log = ThreadCategory.getInstance(getClass());

		if (suspendCollection) {
			log.debug("DiscoveryLink.run: Suspended!");
		} else {
			LinkableNode[] all_snmplinknodes = Linkd.getInstance()
					.getSnmpLinkableNodes();

			Iterator ite = null;

			for (int i = 0; i < all_snmplinknodes.length; i++) {
				LinkableNode curNode = all_snmplinknodes[i];

				if (curNode == null) {
						log.error("run: null linkable node found for iterator " + i);
					continue;
				}

				int curNodeId = curNode.getNodeId();

				activenode.add(curNode);

				if (curNode.isBridgeNode)
					bridgeNodes.put(new Integer(curNodeId), curNode);
				if (curNode.hasCdpInterfaces())
					cdpNodes.add(curNode);
				if (curNode.hasRouteInterfaces())
					routerNodes.add(curNode);
				if (curNode.hasAtInterfaces()) {
					ite = curNode.getAtInterfaces().iterator();
					while (ite.hasNext()) {
						AtInterface at = (AtInterface) ite.next();
						String macAddress = at.getMacAddress();
						if (isMacIdentifierOfBridgeNode(macAddress)) {
							if (log.isInfoEnabled()) 
							log
							.info("run: at interface "
									+ at.toString()
									+ " belongs to bridge node! Not adding to discoverable atinterface.");
							continue;
						} 
						List<AtInterface> ats = macToAtinterface.get(macAddress);
						if (ats == null) ats = new ArrayList<AtInterface>();
						ats.add(at);
						macToAtinterface.put(macAddress, ats);
						Integer node = new Integer(at.getNodeId());
						java.util.Set<String> macs = new HashSet<String>();
						if (nodeToMac.containsKey(node)) {
							macs = nodeToMac.get(node);
						}

						macs.add(macAddress);
						nodeToMac.put(node, macs);
						
					}
				}

			}

			if (log.isDebugEnabled())
				log
						.debug("run: finding links among nodes using Cisco Discovery Protocol");

			// First of all use quick methods to get backbone ports for speeding
			// up
			// the link discovery!!!!!

			// Try Cisco Discovery Protocol to found link among all nodes
			// Add CDP info for backbones
			// complete discovery!!!!!

			ite = cdpNodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = (LinkableNode) ite.next();
				int curCdpNodeId = curNode.getNodeId();
				String curCdpIpAddr = curNode.getSnmpPrimaryIpAddr();

				if (log.isDebugEnabled())
					log.debug("run: parsing nodeid " + curCdpNodeId
							+ " ip address " + curCdpIpAddr + " with "
							+ curNode.getCdpInterfaces().size()
							+ " Cdp Interfaces. ");

				Iterator sub_ite = curNode.getCdpInterfaces().iterator();
				while (sub_ite.hasNext()) {
					CdpInterface cdpIface = (CdpInterface) sub_ite.next();

					int cdpIfIndex = cdpIface.getCdpIfIndex();
					
					if (log.isDebugEnabled()) log.debug("run: found CDP ifindex " + cdpIfIndex);
					if (cdpIfIndex < 0) {
						log.warn("run: found not valid CDP IfIndex "
								+ cdpIfIndex + " . Skipping");
						continue;
					}

					InetAddress targetIpAddr = cdpIface.getCdpTargetIpAddr();

					if (log.isDebugEnabled()) log.debug("run: found CDP target ipaddress " + targetIpAddr);

					int targetCdpNodeId = cdpIface.getCdpTargetNodeId();

					if (targetCdpNodeId == -1) {
						if (log.isDebugEnabled())
							log.debug("run: no node id found for ip address "
									+ targetIpAddr.getHostAddress()
									+ ". Skipping");
						continue;
					}

					if (log.isDebugEnabled()) log.debug("run: found CDP target nodeid " + targetCdpNodeId);

					if (targetCdpNodeId == curCdpNodeId) {
						if (log.isDebugEnabled())
							log.debug("run: node id found for ip address "
									+ targetIpAddr.getHostAddress()
									+ " is itself. Skipping");
						continue;
					}

					int cdpDestIfindex = cdpIface.getCdpTargetIfIndex();
					
					if (cdpDestIfindex < 0) {
						log
								.warn("run: found not valid CDP destination IfIndex "
										+ cdpDestIfindex + " . Skipping");
						continue;
					}

					if (log.isDebugEnabled()) log.debug("run: found CDP target ifindex " + cdpDestIfindex);

					if (log.isDebugEnabled())
						log.debug("run: parsing CDP link: nodeid=" + curCdpNodeId
								+ " ifindex=" + cdpIfIndex + " nodeparentid="
								+ targetCdpNodeId + " parentifindex="
								+ cdpDestIfindex);

					boolean add = true;
					if (curNode.isBridgeNode() && isBridgeNode(targetCdpNodeId)) {
						// adesso chiamo la routine che mi effettua il lavoro!
						LinkableNode targetNode = (LinkableNode) bridgeNodes
						.get(new Integer(targetCdpNodeId));
			
						add = parseCdpLinkOn(curNode, cdpIfIndex,targetNode, cdpDestIfindex,log);
					
					} else if (curNode.isBridgeNode) {
						add = parseCdpLinkOn(curNode,cdpIfIndex,targetCdpNodeId,log);
					} else if (isBridgeNode(targetCdpNodeId)) {
						LinkableNode targetNode = (LinkableNode) bridgeNodes
						.get(new Integer(targetCdpNodeId));
						add = parseCdpLinkOn(targetNode,cdpDestIfindex,curCdpNodeId,log);
					}
					// now add the cdp link
					if (add) {
						if (log.isDebugEnabled()) log.debug("run: adding found CDP link ");
						NodeToNodeLink lk = new NodeToNodeLink(targetCdpNodeId,
								cdpDestIfindex);
						lk.setNodeparentid(curCdpNodeId);
						lk.setParentifindex(cdpIfIndex);
						addNodetoNodeLink(lk, log);
					} else {
						if (log.isDebugEnabled())
							log.debug("run: found CDP link not added");
					}
				}
			}

			// try get backbone links between switches using STP info
			// and store information in Bridge class
			if (log.isDebugEnabled())
				log
						.debug("run: try to found backbone ethernet links among bridge nodes using Spanning Tree Protocol");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = (LinkableNode) ite.next();

				int curNodeId = curNode.getNodeId();
				String cupIpAddr = curNode.getSnmpPrimaryIpAddr();

				if (log.isDebugEnabled())
					log.debug("run: parsing bridge nodeid " + curNodeId
							+ " ip address " + cupIpAddr);

				Iterator sub_ite = curNode.getStpInterfaces().entrySet()
						.iterator();

				if (log.isDebugEnabled())
					log.debug("run: parsing "
							+ curNode.getStpInterfaces().size() + " Vlan. ");

				while (sub_ite.hasNext()) {
					Map.Entry me = (Map.Entry) sub_ite.next();
					String vlan = (String) me.getKey();
					String curBaseBridgeAddress = curNode
							.getBridgeIdentifier(vlan);

					if (log.isDebugEnabled())
						log.debug("run: found bridge identifier "
								+ curBaseBridgeAddress);

					String designatedRoot = null;
					
					if (curNode.hasStpRoot(vlan)) {
						designatedRoot = curNode.getStpRoot(vlan);
					} else {
						if (log.isDebugEnabled())
							log
									.debug("run: desigated root bridge identifier not found. Skipping"
											+ curBaseBridgeAddress);
						continue;
					}

					if (designatedRoot.equals("0000000000000000")) {
						log.warn("run: designated root is invalid. Skipping");
						continue;
					}
					// check if designated
					// bridge is it self
					// if bridge is STP root bridge itself exiting
					// searching on linkablesnmpnodes

					if (curNode.isBridgeIdentifier(designatedRoot.substring(4))) {
						if (log.isDebugEnabled())
							log
									.debug("run: STP designated root is the bridge itself. Skipping");
						continue;
					}

					// Now parse STP bridge port info to get designated bridge
					if (log.isDebugEnabled())
						log
								.debug("run: STP designated root is another bridge. " + designatedRoot + " Parsing Stp Interface");

					Iterator stp_ite = ((List) me.getValue()).iterator();
					while (stp_ite.hasNext()) {
						BridgeStpInterface stpIface = (BridgeStpInterface) stp_ite
								.next();

						// the bridge port number
						int stpbridgeport = stpIface.getBridgeport();
						// if port is a backbone port continue
						if (curNode.isBackBoneBridgePort(stpbridgeport)) {
							if (log.isDebugEnabled())
								log.debug("run: bridge port " + stpbridgeport
										+ " already found .... Skipping");
							continue;
						}

						String stpPortDesignatedPort = stpIface
								.getStpPortDesignatedPort();
						String stpPortDesignatedBridge = stpIface
								.getStpPortDesignatedBridge();

						if (log.isDebugEnabled())
							log.debug("run: parsing bridge port "
									+ stpbridgeport
									+ " with stp designated bridge "
									+ stpPortDesignatedBridge
									+ " and with stp designated port "
									+ stpPortDesignatedPort);

						if (stpPortDesignatedBridge.equals("0000000000000000")) {
							log.warn("run: designated bridge is invalid "
									+ stpPortDesignatedBridge);
							continue;
						}

						if (curNode.isBridgeIdentifier(stpPortDesignatedBridge
								.substring(4))) {
							if (log.isDebugEnabled())
								log.debug("run: designated bridge for port "
										+ stpbridgeport + " is bridge itself ");
							continue;
						}

						if (stpPortDesignatedPort.equals("0000")) {
							log.warn("run: designated port is invalid "
									+ stpPortDesignatedPort);
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
						//NOTE�The number of bits that are considered to be
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
							log
									.warn("run: no nodeid found for stp bridge address "
											+ stpPortDesignatedBridge
											+ " . Nothing to save to db");
							continue; // no saving info if no nodeid
						}
						
						int designatednodeid = designatedNode.getNodeId();

						if (log.isDebugEnabled())
							log.debug("run: found designated nodeid "
									+ designatednodeid);

						// test if there are other bridges between this link
						// USING MAC ADDRESS FORWARDING TABLE

						if (!isNearestBridgeLink(curNode, stpbridgeport,
								designatedNode, designatedbridgeport)) {
							if (log.isDebugEnabled())
								log
										.debug("run: other bridge found between nodes. Nothing to save to db");
							continue; // no saving info if no nodeid
						}

						// this is a backbone port so try adding to Bridge class
						// get the ifindex on node

						int curIfIndex = curNode.getIfindex(stpbridgeport);

						if (curIfIndex == -1) {
							log.warn("run: got invalid ifindex");
							continue;
						}

						int designatedifindex = designatedNode
						.getIfindex(designatedbridgeport);
						
						if (designatedifindex == -1) {
							log
									.warn("run: got invalid ifindex on designated node");
							continue;
						}

						if (log.isDebugEnabled())
							log.debug("run: backbone port found for node "
									+ curNodeId + ". Adding to bridge"
									+ stpbridgeport);

						curNode.addBackBoneBridgePorts(stpbridgeport);
						bridgeNodes.put(new Integer(curNodeId), curNode);

						if (log.isDebugEnabled())
							log.debug("run: backbone port found for node "
									+ designatednodeid
									+ " .Adding to helper class bb port "
									+ " bridge port " + designatedbridgeport);

						designatedNode
								.addBackBoneBridgePorts(designatedbridgeport);
						bridgeNodes.put(new Integer(designatednodeid),
								designatedNode);
						
						if (log.isDebugEnabled())
							log.debug("run: adding links on bb bridge port " + designatedbridgeport);

						addLinks(getMacsOnBridgeLink(curNode,
								stpbridgeport, designatedNode,
								designatedbridgeport),curNodeId,curIfIndex,log);

						// writing to db using class
						// DbDAtaLinkInterfaceEntry
						NodeToNodeLink lk = new NodeToNodeLink(curNodeId,
								curIfIndex);
						lk.setNodeparentid(designatednodeid);
						lk.setParentifindex(designatedifindex);
						addNodetoNodeLink(lk, log);

					}
				}
			}

			// finding links using mac address on ports

			if (log.isDebugEnabled())
				log
						.debug("run: try to found links using Mac Address Forwarding Table");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = (LinkableNode) ite.next();
				int curNodeId = curNode.getNodeId();
				if (log.isDebugEnabled())
					log.debug("run: parsing node bridge " + curNodeId);

				Iterator sub_ite = curNode.getPortMacs().keySet().iterator();

				while (sub_ite.hasNext()) {
					Integer intePort = (Integer) sub_ite.next();
					int curBridgePort = intePort.intValue();

					if (log.isDebugEnabled())
						log.debug("run: parsing bridge port "
								+ curBridgePort
								+ " with mac addresses "
								+ curNode.getMacAddressesOnBridgePort(
										curBridgePort).toString());

					if (curNode.isBackBoneBridgePort(curBridgePort)) {
						if (log.isDebugEnabled())
							log.debug("run: parsing backbone bridge port "
									+ curBridgePort + " .... Skipping");
						continue;
					}
					
					int curIfIndex = curNode.getIfindex(curBridgePort);
					if (curIfIndex == -1) {
						log.warn("run: got invalid ifindex on bridge port "
											+ curBridgePort);
						continue;
					}
					// operazione A: ottengo la lista dei bridge sulla porta

					Set macs = curNode.getMacAddressesOnBridgePort(curBridgePort);

					// operazione B trovo i bridgenode corrispondenti
					HashMap bridgesOnPort = getBridgesFromMacs(macs);
					
					if (bridgesOnPort.isEmpty()) {
						if (log.isDebugEnabled())
							log.debug("run: no bridge info found on port "
									+ curBridgePort + " .... Saving Macs");
						addLinks(macs, curNodeId, curIfIndex, log);
					} else {
						// Cerco il bridge più vicino
						if (log.isDebugEnabled())
							log.debug("run: bridge info found on port "
									+ curBridgePort + " .... Finding nearest.");
						Iterator bridge_ite = bridgesOnPort.values().iterator();
						BRIDGE: while (bridge_ite.hasNext()) {
							LinkableNode endNode = (LinkableNode) bridge_ite
									.next();
							int endNodeid = endNode.getNodeId();
							
							int endBridgePort = getBridgePortOnEndBridge(
									curNode, endNode,log);
							
							if (endBridgePort == -1) {
								if (log.isDebugEnabled())
									log
											.debug("run: no port found on bridge nodeid "
													+ endNodeid
													+ " for node bridge identifiers nodeid "
													+ curNodeId
													+ " . .....Skipping");
								// FIXME why continue, it's a link found!!!!!!!!
								continue;
							}
							
							// esco da questo loop solo se trovo il bridge più vicino
							while (true) {
								LinkableNode targetNode = findNearestBridgeLink(
										curNode, curBridgePort, endNode,
										endBridgePort);
								if (targetNode.getNodeId() == endNode
										.getNodeId()) {
									// port found save
									break;
								} else {
									endNode = targetNode;
									endNodeid = endNode.getNodeId();
									endBridgePort = getBridgePortOnEndBridge(
											curNode, endNode,log);
									if (endBridgePort == -1) {
										if (log.isDebugEnabled())
											log
													.debug("run: no port found on bridge nodeid "
															+ endNodeid
															+ " for node bridge identifiers nodeid "
															+ curNodeId
															+ " . .....Skipping");
										continue BRIDGE;
									}

									if (log.isDebugEnabled())
										log
												.debug("run: other bridge found between nodes. Iteration on bridge node");
								}
							}

							// this is a backbone port so adding to Bridge class
							// get the ifindex
							int endIfindex = endNode.getIfindex(endBridgePort);
							if (endIfindex == -1) {
								log
										.warn("run: got invalid ifindex o designated bridge port "
												+ endBridgePort);
								continue;
							}

							if (log.isDebugEnabled())
								log.debug("run: backbone port found for node "
										+ curNodeId + ". Adding backbone port "
										+ curBridgePort + " to bridge");

							curNode.addBackBoneBridgePorts(curBridgePort);
							bridgeNodes.put(new Integer(curNodeId), curNode);

							if (log.isDebugEnabled())
								log.debug("run: backbone port found for node "
										+ endNodeid
										+ " .Adding to helper class bb port "
										+ " bridge port " + endBridgePort);

							endNode.addBackBoneBridgePorts(endBridgePort);
							bridgeNodes.put(new Integer(endNodeid), endNode);

							// finding links between two backbone ports
							addLinks(getMacsOnBridgeLink(curNode,
									curBridgePort, endNode, endBridgePort),curNodeId,curIfIndex,log);

							// writing to db using class
							// DbDAtaLinkInterfaceEntry
							NodeToNodeLink lk = new NodeToNodeLink(curNodeId,
									curIfIndex);
							lk.setNodeparentid(endNodeid);
							lk.setParentifindex(endIfindex);
							addNodetoNodeLink(lk, log);
							break BRIDGE;
						}
					}
				}
			}

			// now found remaing links (not yet parsed) present on one side of backbone link
			if (log.isDebugEnabled())
				log.debug("run: try to found remaining links (Orfani!) on BackBoneBridgePort");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = (LinkableNode) ite.next();
				Iterator sub_ite = curNode.getBackBoneBridgePorts().iterator();
				while (sub_ite.hasNext()) {
					Integer intePort = (Integer) sub_ite.next();
					int bridgePort = intePort.intValue();
					if (log.isDebugEnabled())
						log.debug("run: parsing backbone bridge port "
								+ bridgePort + " on node "
								+ curNode.getSnmpPrimaryIpAddr());
					if (!curNode.hasMacAddressesOnBridgePort(bridgePort)) {
						log
								.warn("run: bridge port has no mac address on.   Skipping. ");
						continue;
					}
					
					int curIfIndex = curNode.getIfindex(bridgePort);
					if (curIfIndex == -1) {
						log
								.warn("run: got invalid ifindex on backbone bridge port "
										+ bridgePort);
						continue;
					}
					addLinks(curNode.getMacAddressesOnBridgePort(
							bridgePort),curNode.getNodeId(),curIfIndex,log);

				}
			}
			// fourth find inter router links,
			// this part could have several special function to get inter router
			// links, but at the moment we worked much on switches.
			// In future we can try to extend this part.
			if (log.isDebugEnabled())
				log
						.debug("run: try to found  not ethernet links on Router nodes");

			ite = routerNodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = (LinkableNode) ite.next();
				int curNodeId = curNode.getNodeId();
				String curIpAddr = curNode.getSnmpPrimaryIpAddr();
				if (log.isDebugEnabled())
					log.debug("run: parsing router nodeid " + curNodeId
							+ " ip address " + curIpAddr);

				Iterator sub_ite = curNode.getRouteInterfaces().iterator();
				if (log.isDebugEnabled())
					log.debug("run: parsing "
							+ curNode.getRouteInterfaces().size()
							+ " Route Interface. ");

				while (sub_ite.hasNext()) {
					RouterInterface routeIface = (RouterInterface) sub_ite
							.next();

					if (log.isDebugEnabled()) {
						log.debug("run: parsing RouterInterface: " + routeIface.toString());
					}

					if (routeIface.getMetric() == -1) {
						if (log.isInfoEnabled())
							log
									.info("run: Router interface has invalid metric "
											+ routeIface.getMetric()
											+ ". Skipping");
						continue;
					}

					int snmpiftype = routeIface.getSnmpiftype();
					
					if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
						if (log.isInfoEnabled())
							log
									.info("run: Ethernet interface for nodeid. Skipping ");
						continue;
					} else if (snmpiftype == SNMP_IF_TYPE_PROP_VIRTUAL) {
						if (log.isInfoEnabled())
							log
									.info("run: PropVirtual interface for nodeid. Skipping ");
						continue;
					} else if (snmpiftype == SNMP_IF_TYPE_L2_VLAN) {
						if (log.isInfoEnabled())
							log
									.info("run: Layer2 Vlan interface for nodeid. Skipping ");
						continue;
					} else if (snmpiftype == SNMP_IF_TYPE_L3_VLAN) {
						if (log.isInfoEnabled())
							log
									.info("run: Layer3 Vlan interface for nodeid. Skipping ");
						continue;
					} else if (snmpiftype == -1) {
						if (log.isInfoEnabled())
							log.info("store: interface has unknown snmpiftype "
									+ snmpiftype + " . Skipping ");
					} 

					InetAddress nexthop = routeIface.getNextHop();

					if (nexthop.getHostAddress().equals("0.0.0.0")) {
						if (log.isInfoEnabled())
							log
									.info("run: nexthop address is broadcast address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						// FIXME this should be further analized 
						// working on routeDestNet you can find hosts that
						// are directly connected with the dest network
						// This happens when routing is made in such a way:
						// route 10.3.2.0 255.255.255.0 Serial0
						// so the router broadcasts on Serial0
						continue;
					}

					if (nexthop.isLoopbackAddress()) {
						if (log.isInfoEnabled())
							log
									.info("run: nexthop address is localhost address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						continue;
					}

					int nextHopNodeid = routeIface.getNextHopNodeid();

					if (nextHopNodeid == -1) {
						if (log.isInfoEnabled())
							log
									.info("run: no node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " , skipping ");
						continue;
					}

					if (nextHopNodeid == curNodeId) {
						if (log.isDebugEnabled())
							log
									.debug("run: node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " is itself, skipping ");
						continue;
					}

					int ifindex = routeIface.getIfindex();
					
					if (ifindex == 0) {
						if (log.isInfoEnabled())
							log
									.info("run: route interface has ifindex "
											+ ifindex + " . trying to get ifindex from nextHopNet: " 
											+ routeIface.getNextHopNet());
						ifindex = getIfIndexFromRouter(curNode, routeIface.getNextHopNet());
						if (ifindex == -1 ) {
							if (log.isDebugEnabled())
								log
										.debug("run: found not correct ifindex "
												+ ifindex + " skipping.");
							continue;
						} else {
							if (log.isDebugEnabled())
								log
										.debug("run: found correct ifindex "
												+ ifindex + " .");
						}
						
					}
					if (log.isDebugEnabled())
						log
								.debug("run: saving route link");
					
					// Saving link also when ifindex = -1 (not found)
					NodeToNodeLink lk = new NodeToNodeLink(nextHopNodeid,
							routeIface.getNextHopIfindex());
					lk.setNodeparentid(curNodeId);
					lk.setParentifindex(ifindex);
					addNodetoNodeLink(lk, log);
				}
			}

			//		making clean
			activenode.clear();
			bridgeNodes.clear();
			routerNodes.clear();
			cdpNodes.clear();
			macsParsed.clear();
			macToAtinterface.clear();
			nodeToMac.clear();

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
		Iterator ite = parentnode.getRouteInterfaces().iterator();
		while (ite.hasNext()) {
			RouterInterface curIface = (RouterInterface) ite.next();

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

	LinkableNode getLinkableNodeFromNodeId(int nodeid) {

		Iterator ite = activenode.iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();
			if (nodeid == curNode.getNodeId())
				return curNode;
		}
		return null;
	}

	/**
	 * 
	 * @param nodeid
	 * @return LinkableSnmpNode or null if not found
	 */

	boolean isBridgeNode(int nodeid) {

		Iterator ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();
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

		Iterator ite = routerNodes.iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();
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

		Iterator ite = cdpNodes.iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();
			if (nodeid == curNode.getNodeId())
				return true;
		}
		return false;
	}

	private boolean isEndBridgePort(LinkableNode bridge, int bridgeport){

		Set macsOnBridge = bridge.getMacAddressesOnBridgePort(bridgeport);

		if (macsOnBridge == null || macsOnBridge.isEmpty())
			return true;

		Iterator macsonbridge_ite = macsOnBridge.iterator();

		while (macsonbridge_ite.hasNext()) {
			String macaddr = (String) macsonbridge_ite.next();
			if (isMacIdentifierOfBridgeNode(macaddr)) return false;
		}

		return true;
	}
	
	private boolean isNearestBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		Set macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		Set macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

		if (macsOnBridge2 == null || macsOnBridge1 == null)
			return true;

		if (macsOnBridge2.isEmpty() || macsOnBridge1.isEmpty())
			return true;

		Iterator macsonbridge1_ite = macsOnBridge1.iterator();

		while (macsonbridge1_ite.hasNext()) {
			String curMacOnBridge1 = (String) macsonbridge1_ite.next();
			if (bridge2.isBridgeIdentifier(curMacOnBridge1))
				continue;
			if (bridge1.isBridgeIdentifier(curMacOnBridge1))
				continue;
			if (macsOnBridge2.contains(curMacOnBridge1)
					&& isMacIdentifierOfBridgeNode(curMacOnBridge1))
				return false;
		}

		return true;
	}

	private LinkableNode findNearestBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		Set macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		Set macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

		if (macsOnBridge2 == null || macsOnBridge1 == null)
			return bridge2;

		if (macsOnBridge2.isEmpty() || macsOnBridge1.isEmpty())
			return bridge2;

		Iterator macsonbridge1_ite = macsOnBridge1.iterator();

		while (macsonbridge1_ite.hasNext()) {
			String curMacOnBridge1 = (String) macsonbridge1_ite.next();
			if (bridge2.isBridgeIdentifier(curMacOnBridge1))
				continue;
			if (bridge1.isBridgeIdentifier(curMacOnBridge1))
				continue;
			if (macsOnBridge2.contains(curMacOnBridge1)
					&& isMacIdentifierOfBridgeNode(curMacOnBridge1))
				return getNodeFromMacIdentifierOfBridgeNode(curMacOnBridge1);
		}

		return bridge2;
	}

	private Set getMacsOnBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		Set<String> macsOnLink = new HashSet<String>();

    	Set<String> macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

		Set<String> macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		if (macsOnBridge2 == null || macsOnBridge1 == null)
			return null;

		if (macsOnBridge2.isEmpty() || macsOnBridge1.isEmpty())
			return null;

		Iterator macsonbridge1_ite = macsOnBridge1.iterator();

		while (macsonbridge1_ite.hasNext()) {
			String curMacOnBridge1 = (String) macsonbridge1_ite.next();
			if (bridge2.isBridgeIdentifier(curMacOnBridge1))
				continue;
			if (macsOnBridge2.contains(curMacOnBridge1))
				macsOnLink.add(curMacOnBridge1);
		}
		return macsOnLink;
	}

	private boolean isMacIdentifierOfBridgeNode(String macAddress) {
		Iterator ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();
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

	private LinkableNode getNodeFromMacIdentifierOfBridgeNode(String macAddress) {
		Iterator ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();

			if (curNode.isBridgeIdentifier(macAddress))
				return curNode;
		}
		return null;
	}

	private HashMap getBridgesFromMacs(Set macs) {
		HashMap<Integer,LinkableNode> bridges = new HashMap<Integer,LinkableNode>();
		Iterator ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();

			Iterator sub_ite = curNode.getBridgeIdentifiers().iterator();
			while (sub_ite.hasNext()) {
				String curBridgeIdentifier = (String) sub_ite.next();
				if (macs.contains((curBridgeIdentifier)))
					bridges.put(new Integer(curNode.getNodeId()), curNode);
			}
		}
		return bridges;
	}

	private int getBridgePortOnEndBridge(LinkableNode startBridge,
			LinkableNode endBridge, Category log ) {

		int port = -1;
		Iterator bridge_ident_ite = startBridge.getBridgeIdentifiers()
				.iterator();
		while (bridge_ident_ite.hasNext()) {
			String curBridgeIdentifier = (String) bridge_ident_ite.next();
			if (log.isDebugEnabled())
				log
						.debug("getBridgePortOnEndBridge: parsing bridge identifier "
								+ curBridgeIdentifier);
			
			if (endBridge.hasMacAddress(curBridgeIdentifier)) {
				List<Integer> ports = endBridge.getBridgePortsFromMac(curBridgeIdentifier);
				Iterator<Integer> ports_ite = ports.iterator();
				while (ports_ite.hasNext()) {
					port = ports_ite.next();
					if (endBridge.isBackBoneBridgePort(port)) {
						if (log.isDebugEnabled())
							log
									.debug("getBridgePortOnEndBridge: found backbone bridge port "
											+ port
											+ " .... Skipping");
						continue;
					}
					if (port == -1) {
						if (log.isDebugEnabled())
							log
									.debug("run: no port found on bridge nodeid "
											+ endBridge.getNodeId()
											+ " for node bridge identifiers nodeid "
											+ startBridge.getNodeId()
											+ " . .....Skipping");
						continue;
					}
					if (log.isDebugEnabled())
						log
								.debug("run: using mac address table found bridge port "
										+ port
										+ " on node "
										+ endBridge.getNodeId());
					return port;
				}
					
			} else {
				if (log.isDebugEnabled())
					log
							.debug("run: bridge identifier not found on node "
									+ endBridge.getNodeId());
			}
		}
		return -1;
	}

	
	/**
	 * Return the Scheduler
	 * 
	 * @return
	 */

	public Scheduler getScheduler() {
		return m_scheduler;
	}

	/**
	 * Set the Scheduler
	 * 
	 * @param scheduler
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
	 * @return Returns the initial_sleep_time.
	 */

	public long getInitialSleepTime() {
		return initial_sleep_time;
	}

	/**
	 * @param initial_sleep_time
	 *            The initial_sleep_timeto set.
	 */
	public void setInitialSleepTime(long initial_sleep_time) {
		this.initial_sleep_time = initial_sleep_time;
	}

	public boolean isReady() {
		return true;
	}

	/**
	 * @return Returns the discovery_link_interval.
	 */

	public long getDiscoveryInterval() {
		return discovery_interval;
	}

	/**
	 * @param interval
	 *            The discovery_link_interval to set.
	 */

	public void setSnmpPollInterval(long interval) {
		this.snmp_poll_interval = interval;
	}

	/**
	 * @return Returns the discovery_link_interval.
	 */

	public long getSnmpPollInterval() {
		return snmp_poll_interval;
	}

	/**
	 * @param interval
	 *            The discovery_link_interval to set.
	 */

	public void setDiscoveryInterval(long interval) {
		this.discovery_interval = interval;
	}

	public NodeToNodeLink[] getLinks() {
		return (NodeToNodeLink[]) links.toArray(new NodeToNodeLink[0]);
	}

	public MacToNodeLink[] getMacLinks() {
		return (MacToNodeLink[]) maclinks.toArray(new MacToNodeLink[0]);
	}

	/**
	 * @return Returns the suspendCollection.
	 */
	public boolean isSuspended() {
		return suspendCollection;
	}

	/**
	 * @param suspendCollection
	 *            The suspendCollection to set.
	 */

	public void suspend() {
		this.suspendCollection = true;
	}

	/**
	 * @param suspendCollection
	 *            The suspendCollection to set.
	 */

	public void wakeUp() {
		this.suspendCollection = false;
	}

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
								int nodeid2,
								Category log) {

		int bridgeport = node1.getBridgePort(ifindex1);

		if (node1.isBackBoneBridgePort(bridgeport)) {
			if (log.isDebugEnabled())
				log.debug("run: backbone bridge port "
						+ bridgeport
						+ " already parsed. Skipping");
			return false;
		}

		if (isEndBridgePort(node1, bridgeport)) {

			node1.addBackBoneBridgePorts(bridgeport);
			bridgeNodes.put(new Integer(node1.getNodeId()), node1);
			
			Set<String> macs = node1.getMacAddressesOnBridgePort(bridgeport);
			addLinks(macs,node1.getNodeId(),ifindex1,log);
		} else {
			if (log.isDebugEnabled())
				log
						.debug("run: link found not on nearest. Skipping");
			return false;
		}


		return true;
	}

	private boolean parseCdpLinkOn(LinkableNode node1,int ifindex1,
								LinkableNode node2,int ifindex2,
								Category log) {
		
		int bridgeport1 = node1.getBridgePort(ifindex1);

		if (node1.isBackBoneBridgePort(bridgeport1)) {
			if (log.isDebugEnabled())
				log.debug("parseCdpLinkOn: backbone bridge port "
						+ bridgeport1
						+ " already parsed. Skipping");
			return false;
		}
		
		int bridgeport2 = node2
				.getBridgePort(ifindex2);
		if (node2.isBackBoneBridgePort(bridgeport2)) {
			if (log.isDebugEnabled())
				log.debug("parseCdpLinkOn: backbone bridge port "
						+ bridgeport2
						+ " already parsed. Skipping");
			return false;
		}

		if (isNearestBridgeLink(node1, bridgeport1,
				node2, bridgeport2)) {

			node1.addBackBoneBridgePorts(bridgeport1);
			bridgeNodes.put(new Integer(node1.getNodeId()), node1);

			node2.addBackBoneBridgePorts(bridgeport2);
			bridgeNodes.put(new Integer(node2.getNodeId()),node2);

			if (log.isDebugEnabled())
				log.debug("parseCdpLinkOn: Adding node on links.");
			addLinks(getMacsOnBridgeLink(node1,
					bridgeport1, node2, bridgeport2),node1.getNodeId(),ifindex1,log);
		} else {
			if (log.isDebugEnabled())
				log
						.debug("parseCdpLinkOn: link found not on nearest. Skipping");
			return false;
		}
		return true;
	} 	

	private void addNodetoNodeLink(NodeToNodeLink nnlink, Category log) {
		if (nnlink == null)
		{
				log.warn("addNodetoNodeLink: node link is null.");
				return;
		}
		if (!links.isEmpty()) {
			Iterator<NodeToNodeLink> ite = links.iterator();
			while (ite.hasNext()) {
				NodeToNodeLink curNnLink = ite.next();
				if (curNnLink.equals(nnlink)) {
					if (log.isInfoEnabled())
						log.info("addNodetoNodeLink: link " + nnlink.toString() + " exists, not adding");
					return;
				}
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("addNodetoNodeLink: adding link " + nnlink.toString());
		links.add(nnlink);
	}

	private void addLinks(Set macs,int nodeid,int ifindex,Category log) { 
		if (macs == null || macs.isEmpty()) {
			if (log.isDebugEnabled())
				log
						.debug("addLinks: mac's list on link is empty.");
		} else {
			Iterator mac_ite = macs.iterator();

			while (mac_ite.hasNext()) {
				String curMacAddress = (String) mac_ite
						.next();
				if (macsParsed.contains(curMacAddress)) {
					log
							.warn("addLinks: mac address "
									+ curMacAddress
									+ " just found on other bridge port! Skipping...");
					continue;
				}
				
				if (isMacIdentifierOfBridgeNode(curMacAddress)) {
					log
							.warn("addLinks: mac address "
									+ curMacAddress
									+ " is bridge identifier! Skipping...");
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
						addNodetoNodeLink(lNode, log);
					}
				} else {
					if (log.isDebugEnabled())
						log.debug("addLinks: not find nodeid for ethernet mac address "
										+ curMacAddress
										+ " found on node/ifindex" + nodeid+ "/" +ifindex);
					MacToNodeLink lMac = new MacToNodeLink(curMacAddress);
					lMac.setNodeparentid(nodeid);
					lMac.setParentifindex(ifindex);
					maclinks.add(lMac);
				}
				macsParsed.add(curMacAddress);
			}
		}
	}
	
	public boolean equals(ReadyRunnable r) {
		return (r instanceof DiscoveryLink);
	}
	
	public String getInfo() {
		return " Ready Runnable Discovery Link ";
	}



}