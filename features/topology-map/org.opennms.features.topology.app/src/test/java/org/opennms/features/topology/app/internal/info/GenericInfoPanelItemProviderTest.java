/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal.info;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;

public class GenericInfoPanelItemProviderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
    public void setUp() throws IOException {
        System.setProperty("opennms.home", tempFolder.newFolder().getAbsolutePath());

        m_nodeDao = mock(NodeDao.class);
        m_measurementsService = mock(MeasurementsService.class);
        m_genericInfoPanelItemProvider = new GenericInfoPanelItemProvider(m_nodeDao, m_measurementsService);
        m_onmsNode = new OnmsNode();
        m_onmsNode.setLabel("nodeLabel");
        m_onmsNode.setId(10);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_nodeDao);
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
        when(m_nodeDao.get(NODE_ID)).thenReturn(m_onmsNode);
        TestVertexRef testVertexRef = new TestVertexRef(VERTEX_ID, VERTEX_LABEL, NODE_ID);
        Map<String, Object> context = m_genericInfoPanelItemProvider.createVertexContext(testVertexRef);
        Assert.assertTrue(context.containsKey("vertex"));
        Assert.assertTrue(context.get("vertex").equals(testVertexRef));
        Assert.assertTrue(context.containsKey("node"));
        Assert.assertTrue(context.get("node").equals(m_onmsNode));
        Assert.assertTrue(((OnmsNode) context.get("node")).getId() == NODE_ID);

        verify(m_nodeDao, atLeastOnce()).get(anyInt());
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
