package org.opennms.features.topology.app.internal;

import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;

import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class TopologyComponentTest {
    
    
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
        
        target.startTag("group");
        target.addAttribute("key", "1");
        target.addAttribute("x", 0);
        target.addAttribute("y", 0);
        target.addAttribute("selected", false);
        target.addAttribute(EasyMock.eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute("semanticZoomLevel", 0);
        target.addAttribute(EasyMock.eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        
        
        target.endTag("group");
        
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

    private void mockVertex(PaintTarget target) throws PaintException {
        mockVertex(target, 0);
    }
    
    private void mockVertex(PaintTarget target, int semanticZoomLevel) throws PaintException {
        target.startTag("vertex");
        target.addAttribute(EasyMock.eq("id"), EasyMock.notNull(String.class));
        target.addAttribute(eq("x"), EasyMock.anyInt());
        target.addAttribute(eq("y"), EasyMock.anyInt());
        target.addAttribute(eq("selected"), EasyMock.anyBoolean());
        target.addAttribute(eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute("semanticZoomLevel", semanticZoomLevel);
        if(semanticZoomLevel > 0) {
            target.addAttribute("groupKey", "9");
        }
        target.addAttribute(eq("actionKeys"), EasyMock.aryEq(new Object[0]));
        
        target.endTag("vertex");
    }

    private String eq(String arg) {
        return EasyMock.eq(arg);
    }

}
