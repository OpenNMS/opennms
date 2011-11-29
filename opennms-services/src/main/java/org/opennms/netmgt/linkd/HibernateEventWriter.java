/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AtInterfaceDao;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.IpRouteInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.StpInterfaceDao;
import org.opennms.netmgt.dao.StpNodeDao;
import org.opennms.netmgt.dao.VlanDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsVlan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

public class HibernateEventWriter extends AbstractQueryManager implements InitializingBean {
	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private IpInterfaceDao m_ipInterfaceDao;
	
	@Autowired
	private SnmpInterfaceDao m_snmpInterfaceDao;

	@Autowired
	private AtInterfaceDao m_atInterfaceDao;

	@Autowired
	private VlanDao m_vlanDao;
	
	@Autowired
	private StpNodeDao m_stpNodeDao;

	@Autowired
	private StpInterfaceDao m_stpInterfaceDao;

	@Autowired
	private IpRouteInterfaceDao m_ipRouteInterfaceDao;

	@Autowired
	private DataLinkInterfaceDao m_dataLinkInterfaceDao;

	@Autowired
	private PlatformTransactionManager m_transactionManager;

	// SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = j.nodeid WHERE nodetype = 'A' AND issnmpprimary = 'P'
	@Override
	public List<LinkableNode> getSnmpNodeList() throws SQLException {
		final List<LinkableNode> nodes = new ArrayList<LinkableNode>();
		
		final OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("ipInterfaces", "iface", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("type", "A"));
        criteria.add(Restrictions.eq("iface.isSnmpPrimary", PrimaryType.PRIMARY));
        for (final OnmsNode node : m_nodeDao.findMatching(criteria)) {
            final String sysObjectId = node.getSysObjectId();
            nodes.add(new LinkableNode(node.getId(), node.getPrimaryInterface().getIpAddress(), sysObjectId == null? "-1" : sysObjectId));
        }

        return nodes;
	}

	// SELECT nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE node.nodeid = ? AND nodetype = 'A' AND issnmpprimary = 'P'
	@Override
	public LinkableNode getSnmpNode(final int nodeid) throws SQLException {
		final OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        criteria.createAlias("ipInterfaces", "iface", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("type", "A"));
        criteria.add(Restrictions.eq("iface.isSnmpPrimary", PrimaryType.PRIMARY));
        criteria.add(Restrictions.eq("id", nodeid));
        final List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);

        if (nodes.size() > 0) {
        	final OnmsNode node = nodes.get(0);
        	final String sysObjectId = node.getSysObjectId();
			return new LinkableNode(node.getId(), node.getPrimaryInterface().getIpAddress(), sysObjectId == null? "-1" : sysObjectId);
        } else {
        	return null;
        }
	}

	@Override
	public void updateDeletedNodes() throws SQLException {
		// UPDATE atinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'
		m_atInterfaceDao.markDeletedIfNodeDeleted();
		m_atInterfaceDao.flush();

        // UPDATE vlan set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'
		m_vlanDao.markDeletedIfNodeDeleted();
        m_vlanDao.flush();

        // UPDATE stpnode set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'
        m_stpNodeDao.markDeletedIfNodeDeleted();
        m_stpNodeDao.flush();

        // UPDATE stpinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'
        m_stpInterfaceDao.markDeletedIfNodeDeleted();
        m_stpInterfaceDao.flush();

        // UPDATE iprouteinterface set status = 'D' WHERE nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'
        m_ipRouteInterfaceDao.markDeletedIfNodeDeleted();
        m_ipRouteInterfaceDao.flush();

        // UPDATE datalinkinterface set status = 'D' WHERE (nodeid IN (SELECT nodeid from node WHERE nodetype = 'D' ) OR nodeparentid IN (SELECT nodeid from node WHERE nodetype = 'D' )) AND status <> 'D'
        m_dataLinkInterfaceDao.markDeletedIfNodeDeleted();
        m_dataLinkInterfaceDao.flush();
	}

    @Override
    protected void markOldDataInactive(final Connection dbConn, final Timestamp scanTime, final int nodeid) throws SQLException {
        // UPDATE atinterface set status = 'N'  WHERE sourcenodeid = ? AND lastpolltime < ? AND status = 'A'
        m_atInterfaceDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_atInterfaceDao.flush();

        // UPDATE vlan set status = 'N'  WHERE nodeid =? AND lastpolltime < ? AND status = 'A'
        m_vlanDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_vlanDao.flush();

        // UPDATE iprouteinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'
        m_ipRouteInterfaceDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_ipRouteInterfaceDao.flush();

        // UPDATE stpnode set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'
        m_stpNodeDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_stpNodeDao.flush();

        // UPDATE stpinterface set status = 'N'  WHERE nodeid = ? AND lastpolltime < ? AND status = 'A'
        m_stpInterfaceDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_stpInterfaceDao.flush();
    }
    
	@Override
	public LinkableNode storeSnmpCollection(final LinkableNode node, final SnmpCollection snmpColl) throws SQLException {
		final Timestamp scanTime = new Timestamp(System.currentTimeMillis());
        if (snmpColl.hasIpNetToMediaTable()) {
            processIpNetToMediaTable(node, snmpColl, null, scanTime);
        } else {
            LogUtils.debugf(this, "storeSnmpCollection: hasIpNetToMediaTable = false");
        }

        if (snmpColl.hasCdpCacheTable()) {
            processCdpCacheTable(node, snmpColl, null, scanTime);
        } else {
            LogUtils.debugf(this, "storeSnmpCollection: hasCdpCacheTable = false");
        }

        if (snmpColl.hasRouteTable()) {
            processRouteTable(node, snmpColl, null, scanTime);
        } else {
            LogUtils.debugf(this, "storeSnmpCollection: hasRouteTable = false");
        }

        if (snmpColl.hasVlanTable()) {
            processVlanTable(node, snmpColl, null, scanTime);
        } else {
            LogUtils.debugf(this, "storeSnmpCollection: hasVlanTable = false");
        }

        for (final OnmsVlan vlan : snmpColl.getSnmpVlanCollections().keySet()) {
            LogUtils.debugf(this, "storeSnmpCollection: parsing VLAN %s/%s", vlan.getVlanId(), vlan.getVlanName());

            final SnmpVlanCollection snmpVlanColl = snmpColl.getSnmpVlanCollections().get(vlan);

            if (snmpVlanColl.hasDot1dBase()) {
                processDot1DBase(node, snmpColl, null, null, scanTime, vlan, snmpVlanColl);
            }
        }

        markOldDataInactive(null, scanTime, node.getNodeId());
        
        return node;
	}

	@Override
	public void storeDiscoveryLink(final DiscoveryLink discoveryLink) throws SQLException {
	    final Timestamp now = new Timestamp(System.currentTimeMillis());

	    for (final NodeToNodeLink lk : discoveryLink.getLinks()) {

	        DataLinkInterface iface = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(lk.getNodeId(), lk.getIfindex());
	        if (iface == null) {
	            final OnmsNode onmsNode = m_nodeDao.get(lk.getNodeId());
	            iface = new DataLinkInterface(onmsNode, lk.getIfindex(), lk.getNodeparentid(), lk.getParentifindex(), String.valueOf(DbDataLinkInterfaceEntry.STATUS_ACTIVE), now);
	        }
	        iface.setNodeParentId(lk.getNodeparentid());
	        iface.setParentIfIndex(lk.getParentifindex());
	        iface.setStatus(String.valueOf(DbDataLinkInterfaceEntry.STATUS_ACTIVE));
	        iface.setLastPollTime(now);

	        m_dataLinkInterfaceDao.saveOrUpdate(iface);

	        final DataLinkInterface parent = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(lk.getNodeparentid(), lk.getParentifindex());
	        if (parent != null) {
	            if (parent.getNodeParentId() == lk.getNodeId() && parent.getParentIfIndex() == lk.getIfindex()
	                    && parent.getStatus().equals(String.valueOf(DbDataLinkInterfaceEntry.STATUS_DELETED))) {
	                parent.setStatus(String.valueOf(DbDataLinkInterfaceEntry.STATUS_DELETED));
	                m_dataLinkInterfaceDao.saveOrUpdate(parent);
	            }
            }
	    }

	    for (final MacToNodeLink lkm : discoveryLink.getMacLinks()) {
	        final Collection<OnmsAtInterface> atInterfaces = m_atInterfaceDao.findByMacAddress(lkm.getMacAddress());
	        if (atInterfaces.size() == 0) {
                LogUtils.debugf(this, "storeDiscoveryLink: No nodeid found on DB for mac address %s on link. Skipping.", lkm.getMacAddress());
                continue;
	        }
	        
	        if (atInterfaces.size() > 1) {
	            LogUtils.debugf(this, "storeDiscoveryLink: More than one atInterface returned for the mac address %s. Returning the first.", lkm.getMacAddress());
	        }

	        final OnmsAtInterface atInterface = atInterfaces.iterator().next();
	        
	        if (!m_linkd.isInterfaceInPackage(atInterface.getIpAddress(), discoveryLink.getPackageName())) {
	            LogUtils.debugf(this, "storeDiscoveryLink: IP address %s not found on link.  Skipping.", atInterface.getIpAddress());
	            continue;
	        }
	        
	        final OnmsNode atInterfaceNode = atInterface.getNode();
	        DataLinkInterface dli = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(atInterfaceNode.getId(), atInterface.getIfIndex());
            if (dli == null) {
                dli = new DataLinkInterface(atInterfaceNode, atInterface.getIfIndex(), lkm.getNodeparentid(), lkm.getParentifindex(), String.valueOf(DbDataLinkInterfaceEntry.STATUS_ACTIVE), now);
            }
            dli.setNodeParentId(lkm.getNodeparentid());
            dli.setParentIfIndex(lkm.getParentifindex());
            dli.setStatus(String.valueOf(DbDataLinkInterfaceEntry.STATUS_ACTIVE));
            dli.setLastPollTime(now);
            m_dataLinkInterfaceDao.saveOrUpdate(dli);

            LogUtils.debugf(this, "storeDiscoveryLink: Storing %s", dli);
            m_dataLinkInterfaceDao.deactivateIfOlderThan(now);
	    }
	}

	@Override
	public void update(final int nodeid, final char action) throws SQLException {
	    m_vlanDao.setStatusForNode(nodeid, action);
	    m_atInterfaceDao.setStatusForNode(nodeid, action);
	    m_ipRouteInterfaceDao.setStatusForNode(nodeid, action);
	    m_stpNodeDao.setStatusForNode(nodeid, action);
	    m_stpInterfaceDao.setStatusForNode(nodeid, action);
	    m_dataLinkInterfaceDao.setStatusForNode(nodeid, action);
	}

	@Override
	public void updateForInterface(final int nodeid, final String ipAddr, final int ifIndex, final char action) throws SQLException {
	    if (!EventUtils.isNonIpInterface(ipAddr)) {
	        m_atInterfaceDao.setStatusForNodeAndIp(nodeid, ipAddr, action);
	    }
	    if (ifIndex > -1) {
	        m_atInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	        m_stpInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	        m_ipRouteInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	        m_dataLinkInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	    }
	}

	// SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND (snmpifname = ? OR snmpifdescr = ?)
	@Override
	protected int getIfIndexByName(final Connection dbConn, final int targetCdpNodeId, final String cdpTargetDevicePort) throws SQLException {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", targetCdpNodeId));
        criteria.add(Restrictions.or(Restrictions.eq("ifName", cdpTargetDevicePort), Restrictions.eq("ifDescr", cdpTargetDevicePort)));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(criteria);

        if (interfaces.isEmpty()) {
        	return -1;
        } else {
        	if (interfaces.size() > 1) {
        		LogUtils.debugf(this, "getIfIndexByName: More than one SnmpInterface matches nodeId %d and snmpIfName/snmpIfDescr %s", targetCdpNodeId, cdpTargetDevicePort);
        	}
        	return interfaces.get(0).getIfIndex();
        }
	}

	// SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?
	@Override
	protected int getNodeidFromIp(final Connection dbConn, final InetAddress cdpTargetIpAddr) throws SQLException {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("ipAddress", cdpTargetIpAddr));
        criteria.add(Restrictions.eq("node.type", "A"));
        List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(criteria);
        
        if (interfaces.isEmpty()) {
        	return -1;
        } else {
        	if (interfaces.size() > 1) {
        		LogUtils.debugf(this, "getNodeidFromIp: More than one node matches ipAddress %s", str(cdpTargetIpAddr));
        	}
        	final OnmsNode node = interfaces.get(0).getNode();
        	if (node == null) return -1;
			return node.getId();
        }
	}

	// SELECT node.nodeid,snmpinterface.snmpifindex,snmpinterface.snmpipadentnetmask FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid LEFT JOIN snmpinterface ON ipinterface.snmpinterfaceid = snmpinterface.id WHERE node.nodetype = 'A' AND ipinterface.ipaddr = ?
	@Override
	protected RouterInterface getNodeidMaskFromIp(final Connection dbConn, final InetAddress nexthop) throws SQLException {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("snmpInterface", "snmpInterface", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("ipAddress", nexthop));
        criteria.add(Restrictions.eq("node.type", "A"));
        final List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(criteria);
		
        if (interfaces.isEmpty()) {
        	return null;
        } else {
        	if (interfaces.size() > 1) {
        		LogUtils.debugf(this, "getNodeidMaskFromIp: More than one IP Interface matches ipAddress %s", str(nexthop));
        	}
        	final OnmsIpInterface ipInterface = interfaces.get(0);
        	final OnmsNode node = ipInterface.getNode();
			final OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();

			if (node == null) {
			    LogUtils.warnf(this, "getNodeidMaskFromIp: No node associated with OnmsIpInterface: %s", ipInterface);
			    return null;
			}
			if (snmpInterface == null) {
			    LogUtils.warnf(this, "getNodeidMaskFromIp: No SNMP interface associated with OnmsIpInterface: %s", ipInterface);
			    return null;
			}

			return new RouterInterface(node.getId(), snmpInterface.getIfIndex(), snmpInterface.getNetMask());
        }
	}

	// SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?
	@Override
	protected RouterInterface getNodeFromIp(final Connection dbConn, final InetAddress nexthop) throws SQLException {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("ipAddress", nexthop));
        criteria.add(Restrictions.eq("node.type", "A"));
        final List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(criteria);
		
        if (interfaces.isEmpty()) {
        	return null;
        } else {
        	if (interfaces.size() > 1) {
        		LogUtils.debugf(this, "getNodeFromIp: More than one IP Interface matches ipAddress %s", str(nexthop));
        	}
        	final OnmsIpInterface ipInterface = interfaces.get(0);
        	final OnmsNode node = ipInterface.getNode();

			if (node == null) {
			    LogUtils.warnf(this, "getNodeFromIp: No node associated with OnmsIpInterface: %s", ipInterface);
			    return null;
			}

			int ifIndex = -1;

			// the existing Linkd code always put -1 in the ifIndex here, but we should probably fill it in if we know it
			/*
			final OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();
			if (snmpInterface != null) {
				ifIndex = snmpInterface.getIfIndex();
			}
			*/

			return new RouterInterface(node.getId(), ifIndex);
        }
	}

	// SELECT snmpiftype FROM snmpinterface WHERE nodeid = ? AND snmpifindex = ?"
	@Override
	protected int getSnmpIfType(final Connection dbConn, final int nodeId, final Integer ifIndex) throws SQLException {
	    LogUtils.debugf(this, "getSnmpIfType(%d, %s)", nodeId, ifIndex);
	    OnmsSnmpInterface snmpInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex);
	    if (snmpInterface == null) {
	        return -1;
	    } else {
	        return snmpInterface.getIfType();
	    }
	}

    @Override
    protected List<String> getPhysAddrs(int nodeId, DBUtils d, Connection dbConn) throws SQLException {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", nodeId));
        
        final List<String> addrs = new ArrayList<String>();

        for (final OnmsSnmpInterface snmpInterface : m_snmpInterfaceDao.findMatching(criteria)) {
            addrs.add(snmpInterface.getPhysAddr());
        }

        return addrs;
    }

	@Override
	protected synchronized void saveIpRouteInterface(final Connection dbConn, final OnmsIpRouteInterface route) throws SQLException {
	    new UpsertTemplate<OnmsIpRouteInterface, IpRouteInterfaceDao>(m_transactionManager, m_ipRouteInterfaceDao) {

	        @Override
	        protected OnmsIpRouteInterface query() {
	            return m_ipRouteInterfaceDao.findByNodeAndDest(route.getNode().getId(), route.getRouteDest());
	        }

	        @Override
	        protected OnmsIpRouteInterface doUpdate(OnmsIpRouteInterface ipRouteInterface) {
	            ipRouteInterface.setLastPollTime(route.getLastPollTime());
	            //ipRouteInterface.setRouteDest(route.getRouteDest());
	            ipRouteInterface.setRouteIfIndex(route.getRouteIfIndex());
	            ipRouteInterface.setRouteMask(route.getRouteMask());
	            ipRouteInterface.setRouteMetric1(route.getRouteMetric1());
	            ipRouteInterface.setRouteMetric2(route.getRouteMetric2());
	            ipRouteInterface.setRouteMetric3(route.getRouteMetric3());
	            ipRouteInterface.setRouteMetric4(route.getRouteMetric4());
	            ipRouteInterface.setRouteMetric5(route.getRouteMetric5());
	            ipRouteInterface.setRouteNextHop(route.getRouteNextHop());
	            ipRouteInterface.setRouteProto(route.getRouteProto());
	            ipRouteInterface.setRouteType(route.getRouteType());
	            ipRouteInterface.setStatus(route.getStatus());
	            m_ipRouteInterfaceDao.update(ipRouteInterface);
	            return ipRouteInterface;
	        }

	        @Override
	        protected OnmsIpRouteInterface doInsert() {
	            m_ipRouteInterfaceDao.save(route);
	            return route;
	        }
	    }.execute();
	}

	@Override
	protected void saveVlan(final Connection dbConn, final OnmsVlan v) throws SQLException {
	    new UpsertTemplate<OnmsVlan, VlanDao>(m_transactionManager, m_vlanDao) {

	        @Override
	        protected OnmsVlan query() {
	            return m_vlanDao.findByNodeAndVlan(v.getNode().getId(), v.getVlanId());
	        }

	        @Override
	        protected OnmsVlan doUpdate(OnmsVlan vlan) {
	            // Make sure that the fields used in the query match
	            Assert.isTrue(vlan.getNode().equals(v.getNode()));
	            Assert.isTrue(vlan.getVlanId().equals(v.getVlanId()));

	            if (vlan.getId() == null && v.getId() != null) {
	                vlan.setId(v.getId());
	            }
	            vlan.setLastPollTime(v.getLastPollTime());
	            vlan.setStatus(v.getStatus());
	            vlan.setVlanName(v.getVlanName());
	            vlan.setVlanStatus(v.getVlanStatus());
	            vlan.setVlanType(v.getVlanType());
	            m_vlanDao.update(v);
	            m_vlanDao.flush();
	            return vlan;
	        }

	        @Override
	        protected OnmsVlan doInsert() {
	            m_vlanDao.save(v);
	            m_vlanDao.flush();
	            return v;
	        }
	    }.execute();
	}

	@Override
	protected synchronized void saveStpNode(final Connection dbConn, final OnmsStpNode stp) throws SQLException {
	    OnmsStpNode stpNode = m_stpNodeDao.findByNodeAndVlan(stp.getNode().getId(), stp.getBaseVlan());
	    if (stpNode == null) {
	        stpNode = stp;
	    } else {
	        stpNode.setBaseBridgeAddress(stp.getBaseBridgeAddress());
	        stpNode.setBaseNumPorts(stp.getBaseNumPorts());
	        stpNode.setBaseType(stp.getBaseType());
	        stpNode.setBaseVlan(stp.getBaseVlan());
	        stpNode.setBaseVlanName(stp.getBaseVlanName());
	        stpNode.setLastPollTime(stp.getLastPollTime());
	        stpNode.setNode(stp.getNode());
	        stpNode.setStatus(stp.getStatus());
	        stpNode.setStpDesignatedRoot(stp.getStpDesignatedRoot());
	        stpNode.setStpPriority(stp.getStpPriority());
	        stpNode.setStpProtocolSpecification(stp.getStpProtocolSpecification());
	        stpNode.setStpRootCost(stp.getStpRootCost());
	        stpNode.setStpRootPort(stp.getStpRootPort());
	    }
		m_stpNodeDao.saveOrUpdate(stpNode);
	}

    @Override
    protected void saveStpInterface(final Connection dbConn, final OnmsStpInterface stp) throws SQLException {
        OnmsStpInterface stpInterface = m_stpInterfaceDao.findByNodeAndVlan(stp.getNode().getId(), stp.getBridgePort(), stp.getVlan());
        if (stpInterface == null) {
            stpInterface = stp;
        } else {
            stpInterface.setBridgePort(stp.getBridgePort());
            stpInterface.setIfIndex(stp.getIfIndex());
            stpInterface.setLastPollTime(stp.getLastPollTime());
            stpInterface.setNode(stp.getNode());
            stpInterface.setStatus(stp.getStatus());
            stpInterface.setStpPortDesignatedBridge(stp.getStpPortDesignatedBridge());
            stpInterface.setStpPortDesignatedCost(stp.getStpPortDesignatedCost());
            stpInterface.setStpPortDesignatedPort(stp.getStpPortDesignatedPort());
            stpInterface.setStpPortDesignatedRoot(stp.getStpPortDesignatedRoot());
            stpInterface.setStpPortPathCost(stp.getStpPortPathCost());
            stpInterface.setStpPortState(stp.getStpPortState());
            stpInterface.setVlan(stp.getVlan());
        }
        m_stpInterfaceDao.saveOrUpdate(stpInterface);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_atInterfaceDao);
        Assert.notNull(m_dataLinkInterfaceDao);
        Assert.notNull(m_ipInterfaceDao);
        Assert.notNull(m_ipRouteInterfaceDao);
        Assert.notNull(m_nodeDao);
        Assert.notNull(m_snmpInterfaceDao);
        Assert.notNull(m_stpInterfaceDao);
        Assert.notNull(m_stpNodeDao);
        Assert.notNull(m_transactionManager);
        Assert.notNull(m_vlanDao);
        LogUtils.debugf(this, "Initialized %s", this.getClass().getSimpleName());
    }

    @Override
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(final IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(final SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public AtInterfaceDao getAtInterfaceDao() {
        return m_atInterfaceDao;
    }

    public void setAtInterfaceDao(final AtInterfaceDao atInterfaceDao) {
        m_atInterfaceDao = atInterfaceDao;
    }

    public VlanDao getVlanDao() {
        return m_vlanDao;
    }

    public void setVlanDao(final VlanDao vlanDao) {
        m_vlanDao = vlanDao;
    }

    public StpNodeDao getStpNodeDao() {
        return m_stpNodeDao;
    }

    public void setStpNodeDao(final StpNodeDao stpNodeDao) {
        m_stpNodeDao = stpNodeDao;
    }

    public StpInterfaceDao getStpInterfaceDao() {
        return m_stpInterfaceDao;
    }

    public void setStpInterfaceDao(final StpInterfaceDao stpInterfaceDao) {
        m_stpInterfaceDao = stpInterfaceDao;
    }

    public IpRouteInterfaceDao getIpRouteInterfaceDao() {
        return m_ipRouteInterfaceDao;
    }

    public void setIpRouteInterfaceDao(final IpRouteInterfaceDao ipRouteInterfaceDao) {
        m_ipRouteInterfaceDao = ipRouteInterfaceDao;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(final DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

}
