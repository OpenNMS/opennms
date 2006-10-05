/*
 * Created on 8-lug-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.opennms.netmgt.linkd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.DataSourceFactory;

import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dBasePortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dStpGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dStpPortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dTpFdbTableEntry;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTableEntry;
import org.opennms.netmgt.linkd.snmp.IpRouteTableEntry;

/**
 * <P>
 * This class is used to store informations owned by SnmpCollection
 * and DiscoveryLink Classes in DB. 
 * When saving Snmp Collection it populate Bean LinkableNode
 * with information for DiscoveryLink.
 * It performes data test for DiscoveryLink.
 * Also take correct action on DB tables in case node is deleted
 * service SNMP is discovered, service SNMP is Lost and Regained
 * </P>
 *
 * @author antonio
 * 
 *  
 * */

public class DbEventWriter implements Runnable {

//  FIXME: change all getInt32 calls to not autobox the result into an int
//  FIXME: send routedest 0.0.0.0 to discoverylink  
	
	static final char ACTION_UPTODATE = 'N';

	static final char ACTION_DELETE = 'D';

	static final char ACTION_STORE = 'S';
	
	static final char ACTION_STORE_LINKS = 'A';

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

	LinkableNode m_node;
	
	SnmpCollection m_snmpcoll;

	DiscoveryLink m_discovery;

	int m_nodeId;

	private static final String SQL_GET_NODEID = "SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?";

	private static final String SQL_GET_NODEID__IFINDEX_MASK = "SELECT node.nodeid,snmpinterface.snmpifindex,snmpinterface.snmpipadentnetmask FROM node LEFT JOIN snmpinterface ON node.nodeid = snmpinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?";

	private static final String SQL_GET_NODEID_IFINDEX_IPINT = "SELECT node.nodeid,ipinterface.ifindex FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?";

	private static final String SQL_UPDATE_DATAINTERFACE = "UPDATE datalinkinterface set status = 'N'  WHERE lastpolltime < ? AND status = 'A'";
	
	private static final String SQL_UPDATE_ATINTERFACE = "UPDATE atinterface set status = 'N'  WHERE sourcenodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_IPROUTEINTERFACE = "UPDATE iprouteinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_STPNODE = "UPDATE stpnode set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_STPINTERFACE = "UPDATE stpinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

	private static final String SQL_UPDATE_ATINTERFACE_STATUS = "UPDATE atinterface set status = ?  WHERE sourcenodeid = ? OR nodeid = ?";

	private static final String SQL_UPDATE_IPROUTEINTERFACE_STATUS = "UPDATE iprouteinterface set status = ? WHERE nodeid = ? ";

	private static final String SQL_UPDATE_STPNODE_STATUS = "UPDATE stpnode set status = ?  WHERE nodeid = ? ";

	private static final String SQL_UPDATE_STPINTERFACE_STATUS = "UPDATE stpinterface set status = ? WHERE nodeid = ? ";

	private static final String SQL_UPDATE_DATALINKINTERFACE_STATUS = "UPDATE datalinkinterface set status = ? WHERE nodeid = ? OR nodeparentid = ? ";

	private static final String SQL_GET_NODEID_IFINDEX = "SELECT atinterface.nodeid, snmpinterface.snmpifindex from atinterface left JOIN snmpinterface ON atinterface.nodeid = snmpinterface.nodeid AND atinterface.ipaddr = snmpinterface.ipaddr WHERE atphysaddr = ? AND status = 'A'";
	
	private static final String SQL_GET_SNMPIFTYPE = "SELECT snmpiftype FROM snmpinterface WHERE nodeid = ? AND snmpifindex = ?";

	private static final String SQL_GET_IFINDEX_SNMPINTERFACE_NAME = "SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND (snmpifname = ? OR snmpifdescr = ?) ";
	
	private static final String SQL_GET_SNMPPHYSADDR_SNMPINTERFACE = "SELECT snmpphysaddr FROM snmpinterface WHERE nodeid = ? AND  snmpphysaddr <> ''";

	private char action = ACTION_STORE;

	/**
	 * @param m_snmpcoll
	 */

	public DbEventWriter(int nodeid, SnmpCollection m_snmpcoll) {

		super();
		this.m_nodeId = nodeid;
		this.m_node = new LinkableNode(nodeid,m_snmpcoll.getSnmpIpPrimary().getHostAddress());
		this.m_snmpcoll = m_snmpcoll;

	}

	/**
	 * 
	 * @param nodeid
	 * @param action
	 */
	public DbEventWriter(int nodeid, char action) {

		super();
		m_node = null;
		this.m_nodeId = nodeid;
		this.action = action;
	}

	public DbEventWriter (DiscoveryLink discoverLink) {
		m_node = null;
		m_discovery = discoverLink;
		this.action = ACTION_STORE_LINKS;
	}

	public void run() {

		Category log = ThreadCategory.getInstance(getClass());
		Connection dbConn = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());

		try {
			dbConn = DataSourceFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("run: Storing information into database");
			}
			if (action == ACTION_STORE)
				storeSnmpCollection(dbConn, now);
			else if (action == ACTION_UPTODATE || action == ACTION_DELETE)
				update(dbConn, action);
			else if (action == ACTION_STORE_LINKS)
				storeDiscoveryLink(dbConn,now);
			else
				log.warn("Unknown action: " + action + " . Exiting");
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
				log
						.fatal(
								"Unknown error while syncing node object with database information.",
								e);
			}
		}
	}

	/**
	 * 
	 * @param dbConn
	 * @param now
	 * @throws SQLException
	 */
	private void storeDiscoveryLink(Connection dbConn, Timestamp now) throws SQLException,
			UnknownHostException {

		Category log = ThreadCategory.getInstance(getClass());
		
		PreparedStatement stmt = null;
		ResultSet rs = null;

		NodeToNodeLink[] links = m_discovery.getLinks();
		
		if (log.isDebugEnabled()) {
			log.debug("storelink: Storing " + links.length + " NodeToNodeLink information into database");
		}
		for (int i=0; i <links.length; i++) {
			NodeToNodeLink lk = links[i];
			int nodeid = lk.getNodeId();
			int ifindex = lk.getIfindex();
			int nodeparentid = lk.getNodeparentid();
			int parentifindex = lk.getParentifindex();
			
			DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry.get(dbConn,
					nodeid, ifindex);
			if (dbentry == null) {
				// Create a new entry
				dbentry = DbDataLinkInterfaceEntry.create(nodeid, ifindex);
			}
			dbentry.updateNodeParentId(nodeparentid);
			dbentry.updateParentIfIndex(parentifindex);
			dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
			dbentry.set_lastpolltime(now);

			dbentry.store(dbConn);

			// now parsing simmetrical and setting to D if necessary

			dbentry = DbDataLinkInterfaceEntry.get(dbConn, nodeparentid,
						parentifindex);

			if (dbentry != null) {
				if (dbentry.get_nodeparentid() == nodeid
						&& dbentry.get_parentifindex() == ifindex
						&& dbentry.get_status() != DbDataLinkInterfaceEntry.STATUS_DELETE) {
					dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_DELETE);
					dbentry.store(dbConn);
				}
			}
		}
		
		MacToNodeLink[] linkmacs = m_discovery.getMacLinks();
		
		if (log.isDebugEnabled()) {
			log.debug("storelink: Storing " + linkmacs.length + " MacToNodeLink information into database");
		}
		for (int i = 0; i < linkmacs.length; i++) {
			MacToNodeLink lkm = linkmacs[i];
			String macaddr = lkm.getMacAddress();
			int nodeparentid = lkm.getNodeparentid();
			int parentifindex = lkm.getParentifindex();

			stmt = dbConn.prepareStatement(SQL_GET_NODEID_IFINDEX);

			stmt.setString(1, macaddr);

			
			rs = stmt.executeQuery();
			
			if (log.isDebugEnabled())
				log.debug("storelink: finding nodeid,ifindex on DB. Sql Statement "
					+ SQL_GET_NODEID_IFINDEX + " with mac address " + macaddr);

			if (!rs.next()) {
				rs.close();
				stmt.close();
				if (log.isDebugEnabled())
					log.debug("storelink: no nodeid found on DB for mac address "
						+ macaddr
						+ " on link. .... Skipping");
				continue;
			}

			// extract the values.
			//
			int ndx = 1;

			int nodeid = rs.getInt(ndx++);
			if (rs.wasNull()) {
				rs.close();
				stmt.close();
				if (log.isDebugEnabled())
					log.debug("storelink: no nodeid found on DB for mac address "
						+ macaddr
						+ " on link. .... Skipping");
				continue;
			}

			int ifindex = rs.getInt(ndx++);
			if (rs.wasNull()) {
				if (log.isDebugEnabled())
					log.debug("storelink: no ifindex found on DB for mac address "
						+ macaddr
						+ " on link.");
				ifindex = -1;
			}

			rs.close();
			stmt.close();

			DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry.get(dbConn,
					nodeid, ifindex);
			if (dbentry == null) {
				// Create a new entry
				dbentry = DbDataLinkInterfaceEntry.create(nodeid, ifindex);
			}
			dbentry.updateNodeParentId(nodeparentid);
			dbentry.updateParentIfIndex(parentifindex);
			dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
			dbentry.set_lastpolltime(now);

			dbentry.store(dbConn);

			
		}
		
		stmt = dbConn.prepareStatement(SQL_UPDATE_DATAINTERFACE);
		stmt.setTimestamp(1, now);

		int i = stmt.executeUpdate();
		stmt.close();
		if (log.isDebugEnabled())
			log
					.debug("storelink: datalinkinterface - updated to NOT ACTIVE status "
							+ i + " rows ");



	}
			
	/**
	 * 
	 * @param dbConn
	 * @param now
	 * @throws SQLException
	 */
	private void storeSnmpCollection(Connection dbConn, Timestamp now) throws SQLException,
			UnknownHostException {

		Category log = ThreadCategory.getInstance(getClass());
		Iterator ite = null;

		int nodeid = m_node.getNodeId();

		if (m_snmpcoll.hasIpNetToMediaTable()) {
			ite = m_snmpcoll.getIpNetToMediaTable()
					.getEntries().iterator();
			if (log.isDebugEnabled())
				log
						.debug("store: saving IpNetToMediaTable to atinterface table in DB");
			// the AtInterfaces used by LinkableNode where to save info
			java.util.List<AtInterface> atInterfaces = new java.util.ArrayList<AtInterface>();
			while (ite.hasNext()) {

				IpNetToMediaTableEntry ent = (IpNetToMediaTableEntry) ite
						.next();
				
				int ifindex = ent
						.getInt32(IpNetToMediaTableEntry.INTM_INDEX);
				
				InetAddress ipaddress = ent
						.getIPAddress(IpNetToMediaTableEntry.INTM_NETADDR);

				if (ipaddress.isLoopbackAddress() || ipaddress.getHostAddress().equals("0.0.0.0")) {
					log.warn("store: invalid ip " + ipaddress.getHostAddress());
					continue;
				}

				String physAddr = ent
						.getHexString(IpNetToMediaTableEntry.INTM_PHYSADDR);
				
				if (physAddr.equals("000000000000") || physAddr.equalsIgnoreCase("ffffffffffff")) {
					log.warn("store: invalid mac address " + physAddr
							+ " for ip " + ipaddress.getHostAddress());
					continue;
				}


				if (log.isDebugEnabled())
					log.debug("store: trying save info for ipaddr " + ipaddress.getHostName()
							+ " mac address " + physAddr + " found on ifindex "
							+ ifindex);

				// get an At interface but without setting mac address
				AtInterface at = getNodeidIfindexFromIp(dbConn, ipaddress);
				if (at == null) {
						log.warn("getNodeidIfindexFromIp: no nodeid found for ipaddress "
								+ ipaddress + ".");
					sendNewSuspectEvent(ipaddress);
					continue;
				}
				//set the mac address
				at.setMacAddress(physAddr);
				// add At Inteface to list of valid interfaces
				atInterfaces.add(at);

				// Save in DB
				DbAtInterfaceEntry atInterfaceEntry = DbAtInterfaceEntry.get(
						dbConn, at.getNodeId(), ipaddress.getHostAddress());
				if (atInterfaceEntry == null) {
					atInterfaceEntry = DbAtInterfaceEntry.create(at.getNodeId(),
							ipaddress.getHostAddress());
				}
				// update object

				atInterfaceEntry.updateAtPhysAddr(physAddr);

				atInterfaceEntry.updateSourceNodeId(nodeid);
				atInterfaceEntry.updateIfIndex(ifindex);
				atInterfaceEntry.updateStatus(DbAtInterfaceEntry.STATUS_ACTIVE);
				atInterfaceEntry.set_lastpolltime(now);

				// store object in database
				atInterfaceEntry.store(dbConn);
			}
			// set AtInterfaces in LinkableNode
			m_node.setAtInterfaces(atInterfaces);
		}

		if (m_snmpcoll.hasCdpCacheTable()) {
			if (log.isDebugEnabled())
				log
						.debug("store: saving CdpCacheTable into SnmpLinkableNode");
			java.util.List<CdpInterface> cdpInterfaces = new java.util.ArrayList<CdpInterface>();
			ite = m_snmpcoll.getCdpCacheTable()
					.getEntries().iterator();
			while (ite.hasNext()) {
				CdpCacheTableEntry cdpEntry = (CdpCacheTableEntry) ite.next();
				int cdpAddrType = cdpEntry.getInt32(CdpCacheTableEntry.CDP_ADDRESS_TYPE);

				if (cdpAddrType != 1)
					continue;
				String cdptargetipaddress = cdpEntry.getHexString(CdpCacheTableEntry.CDP_ADDRESS);
				if (log.isDebugEnabled())	log.debug(" cdp ip address octet string is " + cdptargetipaddress);

				long ipAddr = Long.parseLong(cdptargetipaddress, 16);
				byte[] bytes = new byte[4];
				bytes[3] = (byte) (ipAddr & 0xff);
				bytes[2] = (byte) ((ipAddr >> 8) & 0xff);
				bytes[1] = (byte) ((ipAddr >> 16) & 0xff);
				bytes[0] = (byte) ((ipAddr >> 24) & 0xff);
				                         
				InetAddress cdpTargetIpAddr = InetAddress.getByAddress(bytes);
				if (log.isDebugEnabled())	log.debug(" cdp ip address after parsing is " + cdpTargetIpAddr.getHostAddress());
				
				int cdpIfIndex = cdpEntry.getInt32(CdpCacheTableEntry.CDP_IFINDEX);
				if (log.isDebugEnabled())	log.debug(" cdp ifindex is " + cdpIfIndex);

				String cdpTargetDevicePort = cdpEntry.getDisplayString(CdpCacheTableEntry.CDP_DEVICEPORT);
				if (log.isDebugEnabled())	log.debug(" cdp Target device port name is " + cdpTargetDevicePort);


				CdpInterface cdpIface = new CdpInterface(cdpIfIndex);

				int targetCdpNodeId = getNodeidFromIp(dbConn, cdpTargetIpAddr);

				cdpIface.setCdpTargetNodeId(targetCdpNodeId);
				cdpIface.setCdpTargetIpAddr(cdpTargetIpAddr);
				
				int cdpTargetIfindex = getIfIndexByName(
						dbConn, targetCdpNodeId, cdpTargetDevicePort);

				if (targetCdpNodeId == -1 || cdpTargetIfindex == -1) {
					log.warn("No nodeid found: cdp interface not added to Linkable Snmp Node");
					sendNewSuspectEvent(cdpTargetIpAddr);
				} else  {

					cdpIface.setCdpTargetIfIndex(cdpTargetIfindex);
					cdpInterfaces.add(cdpIface);
				}

			}
			m_node.setCdpInterfaces(cdpInterfaces);
		}

		if (m_snmpcoll.hasRouteTable()) {
			java.util.List<RouterInterface> routeInterfaces = new java.util.ArrayList<RouterInterface>();
			
			ite = m_snmpcoll.getIpRouteTable().getEntries()
					.iterator();
			if (log.isDebugEnabled())
				log
						.debug("store: saving ipRouteTable to iprouteinterface table in DB");
			while (ite.hasNext()) {
				IpRouteTableEntry ent = (IpRouteTableEntry) ite.next();
                
                log.debug("storeSnmpCollection: ent="+ent);

				InetAddress routedest = ent.getIPAddress(IpRouteTableEntry.IP_ROUTE_DEST);
				InetAddress routemask = ent.getIPAddress(IpRouteTableEntry.IP_ROUTE_MASK);
				InetAddress nexthop = ent.getIPAddress(IpRouteTableEntry.IP_ROUTE_NXTHOP);
				Integer ifindex = ent.getInt32(IpRouteTableEntry.IP_ROUTE_IFINDEX);
                log.debug("storeSnmpCollection: ifindex is: "+ (ifindex < 1 ? "less than 1" : ifindex)+"; IP_ROUTE_IFINDEX: "+IpRouteTableEntry.IP_ROUTE_IFINDEX);

                if (ifindex == null) {
					log.warn("store: Not valid ifindex" + ifindex 
							+ " Skipping...");
					continue;
				}
                
				Integer routemetric1 = ent.getInt32(IpRouteTableEntry.IP_ROUTE_METRIC1);
				Integer routemetric2 = ent.getInt32(IpRouteTableEntry.IP_ROUTE_METRIC2);
				Integer routemetric3  =ent.getInt32(IpRouteTableEntry.IP_ROUTE_METRIC3);
				Integer routemetric4 = ent.getInt32(IpRouteTableEntry.IP_ROUTE_METRIC4);
				Integer routemetric5 = ent.getInt32(IpRouteTableEntry.IP_ROUTE_METRIC5);
				Integer routetype = ent.getInt32(IpRouteTableEntry.IP_ROUTE_TYPE);
				Integer routeproto = ent.getInt32(IpRouteTableEntry.IP_ROUTE_PROTO);

				/**
				 *  TODO remeber that now nexthop 0.0.0.0 is not 
				 *  parsed, anyway we should analize this case in link discovery
				 *  so here is the place where you can have this info saved for
				 * now is discarded. See DiscoveryLink for more details......
				 * 
				**/
				
				// info used for Discovery Link
				// the routeiface constructor set nodeid, ifindex, netmask for nexthop address
				RouterInterface routeIface = getNodeidMaskFromIp(dbConn,nexthop);

				// if target node is not snmp node always try to save info
				if (routeIface == null) {
					routeIface = getNodeFromIp(dbConn, nexthop);
				}
					
				if (routeIface == null) {
					log.warn("store: No nodeid found for next hop ip" + nexthop 
							+ " Skipping ip route interface add to Linkable Snmp Node");
					// try to find it in ipinterface
					sendNewSuspectEvent(nexthop);
				} else {
					int snmpiftype = -2;
                    
                    //Okay to autobox here, we checked for null
					if (ifindex != null && ifindex > 0) snmpiftype = getSnmpIfType(dbConn, nodeid, ifindex);

					if (snmpiftype == -1) {
						log.warn("store: interface has wrong or null snmpiftype "
								+ snmpiftype + " . Skip adding to DiscoverLink ");
						
					} else {
						if (log.isDebugEnabled())
							log.debug("store: interface has snmpiftype "
										+ snmpiftype + " . Adding to DiscoverLink ");

						routeIface.setRouteDest(routedest);
						routeIface.setRoutemask(routemask);
						routeIface.setSnmpiftype(snmpiftype);
						routeIface.setIfindex(ifindex);
						routeIface.setMetric(routemetric1);

						routeIface.setNextHop(nexthop);
							
						routeInterfaces.add(routeIface);
						
					}
				}

				// save info to DB
				DbIpRouteInterfaceEntry iprouteInterfaceEntry = DbIpRouteInterfaceEntry
						.get(dbConn, nodeid, routedest.getHostAddress());
				if (iprouteInterfaceEntry == null) {
					// Create a new entry
					iprouteInterfaceEntry = DbIpRouteInterfaceEntry.create(
							m_node.getNodeId(), routedest.getHostAddress());
				}
				// update object
				iprouteInterfaceEntry.updateRouteMask(routemask.getHostAddress());
				iprouteInterfaceEntry.updateRouteNextHop(nexthop.getHostAddress());
				iprouteInterfaceEntry.updateIfIndex(ifindex);
                
                //okay to autobox these since were checking for null
				if (routemetric1 != null)
					iprouteInterfaceEntry.updateRouteMetric1(routemetric1);
				if (routemetric2 != null)
					iprouteInterfaceEntry.updateRouteMetric2(routemetric2);
				if (routemetric3 != null)
					iprouteInterfaceEntry.updateRouteMetric3(routemetric3);
				if (routemetric4 != null)
					iprouteInterfaceEntry.updateRouteMetric4(routemetric4);
				if (routemetric5 != null)
					iprouteInterfaceEntry.updateRouteMetric5(routemetric5);
				if (routetype != null)
					iprouteInterfaceEntry.updateRouteType(routetype);
				if (routeproto != null)
					iprouteInterfaceEntry.updateRouteProto(routeproto);
				iprouteInterfaceEntry
						.updateStatus(DbAtInterfaceEntry.STATUS_ACTIVE);
				iprouteInterfaceEntry.set_lastpolltime(now);

				// store object in database
				iprouteInterfaceEntry.store(dbConn);
			}
			m_node.setRouteInterfaces(routeInterfaces);
		}
		// STARTS loop on vlans

		if (m_snmpcoll.hasVlanTable()) {
			
			if (log.isDebugEnabled())
				log
						.debug("store: saving SnmpVlanCollection in DB");
			ite = m_snmpcoll.getSnmpVlanCollections()
					.iterator();
			while (ite.hasNext()) {
				SnmpVlanCollection snmpVlanColl = (SnmpVlanCollection) ite
						.next();

				String vlanindex = snmpVlanColl.getVlanIndex();
				int vlan = Integer.parseInt(vlanindex);
				String vlanname = snmpVlanColl.getVlanName();
				if (log.isDebugEnabled())
					log
							.debug("store: parsing VLAN "
									+ vlanindex + " VLAN_NAME " + vlanname);

				if (snmpVlanColl.hasDot1dBase()) {
					if (log.isDebugEnabled())
						log
								.debug("store: saving Dot1dBaseGroup in stpnode table");

					Dot1dBaseGroup dod1db = (Dot1dBaseGroup) snmpVlanColl.getDot1dBase();
					String baseBridgeAddress = dod1db.getBridgeAddress();
					int basenumports = dod1db.getNumberOfPorts();
					int bridgetype = dod1db.getBridgeType();
					

					if (baseBridgeAddress == "000000000000") {
						log.warn("store: base bridge address " + baseBridgeAddress
								+ " is invalid for ipaddress " );
					} else {
						m_node.addBridgeIdentifier(baseBridgeAddress,vlanindex);
						DbStpNodeEntry dbStpNodeEntry = DbStpNodeEntry.get(dbConn,
							m_node.getNodeId(), vlan);
						if (dbStpNodeEntry == null) {
							// Create a new entry
							dbStpNodeEntry = DbStpNodeEntry.create(m_node
								.getNodeId(), vlan);
						}
						// update object

						dbStpNodeEntry.updateBaseBridgeAddress(baseBridgeAddress);
						dbStpNodeEntry.updateBaseNumPorts(basenumports);
						dbStpNodeEntry.updateBaseType(bridgetype);
						dbStpNodeEntry.updateBaseVlanName(vlanname);
					
						if (snmpVlanColl.hasDot1dStp()) {
							if (log.isDebugEnabled())
								log
									.debug("store: adding Dot1dStpGroup in stpnode table");

							Dot1dStpGroup dod1stp = (Dot1dStpGroup) snmpVlanColl
								.getDot1dStp();
							int protospec = dod1stp.getStpProtocolSpecification();
							int stppriority = dod1stp.getStpPriority();
							String stpDesignatedRoot = dod1stp.getStpDesignatedRoot();
							int stprootcost = dod1stp.getStpRootCost();
							int stprootport = dod1stp.getStpRootPort();

							if (stpDesignatedRoot != "0000000000000000") {
								m_node.setVlanStpRoot(vlanindex,stpDesignatedRoot);
							}
							
							dbStpNodeEntry.updateStpProtocolSpecification(protospec);
							dbStpNodeEntry.updateStpPriority(stppriority);
							dbStpNodeEntry.updateStpDesignatedRoot(stpDesignatedRoot);
							dbStpNodeEntry.updateStpRootCost(stprootcost);
							dbStpNodeEntry.updateStpRootPort(stprootport);
						}
					// store object in database
						dbStpNodeEntry.updateStatus(DbStpNodeEntry.STATUS_ACTIVE);
						dbStpNodeEntry.set_lastpolltime(now);

						dbStpNodeEntry.store(dbConn);
					
						if (snmpVlanColl.hasDot1dBasePortTable()) {
							Iterator sub_ite = snmpVlanColl.getDot1dBasePortTable()
								.getEntries().iterator();
							if (log.isDebugEnabled())
								log
									.debug("store: saving Dot1dBasePortTable in stpinterface table");
							while (sub_ite.hasNext()) {
								Dot1dBasePortTableEntry dot1dbaseptentry = (Dot1dBasePortTableEntry) sub_ite
									.next();
								Integer baseport = dot1dbaseptentry.getInt32(Dot1dBasePortTableEntry.BASE_PORT);
								Integer ifindex = dot1dbaseptentry.getInt32(Dot1dBasePortTableEntry.BASE_IFINDEX);
							
								m_node.setIfIndexBridgePort(ifindex,baseport);
							
								DbStpInterfaceEntry dbStpIntEntry = DbStpInterfaceEntry
									.get(dbConn, m_node.getNodeId(),
											baseport, vlan);
								if (dbStpIntEntry == null) {
								// Create a new entry
									dbStpIntEntry = DbStpInterfaceEntry.create(
										m_node.getNodeId(), baseport, vlan);
								}
								dbStpIntEntry.updateIfIndex(ifindex);

								dbStpIntEntry
									.updateStatus(DbStpNodeEntry.STATUS_ACTIVE);
								dbStpIntEntry.set_lastpolltime(now);

								dbStpIntEntry.store(dbConn);

							}
						}

						if (snmpVlanColl.hasDot1dStpPortTable()) {
							if (log.isDebugEnabled())
								log
									.debug(" store: adding Dot1dStpPortTable in stpinterface table");
							Iterator sub_ite = snmpVlanColl.getDot1dStpPortTable()
								.getEntries().iterator();
							while (sub_ite.hasNext()) {
								Dot1dStpPortTableEntry dot1dstpptentry = (Dot1dStpPortTableEntry) sub_ite
									.next();
								int stpport = dot1dstpptentry.getInt32(Dot1dStpPortTableEntry.STP_PORT);
								DbStpInterfaceEntry dbStpIntEntry = DbStpInterfaceEntry
									.get(dbConn, m_node.getNodeId(),
											stpport, vlan);
								if (dbStpIntEntry == null) {
								// Cannot create the object becouse must exists the dot1dbase
								// object!!!!!
									log
											.warn("store StpInterface: when storing STP info"
													+ " for bridge node with nodeid "
													+ m_node.getNodeId()
													+ " bridgeport number "
													+ stpport
													+ " and vlan index "
													+ vlanindex
													+ " info not found in database, ERROR skipping.....");
								} else {

									int stpportstate = dot1dstpptentry
										.getInt32(Dot1dStpPortTableEntry.STP_PORT_STATE);
									int stpportpathcost = dot1dstpptentry
										.getInt32(Dot1dStpPortTableEntry.STP_PORT_PATH_COST);
									String stpPortDesignatedBridge = dot1dstpptentry
										.getHexString(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_BRIDGE);
									String stpPortDesignatedRoot = dot1dstpptentry
										.getHexString(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_ROOT);
									int stpportdesignatedcost = dot1dstpptentry
										.getInt32(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_COST);
									String stpPortDesignatedPort = dot1dstpptentry
										.getHexString(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_PORT);
								

									if (stpPortDesignatedBridge.equals("0000000000000000")) {
										log.warn("storeSnmpCollection: designated bridge is invalid not adding to discoveryLink");
									} else if (stpPortDesignatedPort.equals("0000")) {
										log.warn("storeSnmpCollection: designated port is invalid not adding to discoveryLink");
									} else {
										BridgeStpInterface stpIface = new BridgeStpInterface(stpport,vlanindex);
										stpIface.setStpPortDesignatedBridge(stpPortDesignatedBridge);
										stpIface.setStpPortDesignatedPort(stpPortDesignatedPort);
										m_node.addStpInterface(stpIface);
									}
								
									dbStpIntEntry.updateStpPortState(stpportstate);
									dbStpIntEntry.updateStpPortPathCost(stpportpathcost);
									dbStpIntEntry.updateStpportDesignatedBridge(stpPortDesignatedBridge);
									dbStpIntEntry.updateStpportDesignatedRoot(stpPortDesignatedRoot);
									dbStpIntEntry.updateStpPortDesignatedCost(stpportdesignatedcost);
									dbStpIntEntry.updateStpportDesignatedPort(stpPortDesignatedPort);
									dbStpIntEntry.updateStatus(DbStpNodeEntry.STATUS_ACTIVE);
									dbStpIntEntry.set_lastpolltime(now);

									dbStpIntEntry.store(dbConn);

								}
							}
						}
					
						if (snmpVlanColl.hasDot1dTpFdbTable()) {
							if (log.isDebugEnabled())
								log
										.debug("store: parsing Dot1dTpFdbTable");

							Iterator subite = snmpVlanColl.getDot1dFdbTable()
								.getEntries().iterator();
							while (subite.hasNext()) {
								Dot1dTpFdbTableEntry dot1dfdbentry = (Dot1dTpFdbTableEntry) subite
									.next();
								String curMacAddress = dot1dfdbentry
									.getHexString(Dot1dTpFdbTableEntry.FDB_ADDRESS);

								int fdbport = dot1dfdbentry.getInt32(Dot1dTpFdbTableEntry.FDB_PORT);

								if (fdbport == 0) {
									if (log.isDebugEnabled())
										log.debug("populateBridge: macaddress "
											+ curMacAddress
											+ " learned on invalid port "
											+ fdbport + " . Skipping");
									continue;
								}

								int curfdbstatus = dot1dfdbentry.getInt32(Dot1dTpFdbTableEntry.FDB_STATUS);

								if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_LEARNED) {
									m_node.addMacAddress(fdbport,
										curMacAddress, vlanindex);
									if (log.isDebugEnabled())
										log
											.debug("storeSnmpCollection: found learned mac address "
													+ curMacAddress
													+ " on bridge port "
													+ fdbport
													+ " for VLAN "
													+ snmpVlanColl.getVlanIndex());
								}

								if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_SELF) {
									m_node.addBridgeIdentifier(curMacAddress);
									if (log.isDebugEnabled())
									log
											.debug("storeSnmpCollection: found bridge identifier "
													+ curMacAddress
													+ " for VLAN "
													+ snmpVlanColl.getVlanIndex()
													+ " and bridge port "
													+ fdbport);
								}

								if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_INVALID) {
									if (log.isDebugEnabled())
										log.debug("storeSnmpCollection: macaddress "
											+ curMacAddress
											+ " has INVALID status on port "
											+ fdbport + " . Skipping");
									continue;
								}

								if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_MGMT) {
									if (log.isDebugEnabled())
										log.debug("storeSnmpCollection: macaddress "
											+ curMacAddress
											+ " has MGMT status on port "
											+ fdbport + " . Skipping");
									continue;
								}

								if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_OTHER) {
									if (log.isDebugEnabled())
										log.debug("storeSnmpCollection: macaddress "
											+ curMacAddress
											+ " has OTHER status on port "
											+ fdbport + " . Skipping");
									continue;
								}

							}
						}
						
						//now adding bridge identifier mac addresses of switch from snmpinterface
						setBridgeIdentifierFromSnmpInterface(dbConn);
					}
				}
			}
		}
		update(dbConn, now);
		
	}	
	
	private void update(Connection dbConn, Timestamp now) throws SQLException {
	
		Category log = ThreadCategory.getInstance(getClass());
		PreparedStatement stmt = null;

		int i = 0;
		stmt = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE);
		stmt.setInt(1, m_node.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("store: SQL statement " + SQL_UPDATE_ATINTERFACE + ". " + i
					+ " rows UPDATED for nodeid=" + m_node.getNodeId()
					+ ".");

		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE);
		stmt.setInt(1, m_node.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("store: SQL statement " + SQL_UPDATE_IPROUTEINTERFACE + ". " + i
					+ " rows UPDATED for nodeid=" + m_node.getNodeId()
					+ ".");

		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_STPNODE);
		stmt.setInt(1, m_node.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("store: SQL statement " + SQL_UPDATE_STPNODE + ". " + i
					+ " rows UPDATED for nodeid=" + m_node.getNodeId()
					+ ".");
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE);
		stmt.setInt(1, m_node.getNodeId());
		stmt.setTimestamp(2, now);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("store: SQL statement " + SQL_UPDATE_STPINTERFACE + ". " + i
					+ " rows UPDATED for nodeid=" + m_node.getNodeId()
					+ ".");
		stmt.close();
	}

	private void update(Connection dbConn, char status) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());
		PreparedStatement stmt = null;

		int i = 0;
		stmt = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2, m_nodeId);
		stmt.setInt(3, m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("update: SQL statement " + SQL_UPDATE_ATINTERFACE_STATUS + ". " + i
					+ " rows UPDATED for nodeid=" + m_nodeId
					+ ".");
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2, m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("update: SQL statement " + SQL_UPDATE_IPROUTEINTERFACE_STATUS + ". " + i
					+ " rows UPDATED for nodeid=" + m_nodeId
					+ ".");
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_STPNODE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2, m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("update: SQL statement " + SQL_UPDATE_STPNODE_STATUS + ". " + i
					+ " rows UPDATED for nodeid=" + m_nodeId
					+ ".");
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2, m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("update: SQL statement " + SQL_UPDATE_STPINTERFACE_STATUS + ". " + i
					+ " rows UPDATED for nodeid=" + m_nodeId
					+ ".");
		stmt.close();

		stmt = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE_STATUS);
		stmt.setString(1, new String(new char[] { status }));
		stmt.setInt(2, m_nodeId);
		stmt.setInt(3, m_nodeId);

		i = stmt.executeUpdate();
		if (log.isDebugEnabled())
			log.debug("update: SQL statement " + SQL_UPDATE_DATALINKINTERFACE_STATUS + ". " + i
					+ " rows UPDATED for nodeid=" + m_nodeId
					+ ".");
		stmt.close();

	}

	private void setBridgeIdentifierFromSnmpInterface(Connection dbConn) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_SNMPPHYSADDR_SNMPINTERFACE);
		stmt.setInt(1, m_node.getNodeId());
		
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			String macaddr = rs.getString("snmpphysaddr");
			if (macaddr == null)
				continue;
			m_node.addBridgeIdentifier(macaddr);
			if (log.isDebugEnabled())
				log
						.debug("setBridgeIdentifierFromSnmpInterface: found bridge identifier "
								+ macaddr
								+ " from snmpinterface db table");
		}


	}



	private int getNodeidFromIp(Connection dbConn, InetAddress ipaddr)
			throws SQLException {


		if (ipaddr.isLoopbackAddress() || ipaddr.getHostAddress().equals("0.0.0.0")) return -1;

		Category log = ThreadCategory.getInstance(getClass());
		
		int nodeid = -1;
		
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEID);
		stmt.setString(1, ipaddr.getHostAddress());

		if (log.isDebugEnabled())
			log.debug("getNodeidFromIp: executing query " + SQL_GET_NODEID + " with ip address=" + ipaddr.getHostAddress());

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
			log.debug("getNodeidFromIp: found nodeid " + nodeid);

		stmt.close();

		return nodeid;

	}

	private RouterInterface getNodeidMaskFromIp(Connection dbConn, InetAddress ipaddr)
	throws SQLException {
		if (ipaddr.isLoopbackAddress() || ipaddr.getHostAddress().equals("0.0.0.0")) return null;
			
		Category log = ThreadCategory.getInstance(getClass());
			
		int nodeid = -1;
		int ifindex = -1;
		String netmask = null;
		
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEID__IFINDEX_MASK);
		stmt.setString(1, ipaddr.getHostAddress());
			
		if (log.isDebugEnabled())
			log.debug("getNodeidMaskFromIp: executing query " + SQL_GET_NODEID__IFINDEX_MASK + " with ip address=" + ipaddr.getHostAddress());
			
			
		ResultSet rs = stmt.executeQuery();
			
		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log.debug("getNodeidMaskFromIp: no entries found in snmpinterface");
			return null;
		}
		// extract the values.
		//
		// get the node id
		//
		nodeid = rs.getInt("nodeid");
		if (rs.wasNull()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log.debug("getNodeidMaskFromIp: no nodeid found");
			return null;
		}

		ifindex = rs.getInt("snmpifindex");
		if (rs.wasNull()) {
			if (log.isDebugEnabled())
				log.debug("getNodeidMaskFromIp: no snmsnmpifindex found");
			ifindex = -1;
		}

		netmask = rs.getString("snmpipadentnetmask");
		if (rs.wasNull()) {
			if (log.isDebugEnabled())
				log.debug("getNodeidMaskFromIp: no snmpipadentnetmask found");
			netmask = "255.255.255.255";
		}

		rs.close();
		stmt.close();
		RouterInterface ri = new RouterInterface(nodeid,ifindex,netmask);
		return ri;
		
	}

	private RouterInterface getNodeFromIp(Connection dbConn, InetAddress ipaddr)
	throws SQLException {
		if (ipaddr.isLoopbackAddress() || ipaddr.getHostAddress().equals("0.0.0.0")) return null;
			
		Category log = ThreadCategory.getInstance(getClass());
			
		int nodeid = -1;
		int ifindex = -1;
		
		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_NODEID);
		stmt.setString(1, ipaddr.getHostAddress());
			
		if (log.isDebugEnabled())
			log.debug("getNodeFromIp: executing query " + SQL_GET_NODEID + " with ip address=" + ipaddr.getHostAddress());
			
			
		ResultSet rs = stmt.executeQuery();
			
		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log.debug("getNodeFromIp: no entries found in snmpinterface");
			return null;
		}
		// extract the values.
		//
		// get the node id
		//
		nodeid = rs.getInt("nodeid");
		if (rs.wasNull()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log.debug("getNodeFromIp: no nodeid found");
			return null;
		}

		rs.close();
		stmt.close();
		RouterInterface ri = new RouterInterface(nodeid,ifindex);
		return ri;
		
	}

	private AtInterface getNodeidIfindexFromIp(Connection dbConn, InetAddress ipaddr)
	throws SQLException {


		if (ipaddr.isLoopbackAddress() || ipaddr.getHostAddress().equals("0.0.0.0")) return null;
		
		Category log = ThreadCategory.getInstance(getClass());
		
		int atnodeid = -1;
		int atifindex = -1;
		
		PreparedStatement stmt = dbConn.prepareStatement(SQL_GET_NODEID_IFINDEX_IPINT);
	
		stmt.setString(1, ipaddr.getHostAddress());

		if (log.isDebugEnabled()) 
			log.debug("getNodeidIfindexFromIp: executing SQL Statement " + SQL_GET_NODEID_IFINDEX_IPINT + " with ip address=" + ipaddr.getHostAddress());
		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			return null;
		}
		
		atnodeid = rs.getInt("nodeid");
		if (rs.wasNull()) {
			return null;
		}
		// save info for DiscoveryLink
		AtInterface ati = new AtInterface(atnodeid,ipaddr.getHostAddress());

		// get ifindex if exists
		atifindex = rs.getInt("ifindex");
		if (rs.wasNull()) {
			if (log.isInfoEnabled())
				log.info("getNodeidIfindexFromIp: nodeid "+ atnodeid +" no ifindex (-1) found for ipaddress "
						+ ipaddr + ".");
		} else {
			if (log.isInfoEnabled())
				log.info("getNodeidIfindexFromIp: nodeid "+ atnodeid +" ifindex " + atifindex + " found for ipaddress "
						+ ipaddr + ".");
			ati.setIfindex(atifindex);
		}
		
		return ati;
		
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
			log.debug("getSnmpIfType: executing query "
					+ SQL_GET_SNMPIFTYPE + " with nodeid=" + nodeid + " and ifindex=" + ifindex);

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getSnmpIfType: no entries found in snmpinterface");
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
			log
					.debug("getSnmpIfType: found in snmpinterface snmpiftype="
							+ snmpiftype);

		stmt.close();

		return snmpiftype;

	}
	
	private int getIfIndexByName(Connection dbConn,
			int nodeid, String ifName) throws SQLException {

		Category log = ThreadCategory.getInstance(getClass());

		PreparedStatement stmt = null;
		stmt = dbConn.prepareStatement(SQL_GET_IFINDEX_SNMPINTERFACE_NAME);
		stmt.setInt(1, nodeid);
		stmt.setString(2, ifName);
		stmt.setString(3, ifName);
		if (log.isDebugEnabled())
			log.debug("getIfIndexByName: executing query"
					+ SQL_GET_IFINDEX_SNMPINTERFACE_NAME + "nodeid =" + nodeid + "and ifName=" + ifName);

		ResultSet rs = stmt.executeQuery();

		if (!rs.next()) {
			rs.close();
			stmt.close();
			if (log.isDebugEnabled())
				log
						.debug("getIfIndexByName: no entries found in snmpinterface");
			return -1;
		}

		// extract the values.
		//
		int ndx = 1;

		if (rs.wasNull()) {
			
			if (log.isDebugEnabled())
				log
						.debug("getIfIndexByName: no entries found in snmpinterface");
			return -1;
			
		}

		int ifindex = rs.getInt(ndx++);

		if (log.isDebugEnabled())
			log.debug("getIfIndexByName: found ifindex="
					+ ifindex);

		stmt.close();

		return ifindex;
	}

	public LinkableNode getLinkableNode() {
		return m_node;
	}
	
	private void sendNewSuspectEvent(InetAddress ipaddress) {
		if (m_snmpcoll.isAutoDiscoveryEnabled())

			Linkd.getInstance().sendNewSuspectEvent(ipaddress.getHostAddress(), m_snmpcoll.getSnmpIpPrimary().getHostAddress());
	}
}
