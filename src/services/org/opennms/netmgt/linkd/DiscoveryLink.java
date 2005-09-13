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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dBasePortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dStpGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dStpPortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dTpFdbTableEntry;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTableEntry;
import org.opennms.netmgt.linkd.snmp.IpRouteTableEntry;

import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.protocols.snmp.SnmpBadConversionException;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpOctetString;

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

	private static final String SQL_GET_COUNT_SNMPINTERFACE = "SELECT count(*) FROM snmpinterface WHERE nodeid = ? AND snmpphysaddr = ?";

	private static final String SQL_GET_COUNT_ATINTERFACE = "SELECT count(*) FROM atinterface WHERE nodeid = ? AND atphysaddr = ?";

	private static final String SQL_GET_NODEIDIFINDEX_SNMPINTERFACE = "SELECT node.nodeid, snmpinterface.snmpifindex FROM node LEFT JOIN snmpinterface ON node.nodeid = snmpinterface.nodeid WHERE snmpphysaddr = ? AND node.nodetype = 'A'";

	private static final String SQL_GET_IFINDEX_SNMPINTERFACE = "SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND snmpphysaddr = ? ";

	private static final String SQL_GET_NODEID_ATINTERFACE = "SELECT node.nodeid FROM node LEFT JOIN atinterface ON node.nodeid = atinterface.nodeid WHERE atphysaddr = ? AND node.nodetype = 'A'";

	private static final String SQL_GET_SNMPIFTYPE = "SELECT snmpiftype FROM snmpinterface WHERE nodeid = ? AND snmpifindex = ?";

	private static final String SQL_GET_NODEID = "SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?";

	private static final String SQL_UPDATE_DATAINTERFACE = "UPDATE datalinkinterface set status = 'N'  WHERE lastpolltime < ? AND status = 'A'";

	private static final int SNMP_IF_TYPE_ETHERNET = 6;

	/**
	 * The status of the info in FDB table entry The meanings of the value is
	 * other(1): none of the following. This would include the case where some
	 * other MIB object (not the corresponding instance of dot1dTpFdbPort, nor
	 * an entry in the dot1dStaticTable) is being used to determine if and how
	 * frames addressed to the value of the corresponding instance of
	 * dot1dTpFdbAddress are being forwarded.
	 */
	private static final int SNMP_DOT1D_FDB_STATUS_OTHER = 1;

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: invalid(2) : this entry is not longer valid
	 * (e.g., it was learned but has since aged-out), but has not yet been
	 * flushed from the table.
	 */
	private static final int SNMP_DOT1D_FDB_STATUS_INVALID = 2;

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: learned(3) : the value of the corresponding
	 * instance of dot1dTpFdbPort was learned, and is being used.
	 */
	private static final int SNMP_DOT1D_FDB_STATUS_LEARNED = 3;

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: self(4) : the value of the corresponding
	 * instance of dot1dTpFdbAddress represents one of the bridge's addresses.
	 * The corresponding instance of dot1dTpFdbPort indicates which of the
	 * bridge's ports has this address.
	 */
	private static final int SNMP_DOT1D_FDB_STATUS_SELF = 4;

	/**
	 * mgmt(5) : the value of the corresponding instance of dot1dTpFdbAddress is
	 * also the value of an existing instance of dot1dStaticAddress.
	 */
	private static final int SNMP_DOT1D_FDB_STATUS_MGMT = 5;

	/**
	 * The list of linkable nodes fron whom getting link info.
	 */
	private LinkableSnmpNode[] m_snmplinknodes;

	/**
	 * The HashMap of bridge nodes with some other useful info.
	 */
	private HashMap m_helper = new HashMap();

	/**
	 * The scheduler object
	 *
	 */
	
	private Scheduler m_scheduler;

	/**
	 * The interval
	 * default value 6 min
	 */
	
	private long interval = 360000;

	/**
	 * The initial sleep time 
	 * default value 5 min
	 */
	
	private long initial_sleep_time = 300000;

    private class Helper {

		/**
		 * the node identifier
		 */
		int nodeid;

		/**
		 * variable useful to test if bridgescale factor has to be calculated
		 */
		boolean hasbridgescalefactor = false;

		/**
		 * the bridge scale factor, necessary to calculate correctly the
		 * designated port/index
		 */
		int bridgescalefactor = -1;

		/**
		 * the list o bridge port that are backbone bridge ports ou that are
		 * link between switches
		 */
		List BackBoneBridgePorts = new java.util.ArrayList();

		Helper(int nodeid) {
			this.nodeid = nodeid;
		}

		/**
		 * @return Returns the backBoneBridgePorts.
		 */
		public List getBackBoneBridgePorts() {
			return BackBoneBridgePorts;
		}

		/**
		 * @param backBoneBridgePorts
		 *            The backBoneBridgePorts to set.
		 */
		public void setBackBoneBridgePorts(List backBoneBridgePorts) {
			BackBoneBridgePorts = backBoneBridgePorts;
		}

		public boolean isBackBoneBridgePort(int bridgeport) {
			return BackBoneBridgePorts.contains((Integer) new Integer(
					bridgeport));
		}

		public void addBackBoneBridgePorts(int bridgeport) {
			if (BackBoneBridgePorts.contains((Integer) new Integer(bridgeport)))
				return;
			BackBoneBridgePorts.add((Integer) new Integer(bridgeport));
		}

		public boolean hasBridgeScaleFactorCalculated() {
			return hasbridgescalefactor;
		}

		/**
		 * @return Returns the bridgescalefactor.
		 */
		public int getBridgeScaleFactor() {
			return bridgescalefactor;
		}

		/**
		 * @param bridgescalefactor
		 *            The bridgescalefactor to set.
		 */
		public void setBridgeScaleFactor(int bridgescalefactor) {
			if (bridgescalefactor > 0) {
				this.bridgescalefactor = bridgescalefactor;
				this.hasbridgescalefactor = true;
			}
		}

		/**
		 * @return Returns the nodeid.
		 */
		public int getNodeid() {
			return nodeid;
		}
	}

	/**
	 * Constructs a new DiscoveryLink object . The discovery does not occur
	 * until the <code>run</code> method is invoked.
	 */
	DiscoveryLink() {
		super();
	}

	/**
	 * <p>
	 * Performs the discovery for the Linkable Nodes
	 * 
	 * <p>
	 * No synchronization is performed, so if this is used in a separate thread
	 * context synchornization must be added.
	 * </p>
	 *  
	 */

	public void run() {
		Category log = ThreadCategory.getInstance(getClass());
		Connection dbConn = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());

		LinkableSnmpNode[] all_snmplinknodes = Linkd.getInstance().getSnmpLinkableNodes();
		List activenode = new ArrayList();
		for (int i = 0; i < all_snmplinknodes.length; i++) {
			LinkableSnmpNode curNode = all_snmplinknodes[i];

			if (curNode == null) {
				if (log.isDebugEnabled())
					log
							.debug("run: null linkable node found for iterator "
									+ i
									);
					continue;
				}

			SnmpCollection snmpcoll = curNode.getSnmpCollection();
			if (snmpcoll == null) {
				if (log.isDebugEnabled())
				log
						.debug("run: no snmp collection found for node "
								+ all_snmplinknodes[i].getNodeId()
								+ " and snmpprimary ip address "
								+ all_snmplinknodes[i].getSnmpPrimaryIpAddr()
								);
				continue;
			}

			if (snmpcoll.isSuspendCollection()){
				if (log.isDebugEnabled())
					log
							.debug("run: snmp collection suspended for node "
									+ all_snmplinknodes[i].getNodeId()
									+ " and snmpprimary ip address "
									+ all_snmplinknodes[i].getSnmpPrimaryIpAddr()
									);
					continue;
			}

			if (!snmpcoll.isRunned() ){
				if (log.isDebugEnabled())
					log
							.debug("run: not yet completed snmp collection for node "
									+ all_snmplinknodes[i].getNodeId()
									+ " and snmpprimary ip address "
									+ all_snmplinknodes[i].getSnmpPrimaryIpAddr()
									);
					continue;
			}
			activenode.add(all_snmplinknodes[i]); 
		}
		
		m_snmplinknodes = (LinkableSnmpNode[]) activenode.toArray(new LinkableSnmpNode[0]);
		
		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("run: finding links among nodes");
			}
			store(dbConn, now);
		} catch (SQLException sqlE) {
			log
					.fatal(
							"SQL Exception while syncing node object with database information.",
							sqlE);
			throw new UndeclaredThrowableException(sqlE);
		} catch (Throwable t) {
			log
					.fatal(
							"Unknown error while syncing node object with database information.",
							t);
			throw new UndeclaredThrowableException(t);
		} finally {
			try {
				if (dbConn != null) {
					dbConn.close();
				}
			} catch (Exception e) {
			}
		}
		reschedule();
		
		

	}

	private void store(Connection dbConn, Timestamp now) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Iterator ite = null;

		// first of all get backbone links between switches using STP info

		for (int i = 0; i < m_snmplinknodes.length; i++) {
			LinkableSnmpNode curNode = m_snmplinknodes[i];

			if (curNode == null) {
				if (log.isDebugEnabled())
					log
							.debug("store: null linkable node found for iterator "
									+ i
									);
					continue;
				}

			SnmpCollection snmpcoll = curNode.getSnmpCollection();
			if (snmpcoll == null) {
			if (log.isDebugEnabled())
				log
						.debug("store: no snmp collection found for node "
								+ m_snmplinknodes[i].getNodeId()
								+ " and snmpprimary ip address "
								+ m_snmplinknodes[i].getSnmpPrimaryIpAddr()
								);
				continue;
			}

			if (log.isDebugEnabled())
				log
						.debug("store: parsing element "
								+ i
								+ " with correspondant nodeid "
								+ m_snmplinknodes[i].getNodeId()
								+ " and snmpprimary ip address "
								+ m_snmplinknodes[i].getSnmpPrimaryIpAddr()
								+ " with vlan table: " 
								+ m_snmplinknodes[i].getSnmpCollection().hasVlanTable()
								);

			if (!m_snmplinknodes[i].getSnmpCollection().hasVlanTable()) {
				if (log.isDebugEnabled())
					log
							.debug("store: no vlan table found fo node "
									+ m_snmplinknodes[i].getNodeId()
									+ " and snmpprimary ip address "
									+ m_snmplinknodes[i].getSnmpPrimaryIpAddr()
									+ " skipping bridge stuff " 
									);
				continue;
			}
			Helper curBridgeHelper = null;
			if (m_helper
					.containsKey(new Integer(m_snmplinknodes[i].getNodeId()))) {
				curBridgeHelper = (Helper) m_helper.get(new Integer(
						m_snmplinknodes[i].getNodeId()));
			} else {
				curBridgeHelper = new Helper(m_snmplinknodes[i].getNodeId());
			}
			ite = m_snmplinknodes[i].getSnmpCollection().getVlanSnmpList()
					.iterator();
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();

				if (snmpVlanColl.hasDot1dStpPortTable()) {
					Iterator sub_ite = snmpVlanColl.getDot1dStpPortTable()
							.getEntries().iterator();
					while (sub_ite.hasNext()) {
						Dot1dStpPortTableEntry dot1dstpptentry = (Dot1dStpPortTableEntry) sub_ite
								.next();

						// the bridge port number
						SnmpInt32 stpbridgeport = (SnmpInt32) dot1dstpptentry
								.get(Dot1dStpPortTableEntry.STP_PORT);

						// if port is a backbone port continue
						if (curBridgeHelper.isBackBoneBridgePort(stpbridgeport
								.getValue()))
							continue;

						// the stp port designated port on designated bridge
						SnmpOctetString stpportdesignatedport = (SnmpOctetString) dot1dstpptentry
								.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_PORT);

						// need to be changed in String Type
						StringBuffer sbuf = new StringBuffer();
				        byte[] bytes = stpportdesignatedport.getString();
				        for (int subi = 0; subi < bytes.length; subi++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[subi] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[subi] & 0xf));
				        }
				        String stpPortDesignatedPort = sbuf.toString().trim();
						
						// the stp bridge address of designated stp bridge
						SnmpOctetString stpportdesignatedbridge = (SnmpOctetString) dot1dstpptentry
								.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_BRIDGE);

						// need to be changed in String Type
						sbuf = new StringBuffer();
				        bytes = stpportdesignatedbridge.getString();
				        for (int subi = 0; subi < bytes.length; subi++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[subi] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[subi] & 0xf));
				        }
				        String stpPortDesignatedBridge = sbuf.toString().trim();
						
						if (log.isDebugEnabled())
							log
									.debug("store: bridging info for nodeid "
											+ m_snmplinknodes[i].getNodeId()
											+ " and bridge port "
											+ stpbridgeport.getValue() 
											+ " stp designated bridge "
											+ stpPortDesignatedBridge
											+ " stp designated port " 
											+ stpPortDesignatedPort
											);
				        
						// first check on Linkable Snmp Node if designated
						// bridge is it self
						if (isStpPortDesignatedBridgeOfLinkableSnmpNode(
								m_snmplinknodes[i], stpPortDesignatedBridge)) {
							if (curBridgeHelper
									.hasBridgeScaleFactorCalculated())
								continue;
							curBridgeHelper
									.setBridgeScaleFactor(calculateBridgeScaleFactor(
											stpbridgeport.getValue(),
											stpPortDesignatedPort));

							// also verify if bridge designated mac addess is
							// somewhere in snmpinterface
							// this is necessary becouse some switch has
							// odd SNMP STP implementation

						} else if (isStpDesignatedBridgeNodeIdInSnmpInterface(
								dbConn, m_snmplinknodes[i].getNodeId(),
								stpPortDesignatedBridge)) {
							if (curBridgeHelper
									.hasBridgeScaleFactorCalculated())
								continue;
							curBridgeHelper
									.setBridgeScaleFactor(calculateBridgeScaleFactor(
											stpbridgeport.getValue(),
											stpPortDesignatedPort));

						} else if (isStpPortDesignatedBridgeNodeIdInAtInterface(
								dbConn, m_snmplinknodes[i].getNodeId(),
								stpPortDesignatedBridge)) {
							if (curBridgeHelper
									.hasBridgeScaleFactorCalculated())
								continue;
							curBridgeHelper
									.setBridgeScaleFactor(calculateBridgeScaleFactor(
											stpbridgeport.getValue(),
											stpPortDesignatedPort));
						} else {
							// this is a backbone port so adding to helper class
							curBridgeHelper
									.addBackBoneBridgePorts(stpbridgeport
											.getValue());
							m_helper.put(new Integer(m_snmplinknodes[i]
									.getNodeId()), curBridgeHelper);

							// the designated bridge is a different bridge
							//try to find address on linkable snmp node stp
							// node info
							int designatednodeid = -1;
							int designatedifindex = -1;
							LinkableSnmpNode designatednode = null;
							designatednode = getLinkableNodeFromStpPortDesignatedBridge(stpPortDesignatedBridge);

							if (designatednode == null) {
								if (log.isDebugEnabled())
									log
											.debug("store: no node found for stp bridge address "
													+ stpPortDesignatedBridge
													+ " using linkable snmp node info ");
								// try to find address on snmp interface info
								int[] values = getNodeidFromStpDesignatedBridgeInSnmpInterface(
										dbConn, stpPortDesignatedBridge);
								if (values != null) {
									designatednodeid = values[0];
									designatedifindex = values[1];
								}
								if (designatednodeid == -1) {
									if (log.isDebugEnabled())
										log
												.debug("store: no nodeid found for stp bridge address "
														+ stpPortDesignatedBridge
														+ " on snmp interface");

									// try to find address on at interface info
									designatednodeid = getNodeidFromStpDesignatedBridgeInAtInterface(
											dbConn, stpPortDesignatedBridge);
									if (designatednodeid == -1) {
										if (log.isDebugEnabled())
											log
													.debug("store: no nodeid found for stp bridge address "
															+ stpPortDesignatedBridge
															+ " on atinterface - nothing to save to db");
										continue; // no saving info if no nodeid
										// found
									}
								}
								designatednode = getLinkableNodeFromNodeId(designatednodeid);
							}
							// set the bridge scale factor unles in not yet
							// calculated
							if (!curBridgeHelper
									.hasBridgeScaleFactorCalculated()) {
								curBridgeHelper
										.setBridgeScaleFactor(calculateBridgeScaleFactor(
												dbConn, m_snmplinknodes[i]));
							}
							// then try to see if designated bridge is linkable
							// snmp node
							if (designatednode != null) {
								designatednodeid = designatednode.getNodeId();
								Helper designatedBridgeHelper = null;
								if (m_helper.containsKey(new Integer(
										designatednode.getNodeId()))) {
									designatedBridgeHelper = (Helper) m_helper
											.get(new Integer(designatednode
													.getNodeId()));
								} else {
									designatedBridgeHelper = new Helper(
											designatednode.getNodeId());
								}
								int designatedbridgeport = Integer.parseInt(
										stpPortDesignatedPort, 16)
										- curBridgeHelper
												.getBridgeScaleFactor();
								designatedBridgeHelper
										.addBackBoneBridgePorts(designatedbridgeport);
								designatedifindex = getIfindexFromBridgePort(
										designatednode, designatedbridgeport);
								m_helper.put(new Integer(designatedBridgeHelper
										.getNodeid()), designatedBridgeHelper);
							}
							int ifindex = getIfindexFromBridgePort(
									m_snmplinknodes[i], stpbridgeport
											.getValue());
							// writing to db using class
							// DbDAtaLinkInterfaceEntry

							DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry
									.get(dbConn,
											m_snmplinknodes[i].getNodeId(),
											ifindex);
							if (dbentry == null) {
								// Create a new entry
								if (log.isDebugEnabled())
									log
											.debug("store DataLinkInterface: Data Link interface with nodeid "
													+ m_snmplinknodes[i]
															.getNodeId()
													+ " and ifindex"
													+ ifindex
													+ " not in database, creating new interface object.");
								dbentry = DbDataLinkInterfaceEntry
										.create(m_snmplinknodes[i].getNodeId(),
												ifindex);
							}
							dbentry.updateNodeParentId(designatednodeid);
							dbentry.updateParentIfIndex(designatedifindex);
							dbentry
									.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
							dbentry.set_lastpolltime(now);

							dbentry.store(dbConn);
							// write reverse info

							dbentry = DbDataLinkInterfaceEntry.get(dbConn,
									designatednodeid, designatedifindex);
							if (dbentry == null) {
								// Create a new entry
								if (log.isDebugEnabled())
									log
											.debug("store DataLinkInterface: Data Link interface with nodeid "
													+ designatednodeid
													+ " and ifindex"
													+ designatedifindex
													+ " not in database, creating new interface object.");
								dbentry = DbDataLinkInterfaceEntry.create(
										designatednodeid, designatedifindex);
							}
							dbentry.updateNodeParentId(m_snmplinknodes[i]
									.getNodeId());
							dbentry.updateParentIfIndex(ifindex);
							dbentry
									.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
							dbentry.set_lastpolltime(now);

							dbentry.store(dbConn);

						}
					}
				}
			}
		}

		// second find mac address on ports
		for (int i = 0; i < m_snmplinknodes.length; i++) {
			if (!m_snmplinknodes[i].getSnmpCollection().hasVlanTable())
				continue;

			ite = m_snmplinknodes[i].getSnmpCollection().getVlanSnmpList()
					.iterator();
			Helper curBridgeHelper = null;
			if (m_helper
					.containsKey(new Integer(m_snmplinknodes[i].getNodeId()))) {
				curBridgeHelper = (Helper) m_helper.get(new Integer(
						m_snmplinknodes[i].getNodeId()));
			} else {
				curBridgeHelper = new Helper(m_snmplinknodes[i].getNodeId());
			}
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();
				String vlanindex = snmpVlanColl.getVlanIndex();
				int rootpathcost = 0;
				if (snmpVlanColl.hasDot1dStp()) {
					Dot1dStpGroup dod1stp = (Dot1dStpGroup) snmpVlanColl
							.getDot1dStp();
					SnmpInt32 stprootcost = (SnmpInt32) dod1stp
							.get(Dot1dStpGroup.STP_ROOT_COST);
					rootpathcost = stprootcost.getValue();
				}
				if (snmpVlanColl.hasDot1dTpFdbTable()) {
					Iterator subite = snmpVlanColl.getDot1dFdbTable()
							.getEntries().iterator();
					while (subite.hasNext()) {
						Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
								.next();
						SnmpInt32 fdbport = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_PORT);
						if (fdbport.getValue() == 0)
							continue;
						if (curBridgeHelper.isBackBoneBridgePort(fdbport
								.getValue()))
							continue;
						SnmpInt32 fdbstatus = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_STATUS);
						if (fdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_INVALID)
							continue;
						if (fdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_SELF)
							continue;
						SnmpOctetString macaddress = (SnmpOctetString) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_ADDRESS);
						
						// need to be changed in String Type
						StringBuffer sbuf = new StringBuffer();
				        byte[] bytes = macaddress.getString();
				        for (int subi = 0; subi < bytes.length; subi++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[subi] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[subi] & 0xf));
				        }
				        String macAddress = sbuf.toString().trim();

						
						// now find on which switch ports where learned current mac
				        // address, verify root path cost
						// if some switch has higher than mac address is owned by it
						if (isMacAddressOwnedByOtherBridgePort(macAddress, i,
								vlanindex, rootpathcost))
							continue;
						int nodeid = -1;
						int ifindex = -1;
						LinkableSnmpNode node = null;
						node = getLinkableNodeFromMacAddress(macAddress);

						if (node == null) {
							int[] values = getNodeidFromMacAddressInSnmpInterface(
									dbConn, macAddress);
							if (values != null) {
								nodeid = values[0];
								ifindex = values[1];
							}
							if (nodeid == -1) {
								if (log.isDebugEnabled())
									log
											.debug("store: no nodeid found for mac address "
													+ macaddress.toString()
													+ "on snmp interface");

								// try to find address on at interface info
								nodeid = getNodeidFromMacAddressInAtInterface(
										dbConn, macAddress);
								if (nodeid == -1) {
									if (log.isDebugEnabled())
										log
												.debug("store: no nodeid found for mac address "
														+ macaddress.toString()
														+ "on atinterface - nothing to save to db");
									continue; // no saving info if no nodeid
									// found
								}
							}
						} else if (node.getNodeId() != m_snmplinknodes[i]
								.getNodeId()) {
							nodeid = node.getNodeId();
							ifindex = getIfindexFromBridgePort(node,
									getBridgePortFromMacAddress(node,
											macAddress));
							if (ifindex == -1)
								ifindex = getIfIndexFromSnmpInterface(dbConn,
										nodeid, macAddress);
						} else {
							continue;
						}
						int parentifindex = getIfindexFromBridgePort(
								m_snmplinknodes[i], fdbport.getValue());
						// writing to db using class DbDAtaLinkInterfaceEntry

						DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry
								.get(dbConn, nodeid, ifindex);
						if (dbentry == null) {
							// Create a new entry
							if (log.isDebugEnabled())
								log
										.debug("store DataLinkInterface: Data Link interface with nodeid "
												+ nodeid
												+ " and ifindex"
												+ ifindex
												+ " not in database, creating new interface object.");
							dbentry = DbDataLinkInterfaceEntry.create(nodeid,
									ifindex);
						}
						dbentry.updateNodeParentId(m_snmplinknodes[i]
								.getNodeId());
						dbentry.updateParentIfIndex(parentifindex);
						dbentry
								.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
						dbentry.set_lastpolltime(now);

						dbentry.store(dbConn);

					}
				}
			}
		}

		// third find inter router links,
		// this part could have several special function to get inter router
		// links, but at the moment we worked much on switches.
		// In future we can try to extend this part.
		
		for (int i = 0; i < m_snmplinknodes.length; i++) {
			if (!m_snmplinknodes[i].getSnmpCollection().hasRouteTable()) {
				if (log.isDebugEnabled())
					log
							.debug("store DataLinkInterface: Data Link interface with nodeid "
									+ m_snmplinknodes[i].getNodeId()
									+ " has not routing table info, skipping"
							);
				continue;
			}
			
			ite = m_snmplinknodes[i].getSnmpCollection().getIpRouteTable()
					.getEntries().iterator();
			while (ite.hasNext()) {
				IpRouteTableEntry ent = (IpRouteTableEntry) ite.next();

				SnmpInt32 metric = (SnmpInt32) ent
				.get(IpRouteTableEntry.IP_ROUTE_METRIC1);

				if (metric.getValue() == -1 ) {
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: Data Link interface with nodeid "
										+ m_snmplinknodes[i].getNodeId()
										+ " has metric "
										+ metric
										+ " ip route not used, skipping "
								);
					continue;
				}
				
				SnmpInt32 ifindex = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_IFINDEX);

				if (ifindex.getValue() == 0 || ifindex == null) {
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: Data Link interface with nodeid "
										+ m_snmplinknodes[i].getNodeId()
										+ " has ifindex "
										+ ifindex
										+ " skipping "
								);
					continue;
				}
				int snmpiftype = getSnmpIfType(dbConn, m_snmplinknodes[i]
						.getNodeId(), ifindex.getValue());

				// no processing ethernet type
				if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: Data Link interface with nodeid "
										+ m_snmplinknodes[i].getNodeId()
										+ " with ifindex "
										+ ifindex
										+ " is Ethernet type, skipping "
								);
					continue;
				}
				// no processing unknown type
				if (snmpiftype == -1 ) {
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: Data Link interface with nodeid "
										+ m_snmplinknodes[i].getNodeId()
										+ " with ifindex "
										+ ifindex
										+ " is of unknown type, skipping "
								);
					continue;
				}
	
				SnmpIPAddress nexthop = (SnmpIPAddress) ent
						.get(IpRouteTableEntry.IP_ROUTE_NXTHOP);
				if (nexthop.toString().equals("0.0.0.0"))
					continue; // TODO this case should be further analized 
				if (nexthop.toString().equals("127.0.0.1"))
					continue;

				int nodeparentid = -1;

				// scrivi scrivi............
				try {
					nodeparentid = getNodeidFromIp(dbConn, nexthop
							.convertToIpAddress());
				} catch (SnmpBadConversionException e) {
					log.warn("Cannot convert ipaddress" + nexthop.toString());
				}

				if (nodeparentid == -1) {
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: no node id found for ip next hop address "
										+ nexthop.toString()
										+ " , skipping "
								);
					continue;
				}
				if (nodeparentid == m_snmplinknodes[i].getNodeId()) {
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: node id found for ip next hop address "
										+ nexthop.toString()
										+ " is itself, skipping "
								);
					continue;
			}
				//find ifindex on parent node
				int parentifindex = -1;
				LinkableSnmpNode nodeparent = getLinkableNodeFromNodeId(nodeparentid);
				if (nodeparent != null) {
					parentifindex = getIfIndexFromParentRouter(dbConn,nodeparent,
							m_snmplinknodes[i].getNodeId());
				}
				// store bilateral
				DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry
						.get(dbConn, m_snmplinknodes[i].getNodeId(), ifindex
								.getValue());
				if (dbentry == null) {
					// Create a new entry
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: Data Link interface with nodeid "
										+ m_snmplinknodes[i].getNodeId()
										+ " and ifindex"
										+ ifindex
										+ " not in database, creating new interface object.");
					dbentry = DbDataLinkInterfaceEntry.create(
							m_snmplinknodes[i].getNodeId(), ifindex.getValue());
				}
				dbentry.updateNodeParentId(nodeparentid);
				dbentry.updateParentIfIndex(parentifindex);
				dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
				dbentry.set_lastpolltime(now);

				dbentry.store(dbConn);
				// write reverse info

				dbentry = DbDataLinkInterfaceEntry.get(dbConn,
						nodeparentid, parentifindex);
				if (dbentry == null) {
					// Create a new entry
					if (log.isDebugEnabled())
						log
								.debug("store DataLinkInterface: Data Link interface with nodeid "
										+ nodeparentid
										+ " and ifindex"
										+ parentifindex
										+ " not in database, creating new interface object.");
					dbentry = DbDataLinkInterfaceEntry.create(nodeparentid,
							parentifindex);
				}
				dbentry.updateNodeParentId(m_snmplinknodes[i].getNodeId());
				dbentry.updateParentIfIndex(ifindex.getValue());
				dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
				dbentry.set_lastpolltime(now);

				dbentry.store(dbConn);

			}
		}
		
		// finally update status to N for all entries that have not yet been updated by this run
		
		stmt = dbConn.prepareStatement(SQL_UPDATE_DATAINTERFACE);
		stmt.setTimestamp(1, now);

		int i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update datalinkinterface: updated to NOT ACTIVE status"
							+ i + " rows ");
		
		stmt.close();
// finish store
	}

	private int getIfIndexFromParentRouter(Connection dbConn, LinkableSnmpNode parentnode,
			int nodeid) throws SQLException {
		Category log = ThreadCategory.getInstance(getClass());

		if (!parentnode.getSnmpCollection().hasRouteTable())
			return -1;
		Iterator ite = parentnode.getSnmpCollection().getIpRouteTable()
				.getEntries().iterator();
		while (ite.hasNext()) {
			IpRouteTableEntry ent = (IpRouteTableEntry) ite.next();

			SnmpInt32 metric = (SnmpInt32) ent
			.get(IpRouteTableEntry.IP_ROUTE_METRIC1);

			if (metric.getValue() == -1 ) {
				if (log.isDebugEnabled())
					log
							.debug("getIfIndexFromParentRouter: Data Link interface with nodeid "
									+ parentnode.getNodeId()
									+ " has metric "
									+ metric
									+ " ip route not used, skipping "
							);
				continue;
			}

			SnmpInt32 ifindex = (SnmpInt32) ent
					.get(IpRouteTableEntry.IP_ROUTE_IFINDEX);

			if (ifindex.getValue() == 0 || ifindex == null)
				continue;

			int snmpiftype = getSnmpIfType(dbConn, parentnode
					.getNodeId(), ifindex.getValue());

			// no processing ethernet type or unknown
			if (snmpiftype == SNMP_IF_TYPE_ETHERNET || snmpiftype == -1)
				continue;

			SnmpIPAddress nexthop = (SnmpIPAddress) ent
					.get(IpRouteTableEntry.IP_ROUTE_NXTHOP);
			if (nexthop.toString().equals("0.0.0.0"))
				continue; // this case must be analized in detail
			if (nexthop.toString().equals("127.0.0.1"))
				continue;

			int curnodeid = -1;

			try {
				curnodeid = getNodeidFromIp(dbConn, nexthop
						.convertToIpAddress());
			} catch (SnmpBadConversionException e) {
				log.warn("Cannot convert ipaddress " + nexthop.toString());
			}
			if (curnodeid == nodeid) return ifindex.getValue();
		}
		return -1;
	}

	private InetAddress getIpAddrFromMac(String macAddr)
			throws SnmpBadConversionException {
		
		for (int i = 0; i < m_snmplinknodes.length; i++) {
			if (m_snmplinknodes[i].getSnmpCollection().hasIpNetToMediaTable()) {
				Iterator ite = m_snmplinknodes[i].getSnmpCollection()
						.getIpNetToMediaTable().getEntries().iterator();
				while (ite.hasNext()) {
					int nodeid = -1;
					IpNetToMediaTableEntry ent = (IpNetToMediaTableEntry) ite
							.next();
					SnmpOctetString physaddr = (SnmpOctetString) ent
							.get(IpNetToMediaTableEntry.INTM_PHYSADDR);
					StringBuffer sbuf = new StringBuffer();
			        byte[] bytes = physaddr.getString();
			        for (int subi = 0; subi < bytes.length; subi++) {
			        	sbuf.append(Integer.toHexString(((int) bytes[subi] >> 4) & 0xf));
			            sbuf.append(Integer.toHexString((int) bytes[subi] & 0xf));
			        }
			        String physAddress = sbuf.toString().trim();

					if (physAddress.equals(macAddr)) {
						SnmpIPAddress ipaddress = (SnmpIPAddress) ent
								.get(IpNetToMediaTableEntry.INTM_NETADDR);
						return ipaddress.convertToIpAddress();
					}
				}
			}
		}
		return null;
	}

	private boolean isMacAddressOwnedByOtherBridgePort(String macAddress,
			int inode, String vlanindex, int rootpathcost) {
		for (int i = 0; i < m_snmplinknodes.length; i++) {
			if (!m_snmplinknodes[i].getSnmpCollection().hasVlanTable())
				continue;
			if (i == inode)
				continue;
			Iterator ite = m_snmplinknodes[i].getSnmpCollection()
					.getVlanSnmpList().iterator();
			Helper curBridgeHelper = null;
			if (m_helper
					.containsKey(new Integer(m_snmplinknodes[i].getNodeId()))) {
				curBridgeHelper = (Helper) m_helper.get(new Integer(
						m_snmplinknodes[i].getNodeId()));
			} else {
				curBridgeHelper = new Helper(m_snmplinknodes[i].getNodeId());
			}
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();
				String curvlanindex = snmpVlanColl.getVlanIndex();
				if (!curvlanindex.equals(vlanindex))
					continue;
				int currootpathcost = 0;
				if (snmpVlanColl.hasDot1dStp()) {
					Dot1dStpGroup dod1stp = (Dot1dStpGroup) snmpVlanColl
							.getDot1dStp();
					SnmpInt32 curstprootcost = (SnmpInt32) dod1stp
							.get(Dot1dStpGroup.STP_ROOT_COST);
					currootpathcost = curstprootcost.getValue();
				}
				// next collection if root cost is less than previus
				if (currootpathcost < rootpathcost)
					continue;
				if (snmpVlanColl.hasDot1dTpFdbTable()) {
					Iterator subite = snmpVlanColl.getDot1dFdbTable()
							.getEntries().iterator();
					while (subite.hasNext()) {
						Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
								.next();
						SnmpOctetString curmacaddress = (SnmpOctetString) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_ADDRESS);
						// need to be changed in String Type
						StringBuffer sbuf = new StringBuffer();
				        byte[] bytes = curmacaddress.getString();
				        for (int subi = 0; subi < bytes.length; subi++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[subi] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[subi] & 0xf));
				        }
				        String curMacAddress = sbuf.toString().trim();

						if (!macAddress.equals(
								curMacAddress))
							continue;
						SnmpInt32 curfdbport = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_PORT);
						if (curfdbport.getValue() == 0)
							continue;
						if (curBridgeHelper.isBackBoneBridgePort(curfdbport
								.getValue()))
							continue;
						SnmpInt32 curfdbstatus = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_STATUS);
						if (curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_INVALID)
							continue;
						if (curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_MGMT)
							continue;
						if (curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_LEARNED)
							return true;
						if (curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_SELF)
							return true;
						if (curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_OTHER)
							return true;
					}
				}
			}
		}
		return false;
	}

	private int getNodeidFromIp(Connection dbConn, InetAddress ipaddr)
			throws SQLException {
		Category log = ThreadCategory.getInstance(getClass());

		int nodeid = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEID);
		stmt.setString(1, ipaddr.getHostAddress());

		if (log.isDebugEnabled())
			log.debug("getNodeidFromIp: executing query " + stmt.toString());

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log.debug("getNodeidFromIp: no entries found in ipinterface");
			return -1;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the node id
		//
		nodeid = rs.getInt(ndx++);
		if (rs.wasNull())
			nodeid = -1;

		if (log.isDebugEnabled())
			log.debug("getNodeidFromIp: found nodeid " + nodeid
					+ " for ipaddress " + ipaddr.getHostAddress());

		stmt.close();

		return nodeid;
	}

	private int getSnmpIfType(Connection dbConn, int nodeid, int ifindex)
			throws SQLException {
		Category log = ThreadCategory.getInstance(getClass());

		int snmpiftype = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_SNMPIFTYPE);
		stmt.setInt(1, nodeid);
		stmt.setInt(2, ifindex);

		if (log.isDebugEnabled())
			log.debug("getSnmpIfTypeEthernet: executing query "
					+ SQL_GET_SNMPIFTYPE);

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getSnmpIfTypeEthernet: no entries found in snmpinterface");
			return -1;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the node id
		//
		snmpiftype = rs.getInt(ndx++);
		if (rs.wasNull())
			snmpiftype = -1;

		if (log.isDebugEnabled())
			log.debug("IsSnmpIfTypeEthernet: found snmpiftype in snmpinterface"
					+ snmpiftype + " for nodeid, ifindex" + nodeid + " , "
					+ ifindex);

		stmt.close();

		return snmpiftype;

	}

	private boolean isStpPortDesignatedBridgeOfLinkableSnmpNode(
			LinkableSnmpNode node, String stpPortDesignatedBridge) {

		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled())
			log
					.debug("isStpPortDesignatedBridgeOfLinkableNOde: getting info for: "
							+ node.getNodeId()
							+ "/"
							+ stpPortDesignatedBridge);
        
		Iterator ite = node.getSnmpCollection().getVlanSnmpList().iterator();
		while (ite.hasNext()) {
			SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite.next();

			if (snmpVlanColl.hasDot1dBase()) {
				Dot1dBaseGroup dod1db = (Dot1dBaseGroup) snmpVlanColl
						.getDot1dBase();
				SnmpOctetString basebridgeaddr = (SnmpOctetString) dod1db
						.get(Dot1dBaseGroup.BASE_BRIDGE_ADDRESS);
				
				// decode correctly snmp octet string
                
				StringBuffer sbuf = new StringBuffer();
                byte[] bytes = basebridgeaddr.getString();
                for (int i = 0; i < bytes.length; i++) {
                	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
                    sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
                }
                String baseBridgeAddress = sbuf.toString().trim();

				if (baseBridgeAddress.equals(
						stpPortDesignatedBridge.substring(4)))
					return true;
			}

			if (snmpVlanColl.hasDot1dTpFdbTable()) {
				Iterator subite = snmpVlanColl.getDot1dFdbTable().getEntries()
						.iterator();
				while (subite.hasNext()) {
					Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
							.next();
					SnmpOctetString curmacaddress = (SnmpOctetString) dot1dfdbentry
							.get(Dot1dTpFdbTableEntry.FDB_ADDRESS);
					StringBuffer sbuf = new StringBuffer();
	                byte[] bytes = curmacaddress.getString();
	                for (int i = 0; i < bytes.length; i++) {
	                	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
	                    sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
	                }
	                String curMacAddress = sbuf.toString().trim();
					
					SnmpInt32 curfdbstatus = (SnmpInt32) dot1dfdbentry
							.get(Dot1dTpFdbTableEntry.FDB_STATUS);
					if (curMacAddress.equals(
							stpPortDesignatedBridge.substring(4))
							&& curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_SELF) {
						return true;
					}
				}
			}

		}
		return false;
	}

	private boolean isStpDesignatedBridgeNodeIdInSnmpInterface(
			Connection dbConn, int nodeid,
			String stpPortDesignatedBridge) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_COUNT_SNMPINTERFACE);
		stmt.setInt(1, nodeid);
		stmt.setString(2, stpPortDesignatedBridge.substring(4));

		ResultSet rs = stmt.executeQuery();
		int count = 0;
		while (rs.next()) {
			count = rs.getInt(1);
		}

		if (log.isDebugEnabled())
			log
					.debug("isStpPortDesignatedBridgeOfSnmpInterfaceNodeId: count rows for interface "
							+ nodeid
							+ "/"
							+ stpPortDesignatedBridge.toString()
							+ ": found " + count);

		stmt.close();

		if (count == 1)
			return true;
		else
			return false;
	}

	private int[] getNodeidFromStpDesignatedBridgeInSnmpInterface(
			Connection dbConn,String stpBridgeAddress)
			throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		int[] values = null;
		int nodeid = -1;
		int ifindex = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEIDIFINDEX_SNMPINTERFACE);
		stmt.setString(1, stpBridgeAddress.substring(4));
		if (log.isDebugEnabled())
			log
					.debug("getNodeidFromStpDesignatedBridgeInSnmpInterface: executing query"
							+ stmt.toString());

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getNodeidFromStpDesignatedBridgeInSnmpInterface: no entries found in snmpinterface");
			return values;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the node id
		//
		nodeid = rs.getInt(ndx++);
		if (rs.wasNull())
			nodeid = -1;

		ifindex = rs.getInt(ndx++);
		if (rs.wasNull())
			ifindex = -1;

		if (log.isDebugEnabled())
			log
					.debug("isStpPortDesignatedBridgeOfSnmpInterfaceNodeId: found nodeid, ifindex in snmpinterface"
							+ nodeid
							+ ifindex
							+ " for stp bridge address "
							+ stpBridgeAddress);

		stmt.close();

		values[0] = nodeid;
		values[1] = ifindex;
		return values;
	}

	private int[] getNodeidFromMacAddressInSnmpInterface(Connection dbConn,
			String macAddress) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		int[] values = new int[2];
		values[0] = -1;
		values[1] = -1;
		
		int nodeid = -1;
		int ifindex = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEIDIFINDEX_SNMPINTERFACE);
		if (log.isDebugEnabled())
			log.debug("getNodeidFromMacAddressInSnmpInterface: preparing execute query"
					+ SQL_GET_NODEIDIFINDEX_SNMPINTERFACE);

		stmt.setString(1, macAddress);

		if (log.isDebugEnabled())
			log.debug("getNodeidFromMacAddressInSnmpInterface: executing query"
					+ stmt.toString());

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getNodeidFromMacAddressInSnmpInterface: no entries found in snmpinterface");
			return values;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the node id
		//
		nodeid = rs.getInt(ndx++);
		if (rs.wasNull())
			nodeid = -1;

		ifindex = rs.getInt(ndx++);
		if (rs.wasNull())
			ifindex = -1;

		if (log.isDebugEnabled())
			log
					.debug("getNodeidFromMacAddressInSnmpInterface: found nodeid, ifindex in snmpinterface "
							+ nodeid + ", "
							+ ifindex
							+ " for mac address "
							+ macAddress);

		stmt.close();

		values[0] = nodeid;
		values[1] = ifindex;
		return values;
	}

	private int getIfIndexFromSnmpInterface(Connection dbConn, int nodeid,
			String macAddress) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		int ifindex = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_IFINDEX_SNMPINTERFACE);
		stmt.setInt(1, nodeid);
		stmt.setString(2, macAddress);
		if (log.isDebugEnabled())
			log.debug("getIfIndexFromSnmpInterface: executing query"
					+ SQL_GET_IFINDEX_SNMPINTERFACE);

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getIfIndexFromSnmpInterface: no entries found in snmpinterface");
			return -1;
		}

		// extract the values.
		//
		int ndx = 1;

		ifindex = rs.getInt(ndx++);
		if (rs.wasNull())
			ifindex = -1;

		if (log.isDebugEnabled())
			log
					.debug("getIfIndexFromSnmpInterface: found ifindex in snmpinterface"
							+ ifindex
							+ " for nodeid/mac address "
							+ nodeid
							+ macAddress);

		stmt.close();

		return ifindex;
	}

	private boolean isStpPortDesignatedBridgeNodeIdInAtInterface(
			Connection dbConn, int nodeid,
			String stpPortDesignatedBridge) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_COUNT_ATINTERFACE);
		stmt.setInt(1, nodeid);
		stmt.setString(2, stpPortDesignatedBridge.substring(4));

		ResultSet rs = stmt.executeQuery();
		int count = 0;
		while (rs.next()) {
			count = rs.getInt(1);
		}

		if (log.isDebugEnabled())
			log
					.debug("isStpPortDesignatedBridgeOfAtInterfaceNodeId: count rows for interface "
							+ nodeid
							+ "/"
							+ stpPortDesignatedBridge
							+ ": found " + count);

		stmt.close();

		if (count == 1)
			return true;
		else
			return false;
	}

	private int getNodeidFromStpDesignatedBridgeInAtInterface(
			Connection dbConn, String stpBridgeAddress)
			throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		int nodeid = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEID_ATINTERFACE);
		stmt.setString(1, stpBridgeAddress.substring(4));
		if (log.isDebugEnabled())
			log
					.debug("getNodeidFromStpDesignatedBridgeInAtInterface: executing query"
							+ SQL_GET_NODEID_ATINTERFACE);

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getNodeidFromStpDesignatedBridgeInAtInterface: no entries found in atinterface");
			return nodeid;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the node id
		//
		nodeid = rs.getInt(ndx++);
		if (rs.wasNull())
			nodeid = -1;

		if (log.isDebugEnabled())
			log
					.debug("isStpPortDesignatedBridgeOfAtInterfaceNodeId: found nodeid in atinterface"
							+ nodeid
							+ " for stp bridge address "
							+ stpBridgeAddress);

		stmt.close();

		return nodeid;
	}

	private int getNodeidFromMacAddressInAtInterface(Connection dbConn,
			String macAddress) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		int nodeid = -1;
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEID_ATINTERFACE);
		stmt.setString(1, macAddress);
		if (log.isDebugEnabled())
			log.debug("getNodeidFromMacAddressInAtInterface: executing query"
					+ stmt.toString());

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getNodeidFromMacAddressInAtInterface: no entries found in atinterface");
			return nodeid;
		}

		// extract the values.
		//
		int ndx = 1;

		// get the node id
		//
		nodeid = rs.getInt(ndx++);
		if (rs.wasNull())
			nodeid = -1;

		if (log.isDebugEnabled())
			log
					.debug("getNodeidFromMacAddressInAtInterface: found nodeid in atinterface"
							+ nodeid
							+ " for mac address "
							+ macAddress);

		stmt.close();

		return nodeid;
	}

	/**
	 * 
	 * @param stpportdesignatedbridge
	 * @return Linkablr Snmp Node if found else null
	 */
	private LinkableSnmpNode getLinkableNodeFromStpPortDesignatedBridge(
			String stpPortDesignatedBridge) {

		Category log = ThreadCategory.getInstance(getClass());

		for (int j = 0; j < m_snmplinknodes.length; j++) {
			if (log.isDebugEnabled())
				log
						.debug("getLinkableNodeFromStpPortDesignatedBridge: cursor "
								+ j
								+ " with correspondant nodeid "
								+ m_snmplinknodes[j].getNodeId()
								+ " and snmpprimary ip address "
								+ m_snmplinknodes[j].getSnmpPrimaryIpAddr()
								+ " snmp collection " 
								+ m_snmplinknodes[j].getSnmpCollection()
								+ " vlan table " 
								+ m_snmplinknodes[j].getSnmpCollection().hasVlanTable()
								);

			if (!m_snmplinknodes[j].getSnmpCollection().hasVlanTable()) {
				continue;
				
			}
			Iterator ite = m_snmplinknodes[j].getSnmpCollection()
					.getVlanSnmpList().iterator();
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();
				String vlanindex = snmpVlanColl.getVlanIndex();

				if (snmpVlanColl.hasDot1dBase()) {
					Dot1dBaseGroup dod1db = (Dot1dBaseGroup) snmpVlanColl
							.getDot1dBase();
					SnmpOctetString basebridgeaddr = (SnmpOctetString) dod1db
							.get(Dot1dBaseGroup.BASE_BRIDGE_ADDRESS);
					
					// cure snmp octet String
					StringBuffer sbuf = new StringBuffer();
			        byte[] bytes = basebridgeaddr.getString();
			        for (int i = 0; i < bytes.length; i++) {
			        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
			            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
			        }
			        String baseBridgeAddr = sbuf.toString().trim();

					
					if (baseBridgeAddr.equals(
							stpPortDesignatedBridge.substring(4)))
						return m_snmplinknodes[j];
				}

				if (snmpVlanColl.hasDot1dTpFdbTable()) {
					Iterator subite = snmpVlanColl.getDot1dFdbTable()
							.getEntries().iterator();
					while (subite.hasNext()) {
						Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
								.next();
						SnmpOctetString curmacaddress = (SnmpOctetString) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_ADDRESS);

						// cure snmp octet String
						StringBuffer sbuf = new StringBuffer();
				        byte[] bytes = curmacaddress.getString();
				        for (int i = 0; i < bytes.length; i++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
				        }
				        String curMacAddress = sbuf.toString().trim();

						SnmpInt32 curfdbstatus = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_STATUS);

						if (curMacAddress
								.equals(stpPortDesignatedBridge.substring(4))
								&& curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_SELF) {
							return m_snmplinknodes[j];
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param macaddr
	 * @return Linkable Snmp Node if found else null
	 */
	private LinkableSnmpNode getLinkableNodeFromMacAddress(
			String macAddress) {
		for (int j = 0; j < m_snmplinknodes.length; j++) {
			if (!m_snmplinknodes[j].getSnmpCollection().hasVlanTable())
				continue;
			Iterator ite = m_snmplinknodes[j].getSnmpCollection()
					.getVlanSnmpList().iterator();
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();
				String vlanindex = snmpVlanColl.getVlanIndex();

				if (snmpVlanColl.hasDot1dBase()) {
					Dot1dBaseGroup dod1db = (Dot1dBaseGroup) snmpVlanColl
							.getDot1dBase();
					SnmpOctetString basebridgeaddr = (SnmpOctetString) dod1db
							.get(Dot1dBaseGroup.BASE_BRIDGE_ADDRESS);

					StringBuffer sbuf = new StringBuffer();
			        byte[] bytes = basebridgeaddr.getString();
			        for (int i = 0; i < bytes.length; i++) {
			        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
			            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
			        }
			        String baseBridgeAddr = sbuf.toString().trim();

					if (baseBridgeAddr.equals(macAddress))
						return m_snmplinknodes[j];
				}

				if (snmpVlanColl.hasDot1dTpFdbTable()) {
					Iterator subite = snmpVlanColl.getDot1dFdbTable()
							.getEntries().iterator();
					while (subite.hasNext()) {
						Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
								.next();
						SnmpOctetString curmacaddress = (SnmpOctetString) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_ADDRESS);

						// cure snmp octet String
						StringBuffer sbuf = new StringBuffer();
				        byte[] bytes = curmacaddress.getString();
				        for (int i = 0; i < bytes.length; i++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
				        }
				        String curMacAddress = sbuf.toString().trim();

						SnmpInt32 curfdbstatus = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_STATUS);
						
						if (macAddress.equals(
								curMacAddress)
								&& curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_SELF) {
							return m_snmplinknodes[j];
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param macaddr
	 * @return Linkable Snmp Node if found else null
	 */
	private int getBridgePortFromMacAddress(LinkableSnmpNode node,
			String macAddress) {
		if (node.getSnmpCollection().hasVlanTable()) {
			Iterator ite = node.getSnmpCollection().getVlanSnmpList()
					.iterator();
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();
				if (snmpVlanColl.hasDot1dTpFdbTable()) {
					Iterator subite = snmpVlanColl.getDot1dFdbTable()
							.getEntries().iterator();
					while (subite.hasNext()) {
						Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
								.next();
						SnmpOctetString curmacaddress = (SnmpOctetString) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_ADDRESS);
						
						StringBuffer sbuf = new StringBuffer();
				        byte[] bytes = curmacaddress.getString();
				        for (int i = 0; i < bytes.length; i++) {
				        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
				            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
				        }
				        String curMacAddress = sbuf.toString().trim();

						SnmpInt32 curfdbstatus = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_STATUS);
						SnmpInt32 curfdbport = (SnmpInt32) dot1dfdbentry
								.get(Dot1dTpFdbTableEntry.FDB_PORT);

						if (macAddress.equals(
								curMacAddress)
								&& curfdbstatus.getValue() == SNMP_DOT1D_FDB_STATUS_SELF) {
							return curfdbport.getValue();
						}
					}
				}
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param nodeid
	 * @return LinkableSnmpNode or null if not found
	 */

	private LinkableSnmpNode getLinkableNodeFromNodeId(int nodeid) {

		for (int i = 0; i < m_snmplinknodes.length; i++) {
			if (nodeid == m_snmplinknodes[i].getNodeId())
				return m_snmplinknodes[i];
		}
		LinkableSnmpNode lsn = null;
		return lsn;
	}

	private int getIfindexFromBridgePort(LinkableSnmpNode node, int bridgeport) {

		if (!node.getSnmpCollection().hasVlanTable())
			return -1;
		Iterator ite = node.getSnmpCollection().getVlanSnmpList().iterator();
		while (ite.hasNext()) {
			SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite.next();
			if (snmpVlanColl.hasDot1dBasePortTable()) {
				Iterator sub_ite = snmpVlanColl.getDot1dBasePortTable()
						.getEntries().iterator();
				while (sub_ite.hasNext()) {
					Dot1dBasePortTableEntry dot1dbaseptentry = (Dot1dBasePortTableEntry) sub_ite
							.next();
					SnmpInt32 bport = (SnmpInt32) dot1dbaseptentry
							.get(Dot1dBasePortTableEntry.BASE_PORT);
					if (bridgeport == bport.getValue()) {
						SnmpInt32 ifindex = (SnmpInt32) dot1dbaseptentry
								.get(Dot1dBasePortTableEntry.BASE_IFINDEX);
						return ifindex.getValue();
					}
				}
			}
		}
		return -1;
	}


	private int calculateBridgeScaleFactor(int stpport,
			String stpPortDesignatedPort) {
		return (Integer.parseInt(stpPortDesignatedPort, 16) - stpport);
	}

	private int calculateBridgeScaleFactor(Connection dbConn,
			LinkableSnmpNode node) throws SQLException {
		if (!node.getSnmpCollection().hasVlanTable())
			return 0;
		Iterator ite = node.getSnmpCollection().getVlanSnmpList().iterator();
		while (ite.hasNext()) {
			SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite.next();
			if (snmpVlanColl.hasDot1dStpPortTable()) {
				Iterator sub_ite = snmpVlanColl.getDot1dStpPortTable()
						.getEntries().iterator();
				while (sub_ite.hasNext()) {
					Dot1dStpPortTableEntry dot1dstpptentry = (Dot1dStpPortTableEntry) sub_ite
							.next();
					SnmpOctetString stpportdesignatedbridge = (SnmpOctetString) dot1dstpptentry
							.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_BRIDGE);
					StringBuffer sbuf = new StringBuffer();
					byte[] bytes = stpportdesignatedbridge.getString();

			        for (int i = 0; i < bytes.length; i++) {
			        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
			            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
			        }
			        String stpPortDesignatedBridge = sbuf.toString().trim();

					SnmpInt32 stpport = (SnmpInt32) dot1dstpptentry
							.get(Dot1dStpPortTableEntry.STP_PORT);
					
					SnmpOctetString stpportdesignatedport = (SnmpOctetString) dot1dstpptentry
							.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_PORT);

					sbuf = new StringBuffer();
			        bytes = stpportdesignatedport.getString();
			        for (int i = 0; i < bytes.length; i++) {
			        	sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
			            sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
			        }
			        String stpPortDesignatedPort = sbuf.toString().trim();

			        if (isStpPortDesignatedBridgeOfLinkableSnmpNode(node,
							stpPortDesignatedBridge)
							|| isStpDesignatedBridgeNodeIdInSnmpInterface(
									dbConn, node.getNodeId(),
									stpPortDesignatedBridge)
							|| isStpPortDesignatedBridgeNodeIdInAtInterface(
									dbConn, node.getNodeId(),
									stpPortDesignatedBridge)) {
						return (Integer.parseInt(stpportdesignatedport
								.toString(), 16) - stpport.getValue());
					}
				}
			}
		}
		return 0;
	}

	public Scheduler getScheduler() {
		return m_scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}

    /**
     * 
     */
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException("Cannot schedule a service whose scheduler is set to null");
        
        m_scheduler.schedule(interval+initial_sleep_time,this);
    }

    /**
     * 
     */
    private void reschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException("Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(interval,this);
    }

	/**
	 * @return Returns the interval.
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @param interval The interval to set.
	 */
	public void setInterval(long interval) {
		this.interval = interval;
	}

	/**
	 * @return Returns the initial_sleep_time.
	 */
	public long getInitialSleepTime() {
		return initial_sleep_time;
	}

	/**
	 * @param initial_sleep_time The initial_sleep_timeto set.
	 */
	public void setInitialSleepTime(long initial_sleep_time) {
		this.initial_sleep_time = initial_sleep_time;
	}


	public boolean isReady() {
		return true;
	}

}