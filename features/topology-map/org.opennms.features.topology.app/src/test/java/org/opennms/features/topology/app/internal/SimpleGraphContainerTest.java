package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.opennms.features.topology.app.internal.SimpleGraphContainer.GEdge;

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
        assertEquals(2, vertices);
        
        //Add another vertex to the TopologyProvider
        Object vertId = topologyProvider.addVertex();
        
        assertEquals(3, vertices.size());
        
        Collection<?> gcIds = graphContainer.getVertexIds();
        assertEquals(3, gcIds.size());
        
        Object vertId2 = topologyProvider.addVertex();
        assertEquals(4, gcIds.size());
        
        assertEquals(4, vertices.size());
        
        
        
    }
    
}
