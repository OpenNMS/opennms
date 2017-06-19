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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Lists;

public class AlarmStatusProviderTest {

    private AlarmDao m_alarmDao;
    private AlarmStatusProvider m_statusProvider;
    private VertexProvider m_vertexProvider;
    
    @Before
    public void setUp() {
        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_statusProvider = new AlarmStatusProvider(m_alarmDao);

        m_vertexProvider = EasyMock.createMock(VertexProvider.class);
        EasyMock.expect(m_vertexProvider.getChildren(EasyMock.<VertexRef>anyObject())).andReturn(new ArrayList<>());
        EasyMock.replay(m_vertexProvider);
    }
    
    
    @Test
    public void testGetAlarmStatus() {
        Vertex vertex = createVertex(1);
        Vertex vertex2 = createVertex(2);
        Vertex vertex3 = createVertex(3);
        List<VertexRef> vertexList = Lists.newArrayList(vertex, vertex2, vertex3);

        EasyMock.expect(
                m_alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(EasyMock.anyObject())).andReturn(createNormalAlarmSummaryList());
        
        EasyMock.replay(m_alarmDao);
        
        Map<VertexRef, Status> statusMap = m_statusProvider.getStatusForVertices(m_vertexProvider, vertexList, new Criteria[0]);
        assertEquals(3, statusMap.size());
        assertEquals(vertex, statusMap.keySet().stream().sorted((v1, v2) -> v1.getId().compareTo(v2.getId())).collect(Collectors.toList()).get(0));
        assertEquals("major", statusMap.get(vertex).computeStatus()); // use defined status
        assertEquals("normal", statusMap.get(vertex2).computeStatus()); // fallback to normal
        assertEquals("indeterminate", statusMap.get(vertex3).computeStatus()); // use defined status
        
        EasyMock.verify(m_alarmDao);
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
