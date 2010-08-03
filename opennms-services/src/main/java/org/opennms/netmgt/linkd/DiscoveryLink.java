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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.snmp.FdbTableGet;
import org.opennms.netmgt.linkd.snmp.VlanCollectorEntry;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * This class is designed to discover link among nodes using the collected and
 * the necessary SNMP information. When the class is initially constructed no
 * information is used.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @version $Id: $
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
	
	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
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
			log().warn("DiscoveryLink.run: Suspended!");
		} else {
			Collection<LinkableNode> all_snmplinknodes = Linkd.getInstance()
					.getLinkableNodesOnPackage(getPackageName());

			if (log().isDebugEnabled()) {
				log().debug("run: LinkableNodes/package found: " + all_snmplinknodes.size() +"/" + getPackageName());
				log().debug("run: discoveryUsingBridge/discoveryUsingCdp/discoveryUsingRoutes: " + discoveryUsingBridge+"/" + discoveryUsingCdp +"/" + discoveryUsingRoutes);
				log().debug("run: enableDownloadDiscovery: " +enableDownloadDiscovery);
			}
			Iterator<LinkableNode> ite = all_snmplinknodes.iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				if (log().isDebugEnabled())
					log().debug("run: Iterating on LinkableNode's found node: " + curNode.getNodeId());

				if (curNode == null) {
						log().error("run: null linkable node found for iterator " + ite);
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

			

			if (log().isDebugEnabled())
				log().debug("run: using atNodes to populate macToAtinterface");

			ite = atNodes.iterator();
			while (ite.hasNext()) {
				Iterator<AtInterface> at_ite = ite.next().getAtInterfaces().iterator();
				while (at_ite.hasNext()) {
					AtInterface at = at_ite.next();
					int nodeid = at.getNodeId();
					String ipaddr = at.getIpAddress();
					String macAddress = at.getMacAddress();
					if (log().isDebugEnabled()) {
						log().debug("Parsing at Interface nodeid/ipaddr/macaddr: " + nodeid + "/" + ipaddr +"/" + macAddress);
					}
					if (!Linkd.getInstance().isInterfaceInPackage(at.getIpAddress(), getPackageName())) {
						if (log().isInfoEnabled()) 
							log()
							.info("run: at interface: " + ipaddr+ " does not belong to package: " + getPackageName()+ "! Not adding to discoverable atinterface.");
						macsExcluded.add(macAddress);
						continue;
					}
					if (isMacIdentifierOfBridgeNode(macAddress)) {
						if (log().isInfoEnabled()) 
						log()
						.info("run: at interface "
								+ macAddress
								+ " belongs to bridge node! Not adding to discoverable atinterface.");
						macsExcluded.add(macAddress);
						continue;
					}
                    if (macAddress.indexOf("00000c07ac") == 0) {
                       log().info("run: at interface "
                                   + macAddress
                                   + " is cisco hsrp address! Not adding to discoverable atinterface.");
                       macsExcluded.add(macAddress); 
                       continue; 
                    }
					List<AtInterface> ats = macToAtinterface.get(macAddress);
					if (ats == null) ats = new ArrayList<AtInterface>();
					if (log().isInfoEnabled()) 
						log()
						.info("parseAtNodes: Adding to discoverable atinterface.");
					ats.add(at);
					macToAtinterface.put(macAddress, ats);
					if (log().isDebugEnabled())
						log().debug("parseAtNodes: mac:" + macAddress + " has now atinterface reference: " + ats.size());
				}		
			}

			if (log().isDebugEnabled())
				log().debug("run: end populate macToAtinterface");

			//now perform operation to complete
			if (enableDownloadDiscovery) {
				if (log().isInfoEnabled())
					log().info("run: get further unknown mac address snmp bridge table info");
				snmpParseBridgeNodes();
			} else {
				if (log().isInfoEnabled())
					log().info("run: skipping get further unknown mac address snmp bridge table info");
			}

			// First of all use quick methods to get backbone ports for speeding
			// up the link discovery

			if (log().isDebugEnabled())
				log()
						.debug("run: finding links among nodes using Cisco Discovery Protocol");

			// Try Cisco Discovery Protocol to found link among all nodes
			// Add CDP info for backbones ports

			ite = cdpNodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				int curCdpNodeId = curNode.getNodeId();
				String curCdpIpAddr = curNode.getSnmpPrimaryIpAddr();

				if (log().isDebugEnabled())
					log().debug("run: parsing nodeid " + curCdpNodeId
							+ " ip address " + curCdpIpAddr + " with "
							+ curNode.getCdpInterfaces().size()
							+ " Cdp Interfaces. ");

				Iterator<CdpInterface> sub_ite = curNode.getCdpInterfaces().iterator();
				while (sub_ite.hasNext()) {
					CdpInterface cdpIface = sub_ite.next();

					int cdpIfIndex = cdpIface.getCdpIfIndex();
					
					if (cdpIfIndex < 0) {
						log().warn("run: found not valid CDP IfIndex "
								+ cdpIfIndex + " . Skipping");
						continue;
					}

					if (log().isDebugEnabled()) log().debug("run: found CDP ifindex " + cdpIfIndex);

					InetAddress targetIpAddr = cdpIface.getCdpTargetIpAddr();
					
					if (!Linkd.getInstance().isInterfaceInPackage(targetIpAddr.getHostAddress(), getPackageName())) 
					{
						log().warn("run: ip address "
								+ targetIpAddr.getHostAddress()
								+ " Not in package: " +getPackageName()+". Skipping");
					continue;
				}

					int targetCdpNodeId = cdpIface.getCdpTargetNodeId();

					if (targetCdpNodeId == -1) {
							log().warn("run: no node id found for ip address "
									+ targetIpAddr.getHostAddress()
									+ ". Skipping");
						continue;
					}

					if (log().isDebugEnabled()) log().debug("run: found nodeid/CDP target ipaddress: " + targetCdpNodeId+ ":"+ targetIpAddr);

					if (targetCdpNodeId == curCdpNodeId) {
						if (log().isDebugEnabled())
							log().debug("run: node id found for ip address "
									+ targetIpAddr.getHostAddress()
									+ " is itself. Skipping");
						continue;
					}

					int cdpDestIfindex = cdpIface.getCdpTargetIfIndex();
					
					if (cdpDestIfindex < 0) {
						log()
								.warn("run: found not valid CDP destination IfIndex "
										+ cdpDestIfindex + " . Skipping");
						continue;
					}
					
					if (log().isDebugEnabled()) log().debug("run: found CDP target ifindex " + cdpDestIfindex);

					if (log().isDebugEnabled())
						log().debug("run: parsing CDP link: nodeid=" + curCdpNodeId
								+ " ifindex=" + cdpIfIndex + " nodeparentid="
								+ targetCdpNodeId + " parentifindex="
								+ cdpDestIfindex);

					boolean add = false;
					if (curNode.isBridgeNode() && isBridgeNode(targetCdpNodeId)) {
						LinkableNode targetNode = bridgeNodes.get(new Integer(targetCdpNodeId));
						add = parseCdpLinkOn(curNode, cdpIfIndex,targetNode, cdpDestIfindex);
						if (log().isDebugEnabled())
							log().debug("run: both node are bridge nodes! Adding: " + add);
					} else if (curNode.isBridgeNode) {
						if (log().isDebugEnabled())
							log().debug("run: source node is bridge node, target node is not bridge node! Adding: " + add);
						add = parseCdpLinkOn(curNode,cdpIfIndex,targetCdpNodeId);
					} else if (isBridgeNode(targetCdpNodeId)) {
						if (log().isDebugEnabled())
							log().debug("run: source node is not bridge node, target node is bridge node! Adding: " + add);
						LinkableNode targetNode = bridgeNodes.get(new Integer(targetCdpNodeId));
						add = parseCdpLinkOn(targetNode,cdpDestIfindex,curCdpNodeId);
					} else {
						if (log().isDebugEnabled())
							log().debug("run: no node is bridge node! Adding CDP link");
							add = true;
					}

					// now add the cdp link
					if (add) {
						NodeToNodeLink lk = new NodeToNodeLink(targetCdpNodeId,
								cdpDestIfindex);
						lk.setNodeparentid(curCdpNodeId);
						lk.setParentifindex(cdpIfIndex);
						addNodetoNodeLink(lk);
						if (log().isDebugEnabled())
							log().debug("run: CDP link added: " + lk.toString());
					}
				}
			}

			// try get backbone links between switches using STP info
			// and store information in Bridge class
			if (log().isDebugEnabled())
				log()
						.debug("run: try to found backbone ethernet links among bridge nodes using Spanning Tree Protocol");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();

				int curNodeId = curNode.getNodeId();
				String cupIpAddr = curNode.getSnmpPrimaryIpAddr();

				if (log().isDebugEnabled())
					log().debug("run: parsing bridge nodeid " + curNodeId
							+ " ip address " + cupIpAddr);

				Iterator<Map.Entry<String, List<BridgeStpInterface>>> sub_ite = curNode.getStpInterfaces().entrySet()
						.iterator();

				if (log().isDebugEnabled())
					log().debug("run: parsing "
							+ curNode.getStpInterfaces().size() + " Vlan. ");

				while (sub_ite.hasNext()) {
					Map.Entry<String, List<BridgeStpInterface>> me = sub_ite.next();
					String vlan = me.getKey();
					String curBaseBridgeAddress = curNode
							.getBridgeIdentifier(vlan);

					if (log().isDebugEnabled())
						log().debug("run: found bridge identifier "
								+ curBaseBridgeAddress);

					String designatedRoot = null;
					
					if (curNode.hasStpRoot(vlan)) {
						designatedRoot = curNode.getStpRoot(vlan);
					} else {
						if (log().isDebugEnabled())
							log()
									.debug("run: desigated root bridge identifier not found. Skipping"
											+ curBaseBridgeAddress);
						continue;
					}

					if (designatedRoot == null || designatedRoot.equals("0000000000000000")) {
						log().warn("run: designated root is invalid. Skipping");
						continue;
					}
					// check if designated
					// bridge is it self
					// if bridge is STP root bridge itself exiting
					// searching on linkablesnmpnodes

					if (curNode.isBridgeIdentifier(designatedRoot.substring(4))) {
						if (log().isDebugEnabled())
							log()
									.debug("run: STP designated root is the bridge itself. Skipping");
						continue;
					}

					// Now parse STP bridge port info to get designated bridge
					if (log().isDebugEnabled())
						log()
								.debug("run: STP designated root is another bridge. " + designatedRoot + " Parsing Stp Interface");

					Iterator<BridgeStpInterface> stp_ite = me.getValue().iterator();
					while (stp_ite.hasNext()) {
						BridgeStpInterface stpIface = stp_ite
								.next();

						// the bridge port number
						int stpbridgeport = stpIface.getBridgeport();
						// if port is a backbone port continue
						if (curNode.isBackBoneBridgePort(stpbridgeport)) {
							if (log().isDebugEnabled())
								log().debug("run: bridge port " + stpbridgeport
										+ " already found .... Skipping");
							continue;
						}

						String stpPortDesignatedPort = stpIface
								.getStpPortDesignatedPort();
						String stpPortDesignatedBridge = stpIface
								.getStpPortDesignatedBridge();

						if (log().isDebugEnabled())
							log().debug("run: parsing bridge port "
									+ stpbridgeport
									+ " with stp designated bridge "
									+ stpPortDesignatedBridge
									+ " and with stp designated port "
									+ stpPortDesignatedPort);

						if (stpPortDesignatedBridge == null || stpPortDesignatedBridge.equals("0000000000000000")
						        || stpPortDesignatedBridge.equals("")) {
							log().warn("run: designated bridge is invalid "
									+ stpPortDesignatedBridge);
							continue;
						}

						if (curNode.isBridgeIdentifier(stpPortDesignatedBridge
								.substring(4))) {
							if (log().isDebugEnabled())
								log().debug("run: designated bridge for port "
										+ stpbridgeport + " is bridge itself ");
							continue;
						}

						if (stpPortDesignatedPort == null || stpPortDesignatedPort.equals("0000")) {
							log().warn("run: designated port is invalid "
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
							log()
									.warn("run: no nodeid found for stp bridge address "
											+ stpPortDesignatedBridge
											+ " . Nothing to save to db");
							continue; // no saving info if no nodeid
						}
						
						int designatednodeid = designatedNode.getNodeId();

						if (log().isDebugEnabled())
							log().debug("run: found designated nodeid "
									+ designatednodeid);

						// test if there are other bridges between this link
						// USING MAC ADDRESS FORWARDING TABLE

						if (!isNearestBridgeLink(curNode, stpbridgeport,
								designatedNode, designatedbridgeport)) {
							if (log().isDebugEnabled())
								log()
										.debug("run: other bridge found between nodes. No links to save!");
							continue; // no saving info if no nodeid
						}

						// this is a backbone port so try adding to Bridge class
						// get the ifindex on node

						int curIfIndex = curNode.getIfindex(stpbridgeport);

						if (curIfIndex == -1) {
							log().warn("run: got invalid ifindex");
							continue;
						}

						int designatedifindex = designatedNode
						.getIfindex(designatedbridgeport);
						
						if (designatedifindex == -1) {
							log()
									.warn("run: got invalid ifindex on designated node");
							continue;
						}

						if (log().isDebugEnabled())
							log().debug("run: backbone port found for node "
									+ curNodeId + ". Adding to bridge"
									+ stpbridgeport);

						curNode.addBackBoneBridgePorts(stpbridgeport);
						bridgeNodes.put(new Integer(curNodeId), curNode);

						if (log().isDebugEnabled())
							log().debug("run: backbone port found for node "
									+ designatednodeid
									+ " .Adding to helper class bb port "
									+ " bridge port " + designatedbridgeport);

						designatedNode
								.addBackBoneBridgePorts(designatedbridgeport);
						bridgeNodes.put(new Integer(designatednodeid),
								designatedNode);
						
						if (log().isDebugEnabled())
							log().debug("run: adding links on bb bridge port " + designatedbridgeport);

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

			if (log().isDebugEnabled())
				log()
						.debug("run: try to found links using Mac Address Forwarding Table");

			ite = bridgeNodes.values().iterator();

			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				int curNodeId = curNode.getNodeId();
				if (log().isDebugEnabled())
					log().debug("run: parsing node bridge " + curNodeId);

				Iterator<Integer> sub_ite = curNode.getPortMacs().keySet().iterator();

				while (sub_ite.hasNext()) {
					Integer intePort = sub_ite.next();
					int curBridgePort = intePort.intValue();

					if (log().isDebugEnabled())
						log().debug("run: parsing bridge port "
								+ curBridgePort
								+ " with mac addresses "
								+ curNode.getMacAddressesOnBridgePort(
										curBridgePort).toString());

					if (curNode.isBackBoneBridgePort(curBridgePort)) {
						if (log().isDebugEnabled())
							log().debug("run: parsing backbone bridge port "
									+ curBridgePort + " .... Skipping");
						continue;
					}
					
					int curIfIndex = curNode.getIfindex(curBridgePort);
					if (curIfIndex == -1) {
						log().warn("run: got invalid ifindex on bridge port "
											+ curBridgePort);
						continue;
					}
					// First get the mac addresses on bridge port

					Set<String> macs = curNode.getMacAddressesOnBridgePort(curBridgePort);

					// Then find the bridges whose mac addresses are learned on bridge port
					List<LinkableNode> bridgesOnPort = getBridgesFromMacs(macs);
					
					if (bridgesOnPort.isEmpty()) {
						if (log().isDebugEnabled())
							log().debug("run: no bridge info found on port "
									+ curBridgePort + " .... Saving Macs");
						addLinks(macs, curNodeId, curIfIndex);
					} else {
						// a bridge mac address was found on port so you should analyze what happens
						if (log().isDebugEnabled())
							log().debug("run: bridge info found on port "
									+ curBridgePort + " .... Finding nearest.");
						Iterator<LinkableNode> bridge_ite = bridgesOnPort.iterator();
						// one among these bridges should be the node more close to the curnode, curport
						while (bridge_ite.hasNext()) {
							LinkableNode endNode = bridge_ite
									.next();
							
							int endNodeid = endNode.getNodeId();
							
							int endBridgePort = getBridgePortOnEndBridge(
									curNode, endNode);
// The bridge port should be valid! This control is not properly done
							if (endBridgePort == -1) {
									log()
											.error("run: no valid port found on bridge nodeid "
													+ endNodeid
													+ " for node bridge identifiers nodeid "
													+ curNodeId
													+ " . .....Skipping");
								continue;
							}
							
							// Try to found a new 
							boolean isTargetNode = isNearestBridgeLink(
									curNode, curBridgePort, endNode,
									endBridgePort);
							if (!isTargetNode)
									continue;

							int endIfindex = endNode.getIfindex(endBridgePort);
							if (endIfindex == -1) {
								log()
										.warn("run: got invalid ifindex o designated bridge port "
												+ endBridgePort);
								break;
							}

							if (log().isDebugEnabled())
								log().debug("run: backbone port found for node "
										+ curNodeId + ". Adding backbone port "
										+ curBridgePort + " to bridge");

							curNode.addBackBoneBridgePorts(curBridgePort);
							bridgeNodes.put(curNodeId, curNode);

							if (log().isDebugEnabled())
								log().debug("run: backbone port found for node "
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
			if (log().isDebugEnabled())
				log()
						.debug("run: try to found  not ethernet links on Router nodes");

			ite = routerNodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				int curNodeId = curNode.getNodeId();
				String curIpAddr = curNode.getSnmpPrimaryIpAddr();
				if (log().isDebugEnabled())
					log().debug("run: parsing router nodeid " + curNodeId
							+ " ip address " + curIpAddr);

				Iterator<RouterInterface> sub_ite = curNode.getRouteInterfaces().iterator();
				if (log().isDebugEnabled())
					log().debug("run: parsing "
							+ curNode.getRouteInterfaces().size()
							+ " Route Interface. ");

				while (sub_ite.hasNext()) {
					RouterInterface routeIface = sub_ite
							.next();

					if (log().isDebugEnabled()) {
						log().debug("run: parsing RouterInterface: " + routeIface.toString());
					}

					if (routeIface.getMetric() == -1) {
						if (log().isInfoEnabled())
							log()
									.info("run: Router interface has invalid metric "
											+ routeIface.getMetric()
											+ ". Skipping");
						continue;
					}

					if (forceIpRouteDiscoveryOnEtherNet) {
						if (log().isInfoEnabled())
							log().info("run: force ip route discovery not getting SnmpIfType");
							
					} else {
						int snmpiftype = routeIface.getSnmpiftype();
						if (log().isInfoEnabled())
							log().info("run: force ip route discovery getting SnmpIfType: " + snmpiftype);
						
						if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
							if (log().isInfoEnabled())
								log()
										.info("run: Ethernet interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == SNMP_IF_TYPE_PROP_VIRTUAL) {
							if (log().isInfoEnabled())
								log()
										.info("run: PropVirtual interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == SNMP_IF_TYPE_L2_VLAN) {
							if (log().isInfoEnabled())
								log()
										.info("run: Layer2 Vlan interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == SNMP_IF_TYPE_L3_VLAN) {
							if (log().isInfoEnabled())
								log()
										.info("run: Layer3 Vlan interface for nodeid. Skipping ");
							continue;
						} else if (snmpiftype == -1) {
							if (log().isInfoEnabled())
								log().info("store: interface has unknown snmpiftype "
										+ snmpiftype + " . Skipping ");
							continue;
						} 
					}
					
					InetAddress nexthop = routeIface.getNextHop();

					if (nexthop.getHostAddress().equals("0.0.0.0")) {
						if (log().isInfoEnabled())
							log()
									.info("run: nexthop address is broadcast address "
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
						if (log().isInfoEnabled())
							log()
									.info("run: nexthop address is localhost address "
											+ nexthop.getHostAddress()
											+ " . Skipping ");
						continue;
					}

					if (!Linkd.getInstance().isInterfaceInPackage(nexthop.getHostAddress(), getPackageName())) {
						if (log().isInfoEnabled())
							log()
									.info("run: nexthop address is not in package "
											+ nexthop.getHostAddress() + "/"+getPackageName() 
											+ " . Skipping ");
						continue;
					}

					
					int nextHopNodeid = routeIface.getNextHopNodeid();

					if (nextHopNodeid == -1) {
						if (log().isInfoEnabled())
							log()
									.info("run: no node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " , skipping ");
						continue;
					}

					if (nextHopNodeid == curNodeId) {
						if (log().isDebugEnabled())
							log()
									.debug("run: node id found for ip next hop address "
											+ nexthop.getHostAddress()
											+ " is itself, skipping ");
						continue;
					}

					int ifindex = routeIface.getIfindex();
					
					if (ifindex == 0) {
						if (log().isInfoEnabled())
							log()
									.info("run: route interface has ifindex "
											+ ifindex + " . trying to get ifindex from nextHopNet: " 
											+ routeIface.getNextHopNet());
						ifindex = getIfIndexFromRouter(curNode, routeIface.getNextHopNet());
						if (ifindex == -1 ) {
							if (log().isDebugEnabled())
								log()
										.debug("run: found not correct ifindex "
												+ ifindex + " skipping.");
							continue;
						} else {
							if (log().isDebugEnabled())
								log()
										.debug("run: found correct ifindex "
												+ ifindex + " .");
						}
						
					}
					if (log().isDebugEnabled())
						log()
								.debug("run: saving route link");
					
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
			if (log().isDebugEnabled())
				log()
						.debug("getBridgePortOnEndBridge: parsing bridge identifier "
								+ curBridgeIdentifier);
			
			if (endBridge.hasMacAddress(curBridgeIdentifier)) {
			    for (final Integer p : endBridge.getBridgePortsFromMac(curBridgeIdentifier)) {
			        port = p;
					if (endBridge.isBackBoneBridgePort(port)) {
						if (log().isDebugEnabled())
							log()
									.debug("getBridgePortOnEndBridge: found backbone bridge port "
											+ port
											+ " .... Skipping");
						continue;
					}
					if (port == -1) {
						if (log().isDebugEnabled())
							log()
									.debug("run: no port found on bridge nodeid "
											+ endBridge.getNodeId()
											+ " for node bridge identifiers nodeid "
											+ startBridge.getNodeId()
											+ " . .....Skipping");
						continue;
					}
					if (log().isDebugEnabled())
						log()
								.debug("run: using mac address table found bridge port "
										+ port
										+ " on node "
										+ endBridge.getNodeId());
					return port;
				}
					
			} else {
				if (log().isDebugEnabled())
					log()
							.debug("run: bridge identifier not found on node "
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
			if (log().isDebugEnabled())
				log().debug("parseCdpLinkOn: node/backbone bridge port "
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
				log()
						.warn("parseCdpLinkOn: link cannot be saved. Skipping");
			return false;
		}


		return true;
	}

	private boolean parseCdpLinkOn(LinkableNode node1,int ifindex1,
								LinkableNode node2,int ifindex2) {
		
		int bridgeport1 = node1.getBridgePort(ifindex1);

		if (node1.isBackBoneBridgePort(bridgeport1)) {
			if (log().isDebugEnabled())
				log().debug("parseCdpLinkOn: backbone bridge port "
						+ bridgeport1
						+ " already parsed. Skipping");
			return false;
		}
		
		int bridgeport2 = node2
				.getBridgePort(ifindex2);
		if (node2.isBackBoneBridgePort(bridgeport2)) {
			if (log().isDebugEnabled())
				log().debug("parseCdpLinkOn: backbone bridge port "
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

			if (log().isDebugEnabled())
				log().debug("parseCdpLinkOn: Adding node on links.");
			addLinks(getMacsOnBridgeLink(node1,
					bridgeport1, node2, bridgeport2),node1.getNodeId(),ifindex1);
		} else {
			if (log().isDebugEnabled())
				log()
						.debug("parseCdpLinkOn: link found not on nearest. Skipping");
			return false;
		}
		return true;
	} 	

	private void addNodetoNodeLink(NodeToNodeLink nnlink) {
		if (nnlink == null)
		{
				log().warn("addNodetoNodeLink: node link is null.");
				return;
		}
		if (!links.isEmpty()) {
			Iterator<NodeToNodeLink> ite = links.iterator();
			while (ite.hasNext()) {
				NodeToNodeLink curNnLink = ite.next();
				if (curNnLink.equals(nnlink)) {
					if (log().isInfoEnabled())
						log().info("addNodetoNodeLink: link " + nnlink.toString() + " exists, not adding");
					return;
				}
			}
		}
		
		if (log().isDebugEnabled())
			log().debug("addNodetoNodeLink: adding link " + nnlink.toString());
		links.add(nnlink);
	}

	private void addLinks(Set<String> macs,int nodeid,int ifindex) { 
		if (macs == null || macs.isEmpty()) {
			if (log().isDebugEnabled())
				log()
						.debug("addLinks: mac's list on link is empty.");
		} else {
			Iterator<String> mac_ite = macs.iterator();

			while (mac_ite.hasNext()) {
				String curMacAddress = mac_ite
						.next();
				if (macsParsed.contains(curMacAddress)) {
					log()
							.warn("addLinks: mac address "
									+ curMacAddress
									+ " just found on other bridge port! Skipping...");
					continue;
				}
				
				if (macsExcluded.contains(curMacAddress)) {
					log()
							.warn("addLinks: mac address "
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
					if (log().isDebugEnabled())
						log().debug("addLinks: not find nodeid for ethernet mac address "
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

		if(log().isDebugEnabled())
			log().debug("parseBridgeNodes: searching bridge port for bridge identifier not yet already found. Iterating on bridge nodes.");
		
		List<LinkableNode> bridgenodeschanged = new ArrayList<LinkableNode>();
		Iterator<LinkableNode> ite = bridgeNodes.values().iterator();
		while (ite.hasNext()) {
			LinkableNode curNode = ite.next();
			if(log().isDebugEnabled())
				log().debug("parseBridgeNodes: parsing bridge: " + curNode.getNodeId() + "/" + curNode.getSnmpPrimaryIpAddr());

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
				log().error(
						"parseBridgeNodes: Failed to load snmp parameter from snmp configuration file " +
				e);
				return;
			}
			
			String community = agentConfig.getReadCommunity();
			
			Iterator<String> mac_ite = macs.iterator();
			
			while (mac_ite.hasNext()) {
				String mac = mac_ite.next();
				if(log().isDebugEnabled())
					log().debug("parseBridgeNodes: parsing mac: " + mac);

				if (className != null && (className.equals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable") 
						|| className.equals("org.opennms.netmgt.linkd.snmp.IntelVlanTable"))){
					Iterator<Vlan> vlan_ite = curNode.getVlans().iterator();
					while (vlan_ite.hasNext()) {
						Vlan vlan = vlan_ite.next();
						if (vlan.getVlanStatus() != VlanCollectorEntry.VLAN_STATUS_OPERATIONAL || vlan.getVlanType() != VlanCollectorEntry.VLAN_TYPE_ETHERNET) {
							if (log().isDebugEnabled()) log().debug("parseBridgeNodes: skipping vlan: " + vlan.getVlanName());
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
		if (log().isInfoEnabled())
			log().info("collectMacAddress: finding entry in bridge forwarding table for mac on node: " + mac + "/" + node.getNodeId());
		int bridgeport = coll.getBridgePort();
		if (bridgeport > 0 && coll.getBridgePortStatus() == QueryManager.SNMP_DOT1D_FDB_STATUS_LEARNED) {
			node.addMacAddress(bridgeport, mac, Integer.toString(vlan));
			if (log().isInfoEnabled())
				log().info("collectMacAddress: found mac on bridge port: " + bridgeport);
		} else {
			bridgeport = coll.getQBridgePort();
			if (bridgeport > 0 && coll.getQBridgePortStatus() == QueryManager.SNMP_DOT1D_FDB_STATUS_LEARNED) 
				node.addMacAddress(bridgeport, mac, Integer.toString(vlan));
			if (log().isInfoEnabled())
				log().info("collectMacAddress: found mac on bridge port: " + bridgeport);
			else if (log().isInfoEnabled())
				log().info("collectMacAddress: mac not found: " + bridgeport);
		}
		return node;
	}
	
	private List<String> getNotAlreadyFoundMacsOnNode(LinkableNode node){
		if (log().isDebugEnabled())
			log().debug("Searching Not Yet Found Bridge Identifier Occurrence on Node: " + node.getNodeId());
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
				if (log().isDebugEnabled())
					log().debug("Found a node/Bridge Identifier " + curNode.getNodeId() + "/"+ curMac +" that was not found in bridge forwarding table for bridge node: " + node.getNodeId());
				macs.add(curMac);
			}
		}

		if (log().isDebugEnabled())
			log().debug("Searching Not Yet Found Mac Address Occurrence on Node: " + node.getNodeId());

		Iterator<String> mac_ite = macToAtinterface.keySet().iterator();
		while (mac_ite.hasNext()) {
			String curMac = mac_ite.next();
			if (node.hasMacAddress(curMac)) continue;
			if (macs.contains(curMac)) continue;
			if (log().isDebugEnabled())
				log().debug("Found a Mac Address " + curMac +" that was not found in bridge forwarding table for bridge node: " + node.getNodeId());
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
