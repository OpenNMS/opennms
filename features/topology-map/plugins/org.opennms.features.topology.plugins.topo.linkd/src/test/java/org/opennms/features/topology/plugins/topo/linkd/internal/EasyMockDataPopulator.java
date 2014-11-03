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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsNode.NodeType;
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
    private List<DataLinkInterface> m_links;

    public void populateDatabase() {
        final OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");

        final String icmp = "ICMP";
        final String snmp = "SNMP";
        final String http = "HTTP";
        
        final NetworkBuilder builder = new NetworkBuilder(distPoller);
        
        setNode1(builder.addNode("node1").setForeignSource("imported:").setForeignId("1").setType(NodeType.ACTIVE).setSysObjectId("1.3.6.1.4.1.5813.1.25").getNode());
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
        setNode1(node1);
        
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType(NodeType.ACTIVE);
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
        
        builder.addNode("node3").setForeignSource("imported:").setForeignId("3").setType(NodeType.ACTIVE);
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
        
        builder.addNode("node4").setForeignSource("imported:").setForeignId("4").setType(NodeType.ACTIVE);
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
        builder.addNode("alternate-node1").setType(NodeType.ACTIVE).getAssetRecord().setAssetNumber("5");
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
        builder.addNode("alternate-node2").setType(NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
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
        
        builder.addNode("alternate-node3").setType(NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
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

        builder.addNode("alternate-node4").setType(NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
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
        
        final DataLinkInterface dli12 = new DataLinkInterface(getNode2(), -1, getNode1().getId(), -1,StatusType.ACTIVE, new Date());
        final DataLinkInterface dli23 = new DataLinkInterface(getNode3(), -1, getNode2().getId(), -1, StatusType.ACTIVE, new Date());
        final DataLinkInterface dli34 = new DataLinkInterface(getNode4(), -1, getNode3().getId(), -1, StatusType.ACTIVE, new Date());
        final DataLinkInterface dli45 = new DataLinkInterface(getNode5(), -1, getNode4().getId(), -1, StatusType.ACTIVE, new Date());
        final DataLinkInterface dli56 = new DataLinkInterface(getNode6(), -1, getNode5().getId(), -1, StatusType.ACTIVE, new Date());
        final DataLinkInterface dli67 = new DataLinkInterface(getNode7(), -1, getNode6().getId(), -1, StatusType.ACTIVE, new Date());
        final DataLinkInterface dli78 = new DataLinkInterface(getNode8(), -1, getNode7().getId(), -1, StatusType.ACTIVE, new Date());
        final DataLinkInterface dli81 = new DataLinkInterface(getNode1(), -1, getNode8().getId(), -1, StatusType.ACTIVE, new Date());
        
        dli12.setId(10012);
        dli23.setId(10023);
        dli34.setId(10034);
        dli45.setId(10045);
        dli56.setId(10056);
        dli67.setId(10067);
        dli78.setId(10078);
        dli81.setId(10081);

        List<DataLinkInterface> links = new ArrayList<DataLinkInterface>();
        
        links.add(dli12);
        links.add(dli23);
        links.add(dli34);
        links.add(dli45);
        links.add(dli56);
        links.add(dli67);
        links.add(dli78);
        links.add(dli81);
        setLinks(links);

    }
    
    private List<OnmsIpInterface> getList(Set<OnmsIpInterface> ipset) {
        List<OnmsIpInterface> ips = new ArrayList<OnmsIpInterface>();
        for (OnmsIpInterface ip: ipset) {
            ips.add(ip);
        }
        return ips;
        
    }
    public void setUpMock() {
        
        EasyMock.expect(m_dataLinkInterfaceDao.findAll()).andReturn(getLinks()).anyTimes();
        EasyMock.expect(m_nodeDao.findAll()).andReturn(getNodes()).anyTimes();
        
        for (int i=1;i<9;i++) {
            EasyMock.expect(m_nodeDao.get(i)).andReturn(getNode(i)).anyTimes();
            EasyMock.expect(m_snmpInterfaceDao.findByNodeIdAndIfIndex(i, -1)).andReturn(null).anyTimes();
            EasyMock.expect(m_ipInterfaceDao.findPrimaryInterfaceByNodeId(i)).andReturn(getNode(i).getPrimaryInterface()).anyTimes();
            EasyMock.expect(m_ipInterfaceDao.findByNodeId(i)).andReturn(getList(getNode(i).getIpInterfaces())).anyTimes();
        }

        EasyMock.replay(m_dataLinkInterfaceDao);
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
        EasyMock.reset(m_dataLinkInterfaceDao);
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

    private void setLinks(final List<DataLinkInterface> links) {
        m_links=links;
    }
    
    public List<DataLinkInterface> getLinks() {
        return m_links;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(final DataLinkInterfaceDao dataLinkInterfaceDao) {
        this.m_dataLinkInterfaceDao = dataLinkInterfaceDao;
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
        
        Assert.assertEquals(8, topologyProvider.getEdges().size());

        Assert.assertTrue(topologyProvider.containsVertexId("1"));
        Assert.assertTrue(topologyProvider.containsVertexId("2"));
        Assert.assertTrue(topologyProvider.containsVertexId("3"));
        Assert.assertTrue(topologyProvider.containsVertexId("4"));
        Assert.assertTrue(topologyProvider.containsVertexId("5"));
        Assert.assertTrue(topologyProvider.containsVertexId("6"));
        Assert.assertTrue(topologyProvider.containsVertexId("7"));
        Assert.assertTrue(topologyProvider.containsVertexId("8"));
        Assert.assertTrue(!topologyProvider.containsVertexId("15"));
        
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "1")).length);
        Assert.assertEquals(2, topologyProvider.getEdgeIdsForVertex(new DefaultVertexRef(vertexNamespace, "2")).length);
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
            new AbstractEdge("nodes", "10012", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10081", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex2 = {
            new AbstractEdge("nodes", "10012", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10023", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex3 = {
            new AbstractEdge("nodes", "10023", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10034", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex4 = {
            new AbstractEdge("nodes", "10034", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10045", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex5 = {
            new AbstractEdge("nodes", "10045", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10056", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex6 = {
            new AbstractEdge("nodes", "10056", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10067", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex7 = {
            new AbstractEdge("nodes", "10067", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10078", mockVertex, mockVertex)
        };
        AbstractEdge[] edgeidsforvertex8 = {
            new AbstractEdge("nodes", "10078", mockVertex, mockVertex),
            new AbstractEdge("nodes", "10081", mockVertex, mockVertex)
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

}
