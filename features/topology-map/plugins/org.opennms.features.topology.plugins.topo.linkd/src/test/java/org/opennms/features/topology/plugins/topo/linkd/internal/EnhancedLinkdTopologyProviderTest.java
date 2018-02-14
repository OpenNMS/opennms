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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OspfLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-enhanced-mock.xml"
})
public class EnhancedLinkdTopologyProviderTest {

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
        m_topologyProvider.refresh();
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
        Assert.assertTrue("linkd.system.snmp.1.3.6.1.4.1.5813.1.25".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode1().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode2().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode3().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode4().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode5().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode6().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode7().getSysObjectId())));
        Assert.assertTrue("linkd.system".equals(EnhancedLinkdTopologyProvider.getIconName(m_databasePopulator.getNode8().getSysObjectId())));

    }

    @Test
    public void testAddGroup() {
        Vertex parentId = m_topologyProvider.addGroup("Linkd Group", "linkd:group");
        Assert.assertEquals(true, m_topologyProvider.containsVertexId(parentId));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void test() throws Exception {
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
        VertexRef vertexAref = new DefaultVertexRef(m_topologyProvider.getNamespace(), "v0");
        VertexRef vertexBref = new DefaultVertexRef(m_topologyProvider.getNamespace(), "v1");
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
        // Groups should not be reloaded, because they are not persisted
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(group1)).size());
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(group2)).size());
        assertEquals(8, m_topologyProvider.getVertices().size());
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
    public void testLoadSimpleGraph() throws Exception {
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
        assertEquals(new Integer(1), v1.getNodeID());
        assertEquals(false, v1.isSelected());
        assertEquals(new Integer(0), v1.getX());
        assertEquals(new Integer(0), v1.getY());

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
    }
}
