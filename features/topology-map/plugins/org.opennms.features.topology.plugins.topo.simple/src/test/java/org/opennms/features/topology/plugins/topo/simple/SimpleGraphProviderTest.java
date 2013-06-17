/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXB;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedLeafVertex;
import org.opennms.features.topology.api.topo.WrappedVertex;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.AddVertexOperation;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.RemoveVertexOperation;

import com.vaadin.ui.Window;

public class SimpleGraphProviderTest {

    private static class TestOperationContext implements OperationContext {

        private GraphContainer m_graphContainer;

        public TestOperationContext(GraphContainer graphContainer) {
            m_graphContainer = graphContainer;
        }

        @Override
        public Window getMainWindow() {
            return EasyMock.createMock(Window.class);
        }

        @Override
        public GraphContainer getGraphContainer() {
            return m_graphContainer;
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public DisplayLocation getDisplayLocation() {
            return DisplayLocation.MENUBAR;
        }

    }

    private static TestOperationContext getOperationContext(GraphContainer mockedContainer) {
        return new TestOperationContext(mockedContainer);
    }

    private VertexRef addVertexToTopr() {
        return m_topologyProvider.addVertex(0, 0);
    }

    private SimpleGraphProvider m_topologyProvider;

    @Before
    public void setUp() {
        if(m_topologyProvider == null) {
            m_topologyProvider = new SimpleGraphProvider();
        }

        m_topologyProvider.resetContainer();

        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() {
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
        }
    }

    /**
     * This test makes sure that the afterUnmarshall() functions are working on the
     * {@link WrappedVertex} class.
     */
    @Test
    public void testUnmarshallVertex() throws Exception {
        String vertexString = "<graph namespace=\"blah\"><vertex><id>hello</id></vertex></graph>";
        WrappedGraph graph = JAXB.unmarshal(new ByteArrayInputStream(vertexString.getBytes()), WrappedGraph.class);
        assertEquals("blah", graph.m_namespace);
        assertEquals(1, graph.m_vertices.size());
        WrappedVertex vertex = graph.m_vertices.get(0);
        assertEquals("hello", vertex.id);
        assertEquals("blah", vertex.namespace);
    }

    @Test
    public void test() throws Exception {
        assertEquals(0, m_topologyProvider.getVertices().size());

        Vertex vertexA = m_topologyProvider.addVertex(50, 100);
        assertEquals(1, m_topologyProvider.getVertices().size());
        //LoggerFactory.getLogger(this.getClass()).debug(m_topologyProvider.getVertices().get(0).toString());
        assertTrue(m_topologyProvider.containsVertexId(vertexA));
        assertTrue(m_topologyProvider.containsVertexId("v0"));
        assertFalse(m_topologyProvider.containsVertexId("v1"));
        ((AbstractVertex)vertexA).setIpAddress("10.0.0.4");
        final String namespace = m_topologyProvider.getVertexNamespace();
        VertexRef ref0 = new AbstractVertexRef(namespace, "v0", namespace + ":v0");
        VertexRef ref1 = new AbstractVertexRef(namespace, "v1", namespace + ":v0");
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());

        Vertex vertexB = m_topologyProvider.addVertex(100, 50);
        assertTrue(m_topologyProvider.containsVertexId(vertexB));
        assertTrue(m_topologyProvider.containsVertexId("v1"));
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());

        Vertex vertexC = m_topologyProvider.addVertex(100, 150);
        Vertex vertexD = m_topologyProvider.addVertex(150, 100);
        Vertex vertexE = m_topologyProvider.addVertex(200, 200);
        assertEquals(5, m_topologyProvider.getVertices().size());

        Vertex group1 = m_topologyProvider.addGroup("Group 1", Constants.GROUP_ICON_KEY);
        Vertex group2 = m_topologyProvider.addGroup("Group 2", Constants.GROUP_ICON_KEY);
        assertEquals(7, m_topologyProvider.getVertices().size());

        m_topologyProvider.setParent(vertexA, group1);
        m_topologyProvider.setParent(vertexB, group1);
        m_topologyProvider.setParent(vertexC, group2);
        m_topologyProvider.setParent(vertexD, group2);

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
        assertEquals("simple", wrappedVertex.namespace);
        assertEquals("10.0.0.4", wrappedVertex.ipAddr);
        assertEquals(50, wrappedVertex.x.intValue());
        assertEquals(100, wrappedVertex.y.intValue());

        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());
        assertEquals(7, m_topologyProvider.getVertices().size());
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref0)).length);
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref1)).length);

        m_topologyProvider.save("target/test-classes/test-graph.xml");

        m_topologyProvider.resetContainer();

        // Ensure that the topology provider has been erased
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(0, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());
        assertEquals(0, m_topologyProvider.getVertices().size());
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref0)).length);
        assertEquals(0, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref1)).length);

        m_topologyProvider.load("target/test-classes/test-graph.xml");

        // Ensure that all of the content has been reloaded properly
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(1, m_topologyProvider.getVertices(Collections.singletonList(ref1)).size());
        assertEquals(7, m_topologyProvider.getVertices().size());
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref0)).length);
        assertEquals(3, m_topologyProvider.getEdgeIdsForVertex(m_topologyProvider.getVertex(ref1)).length);
    }

    @Test
    public void loadSampleGraph() throws Exception {
        GraphProvider topologyProvider = new SimpleGraphProvider();
        topologyProvider.load("saved-vmware-graph.xml");

        System.err.println("Vertex Count: " + topologyProvider.getVertices().size());
        System.err.println("Edge Count: " + topologyProvider.getEdges().size());
    }

    @Test
    public void testLoadSimpleGraph() throws Exception {
        SimpleGraphProvider topologyProvider = new SimpleGraphProvider();
        topologyProvider.load(URI.create("file:target/test-classes/simple-graph.xml"));

        assertEquals(7, topologyProvider.getVertices().size());
        assertEquals(7, topologyProvider.getEdges().size());

        Vertex v0 = topologyProvider.getVertex("vertex", "v0");
        Vertex v1 = topologyProvider.getVertex("vertex", "v1");
        Vertex v2 = topologyProvider.getVertex("vertex", "v2");
        Vertex v3 = topologyProvider.getVertex("vertex", "v3");
        Vertex v4 = topologyProvider.getVertex("vertex", "v4");
        Vertex g0 = topologyProvider.getVertex("vertex", "g0");
        assertEquals("Vertex v0", v0.getLabel());
        assertEquals("64.146.64.214", v0.getIpAddress());
        assertEquals(false, v0.isLocked());
        assertEquals(new Integer(-1), v0.getNodeID());
        assertEquals(false, v0.isSelected());
        assertEquals(new Integer(50), v0.getX());
        assertEquals(new Integer(100), v0.getY());
        assertEquals(g0, v0.getParent());

        assertEquals(2, topologyProvider.getChildren(g0).size());

        assertEquals(0, topologyProvider.getSemanticZoomLevel(g0));
        assertEquals(1, topologyProvider.getSemanticZoomLevel(v0));

        assertEquals(3, topologyProvider.getEdgeIdsForVertex(v0).length);
        assertEquals(3, topologyProvider.getEdgeIdsForVertex(v1).length);
        assertEquals(3, topologyProvider.getEdgeIdsForVertex(v2).length);
        assertEquals(3, topologyProvider.getEdgeIdsForVertex(v3).length);
        assertEquals(2, topologyProvider.getEdgeIdsForVertex(v4).length);

        for (Vertex vertex : topologyProvider.getVertices()) {
            assertEquals("vertex", vertex.getNamespace());
            assertTrue(vertex.getIpAddress(), "127.0.0.1".equals(vertex.getIpAddress()) || "64.146.64.214".equals(vertex.getIpAddress()));
        }
        for (Edge edge : topologyProvider.getEdges()) {
            assertEquals("vertex", edge.getNamespace());
        }
    }

    @Test
    public void testAddVertexWithOperation() {

        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

        EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
        graphContainer.redoLayout();

        EasyMock.replay(graphContainer);

        List<VertexRef> targets = Collections.emptyList();
        OperationContext operationContext = getOperationContext(graphContainer);

        AddVertexOperation addOperation = new AddVertexOperation(Constants.GROUP_ICON_KEY);
        addOperation.execute(targets, operationContext);

        Collection<? extends Vertex> vertIds =  m_topologyProvider.getVertices();
        assertEquals(1, vertIds.size());
        assertTrue("v0".equals(vertIds.iterator().next().getId()));
    }

    @Test
    public void testAddVertexToAnotherVertexOperation() {

        m_topologyProvider.resetContainer();

        //Add existing vertex
        VertexRef vertexRef = addVertexToTopr();

        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

        EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
        graphContainer.redoLayout();

        EasyMock.replay(graphContainer);


        List<VertexRef> targets = new ArrayList<VertexRef>();
        targets.add(vertexRef);

        OperationContext operationContext = getOperationContext(graphContainer);
        AddVertexOperation addOperation = new AddVertexOperation(Constants.SERVER_ICON_KEY);
        addOperation.execute(targets, operationContext);

        Collection<? extends Vertex> vertIds = m_topologyProvider.getVertices();
        assertEquals(2, vertIds.size());

        Collection<? extends Edge> edgeIds = m_topologyProvider.getEdges();
        assertEquals(1, edgeIds.size());

        EasyMock.verify(graphContainer);

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
    public void testRemoveVertexOperation() {
        m_topologyProvider.resetContainer();

        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

        EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
        graphContainer.redoLayout();

        EasyMock.replay(graphContainer);

        VertexRef vertexRef = addVertexToTopr();

        OperationContext operationContext = getOperationContext(graphContainer);
        RemoveVertexOperation removeOperation = new RemoveVertexOperation();
        removeOperation.execute(Arrays.asList(vertexRef), operationContext);

        assertEquals(0, m_topologyProvider.getVertices().size());

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
}
