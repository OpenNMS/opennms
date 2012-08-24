/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AtInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsVlan;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <P>
 * This class is used to store informations owned by SnmpCollection and
 * DiscoveryLink Classes in DB. When saving SNMP Collection it populate Bean
 * LinkableNode with information for DiscoveryLink. It performs data test for
 * DiscoveryLink. Also take correct action on DB tables in case node is deleted
 * service SNMP is discovered, service SNMP is Lost and Regained Also this class
 * holds
 * </P>
 *
 * @author antonio
 * @version $Id: $
 */
public class DbEventWriter extends AbstractQueryManager {

    private JdbcTemplate jdbcTemplate;

    private NodeDao m_nodeDao;

    private IpInterfaceDao m_ipInterfaceDao;
    
    private SnmpInterfaceDao m_snmpInterfaceDao;

    private AtInterfaceDao m_atInterfaceDao;

    /**
     * Query to select info for specific node
     */
    private static final String SQL_SELECT_SNMP_NODE = "SELECT nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE node.nodeid = ? AND nodetype = 'A' AND issnmpprimary = 'P'";

    private static final String SQL_GET_NODEID = "SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?";

    private static final String SQL_GET_NODEID__IFINDEX_MASK = "SELECT node.nodeid,snmpinterface.snmpifindex,snmpinterface.snmpipadentnetmask FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid LEFT JOIN snmpinterface ON ipinterface.snmpinterfaceid = snmpinterface.id WHERE node.nodetype = 'A' AND ipinterface.ipaddr = ?";

    private static final String SQL_UPDATE_DATALINKINTERFACE = "UPDATE datalinkinterface set status = 'N'  WHERE lastpolltime < ? AND status = 'A'";

    private static final String SQL_UPDATE_ATINTERFACE = "UPDATE atinterface set status = 'N'  WHERE sourcenodeid = ? AND lastpolltime < ? AND status = 'A'";

    private static final String SQL_UPDATE_IPROUTEINTERFACE = "UPDATE iprouteinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

    private static final String SQL_UPDATE_STPNODE = "UPDATE stpnode set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

    private static final String SQL_UPDATE_STPINTERFACE = "UPDATE stpinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'";

    private static final String SQL_UPDATE_VLAN = "UPDATE vlan set status = 'N'  WHERE nodeid =? AND lastpolltime < ? AND status = 'A'";

    private static final String SQL_UPDATE_ATINTERFACE_STATUS = "UPDATE atinterface set status = ?  WHERE sourcenodeid = ? OR nodeid = ?";

    private static final String SQL_UPDATE_IPROUTEINTERFACE_STATUS = "UPDATE iprouteinterface set status = ? WHERE nodeid = ? ";

    private static final String SQL_UPDATE_STPNODE_STATUS = "UPDATE stpnode set status = ?  WHERE nodeid = ? ";

    private static final String SQL_UPDATE_STPINTERFACE_STATUS = "UPDATE stpinterface set status = ? WHERE nodeid = ? ";

    private static final String SQL_UPDATE_VLAN_STATUS = "UPDATE vlan set status = ?  WHERE nodeid = ? ";

    private static final String SQL_UPDATE_DATALINKINTERFACE_STATUS = "UPDATE datalinkinterface set status = ? WHERE nodeid = ? OR nodeparentid = ? ";

    // private static final String SQL_GET_NODEID_IFINDEX =
    // "SELECT atinterface.nodeid, atinterface.ipaddr, snmpinterface.snmpifindex from atinterface left JOIN snmpinterface ON atinterface.nodeid = snmpinterface.nodeid AND atinterface.ipaddr = snmpinterface.ipaddr WHERE atphysaddr = ? AND status = 'A'";
    private static final String SQL_GET_NODEID_IFINDEX = "SELECT atinterface.nodeid, atinterface.ipaddr, ipinterface.ifindex from atinterface left JOIN ipinterface ON atinterface.nodeid = ipinterface.nodeid AND atinterface.ipaddr = ipinterface.ipaddr WHERE atphysaddr = ? AND atinterface.status <> 'D'";

    private static final String SQL_GET_SNMPIFTYPE = "SELECT snmpiftype FROM snmpinterface WHERE nodeid = ? AND snmpifindex = ?";

    private static final String SQL_GET_IFINDEX_SNMPINTERFACE_NAME = "SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND (snmpifname = ? OR snmpifdescr = ?) ";

    /**
     * query to select SNMP nodes
     */
    private static final String SQL_SELECT_SNMP_NODES = "SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND issnmpprimary = 'P'";

    /**
     * update status to D on node marked as Deleted on table Nodes
     */
    private static final String SQL_UPDATE_VLAN_D = "UPDATE vlan set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D' ";

    private static final String SQL_UPDATE_ATINTERFACE_D = "UPDATE atinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D' ";

    private static final String SQL_UPDATE_STPNODE_D = "UPDATE stpnode set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

    private static final String SQL_UPDATE_STPINTERFACE_D = "UPDATE stpinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

    private static final String SQL_UPDATE_IPROUTEINTERFACE_D = "UPDATE iprouteinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'";

    private static final String SQL_UPDATE_DATALINKINTERFACE_D = "UPDATE datalinkinterface set status = 'D' WHERE (nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) OR nodeparentid IN (SELECT nodeid from node WHERE nodetype = 'D' )) AND status <> 'D'";

    private static final String SQL_GET_IFINDEX_FROM_SYSNAME_IPADDRESS = "SELECT ifindex FROM ipinterface ip LEFT JOIN node n ON n.nodeid=ip.nodeid WHERE n.nodesysname = ? AND ip.ipaddr = ?";
    /**
     * <p>Constructor for DbEventWriter.</p>
     */
    public DbEventWriter() {

    }

    private Connection getConnection() throws SQLException {
        return jdbcTemplate.getDataSource().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public void storeDiscoveryLink(final DiscoveryLink discovery) throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection dbConn = getConnection();
            d.watch(dbConn);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            PreparedStatement stmt = null;
            ResultSet rs = null;

            NodeToNodeLink[] links = discovery.getLinks();
    
            LogUtils.debugf(this, "storelink: Storing %d NodeToNodeLink information into database", links.length);
            for (final NodeToNodeLink lk : discovery.getLinks()) {
                final int nodeid = lk.getNodeId();
                final int ifindex = lk.getIfindex();
                final int nodeparentid = lk.getNodeparentid();
                final int parentifindex = lk.getParentifindex();
    
                DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry.get(dbConn, nodeid, ifindex);
                if (dbentry == null) {
                    // Create a new entry
                    dbentry = DbDataLinkInterfaceEntry.create(nodeid, ifindex);
                }
                dbentry.updateNodeParentId(nodeparentid);
                dbentry.updateParentIfIndex(parentifindex);
                dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_ACTIVE);
                dbentry.set_lastpolltime(now);
    
                dbentry.store(dbConn);
    
                // now parsing symmetrical and setting to D if necessary
    
                dbentry = DbDataLinkInterfaceEntry.get(dbConn, nodeparentid, parentifindex);
    
                if (dbentry != null) {
                    if (dbentry.get_nodeparentid() == nodeid && dbentry.get_parentifindex() == ifindex
                            && dbentry.get_status() != DbDataLinkInterfaceEntry.STATUS_DELETED) {
                        dbentry.updateStatus(DbDataLinkInterfaceEntry.STATUS_DELETED);
                        dbentry.store(dbConn);
                    }
                }
            }
    
            MacToNodeLink[] linkmacs = discovery.getMacLinks();
    
            LogUtils.debugf(this, "storelink: Storing " + linkmacs.length + " MacToNodeLink information into database");
            for (int i = 0; i < linkmacs.length; i++) {
    
                MacToNodeLink lkm = linkmacs[i];
                String macaddr = lkm.getMacAddress();
    
                LogUtils.debugf(this, "storelink: finding nodeid,ifindex on DB using MAC address: " + macaddr);
    
                stmt = dbConn.prepareStatement(SQL_GET_NODEID_IFINDEX);
                d.watch(stmt);
    
                stmt.setString(1, macaddr);
    
                rs = stmt.executeQuery();
                d.watch(rs);
    
                LogUtils.debugf(this, "storelink: finding nodeid,ifindex on DB, SQL statement: " + SQL_GET_NODEID_IFINDEX + " with MAC address " + macaddr);
    
                if (!rs.next()) {
                    LogUtils.debugf(this, "storelink: no nodeid found on DB for MAC address " + macaddr + " on link. .... Skipping");
                    continue;
                }
    
                // extract the values.
                //
                int ndx = 1;
    
                int nodeid = rs.getInt(ndx++);
                if (rs.wasNull()) {
                    LogUtils.debugf(this, "storelink: no nodeid found on DB for MAC address " + macaddr + " on link. .... Skipping");
                    continue;
                }
    
                String ipaddrString = rs.getString(ndx++);
                if (rs.wasNull()) {
                    LogUtils.debugf(this, "storelink: no ipaddr found on DB for MAC address " + macaddr + " on link. .... Skipping");
                    continue;
                }
    
                InetAddress ipaddr = addr(ipaddrString);
                if (!m_linkd.isInterfaceInPackage(ipaddr, discovery.getPackageName())) {
                    LogUtils.debugf(this, "storelink: not in package ipaddr found: " + ipaddr + " on link. .... Skipping");
                    continue;
    
                }
                int ifindex = rs.getInt(ndx++);
                if (rs.wasNull()) {
                    LogUtils.debugf(this, "storelink: no ifindex found on DB for MAC address " + macaddr + " on link.");
                    ifindex = -1;
                }
    
                int nodeparentid = lkm.getNodeparentid();
                int parentifindex = lkm.getParentifindex();
                DbDataLinkInterfaceEntry dbentry = DbDataLinkInterfaceEntry.get(dbConn, nodeid, ifindex);
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
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE);
            d.watch(stmt);
            stmt.setTimestamp(1, now);
    
            int i = stmt.executeUpdate();
            LogUtils.debugf(this, "storelink: datalinkinterface - updated to NOT ACTIVE status " + i + " rows ");
        } finally {
            d.cleanUp();
        }
    }

    /** {@inheritDoc} */
    @Override
    public LinkableNode storeSnmpCollection(LinkableNode node, SnmpCollection snmpcoll) throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection dbConn = getConnection();
            d.watch(dbConn);
            Timestamp scanTime = new Timestamp(System.currentTimeMillis());
    
            if (snmpcoll.hasLldpLocalGroup() && snmpcoll.hasLldpLocTable() && snmpcoll.hasLldpRemTable()) {
                processLldp(node,snmpcoll,dbConn,scanTime);
            }
            
            if (snmpcoll.hasIpNetToMediaTable()) {
                processIpNetToMediaTable(node, snmpcoll, dbConn, scanTime);
            }
    
            if (snmpcoll.hasCdpCacheTable()) {
                processCdpCacheTable(node, snmpcoll, dbConn, scanTime);
            }
    
            if (snmpcoll.hasRouteTable()) {
                processRouteTable(node, snmpcoll, dbConn, scanTime);
            }
    
            if (snmpcoll.hasVlanTable()) {
                processVlanTable(node, snmpcoll, dbConn, scanTime);
            }
    
            LogUtils.debugf(this, "store: saving SnmpVlanCollection's in DB");

            for (final OnmsVlan vlan : snmpcoll.getSnmpVlanCollections().keySet()) {
    
            	LogUtils.debugf(this, "store: parsing VLAN %s/%s", vlan.getVlanId(), vlan.getVlanName());

                final SnmpVlanCollection snmpVlanColl = snmpcoll.getSnmpVlanCollections().get(vlan);
    
                if (snmpVlanColl.hasDot1dBase()) {
                	processDot1DBase(node, snmpcoll, d, dbConn, scanTime, vlan, snmpVlanColl);
                }
            }

            markOldDataInactive(dbConn, scanTime, node.getNodeId());
    
            return node;
        } catch (Throwable e) {
            LogUtils.errorf(this, e, "Unexpected exception while storing SNMP collections: %s", e.getMessage());
            return null;
        } finally {
            d.cleanUp();
        }

    }

    @Override
    protected void markOldDataInactive(final Connection dbConn, final Timestamp now, final int nodeid) throws SQLException {

        final DBUtils d = new DBUtils(getClass());

        try {
            PreparedStatement stmt = null;
    
            int i = 0;
            stmt = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setTimestamp(2, now);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "store: SQL statement " + SQL_UPDATE_ATINTERFACE + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_VLAN);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setTimestamp(2, now);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "store: SQL statement " + SQL_UPDATE_VLAN + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setTimestamp(2, now);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "store: SQL statement " + SQL_UPDATE_IPROUTEINTERFACE + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_STPNODE);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setTimestamp(2, now);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "store: SQL statement " + SQL_UPDATE_STPNODE + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setTimestamp(2, now);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "store: SQL statement " + SQL_UPDATE_STPINTERFACE + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
        } finally {
            d.cleanUp();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void update(int nodeid, char status) throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection dbConn = getConnection();
            d.watch(dbConn);
            PreparedStatement stmt = null;
    
            int i = 0;
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_VLAN_STATUS);
            d.watch(stmt);
            stmt.setString(1, new String(new char[] { status }));
            stmt.setInt(2, nodeid);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "update: SQL statement " + SQL_UPDATE_VLAN_STATUS + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE_STATUS);
            d.watch(stmt);
            stmt.setString(1, new String(new char[] { status }));
            stmt.setInt(2, nodeid);
            stmt.setInt(3, nodeid);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "update: SQL statement " + SQL_UPDATE_ATINTERFACE_STATUS + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE_STATUS);
            d.watch(stmt);
            stmt.setString(1, new String(new char[] { status }));
            stmt.setInt(2, nodeid);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "update: SQL statement " + SQL_UPDATE_IPROUTEINTERFACE_STATUS + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_STPNODE_STATUS);
            d.watch(stmt);
            stmt.setString(1, new String(new char[] { status }));
            stmt.setInt(2, nodeid);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "update: SQL statement " + SQL_UPDATE_STPNODE_STATUS + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE_STATUS);
            d.watch(stmt);
            stmt.setString(1, new String(new char[] { status }));
            stmt.setInt(2, nodeid);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "update: SQL statement " + SQL_UPDATE_STPINTERFACE_STATUS + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
    
            stmt = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE_STATUS);
            d.watch(stmt);
            stmt.setString(1, new String(new char[] { status }));
            stmt.setInt(2, nodeid);
            stmt.setInt(3, nodeid);
    
            i = stmt.executeUpdate();
            LogUtils.debugf(this, "update: SQL statement " + SQL_UPDATE_DATALINKINTERFACE_STATUS + ". " + i + " rows UPDATED for nodeid=" + nodeid + ".");
        } finally {
            d.cleanUp();
        }

    }

    @Override
    protected List<Integer> getNodeidFromIp(Connection dbConn, InetAddress ipaddr) throws SQLException {

        List<Integer> nodeids = new ArrayList<Integer>();

        final String hostAddress = str(ipaddr);
        final DBUtils d = new DBUtils(getClass());
        try {
        	final PreparedStatement stmt = dbConn.prepareStatement(SQL_GET_NODEID);
            d.watch(stmt);
            stmt.setString(1, hostAddress);
    
            LogUtils.debugf(this, "getNodeidFromIp: executing query " + SQL_GET_NODEID + " with IP address=" + hostAddress);
    
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            if (!rs.next()) {
                LogUtils.debugf(this, "getNodeidFromIp: no entries found in ipinterface");
                return nodeids;
            }
            // extract the values.
            //
            while (rs.next()) {                    
            // get the node id
            //
                int nodeid = rs.getInt("nodeid");
                nodeids.add(nodeid);
    
                LogUtils.debugf(this, "getNodeidFromIp: found nodeid " + nodeid);
            }
        } finally {
            d.cleanUp();
        }

        return nodeids;

    }

    @Override
    protected RouterInterface getNodeidMaskFromIp(Connection dbConn, InetAddress ipaddr) throws SQLException {
        final String hostAddress = str(ipaddr);
		if (ipaddr.isLoopbackAddress() || hostAddress.equals("0.0.0.0")) return null;

        int nodeid = -1;
        int ifindex = -1;
        String netmask = null;

        PreparedStatement stmt = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_GET_NODEID__IFINDEX_MASK);
            d.watch(stmt);
            stmt.setString(1, hostAddress);
    
            LogUtils.debugf(this, "getNodeidMaskFromIp: executing query " + SQL_GET_NODEID__IFINDEX_MASK + " with IP address=" + hostAddress);
    
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            if (!rs.next()) {
                LogUtils.debugf(this, "getNodeidMaskFromIp: no entries found in snmpinterface");
                return null;
            }
            // extract the values.
            //
            // get the node id
            //
            nodeid = rs.getInt("nodeid");
            if (rs.wasNull()) {
                LogUtils.debugf(this, "getNodeidMaskFromIp: no nodeid found");
                return null;
            }
    
            ifindex = rs.getInt("snmpifindex");
            if (rs.wasNull()) {
                LogUtils.debugf(this, "getNodeidMaskFromIp: no snmpifindex found");
                ifindex = -1;
            }
    
            netmask = rs.getString("snmpipadentnetmask");
            if (rs.wasNull()) {
                LogUtils.debugf(this, "getNodeidMaskFromIp: no snmpipadentnetmask found");
                netmask = "255.255.255.255";
            }
        } finally {
            d.cleanUp();
        }

        RouterInterface ri = new RouterInterface(nodeid, ifindex, addr(netmask));
        return ri;

    }

    @Override
    protected RouterInterface getNodeFromIp(Connection dbConn, InetAddress ipaddr) throws SQLException {
        final String hostAddress = str(ipaddr);
		if (ipaddr.isLoopbackAddress() || hostAddress.equals("0.0.0.0")) return null;

        int nodeid = -1;
        int ifindex = -1;

        PreparedStatement stmt = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_GET_NODEID);
            d.watch(stmt);
            stmt.setString(1, hostAddress);
    
            LogUtils.debugf(this, "getNodeFromIp: executing query " + SQL_GET_NODEID + " with IP address=" + hostAddress);
    
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            if (!rs.next()) {
                LogUtils.debugf(this, "getNodeFromIp: no entries found in snmpinterface");
                return null;
            }
            // extract the values.
            //
            // get the node id
            //
            nodeid = rs.getInt("nodeid");
            if (rs.wasNull()) {
                LogUtils.debugf(this, "getNodeFromIp: no nodeid found");
                return null;
            }
        } finally {
            d.cleanUp();
        }

        RouterInterface ri = new RouterInterface(nodeid, ifindex);
        return ri;

    }

    @Override
    protected int getSnmpIfType(Connection dbConn, int nodeid, Integer ifindex) throws SQLException {

        int snmpiftype = -1;
        PreparedStatement stmt = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_GET_SNMPIFTYPE);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setInt(2, ifindex == null? 0 : ifindex);
    
            LogUtils.debugf(this, "getSnmpIfType: executing query " + SQL_GET_SNMPIFTYPE + " with nodeid=" + nodeid + " and ifindex=" + ifindex);
    
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            if (!rs.next()) {
                LogUtils.debugf(this, "getSnmpIfType: no entries found in snmpinterface");
                return -1;
            }
    
            // extract the values.
            //
            int ndx = 1;
    
            // get the node id
            //
            snmpiftype = rs.getInt(ndx++);
            if (rs.wasNull()) snmpiftype = -1;
    
            LogUtils.debugf(this, "getSnmpIfType: found in snmpinterface snmpiftype=" + snmpiftype);
    
            return snmpiftype;
        } finally {
            d.cleanUp();
        }

    }

    @Override
    protected int getIfIndexByName(Connection dbConn, int nodeid, String ifName) throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement stmt = null;
            stmt = dbConn.prepareStatement(SQL_GET_IFINDEX_SNMPINTERFACE_NAME);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setString(2, ifName);
            stmt.setString(3, ifName);
    
            LogUtils.debugf(this, "getIfIndexByName: executing query" + SQL_GET_IFINDEX_SNMPINTERFACE_NAME + "nodeid =" + nodeid + "and ifName=" + ifName);
    
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            if (!rs.next()) {
                LogUtils.debugf(this, "getIfIndexByName: no entries found in snmpinterface");
                return -1;
            }
    
            // extract the values.
            //
            int ndx = 1;
    
            if (rs.wasNull()) {
    
                LogUtils.debugf(this, "getIfIndexByName: no entries found in snmpinterface");
                return -1;
    
            }
    
            int ifindex = rs.getInt(ndx++);
    
            LogUtils.debugf(this, "getIfIndexByName: found ifindex=" + ifindex);
    
            return ifindex;
        } finally {
            d.cleanUp();
        }
    }

    /** {@inheritDoc} */
    @Override
    public LinkableNode getSnmpNode(int nodeid) throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {

        	final Connection dbConn = getConnection();
            d.watch(dbConn);
            LinkableNode node = null;
    
            final PreparedStatement stmt = dbConn.prepareStatement(SQL_SELECT_SNMP_NODE);
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            LogUtils.debugf(this, "getSnmpCollection: execute '" + SQL_SELECT_SNMP_NODE + "' with nodeid = " + nodeid);
    
            final ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            while (rs.next()) {
            	String sysoid = rs.getString("nodesysoid");
                if (sysoid == null) sysoid = "-1";
                String ipaddr = rs.getString("ipaddr");
                LogUtils.debugf(this, "getSnmpCollection: found nodeid " + nodeid + " ipaddr " + ipaddr + " sysoid " + sysoid);
    
                node = new LinkableNode(nodeid, addr(ipaddr), sysoid);
            }
    
            return node;
        } finally {
            d.cleanUp();
        }

    }

    /**
     * <p>getSnmpNodeList</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public List<LinkableNode> getSnmpNodeList() throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {

            Connection dbConn = getConnection();
            d.watch(dbConn);
    
            List<LinkableNode> linknodes = new ArrayList<LinkableNode>();
            PreparedStatement ps = dbConn.prepareStatement(SQL_SELECT_SNMP_NODES);
            d.watch(ps);
    
            ResultSet rs = ps.executeQuery();
            d.watch(rs);
            LogUtils.debugf(this, "getNodesInfo: execute query: \" " + SQL_SELECT_SNMP_NODES + "\"");
    
            while (rs.next()) {
                int nodeid = rs.getInt("nodeid");
                String ipaddr = rs.getString("ipaddr");
                String sysoid = rs.getString("nodesysoid");
                if (sysoid == null) sysoid = "-1";
                LogUtils.debugf(this, "getNodesInfo: found node element: nodeid " + nodeid + " ipaddr " + ipaddr + " sysoid " + sysoid);
    
                LinkableNode node = new LinkableNode(nodeid, addr(ipaddr), sysoid);
                linknodes.add(node);
    
            }
    
            LogUtils.debugf(this, "getNodesInfo: found " + linknodes.size() + " SNMP primary IP nodes");
    
            return linknodes;
        } finally {
            d.cleanUp();
        }
    }

    /**
     * <p>updateDeletedNodes</p>
     *
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void updateDeletedNodes() throws SQLException {

        final DBUtils d = new DBUtils(getClass());
        try {

            Connection dbConn = getConnection();
            d.watch(dbConn);
    
            // update atinterface
            int i = 0;
            PreparedStatement ps = dbConn.prepareStatement(SQL_UPDATE_ATINTERFACE_D);
            d.watch(ps);
            i = ps.executeUpdate();
            LogUtils.infof(this, "updateDeletedNodes: execute '" + SQL_UPDATE_ATINTERFACE_D + "' updated rows: " + i);
    
            // update vlan
            ps = dbConn.prepareStatement(SQL_UPDATE_VLAN_D);
            d.watch(ps);
            i = ps.executeUpdate();
            LogUtils.infof(this, "updateDeletedNodes: execute '" + SQL_UPDATE_VLAN_D + "' updated rows: " + i);
    
            // update stpnode
            ps = dbConn.prepareStatement(SQL_UPDATE_STPNODE_D);
            d.watch(ps);
            i = ps.executeUpdate();
            LogUtils.infof(this, "updateDeletedNodes: execute '" + SQL_UPDATE_STPNODE_D + "' updated rows: " + i);
    
            // update stpinterface
            ps = dbConn.prepareStatement(SQL_UPDATE_STPINTERFACE_D);
            d.watch(ps);
            i = ps.executeUpdate();
            LogUtils.infof(this, "updateDeletedNodes: execute '" + SQL_UPDATE_STPINTERFACE_D + "' updated rows: " + i);
    
            // update iprouteinterface
            ps = dbConn.prepareStatement(SQL_UPDATE_IPROUTEINTERFACE_D);
            d.watch(ps);
            i = ps.executeUpdate();
            LogUtils.infof(this, "updateDeletedNodes: execute '" + SQL_UPDATE_IPROUTEINTERFACE_D + "'updated rows: " + i);
    
            // update datalinkinterface
            ps = dbConn.prepareStatement(SQL_UPDATE_DATALINKINTERFACE_D);
            d.watch(ps);
            i = ps.executeUpdate();
            LogUtils.infof(this, "updateDeletedNodes: execute '" + SQL_UPDATE_DATALINKINTERFACE_D + "' updated rows: " + i);
        } finally {
            d.cleanUp();
        }

    }
    
    /** {@inheritDoc} */
    @Override
    public void updateForInterface(int nodeId, String ipAddr, int ifIndex, char status) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            Connection dbConn = getConnection();
            d.watch(dbConn);
            PreparedStatement ps = null;
            int i=0;
            if(!EventUtils.isNonIpInterface(ipAddr)) {  
                // update atinterface
                ps = dbConn.prepareStatement("UPDATE atinterface set status = ?  WHERE nodeid = ? AND ipaddr = ?");
                d.watch(ps);
                ps.setString(1, new String(new char[] { status }));
                ps.setInt(2, nodeId);
                ps.setString(3, ipAddr);
                i = ps.executeUpdate();
                LogUtils.infof(this, "updateForInterface: atinterface: node = " + nodeId
                               + ", IP Address = " + ipAddr + ", status = " + status + ": updated rows = " + i);
            }
            if(ifIndex > -1) {
                 // update atinterface
                ps = dbConn.prepareStatement("UPDATE atinterface set status = ?  WHERE sourcenodeid = ? AND ifindex = ?");
                d.watch(ps);
                ps.setString(1, new String(new char[] { status }));
                ps.setInt(2, nodeId);
                ps.setInt(3, ifIndex);
                i = ps.executeUpdate();
                LogUtils.infof(this, "updateForInterface: atinterface: source node = " + nodeId
                               + ", ifIndex = " + ifIndex + ", status = " + status + ": updated rows = " + i);
                // update stpinterface
                ps = dbConn.prepareStatement("UPDATE stpinterface set status = ? WHERE nodeid = ? AND ifindex = ?");
                d.watch(ps);
                ps.setString(1, new String(new char[] { status }));
                ps.setInt(2, nodeId);
                ps.setInt(3, ifIndex);
                i = ps.executeUpdate();
                LogUtils.infof(this, "updateForInterface: stpinterface: node = " + nodeId
                               + ", ifIndex = " + ifIndex  + ", status = " + status + ": updated rows = " + i);
    
                // update iprouteinterface
                ps = dbConn.prepareStatement("UPDATE iprouteinterface set status = ? WHERE nodeid = ? AND routeifindex = ?");
                d.watch(ps);
                ps.setString(1, new String(new char[] { status }));
                ps.setInt(2, nodeId);
                ps.setInt(3, ifIndex);
                i = ps.executeUpdate();
                LogUtils.infof(this, "updateForInterface: iprouteinterface: node = " + nodeId
                               + ", rpouteIfIndex = " + ifIndex  + ", status = " + status + ": updated rows = " + i);
    
                // update datalinkinterface
                ps = dbConn.prepareStatement("UPDATE datalinkinterface set status = ? WHERE (nodeid = ? and ifindex = ?) OR (nodeparentid = ? AND parentifindex = ?)");
                d.watch(ps);
                ps.setString(1, new String(new char[] { status }));
                ps.setInt(2, nodeId);
                ps.setInt(3, ifIndex);
                ps.setInt(4, nodeId);
                ps.setInt(5, ifIndex);
                i = ps.executeUpdate();
                LogUtils.infof(this, "updateForInterface: datalinkinterface: node = " + nodeId
                               + ", ifIndex = " + ifIndex  + ", status = " + status + ": updated rows = " + i);
            }
            
        } finally {
            d.cleanUp();
        }
    }

    @Override
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Override
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(final IpInterfaceDao dao) {
        m_ipInterfaceDao = dao;
    }

    @Override
    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }


    @Override
    public AtInterfaceDao getAtInterfaceDao() {
        return m_atInterfaceDao;
    }

    public void setAtInterfaceDao(final AtInterfaceDao dao) {
        m_atInterfaceDao = dao;
    }

    /** {@inheritDoc} */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
	protected void saveIpRouteInterface(final Connection dbConn, OnmsIpRouteInterface ipRouteInterface) throws SQLException {
		DbIpRouteInterfaceEntry iprouteInterfaceEntry = DbIpRouteInterfaceEntry.get(dbConn, ipRouteInterface.getNode().getId(), ipRouteInterface.getRouteDest());
		if (iprouteInterfaceEntry == null) {
		    // Create a new entry
		    iprouteInterfaceEntry = DbIpRouteInterfaceEntry.create(ipRouteInterface.getNode().getId(), ipRouteInterface.getRouteDest());
		}
		// update object
		iprouteInterfaceEntry.updateRouteMask(ipRouteInterface.getRouteMask());
		iprouteInterfaceEntry.updateRouteNextHop(ipRouteInterface.getRouteNextHop());
		iprouteInterfaceEntry.updateIfIndex(ipRouteInterface.getRouteIfIndex());

		// okay to autobox these since we're checking for null
		if (ipRouteInterface.getRouteMetric1() != null) iprouteInterfaceEntry.updateRouteMetric1(ipRouteInterface.getRouteMetric1());
		if (ipRouteInterface.getRouteMetric2() != null) iprouteInterfaceEntry.updateRouteMetric2(ipRouteInterface.getRouteMetric2());
		if (ipRouteInterface.getRouteMetric3() != null) iprouteInterfaceEntry.updateRouteMetric3(ipRouteInterface.getRouteMetric3());
		if (ipRouteInterface.getRouteMetric4() != null) iprouteInterfaceEntry.updateRouteMetric4(ipRouteInterface.getRouteMetric4());
		if (ipRouteInterface.getRouteMetric5() != null) iprouteInterfaceEntry.updateRouteMetric5(ipRouteInterface.getRouteMetric5());
		if (ipRouteInterface.getRouteType()    != null) iprouteInterfaceEntry.updateRouteType(ipRouteInterface.getRouteType());
		if (ipRouteInterface.getRouteProto()   != null) iprouteInterfaceEntry.updateRouteProto(ipRouteInterface.getRouteProto());
		iprouteInterfaceEntry.updateStatus(DbAtInterfaceEntry.STATUS_ACTIVE);
		iprouteInterfaceEntry.set_lastpolltime(ipRouteInterface.getLastPollTime());

		// store object in database
		iprouteInterfaceEntry.store(dbConn);
	}

    @Override
	protected void saveVlan(final Connection dbConn, final OnmsVlan vlan) throws SQLException {
		// always save info to DB
		DbVlanEntry vlanEntry = DbVlanEntry.get(dbConn, vlan.getNode().getId(), vlan.getVlanId());
		if (vlanEntry == null) {
		    // Create a new entry
		    vlanEntry = DbVlanEntry.create(vlan.getNode().getId(), vlan.getVlanId());
		}

		if (vlan.getVlanType() != null) {
			vlanEntry.updateVlanType(vlan.getVlanType());
		}
		if (vlan.getVlanStatus() != null) {
			vlanEntry.updateVlanStatus(vlan.getVlanStatus());
		}
		vlanEntry.updateVlanName(vlan.getVlanName());
		vlanEntry.updateStatus(vlan.getStatus());
		vlanEntry.set_lastpolltime(vlan.getLastPollTime());

		// store object in database
		vlanEntry.store(dbConn);
	}

    @Override
	protected void saveStpNode(final Connection dbConn, final OnmsStpNode stpNode) throws SQLException {
		DbStpNodeEntry dbStpNodeEntry = DbStpNodeEntry.get(dbConn, stpNode.getNode().getId(), stpNode.getBaseVlan());
		if (dbStpNodeEntry == null) {
		    dbStpNodeEntry = DbStpNodeEntry.create(stpNode.getNode().getId(), stpNode.getBaseVlan());
		}

		dbStpNodeEntry.updateBaseBridgeAddress(stpNode.getBaseBridgeAddress());
		dbStpNodeEntry.updateBaseNumPorts(stpNode.getBaseNumPorts());
		dbStpNodeEntry.updateBaseType(stpNode.getBaseType());
		dbStpNodeEntry.updateBaseVlanName(stpNode.getBaseVlanName());
		dbStpNodeEntry.updateStpProtocolSpecification(stpNode.getStpProtocolSpecification());
		dbStpNodeEntry.updateStpPriority(stpNode.getStpPriority());
		dbStpNodeEntry.updateStpRootCost(stpNode.getStpRootCost());
		dbStpNodeEntry.updateStpRootPort(stpNode.getStpRootPort());
		dbStpNodeEntry.updateStpDesignatedRoot(stpNode.getStpDesignatedRoot());
		dbStpNodeEntry.updateStatus(stpNode.getStatus());
		dbStpNodeEntry.set_lastpolltime(stpNode.getLastPollTime());
		dbStpNodeEntry.store(dbConn);
	}

    @Override
    protected void saveStpInterface(final Connection dbConn, final OnmsStpInterface stpInterface) throws SQLException {
        DbStpInterfaceEntry dbStpIntEntry = DbStpInterfaceEntry.get(dbConn, stpInterface.getNode().getId(), stpInterface.getBridgePort(), stpInterface.getVlan());
        if (dbStpIntEntry == null) {
            // Create a new entry
            dbStpIntEntry = DbStpInterfaceEntry.create(stpInterface.getNode().getId(), stpInterface.getBridgePort(), stpInterface.getVlan());
        }

        if (stpInterface.getIfIndex() != null) {
            dbStpIntEntry.updateIfIndex(stpInterface.getIfIndex());
        }
        dbStpIntEntry.updateStpportDesignatedBridge(stpInterface.getStpPortDesignatedBridge());
        dbStpIntEntry.updateStpPortDesignatedCost(stpInterface.getStpPortDesignatedCost());
        dbStpIntEntry.updateStpportDesignatedPort(stpInterface.getStpPortDesignatedPort());
        dbStpIntEntry.updateStpportDesignatedRoot(stpInterface.getStpPortDesignatedRoot());
        dbStpIntEntry.updateStpPortPathCost(stpInterface.getStpPortPathCost());
        dbStpIntEntry.updateStpPortState(stpInterface.getStpPortState());
        
        dbStpIntEntry.updateStatus(stpInterface.getStatus());
        dbStpIntEntry.set_lastpolltime(stpInterface.getLastPollTime());
        dbStpIntEntry.store(dbConn);
    }

    @Override
    protected List<String> getPhysAddrs(final int nodeId, final DBUtils d, final Connection dbConn) throws SQLException {
        final List<String> physaddrs = new ArrayList<String>();

        // now adding bridge identifier MAC addresses of switch from snmpinterface
        final PreparedStatement stmt = dbConn.prepareStatement("SELECT snmpphysaddr FROM snmpinterface WHERE nodeid = ? AND  snmpphysaddr <> ''");
        d.watch(stmt);
        stmt.setInt(1, nodeId);

        final ResultSet rs = stmt.executeQuery();
        d.watch(rs);

        while (rs.next()) {
            String macaddr = rs.getString("snmpphysaddr");
            if (macaddr == null) continue;
            physaddrs.add(macaddr);
            LogUtils.debugf(this, "setBridgeIdentifierFromSnmpInterface: found bridge identifier " + macaddr + " from snmpinterface db table");
        }
        return physaddrs;
    }

    @Override
    protected Integer getFromSysnameIpAddress(String lldpRemSysname,
            InetAddress lldpRemPortid) {
        final DBUtils d = new DBUtils(getClass());
        int ifindex = -1;
        try {
            Connection dbConn = getConnection();
            PreparedStatement stmt = null;
            stmt = dbConn.prepareStatement(SQL_GET_IFINDEX_FROM_SYSNAME_IPADDRESS);
            d.watch(stmt);
            stmt.setString(1, lldpRemSysname);
            stmt.setString(2, lldpRemPortid.getHostAddress());
    
            LogUtils.debugf(this, "getFromSysnameIpAddress: executing query" + SQL_GET_IFINDEX_FROM_SYSNAME_IPADDRESS + " nodeSysname=" + lldpRemSysname + "and ipAddr=" + lldpRemPortid);
    
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    
            if (!rs.next()) {
                LogUtils.debugf(this, "getFromSysnameIpAddress: no entries found in ipinterface");
                return -1;
            }
    
            // extract the values.
            //
            int ndx = 1;
    
            if (rs.wasNull()) {
    
                LogUtils.debugf(this, "getFromSysnameIpAddress: no entries found in snmpinterface");
                return -1;
    
            }
    
            ifindex = rs.getInt(ndx++);
    
            LogUtils.debugf(this, "getFromSysnameIpAddress: found ifindex=" + ifindex);
    
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            d.cleanUp();
        }
        return Integer.valueOf(ifindex);
    }

}
