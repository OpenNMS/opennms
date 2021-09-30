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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.EventRegistry;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextCreator;
import org.osgi.framework.BundleContext;

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
        final List<Vertex> vertexRefsWithIds = createVerticsWithIds(vertIds);

        final Graph graphMock = EasyMock.createNiceMock(Graph.class);
        final GraphContainer graphContainerMock = EasyMock.createNiceMock(GraphContainer.class);

        EasyMock.expect(graphMock.getDisplayVertices()).andReturn(vertexRefsWithIds).anyTimes();
        EasyMock.expect(graphContainerMock.getGraph()).andReturn(graphMock).anyTimes();
        EasyMock.replay(graphMock, graphContainerMock);

        return graphContainerMock;
    }

    private List<Vertex> createVerticsWithIds(int... vertexIds) {
        List<Vertex> vertices = new ArrayList<>();
        for(int i = 0; i < vertexIds.length; i++) {
            Vertex vertex = new TestVertex(String.valueOf(vertexIds[i]), "no-label");
            vertices.add(vertex);
        }

        return vertices;
    }

    private SelectionContext createContextWithVertRefIds(int... vertIds) {
        SelectionContext context = new DefaultSelectionManager(createGraph());
        List<VertexRef> vertices = createVertexRefsWithIds(vertIds);

        context.setSelectedVertexRefs(vertices);

        return context;
    }

    private List<VertexRef> createVertexRefsWithIds(int... vertIds) {
        List<VertexRef> vertices = new ArrayList<>();
        for (int i = 0; i < vertIds.length; i++) {
            VertexRef vRef = new DefaultVertexRef("nodes", "" + vertIds[i], "");
            vertices.add(vRef);
        }
        return vertices;
    }
}
