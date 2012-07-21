/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import org.easymock.EasyMock;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;

import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Populates a test database with some entities (nodes, interfaces, services).
 * 
 * Example usage:
 * <pre>
 * private DatabasePopulator m_populator;
 *
 * @Override
 * protected String[] getConfigLocations() {
 *     return new String[] {
 *         "classpath:/META-INF/opennms/applicationContext-dao.xml",
 *         "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
 *     };
 * }
 * 
 * @Override
 * protected void onSetUpInTransactionIfEnabled() {
 *     m_populator.populateDatabase();
 * }
 * 
 * public void setPopulator(DatabasePopulator populator) {
 *     m_populator = populator;
 * }
 * </pre>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EasyMockDataPopulator {
    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_node6;
    private OnmsNode m_node7;
    private OnmsNode m_node8;
    
    public void populateDatabase() {
        final OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");

        final OnmsServiceType icmp = new OnmsServiceType("ICMP");
        final OnmsServiceType snmp = new OnmsServiceType("SNMP");
        final OnmsServiceType http = new OnmsServiceType("HTTP");
        
        final NetworkBuilder builder = new NetworkBuilder(distPoller);
        
        setNode1(builder.addNode("node1").setForeignSource("imported:").setForeignId("1").setType("A").getNode());
        Assert.assertNotNull("newly built node 1 should not be null", getNode1());
        builder.setBuilding("HQ");
        builder.addInterface("192.168.1.1").setIsManaged("M").setIsSnmpPrimary("P").addSnmpInterface(1)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfDescr("ATM0")
            .setIfAlias("Initial ifAlias value")
            .setIfType(37);
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S").addSnmpInterface(2)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfName("eth0")
            .setIfType(6);
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface(3)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000);
        builder.addService(icmp);
        builder.addInterface("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface(4)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000);
        builder.addService(icmp);
        final OnmsNode node1 = builder.getCurrentNode();
        setNode1(node1);
        
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType("A");
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
        setNode2(node2);
        
        builder.addNode("node3").setForeignSource("imported:").setForeignId("3").setType("A");
        builder.addInterface("192.168.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("192.168.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("192.168.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node3 = builder.getCurrentNode();
        setNode3(node3);
        
        builder.addNode("node4").setForeignSource("imported:").setForeignId("4").setType("A");
        builder.addInterface("192.168.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("192.168.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("192.168.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node4 = builder.getCurrentNode();
        setNode4(node4);
        
        //This node purposely doesn't have a foreignId style assetNumber
        builder.addNode("alternate-node1").setType("A").getAssetRecord().setAssetNumber("5");
        builder.addInterface("10.1.1.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.1.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.1.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node5 = builder.getCurrentNode();
        setNode5(node5);
        
        //This node purposely doesn't have a assetNumber and is used by a test to check the category
        builder.addNode("alternate-node2").setType("A").getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node6 = builder.getCurrentNode();
        setNode6(node6);
        
        builder.addNode("alternate-node3").setType("A").getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node7 = builder.getCurrentNode();
        setNode7(node7);

        builder.addNode("alternate-node4").setType("A").getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(icmp);
        builder.addService(snmp);
        builder.addInterface("10.1.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(icmp);
        builder.addService(http);
        builder.addInterface("10.1.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(icmp);
        OnmsNode node8 = builder.getCurrentNode();
        setNode8(node8);

        final OnmsMap map1 = new OnmsMap("DB_Top_Test_Map", "admin");
        map1.setBackground("fake_background.jpg");
        map1.setAccessMode(OnmsMap.ACCESS_MODE_ADMIN);
        map1.setType(OnmsMap.USER_GENERATED_MAP);
        map1.setMapGroup("admin");
        map1.setId(1);

        final OnmsMap map2 = new OnmsMap("DB_Pop_Test_Map1", "admin");
        map2.setBackground("fake_background.jpg");
        map2.setAccessMode(OnmsMap.ACCESS_MODE_ADMIN);
        map2.setType(OnmsMap.USER_GENERATED_MAP);
        map2.setMapGroup("admin");
        map2.setId(2);

        final OnmsMap map3 = new OnmsMap("DB_Pop_Test_Map2", "admin");
        map3.setBackground("fake_background.jpg");
        map3.setAccessMode(OnmsMap.ACCESS_MODE_ADMIN);
        map3.setType(OnmsMap.USER_GENERATED_MAP);
        map3.setMapGroup("admin");
        map3.setId(3);

        final OnmsMap map4 = new OnmsMap("DB_Pop_Test_Map3", "admin");
        map4.setBackground("fake_background.jpg");
        map4.setAccessMode(OnmsMap.ACCESS_MODE_ADMIN);
        map4.setType(OnmsMap.USER_GENERATED_MAP);
        map4.setMapGroup("admin");
        map4.setId(4);


        
        final OnmsMapElement element1 = new OnmsMapElement(map1, getNode1().getId(),
                OnmsMapElement.NODE_TYPE,
                "Test Node",
                OnmsMapElement.defaultNodeIcon,
                0,
                10);
        element1.setId(1001);
        map1.addMapElement(element1);

        final OnmsMapElement element2 = new OnmsMapElement(map1, getNode2().getId(),
                                                              OnmsMapElement.NODE_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultNodeIcon,
                                                              0,
                                                              10);
        element2.setId(1002);
        map1.addMapElement(element2);

        final OnmsMapElement element3 = new OnmsMapElement(map2, getNode3().getId(),
                                                              OnmsMapElement.NODE_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultNodeIcon,
                                                              0,
                                                              10);
        element3.setId(1003);
        map2.addMapElement(element3);

        final OnmsMapElement element4 = new OnmsMapElement(map2, getNode4().getId(),
                                                              OnmsMapElement.NODE_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultNodeIcon,
                                                              0,
                                                              10);
        element4.setId(1004);
        map2.addMapElement(element4);

        final OnmsMapElement element5 = new OnmsMapElement(map3, getNode5().getId(),
                                                              OnmsMapElement.NODE_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultNodeIcon,
                                                              0,
                                                              10);
        element5.setId(1005);
        map3.addMapElement(element5);

        final OnmsMapElement element6 = new OnmsMapElement(map3, getNode6().getId(),
                                                              OnmsMapElement.NODE_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultNodeIcon,
                                                              0,
                                                              10);
        element6.setId(1006);
        map3.addMapElement(element6);

        final OnmsMapElement element7 = new OnmsMapElement(map1, map2.getId(),
                                                              OnmsMapElement.MAP_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultMapIcon,
                                                              0,
                                                              10);
        element7.setId(1007);
        map1.addMapElement(element7);

        final OnmsMapElement element8 = new OnmsMapElement(map1, map3.getId(),
                                                              OnmsMapElement.MAP_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultMapIcon,
                                                              0,
                                                              10);
        element8.setId(1009);
        map1.addMapElement(element8);

        final OnmsMapElement element9 = new OnmsMapElement(map4, getNode7().getId(),
                                                              OnmsMapElement.NODE_TYPE,
                                                              "Test Node",
                                                              OnmsMapElement.defaultNodeIcon,
                                                              0,
                                                              10);
        element9.setId(1009);
        map4.addMapElement(element9);

        final DataLinkInterface dli12 = new DataLinkInterface(getNode2(), 1, getNode1().getId(), 1, "A", new Date());
        final DataLinkInterface dli13 = new DataLinkInterface(getNode3(), 2, getNode1().getId(), 1, "A", new Date());
        final DataLinkInterface dli14 = new DataLinkInterface(getNode4(), 1, getNode1().getId(), 1, "A", new Date());
        final DataLinkInterface dli15 = new DataLinkInterface(getNode5(), 1, getNode1().getId(), 1, "A", new Date());
        final DataLinkInterface dli68 = new DataLinkInterface(getNode8(), 1, getNode6().getId(), 1, "A", new Date());
        final DataLinkInterface dli76 = new DataLinkInterface(getNode6(), 2, getNode7().getId(), 1, "A", new Date());
        final DataLinkInterface dli78 = new DataLinkInterface(getNode8(), 1, getNode7().getId(), 1, "A", new Date());
        
        dli12.setId(10012);
        dli13.setId(10013);
        dli14.setId(10014);
        dli15.setId(10015);
        dli68.setId(10068);
        dli76.setId(10076);
        dli78.setId(10078);

        List<DataLinkInterface> links = new ArrayList<DataLinkInterface>();
        
        links.add(dli12);
        links.add(dli13);
        links.add(dli14);
        links.add(dli15);
        links.add(dli68);
        links.add(dli76);
        links.add(dli78);
        
        EasyMock.expect(m_dataLinkInterfaceDao.findAll()).andReturn(links).anyTimes();

        EasyMock.replay(m_dataLinkInterfaceDao);
         
    }
    

    public void tearDown() {
        EasyMock.verify(m_dataLinkInterfaceDao);
        EasyMock.reset(m_dataLinkInterfaceDao);
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

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(final DataLinkInterfaceDao dataLinkInterfaceDao) {
        this.m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }
    
    public void check(LinkdTopologyProvider topologyProvider) {
        Assert.assertTrue(topologyProvider.getVertexIds().size()==8);
        
        Assert.assertTrue(topologyProvider.getEdgeIds().size()==7);

        Assert.assertTrue(topologyProvider.containsVertexId("1"));
        Assert.assertTrue(topologyProvider.containsVertexId("2"));
        Assert.assertTrue(topologyProvider.containsVertexId("3"));
        Assert.assertTrue(topologyProvider.containsVertexId("4"));
        Assert.assertTrue(topologyProvider.containsVertexId("5"));
        Assert.assertTrue(topologyProvider.containsVertexId("6"));
        Assert.assertTrue(topologyProvider.containsVertexId("7"));
        Assert.assertTrue(topologyProvider.containsVertexId("8"));
        Assert.assertTrue(!topologyProvider.containsVertexId("15"));
        
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("1").size() == 4);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("2").size() == 1);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("3").size() == 1);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("4").size() == 1);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("5").size() == 1);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("6").size() == 2);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("7").size() == 2);
        Assert.assertTrue(topologyProvider.getEdgeIdsForVertex("8").size() == 2);
        
        String[] edgeidsforvertex1 = { "10012","10013","10014","10015" };
        String[] edgeidsforvertex2 = { "10012" };
        String[] edgeidsforvertex3 = { "10013" };
        String[] edgeidsforvertex4 = { "10014" };
        String[] edgeidsforvertex5 = { "10015" };
        String[] edgeidsforvertex6 = { "10068","10076" };
        String[] edgeidsforvertex7 = { "10076","10078" };
        String[] edgeidsforvertex8 = { "10068","10078" };

        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("1").toArray(), edgeidsforvertex1);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("2").toArray(), edgeidsforvertex2);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("3").toArray(), edgeidsforvertex3);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("4").toArray(), edgeidsforvertex4);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("5").toArray(), edgeidsforvertex5);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("6").toArray(), edgeidsforvertex6);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("7").toArray(), edgeidsforvertex7);
        Assert.assertArrayEquals(topologyProvider.getEdgeIdsForVertex("8").toArray(), edgeidsforvertex8);
    }
}
