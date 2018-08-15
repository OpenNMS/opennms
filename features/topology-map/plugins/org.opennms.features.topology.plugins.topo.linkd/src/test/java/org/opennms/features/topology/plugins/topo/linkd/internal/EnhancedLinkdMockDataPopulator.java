/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.opennms.core.test.OnmsAssert;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.ProtocolSupported;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.springframework.beans.factory.annotation.Autowired;

public class EnhancedLinkdMockDataPopulator {

    @Autowired
    private BridgeTopologyDao m_bridgeTopologyDao;

    @Autowired
    private CdpLinkDao m_cdpLinkDao;

    @Autowired
    private CdpElementDao m_cdpElementDao;

    @Autowired
    private IsIsLinkDao m_isisLinkDao;

    @Autowired
    private IsIsElementDao m_isisElementDao;

    @Autowired
    private LldpLinkDao m_lldpLinkDao;

    @Autowired
    private LldpElementDao m_lldpElementDao;

    @Autowired
    private OspfLinkDao m_ospfLinkDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private IpNetToMediaDao m_ipNetToMediaDao;

    @Autowired
    private OperationContext m_operationContext;

    @Autowired
    private GraphContainer m_graphContainer;

    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_node6;
    private OnmsNode m_node7;
    private OnmsNode m_node8;

    private List<OnmsNode> m_nodes;
    private List<LldpLink> m_links;
    private List<OspfLink> m_ospfLinks;

    public void populateDatabase() {

        final String icmp = "ICMP";
        final String snmp = "SNMP";
        final String http = "HTTP";

        final NetworkBuilder builder = new NetworkBuilder();

        setNode1(builder.addNode("node1").setForeignSource("imported:").setForeignId("1").setType(OnmsNode.NodeType.ACTIVE).setSysObjectId("1.3.6.1.4.1.5813.1.25").getNode());
        Assert.assertNotNull("newly built node 1 should not be null", getNode1());
        builder.setBuilding("HQ");
        builder.addSnmpInterface(1)
                .setCollectionEnabled(true)
                .setIfOperStatus(1)
                .setIfSpeed(10000000)
                .setIfDescr("ATM0")
                .setIfAlias("Initial ifAlias value")
                .setIfType(37)
                .addIpInterface("192.168.1.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addSnmpInterface(2)
                .setCollectionEnabled(true)
                .setIfOperStatus(1)
                .setIfSpeed(10000000)
                .setIfName("eth0")
                .setIfType(6)
                .addIpInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addSnmpInterface(3)
                .setCollectionEnabled(false)
                .setIfOperStatus(1)
                .setIfSpeed(10000000)
                .addIpInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        builder.addSnmpInterface(4)
                .setCollectionEnabled(false)
                .setIfOperStatus(1)
                .setIfSpeed(10000000)
                .addIpInterface("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        final OnmsNode node1 = builder.getCurrentNode();
        node1.setLldpElement(new LldpElement(node1, "node1ChassisId", "node1SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode1(node1);

        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE);
        builder.setBuilding("HQ");
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("192.168.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("192.168.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node2 = builder.getCurrentNode();
        node2.setLldpElement(new LldpElement(node2, "node2ChassisId", "node2SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode2(node2);

        builder.addNode("node3").setForeignSource("imported:").setForeignId("3").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("192.168.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("192.168.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node3 = builder.getCurrentNode();
        node3.setLldpElement(new LldpElement(node3, "node3ChassisId", "node3SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode3(node3);

        builder.addNode("node4").setForeignSource("imported:").setForeignId("4").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("192.168.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("192.168.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node4 = builder.getCurrentNode();
        node4.setLldpElement(new LldpElement(node4, "node4ChassisId", "node4SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode4(node4);

        //This node purposely doesn't have a foreignId style assetNumber
        builder.addNode("alternate-node1").setType(OnmsNode.NodeType.ACTIVE).getAssetRecord().setAssetNumber("5");
        builder.addInterface("10.1.1.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.1.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.1.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node5 = builder.getCurrentNode();
        node5.setLldpElement(new LldpElement(node5, "node5ChassisId", "node5SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode5(node5);

        //This node purposely doesn't have a assetNumber and is used by a test to check the category
        builder.addNode("alternate-node2").setType(OnmsNode.NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node6 = builder.getCurrentNode();
        node6.setLldpElement(new LldpElement(node6, "node6ChassisId", "node6SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode6(node6);

        builder.addNode("alternate-node3").setType(OnmsNode.NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node7 = builder.getCurrentNode();
        node7.setLldpElement(new LldpElement(node7, "node7ChassisId", "node7SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode7(node7);

        builder.addNode("alternate-node4").setType(OnmsNode.NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node8 = builder.getCurrentNode();
        node8.setLldpElement(new LldpElement(node8, "node8ChassisId", "mode8SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode8(node8);

        List<OnmsNode> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        nodes.add(node5);
        nodes.add(node6);
        nodes.add(node7);
        nodes.add(node8);
        setNodes(nodes);

        //final OnmsNode node, final int ifIndex, final int nodeParentId, final int parentIfIndex, final StatusType status,final Date lastPollTime
        /*OnmsNode node, Integer localPortNum, Integer portIfIndex, String portId,
                String portDescr, String remChassisId, String remSysname, LldpElement.LldpChassisIdSubType
        remChassisIdSubType,
                String remPortId, String remPortDescr*/
        final LldpLink dli12 = createLldpLink(getNode1(), "node1PortId", "node1PortDescr", 12, 10, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode2().getLldpElement(), "node2PortDescr", "node2PortId");
        final LldpLink dli21 = createLldpLink(getNode2(), "node2PortId", "node2PortDescr", 22, 20, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode1().getLldpElement(), "node1PortDescr", "node1PortId");

        final LldpLink dli23 = createLldpLink(getNode2(), "node2PortId", "node2PortDescr", 22, 20, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode3().getLldpElement(), "node3PortDescr", "node3PortId");
        final LldpLink dli32 = createLldpLink(getNode3(), "node3PortId", "node3PortDescr", 33, 30, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode2().getLldpElement(), "node2PortDescr", "node2PortId");

        final LldpLink dli34 = createLldpLink(getNode3(), "node3PortId", "node3PortDescr", 33, 30, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode4().getLldpElement(), "node4PortDescr", "node4PortId");
        final LldpLink dli43 = createLldpLink(getNode4(), "node4PortId", "node4PortDescr", 44, 40, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode3().getLldpElement(), "node3PortDescr", "node3PortId");

        final LldpLink dli45 = createLldpLink(getNode4(), "node4PortId", "node4PortDescr", 44, 40, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode5().getLldpElement(), "node5PortDescr", "node5PortId");
        final LldpLink dli54 = createLldpLink(getNode5(), "node5PortId", "node5PortDescr", 55, 50, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode4().getLldpElement(), "node4PortDescr", "node4PortId");

        final LldpLink dli56 = createLldpLink(getNode5(), "node5PortId", "node5PortDescr", 55, 50, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode6().getLldpElement(), "node6PortDescr", "node6PortId");
        final LldpLink dli65 = createLldpLink(getNode6(), "node6PortId", "node6PortDescr", 66, 60, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode5().getLldpElement(), "node5PortDescr", "node5PortId");

        final LldpLink dli67 = createLldpLink(getNode6(), "node6PortId", "node6PortDescr", 66, 60, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode7().getLldpElement(), "node7PortDescr", "node7PortId");
        final LldpLink dli76 = createLldpLink(getNode7(), "node7PortId", "node7PortDescr", 77, 70, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode6().getLldpElement(), "node6PortDescr", "node6PortId");

        final LldpLink dli78 = createLldpLink(getNode7(), "node7PortId", "node7PortDescr", 77, 70, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode8().getLldpElement(), "node8PortDescr", "node8PortId");
        final LldpLink dli87 = createLldpLink(getNode8(), "node8PortId", "node8PortDescr", 88, 80, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode7().getLldpElement(), "node7PortDescr", "node7PortId");

        final LldpLink dli81 = createLldpLink(getNode8(), "node8PortId", "node8PortDescr", 88, 80, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode1().getLldpElement(), "node1PortDescr", "node1PortId");
        final LldpLink dli18 = createLldpLink(getNode1(), "node1PortId", "node1PortDescr", 12, 10, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode8().getLldpElement(), "node8PortDescr", "node8PortId");

        dli12.setId(10012);
        dli21.setId(10021);

        dli23.setId(10023);
        dli32.setId(10032);

        dli34.setId(10034);
        dli43.setId(10043);

        dli45.setId(10045);
        dli54.setId(10054);

        dli56.setId(10056);
        dli65.setId(10065);

        dli67.setId(10067);
        dli76.setId(10076);

        dli78.setId(10078);
        dli87.setId(10087);

        dli81.setId(10081);
        dli18.setId(10018);

        List<LldpLink> links = new ArrayList<>();

        links.add(dli12);
        links.add(dli21);

        links.add(dli23);
        links.add(dli32);

        links.add(dli34);
        links.add(dli43);

        links.add(dli45);
        links.add(dli54);

        links.add(dli56);
        links.add(dli65);

        links.add(dli67);
        links.add(dli76);

        links.add(dli78);
        links.add(dli87);

        links.add(dli81);
        links.add(dli18);
        setLinks(links);

        //OSPF links
        OspfLink ospfLink12 = createOspfLink(getNode1(), "192.168.100.246", "255.255.255.252", 0, 10101, "192.168.100.249", "192.168.100.245", 0);
        OspfLink ospfLink21 = createOspfLink(getNode2(), "192.168.100.245", "255.255.255.252", 0, 10101, "192.168.100.250", "192.168.100.246", 0);

        ospfLink12.setId(10112);
        ospfLink21.setId(10121);

        List<OspfLink> ospfLinks = new ArrayList<>();
        ospfLinks.add(ospfLink12);
        ospfLinks.add(ospfLink21);
        setOspfLinks(ospfLinks);

    }

    private OspfLink createOspfLink(OnmsNode node, String sourceIpAddr, String sourceIpMask, int addrLessIndex, int ifIndex, String remRouterId, String remIpAddr, int remAddrLessIndex) {
        final OspfLink ospfLink = new OspfLink();
        ospfLink.setNode(node);
        ospfLink.setOspfIpAddr(InetAddressUtils.addr(sourceIpAddr));
        ospfLink.setOspfIpMask(InetAddressUtils.addr(sourceIpMask));
        ospfLink.setOspfAddressLessIndex(addrLessIndex);
        ospfLink.setOspfIfIndex(ifIndex);
        ospfLink.setOspfRemRouterId(InetAddressUtils.addr(remRouterId));
        ospfLink.setOspfRemIpAddr(InetAddressUtils.addr(remIpAddr));
        ospfLink.setOspfRemAddressLessIndex(remAddrLessIndex);
        return ospfLink;
    }

    private LldpLink createLldpLink(OnmsNode node, String nodePortId, String nodePortDescr, int portIfIndex, int localPortNum, LldpPortIdSubType portIdSubType, LldpElement remLldpElement, String node2PortDescr, String node2PortId) {
        return new LldpLink(node, localPortNum, portIfIndex, nodePortId, nodePortDescr,
                portIdSubType, remLldpElement.getLldpChassisId(), remLldpElement.getLldpSysname(), remLldpElement.getLldpChassisIdSubType(),
                node2PortId, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, node2PortDescr);
    }

    private List<OnmsIpInterface> getList(Set<OnmsIpInterface> ipset) {
        List<OnmsIpInterface> ips = new ArrayList<>();
        for (OnmsIpInterface ip: ipset) {
            ips.add(ip);
        }
        return ips;

    }

    public void setUpMock() {
        EasyMock.expect(m_cdpLinkDao.findAll()).andReturn(new ArrayList<CdpLink>()).anyTimes();
        EasyMock.expect(m_cdpElementDao.findAll()).andReturn(new ArrayList<CdpElement>()).anyTimes();
        EasyMock.expect(m_bridgeTopologyDao.load()).andReturn(new HashSet<BroadcastDomain>()).anyTimes();
        EasyMock.expect(m_isisLinkDao.findAll()).andReturn(new ArrayList<IsIsLink>()).anyTimes();
        EasyMock.expect(m_isisElementDao.findAll()).andReturn(new ArrayList<IsIsElement>()).anyTimes();

        EasyMock.expect(m_nodeDao.findAll()).andReturn(getNodes()).anyTimes();
        EasyMock.expect(m_ipInterfaceDao.findAll()).andReturn(getOnmsIpInterfaces()).anyTimes();
        EasyMock.expect(m_ipNetToMediaDao.findAll()).andReturn(new ArrayList<IpNetToMedia>()).anyTimes();
        EasyMock.expect(m_snmpInterfaceDao.findAll()).andReturn(getOnmsSnmpInterfaces()).anyTimes();
        EasyMock.expect(m_lldpElementDao.findAll()).andReturn(getLldpElements()).anyTimes();
        EasyMock.expect(m_lldpLinkDao.findAll()).andReturn(getLinks()).anyTimes();
        EasyMock.expect(m_ospfLinkDao.findAll()).andReturn(getOspfLinks()).anyTimes();
        EasyMock.expect(m_nodeDao.getAllLabelsById());
        EasyMock.expectLastCall().andReturn(getNodeLabelsById()).anyTimes();
        
        for (int i=1;i<9;i++) {
            EasyMock.expect(m_nodeDao.get(i)).andReturn(getNode(i)).anyTimes();
            EasyMock.expect(m_snmpInterfaceDao.findByNodeIdAndIfIndex(i, -1)).andReturn(null).anyTimes();
            EasyMock.expect(m_ipInterfaceDao.findPrimaryInterfaceByNodeId(i)).andReturn(getNode(i).getPrimaryInterface()).anyTimes();
            EasyMock.expect(m_ipInterfaceDao.findByNodeId(i)).andReturn(getList(getNode(i).getIpInterfaces())).anyTimes();
        }
        
        EasyMock.replay(m_cdpLinkDao, m_isisLinkDao,m_bridgeTopologyDao);
        EasyMock.replay(m_cdpElementDao);
        EasyMock.replay(m_lldpLinkDao);
        EasyMock.replay(m_lldpElementDao);
        EasyMock.replay(m_isisElementDao);
        EasyMock.replay(m_ospfLinkDao);
        EasyMock.replay(m_nodeDao);
        EasyMock.replay(m_snmpInterfaceDao);
        EasyMock.replay(m_ipInterfaceDao);
        EasyMock.replay(m_ipNetToMediaDao);
    }

    public void tearDown() {
        EasyMock.reset(m_cdpLinkDao, m_isisLinkDao,m_bridgeTopologyDao);
        EasyMock.reset(m_cdpElementDao);
        EasyMock.reset(m_lldpElementDao);
        EasyMock.reset(m_isisElementDao);
        EasyMock.reset(m_lldpLinkDao);
        EasyMock.reset(m_ospfLinkDao);
        EasyMock.reset(m_nodeDao);
        EasyMock.reset(m_snmpInterfaceDao);
        EasyMock.reset(m_ipInterfaceDao);
        EasyMock.reset(m_ipNetToMediaDao);
    }


    public OnmsNode getNode(Integer id) {
        OnmsNode node= null;
        switch (id) {
            case 1: node = getNode1();
                break;
            case 2: node = getNode2();
                break;
            case 3: node = getNode3();
                break;
            case 4: node = getNode4();
                break;
            case 5: node = getNode5();
                break;
            case 6: node = getNode6();
                break;
            case 7: node = getNode7();
                break;
            case 8: node = getNode8();
                break;
        }

        return node;
    }

    public OnmsNode getNode1() {
        return m_node1;
    }

    public OnmsNode getNode2() {
        return m_node2;
    }

    public OnmsNode getNode3() {
        return m_node3;
    }

    public OnmsNode getNode4() {
        return m_node4;
    }

    public OnmsNode getNode5() {
        return m_node5;
    }

    public OnmsNode getNode6() {
        return m_node6;
    }

    public OnmsNode getNode7() {
        return m_node7;
    }

    public OnmsNode getNode8() {
        return m_node8;
    }

    private void setNode1(final OnmsNode node1) {
        node1.setId(1);
        m_node1 = node1;
    }

    private void setNode2(final OnmsNode node2) {
        node2.setId(2);
        m_node2 = node2;
    }

    private void setNode3(final OnmsNode node3) {
        node3.setId(3);
        m_node3 = node3;
    }

    private void setNode4(final OnmsNode node4) {
        node4.setId(4);
        m_node4 = node4;
    }

    private void setNode5(final OnmsNode node5) {
        node5.setId(5);
        m_node5 = node5;
    }

    private void setNode6(final OnmsNode node6) {
        node6.setId(6);
        m_node6 = node6;
    }

    private void setNode7(final OnmsNode node7) {
        node7.setId(7);
        m_node7 = node7;
    }

    private void setNode8(final OnmsNode node8) {
        node8.setId(8);
        m_node8 = node8;
    }

    private void setLinks(final List<LldpLink> links) {
        m_links=links;
    }

    public List<LldpLink> getLinks(){
        return m_links;
    }

    private void setOspfLinks(List<OspfLink> ospfLinks) {
        m_ospfLinks = ospfLinks;
    }

    private List<OspfLink> getOspfLinks(){
        return m_ospfLinks;
    }

    public LldpLinkDao getLldpLinkDao(){
        return m_lldpLinkDao;
    }

    public void setLldpLinkDao(final LldpLinkDao lldpLinkDao){
        m_lldpLinkDao = lldpLinkDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    @SuppressWarnings("deprecation")
    public void check(GraphProvider topologyProvider) {
        String vertexNamespace = topologyProvider.getNamespace();
        Assert.assertEquals(8, topologyProvider.getVertices().size());

        Assert.assertEquals(9, topologyProvider.getEdges().size());

        Assert.assertTrue(topologyProvider.containsVertexId("1"));
        Assert.assertTrue(topologyProvider.containsVertexId("2"));
        Assert.assertTrue(topologyProvider.containsVertexId("3"));
        Assert.assertTrue(topologyProvider.containsVertexId("4"));
        Assert.assertTrue(topologyProvider.containsVertexId("5"));
        Assert.assertTrue(topologyProvider.containsVertexId("6"));
        Assert.assertTrue(topologyProvider.containsVertexId("7"));
        Assert.assertTrue(topologyProvider.containsVertexId("8"));
        Assert.assertTrue(!topologyProvider.containsVertexId("15"));

        Assert.assertEquals(3, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "1")).length);
        Assert.assertEquals(3, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "2")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "3")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "4")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "5")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "6")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "7")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "8")).length);

        /**
         * This is a little hokey because it relies on the fact that edges are only judged to be equal based
         * on the namespace and id tuple.
         */
        Vertex mockVertex = EasyMock.createMock(Vertex.class);
        EasyMock.expect(mockVertex.getId()).andReturn("v0").anyTimes();
        EasyMock.expect(mockVertex.getLabel()).andReturn(null).anyTimes();
        EasyMock.replay(mockVertex);
        AbstractEdge[] edgeidsforvertex1 = {
                LinkdEdge.create("10018|10081", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10012|10021", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10112|10121", mockVertex, mockVertex,ProtocolSupported.OSPF)
        };
        AbstractEdge[] edgeidsforvertex2 = {
                LinkdEdge.create("10023|10032", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10012|10021", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10112|10121", mockVertex, mockVertex,ProtocolSupported.OSPF)
        };
        AbstractEdge[] edgeidsforvertex3 = {
                LinkdEdge.create("10023|10032", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10034|10043", mockVertex, mockVertex,ProtocolSupported.LLDP)
        };
        AbstractEdge[] edgeidsforvertex4 = {
                LinkdEdge.create("10045|10054", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10034|10043", mockVertex, mockVertex,ProtocolSupported.LLDP)
        };
        AbstractEdge[] edgeidsforvertex5 = {
                LinkdEdge.create("10045|10054", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10056|10065", mockVertex, mockVertex,ProtocolSupported.LLDP)
        };
        AbstractEdge[] edgeidsforvertex6 = {
                LinkdEdge.create("10056|10065", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10067|10076", mockVertex, mockVertex,ProtocolSupported.LLDP)
        };
        AbstractEdge[] edgeidsforvertex7 = {
                LinkdEdge.create("10078|10087", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10067|10076", mockVertex, mockVertex,ProtocolSupported.LLDP)
        };
        AbstractEdge[] edgeidsforvertex8 = {
                LinkdEdge.create("10018|10081", mockVertex, mockVertex,ProtocolSupported.LLDP),
                LinkdEdge.create("10078|10087", mockVertex, mockVertex,ProtocolSupported.LLDP)
        };
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "1")), edgeidsforvertex1);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "2")), edgeidsforvertex2);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "3")), edgeidsforvertex3);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "4")), edgeidsforvertex4);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "5")), edgeidsforvertex5);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "6")), edgeidsforvertex6);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "7")), edgeidsforvertex7);
        OnmsAssert.assertArrayEqualsIgnoreOrder(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "8")), edgeidsforvertex8);
    }

    public Map<Integer, String> getNodeLabelsById() {
        Map<Integer, String> nodeLabelsById = new HashMap<Integer, String>();
        for (OnmsNode node : getNodes()) {
            nodeLabelsById.put(node.getId(), node.getLabel());
        }
        return nodeLabelsById;
    }

    public List<OnmsNode> getNodes() {
        return m_nodes;
    }

    public List<LldpElement> getLldpElements() {
        List<LldpElement> lldpelements = new ArrayList<>();
        for (OnmsNode node: m_nodes) 
            lldpelements.add(node.getLldpElement());
        return lldpelements;
    }
    
    public List<OnmsIpInterface> getOnmsIpInterfaces() {
        List<OnmsIpInterface> elements = new ArrayList<>();
        for (OnmsNode node: m_nodes) 
            elements.addAll(node.getIpInterfaces());
        return elements;
        
    }
    
    public List<OnmsSnmpInterface> getOnmsSnmpInterfaces() {
        List<OnmsSnmpInterface> elements = new ArrayList<>();
        for (OnmsNode node: m_nodes) 
            elements.addAll(node.getSnmpInterfaces());
        return elements;
        
    }

    public void setNodes(List<OnmsNode> nodes) {
        m_nodes = nodes;
    }

    public OperationContext getOperationContext() {
        return m_operationContext;
    }

    public void setOperationContext(OperationContext operationContext) {
        m_operationContext = operationContext;
    }

    public GraphContainer getGraphContainer() {
        return m_graphContainer;
    }

    public void setGraphContainer(GraphContainer graphContainer) {
        m_graphContainer = graphContainer;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        m_ospfLinkDao = ospfLinkDao;
    }

    public OspfLinkDao getOspfLinkDao() {
        return m_ospfLinkDao;
    }
    
    public LldpElementDao getLldpElementDao() {
        return m_lldpElementDao;
    }

    public void setLldpElementDao(LldpElementDao lldpElementDao) {
        m_lldpElementDao = lldpElementDao;
    }

}
