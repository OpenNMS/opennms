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
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
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

	private List activenode = new ArrayList();

	private List links = new ArrayList();

	private List maclinks = new ArrayList();

	private HashMap m_bridge = new HashMap();

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

			List routerNodes = new ArrayList();

			List cdpNodes = new ArrayList();

			List macsParsed = new ArrayList();

			Iterator ite = null;

			for (int i = 0; i < all_snmplinknodes.length; i++) {
				LinkableNode curNode = all_snmplinknodes[i];

				if (curNode == null) {
					if (log.isEnabledFor(Priority.ERROR))
						log.error("run: null linkable node found for iterator "
								+ i);
					continue;
				}

				int curNodeId = curNode.getNodeId();
				String curIpAddr = curNode.getSnmpPrimaryIpAddr();
				activenode.add(curNode);

				if (curNode.isBridgeNode)
					m_bridge.put(new Integer(curNodeId), curNode);
				if (curNode.hasCdpInterfaces())
					cdpNodes.add(curNode);
				if (curNode.hasRouteInterfaces())
					routerNodes.add(curNode);

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
					log.debug("run: parsing bridge nodeid " + curCdpNodeId
							+ " ip address " + curCdpIpAddr);

				if (log.isDebugEnabled())
					log.debug("run: parsing  "
							+ curNode.getCdpInterfaces().size()
							+ " Cdp Interfaces. ");

				Iterator sub_ite = curNode.getCdpInterfaces().iterator();
				while (sub_ite.hasNext()) {
					CdpInterface cdpIface = (CdpInterface) sub_ite.next();

					InetAddress targetIpAddr = cdpIface.getCdpTargetIpAddr();

					int targetCdpNodeId = cdpIface.getCdpTargetNodeId();

					if (targetCdpNodeId == -1) {
						if (log.isDebugEnabled())
							log.debug("run: no node id found for ip address "
									+ targetIpAddr.getHostAddress()
									+ ". Skipping");
						continue;
					}

					if (targetCdpNodeId == curCdpNodeId) {
						if (log.isDebugEnabled())
							log.debug("run: node id found for ip address "
									+ targetIpAddr.getHostAddress()
									+ " is itself. Skipping");
						continue;
					}

					int cdpIfIndex = cdpIface.getCdpIfIndex();
					if (cdpIfIndex < 0) {
						log.warn("run: found not valid CDP IfIndex "
								+ cdpIfIndex + " . Skipping");
						continue;
					}

					int cdpDestIfindex = cdpIface.getCdpTargetIfIndex();
					if (cdpDestIfindex < 0) {
						log
								.warn("run: found not valid CDP destination IfIndex "
										+ cdpDestIfindex + " . Skipping");
						continue;
					}

					if (log.isDebugEnabled())
						log.debug("run: found link: nodeid " + curCdpNodeId
								+ " ifindex " + cdpIfIndex + " other nodeid "
								+ targetCdpNodeId + " ifindex "
								+ cdpDestIfindex);

					if (curNode.isBridgeNode() && isBridgeNode(targetCdpNodeId)) {
						int bridgeport1 = curNode.getBridgePort(cdpIfIndex);
						if (curNode.isBackBoneBridgePort(bridgeport1)) {
							if (log.isDebugEnabled())
								log.debug("run: backbone bridge port "
										+ bridgeport1
										+ " already parsed. Skipping");
							continue;
						}
						LinkableNode targetNode = (LinkableNode) m_bridge
								.get(new Integer(targetCdpNodeId));
						int bridgeport2 = targetNode
								.getBridgePort(cdpDestIfindex);
						if (targetNode.isBackBoneBridgePort(bridgeport2)) {
							if (log.isDebugEnabled())
								log.debug("run: backbone bridge port "
										+ bridgeport2
										+ " already parsed. Skipping");
							continue;
						}

						if (isNearestBridgeLink(curNode, bridgeport1,
								targetNode, bridgeport2)) {

							curNode.addBackBoneBridgePorts(bridgeport1);
							targetNode.addBackBoneBridgePorts(bridgeport2);
							m_bridge.put(new Integer(curCdpNodeId), curNode);
							m_bridge.put(new Integer(targetCdpNodeId),
									targetNode);
							List temp_macs = getMacsOnBridgeLink(curNode,
									bridgeport1, targetNode, bridgeport2);
							if (temp_macs == null || temp_macs.isEmpty()) {
								if (log.isDebugEnabled())
									log
											.debug("run: mac's list on link is empty.");
							} else {
								Iterator mac_ite = temp_macs.iterator();

								if (log.isDebugEnabled())
									log
											.debug("run: finding ethernet link on founded bridge node link");

								while (mac_ite.hasNext()) {
									String curMacAddress = (String) mac_ite
											.next();
									if (macsParsed.contains(curMacAddress)) {
										log
												.warn("run: mac address "
														+ curMacAddress
														+ " just found on other bridge port! Possible Ethernet Loop. ");
										continue;

									}
									macsParsed.add(curMacAddress);
									if (log.isDebugEnabled())
										log
												.debug("run: find ethernet mac address "
														+ curMacAddress
														+ " on bridge link");

									MacToNodeLink lMac = new MacToNodeLink(
											curMacAddress);
									lMac.setNodeparentid(curCdpNodeId);
									lMac.setParentifindex(cdpIfIndex);
									maclinks.add(lMac);
								}
							}
						} else {
							if (log.isDebugEnabled())
								log
										.debug("run: link found not on nearest. Skipping");
							continue;

						}
					} else if (curNode.isBridgeNode
							|| isBridgeNode(targetCdpNodeId)) {
						if (log.isDebugEnabled())
							log
									.debug("run: ethernet link found not on bridge nodes. Skipping");
						continue;
					}
					NodeToNodeLink lk = new NodeToNodeLink(targetCdpNodeId,
							cdpDestIfindex);
					lk.setNodeparentid(curCdpNodeId);
					lk.setParentifindex(cdpIfIndex);
					links.add(lk);
					if (log.isDebugEnabled())
						log.debug("run: link found added.");

				}
			}

			// try get backbone links between switches using STP info
			// and store information in Bridge class
			if (log.isDebugEnabled())
				log
						.debug("run: try to found backbone ethernet links among bridge nodes using Spanning Tree Protocol");

			ite = m_bridge.values().iterator();

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

					Iterator stp_ite = ((List) me.getValue()).iterator();
					// Now parse STP bridge port info to get designated bridge
					if (log.isDebugEnabled())
						log
								.debug("run: STP designated root is another bridge. Parsing Stp Interface");
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
						//NOTE—The number of bits that are considered to be
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
						int designatednodeid = designatedNode.getNodeId();

						if (designatedNode == null) {
							log
									.warn("run: no nodeid found for stp bridge address "
											+ stpPortDesignatedBridge
											+ " . Nothing to save to db");
							continue; // no saving info if no nodeid
						}
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
						m_bridge.put(new Integer(curNodeId), curNode);


						if (log.isDebugEnabled())
							log.debug("run: backbone port found for node "
									+ designatednodeid
									+ " .Adding to helper class bb port "
									+ " bridge port " + designatedbridgeport);

						designatedNode
								.addBackBoneBridgePorts(designatedbridgeport);
						m_bridge.put(new Integer(designatednodeid),
								designatedNode);

						// writing to db using class
						// DbDAtaLinkInterfaceEntry
						NodeToNodeLink lk = new NodeToNodeLink(curNodeId,
								curIfIndex);
						lk.setNodeparentid(designatednodeid);
						lk.setParentifindex(designatedifindex);
						links.add(lk);

						// finding links between two backbone ports
						List temp_macs = getMacsOnBridgeLink(curNode,
								stpbridgeport, designatedNode,
								designatedbridgeport);
						if (temp_macs == null || temp_macs.isEmpty()) {
							if (log.isDebugEnabled())
								log.debug("run: macs list on link is empty.");
							continue;
						}

						Iterator mac_ite = temp_macs.iterator();

						if (log.isDebugEnabled())
							log
									.debug("run: try to found  ethernet links on bridge node link");

						while (mac_ite.hasNext()) {
							String curMacAddress = (String) mac_ite.next();

							if (macsParsed.contains(curMacAddress)) {
								log
										.warn("run: mac address "
												+ curMacAddress
												+ " just found on other bridge port! Possible Ethernet Loop. ");
								continue;

							}
							macsParsed.add(curMacAddress);
							if (log.isDebugEnabled())
								log.debug("run: find ethernet mac address "
										+ curMacAddress + " on bridge link");

							MacToNodeLink lMac = new MacToNodeLink(
									curMacAddress);
							lMac.setNodeparentid(designatednodeid);
							lMac.setParentifindex(designatedifindex);
							maclinks.add(lMac);
						}
					}
				}
			}

			// finding backbone links using mac address on ports
			if (log.isDebugEnabled())
				log
						.debug("run: try to found remaining links using Mac Address Forwarding Table");

			ite = m_bridge.values().iterator();

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
					// operazione A cerco ottengo la lista dei bridge sulla
					// porta

					List macs = new ArrayList();
					macs = curNode.getMacAddressesOnBridgePort(curBridgePort);

					HashMap bridgesOnPort = new HashMap();
					bridgesOnPort = getBridgesFromMacs(macs);
					if (bridgesOnPort.isEmpty()) {
						if (log.isDebugEnabled())
							log.debug("run: no bridge info found on port "
									+ curBridgePort + " .... Saving Macs");
						Iterator mac_ite = macs.iterator();

						// this is the point where you can test what are the
						// macs
						// learned on port

						while (mac_ite.hasNext()) {
							String macAddress = (String) mac_ite.next();
							if (macsParsed.contains(macAddress)) {
								log
										.warn("run: mac address "
												+ macAddress
												+ " just found on other bridge port! Possible Ethernet Loop. ");
								continue;

							}

							macsParsed.add(macAddress);

							int curIfIndex = curNode.getIfindex(curBridgePort);
							MacToNodeLink lkm = new MacToNodeLink(macAddress);
							lkm.setNodeparentid(curNodeId);
							lkm.setParentifindex(curIfIndex);
							maclinks.add(lkm);
						}
					} else {
						Iterator bridge_ite = bridgesOnPort.values().iterator();
						BRIDGE: while (bridge_ite.hasNext()) {
							LinkableNode endNode = (LinkableNode) bridge_ite
									.next();
							int endNodeid = endNode.getNodeId();
							int endBridgePort = getBridgePortOnEndBridge(
									curNode, endNode);
							if (endNode.isBackBoneBridgePort(endBridgePort)) {
								if (log.isDebugEnabled())
									log
											.debug("run: testing backbone bridge port "
													+ endBridgePort
													+ " .... Skipping");
								continue;
							}
							if (log.isDebugEnabled())
								log
										.debug("run: using mac address table found bridge port "
												+ endBridgePort
												+ " on node "
												+ endNodeid);

							if (endBridgePort == -1) {
								if (log.isDebugEnabled())
									log
											.debug("run: no port found on bridge nodeid "
													+ endNodeid
													+ " for node bridge identifiers nodeid "
													+ curNodeId
													+ " . .....Skipping");
								continue;
							}

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
											curNode, endNode);

									if (endNode
											.isBackBoneBridgePort(endBridgePort)) {
										if (log.isDebugEnabled())
											log
													.debug("run: testing backbone bridge port "
															+ endBridgePort
															+ " .... Skipping");
										continue BRIDGE;
									}

									if (log.isDebugEnabled())
										log
												.debug("run: using mac address table found bridge port "
														+ endBridgePort
														+ " on node "
														+ endNodeid);

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
							int curIfIndex = curNode.getIfindex(curBridgePort);
							if (curIfIndex == -1) {
								log
										.warn("run: got invalid ifindex on bridge port "
												+ curBridgePort);
								continue;
							}

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
							m_bridge.put(new Integer(curNodeId), curNode);

							if (log.isDebugEnabled())
								log.debug("run: backbone port found for node "
										+ endNodeid
										+ " .Adding to helper class bb port "
										+ " bridge port " + endBridgePort);

							endNode.addBackBoneBridgePorts(endBridgePort);
							m_bridge.put(new Integer(endNodeid), endNode);


							// writing to db using class
							// DbDAtaLinkInterfaceEntry
							NodeToNodeLink lk = new NodeToNodeLink(curNodeId,
									curIfIndex);
							lk.setNodeparentid(endNodeid);
							lk.setParentifindex(endIfindex);
							links.add(lk);
							// finding links between two backbone ports
							List temp_macs = getMacsOnBridgeLink(curNode,
									curBridgePort, endNode, endBridgePort);
							if (temp_macs == null || temp_macs.isEmpty()) {
								if (log.isDebugEnabled())
									log
											.debug("run: macs list on link is empty.");
								continue;
							}
							Iterator mac_ite = temp_macs.iterator();

							if (log.isDebugEnabled())
								log
										.debug("run: try to found  ethernet links on bridge node link");

							while (mac_ite.hasNext()) {
								String curMacAddress = (String) mac_ite.next();
								if (macsParsed.contains(curMacAddress)) {
									log
											.warn("run: mac address "
													+ curMacAddress
													+ " just found on other bridge port! Possible Ethernet Loop. ");
									continue;

								}
								macsParsed.add(curMacAddress);

								if (log.isDebugEnabled())
									log
											.debug("run: find ethernet mac address "
													+ curMacAddress
													+ " on bridge link");

								MacToNodeLink lkm = new MacToNodeLink(
										curMacAddress);
								lkm.setNodeparentid(endNodeid);
								lkm.setParentifindex(endIfindex);
								maclinks.add(lkm);
							}
							break BRIDGE;
						}
					}
				}

			}

			// now found remaing links (not yet parsed) present on one side of backbone link

			ite = m_bridge.values().iterator();

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
								.warn("run: bridge port has no mac address defined.   Skipping. ");
						continue;
					}
					Iterator mac_ite = curNode.getMacAddressesOnBridgePort(
							bridgePort).iterator();
					while (mac_ite.hasNext()) {
						String macAddress = (String) mac_ite.next();
						if (macsParsed.contains(macAddress)) {
							log.info("run: mac address " + macAddress
									+ " already parsed! Skipping. ");
							continue;

						}

						if (isMacIdentifierOfBridgeNode(macAddress)) {
							log.info("run: mac address " + macAddress
									+ " identifies a bridge node! Skipping. ");
							continue;

						}
						if (log.isDebugEnabled())
							log.debug("run: found mac addresses " + macAddress
									+ " on link ");

						macsParsed.add(macAddress);

						int curIfIndex = curNode.getIfindex(bridgePort);
						// get the ifindex
						if (curIfIndex == -1) {
							log
									.warn("run: got invalid ifindex on backbone bridge port "
											+ bridgePort);
							continue;
						}

						MacToNodeLink lkm = new MacToNodeLink(macAddress);
						lkm.setNodeparentid(curNode.getNodeId());
						lkm.setParentifindex(curIfIndex);
						maclinks.add(lkm);
					}
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

					if (routeIface.getMetric() == -1) {
						if (log.isDebugEnabled())
							log
									.debug("run: Router interface has invalid metric "
											+ routeIface.getMetric()
											+ ". Skipping");
						continue;
					}

					int ifindex = routeIface.getIfindex();

					if (ifindex == -1) {
						if (log.isDebugEnabled())
							log
									.debug("run: route interface has invalid ifindex "
											+ ifindex + " . Skipping");
						continue;
					}

					int snmpiftype = routeIface.getSnmpiftype();

					// no processing ethernet type
					if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
						if (log.isDebugEnabled())
							log
									.debug("run: Ethernet interface for nodeid. Skipping ");
						continue;
					}
					// no processing unknown type
					if (snmpiftype == -1) {
						if (log.isDebugEnabled())
							log.debug("run: interface has unknown snmpiftype "
									+ snmpiftype + " . Skipping ");
						continue;
					}

					InetAddress nexthop = routeIface.getNextHop();

					if (nexthop.getHostAddress().equals("/0.0.0.0")) {
						if (log.isDebugEnabled())
							log
									.debug("run: nexthop address is broadcast address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						continue;
					}

					if (nexthop.getHostAddress().equals("127.0.0.1")) {
						if (log.isDebugEnabled())
							log
									.debug("run: nexthop address is localhost address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						continue;
					}

					int nodeparentid = routeIface.getNodeparentid();

					if (nodeparentid == -1) {
						if (log.isDebugEnabled())
							log
									.debug("run: no node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " , skipping ");
						continue;
					}

					if (nodeparentid == curNodeId) {
						if (log.isDebugEnabled())
							log
									.debug("run: node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " is itself, skipping ");
						continue;
					}

					//find ifindex on parent node
					int parentifindex = -1;
					LinkableNode nodeparent = getLinkableNodeFromNodeId(nodeparentid);
					if (nodeparent != null) {
						parentifindex = getIfIndexFromParentRouter(nodeparent,
								curNodeId);
					}
					NodeToNodeLink lk = new NodeToNodeLink(nodeparentid,
							parentifindex);
					lk.setNodeparentid(curNodeId);
					lk.setParentifindex(ifindex);
					links.add(lk);
				}
			}

			//		making clean
			activenode.clear();
			m_bridge.clear();
			routerNodes.clear();
			cdpNodes.clear();
			macsParsed.clear();

			Linkd.getInstance().updateDiscoveryLinkCollection(this);

			links.clear();
			maclinks.clear();
		}
		// rescheduling activities
		isRunned = true;
		reschedule();
	}

	private int getIfIndexFromParentRouter(LinkableNode parentnode, int nodeid) {
		Category log = ThreadCategory.getInstance(getClass());

		if (!parentnode.hasRouteInterfaces())
			return -1;
		Iterator ite = parentnode.getRouteInterfaces().iterator();
		while (ite.hasNext()) {
			RouterInterface routeIface = (RouterInterface) ite.next();

			if (routeIface.getMetric() == -1) {
				if (log.isDebugEnabled())
					log
							.debug("getIfIndexFromParentRouter: Data Link interface with nodeid "
									+ parentnode.getNodeId()
									+ " has invalid metric.");
				continue;
			}

			int ifindex = routeIface.getIfindex();

			if (ifindex == 0 || ifindex == -1)
				continue;

			int snmpiftype = routeIface.getSnmpiftype();

			// no processing ethernet type or unknown
			if (snmpiftype == SNMP_IF_TYPE_ETHERNET || snmpiftype == -1)
				continue;

			InetAddress nexthop = routeIface.getNextHop();
			if (nexthop.toString().equals("0.0.0.0"))
				continue; // this case must be analized in detail
			if (nexthop.toString().equals("127.0.0.1"))
				continue;

			int curnodeid = -1;
			curnodeid = routeIface.getNodeparentid();

			if (curnodeid == nodeid)
				return ifindex;
		}
		return -1;
	}

	/**
	 * 
	 * @param nodeid
	 * @return LinkableSnmpNode or null if not found
	 */

	private LinkableNode getLinkableNodeFromNodeId(int nodeid) {

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

	private boolean isBridgeNode(int nodeid) {

		Iterator ite = m_bridge.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();
			if (nodeid == curNode.getNodeId())
				return true;
		}
		return false;
	}

	private boolean isNearestBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		List macsOnBridge2 = new ArrayList();

		macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		List macsOnBridge1 = new ArrayList();
		macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

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

		List macsOnBridge2 = new ArrayList();
		macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

		List macsOnBridge1 = new ArrayList();
		macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

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

	private List getMacsOnBridgeLink(LinkableNode bridge1, int bp1,
			LinkableNode bridge2, int bp2) {

		List macsOnLink = new ArrayList();

		List macsOnBridge1 = new ArrayList();
		macsOnBridge1 = bridge1.getMacAddressesOnBridgePort(bp1);

		List macsOnBridge2 = new ArrayList();
		macsOnBridge2 = bridge2.getMacAddressesOnBridgePort(bp2);

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
		Iterator ite = m_bridge.values().iterator();
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
		Iterator ite = m_bridge.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = (LinkableNode) ite.next();

			if (curNode.isBridgeIdentifier(macAddress))
				return curNode;
		}
		return null;
	}

	private HashMap getBridgesFromMacs(List macs) {
		HashMap bridges = new HashMap();
		Iterator ite = m_bridge.values().iterator();
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
			LinkableNode endBridge) {

		Iterator bridge_ident_ite = startBridge.getBridgeIdentifiers()
				.iterator();
		while (bridge_ident_ite.hasNext()) {
			String curBridgeIdentifier = (String) bridge_ident_ite.next();
			if (endBridge.hasMacAddress(curBridgeIdentifier)) {
				return endBridge.getBridgePort(curBridgeIdentifier);
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

	public boolean isSnmpCollection() {
		return false;
	}

	public boolean isDiscoveryLink() {
		return true;
	}

	public InetAddress getTarget() throws UnknownHostException {
		return InetAddress.getLocalHost();
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

	public void unschedule() throws UnknownHostException, Throwable {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"rescedule: Cannot schedule a service whose scheduler is set to null");
		if (isRunned) {
			m_scheduler.unschedule(getTarget(), snmp_poll_interval);
		} else {
			m_scheduler.unschedule(getTarget(), snmp_poll_interval
					+ initial_sleep_time + discovery_interval);
		}
	}

}