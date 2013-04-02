/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.opennms.core.utils.BeanUtils;
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
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.PrimaryType;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
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
	public List<LinkableNode> getSnmpNodeList() {
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
	public LinkableNode getSnmpNode(final int nodeid) {
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
	public void updateDeletedNodes() {
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
    protected void markOldDataInactive(final Date scanTime, final int nodeid) {
        // UPDATE atinterface set status = 'N'  WHERE sourcenodeid = ? AND lastpolltime < ? AND status = 'A'
        m_atInterfaceDao.deactivateForSourceNodeIdIfOlderThan(nodeid, scanTime);
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
    protected void deleteOlderData(final Date scanTime, final int nodeid) {
        m_atInterfaceDao.deleteForNodeSourceIdIfOlderThan(nodeid, scanTime);
        m_atInterfaceDao.flush();

        m_vlanDao.deleteForNodeIdIfOlderThan(nodeid, scanTime);
        m_vlanDao.flush();

        m_ipRouteInterfaceDao.deleteForNodeIdIfOlderThan(nodeid, scanTime);
        m_ipRouteInterfaceDao.flush();

        m_stpNodeDao.deleteForNodeIdIfOlderThan(nodeid, scanTime);
        m_stpNodeDao.flush();

        m_stpInterfaceDao.deleteForNodeIdIfOlderThan(nodeid, scanTime);
        m_stpInterfaceDao.flush();
    }

	@Override
	@Transactional
	public LinkableNode storeSnmpCollection(final LinkableNode node, final SnmpCollection snmpColl) {
		final Date scanTime = new Date();
	
		final OnmsNode onmsNode = getNode(node.getNodeId());
        if (onmsNode == null) {
            LogUtils.debugf(this, "no node found!");
            return null;
        }
        
		LogUtils.debugf(this, "storeSnmpCollection: ospf hasOspfGeneralGroup/hasOspfNbrTable: %b/%b", snmpColl.hasOspfGeneralGroup(),snmpColl.hasOspfNbrTable());
		if (snmpColl.hasOspfGeneralGroup() && snmpColl.hasOspfNbrTable()) {
		    processOspf(node,snmpColl,scanTime);
		}
	        
		LogUtils.debugf(this, "storeSnmpCollection: lldp hasLldpLocalGroup/hasLldpLocTable/haLldpRemTable: %b/%b/%b", snmpColl.hasLldpLocalGroup() ,snmpColl.hasLldpLocTable() ,snmpColl.hasLldpRemTable());
	        if (snmpColl.hasLldpLocalGroup()) {
		        processLldp(node,snmpColl,scanTime);
		}
        
        LogUtils.debugf(this, "storeSnmpCollection: hasIpNetToMediaTable: %b", snmpColl.hasIpNetToMediaTable());
        if (snmpColl.hasIpNetToMediaTable()) {
            processIpNetToMediaTable(node, snmpColl,scanTime);
        }

        LogUtils.debugf(this, "storeSnmpCollection: hasCdpCacheTable: %b", snmpColl.hasCdpCacheTable());
        if (snmpColl.hasCdpGlobalGroup() && snmpColl.hasCdpCacheTable()) {
            processCdp(node, snmpColl, scanTime);
        }

        LogUtils.debugf(this, "storeSnmpCollection: hasRouteTable: %b", snmpColl.hasRouteTable());
        if (snmpColl.hasRouteTable()) {
            processRouteTable(onmsNode,node, snmpColl,scanTime);
        }

        LogUtils.debugf(this, "storeSnmpCollection: hasVlanTable: %b", snmpColl.hasVlanTable());
        if (snmpColl.hasVlanTable()) {
            processVlanTable(onmsNode,node, snmpColl,scanTime);
        }

        for (final OnmsVlan vlan : snmpColl.getSnmpVlanCollections().keySet()) {
            LogUtils.debugf(this, "storeSnmpCollection: parsing bridge data on VLAN %s/%s", vlan.getVlanId(), vlan.getVlanName());
            storeSnmpVlanCollection(onmsNode, node, vlan, snmpColl.getSnmpVlanCollections().get(vlan), scanTime);
        }

        markOldDataInactive(scanTime, node.getNodeId());
        deleteOlderData(new Date(scanTime.getTime()-snmpColl.getPollInterval()*3),node.getNodeId());
        
        return node;
	}

	@Override
    public void storeDiscoveryLink(final DiscoveryLink discoveryLink)
         {
        final Date now = new Date();

        for (final NodeToNodeLink lk : discoveryLink.getLinks()) {
            DataLinkInterface iface = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(lk.getNodeId(),
                                                                                    lk.getIfindex());
            if (iface == null) {
                final OnmsNode onmsNode = m_nodeDao.get(lk.getNodeId());
                iface = new DataLinkInterface(
                                              onmsNode,
                                              lk.getIfindex(),
                                              lk.getNodeparentid(),
                                              lk.getParentifindex(),
                                              StatusType.ACTIVE,
                                              now);
            }
            iface.setNodeParentId(lk.getNodeparentid());
            iface.setParentIfIndex(lk.getParentifindex());
            iface.setLastPollTime(now);
            m_dataLinkInterfaceDao.saveOrUpdate(iface);
            final DataLinkInterface parent = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(lk.getNodeparentid(),
                                                                                           lk.getParentifindex());
            if (parent != null) {
                if (parent.getNodeParentId() == lk.getNodeId()
                        && parent.getParentIfIndex() == lk.getIfindex()
                        && parent.getStatus().equals(StatusType.DELETED)) {
                    m_dataLinkInterfaceDao.delete(parent);
                }
            }
        }

        for (final MacToNodeLink lkm : discoveryLink.getMacLinks()) {
            final Collection<OnmsAtInterface> atInterfaces = m_atInterfaceDao.findByMacAddress(lkm.getMacAddress());
            if (atInterfaces.size() == 0) {
                LogUtils.debugf(this,
                                "storeDiscoveryLink: No nodeid found on DB for mac address %s on link. Skipping.",
                                lkm.getMacAddress());
                continue;
            }
            if (atInterfaces.size() > 1) {
                LogUtils.debugf(this,
                                "storeDiscoveryLink: More than one atInterface returned for the mac address %s. Returning the first.",
                                lkm.getMacAddress());
            }
            final OnmsAtInterface atInterface = atInterfaces.iterator().next();
            if (!m_linkd.isInterfaceInPackage(atInterface.getIpAddress(),
                                              discoveryLink.getPackageName())) {
                LogUtils.debugf(this,
                                "storeDiscoveryLink: IP address %s not found on link.  Skipping.",
                                atInterface.getIpAddress());
                continue;
            }
            final OnmsNode atInterfaceNode = atInterface.getNode();
            DataLinkInterface dli = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(atInterfaceNode.getId(),
                                                                                  atInterface.getIfIndex());
            if (dli == null) {
                dli = new DataLinkInterface(
                                            atInterfaceNode,
                                            atInterface.getIfIndex(),
                                            lkm.getNodeparentid(),
                                            lkm.getParentifindex(),
                                            StatusType.ACTIVE,
                                            now);
            }
            dli.setNodeParentId(lkm.getNodeparentid());
            dli.setParentIfIndex(lkm.getParentifindex());
            dli.setLastPollTime(now);
            m_dataLinkInterfaceDao.saveOrUpdate(dli);
            LogUtils.debugf(this, "storeDiscoveryLink: Storing %s", dli);
        }
        m_dataLinkInterfaceDao.deactivateIfOlderThan(now,getLinkd().getSource());
        m_dataLinkInterfaceDao.deleteIfOlderThan(new Date(now.getTime()-3*discoveryLink.getSnmpPollInterval()),getLinkd().getSource());
    }

	@Override
	public void update(final int nodeid, final StatusType action) {
	    m_vlanDao.setStatusForNode(nodeid, action);
	    m_atInterfaceDao.setStatusForNode(nodeid, action);
	    m_ipRouteInterfaceDao.setStatusForNode(nodeid, action);
	    m_stpNodeDao.setStatusForNode(nodeid, action);
	    m_stpInterfaceDao.setStatusForNode(nodeid, action);
	    m_dataLinkInterfaceDao.setStatusForNode(nodeid, getLinkd().getSource(), action);
	}

	@Override
	public void updateForInterface(final int nodeid, final String ipAddr, final int ifIndex, final StatusType action)  {
	    if (!(ipAddr == null || ipAddr.length() == 0 || "0.0.0.0".equals(ipAddr))) {
	        m_atInterfaceDao.setStatusForNodeAndIp(nodeid, ipAddr, action);
	    }
	    if (ifIndex > -1) {
	        m_atInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	        m_stpInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	        m_ipRouteInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, action);
	        m_dataLinkInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, getLinkd().getSource(), action);
	    }
	}

	// SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND (snmpifname = ? OR snmpifdescr = ?)
	@Override
	protected int getIfIndexByName(final int targetCdpNodeId, final String cdpTargetDevicePort) {
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
	protected List<Integer> getNodeidFromIp(final InetAddress cdpTargetIpAddr) {
        List<Integer> nodeids = new ArrayList<Integer>();
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("ipAddress", cdpTargetIpAddr));
        criteria.add(Restrictions.eq("node.type", "A"));
        List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(criteria);
        
        LogUtils.debugf(this, "getNodeidFromIp: Found %d nodeids matching " +
        		"ipAddress %s", interfaces.size(),str(cdpTargetIpAddr));
        for (final OnmsIpInterface ipinterface : interfaces) {
            nodeids.add(ipinterface.getNode().getId());
        }
        return nodeids;
	}

	// SELECT node.nodeid,snmpinterface.snmpifindex,snmpinterface.snmpipadentnetmask FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid LEFT JOIN snmpinterface ON ipinterface.snmpinterfaceid = snmpinterface.id WHERE node.nodetype = 'A' AND ipinterface.ipaddr = ?
	@Override
	protected List<RouterInterface> getRouteInterface(final InetAddress nexthop, int ifindex) {
        
        List<RouterInterface> routes = new ArrayList<RouterInterface>();

        final List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findByIpAddress(str(nexthop));
		
        LogUtils.debugf(this, "getRouteInterface: Found %d interface matching " +
            		"ipAddress %s", interfaces.size(),str(nexthop));

        for (OnmsIpInterface ipInterface : interfaces) {
			RouterInterface route = null;
        	final OnmsNode node = ipInterface.getNode();
			final OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();
			if (snmpInterface == null || snmpInterface.getNetMask() == null) {
				route = new RouterInterface(node.getId(), -1);
			} else {
				route = new RouterInterface(node.getId(), snmpInterface.getIfIndex(), snmpInterface.getNetMask());
			}
			route.setNextHop(nexthop);
			route.setIfindex(ifindex);
            LogUtils.debugf(this, "getRouteInterface: adding %s route interface" ,route.toString());
			routes.add(route);
        }
        return routes;
	}

	// SELECT snmpiftype FROM snmpinterface WHERE nodeid = ? AND snmpifindex = ?"
	@Override
	protected int getSnmpIfType(final int nodeId, final Integer ifIndex) {
		Integer snmpIfType = -1;
	    OnmsSnmpInterface snmpInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex);
	    if (snmpInterface != null) {
	        snmpIfType = snmpInterface.getIfType();
	    }
	    LogUtils.debugf(this, "getSnmpIfType(%d, %s), found %d.", nodeId, ifIndex, snmpIfType);
	    return snmpIfType;
	}

    @Override
    protected List<String> getPhysAddrs(int nodeId) {
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
    protected synchronized void saveIpRouteInterface(final OnmsIpRouteInterface saveMe) {
        new UpsertTemplate<OnmsIpRouteInterface, IpRouteInterfaceDao>(m_transactionManager, m_ipRouteInterfaceDao) {

            @Override
            protected OnmsIpRouteInterface query() {
                return m_dao.findByNodeAndDest(saveMe.getNode().getId(), saveMe.getRouteDest());
            }

            @Override
            protected OnmsIpRouteInterface doUpdate(OnmsIpRouteInterface updateMe) {
                // Make sure that the fields used in the query match
                Assert.isTrue(updateMe.getNode().compareTo(saveMe.getNode()) == 0);
                Assert.isTrue(updateMe.getRouteDest().equals(saveMe.getRouteDest()));

                if (updateMe.getId() == null && saveMe.getId() != null) {
                    updateMe.setId(saveMe.getId());
                }
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                //updateMe.setRouteDest(saveMe.getRouteDest());
                updateMe.setRouteIfIndex(saveMe.getRouteIfIndex());
                updateMe.setRouteMask(saveMe.getRouteMask());
                updateMe.setRouteMetric1(saveMe.getRouteMetric1());
                updateMe.setRouteMetric2(saveMe.getRouteMetric2());
                updateMe.setRouteMetric3(saveMe.getRouteMetric3());
                updateMe.setRouteMetric4(saveMe.getRouteMetric4());
                updateMe.setRouteMetric5(saveMe.getRouteMetric5());
                updateMe.setRouteNextHop(saveMe.getRouteNextHop());
                updateMe.setRouteProto(saveMe.getRouteProto());
                updateMe.setRouteType(saveMe.getRouteType());
                updateMe.setStatus(saveMe.getStatus());

                m_dao.update(updateMe);
                m_dao.flush();
                return updateMe;
            }

            @Override
            protected OnmsIpRouteInterface doInsert() {
                m_dao.save(saveMe);
                m_dao.flush();
                return saveMe;
            }
        }.execute();
    }

    @Override
    protected void saveVlan(final OnmsVlan saveMe) {
        new UpsertTemplate<OnmsVlan, VlanDao>(m_transactionManager, m_vlanDao) {

            @Override
            protected OnmsVlan query() {
                return m_dao.findByNodeAndVlan(saveMe.getNode().getId(), saveMe.getVlanId());
            }

            @Override
            protected OnmsVlan doUpdate(OnmsVlan updateMe) {
                // Make sure that the fields used in the query match
                Assert.isTrue(updateMe.getNode().compareTo(saveMe.getNode()) == 0);
                Assert.isTrue(updateMe.getVlanId().equals(saveMe.getVlanId()));

                if (updateMe.getId() == null && saveMe.getId() != null) {
                    updateMe.setId(saveMe.getId());
                }
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                updateMe.setStatus(saveMe.getStatus());
                updateMe.setVlanName(saveMe.getVlanName());
                updateMe.setVlanStatus(saveMe.getVlanStatus());
                updateMe.setVlanType(saveMe.getVlanType());

                m_dao.update(updateMe);
                m_dao.flush();
                return updateMe;
            }

            @Override
            protected OnmsVlan doInsert() {
                m_dao.save(saveMe);
                m_dao.flush();
                return saveMe;
            }
        }.execute();
    }

    @Override
    protected synchronized void saveStpNode(final OnmsStpNode saveMe) {
        new UpsertTemplate<OnmsStpNode, StpNodeDao>(m_transactionManager, m_stpNodeDao) {

            @Override
            protected OnmsStpNode query() {
                return m_dao.findByNodeAndVlan(saveMe.getNode().getId(), saveMe.getBaseVlan());
            }

            @Override
            protected OnmsStpNode doUpdate(OnmsStpNode updateMe) {
                // Make sure that the fields used in the query match
                Assert.isTrue(updateMe.getNode().compareTo(saveMe.getNode()) == 0);
                Assert.isTrue(updateMe.getBaseVlan().equals(saveMe.getBaseVlan()));

                if (updateMe.getId() == null && saveMe.getId() != null) {
                    updateMe.setId(saveMe.getId());
                }
                updateMe.setBaseBridgeAddress(saveMe.getBaseBridgeAddress());
                updateMe.setBaseNumPorts(saveMe.getBaseNumPorts());
                updateMe.setBaseType(saveMe.getBaseType());
                //updateMe.setBaseVlan(saveMe.getBaseVlan());
                updateMe.setBaseVlanName(saveMe.getBaseVlanName());
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                //updateMe.setNode(saveMe.getNode());
                updateMe.setStatus(saveMe.getStatus());
                updateMe.setStpDesignatedRoot(saveMe.getStpDesignatedRoot());
                updateMe.setStpPriority(saveMe.getStpPriority());
                updateMe.setStpProtocolSpecification(saveMe.getStpProtocolSpecification());
                updateMe.setStpRootCost(saveMe.getStpRootCost());
                updateMe.setStpRootPort(saveMe.getStpRootPort());

                m_dao.update(updateMe);
                m_dao.flush();
                return updateMe;
            }

            @Override
            protected OnmsStpNode doInsert() {
                m_dao.save(saveMe);
                m_dao.flush();
                return saveMe;
            }
        }.execute();
	}

    @Override
    protected void saveStpInterface(final OnmsStpInterface saveMe) {
        new UpsertTemplate<OnmsStpInterface, StpInterfaceDao>(m_transactionManager, m_stpInterfaceDao) {

            @Override
            protected OnmsStpInterface query() {
                return m_dao.findByNodeAndVlan(saveMe.getNode().getId(), saveMe.getBridgePort(), saveMe.getVlan());
            }

            @Override
            protected OnmsStpInterface doUpdate(OnmsStpInterface updateMe) {
                // Make sure that the fields used in the query match
                Assert.isTrue(updateMe.getNode().compareTo(saveMe.getNode()) == 0);
                Assert.isTrue(updateMe.getBridgePort().equals(saveMe.getBridgePort()));
                Assert.isTrue(updateMe.getVlan().equals(saveMe.getVlan()));

                if (updateMe.getId() == null && saveMe.getId() != null) {
                    updateMe.setId(saveMe.getId());
                }
                //updateMe.setBridgePort(saveMe.getBridgePort());
                updateMe.setIfIndex(saveMe.getIfIndex());
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                //updateMe.setNode(saveMe.getNode());
                updateMe.setStatus(saveMe.getStatus());
                updateMe.setStpPortDesignatedBridge(saveMe.getStpPortDesignatedBridge());
                updateMe.setStpPortDesignatedCost(saveMe.getStpPortDesignatedCost());
                updateMe.setStpPortDesignatedPort(saveMe.getStpPortDesignatedPort());
                updateMe.setStpPortDesignatedRoot(saveMe.getStpPortDesignatedRoot());
                updateMe.setStpPortPathCost(saveMe.getStpPortPathCost());
                updateMe.setStpPortState(saveMe.getStpPortState());
                //updateMe.setVlan(saveMe.getVlan());

                m_dao.update(updateMe);
                m_dao.flush();
                return updateMe;
            }

            @Override
            protected OnmsStpInterface doInsert() {
                m_dao.save(saveMe);
                m_dao.flush();
                return saveMe;
            }
        }.execute();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
    
    @Transactional
    public Integer getFromSysnameIpAddress(String lldpRemSysname,
            InetAddress lldpRemPortid) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.sysName", lldpRemSysname));
        criteria.add(Restrictions.eq("ipAddress",lldpRemPortid));
        final List<OnmsIpInterface> interfaces = getIpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()) {
            return interfaces.get(0).getIfIndex();
        }
        return null;
    }

}
