/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.info;

import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;

public class GenericInfoPanelItemProviderTest {

    public class TestVertexRef extends AbstractVertex {

        public TestVertexRef(String id, String label, Integer nodeId) {
            super("nodes", id, label);

            if (nodeId != null) {
                setNodeID(nodeId);
            }
        }

        @Override
        public int compareTo(Ref o) {
            return 0;
        }
    }

    public class TestEdgeRef implements EdgeRef {

        private final String m_id;
        private final String m_label;

        public TestEdgeRef(String id, String label) {
            m_id = id;
            m_label = label;
        }

        @Override
        public String getId() {
            return m_id;
        }

        @Override
        public String getNamespace() {
            return "nodes";
        }

        @Override
        public String getLabel() {
            return m_label;
        }

        @Override
        public int compareTo(Ref o) {
            return 0;
        }
    }

    private NodeDao m_nodeDao;
    private MeasurementsService m_measurementsService;
    private GenericInfoPanelItemProvider m_genericInfoPanelItemProvider;
    private OnmsNode m_onmsNode;

    private static final int NODE_ID = 10;

    private static final String VERTEX_LABEL = "vertexLabel";
    private static final String VERTEX_ID = "vertexId";

    private static final String EDGE_LABEL = "edgeLabel";
    private static final String EDGE_ID = "edgeId";

    @Before
    public void setUp() throws IllegalAccessException, InstantiationException {
        m_nodeDao = EasyMock.createMock(NodeDao.class);
        m_measurementsService = EasyMock.createMock(MeasurementsService.class);
        m_genericInfoPanelItemProvider = new GenericInfoPanelItemProvider(m_nodeDao, m_measurementsService);
        m_onmsNode = new OnmsNode();
        m_onmsNode.setLabel("nodeLabel");
        m_onmsNode.setId(10);
    }

    @Test
    public void testEdgeContext() throws IllegalAccessException, InstantiationException {
        TestEdgeRef testEdgeRef = new TestEdgeRef(EDGE_ID, EDGE_LABEL);
        Map<String, Object> context = m_genericInfoPanelItemProvider.createEdgeContext(testEdgeRef);
        Assert.assertTrue(context.containsKey("edge"));
        Assert.assertTrue(context.get("edge").equals(testEdgeRef));
    }

    @Test
    public void testVertexContextWithNode() throws IllegalAccessException, InstantiationException {
        EasyMock.expect(m_nodeDao.get(NODE_ID)).andReturn(m_onmsNode).anyTimes();
        EasyMock.replay(m_nodeDao);
        TestVertexRef testVertexRef = new TestVertexRef(VERTEX_ID, VERTEX_LABEL, NODE_ID);
        Map<String, Object> context = m_genericInfoPanelItemProvider.createVertexContext(testVertexRef);
        Assert.assertTrue(context.containsKey("vertex"));
        Assert.assertTrue(context.get("vertex").equals(testVertexRef));
        Assert.assertTrue(context.containsKey("node"));
        Assert.assertTrue(context.get("node").equals(m_onmsNode));
        Assert.assertTrue(((OnmsNode) context.get("node")).getId() == NODE_ID);
        EasyMock.verify(m_nodeDao);
    }

    @Test
    public void testVertexContextWithoutNode() throws IllegalAccessException, InstantiationException {
        TestVertexRef testVertexRef = new TestVertexRef(VERTEX_ID, VERTEX_LABEL, null);
        Map<String, Object> context = m_genericInfoPanelItemProvider.createVertexContext(testVertexRef);
        Assert.assertTrue(context.containsKey("vertex"));
        Assert.assertTrue(context.get("vertex").equals(testVertexRef));
        Assert.assertFalse(context.containsKey("node"));
    }
}
