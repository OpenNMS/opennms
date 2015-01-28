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

import org.easymock.EasyMock;
import org.junit.Assert;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.*;
import org.opennms.netmgt.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnhancedLinkdMockDataPopulator {

    @Autowired
    private LldpLinkDao m_lldpLinkDao;

    @Autowired
    private OspfLinkDao m_ospfLinkDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

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
    //private List<DataLinkInterface> m_links;
    private List<LldpLink> m_links;
    private List<OspfLink> m_ospfLinks;

    public void populateDatabase() {
        final OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");

        final String icmp = "ICMP";
        final String snmp = "SNMP";
        final String http = "HTTP";

        final NetworkBuilder builder = new NetworkBuilder(distPoller);

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
        node1.setLldpElement(new LldpElement(node1, "node1ChassisId", "node1SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        builder.addAtInterface(node1, "192.168.2.1", "AA:BB:CC:DD:EE:FF").setIfIndex(1).setLastPollTime(new Date()).setStatus('A');
        OnmsNode node2 = builder.getCurrentNode();
        node2.setLldpElement(new LldpElement(node2, "node2ChassisId", "node2SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        node3.setLldpElement(new LldpElement(node3, "node3ChassisId", "node3SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        node4.setLldpElement(new LldpElement(node4, "node4ChassisId", "node4SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        node5.setLldpElement(new LldpElement(node5, "node5ChassisId", "node5SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        node6.setLldpElement(new LldpElement(node6, "node6ChassisId", "node6SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        node7.setLldpElement(new LldpElement(node7, "node7ChassisId", "node7SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
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
        node8.setLldpElement(new LldpElement(node8, "node8ChassisId", "mode8SysName", LldpElement.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));
        setNode8(node8);

        List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        nodes.add(node5);
        nodes.add(node6);
        nodes.add(node7);
        nodes.add(node8);
        setNodes(nodes);
/*
        final DataLinkInterface dli12 = new DataLinkInterface(getNode2(), -1, getNode1().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli23 = new DataLinkInterface(getNode3(), -1, getNode2().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli34 = new DataLinkInterface(getNode4(), -1, getNode3().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli45 = new DataLinkInterface(getNode5(), -1, getNode4().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli56 = new DataLinkInterface(getNode6(), -1, getNode5().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli67 = new DataLinkInterface(getNode7(), -1, getNode6().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli78 = new DataLinkInterface(getNode8(), -1, getNode7().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
        final DataLinkInterface dli81 = new DataLinkInterface(getNode1(), -1, getNode8().getId(), -1, OnmsArpInterface.StatusType.ACTIVE, new Date());
*/
        //final OnmsNode node, final int ifIndex, final int nodeParentId, final int parentIfIndex, final StatusType status,final Date lastPollTime
        /*OnmsNode node, Integer localPortNum, Integer portIfIndex, String portId,
                String portDescr, String remChassisId, String remSysname, LldpElement.LldpChassisIdSubType
        remChassisIdSubType,
                String remPortId, String remPortDescr*/
        final LldpLink dli12 = createLldpLink(getNode1(), "node1PortId", "node1PortDescr", 12, 10, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode2().getLldpElement(), "node2PortDescr", "node2PortId");
        final LldpLink dli21 = createLldpLink(getNode2(), "node2PortId", "node2PortDescr", 22, 20, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode1().getLldpElement(), "node1PortDescr", "node1PortId");

        final LldpLink dli23 = createLldpLink(getNode2(), "node2PortId", "node2PortDescr", 22, 20, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode3().getLldpElement(), "node3PortDescr", "node3PortId");
        final LldpLink dli32 = createLldpLink(getNode3(), "node3PortId", "node3PortDescr", 33, 30, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode2().getLldpElement(), "node2PortDescr", "node2PortId");

        final LldpLink dli34 = createLldpLink(getNode3(), "node3PortId", "node3PortDescr", 33, 30, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode4().getLldpElement(), "node4PortDescr", "node4PortId");
        final LldpLink dli43 = createLldpLink(getNode4(), "node4PortId", "node4PortDescr", 44, 40, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode3().getLldpElement(), "node3PortDescr", "node3PortId");

        final LldpLink dli45 = createLldpLink(getNode4(), "node4PortId", "node4PortDescr", 44, 40, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode5().getLldpElement(), "node5PortDescr", "node5PortId");
        final LldpLink dli54 = createLldpLink(getNode5(), "node5PortId", "node5PortDescr", 55, 50, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode4().getLldpElement(), "node4PortDescr", "node4PortId");

        final LldpLink dli56 = createLldpLink(getNode5(), "node5PortId", "node5PortDescr", 55, 50, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode6().getLldpElement(), "node6PortDescr", "node6PortId");
        final LldpLink dli65 = createLldpLink(getNode6(), "node6PortId", "node6PortDescr", 66, 60, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode5().getLldpElement(), "node5PortDescr", "node5PortId");

        final LldpLink dli67 = createLldpLink(getNode6(), "node6PortId", "node6PortDescr", 66, 60, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode7().getLldpElement(), "node7PortDescr", "node7PortId");
        final LldpLink dli76 = createLldpLink(getNode7(), "node7PortId", "node7PortDescr", 77, 70, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode6().getLldpElement(), "node6PortDescr", "node6PortId");

        final LldpLink dli78 = createLldpLink(getNode7(), "node7PortId", "node7PortDescr", 77, 70, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode8().getLldpElement(), "node8PortDescr", "node8PortId");
        final LldpLink dli87 = createLldpLink(getNode8(), "node8PortId", "node8PortDescr", 88, 80, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode7().getLldpElement(), "node7PortDescr", "node7PortId");

        final LldpLink dli81 = createLldpLink(getNode8(), "node8PortId", "node8PortDescr", 88, 80, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode1().getLldpElement(), "node1PortDescr", "node1PortId");
        final LldpLink dli18 = createLldpLink(getNode1(), "node1PortId", "node1PortDescr", 12, 10, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, getNode8().getLldpElement(), "node8PortDescr", "node8PortId");

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

        List<LldpLink> links = new ArrayList<LldpLink>();

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

        List<OspfLink> ospfLinks = new ArrayList<OspfLink>();
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

    private LldpLink createLldpLink(OnmsNode node, String nodePortId, String nodePortDescr, int portIfIndex, int localPortNum, LldpLink.LldpPortIdSubType portIdSubType, LldpElement remLldpElement, String node2PortDescr, String node2PortId) {
        return new LldpLink(node, localPortNum, portIfIndex, nodePortId, nodePortDescr,
                portIdSubType, remLldpElement.getLldpChassisId(), remLldpElement.getLldpSysname(), remLldpElement.getLldpChassisIdSubType(),
                node2PortId, LldpLink.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, node2PortDescr);
    }

    private List<OnmsIpInterface> getList(Set<OnmsIpInterface> ipset) {
        List<OnmsIpInterface> ips = new ArrayList<OnmsIpInterface>();
        for (OnmsIpInterface ip: ipset) {
            ips.add(ip);
        }
        return ips;

    }

    public void setUpMock() {
        //EasyMock.expect(m_dataLinkInterfaceDao.findAll()).andReturn(getLinks()).anyTimes();
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

        //EasyMock.replay(m_dataLinkInterfaceDao);
        EasyMock.replay(m_lldpLinkDao);
        EasyMock.replay(m_ospfLinkDao);
        EasyMock.replay(m_nodeDao);
        EasyMock.replay(m_snmpInterfaceDao);
        EasyMock.replay(m_ipInterfaceDao);
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

    public void tearDown() {
//        EasyMock.reset(m_dataLinkInterfaceDao);
        EasyMock.reset(m_lldpLinkDao);
        EasyMock.reset(m_ospfLinkDao);
        EasyMock.reset(m_nodeDao);
        EasyMock.reset(m_snmpInterfaceDao);
        EasyMock.reset(m_ipInterfaceDao);
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

    /*public List<DataLinkInterface> getLinks() {
        return m_links;
    }*/

    public List<LldpLink> getLinks(){
        return m_links;
    }

    private void setOspfLinks(List<OspfLink> ospfLinks) {
        m_ospfLinks = ospfLinks;
    }

    private List<OspfLink> getOspfLinks(){
        return m_ospfLinks;
    }

/*
    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(final DataLinkInterfaceDao dataLinkInterfaceDao) {
        this.m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }
*/
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

    public void check(GraphProvider topologyProvider) {
        String vertexNamespace = topologyProvider.getVertexNamespace();
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
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10018|10081", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10012|10021", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE, "10112|10121", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex2 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10023|10032", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10012|10021", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE, "10112|10121", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex3 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10023|10032", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10034|10043", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex4 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10045|10054", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10034|10043", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex5 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10045|10054", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10056|10065", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex6 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10056|10065", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10067|10076", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex7 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10078|10087", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10067|10076", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex8 = {
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10018|10081", mockVertex, mockVertex),
                new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "10078|10087", mockVertex, mockVertex)
        };
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "1")), edgeidsforvertex1);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "2")), edgeidsforvertex2);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "3")), edgeidsforvertex3);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "4")), edgeidsforvertex4);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "5")), edgeidsforvertex5);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "6")), edgeidsforvertex6);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "7")), edgeidsforvertex7);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "8")), edgeidsforvertex8);

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
}
