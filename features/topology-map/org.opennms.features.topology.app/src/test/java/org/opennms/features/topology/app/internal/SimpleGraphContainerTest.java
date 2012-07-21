package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.features.topology.app.internal.SimpleGraphContainer.GEdge;
import org.opennms.features.topology.app.internal.SimpleGraphContainer.GVertex;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class SimpleGraphContainerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleGraphContainer() {
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(new TestTopologyProvider());
        Collection<?> vertexIds = graphContainer.getVertexIds();
        Collection<?> edgeIds = graphContainer.getEdgeIds();
        
        Object edgeId = edgeIds.iterator().next();
        
        BeanItem<GEdge> edgeItem = (BeanItem<GEdge>) graphContainer.getEdgeItem(edgeId);
        GEdge edge= edgeItem.getBean();
        assertEquals("e0", edge.getItemId());
        
        assertEquals(2, vertexIds.size());
        assertEquals(1, edgeIds.size());
        
        
    }
    
    @Test
    public void testGraph() {
        SimpleGraphContainer graphContaier = new SimpleGraphContainer(new TestTopologyProvider());
        Graph graph = new Graph(graphContaier);
        
        List<Vertex> vertices = graph.getVertices();
        assertEquals(2, vertices.size());
        
        List<Edge> edges = graph.getEdges();
        assertEquals(1, edges.size());
        
    }
    
    @Test
    public void testGroupingVertices() {
        TestTopologyProvider topologyProvider = new TestTopologyProvider();
        
        Object groupId = topologyProvider.addGroup("groupIcon.jpg");
        topologyProvider.setParent("v0", groupId);
        topologyProvider.setParent("v1", groupId);
        
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
        graphContainer.setSemanticZoomLevel(0);
        
        Collection<?> gcIds = graphContainer.getVertexIds();
        assertEquals(3, gcIds.size());
        
        for(Object gcId : gcIds) {
            Item gcItem = graphContainer.getVertexItem(gcId);
            Boolean leaf = (Boolean) gcItem.getItemProperty("leaf").getValue();
            System.out.println("Expecting gcItem: " + gcItem + " id: " + gcId + " leaf is true: " + leaf);
            if(leaf) {
                Object parentId = graphContainer.getVertexContainer().getParent(gcId);
                assertNotNull(parentId);
                
                Object semanticZoomLevel = gcItem.getItemProperty("semanticZoomLevel").getValue();
                assertEquals(1, semanticZoomLevel);
            }
        }
        
        
        Graph graph = new Graph(graphContainer);
        
        List<Vertex> vertices = graph.getVertices();
        assertEquals(3, vertices.size());
        for(Vertex v : vertices) {
            if(v.getGroupId() == null) {
                assertEquals(0, v.getSemanticZoomLevel());
            }else {
                assertEquals(1, v.getSemanticZoomLevel());
            }
        }
        
        List<Vertex> leafVertices = graph.getLeafVertices();
        assertEquals(2, leafVertices.size());
        for(Vertex v: leafVertices) {
            assertEquals(1, v.getSemanticZoomLevel());
        }
        
        List<Edge> edges = graph.getEdges();
        assertEquals(1, edges.size());
    }
    
    @Test
    public void testUpdateTopologyProviderUpdatesGraphContainer() {
        TestTopologyProvider topologyProvider = new TestTopologyProvider();
        
        //Setup the graphcontainer
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
        graphContainer.setSemanticZoomLevel(0);
        
        //Add another vertex to the TopologyProvider
        Object vertId = topologyProvider.addVertex();
        
        Collection<?> gcIds = graphContainer.getVertexIds();
        assertEquals(3, gcIds.size());
        
        Object vertId2 = topologyProvider.addVertex();
        assertEquals(4, gcIds.size());
       
        Object groupId = topologyProvider.addGroup("iconofgroup.jpg");
        assertEquals(5, graphContainer.getVertexIds().size());
        
        topologyProvider.setParent(vertId, groupId);
        topologyProvider.setParent(vertId2, groupId);
        
        Collection<?> vertexKeys = graphContainer.getVertexIds();
        for(Object vertexKey : vertexKeys) {
            BeanItem<GVertex> vItem = (BeanItem<GVertex>) graphContainer.getVertexItem(vertexKey);
            GVertex gVert = vItem.getBean();
            if(gVert.getItemId() == vertId || gVert.getItemId() == vertId2) {
                assertEquals(groupId, gVert.getGroupId());
            }
        }
        
        
    }
    
    @Test
    public void testGraphContainerSendUpdateEvents() {
        final AtomicInteger eventsReceived = new AtomicInteger(0);
        
        TestTopologyProvider topologyProvider = new TestTopologyProvider();
        
        //Setup the graphcontainer
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
        graphContainer.setSemanticZoomLevel(0);
        
        graphContainer.getVertexContainer().addListener(new ItemSetChangeListener() {
            
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                eventsReceived.incrementAndGet();
            }
        });
        
        
        
        //Add another vertex to the TopologyProvider
        Object vertId = topologyProvider.addVertex();
        assertEquals(2, eventsReceived.get());
        eventsReceived.set(0);
        
        Object vertId2 = topologyProvider.addVertex();
        assertEquals(2, eventsReceived.get());
        eventsReceived.set(0);
       
        Object groupId = topologyProvider.addGroup("iconofgroup.jpg");
        assertEquals(5, graphContainer.getVertexIds().size());
        assertEquals(2, eventsReceived.get());
        eventsReceived.set(0);
        
        topologyProvider.setParent(vertId, groupId);
        //assertEquals(2, eventsReceived.get());
        assertEquals(1, eventsReceived.get());
        eventsReceived.set(0);
        
        topologyProvider.setParent(vertId2, groupId);
        //assertEquals(2, eventsReceived.get());
        assertEquals(1, eventsReceived.get());
        eventsReceived.set(0);
        
        Collection<?> vertexKeys = graphContainer.getVertexIds();
        for(Object vertexKey : vertexKeys) {
            BeanItem<GVertex> vItem = (BeanItem<GVertex>) graphContainer.getVertexItem(vertexKey);
            GVertex gVert = vItem.getBean();
            if(gVert.getItemId() == vertId || gVert.getItemId() == vertId2) {
                assertEquals(groupId, gVert.getGroupId());
                assertEquals(1, gVert.getSemanticZoomLevel());
            }else {
                assertEquals(0, gVert.getSemanticZoomLevel());
            }
        }
        
        
    }
    
    @Test
    public void testGraphContainerUpdatesGraph() {
        final AtomicInteger eventsReceived = new AtomicInteger(0);
        
        TestTopologyProvider topologyProvider = new TestTopologyProvider();
        
        //Setup the graphcontainer
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
        graphContainer.setSemanticZoomLevel(0);
        
        final Graph graph = new Graph(graphContainer);
        
        graphContainer.getVertexContainer().addListener(new ItemSetChangeListener() {
            
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                graph.update();
            }
        });
        
        
        
        //Add another vertex to the TopologyProvider
        Object vertId = topologyProvider.addVertex();
        Vertex v = graph.getVertexByItemId(findByItemId(graphContainer, vertId));
        assertNotNull(v);
        
        Object vertId2 = topologyProvider.addVertex();
        Vertex v2 = graph.getVertexByItemId(findByItemId(graphContainer, vertId2));
        assertNotNull(v2);
        
        Object groupId = topologyProvider.addGroup("iconofgroup.jpg");
        assertEquals(5, graphContainer.getVertexIds().size());
        Vertex g = graph.getVertexByItemId(findByItemId(graphContainer, groupId));
        assertNotNull(g);
        
        topologyProvider.setParent(vertId, groupId);
        v = graph.getVertexByItemId(findByItemId(graphContainer, vertId));
        assertEquals( g.getItemId(), v.getGroupId());
        
        topologyProvider.setParent(vertId2, groupId);
        v2 = graph.getVertexByItemId(findByItemId(graphContainer, vertId2));
        assertEquals( g.getItemId(), v2.getGroupId());
        
        Collection<?> vertexKeys = graphContainer.getVertexIds();
        for(Object vertexKey : vertexKeys) {
            BeanItem<GVertex> vItem = (BeanItem<GVertex>) graphContainer.getVertexItem(vertexKey);
            GVertex gVert = vItem.getBean();
            if(gVert.getItemId() == vertId || gVert.getItemId() == vertId2) {
                assertEquals(groupId, gVert.getGroupId());
                assertEquals(1, gVert.getSemanticZoomLevel());
            }else {
                assertEquals(0, gVert.getSemanticZoomLevel());
            }
        }
        
        
    }
    
    public Object findByItemId(SimpleGraphContainer graphContainer, Object vertexId) {
        
        Collection<?> vertexKeys = graphContainer.getVertexIds();
        for(Object vertexKey : vertexKeys) {
            BeanItem<GVertex> vItem = (BeanItem<GVertex>) graphContainer.getVertexItem(vertexKey);
            GVertex gVert = vItem.getBean();
            if(gVert.getItemId().equals(vertexId)) {
                return gVert.getKey();
            }
        }
        
        return null;
    }
    
    
    @Test
    public void testUpdateTopologyProviderUpdatesGraph() {
        TestTopologyProvider topologyProvider = new TestTopologyProvider();
        
        //Setup the graphcontainer
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
        graphContainer.setSemanticZoomLevel(0);
        
        Graph graph = new Graph(graphContainer);
        List<Vertex> vertices = graph.getVertices();
        assertEquals(2, vertices.size());
        
        //Add another vertex to the TopologyProvider
        Object vertId = topologyProvider.addVertex();
        graph.update();
        
        assertEquals(3, topologyProvider.getVertexIds().size());
        assertEquals(3, graphContainer.getVertexIds().size());
        assertEquals(3, graph.getVertices().size());
        
        Collection<?> gcIds = graphContainer.getVertexIds();
        assertEquals(3, gcIds.size());
        
        Object vertId2 = topologyProvider.addVertex();
        graph.update();
        assertEquals(4, gcIds.size());
        
        assertEquals(4, graph.getVertices().size());
        
    }
    
    @Test
    public void testGraphContainerElementIds() {
        TestTopologyProvider topologyProvider = new TestTopologyProvider();

        Object toprEdgeId = topologyProvider.getEdgeIds().iterator().next();
        
        TestEdge testEdge = (TestEdge) topologyProvider.getEdgeContainer().getItem(toprEdgeId).getBean();
        TestVertex sourceVertex = testEdge.getSource();
        TestVertex targetVertex = testEdge.getTarget();
        
        
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        
        GVertex sourceGVertex = findGVertexWithToprId(graphContainer, sourceVertex.getId());
        GVertex targetGVertex = findGVertexWithToprId(graphContainer, targetVertex.getId());
        
        GEdge gEdge = findGEdgeWithToprId(graphContainer, testEdge.getId());
        
        assertEquals(sourceGVertex.getItemId(), gEdge.getSource().getItemId());
        assertEquals(targetGVertex.getItemId(), gEdge.getTarget().getItemId());
        assertEquals(sourceGVertex.getKey(), gEdge.getSource().getKey());
        assertEquals(targetGVertex.getKey(), gEdge.getTarget().getKey());
        
        
        Object groupId = topologyProvider.addGroup("iconGroup.png");
        topologyProvider.setParent(sourceVertex.getId(), groupId);
        topologyProvider.setParent(targetVertex.getId(), groupId);
        
        
        sourceGVertex = findGVertexWithToprId(graphContainer, sourceVertex.getId());
        targetGVertex = findGVertexWithToprId(graphContainer, targetVertex.getId());
        
        gEdge = findGEdgeWithToprId(graphContainer, testEdge.getId());
        
        assertEquals(sourceGVertex.getItemId(), gEdge.getSource().getItemId());
        assertEquals(targetGVertex.getItemId(), gEdge.getTarget().getItemId());
        assertEquals(sourceGVertex.getKey(), gEdge.getSource().getKey());
        assertEquals(targetGVertex.getKey(), gEdge.getTarget().getKey());
        
    }
    
    private GEdge findGEdgeWithToprId(SimpleGraphContainer graphContainer, String id) {
        for(Object edgeId : graphContainer.getEdgeIds()) {
            GEdge edge = (GEdge) graphContainer.getEdgeContainer().getItem(edgeId).getBean();
            if (id.equals(edge.getItemId())) {
                return edge;
            }
        }
        return null;
    }

    private GVertex findGVertexWithToprId(SimpleGraphContainer graphContainer, String id) {
        for(Object vertexId : graphContainer.getVertexIds()) {
            GVertex vertex = (GVertex) graphContainer.getVertexContainer().getItem(vertexId).getBean();
            if (id.equals(vertex.getItemId())) {
                return vertex;
            }
        }
        return null;
    }
    
    @Test
    public void testGraphElementIds() {
        TestTopologyProvider topologyProvider = new TestTopologyProvider();

        Object toprEdgeId = topologyProvider.getEdgeIds().iterator().next();
        
        TestEdge testEdge = (TestEdge) topologyProvider.getEdgeContainer().getItem(toprEdgeId).getBean();
        TestVertex sourceTestVertex = testEdge.getSource();
        TestVertex targetTestVertex = testEdge.getTarget();
        
        
        SimpleGraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
        Graph graph = new Graph(graphContainer);
        
        Vertex sourceVertex = findVertexWithToprId(graph, graphContainer, sourceTestVertex.getId());
        Vertex targetVertex = findVertexWithToprId(graph, graphContainer, targetTestVertex.getId());
        
        Edge edge = findEdgeWithToprId(graph, graphContainer, testEdge.getId());
        
        assertNotNull(edge);
        assertNotNull(edge.getSource());
        assertNotNull(edge.getTarget());
        assertEquals(sourceVertex.getItemId(), edge.getSource().getItemId());
        assertEquals(targetVertex.getItemId(), edge.getTarget().getItemId());
        assertEquals(sourceVertex.getKey(), edge.getSource().getKey());
        assertEquals(targetVertex.getKey(), edge.getTarget().getKey());
        
        
        Object groupId = topologyProvider.addGroup("iconGroup.png");
        topologyProvider.setParent(sourceTestVertex.getId(), groupId);
        topologyProvider.setParent(targetTestVertex.getId(), groupId);
        
        // Need to call graph.update since the topology component is not here to do it
        graph.update();
        graph.update();
        graph.update();
        
        System.err.printf("Vertices: %s\n", graph.getVertices());
        System.err.printf("Edges: %s\n", graph.getEdges());
        
        
        sourceVertex = findVertexWithToprId(graph, graphContainer, sourceTestVertex.getId());
        targetVertex = findVertexWithToprId(graph, graphContainer, targetTestVertex.getId());
        
        edge = findEdgeWithToprId(graph, graphContainer, testEdge.getId());
     
        assertEquals(sourceVertex.getItemId(), edge.getSource().getItemId());
        assertEquals(targetVertex.getItemId(), edge.getTarget().getItemId());
        assertEquals(sourceVertex.getKey(), edge.getSource().getKey());
        assertEquals(targetVertex.getKey(), edge.getTarget().getKey());
        
    }

    private Edge findEdgeWithToprId(Graph graph, SimpleGraphContainer gc, String edgeToprId) {
        GEdge gEdge = findGEdgeWithToprId(gc, edgeToprId);
        
        assertNotNull(gEdge);
        for(Edge e : graph.getEdges()) {
            if (e.getItemId().equals(gEdge.getKey())) {
                return e;
            }
        }
        return null;
    }

    private Vertex findVertexWithToprId(Graph graph, SimpleGraphContainer gc, String vertexToprId) {
        
        GVertex gVertex = findGVertexWithToprId(gc, vertexToprId);
        assertNotNull(gVertex);
        
        for(Vertex v : graph.getVertices()) {
            if (v.getItemId().equals(gVertex.getKey())) {
                return v;
            }
        }
        return null;
    }


}
