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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.operations.ConnectOperation;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphProvider;

import com.vaadin.ui.UI;

public class ConnectOperationTest {

    private static class TestOperationContext implements OperationContext {
        
        private GraphContainer m_graphContainer;

        public TestOperationContext(GraphContainer graphContainer) {
            m_graphContainer = graphContainer;
        }
        
        @Override
        public UI getMainWindow() {
            return EasyMock.createMock(UI.class);
        }

        @Override
        public GraphContainer getGraphContainer() {
            return m_graphContainer;
        }

        @Override
        public boolean isChecked() {
            // TODO Auto-generated method stub
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

    private GraphProvider m_topologyProvider;

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

    @Test
    public void testConnectVerticesOperation() {
    	
		m_topologyProvider.resetContainer();

        VertexRef vertexId1 = addVertexToTopr();
        VertexRef vertexId2 = addVertexToTopr();
        
        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

        EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();

        EasyMock.replay(graphContainer);
        
        List<VertexRef> targets = new ArrayList<VertexRef>();
        targets.add(vertexId1);
        targets.add(vertexId2);
        
        ConnectOperation connectOperation = new ConnectOperation();
        connectOperation.execute(targets, getOperationContext(graphContainer));
        
        Collection<? extends Edge> edgeIds = m_topologyProvider.getEdges();
        assertEquals(1, edgeIds.size());
        
        for(Edge edgeId : edgeIds) {
            SimpleLeafVertex source = (SimpleLeafVertex) edgeId.getSource().getVertex();
            SimpleLeafVertex target = (SimpleLeafVertex) edgeId.getTarget().getVertex();
            assertNotNull(source);
            assertNotNull(target);
            assertEquals(vertexId1.getId(), source.getId());
            assertEquals(vertexId2.getId(), target.getId());
        }
        
        EasyMock.verify(graphContainer);
    }
}
