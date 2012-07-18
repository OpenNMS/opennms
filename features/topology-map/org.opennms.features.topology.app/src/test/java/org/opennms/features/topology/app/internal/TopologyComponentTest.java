package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;

import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class TopologyComponentTest {
    private class TestTopologyComponent extends TopologyComponent{

        public TestTopologyComponent(GraphContainer dataSource) {
            super(dataSource);
        }
        
        public Graph getGraph() {
            return super.getGraph();
        }
        
    }
    
    @Test
    public void testTopologyComponentGraph() throws PaintException {
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        mockInitialSetup(target);
        
        mockDefaultGraph(target);
        
        mockActions(target);
        
        EasyMock.replay(target);
        
        TestTopologyProvider topoProvider = new TestTopologyProvider();
        GraphContainer dataSource = new SimpleGraphContainer(topoProvider);
        TopologyComponent topoComponent = new TopologyComponent(dataSource);
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
    }
    
    @Test
    public void testTopologyComponentGraphUpdate() throws PaintException {
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        mockInitialSetup(target);
        
        mockGraphTagStart(target);
        
        mockVertex(target);
        
        mockVertex(target);
        
        mockVertex(target);
        
        mockEdge(target);
        
        mockGraphTagEnd(target);
        
        mockActions(target);
        
        EasyMock.replay(target);
        
        TestTopologyProvider topoProvider = new TestTopologyProvider();
        GraphContainer dataSource = new SimpleGraphContainer(topoProvider);
        TopologyComponent topoComponent = new TopologyComponent(dataSource);
        
        topoProvider.addVertex();
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
        
    }
    
    @Test
    public void testTopologyComponentGraphUpdateGroup() throws PaintException {
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        mockInitialSetup(target);
        
        mockGraphTagStart(target);
        
        mockGroup(target);
        
        mockVertex(target, 1);
        
        mockVertex(target, 1);
        
        mockEdge(target);
        
        mockGraphTagEnd(target);
        
        mockActions(target);
        
        EasyMock.replay(target);
        
        TestTopologyProvider topoProvider = new TestTopologyProvider();
        GraphContainer dataSource = new SimpleGraphContainer(topoProvider);
        TopologyComponent topoComponent = new TopologyComponent(dataSource);
        
        Collection<?> vertIds = topoProvider.getVertexIds();
        
        Object groupId = topoProvider.addGroup("GroupIcon.jpg");
        
        for(Object vertId : vertIds) {
            BeanItem<TestVertex> beanItem = (BeanItem<TestVertex>) topoProvider.getVertexItem(vertId);
            TestVertex v = beanItem.getBean();
            if(v.isLeaf()) {
                topoProvider.setParent(vertId, groupId);
            }
            
        }
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
        
    }
    
    @Test
    public void testTopologyComponentSendCorrectEdgeIds() throws PaintException {
        TestTopologyProvider topoProvider = new TestTopologyProvider();
        GraphContainer dataSource = new SimpleGraphContainer(topoProvider);
        TestTopologyComponent topoComponent = new TestTopologyComponent(dataSource);
        Graph graph = topoComponent.getGraph();
        
        List<Edge> edges = graph.getEdges();
        assertEquals(1, edges.size());
        
        Edge edge = edges.get(0);
        
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        mockedDefaultToprData(target);
        
        EasyMock.replay(target);
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
        
        System.err.println("\n****** Right before Creation of a Group ******\n");
        
        Collection<?> vertIds = topoProvider.getVertexIds();
        
        Object groupId = topoProvider.addGroup("GroupIcon.jpg");
        
        for(Object vertId : vertIds) {
            TestVertex v = (TestVertex) ((BeanItem<TestVertex>) topoProvider.getVertexItem(vertId)).getBean();
            if(v.isLeaf()) {
                topoProvider.setParent(vertId, groupId);
            }
            
        }
        
        PaintTarget target2 = EasyMock.createMock(PaintTarget.class);
        mockInitialSetup(target2);
        
        mockGraphTagStart(target2);
       
        for(Vertex g : graph.getVertices()) {
            if (!g.isLeaf()) {
                String key = g.getKey();
                mockGroupWithKey(target2, key);
            }
        }
        
        for(Vertex v : graph.getVertices()) {
            if (v.isLeaf()) {
                String key = v.getKey();
                mockVertexWithKey(target2, key);
            }
        }
        
        Map<Object, String> verticesKeyMapper = new HashMap<Object, String>();
        for(Vertex v : graph.getVertices()) {
            verticesKeyMapper.put(v.getItemId(), v.getKey());
        }
        
        for(Edge e: graph.getEdges()) {
            String sourceKey = verticesKeyMapper.get(edge.getSource().getItemId());
            String targetKey = verticesKeyMapper.get(edge.getTarget().getItemId());
            mockEdgeWithKeys(target2, edge.getKey(), sourceKey, targetKey);
        }
        mockGraphTagEnd(target2);
        
        mockActions(target2);
        EasyMock.replay(target2);
        
        topoComponent.paintContent(target2);
        
        EasyMock.verify(target2);
    }

    private void mockGroupWithKey(PaintTarget target, String key) throws PaintException {
        target.startTag("group");
        target.addAttribute("key", key);
        target.addAttribute("x", 0);
        target.addAttribute("y", 0);
        target.addAttribute("selected", false);
        target.addAttribute(EasyMock.eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute("semanticZoomLevel", 0);
        target.addAttribute(EasyMock.eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        
        
        target.endTag("group");
        
    }

    private void mockedDefaultToprData(PaintTarget target)
            throws PaintException {
        mockInitialSetup(target);
        
        mockGraphTagStart(target);
        
        mockVertex(target);
        
        mockVertex(target);
        
        mockEdge(target);
        
        mockGraphTagEnd(target);
        
        mockActions(target);
    }

    private void mockGroup(PaintTarget target) throws PaintException {
        target.startTag("group");
        target.addAttribute(EasyMock.eq("key"), EasyMock.notNull(String.class));
        target.addAttribute("x", 0);
        target.addAttribute("y", 0);
        target.addAttribute("selected", false);
        target.addAttribute(EasyMock.eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute("semanticZoomLevel", 0);
        target.addAttribute(EasyMock.eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        
        
        target.endTag("group");
    }
    

    private void mockInitialSetup(PaintTarget target) throws PaintException {
        target.addAttribute("scale", 1.0);
        target.addAttribute("clientX", 0);
        target.addAttribute("clientY", 0);
        target.addAttribute("semanticZoomLevel", 0);
        target.addAttribute(EasyMock.eq("backgroundActions"), EasyMock.aryEq(new Object[0]));
    }

    private void mockDefaultGraph(PaintTarget target) throws PaintException {
        mockGraphTagStart(target);
        mockVertex(target);
        
        mockVertex(target);
        
        mockEdge(target);
        
        mockGraphTagEnd(target);
    }

    private void mockGraphTagEnd(PaintTarget target) throws PaintException {
        target.endTag("graph");
    }

    private void mockGraphTagStart(PaintTarget target) throws PaintException {
        target.startTag("graph");
    }

    private void mockActions(PaintTarget target) throws PaintException {
        target.startTag("actions");
        target.endTag("actions");
    }

    private void mockEdge(PaintTarget target) throws PaintException {
        target.startTag("edge");
        target.addAttribute(eq("key"), EasyMock.notNull(String.class));
        target.addAttribute(eq("source"), EasyMock.notNull(String.class));
        target.addAttribute(eq("target"), EasyMock.notNull(String.class));
        target.addAttribute(eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        target.endTag("edge");
    }
    
    private void mockEdgeWithKeys(PaintTarget target, String edgeKey, String sourceId, String targetId) throws PaintException {
        target.startTag("edge");
        target.addAttribute("key", edgeKey);
        target.addAttribute("source", sourceId);
        target.addAttribute("target", targetId);
        target.addAttribute(eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        target.endTag("edge");
    }

    private void mockVertex(PaintTarget target) throws PaintException {
        mockVertex(target, 0);
    }
    
    private void mockVertex(PaintTarget target, int semanticZoomLevel) throws PaintException {
        target.startTag("vertex");
        target.addAttribute(EasyMock.eq("key"), EasyMock.notNull(String.class));
        target.addAttribute(eq("x"), EasyMock.anyInt());
        target.addAttribute(eq("y"), EasyMock.anyInt());
        target.addAttribute(eq("selected"), EasyMock.anyBoolean());
        target.addAttribute(eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute("semanticZoomLevel", semanticZoomLevel);
        if(semanticZoomLevel > 0) {
            target.addAttribute(EasyMock.eq("groupKey"), EasyMock.notNull(String.class));
        }
        target.addAttribute(eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        
        target.endTag("vertex");
    }
    
    private void mockVertexWithKey(PaintTarget target, String key) throws PaintException {
        target.startTag("vertex");
        target.addAttribute("key", key);
        target.addAttribute(eq("x"), EasyMock.anyInt());
        target.addAttribute(eq("y"), EasyMock.anyInt());
        target.addAttribute(eq("selected"), EasyMock.anyBoolean());
        target.addAttribute(eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute("semanticZoomLevel", 1);
        target.addAttribute(EasyMock.eq("groupKey"), EasyMock.notNull(String.class));
        target.addAttribute(eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        
        target.endTag("vertex");
    }

    private String eq(String arg) {
        return EasyMock.eq(arg);
    }

}
