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
import java.util.List;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;

public class EnhancedLinkdMockDataPopulator {

    @Autowired
    private TopologyEntityCache m_topologyEntityCache;
    @Autowired
    private OspfElementDao m_ospfElementDao;
    @Autowired
    private NodeDao m_nodeDao;

    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_node6;
    private OnmsNode m_node7;
    private OnmsNode m_node8;
    private LldpElement m_lldpnode1;
    private LldpElement m_lldpnode2;
    private LldpElement m_lldpnode3;
    private LldpElement m_lldpnode4;
    private LldpElement m_lldpnode5;
    private LldpElement m_lldpnode6;
    private LldpElement m_lldpnode7;
    private LldpElement m_lldpnode8;

    private List<OnmsNode> m_nodes;
    private List<LldpElement> m_lldpnodes;
    private List<OspfElement> m_ospfnodes;
    private List<LldpLink> m_lldplinks;
    private List<OspfLink> m_ospfLinks;

    public void populateDatabase() {
        
        final String icmp = "ICMP";
        final String snmp = "SNMP";
        final String http = "HTTP";

        m_nodes = new ArrayList<OnmsNode>();
        m_lldpnodes = new ArrayList<LldpElement>();
        m_ospfnodes = new ArrayList<>();
        final NetworkBuilder builder = new NetworkBuilder();

        builder.addNode("node1").setForeignSource("imported:").setForeignId("1").setType(OnmsNode.NodeType.ACTIVE).setSysObjectId("1.3.6.1.4.1.5813.1.25");
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
        final OspfElement ospfelement1 = new OspfElement();
        ospfelement1.setNode(node1);
        ospfelement1.setOspfRouterId(InetAddressUtils.addr("192.168.100.250"));
        setNode1(node1,
                 new LldpElement(node1, "node1ChassisId", "node1SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL),
                 ospfelement1
                 );

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
        final OspfElement ospfelement2 = new OspfElement();
        ospfelement2.setNode(node2);
        ospfelement2.setOspfRouterId(InetAddressUtils.addr("192.168.100.249"));
        setNode2(node2, 
                 new LldpElement(node2, "node2ChassisId", "node2SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL),
                 ospfelement2
                 );

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
        setNode3(node3,new LldpElement(node3, "node3ChassisId", "node3SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

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
        setNode4(node4, new LldpElement(node4, "node4ChassisId", "node4SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

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
        setNode5(node5,new LldpElement(node5, "node5ChassisId", "node5SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

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
        setNode6(node6,new LldpElement(node6, "node6ChassisId", "node6SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

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
        setNode7(node7,        new LldpElement(node7, "node7ChassisId", "node7SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

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
        setNode8(node8,new LldpElement(node8, "node8ChassisId", "mode8SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

        final LldpLink dli12 = createLldpLink(m_node1, "node1PortId", "node1PortDescr", 12, 10, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode2, "node2PortDescr", "node2PortId");
        final LldpLink dli21 = createLldpLink(m_node2, "node2PortId", "node2PortDescr", 22, 20, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode1, "node1PortDescr", "node1PortId");

        final LldpLink dli23 = createLldpLink(m_node2, "node2PortId", "node2PortDescr", 22, 20, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode3, "node3PortDescr", "node3PortId");
        final LldpLink dli32 = createLldpLink(m_node3, "node3PortId", "node3PortDescr", 33, 30, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode2, "node2PortDescr", "node2PortId");

        final LldpLink dli34 = createLldpLink(m_node3, "node3PortId", "node3PortDescr", 33, 30, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode4, "node4PortDescr", "node4PortId");
        final LldpLink dli43 = createLldpLink(m_node4, "node4PortId", "node4PortDescr", 44, 40, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode3, "node3PortDescr", "node3PortId");

        final LldpLink dli45 = createLldpLink(m_node4, "node4PortId", "node4PortDescr", 44, 40, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode5, "node5PortDescr", "node5PortId");
        final LldpLink dli54 = createLldpLink(m_node5, "node5PortId", "node5PortDescr", 55, 50, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode4, "node4PortDescr", "node4PortId");

        final LldpLink dli56 = createLldpLink(m_node5, "node5PortId", "node5PortDescr", 55, 50, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode6, "node6PortDescr", "node6PortId");
        final LldpLink dli65 = createLldpLink(m_node6, "node6PortId", "node6PortDescr", 66, 60, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode5, "node5PortDescr", "node5PortId");

        final LldpLink dli67 = createLldpLink(m_node6, "node6PortId", "node6PortDescr", 66, 60, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode7, "node7PortDescr", "node7PortId");
        final LldpLink dli76 = createLldpLink(m_node7, "node7PortId", "node7PortDescr", 77, 70, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode6, "node6PortDescr", "node6PortId");

        final LldpLink dli78 = createLldpLink(m_node7, "node7PortId", "node7PortDescr", 77, 70, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode8, "node8PortDescr", "node8PortId");
        final LldpLink dli87 = createLldpLink(m_node8, "node8PortId", "node8PortDescr", 88, 80, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode7, "node7PortDescr", "node7PortId");

        final LldpLink dli81 = createLldpLink(m_node8, "node8PortId", "node8PortDescr", 88, 80, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode1, "node1PortDescr", "node1PortId");
        final LldpLink dli18 = createLldpLink(m_node1, "node1PortId", "node1PortDescr", 12, 10, LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, m_lldpnode8, "node8PortDescr", "node8PortId");

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
        OspfLink ospfLink12 = createOspfLink(m_node1, "192.168.100.246", "255.255.255.252", 0, 10101, "192.168.100.249", "192.168.100.245", 0);
        OspfLink ospfLink21 = createOspfLink(m_node2, "192.168.100.245", "255.255.255.252", 0, 10101, "192.168.100.250", "192.168.100.246", 0);

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

    public void setUpMock() {
        EasyMock.expect(m_nodeDao.getDefaultFocusPoint()).andReturn(getOnmsNode(1)).anyTimes();
        EasyMock.expect(m_ospfElementDao.findAll()).andReturn(getOspfElements()).anyTimes();
        EasyMock.expect(m_topologyEntityCache.getNodeTopologyEntities()).andReturn(getNodes()).anyTimes();
        EasyMock.expect(m_topologyEntityCache.getOspfLinkTopologyEntities()).andReturn(convertToOspf(getOspfLinks())).anyTimes();
        EasyMock.expect(m_topologyEntityCache.getLldpLinkTopologyEntities()).andReturn(convertToLldp(getLinks())).anyTimes();
        EasyMock.expect(m_topologyEntityCache.getLldpElementTopologyEntities()).andReturn(convertToLldpElements(getLldpElements())).anyTimes();
        EasyMock.expect(m_topologyEntityCache.getSnmpInterfaceTopologyEntities()).andReturn(getSnmpInterfaceTopologyEntities()).anyTimes();
        EasyMock.expect(m_topologyEntityCache.getIpInterfaceTopologyEntities()).andReturn(getIpInterfaceTopologyEntities()).anyTimes();

        EasyMock.replay(m_nodeDao);
        EasyMock.replay(m_ospfElementDao);
        EasyMock.replay(m_topologyEntityCache);
    }

    private List<LldpElementTopologyEntity> convertToLldpElements(List<LldpElement> lldpElements) {
        return lldpElements.stream().map(LldpElementTopologyEntity::create).collect(Collectors.toList());
    }

    private List<OspfLinkTopologyEntity> convertToOspf(List<OspfLink> links) {
        return links.stream().map(OspfLinkTopologyEntity::create).collect(Collectors.toList());
    }

    private List<LldpLinkTopologyEntity> convertToLldp(List<LldpLink> links) {
        return links.stream().map(LldpLinkTopologyEntity::create).collect(Collectors.toList());
    }

    public void tearDown() {
        EasyMock.reset(m_topologyEntityCache);
        EasyMock.reset(m_ospfElementDao);
        EasyMock.reset(m_nodeDao);

    }

    public List<OnmsNode> getOnmsNodes() {
        return m_nodes;
    }
    
    private OnmsNode getOnmsNode(Integer id) {
        OnmsNode node= null;
        switch (id) {
            case 1: node = m_node1;
                break;
            case 2: node = m_node2;
                break;
            case 3: node = m_node3;
                break;
            case 4: node = m_node4;
                break;
            case 5: node = m_node5;
                break;
            case 6: node = m_node6;
                break;
            case 7: node = m_node7;
                break;
            case 8: node = m_node8;
                break;
        }

        return node;
    }

    private void setNode1(final OnmsNode node1,final LldpElement lldpelement1, final OspfElement ospfelement1) {
        node1.setId(1);
        m_node1 = node1;
        lldpelement1.setNode(node1);
        m_lldpnode1 = lldpelement1;
        m_nodes.add(m_node1);
        m_lldpnodes.add(m_lldpnode1);
        m_ospfnodes.add(ospfelement1);
    }

    private void setNode2(final OnmsNode node2,final LldpElement lldpelement2, final OspfElement ospfelement2) {
        node2.setId(2);
        m_node2 = node2;
        lldpelement2.setNode(node2);
        m_lldpnode2 = lldpelement2;
        m_nodes.add(m_node2);
        m_lldpnodes.add(m_lldpnode2);
        m_ospfnodes.add(ospfelement2);
    }

    private void setNode3(final OnmsNode node3,final LldpElement lldpelement3) {
        node3.setId(3);
        m_node3 = node3;
        lldpelement3.setNode(node3);
        m_lldpnode3 = lldpelement3;
        m_nodes.add(m_node3);
        m_lldpnodes.add(m_lldpnode3);
    }

    private void setNode4(final OnmsNode node4, LldpElement lldpelement4) {
        node4.setId(4);
        m_node4 = node4;
        lldpelement4.setNode(node4);
        m_lldpnode4 = lldpelement4;
        m_nodes.add(m_node4);
        m_lldpnodes.add(m_lldpnode4);
    }

    private void setNode5(final OnmsNode node5, LldpElement lldpelement5) {
        node5.setId(5);
        m_node5 = node5;
        lldpelement5.setNode(node5);
        m_lldpnode5 = lldpelement5;
        m_nodes.add(m_node5);
        m_lldpnodes.add(m_lldpnode5);
    }

    private void setNode6(final OnmsNode node6, LldpElement lldpelement6) {
        node6.setId(6);
        m_node6 = node6;
        lldpelement6.setNode(node6);
        m_lldpnode6 = lldpelement6;
        m_nodes.add(m_node6);
        m_lldpnodes.add(m_lldpnode6);
    }

    private void setNode7(final OnmsNode node7,LldpElement lldpelement7) {
        node7.setId(7);
        m_node7 = node7;
        lldpelement7.setNode(node7);
        m_lldpnode7 = lldpelement7;
        m_nodes.add(m_node7);
        m_lldpnodes.add(m_lldpnode7);
    }

    private void setNode8(final OnmsNode node8, LldpElement lldpelement8) {
        node8.setId(8);
        m_node8 = node8;
        lldpelement8.setNode(node8);
        m_lldpnode8 = lldpelement8;
        m_nodes.add(m_node8);
        m_lldpnodes.add(m_lldpnode8);
    }

    public NodeTopologyEntity getNode(Integer id) {
        return NodeTopologyEntity.toNodeTopologyInfo(getOnmsNode(id));
    }

    public List<NodeTopologyEntity> getNodes() {
        return m_nodes.stream().map(NodeTopologyEntity::toNodeTopologyInfo).collect(Collectors.toList());
    }

    private void setLinks(final List<LldpLink> links) {
        m_lldplinks=links;
    }

    public List<LldpLink> getLinks(){
        return m_lldplinks;
    }

    private void setOspfLinks(List<OspfLink> ospfLinks) {
        m_ospfLinks = ospfLinks;
    }

    public List<OspfLink> getOspfLinks(){
        return m_ospfLinks;
    }

    public List<LldpElement> getLldpElements() {
        return m_lldpnodes;
    }

    public List<OspfElement> getOspfElements() {
        return m_ospfnodes;
    }

    public List<OnmsIpInterface> getOnmsIpInterfaces() {
        List<OnmsIpInterface> elements = new ArrayList<>();
        for (OnmsNode node: m_nodes)
            elements.addAll(node.getIpInterfaces());
        return elements;
    }

    public List<IpInterfaceTopologyEntity> getIpInterfaceTopologyEntities() {
        List<IpInterfaceTopologyEntity> elements = new ArrayList<>();
        for (OnmsIpInterface ipInterface : getOnmsIpInterfaces()) {
            elements.add(IpInterfaceTopologyEntity.create(ipInterface));
        }
        return elements;
    }

    public List<OnmsSnmpInterface> getOnmsSnmpInterfaces() {
        List<OnmsSnmpInterface> elements = new ArrayList<>();
        for (OnmsNode node: m_nodes) 
            elements.addAll(node.getSnmpInterfaces());
        return elements;
        
    }

    private List<SnmpInterfaceTopologyEntity> getSnmpInterfaceTopologyEntities(){
        List<SnmpInterfaceTopologyEntity> elements = new ArrayList<>();
        for (OnmsSnmpInterface ipInterface : getOnmsSnmpInterfaces()) {
            elements.add(SnmpInterfaceTopologyEntity.create(ipInterface));
        }
        return elements;
    }
}
