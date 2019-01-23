/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.shell.topogen;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyPersister {

    private final static String NODES_INSERT = "INSERT INTO node (nodeid, nodelabel, location, nodecreatetime) VALUES (?, ?, ?, now());";
    private final static String NODES_DELETE = "DELETE FROM node;";
    private final static String CDP_ELEMENTS_INSERT = "INSERT INTO cdpelement (id, nodeid, cdpglobalrun, cdpglobaldeviceid, cdpnodelastpolltime, cdpnodecreatetime) VALUES (?, ?, ?, ?, ?, now());";
    private final static String CDP_ELEMENTS_DELETE = "DELETE FROM cdpelement;";
    private final static String CDP_LINKS_INSERT = "INSERT INTO cdplink (id, nodeid, cdpcacheifindex, cdpinterfacename, cdpcacheaddresstype, cdpcacheaddress, cdpcacheversion, cdpcachedeviceid, cdpcachedeviceport, cdpcachedeviceplatform, cdplinklastpolltime, cdpcachedeviceindex, cdplinkcreatetime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now());";
    private final static String CDP_LINKS_DELETE = "DELETE FROM cdplink;";
    private final static String ISIS_ELEMENTS_INSERT = "INSERT INTO isiselement (id, nodeId, isisSysAdminState, isisSysID, isisNodeLastPollTime, isisNodeCreateTime) VALUES (?, ?, ?, ?, ?, now());";
    private final static String ISIS_ELEMENTS_DELETE = "DELETE FROM isiselement;";
    private final static String ISIS_LINKS_INSERT = "INSERT INTO isislink (id, nodeId, isisCircIndex, isisISAdjIndex, isisCircIfIndex, isisCircAdminState, isisISAdjState, isisISAdjNeighSNPAAddress, isisISAdjNeighSysType, isisISAdjNeighSysID," +
            "isisISAdjNbrExtendedCircID, isisLinkLastPollTime, isisLinkCreateTime ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now());";
    private final static String ISIS_LINKS_DELETE = "DELETE FROM isislink;";

    private final static String LLDP_ELEMENTS_INSERT = "INSERT INTO lldpelement (id, nodeId, lldpChassisIdSubType, lldpSysname, lldpChassisId, lldpNodeLastPollTime, lldpNodeCreateTime) VALUES (?, ?, ?, ?, ?, ?, now());";
    private final static String LLDP_ELEMENTS_DELETE = "DELETE FROM lldpelement;";
    private final static String LLDP_LINKS_INSERT = "INSERT INTO lldplink (id, nodeId, lldpLocalPortNum, lldpPortIdSubType, lldpPortId, lldpPortDescr, lldpPortIfindex, lldpRemChassisId, lldpRemSysname, lldpRemChassisIdSubType, lldpRemPortIdSubType," +
            " lldpRemPortId, lldpRemPortDescr, lldpLinkLastPollTime, lldpLinkCreateTime ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now());";
    private final static String LLDP_LINKS_DELETE = "DELETE FROM lldplink;";

    private final static String OSPF_LINKS_INSERT = "INSERT INTO ospflink (id, nodeId, ospfIpAddr, ospfIpMask, ospfAddressLessIndex, ospfIfIndex, ospfRemRouterId, ospfRemIpAddr, ospfRemAddressLessIndex, ospfLinkLastPollTime, ospfLinkCreateTime ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now());";
    private final static String OSPF_LINKS_DELETE = "DELETE FROM ospflink;";

    private final static String ONMS_INTERFACES_INSERT  = "INSERT INTO snmpinterface (id, snmpPhysAddr, snmpIfIndex, snmpIfDescr," +
            " snmpIfType, snmpIfName, snmpIfSpeed, snmpIfAdminStatus, snmpIfOperStatus, snmpIfAlias, snmpLastCapsdPoll," +
            " snmpCollect, snmpPoll, snmpLastSnmpPoll, nodeId, hasFlows ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private final static String ONMS_INTERFACES_DELETE = "DELETE FROM snmpinterface;";

    private final static String IP_INTERFACES_INSERT = "INSERT INTO ipinterface (id, ipHostName, isManaged, ipLastCapsdPoll, isSnmpPrimary, nodeId, snmpInterfaceId, ipAddr, netmask) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private final static String IP_INTERFACES_DELETE = "DELETE FROM ipinterface;";

    private final static Logger LOG = LoggerFactory.getLogger(TopologyPersister.class);

    private DataSource ds;

    public TopologyPersister() throws IOException {
        setUpDatasource();
    }

    public void setUpDatasource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/opennms");
        config.setUsername("opennms");
        config.setPassword("opennms");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }


    public void persistNodes(List<OnmsNode> nodes) throws SQLException {
        batchInsert(NODES_INSERT, nodes, new BiConsumerWithException<PreparedStatement, OnmsNode>() {
            @Override
            public void accept(PreparedStatement stmt, OnmsNode node) throws SQLException {
                stmt.setInt(1, node.getId());
                stmt.setString(2, node.getLabel());
                stmt.setString(3, node.getLocation().getLocationName());
            }
        });
    }

    public void persistCdpElements(List<CdpElement> elements) throws SQLException {
        batchInsert(CDP_ELEMENTS_INSERT, elements, new BiConsumerWithException<PreparedStatement, CdpElement>() {
            @Override
            public void accept(PreparedStatement stmt, CdpElement element) throws SQLException {
                stmt.setInt(1, element.getId());
                stmt.setInt(2, element.getNode().getId());
                stmt.setInt(3, element.getCdpGlobalRun().getValue());
                stmt.setString(4, element.getCdpGlobalDeviceId());
                stmt.setDate(5, new java.sql.Date(element.getCdpNodeLastPollTime().getTime()));
            }
        });
    }

    public void persistIsIsElements(List<IsIsElement> elements) throws SQLException {
        batchInsert(ISIS_ELEMENTS_INSERT, elements, new BiConsumerWithException<PreparedStatement, IsIsElement>() {
            @Override
            public void accept(PreparedStatement stmt, IsIsElement element) throws SQLException {
                stmt.setInt(1, element.getId());
                stmt.setInt(2, element.getNode().getId());
                stmt.setInt(3, element.getIsisSysAdminState().getValue());
                stmt.setString(4, element.getIsisSysID());
                stmt.setDate(5, new java.sql.Date(element.getIsisNodeLastPollTime().getTime()));
            }
        });
    }

    public void persistLldpElements(List<LldpElement> elements) throws SQLException {
        batchInsert(LLDP_ELEMENTS_INSERT, elements, new BiConsumerWithException<PreparedStatement, LldpElement>() {
            @Override
            public void accept(PreparedStatement stmt, LldpElement element) throws SQLException {
                stmt.setInt(1, element.getId());
                stmt.setInt(2, element.getNode().getId());
                stmt.setInt(3, element.getLldpChassisIdSubType().getValue());
                stmt.setString(4, element.getLldpSysname());
                stmt.setString(5, element.getLldpChassisId());
                stmt.setDate(6, new java.sql.Date(element.getLldpNodeLastPollTime().getTime()));
            }
        });
    }

    public void persistCdpLinks(List<CdpLink> links) throws SQLException {
        batchInsert(CDP_LINKS_INSERT, links, new BiConsumerWithException<PreparedStatement, CdpLink>() {
            @Override
            public void accept(PreparedStatement stmt, CdpLink link) throws SQLException {
                int i = 1;
                stmt.setInt(i++, link.getId());
                stmt.setInt(i++, link.getNode().getId());
                stmt.setInt(i++, link.getCdpCacheIfIndex());
                stmt.setString(i++, link.getCdpInterfaceName());
                stmt.setInt(i++, link.getCdpCacheAddressType().getValue());
                stmt.setString(i++, link.getCdpCacheAddress());
                stmt.setString(i++, link.getCdpCacheVersion());
                stmt.setString(i++, link.getCdpCacheDeviceId());
                stmt.setString(i++, link.getCdpCacheDevicePort());
                stmt.setString(i++, link.getCdpCacheDevicePlatform());
                stmt.setDate(i++, new java.sql.Date(link.getCdpLinkLastPollTime().getTime()));
                stmt.setInt(i, link.getCdpCacheDeviceIndex());
            }
        });
    }

    public void persistIsIsLinks(List<IsIsLink> links) throws SQLException {
        batchInsert(ISIS_LINKS_INSERT, links, new BiConsumerWithException<PreparedStatement, IsIsLink>() {
            @Override
            public void accept(PreparedStatement stmt, IsIsLink link) throws SQLException {
                int i = 1;
                stmt.setInt(i++, link.getId());
                stmt.setInt(i++, link.getNode().getId());
                stmt.setInt(i++, link.getIsisCircIndex());
                stmt.setInt(i++, link.getIsisISAdjIndex());
                stmt.setInt(i++, link.getIsisCircIfIndex());
                stmt.setInt(i++, link.getIsisCircAdminState().getValue());
                stmt.setInt(i++, link.getIsisISAdjState().getValue());
                stmt.setString(i++, link.getIsisISAdjNeighSNPAAddress());
                stmt.setInt(i++, link.getIsisISAdjNeighSysType().getValue());
                stmt.setString(i++, link.getIsisISAdjNeighSysID());
                stmt.setInt(i++, link.getIsisISAdjNbrExtendedCircID());
                stmt.setDate(i++, new java.sql.Date(link.getIsisLinkLastPollTime().getTime()));
            }
        });
    }

    public void persistLldpLinks(List<LldpLink> links) throws SQLException {
        batchInsert(LLDP_LINKS_INSERT, links, new BiConsumerWithException<PreparedStatement, LldpLink>() {
            @Override
            public void accept(PreparedStatement stmt, LldpLink link) throws SQLException {
                int i = 1;
                stmt.setInt(i++, link.getId());
                stmt.setInt(i++, link.getNode().getId());
                stmt.setInt(i++, link.getLldpLocalPortNum());
                stmt.setInt(i++, link.getLldpPortIdSubType().getValue());
                stmt.setString(i++, link.getLldpPortId());
                stmt.setString(i++, link.getLldpPortDescr());
                stmt.setInt(i++, link.getLldpPortIfindex());
                stmt.setString(i++, link.getLldpRemChassisId());
                stmt.setString(i++, link.getLldpRemSysname());
                stmt.setInt(i++, link.getLldpRemChassisIdSubType().getValue());
                stmt.setInt(i++, link.getLldpRemPortIdSubType().getValue());
                stmt.setString(i++, link.getLldpRemPortId());
                stmt.setString(i++, link.getLldpRemPortDescr());
                stmt.setDate(i++, new java.sql.Date(link.getLldpLinkLastPollTime().getTime()));
            }
        });
    }

    public void persistOspfLinks(List<OspfLink> links) throws SQLException {
        batchInsert(OSPF_LINKS_INSERT, links, new BiConsumerWithException<PreparedStatement, OspfLink>() {
            @Override
            public void accept(PreparedStatement stmt, OspfLink link) throws SQLException {
                int i = 1;
                stmt.setInt(i++, link.getId());
                stmt.setInt(i++, link.getNode().getId());
                stmt.setString(i++, InetAddressUtils.str(link.getOspfIpAddr()));
                stmt.setString(i++, InetAddressUtils.str(link.getOspfIpMask()));
                stmt.setInt(i++, link.getOspfAddressLessIndex());
                stmt.setInt(i++, link.getOspfIfIndex());
                stmt.setString(i++, InetAddressUtils.str(link.getOspfRemRouterId()));
                stmt.setString(i++, InetAddressUtils.str(link.getOspfRemIpAddr()));
                stmt.setInt(i++, link.getOspfRemAddressLessIndex());
                stmt.setDate(i++, new java.sql.Date(link.getOspfLinkLastPollTime().getTime()));
            }
        });
    }

    public void persistOnmsInterfaces(List<OnmsSnmpInterface> onmsSnmpInterfaces) throws SQLException{
        batchInsert(ONMS_INTERFACES_INSERT, onmsSnmpInterfaces, new BiConsumerWithException<PreparedStatement, OnmsSnmpInterface>() {
            @Override
            public void accept(PreparedStatement stmt, OnmsSnmpInterface snmpInterface) throws SQLException {
                int i = 1;
                stmt.setInt(i++, snmpInterface.getId());
                stmt.setString(i++, snmpInterface.getPhysAddr());
                stmt.setInt(i++, snmpInterface.getIfIndex());
                stmt.setString(i++, snmpInterface.getIfDescr());
                stmt.setInt(i++, snmpInterface.getIfType());
                stmt.setString(i++, snmpInterface.getIfName());
                stmt.setLong(i++, snmpInterface.getIfSpeed());
                stmt.setInt(i++, snmpInterface.getIfAdminStatus());
                stmt.setInt(i++, snmpInterface.getIfOperStatus());
                stmt.setString(i++, snmpInterface.getIfAlias());
                stmt.setDate(i++, new java.sql.Date(snmpInterface.getLastCapsdPoll().getTime()));
                stmt.setString(i++, snmpInterface.getCollect());
                stmt.setString(i++, snmpInterface.getPoll());
                stmt.setDate(i++, new java.sql.Date(snmpInterface.getLastSnmpPoll().getTime()));
                stmt.setInt(i++, snmpInterface.getNode().getId());
                stmt.setBoolean(i++, snmpInterface.getHasFlows());
            }
        });
    }

    public void persistIpInterfaces(List<OnmsIpInterface> ipInterfaces) throws SQLException {
        batchInsert(IP_INTERFACES_INSERT, ipInterfaces, new BiConsumerWithException<PreparedStatement, OnmsIpInterface>() {
            @Override
            public void accept(PreparedStatement stmt, OnmsIpInterface ip) throws SQLException {
                int i = 1;
                stmt.setInt(i++, ip.getId());
                stmt.setString(i++, ip.getIpHostName());
                stmt.setString(i++, ip.getIsManaged());
                stmt.setDate(i++, new java.sql.Date(ip.getIpLastCapsdPoll().getTime()));
                stmt.setString(i++, ip.getPrimaryString());
                stmt.setInt(i++, ip.getNode().getId());
                stmt.setInt(i++, ip.getSnmpInterface().getId());
                stmt.setString(i++, InetAddressUtils.str(ip.getIpAddress()));
                stmt.setString(i++, InetAddressUtils.str(ip.getNetMask()));
            }
        });
    }

    @FunctionalInterface
    public interface BiConsumerWithException<T, R> {
        void accept(T t, R r) throws SQLException;
    }

    private <T> void batchInsert(String statement, List<T> elements, BiConsumerWithException<PreparedStatement, T> statementFiller) throws SQLException {
        if (elements.size() == 0) {
            return;
        }
        LOG.info("inserting {} {}s", elements.size(), elements.get(0).getClass().getSimpleName());
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement insStmt = c.prepareStatement(statement)) {
                int i;
                for (i = 0; i < elements.size(); i++) {
                    T element = elements.get(i);
                    statementFiller.accept(insStmt, element);
                    insStmt.executeUpdate();
                    insStmt.getGeneratedKeys();
                    if (i % 100 == 0) { // batches of 100
                        insStmt.executeBatch();
                    }
                }
                if (i % 100 != 0) {
                    insStmt.executeBatch(); // insert last elements of batch
                }
            }
        }
        LOG.info("inserting of {} {}s done.", elements.size(), elements.get(0).getClass().getSimpleName());
    }

    public void deleteTopology() throws SQLException {
        LOG.info("deleting existing topology");
        List<String> deleteOperations = Arrays.asList(CDP_LINKS_DELETE,
                ISIS_LINKS_DELETE, LLDP_LINKS_DELETE, CDP_ELEMENTS_DELETE, ISIS_ELEMENTS_DELETE, LLDP_ELEMENTS_DELETE,
                OSPF_LINKS_DELETE, IP_INTERFACES_DELETE, ONMS_INTERFACES_DELETE, NODES_DELETE);

        try (Connection c = ds.getConnection()) {
            for (String sql : deleteOperations) {
                try (PreparedStatement stmt = c.prepareStatement(sql)) {
                    stmt.execute();
                }
            }

        }
    }
}


