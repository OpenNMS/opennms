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

package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class TopologyComponentTest {

    @Test
    public void testTopologyComponentGraph() throws PaintException {
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        mockInitialSetup(target);
        
        mockDefaultGraph(target);
        
        EasyMock.replay(target);
        
        TestTopologyProvider topoProvider = new TestTopologyProvider("test");
        GraphContainer graphContainer = new SimpleGraphContainer(topoProvider);
		TopologyComponent topoComponent = getTopologyComponent(graphContainer);
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
    }

    private static TopologyComponent getTopologyComponent(GraphContainer dataSource) {
    	BeanItem<GraphContainer> item = new BeanItem<GraphContainer>(dataSource);
        TopologyComponent topologyComponent = new TopologyComponent(dataSource, item.getItemProperty("scale"));
        topologyComponent.setIconRepoManager(new IconRepositoryManager());
        return topologyComponent;
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
        
        EasyMock.replay(target);
        
        TestTopologyProvider topoProvider = new TestTopologyProvider("test");
        GraphContainer graphContainer = new SimpleGraphContainer(topoProvider);
		TopologyComponent topoComponent = getTopologyComponent(graphContainer);
        
        topoProvider.addVertex(0, 0);
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
        
    }
    
    @Test
    public void testTopologyComponentGraphUpdateGroup() throws PaintException {
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        // Initial set up has only the group vertex
        mockInitialSetup(target);
        
        mockGraphTagStart(target);
        
        mockVertex(target);
        
        mockGraphTagEnd(target);
        
        // the set the zoomlevel to 1 and we should see two vertices and an edge
        mockGraphAttrs(target, 1, false);
        
        mockGraphTagStart(target);
        
        mockVertex(target);
        
        mockVertex(target);
        
        mockEdge(target);

        mockGraphTagEnd(target);

        EasyMock.replay(target);
        
        TestTopologyProvider topologyProvider = new TestTopologyProvider("test");
        GraphContainer graphContainer = new SimpleGraphContainer(topologyProvider);
		TopologyComponent topoComponent = getTopologyComponent(graphContainer);
        
        Collection<Vertex> vertIds = topologyProvider.getVertices();
        
        Vertex groupId = topologyProvider.addGroup(this.getClass().getSimpleName(), "GroupIcon.jpg");
        
        for(Vertex v : vertIds) {
            if(v.isLeaf()) {
                topologyProvider.setParent(v, groupId);
            }
            
        }
        
        topoComponent.paintContent(target);
        
        graphContainer.setSemanticZoomLevel(1);
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
        
    }
    
    @Test
    @Ignore
    public void testTopologyComponentSendCorrectEdgeIds() throws PaintException {
        TestTopologyProvider topoProvider = new TestTopologyProvider("test");
        GraphContainer graphContainer = new SimpleGraphContainer(topoProvider);
		TopologyComponent topoComponent = getTopologyComponent(graphContainer);
        topoComponent.setIconRepoManager(new IconRepositoryManager());
        Graph graph = topoComponent.getGraph();
        
        Collection<? extends Edge> edges = graph.getDisplayEdges();
        assertEquals(1, edges.size());
        
        Edge edge = edges.iterator().next();
        
        PaintTarget target = EasyMock.createMock(PaintTarget.class);
        
        mockedDefaultToprData(target);
        
        EasyMock.replay(target);
        
        topoComponent.paintContent(target);
        
        EasyMock.verify(target);
        
        System.err.println("\n****** Right before Creation of a Group ******\n");
        
        Collection<Vertex> vertIds = topoProvider.getVertices();
        
        Vertex groupId = topoProvider.addGroup(this.getClass().getSimpleName(), "GroupIcon.jpg");
        
        for(Vertex v: vertIds) {
            if(v.isLeaf()) {
                topoProvider.setParent(v, groupId);
            }
            
        }
        
        PaintTarget target2 = EasyMock.createMock(PaintTarget.class);
        mockInitialSetup(target2);
        
        mockGraphTagStart(target2);
       
        for(Vertex v : graph.getDisplayVertices()) {
        	String key = v.getKey();
        	mockVertexWithKey(target2, key);
        }
        
        for(Edge e: graph.getDisplayEdges()) {
        	Vertex sourceV = graphContainer.getBaseTopology().getVertex(e.getSource().getVertex());
        	Vertex targetV = graphContainer.getBaseTopology().getVertex(e.getTarget().getVertex());
            String sourceKey = sourceV.getKey();
            String targetKey = targetV.getKey();
            mockEdgeWithKeys(target2, e.getKey(), sourceKey, targetKey);
        }
        mockGraphTagEnd(target2);
        
        EasyMock.replay(target2);
        
        topoComponent.paintContent(target2);
        
        EasyMock.verify(target2);
    }

    private void mockedDefaultToprData(PaintTarget target)
            throws PaintException {
        mockInitialSetup(target);
        
        mockGraphTagStart(target);
        
        mockVertex(target);
        
        mockVertex(target);
        
        mockEdge(target);
        
        mockGraphTagEnd(target);
        
    }

    private void mockInitialSetup(PaintTarget target) throws PaintException {
    	mockGraphAttrs(target, 0, true);
    }
    
    private void mockGraphAttrs(PaintTarget target, int semanticZoomLevel, boolean fitToView) throws PaintException {
        target.addAttribute("scale", 1.0);
        target.addAttribute("clientX", 0);
        target.addAttribute("clientY", 0);
        target.addAttribute("semanticZoomLevel", semanticZoomLevel);
        target.addAttribute("activeTool", "pan");
        target.addAttribute("panToSelection", false);
        target.addAttribute("fitToView", fitToView);
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

    private void mockEdge(PaintTarget target) throws PaintException {
        target.startTag("edge");
        target.addAttribute(eq("key"), EasyMock.notNull(String.class));
        target.addAttribute(eq("source"), EasyMock.notNull(String.class));
        target.addAttribute(eq("target"), EasyMock.notNull(String.class));
        target.addAttribute("selected", false);
        target.addAttribute(eq("cssClass"), EasyMock.notNull(String.class));
        target.addAttribute(eq("tooltipText"), EasyMock.notNull(String.class));
        target.endTag("edge");
    }
    
    private void mockEdgeWithKeys(PaintTarget target, String edgeKey, String sourceId, String targetId) throws PaintException {
        target.startTag("edge");
        target.addAttribute("key", edgeKey);
        target.addAttribute("source", sourceId);
        target.addAttribute("target", targetId);
        target.addAttribute(eq("tooltipText"), EasyMock.notNull(String.class));
        target.endTag("edge");
    }

    private void mockVertex(PaintTarget target) throws PaintException {
        target.startTag("vertex");
        target.addAttribute(EasyMock.eq("key"), EasyMock.notNull(String.class));
        target.addAttribute(eq("initialX"), EasyMock.anyInt());
        target.addAttribute(eq("initialY"), EasyMock.anyInt());
        target.addAttribute(eq("x"), EasyMock.anyInt());
        target.addAttribute(eq("y"), EasyMock.anyInt());
        target.addAttribute(eq("selected"), EasyMock.anyBoolean());
        target.addAttribute(eq("iconUrl"), EasyMock.notNull(String.class));
        target.addAttribute(EasyMock.eq("label"), EasyMock.notNull(String.class));
        target.addAttribute(EasyMock.eq("tooltipText"), EasyMock.notNull(String.class));
        
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
        target.addAttribute(eq("tooltipText"), EasyMock.notNull(String.class));
        target.addAttribute(eq("label"), EasyMock.notNull(String.class));
        
        target.endTag("vertex");
    }

    private String eq(String arg) {
        return EasyMock.eq(arg);
    }

}
