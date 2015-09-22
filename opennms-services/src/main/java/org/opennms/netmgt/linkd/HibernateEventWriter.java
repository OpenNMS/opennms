/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2011-2014 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero
 * General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/ For more information contact: OpenNMS(R)
 * Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.utils.InetAddressUtils;
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
import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.linkd.snmp.CdpInterfaceTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dBasePortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dStpPortTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dTpFdbTableEntry;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTableEntry;
import org.opennms.netmgt.linkd.snmp.IpRouteCollectorEntry;
import org.opennms.netmgt.linkd.snmp.IsisCircTableEntry;
import org.opennms.netmgt.linkd.snmp.IsisISAdjTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpMibConstants;
import org.opennms.netmgt.linkd.snmp.LldpRemTableEntry;
import org.opennms.netmgt.linkd.snmp.MtxrWlRtabTableEntry;
import org.opennms.netmgt.linkd.snmp.OspfNbrTableEntry;
import org.opennms.netmgt.linkd.snmp.QBridgeDot1dTpFdbTableEntry;
import org.opennms.netmgt.linkd.snmp.Vlan;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.topology.AtInterface;
import org.opennms.netmgt.model.topology.CdpInterface;
import org.opennms.netmgt.model.topology.IsisISAdjInterface;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;
import org.opennms.netmgt.model.topology.LldpRemInterface;
import org.opennms.netmgt.model.topology.NodeToNodeLink;
import org.opennms.netmgt.model.topology.OspfNbrInterface;
import org.opennms.netmgt.model.topology.RouterInterface;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class HibernateEventWriter implements QueryManager {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventWriter.class);
    private static final InetAddress m_zeroAddress = InetAddressUtils.addr("0.0.0.0");
    public static final int SNMP_IF_TYPE_ETHERNET = 6;

    public static final int SNMP_IF_TYPE_PROP_VIRTUAL = 53;

    public static final int SNMP_IF_TYPE_L2_VLAN = 135;

    public static final int SNMP_IF_TYPE_L3_VLAN = 136;

        /**
         * The status of the info in FDB table entry The meanings of the value is
         * other(1): none of the following. This would include the case where some
         * other MIB object (not the corresponding instance of dot1dTpFdbPort, nor
         * an entry in the dot1dStaticTable) is being used to determine if and how
         * frames addressed to the value of the corresponding instance of
         * dot1dTpFdbAddress are being forwarded.
         */
        public static final int SNMP_DOT1D_FDB_STATUS_OTHER = 1;

        /**
         * The status of the info in FDB table entry The status of this entry. The
         * meanings of the values are: invalid(2) : this entry is not longer valid
         * (e.g., it was learned but has since aged-out), but has not yet been
         * flushed from the table.
         */
        public static final int SNMP_DOT1D_FDB_STATUS_INVALID = 2;

        /**
         * The status of the info in FDB table entry The status of this entry. The
         * meanings of the values are: learned(3) : the value of the corresponding
         * instance of dot1dTpFdbPort was learned, and is being used.
         */
        public static final int SNMP_DOT1D_FDB_STATUS_LEARNED = 3;

        /**
         * The status of the info in FDB table entry The status of this entry. The
         * meanings of the values are: self(4) : the value of the corresponding
         * instance of dot1dTpFdbAddress represents one of the bridge's addresses.
         * The corresponding instance of dot1dTpFdbPort indicates which of the
         * bridge's ports has this address.
         */
        public static final int SNMP_DOT1D_FDB_STATUS_SELF = 4;

        /**
         * mgmt(5) : the value of the corresponding instance of dot1dTpFdbAddress is
         * also the value of an existing instance of dot1dStaticAddress.
         */
        public static final int SNMP_DOT1D_FDB_STATUS_MGMT = 5;


    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private NodeDao m_nodeDao;

    private IpInterfaceDao m_ipInterfaceDao;

    private SnmpInterfaceDao m_snmpInterfaceDao;

    private AtInterfaceDao m_atInterfaceDao;

    private VlanDao m_vlanDao;

    private StpNodeDao m_stpNodeDao;

    private StpInterfaceDao m_stpInterfaceDao;

    private IpRouteInterfaceDao m_ipRouteInterfaceDao;

    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public AtInterfaceDao getAtInterfaceDao() {
        return m_atInterfaceDao;
    }

    public void setAtInterfaceDao(AtInterfaceDao atInterfaceDao) {
        m_atInterfaceDao = atInterfaceDao;
    }

    public VlanDao getVlanDao() {
        return m_vlanDao;
    }

    public void setVlanDao(VlanDao vlanDao) {
        m_vlanDao = vlanDao;
    }

    public StpNodeDao getStpNodeDao() {
        return m_stpNodeDao;
    }

    public void setStpNodeDao(StpNodeDao stpNodeDao) {
        m_stpNodeDao = stpNodeDao;
    }

    public StpInterfaceDao getStpInterfaceDao() {
        return m_stpInterfaceDao;
    }

    public void setStpInterfaceDao(StpInterfaceDao stpInterfaceDao) {
        m_stpInterfaceDao = stpInterfaceDao;
    }

    public IpRouteInterfaceDao getIpRouteInterfaceDao() {
        return m_ipRouteInterfaceDao;
    }

    public void setIpRouteInterfaceDao(IpRouteInterfaceDao ipRouteInterfaceDao) {
        m_ipRouteInterfaceDao = ipRouteInterfaceDao;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    protected void sendNewSuspectEvent(final InetAddress ipaddress,
            final InetAddress ipowner, final String name, final Linkd linkd) {
        linkd.sendNewSuspectEvent(ipaddress, ipowner, name);
    }

    // SELECT node.nodeid, nodesysoid, ipaddr FROM node LEFT JOIN ipinterface
    // ON node.nodeid = j.nodeid WHERE nodetype = 'A' AND issnmpprimary = 'P'
    @Override
    public List<LinkableSnmpNode> getSnmpNodeList() {
        final List<LinkableSnmpNode> nodes = new ArrayList<LinkableSnmpNode>();

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("ipInterfaces", "iface", JoinType.LEFT_JOIN);
        builder.eq("type", "A");
        builder.eq("iface.isSnmpPrimary", PrimaryType.PRIMARY);
        for (final OnmsNode node : m_nodeDao.findMatching(builder.toCriteria())) {
            final String sysObjectId = node.getSysObjectId();
            nodes.add(new LinkableSnmpNode(
                                           node.getId(),
                                           node.getPrimaryInterface().getIpAddress(),
                                           sysObjectId == null ? "-1"
                                                              : sysObjectId,
                                           node.getSysName()));
        }

        return nodes;
    }

    // SELECT nodesysoid, ipaddr FROM node LEFT JOIN ipinterface ON
    // node.nodeid = ipinterface.nodeid WHERE node.nodeid = ? AND nodetype =
    // 'A' AND issnmpprimary = 'P'
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
            return new LinkableSnmpNode(
                                        node.getId(),
                                        node.getPrimaryInterface().getIpAddress(),
                                        sysObjectId == null ? "-1"
                                                           : sysObjectId,
                                        node.getSysName());
        } else {
            return null;
        }
    }

    @Override
    public void updateDeletedNodes() {
        // UPDATE atinterface set status = 'D' WHERE nodeid IN (SELECT nodeid
        // from node WHERE nodetype = 'D' ) AND status <> 'D'
        m_atInterfaceDao.markDeletedIfNodeDeleted();
        m_atInterfaceDao.flush();

        // UPDATE vlan set status = 'D' WHERE nodeid IN (SELECT nodeid from
        // node WHERE nodetype = 'D' ) AND status <> 'D'
        m_vlanDao.markDeletedIfNodeDeleted();
        m_vlanDao.flush();

        // UPDATE stpnode set status = 'D' WHERE nodeid IN (SELECT nodeid from
        // node WHERE nodetype = 'D' ) AND status <> 'D'
        m_stpNodeDao.markDeletedIfNodeDeleted();
        m_stpNodeDao.flush();

        // UPDATE stpinterface set status = 'D' WHERE nodeid IN (SELECT nodeid
        // from node WHERE nodetype = 'D' ) AND status <> 'D'
        m_stpInterfaceDao.markDeletedIfNodeDeleted();
        m_stpInterfaceDao.flush();

        // UPDATE iprouteinterface set status = 'D' WHERE nodeid IN (SELECT
        // nodeid from node WHERE nodetype = 'D' ) AND status <> 'D'
        m_ipRouteInterfaceDao.markDeletedIfNodeDeleted();
        m_ipRouteInterfaceDao.flush();

        // UPDATE datalinkinterface set status = 'D' WHERE (nodeid IN (SELECT
        // nodeid from node WHERE nodetype = 'D' ) OR nodeparentid IN (SELECT
        // nodeid from node WHERE nodetype = 'D' )) AND status <> 'D'
        m_dataLinkInterfaceDao.markDeletedIfNodeDeleted();
        m_dataLinkInterfaceDao.flush();
    }

    protected void markOldDataInactive(final Date scanTime, final int nodeid) {
        // UPDATE atinterface set status = 'N' WHERE sourcenodeid = ? AND
        // lastpolltime < ? AND status = 'A'
        m_atInterfaceDao.deactivateForSourceNodeIdIfOlderThan(nodeid,
                                                              scanTime);
        m_atInterfaceDao.flush();

        // UPDATE vlan set status = 'N' WHERE nodeid =? AND lastpolltime < ?
        // AND status = 'A'
        m_vlanDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_vlanDao.flush();

        // UPDATE iprouteinterface set status = 'N' WHERE nodeid = ? AND
        // lastpolltime < ? AND status = 'A'
        m_ipRouteInterfaceDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_ipRouteInterfaceDao.flush();

        // UPDATE stpnode set status = 'N' WHERE nodeid = ? AND lastpolltime <
        // ? AND status = 'A'
        m_stpNodeDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_stpNodeDao.flush();

        // UPDATE stpinterface set status = 'N' WHERE nodeid = ? AND
        // lastpolltime < ? AND status = 'A'
        m_stpInterfaceDao.deactivateForNodeIdIfOlderThan(nodeid, scanTime);
        m_stpInterfaceDao.flush();
    }

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
    public LinkableNode storeSnmpCollection(final LinkableNode node,
            final SnmpCollection snmpColl, final Linkd linkd) {
        final Date scanTime = new Date();

        final OnmsNode onmsNode = m_nodeDao.get(node.getNodeId());
        if (onmsNode == null) {
            LOG.debug("no node found!");
            return null;
        }

        LOG.debug("storeSnmpCollection: wifi hasMtxrWlRtabTable: {}",
                  snmpColl.hasMtxrWlRtabTable());
        if (snmpColl.hasMtxrWlRtabTable()) {
            processWifi(node, snmpColl, scanTime);
        }

        LOG.debug("storeSnmpCollection: ospf hasOspfGeneralGroup/hasOspfNbrTable: {}/{}",
                  snmpColl.hasOspfGeneralGroup(), snmpColl.hasOspfNbrTable());
        if (snmpColl.hasOspfGeneralGroup() && snmpColl.hasOspfNbrTable()) {
            processOspf(node, snmpColl, scanTime);
        }

        LOG.debug("storeSnmpCollection: isis hasIsIsSystemObjectGroup/hasIsisCircTable/hasIsisISAdjTable: {}/{}/{}",
                  snmpColl.hasIsIsSysObjectGroup(),
                  snmpColl.hasIsisCircTable(), snmpColl.hasIsisISAdjTable());
        if (snmpColl.hasIsIsSysObjectGroup() && snmpColl.hasIsisCircTable()
                && snmpColl.hasIsisISAdjTable()) {
            processIsis(node, snmpColl, scanTime);
        }

        LOG.debug("storeSnmpCollection: lldp hasLldpLocalGroup/hasLldpLocTable/haLldpRemTable: {}/{}/{}",
                  snmpColl.hasLldpLocalGroup(), snmpColl.hasLldpLocTable(),
                  snmpColl.hasLldpRemTable());
        if (snmpColl.hasLldpLocalGroup()) {
            processLldp(node, snmpColl, scanTime);
        }

        LOG.debug("storeSnmpCollection: hasIpNetToMediaTable: {}",
                  snmpColl.hasIpNetToMediaTable());
        if (snmpColl.hasIpNetToMediaTable()) {
            processIpNetToMediaTable(node, snmpColl, scanTime, linkd);
        }

        LOG.debug("storeSnmpCollection: hasCdpGlobalGroup: {}",
                  snmpColl.hasCdpGlobalGroup());
        LOG.debug("storeSnmpCollection: hasCdpCacheTable: {}",
                  snmpColl.hasCdpCacheTable());
        if (snmpColl.hasCdpGlobalGroup() && snmpColl.hasCdpCacheTable()) {
            processCdp(node, snmpColl, scanTime, linkd);
        }

        LOG.debug("storeSnmpCollection: hasRouteTable: {}",
                  snmpColl.hasRouteTable());
        if (snmpColl.hasRouteTable()) {
            processRouteTable(onmsNode, node, snmpColl, scanTime,linkd);
        }

        LOG.debug("storeSnmpCollection: hasVlanTable: {}",
                  snmpColl.hasVlanTable());
        if (snmpColl.hasVlanTable()) {
            processVlanTable(onmsNode, node, snmpColl, scanTime);
        }

        if (!snmpColl.getSnmpVlanCollections().isEmpty()) {
            node.setMacIdentifiers(getPhysAddrs(node.getNodeId()));
            for (final OnmsVlan vlan : snmpColl.getSnmpVlanCollections().keySet()) {
                LOG.debug("storeSnmpCollection: parsing bridge data on VLAN {}/{}",
                          vlan.getVlanId(), vlan.getVlanName());
                storeSnmpVlanCollection(onmsNode,
                                        node,
                                        vlan,
                                        snmpColl.getSnmpVlanCollections().get(vlan),
                                        scanTime,linkd);
            }
        }

        markOldDataInactive(scanTime, node.getNodeId());
        deleteOlderData(new Date(scanTime.getTime()
                                - snmpColl.getPollInterval() * 3),
                        node.getNodeId());

        return node;
    }

    private DataLinkInterface getDatabaseLink(
            Collection<DataLinkInterface> links, int nodeparentid,
            int parentifindex, DiscoveryProtocol protocol) {
        for (DataLinkInterface link : links) {
            if (link.getNodeParentId().intValue() == nodeparentid
                    && link.getParentIfIndex().intValue() == parentifindex
                    && link.getProtocol() == protocol) {
                LOG.info("storeDiscoveryLink: found link {} on database.",
                         link);
                return link;
            }
        }
        return null;
    }

    @Override
    public void storeDiscoveryLink(final DiscoveryLink discoveryLink) {
        final Date now = new Date();
        String source = Linkd.LOG_PREFIX + "/"
                + discoveryLink.getPackageName();

        for (final NodeToNodeLink lk : discoveryLink.getLinks()) {

            LOG.debug("storeDiscoveryLink: parsing link {}.", lk);
            DataLinkInterface link = getDatabaseLink(m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(Integer.valueOf(lk.getNodeId()),
                                                                                                   Integer.valueOf(lk.getIfindex())),
                                                     lk.getNodeparentid(),
                                                     lk.getParentifindex(),
                                                     lk.getProtocol());

            if (link == null) {
                LOG.info("storeDiscoveryLink: no found interface on database for link {}. Creating a new one",
                         lk);
                final OnmsNode onmsNode = m_nodeDao.get(lk.getNodeId());
                link = new DataLinkInterface(onmsNode, lk.getIfindex(),
                                             lk.getNodeparentid(),
                                             lk.getParentifindex(),
                                             StatusType.ACTIVE, now);
                link.setProtocol(lk.getProtocol());
            } else {
                link.setStatus(StatusType.ACTIVE);
                link.setLastPollTime(now);
            }
            link.setSource(source);

            DataLinkInterface reverselink = getDatabaseLink(m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(Integer.valueOf(lk.getNodeparentid()),
                                                                                                          Integer.valueOf(lk.getParentifindex())),
                                                            lk.getNodeId(),
                                                            lk.getIfindex(),
                                                            lk.getProtocol());
            if (reverselink != null) {
                LOG.info("storeDiscoveryLink: Deleting found reverse link {}.",
                         reverselink);
                m_dataLinkInterfaceDao.delete(reverselink);
            }
            LOG.debug("storeDiscoveryLink: Storing {}", link);
            m_dataLinkInterfaceDao.saveOrUpdate(link);
        }

        m_dataLinkInterfaceDao.deactivateIfOlderThan(now, source);
        m_dataLinkInterfaceDao.deleteIfOlderThan(new Date(now.getTime() - 3
                * discoveryLink.getInterval()), source);
        m_dataLinkInterfaceDao.flush();
    }

    @Override
    public void update(final int nodeid, final StatusType action, Set<String> activePackages) {
        m_vlanDao.setStatusForNode(nodeid, action);
        m_atInterfaceDao.setStatusForNode(nodeid, action);
        m_ipRouteInterfaceDao.setStatusForNode(nodeid, action);
        m_stpNodeDao.setStatusForNode(nodeid, action);
        m_stpInterfaceDao.setStatusForNode(nodeid, action);
        for (String packageName : activePackages)
            m_dataLinkInterfaceDao.setStatusForNode(nodeid, Linkd.LOG_PREFIX
                    + "/" + packageName, action);
    }

    @Override
    public void updateForInterface(final int nodeid, final String ipAddr,
            final int ifIndex, final StatusType action, Set<String> activePackages) {
        if (!(ipAddr == null || ipAddr.length() == 0 || "0.0.0.0".equals(ipAddr))) {
            m_atInterfaceDao.setStatusForNodeAndIp(nodeid, ipAddr, action);
        }
        if (ifIndex > -1) {
            m_atInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex,
                                                        action);
            m_stpInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex,
                                                         action);
            m_ipRouteInterfaceDao.setStatusForNodeAndIfIndex(nodeid, ifIndex,
                                                             action);
            for (String packageName : activePackages)
                m_dataLinkInterfaceDao.setStatusForNodeAndIfIndex(nodeid,
                                                                  ifIndex,
                                                                  Linkd.LOG_PREFIX
                                                                          + "/"
                                                                          + packageName,
                                                                  action);
        }
    }

    // SELECT snmpifindex FROM snmpinterface WHERE nodeid = ? AND (snmpifname
    // = ? OR snmpifdescr = ?)
    protected int getIfIndexByName(final int targetCdpNodeId,
            final String cdpTargetDevicePort) {
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsSnmpInterface.class);
        builder.alias("node", "node");
        builder.eq("node.id", targetCdpNodeId);
        builder.or(new EqRestriction("ifName", cdpTargetDevicePort),
                   new EqRestriction("ifDescr", cdpTargetDevicePort));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (interfaces.isEmpty()) {
            return -1;
        } else {
            if (interfaces.size() > 1) {
                LOG.debug("getIfIndexByName: More than one SnmpInterface matches nodeId {} and snmpIfName/snmpIfDescr {}",
                          targetCdpNodeId, cdpTargetDevicePort);
            }
            return interfaces.get(0).getIfIndex();
        }
    }

    // SELECT node.nodeid FROM node LEFT JOIN ipinterface ON node.nodeid =
    // ipinterface.nodeid WHERE nodetype = 'A' AND ipaddr = ?
    protected List<OnmsNode> getNodeidFromIp(final InetAddress cdpTargetIpAddr) {
        List<OnmsNode> nodeids = new ArrayList<OnmsNode>();
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsIpInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.eq("ipAddress", cdpTargetIpAddr);
        builder.eq("node.type", "A");
        List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(builder.toCriteria());

        LOG.debug("getNodeidFromIp: Found {} nodeids matching ipAddress {}",
                  interfaces.size(), str(cdpTargetIpAddr));
        for (final OnmsIpInterface ipinterface : interfaces) {
            nodeids.add(ipinterface.getNode());
        }
        return nodeids;
    }

    // SELECT
    // node.nodeid,snmpinterface.snmpifindex,snmpinterface.snmpipadentnetmask
    // FROM node LEFT JOIN ipinterface ON node.nodeid = ipinterface.nodeid
    // LEFT JOIN snmpinterface ON ipinterface.snmpinterfaceid =
    // snmpinterface.id WHERE node.nodetype = 'A' AND ipinterface.ipaddr = ?
    protected List<RouterInterface> getRouteInterface(
            final InetAddress nexthop, int ifindex) {

        List<RouterInterface> routes = new ArrayList<RouterInterface>();

        final List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findByIpAddress(str(nexthop));

        LOG.debug("getRouteInterface: Found {} interface matching ipAddress {}",
                  interfaces.size(), str(nexthop));

        for (OnmsIpInterface ipInterface : interfaces) {
            RouterInterface route = null;
            final OnmsNode node = ipInterface.getNode();
            final OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();
            if (snmpInterface == null || snmpInterface.getNetMask() == null) {
                route = new RouterInterface(node.getId(), -1);
            } else {
                route = new RouterInterface(node.getId(),
                                            snmpInterface.getIfIndex(),
                                            snmpInterface.getNetMask());
            }
            route.setNextHop(nexthop);
            route.setIfindex(ifindex);
            LOG.debug("getRouteInterface: adding {} route interface", route);
            routes.add(route);
        }
        return routes;
    }

    // SELECT snmpiftype FROM snmpinterface WHERE nodeid = ? AND snmpifindex =
    // ?"
    protected int getSnmpIfType(final int nodeId, final Integer ifIndex) {
        Integer snmpIfType = -1;
        OnmsSnmpInterface snmpInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId,
                                                                                    ifIndex);
        if (snmpInterface != null) {
            snmpIfType = snmpInterface.getIfType();
        }
        LOG.debug("getSnmpIfType({}, {}), found {}.", nodeId, ifIndex,
                  snmpIfType);
        return snmpIfType;
    }

    protected Map<Integer, String> getPhysAddrs(int nodeId) {
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsSnmpInterface.class);
        builder.alias("node", "node");
        builder.eq("node.id", nodeId);

        final Map<Integer, String> addrMap = new TreeMap<Integer, String>();

        for (final OnmsSnmpInterface snmpInterface : m_snmpInterfaceDao.findMatching(builder.toCriteria())) {
            Integer ifindex = snmpInterface.getIfIndex();
            if (ifindex == null)
                ifindex = -1;
            addrMap.put(ifindex, snmpInterface.getPhysAddr());
        }

        return addrMap;
    }

    @Transactional
    protected void saveIpRouteInterface(final OnmsIpRouteInterface saveMe) {
        new UpsertTemplate<OnmsIpRouteInterface, IpRouteInterfaceDao>(
                                                                      m_transactionManager,
                                                                      m_ipRouteInterfaceDao) {

            @Override
            protected OnmsIpRouteInterface query() {
                return m_dao.findByNodeAndDest(saveMe.getNode().getId(),
                                               saveMe.getRouteDest());
            }

            @Override
            protected OnmsIpRouteInterface doUpdate(
                    OnmsIpRouteInterface updateMe) {
                // Make sure that the fields used in the query match
                Assert.isTrue(updateMe.getNode().compareTo(saveMe.getNode()) == 0);
                Assert.isTrue(updateMe.getRouteDest().equals(saveMe.getRouteDest()));

                if (updateMe.getId() == null && saveMe.getId() != null) {
                    updateMe.setId(saveMe.getId());
                }
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                // updateMe.setRouteDest(saveMe.getRouteDest());
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

    @Transactional
    protected void saveVlan(final OnmsVlan saveMe) {
        new UpsertTemplate<OnmsVlan, VlanDao>(m_transactionManager, m_vlanDao) {

            @Override
            protected OnmsVlan query() {
                return m_dao.findByNodeAndVlan(saveMe.getNode().getId(),
                                               saveMe.getVlanId());
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

    @Transactional
    protected void saveStpNode(final OnmsStpNode saveMe) {
        new UpsertTemplate<OnmsStpNode, StpNodeDao>(m_transactionManager,
                                                    m_stpNodeDao) {

            @Override
            protected OnmsStpNode query() {
                return m_dao.findByNodeAndVlan(saveMe.getNode().getId(),
                                               saveMe.getBaseVlan());
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
                // updateMe.setBaseVlan(saveMe.getBaseVlan());
                updateMe.setBaseVlanName(saveMe.getBaseVlanName());
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                // updateMe.setNode(saveMe.getNode());
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

    @Transactional
    protected void saveStpInterface(final OnmsStpInterface saveMe) {
        new UpsertTemplate<OnmsStpInterface, StpInterfaceDao>(
                                                              m_transactionManager,
                                                              m_stpInterfaceDao) {

            @Override
            protected OnmsStpInterface query() {
                return m_dao.findByNodeAndVlan(saveMe.getNode().getId(),
                                               saveMe.getBridgePort(),
                                               saveMe.getVlan());
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
                // updateMe.setBridgePort(saveMe.getBridgePort());
                updateMe.setIfIndex(saveMe.getIfIndex());
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                // updateMe.setNode(saveMe.getNode());
                updateMe.setStatus(saveMe.getStatus());
                updateMe.setStpPortDesignatedBridge(saveMe.getStpPortDesignatedBridge());
                updateMe.setStpPortDesignatedCost(saveMe.getStpPortDesignatedCost());
                updateMe.setStpPortDesignatedPort(saveMe.getStpPortDesignatedPort());
                updateMe.setStpPortDesignatedRoot(saveMe.getStpPortDesignatedRoot());
                updateMe.setStpPortPathCost(saveMe.getStpPortPathCost());
                updateMe.setStpPortState(saveMe.getStpPortState());
                // updateMe.setVlan(saveMe.getVlan());

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

    @Transactional
    protected void saveAtInterface(final OnmsAtInterface saveMe) {
        new UpsertTemplate<OnmsAtInterface, AtInterfaceDao>(
                                                            m_transactionManager,
                                                            m_atInterfaceDao) {

            @Override
            protected OnmsAtInterface query() {
                return m_dao.findByNodeAndAddress(saveMe.getNode().getId(),
                                                  saveMe.getIpAddress(),
                                                  saveMe.getMacAddress());
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
                // updateMe.setBridgePort(saveMe.getBridgePort());
                updateMe.setIfIndex(saveMe.getIfIndex());
                updateMe.setLastPollTime(saveMe.getLastPollTime());
                // updateMe.setNode(saveMe.getNode());
                updateMe.setStatus(saveMe.getStatus());
                // updateMe.setVlan(saveMe.getVlan());
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

    public OnmsSnmpInterface getFromSysnameIpAddress(
            final String lldpRemSysname, final InetAddress lldpRemPortid) {
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsIpInterface.class);
        builder.createAlias("node", "node");
        builder.eq("node.sysName", lldpRemSysname);
        builder.eq("ipAddress", lldpRemPortid);
        final List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findMatching(builder.toCriteria());
        if (interfaces != null && !interfaces.isEmpty()
                && interfaces.size() == 1) {
            OnmsIpInterface ip = interfaces.get(0);
            return m_snmpInterfaceDao.findByNodeIdAndIfIndex(ip.getNode().getId(),
                                                                ip.getIfIndex());
        }
        return null;
    }

    protected OnmsSnmpInterface getFromSysnameIfName(String lldpRemSysname,
            String lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "node",
                                                                  "node",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("node.sysName",
                                                  lldpRemSysname));
        criteria.addRestriction(new EqRestriction("ifName", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()
                && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    protected OnmsSnmpInterface getFromSysnameIfIndex(String lldpRemSysname,
            Integer lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "node",
                                                                  "node",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("node.sysName",
                                                  lldpRemSysname));
        criteria.addRestriction(new EqRestriction("ifIndex", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()
                && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    protected OnmsSnmpInterface getFromSysnameMacAddress(
            String lldpRemSysname, String lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "node",
                                                                  "node",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("node.sysName",
                                                  lldpRemSysname));
        criteria.addRestriction(new EqRestriction("physAddr", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()
                && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    protected OnmsSnmpInterface getFromSysnameIfAlias(String lldpRemSysname,
            String lldpRemPortid) {
        final Criteria criteria = new Criteria(OnmsSnmpInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "node",
                                                                  "node",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("node.sysName",
                                                  lldpRemSysname));
        criteria.addRestriction(new EqRestriction("ifAlias", lldpRemPortid));
        final List<OnmsSnmpInterface> interfaces = m_snmpInterfaceDao.findMatching(criteria);
        if (interfaces != null && !interfaces.isEmpty()
                && interfaces.size() == 1) {
            return interfaces.get(0);
        }
        return null;
    }

    protected OnmsSnmpInterface getFromSysnameAgentCircuitId(
            String lldpRemSysname, String lldpRemPortid) {
        LOG.warn("getFromSysnameAgentCircuitId: AgentCircuitId LLDP PortSubTypeId not supported");
        return null;
    }

    protected OnmsSnmpInterface getFromSysnamePortComponent(
            String lldpRemSysname, String lldpRemPortid) {
        LOG.warn("getFromSysnamePortComponent:PortComponent LLDP PortSubTypeId not supported");
        return null;
    }

    protected void processIpNetToMediaTable(final LinkableNode node,
            final SnmpCollection snmpcoll, final Date scanTime, final Linkd linkd) {

        boolean hasPrimaryIpAsAtinterface = false;
        if (LOG.isDebugEnabled()) {
            if (snmpcoll.getIpNetToMediaTable().size() > 0) {
                LOG.debug("processIpNetToMediaTable: Starting ipNetToMedia table processing for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processIpNetToMediaTable: Zero ipNetToMedia table entries for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        // the AtInterfaces used by LinkableNode where to save info
        for (final IpNetToMediaTableEntry ent : snmpcoll.getIpNetToMediaTable()) {

            final int ifindex = ent.getIpNetToMediaIfIndex();

            if (ifindex < 0) {
                LOG.warn("processIpNetToMediaTable: invalid ifindex {}",
                         ifindex);
                continue;
            }

            final InetAddress ipaddress = ent.getIpNetToMediaNetAddress();

            if (ipaddress.equals(node.getSnmpPrimaryIpAddr()))
                hasPrimaryIpAsAtinterface = true;
            final String hostAddress = InetAddressUtils.str(ipaddress);

            if (ipaddress == null || ipaddress.isLoopbackAddress()
                    || m_zeroAddress.equals(ipaddress)) {
                LOG.warn("processIpNetToMediaTable: invalid IP: {}",
                         hostAddress);
                continue;
            }

            final String physAddr = ent.getIpNetToMediaPhysAddress();

            if (physAddr == null || physAddr.equals("000000000000")
                    || physAddr.equalsIgnoreCase("ffffffffffff")) {
                LOG.warn("processIpNetToMediaTable: invalid MAC address {} for IP {}",
                         physAddr, hostAddress);
                continue;
            }

            LOG.debug("processIpNetToMediaTable: trying save ipNetToMedia info: IP address {}, MAC address {}, ifIndex {}",
                      hostAddress, physAddr, ifindex);

            // get an AtInterface but without setting MAC address
            final Collection<OnmsIpInterface> iplist = m_ipInterfaceDao.findByIpAddress(hostAddress);
            if (iplist.isEmpty()) {
                LOG.debug("processIpNetToMediaTable: no node found for IP address {}.",
                          hostAddress);
                sendNewSuspectEvent(ipaddress, snmpcoll.getTarget(),
                                    snmpcoll.getPackageName(), linkd);
                continue;
            }

            OnmsIpInterface ipinterface = null;
            if (iplist.size() > 1) {
                LOG.debug("processIpNetToMediaTable: found duplicated  IP address {}.",
                          hostAddress);
                for (OnmsIpInterface ip : iplist) {
                    LOG.debug("processIpNetToMediaTable: parsing duplicated  ip interface {}.",
                              ip);
                    if (ip.getNode().getId() == node.getNodeId()) {
                        LOG.debug("processIpNetToMediaTable: suitable ip interface found. Skipping entry {}",
                                  ip);
                        ipinterface = ip;
                        break;
                    }
                }
                if (ipinterface == null) {
                    LOG.debug("processIpNetToMediaTable: no suitable duplicated  arp interface found. Skipping entry {}",
                              ent);
                    continue;
                }
            } else {
                ipinterface = iplist.iterator().next();
            }
            OnmsAtInterface at = new OnmsAtInterface(
                                                     ipinterface.getNode(),
                                                     ipinterface.getIpAddress());
            int interfaceindex = getIfIndex(at.getNode().getId(), hostAddress);
            LOG.debug("processIpNetToMediaTable: found ifindex {} for node {} IP address {}.",
                      interfaceindex, node.getNodeId(), hostAddress);
            at.setSourceNodeId(node.getNodeId());

            if (at.getMacAddress() != null
                    && !at.getMacAddress().equals(physAddr)) {
                LOG.info("processIpNetToMediaTable: Setting OnmsAtInterface MAC address to {} but it used to be '{}' (IP Address = {}, ifIndex = {})",
                         physAddr, at.getMacAddress(), hostAddress, ifindex);
            }
            at.setMacAddress(physAddr);

            if (at.getIfIndex() != null
                    && at.getIfIndex().intValue() != ifindex) {
                LOG.info("processIpNetToMediaTable: Setting OnmsAtInterface ifIndex to {} but it used to be '{}' (IP Address = {}, MAC = {})",
                         ifindex, at.getIfIndex(), hostAddress, physAddr);
            }
            at.setIfIndex(interfaceindex);

            at.setLastPollTime(scanTime);
            at.setStatus(StatusType.ACTIVE);

            saveAtInterface(at);

            // Now store the information that is needed to create link in
            // linkd
            AtInterface atinterface = new AtInterface(at.getNode().getId(),
                                                      physAddr,
                                                      at.getIpAddress());
            atinterface.setIfIndex(interfaceindex);
            linkd.addAtInterface(snmpcoll.getPackageName(), atinterface);

        }

        if (!hasPrimaryIpAsAtinterface)
            savePrimaryAddressAtInterface(snmpcoll.getPackageName(), node, linkd);

    }

    private void savePrimaryAddressAtInterface(final String packageName,
            final LinkableNode node, final Linkd linkd) {
        LOG.info("savePrimaryAddressAtInterface: try to setting ifindex for linkednode primary ip address '{}' ",
                 node.getSnmpPrimaryIpAddr().getHostAddress());
        OnmsIpInterface ipinterface = m_ipInterfaceDao.findByNodeIdAndIpAddress(Integer.valueOf(node.getNodeId()),
                                                                                   node.getSnmpPrimaryIpAddr().getHostAddress());
        if (ipinterface != null) {
            OnmsSnmpInterface snmpinterface = ipinterface.getSnmpInterface();
            if (snmpinterface != null && snmpinterface.getPhysAddr() != null) {
                AtInterface at = new AtInterface(node.getNodeId(),
                                                 snmpinterface.getPhysAddr(),
                                                 node.getSnmpPrimaryIpAddr());
                at.setMacAddress(snmpinterface.getPhysAddr());
                LOG.info("savePrimaryAddressAtInterface: Setting AtInterface ifIndex to {}, for primary IP Address {}, MAC = {})",
                         at.getIfIndex(), at.getIpAddress().getHostAddress(),
                         at.getMacAddress());
                at.setIfIndex(snmpinterface.getIfIndex());
                linkd.addAtInterface(packageName, at);
            }
        }
    }

    // This method retrieve the right interface index from the OnmsIpInterface
    // This is required because the ifindex walked in atInterface snmp table
    // is related to the node that holds the information and not to the
    // effective node that hold the ip address.
    // This ifindex is saved in AtInterface object
    // that is used to find the right information for a linked node.
    // AR Dixit
    protected Integer getIfIndex(Integer nodeid, String ipaddress) {
        OnmsIpInterface ipinterface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeid,
                                                                                   ipaddress);
        if (ipinterface != null && ipinterface.getIfIndex() != null) {
            LOG.info("getIfindex: found ip interface for address '{}' on ifindex {}",
                     ipinterface.getIpAddress().getHostAddress(),
                     ipinterface.getIfIndex());
            return ipinterface.getIfIndex();
        }
        LOG.info("getIfIndex: no (ipinterface)ifindex found for nodeid {}, address '{}'.",
                 nodeid, ipaddress);
        return -1;
    }

    protected void processIsis(final LinkableNode node,
            final SnmpCollection snmpcoll, final Date scanTime) {
        String isisSysId = snmpcoll.getIsIsSystemObjectGroup().getIsisSysId();
        LOG.debug("processIsis: isis node/isissysId: {}/{}",
                  node.getNodeId(), isisSysId);
        if (snmpcoll.getIsIsSystemObjectGroup().getIsisSysAdminState() == IsisAdminState.off) {
            LOG.info("processIsis: isis admin down on node/isisSysId: {}/{}. Skipping!",
                     node.getNodeId(), isisSysId);
            return;
        }

        node.setIsisSysId(isisSysId);
        Map<Integer, Integer> isisCircIndexIfIndexMap = new TreeMap<Integer, Integer>();
        for (final IsisCircTableEntry circ : snmpcoll.getIsisCircTable()) {
            isisCircIndexIfIndexMap.put(circ.getIsisCircIndex(),
                                        circ.getIsisCircIfIndex());
        }

        List<IsisISAdjInterface> isisinterfaces = new ArrayList<IsisISAdjInterface>();
        for (final IsisISAdjTableEntry isisAdj : snmpcoll.getIsisISAdjTable()) {
            if (isisAdj.getIsIsAdjStatus() != IsisISAdjState.up) {
                LOG.info("processIsis: isis adj status not UP but {}, on node/isisISAdjNeighSysId/isisLocalCircIndex: {}/{}/{}. Skipping!",
                         isisAdj.getIsIsAdjStatus(), node.getNodeId(),
                         isisAdj.getIsIsAdjNeighSysId(),
                         isisAdj.getIsisCircIndex());
                return;
            }
            if (!isisCircIndexIfIndexMap.containsKey(isisAdj.getIsisCircIndex())) {
                LOG.info("processIsis: isis Circ Index not found on CircTable, on node/isisISAdjNeighSysId/isisLocalCircIndex: {}/{}/{}. Skipping!",
                         node.getNodeId(), isisAdj.getIsIsAdjNeighSysId(),
                         isisAdj.getIsisCircIndex());
                return;
            }
            IsisISAdjInterface isisinterface = new IsisISAdjInterface(
                                                                      isisAdj.getIsIsAdjNeighSysId(),
                                                                      isisCircIndexIfIndexMap.get(isisAdj.getIsisCircIndex()),
                                                                      isisAdj.getIsIsAdjNeighSnpaAddress(),
                                                                      isisAdj.getIsisISAdjIndex());
            LOG.debug("processIsis: isis adding adj interface node/interface: {}/{}",
                      node.getNodeId(), isisinterface);
            isisinterfaces.add(isisinterface);
        }
        node.setIsisInterfaces(isisinterfaces);
    }

    protected void processWifi(final LinkableNode node,
            final SnmpCollection snmpcoll, final Date scanTime) {
        for (final MtxrWlRtabTableEntry entry : snmpcoll.getMtxrWlRtabTable().getEntries()) {
            node.addWifiMacAddress(entry.getMtxrWlRtabIface(),
                                   entry.getMtxrWlRtabAddr());
        }
    }

    protected void processOspf(final LinkableNode node,
            final SnmpCollection snmpcoll, final Date scanTime) {

        InetAddress ospfRouterId = snmpcoll.getOspfGeneralGroup().getOspfRouterId();

        LOG.debug("processOspf: node {}: ospf router id: {}",
                  node.getNodeId(), str(ospfRouterId));
        if (m_zeroAddress.equals(ospfRouterId)) {
            LOG.info("processOspf: node {}: invalid ospf ruoter id: ospfrouterid: {}. Skipping!",
                     node.getNodeId(), str(ospfRouterId));
            return;
        }

        node.setOspfRouterId(ospfRouterId);

        List<OspfNbrInterface> ospfinterfaces = new ArrayList<OspfNbrInterface>();

        for (final OspfNbrTableEntry ospfNbrTableEntry : snmpcoll.getOspfNbrTable()) {
            InetAddress ospfNbrRouterId = ospfNbrTableEntry.getOspfNbrRouterId();
            InetAddress ospfNbrIpAddr = ospfNbrTableEntry.getOspfNbrIpAddress();
            LOG.debug("processOspf: node {}: ospf nei: ospfnbraddress/ospfnbrrouterid: {}/{}",
                      node.getNodeId(), str(ospfNbrIpAddr),
                      str(ospfNbrRouterId));
            if (m_zeroAddress.equals(ospfNbrIpAddr)
                    || m_zeroAddress.equals(ospfNbrRouterId)) {
                LOG.info("processOspf: node {}: ospf nei found invalid ip address: ospfnbraddress/ospfnbrrouterid: {}/{}",
                         node.getNodeId(), str(ospfNbrIpAddr),
                         str(ospfNbrRouterId));
                continue;
            }
            Integer ifIndex = ospfNbrTableEntry.getOspfNbrAddressLessIndex();
            LOG.debug("processOspf: node {}: ospf nei ospfnbrAddressLessIfIndex {} for: ospfnbraddress/ospfnbrrouterid: {}/{}",
                      node.getNodeId(), ifIndex, str(ospfNbrIpAddr),
                      str(ospfNbrRouterId));
            List<OnmsIpInterface> ipinterfaces = m_ipInterfaceDao.findByIpAddress(str(ospfNbrIpAddr));
            for (OnmsIpInterface ipinterface : ipinterfaces) {

                if (ifIndex.intValue() == 0)
                    ifIndex = ipinterface.getIfIndex();
                LOG.debug("processOspf: node {}: ospf nei nodeid/ifindex {}/{} for: ospfnbraddress/ospfnbrrouterid: {}/{}",
                          node.getNodeId(), ipinterface.getNode().getId(),
                          ifIndex, str(ospfNbrIpAddr), str(ospfNbrRouterId));
                if (ifIndex != null && ifIndex.intValue() > 0) {
                    OspfNbrInterface ospfinterface = new OspfNbrInterface(
                                                                          ospfNbrRouterId);
                    ospfinterface.setOspfNbrNodeId(ipinterface.getNode().getId());
                    ospfinterface.setOspfNbrIpAddr(ospfNbrIpAddr);

                    OnmsSnmpInterface snmpinterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(ipinterface.getNode().getId(),
                                                                                                   ifIndex);
                    if (snmpinterface != null
                            && snmpinterface.getNetMask() != null)
                        ospfinterface.setOspfNbrNetMask(snmpinterface.getNetMask());
                    else
                        ospfinterface.setOspfNbrNetMask(InetAddressUtils.getInetAddress("255.255.255.252"));

                    ospfinterface.setOspfNbrIfIndex(ifIndex);
                    LOG.debug("processOspf: node {}: found ospf nei netmask {} for: ospfnbraddress/ospfnbrrouterid: {}/{}",
                              node.getNodeId(),
                              str(ospfinterface.getOspfNbrNetMask()),
                              str(ospfNbrIpAddr), str(ospfNbrRouterId));
                    LOG.debug("processOspf: node {}: adding ospf nei interface: ospfinterface: {}",
                              node.getNodeId(), ospfinterface);
                    ospfinterfaces.add(ospfinterface);
                } else {
                    LOG.info("processOspf: node {}: ospf nei invalid ifindex {} for: ospfnbraddress/ospfnbrrouterid: {}/{}. Skipping!",
                             node.getNodeId(), ifIndex, str(ospfNbrIpAddr),
                             str(ospfNbrRouterId));
                }
            }
        }
        node.setOspfinterfaces(ospfinterfaces);
    }

    protected void processLldp(final LinkableNode node,
            final SnmpCollection snmpcoll, final Date scanTime) {

        node.setLldpChassisId(snmpcoll.getLldpLocalGroup().getLldpLocChassisid());
        node.setLldpChassisIdSubtype(snmpcoll.getLldpLocalGroup().getLldpLocChassisidSubType());
        node.setLldpSysname(snmpcoll.getLldpLocalGroup().getLldpLocSysname());

        Map<Integer, LldpLocTableEntry> localPortNumberToLocTableEntryMap = getLocalPortNumberToLocalTableEntryMap(snmpcoll);
        List<LldpRemInterface> lldpRemInterfaces = new ArrayList<LldpRemInterface>();

        for (final LldpRemTableEntry lldpRemTableEntry : snmpcoll.getLldpRemTable()) {

            LOG.debug("processLldp: lldp remote entry node/localport/remporttype/remport: {}/{}/{}/{}",
                      node.getNodeId(),
                      lldpRemTableEntry.getLldpRemLocalPortNum(),
                      lldpRemTableEntry.getLldpRemPortidSubtype(),
                      lldpRemTableEntry.getLldpRemPortid());
            Integer lldpLocsnmpIf = getLldpLocIfIndex(node.getLldpSysname(),
                                                      localPortNumberToLocTableEntryMap.get(lldpRemTableEntry.getLldpRemLocalPortNum()));
            if (lldpLocsnmpIf == null) {
                LOG.warn("processLldp: lldp local ifindex not found for local node/lldpLocalPortNumber: {}/{}",
                         node.getNodeId(),
                         lldpRemTableEntry.getLldpRemLocalPortNum());
                continue;
            }
            LOG.debug("processLldp: lldp local entry node/localport/localifIndex: {}/{}/{}",
                      node.getNodeId(),
                      lldpRemTableEntry.getLldpRemLocalPortNum(),
                      lldpLocsnmpIf);

            OnmsSnmpInterface lldpRemSnmpInterface = getLldpRemIfIndex(lldpRemTableEntry);
            if (lldpRemSnmpInterface == null) {
                LOG.warn("processLldp: lldp remote node/ifindex not found for remote sysname/porttype/portid: {}/{}/{}",
                         lldpRemTableEntry.getLldpRemSysname(),
                         lldpRemTableEntry.getLldpRemPortidSubtype(),
                         lldpRemTableEntry.getLldpRemPortid());
                continue;
            }

            LldpRemInterface lldpremint = new LldpRemInterface(
                                                               lldpRemTableEntry.getLldpRemChassisidSubtype(),
                                                               lldpRemTableEntry.getLldpRemChassiid(),
                                                               lldpRemSnmpInterface.getNode().getId(),
                                                               lldpRemSnmpInterface.getIfIndex(),
                                                               lldpLocsnmpIf);
            lldpRemInterfaces.add(lldpremint);
        }
        node.setLldpRemInterfaces(lldpRemInterfaces);
    }

    private Map<Integer, LldpLocTableEntry> getLocalPortNumberToLocalTableEntryMap(
            SnmpCollection snmpcoll) {
        Map<Integer, LldpLocTableEntry> localPortNumberToLocTableEntryMap = new TreeMap<Integer, LldpLocTableEntry>();
        for (final LldpLocTableEntry lldpLocTableEntry : snmpcoll.getLldpLocTable()) {
            localPortNumberToLocTableEntryMap.put(lldpLocTableEntry.getLldpLocPortNum(),
                                                  lldpLocTableEntry);
        }
        return localPortNumberToLocTableEntryMap;

    }

    private OnmsSnmpInterface getLldpRemIfIndex(
            LldpRemTableEntry lldpRemTableEntry) {
        LOG.debug("getLldpRemIfIndex: parsing sysname/porttype/portid: {}/{}/{}",
                  lldpRemTableEntry.getLldpRemSysname(),
                  lldpRemTableEntry.getLldpRemPortidSubtype(),
                  lldpRemTableEntry.getLldpRemPortid());
        OnmsSnmpInterface snmpif = null;
        switch (lldpRemTableEntry.getLldpRemPortidSubtype().intValue()) {
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
            snmpif = getFromSysnameIfAlias(lldpRemTableEntry.getLldpRemSysname(),
                                           lldpRemTableEntry.getLldpRemPortid());
            if (snmpif == null)
                snmpif = getFromSysnameIfName(lldpRemTableEntry.getLldpRemSysname(),
                                              lldpRemTableEntry.getLldpRemPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_PORTCOMPONENT:
            snmpif = getFromSysnamePortComponent(lldpRemTableEntry.getLldpRemSysname(),
                                                 lldpRemTableEntry.getLldpRemPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_MACADDRESS:
            snmpif = getFromSysnameMacAddress(lldpRemTableEntry.getLldpRemSysname(),
                                              lldpRemTableEntry.getLldpRemMacAddress());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
            snmpif = getFromSysnameIpAddress(lldpRemTableEntry.getLldpRemSysname(),
                                             lldpRemTableEntry.getLldpRemIpAddress());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACENAME:
            snmpif = getFromSysnameIfName(lldpRemTableEntry.getLldpRemSysname(),
                                          lldpRemTableEntry.getLldpRemPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID:
            snmpif = getFromSysnameAgentCircuitId(lldpRemTableEntry.getLldpRemSysname(),
                                                  lldpRemTableEntry.getLldpRemPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_LOCAL:
            try {
                snmpif = getFromSysnameIfIndex(lldpRemTableEntry.getLldpRemSysname(),
                                               Integer.parseInt(lldpRemTableEntry.getLldpRemPortid()));
            } catch (NumberFormatException e) {
                snmpif = getFromSysnameIfName(lldpRemTableEntry.getLldpRemSysname(),
                                              lldpRemTableEntry.getLldpRemPortid());
            }
            break;
        }

        return snmpif;
    }

    private Integer getLldpLocIfIndex(String sysname,
            LldpLocTableEntry lldpLocTableEntry) {
        OnmsSnmpInterface snmpif = null;
        LOG.debug("getLldpLocIfIndex: parsing sysname/porttype/portid: {}/{}/{}",
                  sysname, lldpLocTableEntry.getLldpLocPortIdSubtype(),
                  lldpLocTableEntry.getLldpLocPortid());
        switch (lldpLocTableEntry.getLldpLocPortIdSubtype().intValue()) {
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
            snmpif = getFromSysnameIfAlias(sysname,
                                           lldpLocTableEntry.getLldpLocPortid());
            if (snmpif == null)
                snmpif = getFromSysnameIfName(sysname,
                                              lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_PORTCOMPONENT:
            snmpif = getFromSysnamePortComponent(sysname,
                                                 lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_MACADDRESS:
            snmpif = getFromSysnameMacAddress(sysname,
                                              lldpLocTableEntry.getLldpLocMacAddress());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
            snmpif = getFromSysnameIpAddress(sysname,
                                             lldpLocTableEntry.getLldpLocIpAddress());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_INTERFACENAME:
            snmpif = getFromSysnameIfName(sysname,
                                          lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID:
            snmpif = getFromSysnameAgentCircuitId(sysname,
                                                  lldpLocTableEntry.getLldpLocPortid());
            break;
        case LldpMibConstants.LLDP_PORTID_SUBTYPE_LOCAL:
            try {
                return Integer.parseInt(lldpLocTableEntry.getLldpLocPortid());
            } catch (NumberFormatException e) {
                snmpif = getFromSysnameIfName(sysname,
                                              lldpLocTableEntry.getLldpLocPortid());
            }
            break;
        }
        if (snmpif != null)
            return snmpif.getIfIndex();
        return null;
    }

    protected void processCdp(final LinkableNode node,
            final SnmpCollection snmpcoll, final Date scanTime, final Linkd linkd) {
        String cdpDeviceid = snmpcoll.getCdpGlobalGroup().getCdpDeviceId();
        LOG.debug("processCdp: Setting CDP device id {} for node {} with ip primary {}",
                  cdpDeviceid, node.getNodeId(),
                  str(node.getSnmpPrimaryIpAddr()));
        node.setCdpDeviceId(cdpDeviceid);
        if (LOG.isDebugEnabled()) {
            if (snmpcoll.getCdpCacheTable().size() > 0) {
                LOG.debug("processCdp: Starting CDP cache table processing for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processCdp: Zero CDP cache table entries for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }
        Map<Integer, String> cdpifindextoIfnameMap = new TreeMap<Integer, String>();
        if (snmpcoll.hasCdpInterfaceTable()) {
            for (final CdpInterfaceTableEntry cdpEntry : snmpcoll.getCdpInterfaceTable()) {
                LOG.debug("processCdp:adding interface table entries ifindex/ifname {}/{} for node {}",
                          cdpEntry.getCdpInterfaceIfIndex(),
                          cdpEntry.getCdpInterfaceName(), node.getNodeId());
                cdpifindextoIfnameMap.put(cdpEntry.getCdpInterfaceIfIndex(),
                                          cdpEntry.getCdpInterfaceName());
            }
        } else {
            LOG.debug("processCdp:no interface table entries  for node {}",
                      node.getNodeId());
        }

        List<CdpInterface> cdpInterfaces = new ArrayList<CdpInterface>();

        for (final CdpCacheTableEntry cdpEntry : snmpcoll.getCdpCacheTable()) {
            final int cdpIfIndex = cdpEntry.getCdpCacheIfIndex();
            if (cdpIfIndex < 0) {
                LOG.debug("processCdp: ifIndex not valid: {}", cdpIfIndex);
                continue;
            }
            LOG.debug("processCdp: ifIndex found: {}", cdpIfIndex);
            final String cdpTargetDeviceId = cdpEntry.getCdpCacheDeviceId();
            if (cdpTargetDeviceId == null) {
                LOG.warn("processCdp: Target device id not found. Skipping.");
                continue;
            }

            LOG.debug("processCdp: cdpTargetDeviceId found: {}",
                      cdpTargetDeviceId);
            final String cdpTargetIfName = cdpEntry.getCdpCacheDevicePort();
            if (cdpTargetIfName == null) {
                LOG.warn("processCdp: Target device port not found. Skipping.");
                continue;
            }
            LOG.debug("processCdp: Target device port name found: {}",
                      cdpTargetIfName);
            final int cdpAddrType = cdpEntry.getCdpCacheAddressType();
            if (cdpAddrType != CdpInterface.CDP_ADDRESS_TYPE_IP_ADDRESS) {
                LOG.warn("processCdp: CDP address type not ip: {}. Skipping",
                         cdpAddrType);
                continue;
            }
            InetAddress cdpTargetIpAddr = cdpEntry.getCdpCacheIpv4Address();
            LOG.debug("processCdp: cdp cache ip address found: {}",
                      str(cdpTargetIpAddr));
            if (cdpTargetIpAddr == null
                    || cdpTargetIpAddr.isLoopbackAddress()
                    || m_zeroAddress.equals(cdpTargetIpAddr)) {
                LOG.debug("processCdp: IP address is not valid: {}. Skipping",
                          str(cdpTargetIpAddr));
                continue;
            }
            if (!linkd.isInterfaceInPackage(cdpTargetIpAddr,
                                              snmpcoll.getPackageName())) {
                LOG.debug("processCdp: target IP address {} Not in package: {}.  Skipping.",
                          str(cdpTargetIpAddr), snmpcoll.getPackageName());
                continue;
            }
            String cdpIfName = cdpifindextoIfnameMap.get(Integer.valueOf(cdpIfIndex));
            if (cdpIfName == null) {
                OnmsSnmpInterface iface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(node.getNodeId(),
                                                                                       cdpIfIndex);
                if (iface != null)
                    cdpIfName = iface.getIfName();
            }
            final CdpInterface cdpIface = new CdpInterface(cdpIfIndex);
            cdpIface.setCdpIfName(cdpIfName);
            cdpIface.setCdpTargetDeviceId(cdpTargetDeviceId);
            cdpIface.setCdpTargetIfName(cdpTargetIfName);
            LOG.debug("processCdp: Adding cdp interface {} to linkable node {}.",
                      cdpIface, node.getNodeId());
            cdpInterfaces.add(cdpIface);

            LOG.debug("processCdp: try to add cdp interface for non snmp node");
            List<OnmsNode> targetCdpNodeIds = getNodeidFromIp(cdpTargetIpAddr);
            if (targetCdpNodeIds.isEmpty()) {
                LOG.info("processCdp: No Target node IDs found: interface {} not added to linkable SNMP node. Skipping.",
                         str(cdpTargetIpAddr));
                sendNewSuspectEvent(cdpTargetIpAddr, snmpcoll.getTarget(),
                                    snmpcoll.getPackageName(),linkd);
                continue;
            }

            if (targetCdpNodeIds.size() > 1) {
                LOG.info("processCdp: More Then One Target node IDs found: interface {} not added to linkable SNMP node. Skipping adding non snmp node.",
                         str(cdpTargetIpAddr));
                continue;
            }
            OnmsNode targetCdpNode = targetCdpNodeIds.iterator().next();
            if (targetCdpNode.getSysName() == null
                    || targetCdpNode.getSysName().equals("")) {
                LOG.info("processCdp: no snmp Target node ID found: {}.",
                         targetCdpNode.getId());
                final CdpInterface cdpIfaceNotSnmp = new CdpInterface(
                                                                      cdpIfIndex);
                cdpIfaceNotSnmp.setCdpTargetNodeId(targetCdpNode.getId());

                LOG.debug("processCdp: Adding cdp interface {} to linkable node {}.",
                          cdpIfaceNotSnmp, node.getNodeId());
                cdpInterfaces.add(cdpIfaceNotSnmp);
            }
        }
        node.setCdpInterfaces(cdpInterfaces);
    }

    protected void processRouteTable(final OnmsNode onmsNode,
            final LinkableNode node, final SnmpCollection snmpcoll,
            final Date scanTime, final Linkd linkd) {
        if (LOG.isDebugEnabled()) {
            final int routes = snmpcoll.getIpRouteTable().size();
            if (routes > 0) {
                LOG.debug("processRouteTable: Starting route table processing for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
                LOG.debug("processRouteTable: processing # {} routing interfaces",
                          routes);
            } else {
                LOG.debug("processRouteTable: Zero route table entries for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        List<RouterInterface> routeInterfaces = new ArrayList<RouterInterface>();

        for (final SnmpStore ent : snmpcoll.getIpRouteTable()) {

            IpRouteCollectorEntry route = (IpRouteCollectorEntry) ent;

            final InetAddress nexthop = route.getIpRouteNextHop();
            final InetAddress routedest = route.getIpRouteDest();
            final InetAddress routemask = route.getIpRouteMask();
            LOG.debug("processRouteTable: processing routedest/routemask/routenexthop {}/{}/{}",
                      str(routedest), str(routemask), str(nexthop));

            if (linkd.saveRouteTable(snmpcoll.getPackageName())) {
                OnmsIpRouteInterface ipRouteInterface = route.getOnmsIpRouteInterface(new OnmsIpRouteInterface());
                if (ipRouteInterface != null) {
                    LOG.debug("processRouteTable: persisting {}",
                              ipRouteInterface);
                    ipRouteInterface.setNode(onmsNode);
                    ipRouteInterface.setLastPollTime(scanTime);
                    ipRouteInterface.setStatus(StatusType.ACTIVE);

                    saveIpRouteInterface(ipRouteInterface);
                } else {
                    LOG.warn("processRouteTable: cannot persist routing table entry routedest/routemask/routenexthop {}/{}/{}",
                             str(routedest), str(routemask), str(nexthop));
                }
            }

            if (nexthop == null) {
                LOG.warn("processRouteTable: next hop not found on node {}. Skipping.",
                         node.getNodeId());
                continue;
            } else if (nexthop.isLoopbackAddress()) {
                LOG.info("processRouteTable: next hop is a loopback address. Skipping.");
                continue;
            } else if (m_zeroAddress.equals(nexthop)) {
                LOG.info("processRouteTable: next hop is a broadcast address. Skipping.");
                continue;
            } else if (nexthop.isMulticastAddress()) {
                LOG.info("processRouteTable: next hop is a multicast address. Skipping.");
                continue;
            } else if (!linkd.isInterfaceInPackage(nexthop,
                                                        snmpcoll.getPackageName())) {
                LOG.info("processRouteTable: nexthop address {} is not in package {}. Skipping.",
                         str(nexthop), snmpcoll.getPackageName());
                continue;
            }

            if (routedest == null) {
                LOG.warn("processRouteTable: route destination not found on node {}. Skipping.",
                         node.getNodeId());
                continue;
            }

            if (routemask == null) {
                LOG.warn("processRouteTable: route mask not found on node {}. Skipping.",
                         node.getNodeId());
                continue;
            } else if (routemask.getHostAddress().equals("255.255.255.255")) {
                LOG.warn("processRouteTable: route mask 255.255.255.255 on node {}. Skipping.",
                         node.getNodeId());
                continue;
            }

            Integer ifindex = route.getIpRouteIfIndex();

            if (ifindex == null) {
                LOG.warn("processRouteTable: Invalid ifIndex {} on node {}. Skipping.",
                         ifindex, node.getNodeId());
                continue;
            }

            final Integer routemetric1 = route.getIpRouteMetric1();
            if (routemetric1 == null || routemetric1 == -1) {
                LOG.info("processRouteTable: Route metric1 is invalid or \" not used\". checking the route status.");
                final Integer routestatus = route.getIpRouteStatus();
                if (routestatus != null
                        && routestatus.intValue() != IpRouteCollectorEntry.IP_ROUTE_ACTIVE_STATUS) {
                    LOG.info("processRouteTable: Route status {} is not active. Skipping",
                             routestatus);
                    continue;
                }
            }

            LOG.debug("processRouteTable: parsing routeDest/routeMask/nextHop: {}/{}/{} - ifIndex = {}",
                      str(routedest), str(routemask), str(nexthop), ifindex);

            int snmpiftype = -2;
            if (ifindex == 0) {
                LOG.debug("processRouteTable: ifindex is 0. Looking local table to get a valid index.");
                for (OnmsIpInterface ip : m_ipInterfaceDao.findByNodeId(node.getNodeId())) {
                    InetAddress ipaddr = ip.getIpAddress();
                    InetAddress netmask = ip.getSnmpInterface().getNetMask();
                    LOG.debug("processRouteTable: parsing ip {} with netmask {}.",
                              str(ipaddr), str(netmask));
                    InetAddress net1 = Linkd.getNetwork(ip.getIpAddress(),
                                                        netmask);
                    LOG.debug("processRouteTable: found network {}.",
                              str(net1));

                    LOG.debug("processRouteTable: getting network for nexthop {} with netmask {}.",
                              str(nexthop), str(netmask));
                    InetAddress net2 = Linkd.getNetwork(nexthop, netmask);
                    LOG.debug("processRouteTable: found network {}.",
                              str(net2));

                    if (str(net1).equals(str(net2))) {
                        ifindex = (ip.getIfIndex());
                        LOG.debug("processRouteTable: ifindex {} found for local ip {}. ",
                                  ifindex, str(ip.getIpAddress()));
                        break;
                    }
                }
            }

            if (ifindex > 0)
                snmpiftype = getSnmpIfType(node.getNodeId(), ifindex);

            if (snmpiftype <= 0) {
                LOG.warn("processRouteTable: interface has an invalid ifType ({}).",
                         snmpiftype);
            }

            if (linkd.forceIpRoutediscoveryOnEthernet(snmpcoll.getPackageName())) {
                LOG.debug("processRouteTable: forceIpRoutediscoveryOnEthernet is true, no validation for SNMP interface type");
            } else {
                LOG.debug("processRouteTable: forceIpRoutediscoveryOnEthernet is false, checking SNMP interface type");

                if (snmpiftype == SNMP_IF_TYPE_ETHERNET) {
                    LOG.debug("run: Ethernet interface for nexthop {}. Skipping.",
                              nexthop);
                    continue;
                } else if (snmpiftype == SNMP_IF_TYPE_PROP_VIRTUAL) {
                    LOG.debug("run: PropVirtual interface for nodeid {}. Skipping.",
                              nexthop);
                    continue;
                } else if (snmpiftype == SNMP_IF_TYPE_L2_VLAN) {
                    LOG.debug("run: Layer2 VLAN interface for nodeid {}. Skipping.",
                              nexthop);
                    continue;
                } else if (snmpiftype == SNMP_IF_TYPE_L3_VLAN) {
                    LOG.debug("run: Layer3 VLAN interface for nodeid {}. Skipping.",
                              nexthop);
                    continue;
                }
            }

            List<RouterInterface> routeIfaces = getRouteInterface(nexthop,
                                                                  ifindex);
            if (routeIfaces.isEmpty()) {
                LOG.info("processRouteTable: No node ID found for next hop IP address {}. Not adding the IP route interface to the linkable SNMP node.",
                         str(nexthop));
                sendNewSuspectEvent(nexthop, snmpcoll.getTarget(),
                                    snmpcoll.getPackageName(),linkd);
                continue;
            }
            for (RouterInterface routeIface : routeIfaces) {
                if (node.getNodeId() == routeIface.getNextHopNodeid()) {
                    LOG.debug("processRouteTable: node for IP next hop address {} is itself. Skipping.",
                              str(nexthop));
                    continue;
                }
                routeInterfaces.add(routeIface);
            }
        }
        node.setRouteInterfaces(routeInterfaces);
    }

    protected void processVlanTable(final OnmsNode onmsNode,
            final LinkableNode node, final SnmpCollection snmpcoll,
            final Date scanTime) {
        if (LOG.isDebugEnabled()) {
            if (snmpcoll.getVlanTable().size() > 0) {
                LOG.debug("processVlanTable: Starting VLAN table processing for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processVlanTable: Zero VLAN table entries for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        final List<OnmsVlan> vlans = new ArrayList<OnmsVlan>();

        for (final SnmpStore ente : snmpcoll.getVlanTable()) {

            Vlan ent = (Vlan) ente;
            final OnmsVlan vlan = ent.getOnmsVlan();
            vlan.setLastPollTime(scanTime);
            vlan.setNode(onmsNode);
            vlan.setStatus(StatusType.ACTIVE);
            vlans.add(vlan);

            LOG.debug("processVlanTable: Saving VLAN entry: {}", vlan);

            saveVlan(vlan);

        }
    }

    protected void storeSnmpVlanCollection(final OnmsNode onmsNode,
            final LinkableNode node, final OnmsVlan vlan,
            final SnmpVlanCollection snmpVlanColl, final Date scanTime, final Linkd linkd) {

        if (!snmpVlanColl.hasDot1dBase()) {
            LOG.debug("storeSnmpVlanCollection: No Bridge MIB informations found for Vlan: {}. Skipping...",
                      vlan.getVlanName());
            return;
        }

        LOG.debug("storeSnmpVlanCollection: Starting Bridge MIB processing for Vlan: {}.",
                  vlan.getVlanName());
        processDot1dBaseAndDot1dStp(onmsNode, node, vlan, snmpVlanColl,
                                    scanTime, linkd);

        if (snmpVlanColl.hasDot1dBasePortTable()) {
            processDot1dBasePortAndStpPortTables(onmsNode, node, vlan,
                                                 snmpVlanColl, scanTime, linkd);
        }

        if (snmpVlanColl.hasDot1dTpFdbTable()) {
            processDot1DTpFdbTable(node, vlan, snmpVlanColl, scanTime);
        }

        if (snmpVlanColl.hasQBridgeDot1dTpFdbTable()) {
            processQBridgeDot1dTpFdbTable(node, vlan, snmpVlanColl);
        }

        // tt2295 removed adding physical mac address as bridge identifier
        // for (final String physaddr : getPhysAddrs(node.getNodeId())) {
        // LOG.debug("storeSnmpVlanCollection: Try to add Bridge Identifier \"{}\" for node {}",
        // physaddr, node.getNodeId());
        // if (physaddr == null || physaddr.equals("") ||
        // physaddr.equals("000000000000")) continue;
        // LOG.info("storeSnmpVlanCollection: Adding Bridge Identifier {} for node {}",
        // physaddr, node.getNodeId());
        // node.addBridgeIdentifier(physaddr);
        // }

    }

    private void processDot1dBasePortAndStpPortTables(
            final OnmsNode onmsNode, final LinkableNode node,
            final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl,
            final Date scanTime, final Linkd linkd) {
        Map<Integer, OnmsStpInterface> stpinterfaces = new TreeMap<Integer, OnmsStpInterface>();
        stpinterfaces = processDot1DBasePortTable(onmsNode, node, scanTime,
                                                  vlan, snmpVlanColl,
                                                  stpinterfaces);

        if (snmpVlanColl.hasDot1dStpPortTable()) {
            stpinterfaces = processDot1StpPortTable(node, scanTime, vlan,
                                                    snmpVlanColl,
                                                    stpinterfaces);
        }

        if (linkd.saveStpInterfaceTable(snmpVlanColl.getPackageName())) {
            for (OnmsStpInterface stpInterface : stpinterfaces.values()) {
                LOG.debug("processDot1dBasePortAndStpPortTables: saving {} in stpinterface table",
                          stpInterface);
                saveStpInterface(stpInterface);
            }
        }

        for (OnmsStpInterface stpInterface : stpinterfaces.values()) {
            if (stpInterface.getStpPortDesignatedBridge() == null)
                continue;
            if (stpInterface.getStpPortDesignatedBridge().substring(5, 16).equals(snmpVlanColl.getDot1dBase().getBridgeAddress())) {
                LOG.debug("processDot1dBasePortAndStpPortTables: portdesignatedBridge is bridge itself {}. Nothing to add to linkable node ",
                          snmpVlanColl.getDot1dBase().getBridgeAddress());
                continue;
            }
            LOG.debug("processDot1dBasePortAndStpPortTables: portdesignatedBridge/port {}/{} added to linkable node skipped",
                      stpInterface.getStpPortDesignatedBridge(),
                      stpInterface.getBridgePort());
            node.addStpInterface(stpInterface);
        }
    }

    private void processDot1dBaseAndDot1dStp(final OnmsNode onmsNode,
            final LinkableNode node, final OnmsVlan vlan,
            final SnmpVlanCollection snmpVlanColl, final Date scanTime, final Linkd linkd) {

        final String baseBridgeAddress = snmpVlanColl.getDot1dBase().getBridgeAddress();
        if (baseBridgeAddress == null) {
            LOG.info("processDot1dBaseAndDot1dStp: Invalid base bridge address ({}) on node/vlan {}/{}",
                     baseBridgeAddress, node.getNodeId(), vlan.getId());
            return;
        }

        LOG.debug("processDot1dBaseAndDot1dStp: Found Bridge Identifier {} for Vlan {}.",
                  baseBridgeAddress, vlan.getVlanId());
        node.addBridgeIdentifier(baseBridgeAddress, vlan.getVlanId());

        if (snmpVlanColl.hasDot1dStp()) {
            LOG.debug("processDot1dBaseAndDot1dStp: processing Dot1dStpGroup in stpnode");
            final String stpDesignatedRoot = snmpVlanColl.getDot1dStp().getStpDesignatedRoot();

            if (stpDesignatedRoot != null) {
                LOG.debug("processDot1dBaseAndDot1dStp: Dot1dStpGroup found valid stpDesignatedRoot {}, adding to Linkable node",
                          stpDesignatedRoot);
                node.setVlanStpRoot(vlan.getVlanId(), stpDesignatedRoot);
            }
        }

        if (linkd.saveStpNodeTable(snmpVlanColl.getPackageName())) {
            saveStpNode(getOnmsStpNode(onmsNode, node, scanTime, vlan,
                                       snmpVlanColl));
        }
    }

    protected void processQBridgeDot1dTpFdbTable(final LinkableNode node,
            final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getQBridgeDot1dFdbTable().size() > 0) {
                LOG.debug("processQBridgeDot1dTpFdbTable: Starting Q-BRIDGE-MIB dot1dTpFdb table processing for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processQBridgeDot1dTpFdbTable: Zero Q-BRIDGE-MIB dot1dTpFdb table entries for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final QBridgeDot1dTpFdbTableEntry dot1dfdbentry : snmpVlanColl.getQBridgeDot1dFdbTable()) {
            final String curMacAddress = dot1dfdbentry.getQBridgeDot1dTpFdbAddress();

            if (curMacAddress == null || curMacAddress.equals("000000000000")) {
                LOG.info("processQBridgeDot1DTpFdbTable: Invalid MAC addres {} on node {}. Skipping.",
                         curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processQBridgeDot1DTpFdbTable: Found MAC address {} on node {}",
                      curMacAddress, node.getNodeId());

            final int fdbport = dot1dfdbentry.getQBridgeDot1dTpFdbPort();

            if (fdbport == -1) {
                LOG.debug("processQBridgeDot1DTpFdbTable: Invalid FDB port ({}) for MAC address {} on node {}. Skipping.",
                          fdbport, curMacAddress, node.getNodeId());
                continue;
            } else if (fdbport == 0) {
                LOG.debug("processQBridgeDot1DTpFdbTable: FDB port ({}) for MAC address {} on node {}. Saving generic port.",
                          fdbport, curMacAddress, node.getNodeId());
            }

            LOG.debug("processQBridgeDot1DTpFdbTable: Found bridge port {} on node {}.",
                      fdbport, node.getNodeId());

            final int curfdbstatus = dot1dfdbentry.getQBridgeDot1dTpFdbStatus();

            if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_LEARNED) {
                node.addBridgeForwardingTableEntry(fdbport, curMacAddress);
                LOG.debug("processQBridgeDot1DTpFdbTable: Found learned status on bridge port.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_SELF) {
                Integer ifIndex = node.getIfindexFromBridgePort(fdbport);
                if (ifIndex == null)
                    ifIndex = -1;
                node.getMacIdentifiers().put(ifIndex, curMacAddress);
                LOG.debug("processQBridgeDot1DTpFdbTable: MAC address ({}) is used as port identifier.",
                          curMacAddress);
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_INVALID) {
                LOG.debug("processQBridgeDot1DTpFdbTable: Found 'INVALID' status. Skipping.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_MGMT) {
                node.addBridgeForwardingTableEntry(fdbport, curMacAddress);
                LOG.debug("processQBridgeDot1DTpFdbTable: Found 'MGMT' status. Saving.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_OTHER) {
                node.addBridgeForwardingTableEntry(fdbport, curMacAddress);
                LOG.debug("processQBridgeDot1DTpFdbTable: Found 'OTHER' status. Saving.");
            } else if (curfdbstatus == -1) {
                LOG.warn("processQBridgeDot1DTpFdbTable: Unable to determine status. Skipping.");
            }
        }
    }

    protected void processDot1DTpFdbTable(LinkableNode node,
            final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl,
            Date scanTime) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getDot1dFdbTable().size() > 0) {
                LOG.debug("processDot1DTpFdbTable: Starting dot1dTpFdb table processing for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processDot1DTpFdbTable: Zero dot1dTpFdb table entries for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final Dot1dTpFdbTableEntry dot1dfdbentry : snmpVlanColl.getDot1dFdbTable()) {
            final String curMacAddress = dot1dfdbentry.getDot1dTpFdbAddress();
            final int fdbport = dot1dfdbentry.getDot1dTpFdbPort();
            final int curfdbstatus = dot1dfdbentry.getDot1dTpFdbStatus();

            if (curMacAddress == null || curMacAddress.equals("000000000000")) {
                LOG.info("processDot1DTpFdbTable: Invalid MAC address {} on node {}. Skipping.",
                         curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processDot1DTpFdbTable: Found valid MAC address {} on node {}",
                      curMacAddress, node.getNodeId());

            if (fdbport == 0 || fdbport == -1) {
                LOG.debug("processDot1DTpFdbTable: Invalid FDB port ({}) for MAC address {} on node {}. Skipping.",
                          fdbport, curMacAddress, node.getNodeId());
                continue;
            }

            LOG.debug("processDot1DTpFdbTable: MAC address ({}) found on bridge port {} on node {}",
                      curMacAddress, fdbport, node.getNodeId());

            if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_LEARNED
                    && vlan.getVlanId() != null) {
                node.addBridgeForwardingTableEntry(fdbport, curMacAddress);
                LOG.debug("processDot1DTpFdbTable: Found learned status on bridge port.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_SELF) {
                Integer ifIndex = node.getIfindexFromBridgePort(fdbport);
                if (ifIndex == null)
                    ifIndex = -1;
                node.getMacIdentifiers().put(ifIndex, curMacAddress);
                LOG.debug("processDot1DTpFdbTable: MAC address ({}) is used as port identifier.",
                          curMacAddress);
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_INVALID) {
                LOG.debug("processDot1DTpFdbTable: Found 'INVALID' status. Skipping.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_MGMT) {
                node.addBridgeForwardingTableEntry(fdbport, curMacAddress);
                LOG.debug("processDot1DTpFdbTable: Found 'MGMT' status. Saving.");
            } else if (curfdbstatus == SNMP_DOT1D_FDB_STATUS_OTHER) {
                node.addBridgeForwardingTableEntry(fdbport, curMacAddress);
                LOG.debug("processDot1DTpFdbTable: Found 'OTHER' status. Saving.");
            } else if (curfdbstatus == -1) {
                LOG.warn("processDot1DTpFdbTable: Unable to determine status. Skipping.");
            }
        }
    }

    protected Map<Integer, OnmsStpInterface> processDot1StpPortTable(
            final LinkableNode node, final Date scanTime,
            final OnmsVlan vlan, SnmpVlanCollection snmpVlanColl,
            Map<Integer, OnmsStpInterface> stpinterfaces) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getDot1dStpPortTable().size() > 0) {
                LOG.debug("processDot1StpPortTable: Processing dot1StpPortTable for nodeid/ip for {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processDot1StpPortTable: Zero dot1StpPort table entries for nodeid/ip {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final Dot1dStpPortTableEntry dot1dstpptentry : snmpVlanColl.getDot1dStpPortTable()) {

            final Integer stpport = dot1dstpptentry.getDot1dStpPort();

            if (stpport == null || stpinterfaces.get(stpport) == null) {
                LOG.info("processDot1StpPortTable: Found invalid bridge port. Skipping.");
                continue;
            }

            final OnmsStpInterface stpInterface = dot1dstpptentry.getOnmsStpInterface(stpinterfaces.get(stpport));

            LOG.debug("processDot1StpPortTable: found stpport/designatedbridge/designatedport {}/{}/{}",
                      stpport, stpInterface.getStpPortDesignatedBridge(),
                      stpInterface.getStpPortDesignatedPort());
        }
        return stpinterfaces;
    }

    protected Map<Integer, OnmsStpInterface> processDot1DBasePortTable(
            final OnmsNode onmsNode, final LinkableNode node,
            final Date scanTime, final OnmsVlan vlan,
            final SnmpVlanCollection snmpVlanColl,
            Map<Integer, OnmsStpInterface> stpinterfaces) {
        if (LOG.isDebugEnabled()) {
            if (snmpVlanColl.getDot1dBasePortTable().size() > 0) {
                LOG.debug("processDot1DBasePortTable: Processing dot1BasePortTable for nodeid/ip {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            } else {
                LOG.debug("processDot1DBasePortTable: Zero dot1BasePort table entries for nodeid/ip {}/{}",
                          node.getNodeId(), str(node.getSnmpPrimaryIpAddr()));
            }
        }

        for (final Dot1dBasePortTableEntry dot1dbaseptentry : snmpVlanColl.getDot1dBasePortTable()) {
            int baseport = dot1dbaseptentry.getBaseBridgePort();
            int ifindex = dot1dbaseptentry.getBaseBridgePortIfindex();
            LOG.debug("processDot1DBasePortTable: processing bridge port ({}) with ifIndex ({}).",
                      baseport, ifindex);

            if (baseport == -1 || ifindex == -1) {
                LOG.info("processDot1DBasePortTable: Invalid base port ({}) or ifIndex ({}). Skipping.",
                         baseport, ifindex);
                continue;
            }

            node.setIfIndexBridgePort(ifindex, baseport);

            final OnmsStpInterface stpInterface = new OnmsStpInterface(
                                                                       onmsNode,
                                                                       baseport,
                                                                       vlan.getVlanId());
            stpInterface.setBridgePort(baseport);
            stpInterface.setVlan(vlan.getVlanId());
            stpInterface.setIfIndex(ifindex);
            stpInterface.setStatus(StatusType.ACTIVE);
            stpInterface.setLastPollTime(scanTime);

            stpinterfaces.put(baseport, stpInterface);
        }
        return stpinterfaces;
    }

    protected OnmsStpNode getOnmsStpNode(final OnmsNode onmsNode,
            final LinkableNode node, final Date scanTime,
            final OnmsVlan vlan, final SnmpVlanCollection snmpVlanColl) {
        LOG.debug("getOnmsStpNode: Starting stpnode processing for Vlan: {}",
                  vlan.getVlanName());

        LOG.debug("getOnmsStpNode: processing Dot1dBaseGroup in stpnode");
        OnmsStpNode stpNode = new OnmsStpNode(onmsNode, vlan.getVlanId());
        stpNode = snmpVlanColl.getDot1dBase().getOnmsStpNode(stpNode);
        stpNode.setLastPollTime(scanTime);
        stpNode.setStatus(StatusType.ACTIVE);
        stpNode.setBaseVlanName(vlan.getVlanName());

        if (snmpVlanColl.hasDot1dStp()) {
            LOG.debug("getOnmsStpNode: processing Dot1dStpGroup in stpnode");

            stpNode = snmpVlanColl.getDot1dStp().getOnmsStpNode(stpNode);

            if (stpNode.getStpDesignatedRoot() == null) {
                LOG.debug("getOnmsStpNode: Dot1dStpGroup found stpDesignatedRoot null, not adding to Linkable node");
                stpNode.setStpDesignatedRoot("0000000000000000");
            }
            LOG.debug("getOnmsStpNode: stpDesignatedRoot = {}",
                      stpNode.getStpDesignatedRoot());
        }
        return stpNode;
    }

}
