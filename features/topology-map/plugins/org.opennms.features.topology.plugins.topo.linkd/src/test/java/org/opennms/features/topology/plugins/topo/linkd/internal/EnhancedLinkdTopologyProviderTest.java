/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import org.junit.*;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-enhanced-mock.xml"
})
public class EnhancedLinkdTopologyProviderTest {

    @Autowired
    private OperationContext m_operationContext;

    @Autowired
    private EnhancedLinkdTopologyProvider m_topologyProvider;

    @Autowired
    private EnhancedLinkdMockDataPopulator m_databasePopulator;
    private String m_originalFilename;


    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();
        m_databasePopulator.populateDatabase();
        m_databasePopulator.setUpMock();
        m_originalFilename = m_topologyProvider.getConfigurationFile();

        m_topologyProvider.load(null);
    }

    @Test
    public void testDataCorrectness(){
        LldpLinkDao lldpLinkDao = m_databasePopulator.getLldpLinkDao();
        List<LldpLink> links = lldpLinkDao.findAll();
        assertEquals(16, links.size());

        OspfLinkDao ospfLinkDao = m_databasePopulator.getOspfLinkDao();
        List<OspfLink> ospfLinks = ospfLinkDao.findAll();
        assertEquals(2, ospfLinks.size());
    }

    @Test
    public void testGetIcon() {
        Assert.assertTrue("linkd:system:snmp:1.3.6.1.4.1.5813.1.25".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode1())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode2())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode3())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode4())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode5())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode6())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode7())));
        Assert.assertTrue(LinkdTopologyProvider.SERVER_ICON_KEY.equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode8())));

    }

    @Test
    public void testSave() {
        m_topologyProvider.setConfigurationFile("target/test-map.xml");
        m_topologyProvider.save();
        m_databasePopulator.check(m_topologyProvider);
    }

    @Test
    public void testAddGroup() {
        Vertex parentId = m_topologyProvider.addGroup("Linkd Group", LinkdTopologyProvider.GROUP_ICON_KEY);
        Assert.assertEquals(true, m_topologyProvider.containsVertexId(parentId));
    }

    @Test
    public void test() throws Exception {
        new File("target/test-classes/test.xml").delete();
        m_topologyProvider.setConfigurationFile("target/test-classes/test.xml");

        // Load 8 vertices
        assertEquals(8, m_topologyProvider.getVertices().size());

        // Add v0 vertex
        Vertex vertexA = m_topologyProvider.addVertex(50, 100);
        assertEquals(9, m_topologyProvider.getVertices().size());
        assertEquals("v0", vertexA.getId());
        //LoggerFactory.getLogger(this.getClass()).debug(m_topologyProvider.getVertices().get(0).toString());
        assertTrue(m_topologyProvider.containsVertexId(vertexA));
        assertTrue(m_topologyProvider.containsVertexId(new DefaultVertexRef("nodes", "v0")));
        assertFalse(m_topologyProvider.containsVertexId(new DefaultVertexRef("nodes", "v1")));

        ((AbstractVertex)vertexA).setIpAddress("10.0.0.4");

        // Search by VertexRef
        @SuppressWarnings("deprecation") VertexRef vertexAref = new DefaultVertexRef(m_topologyProvider.getVertexNamespace(), "v0");
        @SuppressWarnings("deprecation") VertexRef vertexBref = new DefaultVertexRef(m_topologyProvider.getVertexNamespace(), "v1");
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(vertexAref)).size());
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(vertexBref)).size());

        // Add v1 vertex
        Vertex vertexB = m_topologyProvider.addVertex(100, 50);
        assertEquals("v1", vertexB.getId());
        assertTrue(m_topologyProvider.containsVertexId(vertexB));
        assertTrue(m_topologyProvider.containsVertexId("v1"));
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(vertexBref)).size());

        // Added 3 more vertices
        Vertex vertexC = m_topologyProvider.addVertex(100, 150);
        Vertex vertexD = m_topologyProvider.addVertex(150, 100);
        Vertex vertexE = m_topologyProvider.addVertex(200, 200);
        assertEquals(13, m_topologyProvider.getVertices().size());

        // Add 2 groups
        Vertex group1 = m_topologyProvider.addGroup("Group 1", Constants.GROUP_ICON_KEY);
        Vertex group2 = m_topologyProvider.addGroup("Group 2", Constants.GROUP_ICON_KEY);
        assertEquals(15, m_topologyProvider.getVertices().size());

        // Link v0, v1 to Group 1 and v2, v3 to Group 2
        m_topologyProvider.setParent(vertexA, group1);
        m_topologyProvider.setParent(vertexB, group1);
        m_topologyProvider.setParent(vertexC, group2);
        m_topologyProvider.setParent(vertexD, group2);

        // Connect various vertices together
        m_topologyProvider.connectVertices(vertexA, vertexB);
        m_topologyProvider.connectVertices(vertexA, vertexC);
        m_topologyProvider.connectVertices(vertexB, vertexC);
        m_topologyProvider.connectVertices(vertexB, vertexD);
        m_topologyProvider.connectVertices(vertexC, vertexD);
        m_topologyProvider.connectVertices(vertexA, vertexE);
        m_topologyProvider.connectVertices(vertexD, vertexE);

        // Ensure that the WrappedVertex class is working properly
        WrappedVertex wrappedVertex = new WrappedLeafVertex(vertexA);
        assertEquals("v0", wrappedVertex.id);
        assertEquals("nodes", wrappedVertex.namespace);
        assertEquals("10.0.0.4", wrappedVertex.ipAddr);
        assertEquals(50, wrappedVertex.x.intValue());
        assertEquals(100, wrappedVertex.y.intValue());

        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(vertexAref)).size());
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(vertexBref)).size());
        assertEquals(15, m_topologyProvider.getVertices().size());
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(vertexAref)).length);
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(vertexBref)).length);
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group2));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertexA));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertexB));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertexC));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertexD));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexE));

        m_topologyProvider.save();

        m_topologyProvider.resetContainer();

        // Ensure that the topology provider has been erased
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(vertexAref)).size());
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(vertexBref)).size());
        assertEquals(0, m_topologyProvider.getVertices().size());
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(vertexAref)).length);
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(vertexBref)).length);
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group2));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexA));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexB));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexC));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexD));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexE));

        m_topologyProvider.refresh();

        // Ensure that all of the content has been reloaded properly

        // Plain vertices should not be reloaded from the XML
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(vertexAref)).size());
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(vertexBref)).size());
        // Groups should be reloaded
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(group1)).size());
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(group2)).size());
        assertEquals(10, m_topologyProvider.getVertices().size());
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(vertexAref)).length);
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(vertexBref)).length);
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(group1)).length);
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(group2)).length);
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group2));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexA));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexB));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexC));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexD));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertexE));
    }

    @Test
    public void loadSampleGraph() throws Exception {
        m_topologyProvider.setConfigurationFile("target/test-classes/saved-vmware-graph.xml");

        m_topologyProvider.load(null);

        assertEquals(24, m_topologyProvider.getVertices().size());
        assertEquals(9, m_topologyProvider.getEdges().size());
    }

    @Test
    public void loadSavedGraphWithOnlyGroups() throws Exception {
        m_topologyProvider.setConfigurationFile("target/test-classes/saved-linkd-graph.xml");

        // Temporarily replace the DataLinkInterfaceDao with a mock empty impl
        LldpLinkDao dao = m_topologyProvider.getLldpLinkDao();
        LldpLinkDao mockDao = EasyMock.createMock(LldpLinkDao.class);
        EasyMock.expect(mockDao.findAll()).andReturn(new ArrayList<LldpLink>()).anyTimes();
        EasyMock.replay(mockDao);
        m_topologyProvider.setLldpLinkDao(mockDao);

        m_topologyProvider.load(null);

        // Should have 8 groups
        assertEquals(8, m_topologyProvider.getVertices().size());
        // Ensure that all of the vertices are groups
        for (Vertex vertex : m_topologyProvider.getVertices()) {
            assertEquals(true, vertex.isGroup());
        }
        Vertex vert1 = m_topologyProvider.getVertex("nodes", "linkdg5");
        Vertex vert2 = m_topologyProvider.getVertex("nodes", "linkdg10");
        Vertex vert3 = m_topologyProvider.getVertex("nodes", "linkdg14");
        Vertex vert4 = m_topologyProvider.getVertex("nodes", "linkdg16");
        Vertex vert5 = m_topologyProvider.getVertex("nodes", "linkdg17");
        Vertex vert6 = m_topologyProvider.getVertex("nodes", "linkdg18");
        Vertex vert7 = m_topologyProvider.getVertex("nodes", "linkdg20");
        Vertex vert8 = m_topologyProvider.getVertex("nodes", "linkdg21");

        assertEquals("Almost Top Group", vert1.getLabel());
        assertEquals(vert1.getParent().toString() + " ?= " + vert7, 0, new RefComparator().compare(vert7, vert1.getParent()));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vert1));

        assertEquals("Group 32", vert2.getLabel());
        assertEquals(vert2.getParent().toString() + " ?= " + vert8, 0, new RefComparator().compare(vert8, vert2.getParent()));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vert2));

        assertEquals("FGHKDKL", vert3.getLabel());
        assertEquals(vert3.getParent().toString() + " ?= " + vert4, 0, new RefComparator().compare(vert4, vert3.getParent()));
        assertEquals(3, m_topologyProvider.getSemanticZoomLevel(vert3));

        assertEquals("Hello Again", vert4.getLabel());
        assertEquals(vert4.getParent().toString() + " ?= " + vert6, 0, new RefComparator().compare(vert6, vert4.getParent()));
        assertEquals(2, m_topologyProvider.getSemanticZoomLevel(vert4));

        assertEquals("Big Group", vert5.getLabel());
        assertEquals(vert5.getParent().toString() + " ?= " + vert2, 0, new RefComparator().compare(vert2, vert5.getParent()));
        assertEquals(2, m_topologyProvider.getSemanticZoomLevel(vert5));

        assertEquals("Smaller Group", vert6.getLabel());
        assertEquals(vert6.getParent().toString() + " ?= " + vert7, 0, new RefComparator().compare(vert7, vert6.getParent()));
        assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vert6));

        assertEquals("Top Three", vert7.getLabel());
        assertEquals(null, vert7.getParent());
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vert7));

        assertEquals("Bottom Four", vert8.getLabel());
        assertEquals(null, vert8.getParent());
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vert8));

        // Reset the DataLinkInterfaceDao
        m_topologyProvider.setLldpLinkDao(dao);
    }

    @Test
    public void testLoadSimpleGraph() throws Exception {
		/*
        m_topologyProvider = new EnhancedLinkdTopologyProvider();
		m_topologyProvider.load("target/test-classes/simple-graph.xml");
        */

        assertEquals(8, m_topologyProvider.getVertices().size());
        assertEquals(9, m_topologyProvider.getEdges().size());

        Vertex v1 = m_topologyProvider.getVertex("nodes", "1");
        Vertex v2 = m_topologyProvider.getVertex("nodes", "2");
        Vertex v3 = m_topologyProvider.getVertex("nodes", "3");
        Vertex v4 = m_topologyProvider.getVertex("nodes", "4");
        Vertex v5 = m_topologyProvider.getVertex("nodes", "5");
        Vertex v6 = m_topologyProvider.getVertex("nodes", "6");
        assertEquals("node1", v1.getLabel());
        assertEquals("192.168.1.1", v1.getIpAddress());
        assertEquals(false, v1.isLocked());
        assertEquals((Object)new Integer(1), (Object)v1.getNodeID());
        assertEquals(false, v1.isSelected());
        assertEquals((Object)new Integer(0), (Object)v1.getX());
        assertEquals((Object)new Integer(0), (Object)v1.getY());
        //assertEquals(v5, v1.getParent());

        //assertEquals(2, m_topologyProvider.getChildren(v5).size());

        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(v1));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(v2));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(v3));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(v4));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(v5));
        assertEquals(0, m_topologyProvider.getSemanticZoomLevel(v6));

        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(v1).length);
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(v2).length);
        assertEquals(2, m_topologyProvider.getEdgeIdsForVertex(v3).length);
        assertEquals(2, m_topologyProvider.getEdgeIdsForVertex(v4).length);
        assertEquals(2, m_topologyProvider.getEdgeIdsForVertex(v5).length);
        assertEquals(2, m_topologyProvider.getEdgeIdsForVertex(v6).length);

        for (Vertex vertex : m_topologyProvider.getVertices()) {
            assertEquals("nodes", vertex.getNamespace());
            //assertTrue(vertex.getIpAddress(), "127.0.0.1".equals(vertex.getIpAddress()) || "64.146.64.214".equals(vertex.getIpAddress()));
        }

        int countLLDP = 0;
        int countOSPF = 0;
        for (Edge edge : m_topologyProvider.getEdges()) {
            if (edge.getNamespace().equals(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE)) {
                countLLDP++;
            } else if (edge.getNamespace().equals(EnhancedLinkdTopologyProvider.OSPF_EDGE_NAMESPACE)) {
                countOSPF++;
            }
        }
        assertEquals(8, countLLDP);
        assertEquals(1, countOSPF);
    }

    @Test
    public void testConnectVertices() {
        m_topologyProvider.resetContainer();

        Vertex vertexId = m_topologyProvider.addVertex(0, 0);

        assertEquals(1, m_topologyProvider.getVertices().size());
        Vertex vertex0 = m_topologyProvider.getVertices().iterator().next();
        assertEquals("v0", vertex0.getId());

        Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
        assertEquals(2, m_topologyProvider.getVertices().size());

        Edge edgeId = m_topologyProvider.connectVertices(vertex0, vertex1);
        assertEquals(1, m_topologyProvider.getEdges().size());
        SimpleLeafVertex sourceLeafVert = (SimpleLeafVertex) edgeId.getSource().getVertex();
        SimpleLeafVertex targetLeafVert = (SimpleLeafVertex) edgeId.getTarget().getVertex();

        assertEquals("v0", sourceLeafVert.getId());
        assertEquals("v1", targetLeafVert.getId());

        EdgeRef[] edgeIds = m_topologyProvider.getEdgeIdsForVertex(vertexId);
        assertEquals(1, edgeIds.length);
        assertEquals(edgeId, edgeIds[0]);

    }

    @Test
    public void testTopoProviderSetParent() {
        VertexRef vertexId1 = addVertexToTopr();
        VertexRef vertexId2 = addVertexToTopr();

        final AtomicInteger eventsReceived = new AtomicInteger(0);

        m_topologyProvider.addVertexListener(new VertexListener() {

            @Override
            public void vertexSetChanged(VertexProvider provider,
                                         Collection<? extends Vertex> added,
                                         Collection<? extends Vertex> update,
                                         Collection<String> removedVertexIds) {
                eventsReceived.incrementAndGet();
            }

            @Override
            public void vertexSetChanged(VertexProvider provider) {
                eventsReceived.incrementAndGet();
            }
        });

        Vertex groupId = m_topologyProvider.addGroup("Test Group", "groupIcon.jpg");
        assertEquals(1, eventsReceived.get());
        eventsReceived.set(0);

        m_topologyProvider.setParent(vertexId1, groupId);
        m_topologyProvider.setParent(vertexId2, groupId);

        assertEquals(2, eventsReceived.get());
    }

    /**
     * Tests that the Linkdprovider does load the information stored in the xml file
     * correctly. So that all groups are loaded as expected and all groups have the expected
     * children as well.
     * @throws java.net.MalformedURLException
     * @throws javax.xml.bind.JAXBException
     */
    @Test
    public void testAssignChildrenToParentsCorrectly() throws MalformedURLException, JAXBException {
        LinkdTopologyProvider topologyProvider = new LinkdTopologyProvider();

        DataLinkInterfaceDao datalinkIfDaoMock = EasyMock.createNiceMock(DataLinkInterfaceDao.class);
        EasyMock.expect(datalinkIfDaoMock.findAll()).andReturn(new ArrayList<DataLinkInterface>()).anyTimes();
        EasyMock.replay(datalinkIfDaoMock);

        topologyProvider.setDataLinkInterfaceDao(datalinkIfDaoMock);
        topologyProvider.setNodeDao(m_databasePopulator.getNodeDao());
        topologyProvider.setIpInterfaceDao(m_databasePopulator.getIpInterfaceDao());
        topologyProvider.setSnmpInterfaceDao(m_databasePopulator.getSnmpInterfaceDao());
        topologyProvider.setFilterManager(new TestFilterManager());
        topologyProvider.setConfigurationFile(getClass().getResource("/saved-linkd-graph2.xml").getFile());
        topologyProvider.setAddNodeWithoutLink(true);
        topologyProvider.load(null); // simulate refresh

        // test if topology is loaded correctly results
        for (int i=0; i<topologyProvider.getGroups().size(); i++)  Assert.assertTrue(topologyProvider.containsVertexId("g" + i));
        Assert.assertFalse(topologyProvider.containsVertexId("g" + topologyProvider.getGroups().size()));
        for (int i=0; i<topologyProvider.getVerticesWithoutGroups().size(); i++) Assert.assertTrue(topologyProvider.containsVertexId("" + (i+1)));
        Assert.assertFalse(topologyProvider.containsVertexId("" + (topologyProvider.getVerticesWithoutGroups().size()+1)));

        // now check the parent stuff
        final String namespace = topologyProvider.getVertexNamespace();
        check(topologyProvider.getVertex(namespace, "1"), m_databasePopulator.getNode1(), topologyProvider.getVertex(namespace, "g2"));
        check(topologyProvider.getVertex(namespace, "2"), m_databasePopulator.getNode2(), null);
        check(topologyProvider.getVertex(namespace, "3"), m_databasePopulator.getNode3(), topologyProvider.getVertex(namespace, "g2"));
        check(topologyProvider.getVertex(namespace, "4"), m_databasePopulator.getNode4(), null);

        // now we need to check the groups as well
        Assert.assertEquals(topologyProvider.getVertex(namespace, "g0").getParent(), null);
        Assert.assertEquals(topologyProvider.getVertex(namespace, "g1").getParent(), null);
        Assert.assertEquals(topologyProvider.getVertex(namespace, "g2").getParent(), topologyProvider.getVertex(namespace, "g1"));

    }

    // checks that the vertex and the node are equal
    private void check(Vertex child, OnmsNode node, Vertex parent) {
        Assert.assertNotNull(child);
        Assert.assertNotNull(child.getTooltipText());
        Assert.assertTrue(child.getTooltipText().length() > 0);
        Assert.assertEquals(child.getNodeID(), node.getId());
        Assert.assertEquals(child.getLabel(), node.getLabel());
        Assert.assertEquals(child.getIpAddress(), InetAddressUtils.str(node.getPrimaryInterface().getIpAddress()));
        Assert.assertNotNull(child.getIconKey());
        Assert.assertEquals(child.isLocked(), false);
        Assert.assertEquals(child.isSelected(), false);
        Assert.assertEquals(child.getParent(), parent);
    }

    public class TestFilterManager implements FilterManager {

        @Override
        public void enableAuthorizationFilter(String[] authorizationGroups) {}

        @Override
        public void disableAuthorizationFilter() {}

        @Override
        public String[] getAuthorizationGroups() {
            return new String[0];
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    private VertexRef addVertexToTopr() {
        return m_topologyProvider.addVertex(0, 0);
    }

    @After
    public void tearDown() {
        m_databasePopulator.tearDown();
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
            m_topologyProvider.setLldpLinkDao(m_databasePopulator.getLldpLinkDao());
            m_topologyProvider.setNodeDao(m_databasePopulator.getNodeDao());
        }
        m_topologyProvider.setConfigurationFile(m_originalFilename);
    }
}
