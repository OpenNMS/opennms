/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpRouteInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.StpInterfaceDao;
import org.opennms.netmgt.dao.api.StpNodeDao;
import org.opennms.netmgt.dao.api.VlanDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;
import org.opennms.netmgt.model.topology.NodeToNodeLink;
import org.opennms.netmgt.model.topology.RouterInterface;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.PrimaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class HibernateEventWriter extends AbstractQueryManager implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventWriter.class);
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
    public List<LinkableSnmpNode> getSnmpNodeList() {
        final List<LinkableSnmpNode> nodes = new ArrayList<LinkableSnmpNode>();

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("ipInterfaces", "iface", JoinType.LEFT_JOIN);
        builder.eq("type", "A");
        builder.eq("iface.isSnmpPrimary", PrimaryType.PRIMARY);
        for (final OnmsNode node : m_nodeDao.findMatching(builder.toCriteria())) {
            final String sysObjectId = node.getSysObjectId();
            nodes.add(new LinkableSnmpNode(node.getId(), node.getPrimaryInterface().getIpAddress(), sysObjectId == null? "-1" : sysObjectId, node.getSysName()));
        }

        return nodes;
    }

    // SELECT nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE node.nodeid = ? AND nodetype = 'A' AND issnmpprimary = 'P'
    @Override
    public LinkableSnmpNode getSnmpNode(final int nodeid) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("ipInterfaces", "iface", JoinType.LEFT_JOIN);
        builder.eq("type", "A");
        builder.eq("iface.isSnmpPrimary", PrimaryType.PRIMARY);
        builder.eq("id", nodeid);
        final List<OnmsNode> nodes = m_nodeDao.findMatching(builder.toCriteria());

        if (nodes.size() > 0) {
            final OnmsNode node = nodes.get(0);
            final String sysObjectId = node.getSysObjectId();
            return new LinkableSnmpNode(node.getId(), node.getPrimaryInterface().getIpAddress(), sysObjectId == null? "-1" : sysObjectId, node.getSysName());
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
            LOG.debug("no node found!");
            return null;
        }
        
        LOG.debug("storeSnmpCollection: wifi hasMtxrWlRtabTable: {}", snmpColl.hasMtxrWlRtabTable());
        if (snmpColl.hasMtxrWlRtabTable()) {
            processWifi(node,snmpColl,scanTime);
        }
        
        LOG.debug("storeSnmpCollection: ospf hasOspfGeneralGroup/hasOspfNbrTable: {}/{}", snmpColl.hasOspfGeneralGroup(),snmpColl.hasOspfNbrTable());
        if (snmpColl.hasOspfGeneralGroup() && snmpColl.hasOspfNbrTable()) {
            processOspf(node,snmpColl,scanTime);
        }

        LOG.debug("storeSnmpCollection: isis hasIsIsSystemObjectGroup/hasIsisCircTable/hasIsisISAdjTable: {}/{}/{}", snmpColl.hasIsIsSysObjectGroup(),snmpColl.hasIsisCircTable(),snmpColl.hasIsisISAdjTable());
        if (snmpColl.hasIsIsSysObjectGroup() && snmpColl.hasIsisCircTable() && snmpColl.hasIsisISAdjTable()) {
            processIsis(node,snmpColl,scanTime);
        }

        LOG.debug("storeSnmpCollection: lldp hasLldpLocalGroup/hasLldpLocTable/haLldpRemTable: {}/{}/{}", snmpColl.hasLldpLocalGroup() ,snmpColl.hasLldpLocTable() ,snmpColl.hasLldpRemTable());
        if (snmpColl.hasLldpLocalGroup()) {
            processLldp(node,snmpColl,scanTime);
        }

        LOG.debug("storeSnmpCollection: hasIpNetToMediaTable: {}", snmpColl.hasIpNetToMediaTable());
        if (snmpColl.hasIpNetToMediaTable()) {
            processIpNetToMediaTable(node, snmpColl,scanTime);
        }

        LOG.debug("storeSnmpCollection: hasCdpGlobalGroup: {}", snmpColl.hasCdpGlobalGroup());
        LOG.debug("storeSnmpCollection: hasCdpCacheTable: {}", snmpColl.hasCdpCacheTable());
        if (snmpColl.hasCdpGlobalGroup() && snmpColl.hasCdpCacheTable()) {
            processCdp(node, snmpColl, scanTime);
        }

        LOG.debug("storeSnmpCollection: hasRouteTable: {}", snmpColl.hasRouteTable());
        if (snmpColl.hasRouteTable()) {
            processRouteTable(onmsNode,node, snmpColl,scanTime);
        }

        LOG.debug("storeSnmpCollection: hasVlanTable: {}", snmpColl.hasVlanTable());
        if (snmpColl.hasVlanTable()) {
            processVlanTable(onmsNode,node, snmpColl,scanTime);
        }

        if (!snmpColl.getSnmpVlanCollections().isEmpty()) {
            node.setMacIdentifiers(getPhysAddrs(node.getNodeId()));
            for (final OnmsVlan vlan : snmpColl.getSnmpVlanCollections().keySet()) {
                LOG.debug("storeSnmpCollection: parsing bridge data on VLAN {}/{}", vlan.getVlanId(), vlan.getVlanName());
                storeSnmpVlanCollection(onmsNode, node, vlan, snmpColl.getSnmpVlanCollections().get(vlan), scanTime);
            }
        }

        markOldDataInactive(scanTime, node.getNodeId());
        deleteOlderData(new Date(scanTime.getTime()-snmpColl.getPollInterval()*3),node.getNodeId());

        return node;
    }

    private DataLinkInterface getDatabaseLink(Collection<DataLinkInterface> links, int nodeparentid,int parentifindex, DiscoveryProtocol protocol) {
        for (DataLinkInterface link: links) {
            if (link.getNodeParentId().intValue() == nodeparentid && link.getParentIfIndex().intValue() == parentifindex && link.getProtocol() == protocol ) {
                LOG.info("storeDiscoveryLink: found link {} on database.", link);
                return link;
            }
        }
        return null;
    }
    
    @Override
    @Transactional
    public void storeDiscoveryLink(final DiscoveryLink discoveryLink)
    {
        final Date now = new Date();
        String source = getLinkd().getName()+"/"+discoveryLink.getPackageName();

        for (final NodeToNodeLink lk : discoveryLink.getLinks()) {
            
            LOG.debug("storeDiscoveryLink: parsing link {}.",lk);
            DataLinkInterface link = getDatabaseLink(m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(Integer.valueOf(lk.getNodeId()),
                                                                                       Integer.valueOf(lk.getIfindex())),lk.getNodeparentid(),lk.getParentifindex(),lk.getProtocol());

            if (link == null) {
                LOG.info("storeDiscoveryLink: no found interface on database for link {}. Creating a new one",lk);
                final OnmsNode onmsNode = m_nodeDao.get(lk.getNodeId());
                link = new DataLinkInterface(
                                          onmsNode,
                                          lk.getIfindex(),
                                          lk.getNodeparentid(),
                                          lk.getParentifindex(),
                                          StatusType.ACTIVE,
                                          now);
                link.setProtocol(lk.getProtocol());
            } else {
                link.setStatus(StatusType.ACTIVE);
                link.setLastPollTime(now);
            }
            link.setSource(source);

            DataLinkInterface reverselink = getDatabaseLink(m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(Integer.valueOf(lk.getNodeparentid()),
                                                                                                          Integer.valueOf(lk.getParentifindex())), lk.getNodeId(), lk.getIfindex(), lk.getProtocol());
            if (reverselink != null ) {
                LOG.info("storeDiscoveryLink: Deleting found reverse link {}.", reverselink);
                m_dataLinkInterfaceDao.delete(reverselink);
            }
            LOG.debug("storeDiscoveryLink: Storing {}", link);
            m_dataLinkInterfaceDao.saveOrUpdate(link);
        }

        m_dataLinkInterfaceDao.deactivateIfOlderThan(now,source);
        m_dataLinkInterfaceDao.deleteIfOlderThan(new Date(now.getTime()-3*discoveryLink.getInterval()),source);
        m_dataLinkInterfaceDao.flush();
    }

    @Override
    public void update(final int nodeid, final StatusType action) {
        m_vlanDao.setStatusForNode(nodeid, action);
        m_atInterfaceDao.setStatusForNode(nodeid, action);
        m_ipRouteInterfaceDao.setStatusForNode(nodeid, action);
        m_stpNodeDao.setStatusForNode(nodeid, action);
        m_stpInterfaceDao.setStatusForNode(nodeid, action);
        for (String packageName: getLinkd().getActivePackages())
            m_dataLinkInterfaceDao.setStatusForNode(nodeid, getLinkd().getSource()+"/"+packageName, action);
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
            for (String packageName: getLinkd().getActivePackages())
                m_dataLinkInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex, getLinkd().getSource()+"/"+packageName, action);
        }
    }

    // SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND (snmpifname = ? OR snmpifdescr = ?)
    @Override
    protected int getIfIndexByName(final int targetCdpNodeId, final String cdpTargetDevicePort) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.alias("node", "node");
        builder.eq("node.id", targetCdpNodeId);
        builder.or(new EqRestriction("ifName", cdpTargetDevicePort), new EqRestriction("ifDescr", cdpTargetDevicePort));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (interfaces.isEmpty()) {
            return -1;
        } else {
            if (interfaces.size() > 1) {
                LOG.debug("getIfIndexByName: More than one SnmpInterface matches nodeId {} and snmpIfName/snmpIfDescr {}", targetCdpNodeId, cdpTargetDevicePort);
            }
            return interfaces.get(0).getIfIndex();
        }
    }

    // SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?
    @Override
    protected List<OnmsNode> getNodeidFromIp(final InetAddress cdpTargetIpAddr) {
        List<OnmsNode> nodeids = new ArrayList<OnmsNode>();
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsIpInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.eq("ipAddress", cdpTargetIpAddr);
        builder.eq("node.type", "A");
        List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(builder.toCriteria());

        LOG.debug("getNodeidFromIp: Found {} nodeids matching ipAddress {}", interfaces.size(),str(cdpTargetIpAddr));
        for (final OnmsIpInterface ipinterface : interfaces) {
            nodeids.add(ipinterface.getNode());
        }
        return nodeids;
    }

    // SELECT node.nodeid,snmpinterface.snmpifindex,snmpinterface.snmpipadentnetmask FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid LEFT JOIN snmpinterface ON ipinterface.snmpinterfaceid = snmpinterface.id WHERE node.nodetype = 'A' AND ipinterface.ipaddr = ?
    @Override
    protected List<RouterInterface> getRouteInterface(final InetAddress nexthop, int ifindex) {

        List<RouterInterface> routes = new ArrayList<RouterInterface>();

        final List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findByIpAddress(str(nexthop));

        LOG.debug("getRouteInterface: Found {} interface matching ipAddress {}", interfaces.size(),str(nexthop));

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
            LOG.debug("getRouteInterface: adding {} route interface" ,route);
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
        LOG.debug("getSnmpIfType({}, {}), found {}.", nodeId, ifIndex, snmpIfType);
        return snmpIfType;
    }

    @Override
    protected Map<Integer,String> getPhysAddrs(int nodeId) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.alias("node", "node");
        builder.eq("node.id", nodeId);

        final Map<Integer,String> addrMap = new HashMap<Integer, String>();

        for (final OnmsSnmpInterface snmpInterface : m_snmpInterfaceDao.findMatching(builder.toCriteria())) {
        	Integer ifindex = snmpInterface.getIfIndex();
        	if (ifindex == null) 
        		ifindex = -1;
            addrMap.put(ifindex,snmpInterface.getPhysAddr());
        }

        return addrMap;
    }

    @Override
    protected void saveIpRouteInterface(final OnmsIpRouteInterface saveMe) {
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
    protected void saveStpNode(final OnmsStpNode saveMe) {
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
    protected void saveAtInterface(final OnmsAtInterface saveMe) {
        new UpsertTemplate<OnmsAtInterface, AtInterfaceDao>(m_transactionManager, m_atInterfaceDao) {

            @Override
            protected OnmsAtInterface query() {
                return m_dao.findByNodeAndAddress(saveMe.getNode().getId(), saveMe.getIpAddress(), saveMe.getMacAddress());
            }

            @Override
            protected OnmsAtInterface doUpdate(OnmsAtInterface updateMe) {
                // Make sure that the fields used in the query match
                Assert.isTrue(updateMe.getNode().compareTo(saveMe.getNode()) == 0);
                Assert.isTrue(updateMe.getIpAddress().equals(saveMe.getIpAddress()));
                Assert.isTrue(updateMe.getMacAddress().equals(saveMe.getMacAddress()));

                if (updateMe.getId() == null && saveMe.getId() != null) {
                    updateMe.setId(saveMe.getId());
                }
                //updateMe.setBridgePort(saveMe.getBridgePort());
                updateMe.setIfIndex(saveMe.getIfIndex());
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                //updateMe.setNode(saveMe.getNode());
                updateMe.setStatus(saveMe.getStatus());
                //updateMe.setVlan(saveMe.getVlan());
                m_dao.update(updateMe);
                m_dao.flush();
                return updateMe;
            }

            @Override
            protected OnmsAtInterface doInsert() {
                m_dao.save(saveMe);
                m_dao.flush();
                return saveMe;
            }
        }.execute();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        LOG.debug("Initialized {}", this.getClass().getSimpleName());
    }

    @Override
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    @Override
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    @Override
    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    @Override
    public AtInterfaceDao getAtInterfaceDao() {
        return m_atInterfaceDao;
    }

    public VlanDao getVlanDao() {
        return m_vlanDao;
    }

    public StpNodeDao getStpNodeDao() {
        return m_stpNodeDao;
    }

    public StpInterfaceDao getStpInterfaceDao() {
        return m_stpInterfaceDao;
    }

    public IpRouteInterfaceDao getIpRouteInterfaceDao() {
        return m_ipRouteInterfaceDao;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(final DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    @Override
    @Transactional
    public OnmsSnmpInterface getFromSysnameIpAddress(final String lldpRemSysname, final InetAddress lldpRemPortid) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsIpInterface.class);
        builder.createAlias("node", "node");
        builder.eq("node.sysName", lldpRemSysname);
        builder.eq("ipAddress",lldpRemPortid);
        final List<OnmsIpInterface> interfaces = getIpInterfaceDao().findMatching(builder.toCriteria());
        if (interfaces != null && !interfaces.isEmpty() && interfaces.size() == 1) {
            OnmsIpInterface ip =interfaces.get(0);
            return getSnmpInterfaceDao().findByNodeIdAndIfIndex(ip.getNode().getId(), ip.getIfIndex());
        }
        return null;
    }

    @Transactional
    @Override
    protected OnmsSnmpInterface getFromSysnameIfName(String lldpRemSysname,
            String lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.sysName", lldpRemSysname));
        criteria.addRestriction(new EqRestriction("ifName", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty() && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    @Transactional
    @Override
    protected OnmsSnmpInterface getFromSysnameIfIndex(String lldpRemSysname,
            Integer lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.sysName", lldpRemSysname));
        criteria.addRestriction(new EqRestriction("ifIndex", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty() && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    @Transactional
    @Override
    protected OnmsSnmpInterface getFromSysnameMacAddress(String lldpRemSysname,
            String lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.sysName", lldpRemSysname));
        criteria.addRestriction(new EqRestriction("physAddr", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty() && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    @Transactional
    @Override
    protected OnmsSnmpInterface getFromSysnameIfAlias(String lldpRemSysname,
            String lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.sysName", lldpRemSysname));
        criteria.addRestriction(new EqRestriction("ifAlias", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = getSnmpInterfaceDao().findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty() && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    @Transactional
    @Override
    protected OnmsSnmpInterface getFromSysnameAgentCircuitId(String lldpRemSysname,
            String lldpRemPortid) {
        LOG.warn("getFromSysnameAgentCircuitId: AgentCircuitId LLDP PortSubTypeId not supported");
        return null;
    }

    protected OnmsSnmpInterface getFromSysnamePortComponent(String lldpRemSysname,
            String lldpRemPortid) {
        LOG.warn("getFromSysnamePortComponent:PortComponent LLDP PortSubTypeId not supported");
        return null;
    }

}
