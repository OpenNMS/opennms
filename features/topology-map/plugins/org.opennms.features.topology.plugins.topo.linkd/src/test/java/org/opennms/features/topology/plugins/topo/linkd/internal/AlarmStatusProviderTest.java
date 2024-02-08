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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Lists;

public class AlarmStatusProviderTest {

    private AlarmDao m_alarmDao;
    private LinkdStatusProvider m_statusProvider;
    private LinkdTopologyFactory m_topologyFactory;
    private BackendGraph m_graph;
    
    @Before
    public void setUp() {
        m_alarmDao = mock(AlarmDao.class);

        m_graph = mock(BackendGraph.class);
        m_topologyFactory = mock(LinkdTopologyFactory.class);
        m_statusProvider = new LinkdStatusProvider(m_alarmDao, m_topologyFactory);
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testGetAlarmStatus() {
        Vertex vertex = createVertex(1);
        Vertex vertex2 = createVertex(2);
        Vertex vertex3 = createVertex(3);
        List<VertexRef> vertexList = Lists.newArrayList(vertex, vertex2, vertex3);

        when(m_alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(any())).thenReturn(createNormalAlarmSummaryList());
        when(m_graph.getNamespace()).thenReturn("nodes");
        Map<VertexRef, Status> statusMap = m_statusProvider.getStatusForVertices(m_graph, vertexList, new Criteria[0]);
        assertEquals(3, statusMap.size());
        assertEquals(vertex, statusMap.keySet().stream().sorted(Comparator.comparing(Ref::getId)).collect(Collectors.toList()).get(0));
        assertEquals("major", statusMap.get(vertex).computeStatus()); // use defined status
        assertEquals("normal", statusMap.get(vertex2).computeStatus()); // fallback to normal
        assertEquals("indeterminate", statusMap.get(vertex3).computeStatus()); // use defined status
    }


    private List<AlarmSummary> createNormalAlarmSummaryList() {
        List<AlarmSummary> alarms = new ArrayList<>();
        alarms.add(new AlarmSummary(1, "node1", new Date(), OnmsSeverity.MAJOR, 1L)); // simulate 1 major alarm
        alarms.add(new AlarmSummary(3, "node3", new Date(), OnmsSeverity.INDETERMINATE, 5L)); // simulate 5 indeterminate alarms
        return alarms;
    }

    private static Vertex createVertex(int nodeId) {
        AbstractVertex v = new AbstractVertex("nodes", Integer.toString(nodeId), Integer.toString(nodeId));
        v.setNodeID(nodeId);
        return v;
    }
}
