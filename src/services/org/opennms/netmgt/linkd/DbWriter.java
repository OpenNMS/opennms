/*
 * Created on 8-lug-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.opennms.netmgt.linkd;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.DatabaseConnectionFactory;

import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dBasePortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dStpGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dStpPortTableEntry;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTableEntry;
import org.opennms.netmgt.linkd.snmp.IpRouteTableEntry;

import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpOctetString;

/**
 * @author antonio
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DbWriter implements Runnable {

	static final char ACTION_UPDATE = 'N';

	static final char ACTION_DELETE = 'D';

	static final char ACTION_STORE = 'S';

	LinkableSnmpNode m_snmpnode;
	
	int m_nodeId;

	private static final String SQL_GET_NODEID = "SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?";

	private static final String SQL_UPDATE_ATINTERFACE = "UPDATE atinterface set status = 'N'  WHERE sourcenodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_IPROUTEINTERFACE = "UPDATE iprouteinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_STPNODE = "UPDATE stpnode set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_STPINTERFACE = "UPDATE stpinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_ATINTERFACE_STATUS = "UPDATE atinterface set status = ?  WHERE sourcenodeid = ? OR nodeid = ?";

	private static final String SQL_UPDATE_IPROUTEINTERFACE_STATUS = "UPDATE iprouteinterface set status = ? WHERE nodeid = ? ";

	private static final String SQL_UPDATE_STPNODE_STATUS = "UPDATE stpnode set status = ?  WHERE nodeid = ? ";

	private static final String SQL_UPDATE_STPINTERFACE_STATUS = "UPDATE stpinterface set status = ? WHERE nodeid = ? ";

	private static final String SQL_UPDATE_DATALINKINTERFACE_STATUS = "UPDATE datalinkinterface set status = ? WHERE nodeid = ? OR nodeparentid = ? ";

	private char action = ACTION_STORE;
	
	/**
	 * @param m_snmpcoll
	 */

	public DbWriter(LinkableSnmpNode m_snmpnode) {

		super();
		this.m_snmpnode = m_snmpnode;
		this.m_nodeId = m_snmpnode.getNodeId();

	}

	public DbWriter(LinkableSnmpNode m_snmpnode, char action) {

		super();
		this.m_snmpnode = m_snmpnode;
		this.m_nodeId = m_snmpnode.getNodeId();
		this.action = action;
	}

	public DbWriter(int nodeid, char action) {

		super();
		m_snmpnode = null;
		this.m_nodeId = nodeid;
		this.action = action;
	}

	public void run() {

		Category log = ThreadCategory.getInstance(getClass());
		Connection dbConn = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());

		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("init: Storing information into database");
			}
			if (action == ACTION_STORE) store(dbConn, now);
			else if (action == ACTION_UPDATE || action == ACTION_DELETE) update(dbConn, action);
			else log.fatal("Unknown action: " + action + " ;exiting");
		} catch (SQLException sqlE) {
			log.fatal("SQL Exception while syncing node object with database information.",sqlE);
			throw new UndeclaredThrowableException(sqlE);
		} catch (Throwable t) {
			log.fatal("Unknown error while syncing node object with database information.",t);
			throw new UndeclaredThrowableException(t);
		} finally {
			try {
				if (dbConn != null) {
					dbConn.close();
				}
			} catch (Exception e) {
			}
		}
	}

	private void store(Connection dbConn, Timestamp now) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Iterator ite = null;

		if (m_snmpnode.getSnmpCollection().hasIpNetToMediaTable()) {
			ite = m_snmpnode.getSnmpCollection().getIpNetToMediaTable().getEntries().iterator();
			while (ite.hasNext()) {
				int nodeid = -1;
				IpNetToMediaTableEntry ent = (IpNetToMediaTableEntry) ite
						.next();
				SnmpInt32 ifindex = (SnmpInt32) ent
						.get(IpNetToMediaTableEntry.INTM_INDEX);
				SnmpOctetString physaddr = (SnmpOctetString) ent
						.get(IpNetToMediaTableEntry.INTM_PHYSADDR);
				SnmpIPAddress ipaddress = (SnmpIPAddress) ent
						.get(IpNetToMediaTableEntry.INTM_NETADDR);
				try {
					stmt = dbConn.prepareStatement(SQL_GET_NODEID);
					stmt.setString(1, ipaddress.toString());

					rs = stmt.executeQuery();
					if (rs.getFetchSize() > 1) {
						if (log.isEnabledFor(Priority.WARN))
							log.warn("found " + rs.getFetchSize() + "nodeid for ipaddress "
									+ ipaddress + " go next row; database inconsistency.");
						continue;
					}
					if (!rs.next()) {
						rs.close();
						stmt.close();
						if (log.isEnabledFor(Priority.WARN))
							log.warn("no nodeid find for ipaddress "
									+ ipaddress + " go next row.");
						continue;
					}
					int ndx = 1;
					nodeid = rs.getInt(ndx++);
					if (rs.wasNull()) {
						if (log.isEnabledFor(Priority.WARN))
						log.warn("no nodeid find for ipaddress "
								+ ipaddress + " go next row.");
						continue;
					}
				} finally {
					if (rs != null)
						rs.close();
					if (stmt != null)
						stmt.close();
				}

				DbAtInterfaceEntry atInterfaceEntry = DbAtInterfaceEntry.get(
						dbConn, nodeid, ipaddress.toString());
				if (atInterfaceEntry == null) {
					// Create a new entry
					if (log.isDebugEnabled())
						log
								.debug("store AtInterface: At interface with nodeid "
										+ nodeid
										+ " not in database, creating new interface object.");
					atInterfaceEntry = DbAtInterfaceEntry.create(nodeid,
							ipaddress.toString());
				}
				// update object
				
                
				// physical address
                StringBuffer sbuf = new StringBuffer();
                String physAddr = "000000000000";
                
                if (physaddr != null) {
                    
                    byte[] bytes = physaddr.getString();
                    for (int i = 0; i < bytes.length; i++) {
                        sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
                        sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
                    }
                    
                    ;
                    
                    if (sbuf.toString().trim().length() == 12) {
                    	physAddr = sbuf.toString().trim();
                    }
                }

                atInterfaceEntry.updateAtPhysAddr(physAddr);
				
                atInterfaceEntry.updateSourceNodeId(m_snmpnode.getNodeId());
				atInterfaceEntry.updateIfIndex(ifindex.getValue());
				atInterfaceEntry.updateStatus(DbAtInterfaceEntry.STATUS_ACTIVE);
				atInterfaceEntry.set_lastpolltime(now);

				// store object in database
				atInterfaceEntry.store(dbConn);
			}
		}

		if (m_snmpnode.getSnmpCollection().hasRouteTable()) {
			ite = m_snmpnode.getSnmpCollection().getIpRouteTable().getEntries()
					.iterator();
			while (ite.hasNext()) {
				IpRouteTableEntry ent = (IpRouteTableEntry) ite.next();

				SnmpIPAddress routedest = (SnmpIPAddress) ent
						.get(IpRouteTableEntry.IP_ROUTE_DEST);
				SnmpIPAddress routemask = (SnmpIPAddress) ent
						.get(IpRouteTableEntry.IP_ROUTE_MASK);
				SnmpIPAddress nexthop = (SnmpIPAddress) ent
						.get(IpRouteTableEntry.IP_ROUTE_NXTHOP);
				SnmpInt32 ifindex = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_IFINDEX);
				SnmpInt32 routemetric1 = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_METRIC1);
				SnmpInt32 routemetric2 = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_METRIC2);
				SnmpInt32 routemetric3 = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_METRIC3);
				SnmpInt32 routemetric4 = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_METRIC4);
				SnmpInt32 routemetric5 = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_METRIC5);
				SnmpInt32 routetype = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_TYPE);
				SnmpInt32 routeproto = (SnmpInt32) ent
						.get(IpRouteTableEntry.IP_ROUTE_PROTO);

				DbIpRouteInterfaceEntry iprouteInterfaceEntry = DbIpRouteInterfaceEntry
						.get(dbConn, m_snmpnode.getNodeId(), routedest
								.toString());
				if (iprouteInterfaceEntry == null) {
					// Create a new entry
					if (log.isDebugEnabled())
						log
								.debug("store IpRouteInterface: Ip Route interface with nodeid "
										+ m_snmpnode.getNodeId()
										+ " and route destination "
										+ routedest
										+ " not in database, creating new interface object.");
					iprouteInterfaceEntry = DbIpRouteInterfaceEntry.create(
							m_snmpnode.getNodeId(), routedest.toString());
				}
				// update object
				iprouteInterfaceEntry.updateRouteMask(routemask.toString());
				iprouteInterfaceEntry.updateRouteNextHop(nexthop.toString());
				iprouteInterfaceEntry.updateIfIndex(ifindex.getValue());
				iprouteInterfaceEntry.updateRouteMetric1(routemetric1
						.getValue());
				iprouteInterfaceEntry.updateRouteMetric2(routemetric2
						.getValue());
				iprouteInterfaceEntry.updateRouteMetric3(routemetric3
						.getValue());
				iprouteInterfaceEntry.updateRouteMetric4(routemetric4
						.getValue());
				iprouteInterfaceEntry.updateRouteMetric5(routemetric5
						.getValue());
				iprouteInterfaceEntry.updateRouteType(routetype.getValue());
				iprouteInterfaceEntry.updateRouteProto(routeproto.getValue());
				iprouteInterfaceEntry
						.updateStatus(DbAtInterfaceEntry.STATUS_ACTIVE);
				iprouteInterfaceEntry.set_lastpolltime(now);

				// store object in database
				iprouteInterfaceEntry.store(dbConn);
			}
		}
		// STARTS loop on vlans

		if (m_snmpnode.getSnmpCollection().hasVlanTable()) {
			ite = m_snmpnode.getSnmpCollection().getVlanSnmpList().iterator();
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();

				String vlanindex = snmpVlanColl.getVlanIndex();
				Integer vlan = new Integer(vlanindex);
				String vlanname = m_snmpnode.getSnmpCollection()
						.getVlanName(vlanindex);

				if (snmpVlanColl.hasDot1dBase()) {

					Dot1dBaseGroup dod1db = (Dot1dBaseGroup) snmpVlanColl
							.getDot1dBase();
					SnmpOctetString basebridgeaddr = (SnmpOctetString) dod1db
							.get(Dot1dBaseGroup.BASE_BRIDGE_ADDRESS);
					SnmpInt32 basenumports = (SnmpInt32) dod1db
							.get(Dot1dBaseGroup.BASE_NUM_PORTS);
					SnmpInt32 bridgetype = (SnmpInt32) dod1db
							.get(Dot1dBaseGroup.BASE_NUM_TYPE);

					DbStpNodeEntry dbStpNodeEntry = DbStpNodeEntry.get(dbConn,
							m_snmpnode.getNodeId(), vlan.intValue());
					if (dbStpNodeEntry == null) {
						// Create a new entry
						if (log.isDebugEnabled())
							log
									.debug("store StpNode: bridge node with nodeid "
											+ m_snmpnode.getNodeId()
											+ " and vlan index "
											+ vlanindex
											+ " not in database, creating new interface object.");
						dbStpNodeEntry = DbStpNodeEntry.create(m_snmpnode
								.getNodeId(), vlan.intValue());
					}
					// update object

					// base bridge address 
					StringBuffer sbuf = new StringBuffer();
	                String baseBridgeAddress= "000000000000";
	                
	                if (basebridgeaddr != null) {
	                    
	                    byte[] bytes = basebridgeaddr.getString();
	                    for (int i = 0; i < bytes.length; i++) {
	                        sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
	                        sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
	                    }
	                    
	                    ;
	                    
	                    if (sbuf.toString().trim().length() == 12) {
	                    	baseBridgeAddress = sbuf.toString().trim();
	                    }
	                }	
					dbStpNodeEntry.updateBaseBridgeAddress(baseBridgeAddress);
					dbStpNodeEntry.updateBaseNumPorts(basenumports.getValue());
					dbStpNodeEntry.updateBaseType(bridgetype.getValue());
					dbStpNodeEntry.updateBaseVlanName(vlanname);

					if (snmpVlanColl.hasDot1dStp()) {
						Dot1dStpGroup dod1stp = (Dot1dStpGroup) snmpVlanColl
								.getDot1dStp();
						SnmpInt32 protospec = (SnmpInt32) dod1stp
								.get(Dot1dStpGroup.STP_PROTOCOL_SPEC);
						SnmpInt32 stppriority = (SnmpInt32) dod1stp
								.get(Dot1dStpGroup.STP_PRIORITY);
						SnmpOctetString stpdesignatedroot = (SnmpOctetString) dod1stp
								.get(Dot1dStpGroup.STP_DESIGNATED_ROOT);
						SnmpInt32 stprootcost = (SnmpInt32) dod1stp
								.get(Dot1dStpGroup.STP_ROOT_COST);
						SnmpInt32 stprootport = (SnmpInt32) dod1stp
								.get(Dot1dStpGroup.STP_ROOT_PORT);

						dbStpNodeEntry.updateStpProtocolSpecification(protospec
								.getValue());
						dbStpNodeEntry.updateStpPriority(stppriority.getValue());
						
						
						// designated root
						sbuf = new StringBuffer();
		                String stpDesignatedRoot= "0000000000000000";
		                
		                if (stpdesignatedroot != null) {
		                    
		                    byte[] bytes = stpdesignatedroot.getString();
		                    for (int i = 0; i < bytes.length; i++) {
		                        sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
		                        sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
		                    }
		                    
		                    ;
		                    
		                    if (sbuf.toString().trim().length() == 16) {
		                    	stpDesignatedRoot = sbuf.toString().trim();
		                    }
		                }	

						dbStpNodeEntry.updateStpDesignatedRoot(stpDesignatedRoot);
						dbStpNodeEntry.updateStpRootCost(stprootcost.getValue());
						dbStpNodeEntry.updateStpRootPort(stprootport.getValue());
					}
					// store object in database
					dbStpNodeEntry.updateStatus(DbStpNodeEntry.STATUS_ACTIVE);
					dbStpNodeEntry.set_lastpolltime(now);

					dbStpNodeEntry.store(dbConn);

					if (snmpVlanColl.hasDot1dBasePortTable()) {
						Iterator sub_ite = snmpVlanColl.getDot1dBasePortTable().getEntries().iterator();
						while (sub_ite.hasNext()) {
							Dot1dBasePortTableEntry dot1dbaseptentry = (Dot1dBasePortTableEntry) sub_ite
									.next();
							SnmpInt32 baseport = (SnmpInt32) dot1dbaseptentry
									.get(Dot1dBasePortTableEntry.BASE_PORT);
							SnmpInt32 ifindex = (SnmpInt32) dot1dbaseptentry
									.get(Dot1dBasePortTableEntry.BASE_IFINDEX);
							DbStpInterfaceEntry dbStpIntEntry = DbStpInterfaceEntry
									.get(dbConn, m_snmpnode.getNodeId(),
											baseport.getValue(), vlan
													.intValue());
							if (dbStpIntEntry == null) {
								// Create a new entry
								if (log.isDebugEnabled())
									log
											.debug("store StpInterface: bridge node with nodeid "
													+ m_snmpnode.getNodeId()
													+ " bridgeport number "
													+ baseport
													+ " and vlan index "
													+ vlanindex
													+ " not in database, creating new interface object.");
								dbStpIntEntry = DbStpInterfaceEntry.create(
										m_snmpnode.getNodeId(), baseport
												.getValue(), vlan.intValue());
							}
							dbStpIntEntry.updateIfIndex(ifindex.getValue());

							dbStpIntEntry
									.updateStatus(DbStpNodeEntry.STATUS_ACTIVE);
							dbStpIntEntry.set_lastpolltime(now);

							dbStpIntEntry.store(dbConn);

						}
					}

					if (snmpVlanColl.hasDot1dStpPortTable()) {
						Iterator sub_ite = snmpVlanColl.getDot1dStpPortTable().getEntries().iterator();
						while (sub_ite.hasNext()) {
							Dot1dStpPortTableEntry dot1dstpptentry = (Dot1dStpPortTableEntry) sub_ite
									.next();
							SnmpInt32 stpport = (SnmpInt32) dot1dstpptentry
									.get(Dot1dStpPortTableEntry.STP_PORT);
							DbStpInterfaceEntry dbStpIntEntry = DbStpInterfaceEntry
									.get(dbConn, m_snmpnode.getNodeId(),
											stpport.getValue(), vlan
													.intValue());
							if (dbStpIntEntry == null) {
								// Cannot create the object becouse must exists the dot1dbase
								// object!!!!!
								if (log.isEnabledFor(Priority.WARN))
									log
											.warn("store StpInterface: when storing STP info"
													+ " for bridge node with nodeid "
													+ m_snmpnode.getNodeId()
													+ " bridgeport number "
													+ stpport
													+ " and vlan index "
													+ vlanindex
													+ " info not found in database, ERROR skipping.....");
							} else {

								SnmpInt32 stpportstate = (SnmpInt32) dot1dstpptentry
										.get(Dot1dStpPortTableEntry.STP_PORT_STATE);
								SnmpInt32 stpportpathcost = (SnmpInt32) dot1dstpptentry
										.get(Dot1dStpPortTableEntry.STP_PORT_PATH_COST);
								SnmpOctetString stpportdesignatedbridge = (SnmpOctetString) dot1dstpptentry
										.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_BRIDGE);
								SnmpOctetString stpportdesignatedroot = (SnmpOctetString) dot1dstpptentry
								.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_ROOT);
								SnmpInt32 stpportdesignatedcost = (SnmpInt32) dot1dstpptentry
								.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_COST);
								SnmpOctetString stpportdesignatedport = (SnmpOctetString) dot1dstpptentry
								.get(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_PORT);
						
								dbStpIntEntry.updateStpPortState(stpportstate
										.getValue());
								dbStpIntEntry
										.updateStpPortPathCost(stpportpathcost
												.getValue());
								// designated root
								sbuf = new StringBuffer();
				                String stpBridge = "0000000000000000";
				                
				                if (stpportdesignatedbridge != null) {
				                    
				                    byte[] bytes = stpportdesignatedbridge.getString();
				                    for (int i = 0; i < bytes.length; i++) {
				                        sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
				                        sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
				                    }
				                    
				                    ;
				                    
				                    if (sbuf.toString().trim().length() == 16) {
				                    	stpBridge = sbuf.toString().trim();
				                    }
				                }	

								
								dbStpIntEntry
										.updateStpportDesignatedBridge(stpBridge);

								sbuf = new StringBuffer();
				                stpBridge = "0000000000000000";
				                
				                if (stpportdesignatedroot != null) {
				                    
				                    byte[] bytes = stpportdesignatedroot.getString();
				                    for (int i = 0; i < bytes.length; i++) {
				                        sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
				                        sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
				                    }
				                    
				                    ;
				                    
				                    if (sbuf.toString().trim().length() == 16) {
				                    	stpBridge = sbuf.toString().trim();
				                    }
				                }

				                dbStpIntEntry
										.updateStpportDesignatedRoot(stpBridge);
								dbStpIntEntry
										.updateStpPortDesignatedCost(stpportdesignatedcost
												.getValue());
								
								sbuf = new StringBuffer();
				                String stpDesignatedPort = "0000";
				                
				                if (stpportdesignatedport != null) {
				                    
				                    byte[] bytes = stpportdesignatedport.getString();
				                    for (int i = 0; i < bytes.length; i++) {
				                        sbuf.append(Integer.toHexString(((int) bytes[i] >> 4) & 0xf));
				                        sbuf.append(Integer.toHexString((int) bytes[i] & 0xf));
				                    }
				                    
				                    ;
				                    
				                    if (sbuf.toString().trim().length() == 16) {
				                    	stpDesignatedPort = sbuf.toString().trim();
				                    }
				                }
								dbStpIntEntry
										.updateStpportDesignatedPort(stpDesignatedPort);
								dbStpIntEntry
										.updateStatus(DbStpNodeEntry.STATUS_ACTIVE);
								dbStpIntEntry.set_lastpolltime(now);

								dbStpIntEntry.store(dbConn);

							}

						}
					}

				}
			}
		}
		
		int i = 0;
		stmt = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE);
		stmt.setInt(1,m_snmpnode.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update atinterface: updated to NOT ACTIVE status"
							+ i + " rows for nodeid"
							+ m_snmpnode.getNodeId() + ".");

		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE);
		stmt.setInt(1,m_snmpnode.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update iprouteinterface: updated to NOT ACTIVE status "
							+ i + " rows for nodeid "
							+ m_snmpnode.getNodeId() + ".");
		
		stmt.close();
		
		stmt = dbConn.prepareStatement(SQL_UPDATE_STPNODE);
		stmt.setInt(1,m_snmpnode.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update stpnode: updated to NOT ACTIVE status "
							+ i + " rows for nodeid "
							+ m_snmpnode.getNodeId() + ".");
		
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE);
		stmt.setInt(1,m_snmpnode.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update stpinterface: updated to NOT ACTIVE status "
							+ i + " rows for nodeid "
							+ m_snmpnode.getNodeId() + ".");
		
		stmt.close();
	}

	private void update(Connection dbConn, char status) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());
		PreparedStatement stmt = null;
		Iterator ite = null;

		int i = 0;
		stmt = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2,m_nodeId);
		stmt.setInt(3,m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update atinterface: updated to status " + status +
							+ i + " rows for nodeid"
							+ m_snmpnode.getNodeId() + ".");

		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2,m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update iprouteinterface: updated to status " + status +
							+ i + " rows for nodeid"
							+ m_snmpnode.getNodeId() + ".");
		
		stmt.close();
		
		stmt = dbConn.prepareStatement(SQL_UPDATE_STPNODE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2,m_snmpnode.getNodeId());

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update stpnode: updated to status " + status +
							+ i + " rows for nodeid"
							+ m_snmpnode.getNodeId() + ".");
		
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2,m_snmpnode.getNodeId());

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update stpinterface: updated to status " + status +
							+ i + " rows for nodeid"
							+ m_snmpnode.getNodeId() + ".");
		
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2,m_nodeId);
		stmt.setInt(3,m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log
					.debug("update datalinkinterface: updated to status " + status +
							+ i + " rows for nodeid"
							+ m_snmpnode.getNodeId() + ".");

		stmt.close();

	}
}