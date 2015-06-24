/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import com.vaadin.data.Property;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.AutoRefreshSupport;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.topo.*;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.EventRegistry;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextCreator;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OSGiVerticesUpdateManagerTest {

    private class DummyOnmsServiceManager implements OnmsServiceManager {

        @Override
        public <T> void registerAsService(Class<T> serviceClass, T serviceBean, VaadinApplicationContext applicationContext) {
           
        }

        @Override
        public <T> void registerAsService(Class<T> serviceClass, T serviceBean, VaadinApplicationContext applicationContext, Dictionary<String,Object> additionalProperties) {
           
        }

        @Override
        public <T> T getService(Class<T> clazz, VaadinApplicationContext applicationContext) {
            return null; 
        }

        @Override
        public <T> List<T> getServices(Class<T> clazz, VaadinApplicationContext applicationContext, Hashtable<String,Object> additionalProperties) {
            return null; 
        }

        @Override
        public VaadinApplicationContext createApplicationContext(VaadinApplicationContextCreator creator) {
            return null; 
        }

        @Override
        public EventRegistry getEventRegistry() {
            return null; 
        }

        @Override
        public void sessionDestroyed(String s) {
           
        }

        @Override
        public void sessionInitialized(String s) {
           
        }
    }

    private class MockEventProxy implements EventProxy {
        int m_total = 0;
        VerticesUpdateManager.VerticesUpdateEvent m_firedEvent;
        @Override
        public <T> void fireEvent(T eventObject) {
            m_firedEvent = (VerticesUpdateManager.VerticesUpdateEvent) eventObject;
            m_total++;
        }

        @Override
        public <T> void addPossibleEventConsumer(T possibleEventConsumer) {

        }

        public int getFireEventCalls(){
            return m_total;
        }

        /**
         * Return null if firedEvent was retrieved already
         * @return
         */
        public VerticesUpdateManager.VerticesUpdateEvent getLastFiredEvent() {
            VerticesUpdateManager.VerticesUpdateEvent event = m_firedEvent;
            m_firedEvent = null;
            return event;
        }

    }

    private class DummyVaadinApplicationContext implements VaadinApplicationContext {
        private final MockEventProxy m_mockEventProxy;

        public DummyVaadinApplicationContext(MockEventProxy mockEventProxy) {
            m_mockEventProxy = mockEventProxy;
        }

        @Override
        public int getUiId() {
            return 0;
        }

        @Override
        public String getSessionId() {
            return null;
        }

        @Override
        public String getUsername() {
            return null;
        }

        @Override
        public EventProxy getEventProxy(OnmsServiceManager serviceManager) {
            return m_mockEventProxy;
        }

        @Override
        public EventProxy getEventProxy(BundleContext bundleContext) {
            return null;
        }
    }

    @Before
    public void setUp(){

    }

    @Test
    public void testVertexRefSelectedUpdate(){
        MockEventProxy mockEventProxy = new MockEventProxy();
        OnmsServiceManager serviceManager = new DummyOnmsServiceManager();
        VaadinApplicationContext vaadinApplicationContext = new DummyVaadinApplicationContext(mockEventProxy);
        OsgiVerticesUpdateManager updateManager = new OsgiVerticesUpdateManager(serviceManager, vaadinApplicationContext);


        updateManager.selectionChanged(createContextWithVertRefIds(1, 2, 3, 4));

        assertEquals(1, mockEventProxy.getFireEventCalls());
        VerticesUpdateManager.VerticesUpdateEvent event = mockEventProxy.getLastFiredEvent();
        assertEquals(4, event.getVertexRefs().size());

        updateManager.selectionChanged(createContextWithVertRefIds(1, 2, 3, 4));

        assertEquals(1, mockEventProxy.getFireEventCalls());
        VerticesUpdateManager.VerticesUpdateEvent event2 = mockEventProxy.getLastFiredEvent();
        assertNull(event2);

        updateManager.selectionChanged(createContextWithVertRefIds(2, 3, 4, 5));

        assertEquals(2, mockEventProxy.getFireEventCalls());
        VerticesUpdateManager.VerticesUpdateEvent event3 = mockEventProxy.getLastFiredEvent();
        assertEquals(4, event3.getVertexRefs().size());

        updateManager.selectionChanged(createContextWithVertRefIds(5,6));

        assertEquals(3, mockEventProxy.getFireEventCalls());
        VerticesUpdateManager.VerticesUpdateEvent event4 = mockEventProxy.getLastFiredEvent();
        assertEquals(2, event4.getVertexRefs().size());
    }

    @Test
    public void testGraphUpdated(){
        MockEventProxy mockEventProxy = new MockEventProxy();
        OnmsServiceManager serviceManager = new DummyOnmsServiceManager();
        VaadinApplicationContext vaadinApplicationContext = new DummyVaadinApplicationContext(mockEventProxy);
        OsgiVerticesUpdateManager updateManager = new OsgiVerticesUpdateManager(serviceManager, vaadinApplicationContext);

        updateManager.graphChanged(createGraph(1,2,3,4,5,6,7));

        assertEquals(1, mockEventProxy.getFireEventCalls());

        updateManager.selectionChanged(createContextWithVertRefIds(1, 2, 3, 4));

        assertEquals(2, mockEventProxy.getFireEventCalls());

        updateManager.graphChanged(createGraph(1,2,3,4,5,6,7));

        assertEquals(2, mockEventProxy.getFireEventCalls());

    }

    private GraphContainer createGraph(int... vertIds) {
        return new MockGraphContainer(new MockGraph(createVerticsWithIds(vertIds)));
    }

    private List<Vertex> createVerticsWithIds(int... vertexIds) {
        List<Vertex> vertices = new ArrayList<Vertex>();
        for(int i = 0; i < vertexIds.length; i++) {
            Vertex vertex = new TestVertex(String.valueOf(vertexIds[i]), "no-label");
            vertices.add(vertex);
        }

        return vertices;
    }

    private SelectionContext createContextWithVertRefIds(int... vertIds) {
        SelectionContext context = new DefaultSelectionManager();
        List<VertexRef> vertices = createVertexRefsWithIds(vertIds);

        context.setSelectedVertexRefs(vertices);

        return context;
    }

    private List<VertexRef> createVertexRefsWithIds(int... vertIds) {
        List<VertexRef> vertices = new ArrayList<VertexRef>();
        for (int i = 0; i < vertIds.length; i++) {
            VertexRef vRef = new DefaultVertexRef("nodes", "" + vertIds[i], "");
            vertices.add(vRef);
        }
        return vertices;
    }

    private class MockGraph implements Graph{

        private final List<Vertex> m_displayVertices;

        public MockGraph(List<Vertex> vertexRefs) {
            m_displayVertices = vertexRefs;
        }

        @Override
        public Layout getLayout() {
            return null; 
        }

        @Override
        public Collection<Vertex> getDisplayVertices() {
            return m_displayVertices; 
        }

        @Override
        public Collection<Edge> getDisplayEdges() {
            return null; 
        }

        @Override
        public Edge getEdgeByKey(String edgeKey) {
            return null; 
        }

        @Override
        public Vertex getVertexByKey(String vertexKey) {
            return null; 
        }

        @Override
        public void visit(GraphVisitor visitor) throws Exception {
           
        }
    }

    private class MockGraphContainer implements GraphContainer {
        private final MockGraph m_graph;

        public MockGraphContainer(MockGraph graph) {
            m_graph = graph;
        }

        @Override
        public GraphProvider getBaseTopology() {
            return null; 
        }

        @Override
        public void setBaseTopology(GraphProvider graphProvider) {
           
        }

        @Override
        public Criteria[] getCriteria() {
            return null; 
        }

        @Override
        public void addCriteria(Criteria critiera) {
           
        }

        @Override
        public void removeCriteria(Criteria critiera) {
           
        }

        @Override
        public void clearCriteria() {

        }

        @Override
        public void addChangeListener(ChangeListener listener) {
           
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
           
        }

        @Override
        public SelectionManager getSelectionManager() {
            return null; 
        }

        @Override
        public void setSelectionManager(SelectionManager selectionManager) {
           
        }

        @Override
        public Graph getGraph() {
            return m_graph; 
        }

        @Override
        public AutoRefreshSupport getAutoRefreshSupport() {
            return null;
        }

        @Override
        public boolean hasAutoRefreshSupport() {
            return getAutoRefreshSupport() != null;
        }

        @Override
        public Collection<VertexRef> getVertexRefForest(Collection<VertexRef> vertexRefs) {
            return null; 
        }

        @Override
        public MapViewManager getMapViewManager() {
            return null; 
        }

        @Override
        public Property<Double> getScaleProperty() {
            return null; 
        }

        @Override
        public StatusProvider getVertexStatusProvider() {
            return null; 
        }

        @Override
        public void setVertexStatusProvider(StatusProvider statusProvider) {
           
        }

        @Override
        public Set<EdgeStatusProvider> getEdgeStatusProviders() {
            return Collections.emptySet();
        }

        @Override
        public String getSessionId() {
            return null; 
        }

        @Override
        public void setSessionId(String sessionId) {
           
        }

        @Override
        public void setDirty(boolean dirty) {
        }

        @Override
        public int getSemanticZoomLevel() {
            return 0; 
        }

        @Override
        public void setSemanticZoomLevel(int level) {
           
        }

        @Override
        public double getScale() {
            return 0; 
        }

        @Override
        public void setScale(double scale) {
           
        }

        @Override
        public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
           
        }

        @Override
        public LayoutAlgorithm getLayoutAlgorithm() {
            return null; 
        }

        @Override
        public void redoLayout() {
        }

        @Override
        public void fireGraphChanged() {
            // TODO Auto-generated method stub
            
        }
    }
}
