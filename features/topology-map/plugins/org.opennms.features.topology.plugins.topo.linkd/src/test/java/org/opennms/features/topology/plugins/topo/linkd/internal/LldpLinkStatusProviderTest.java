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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class LldpLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private LldpLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;

    @Before
    public void setUp() {
        m_alarmDao = EasyMock.createMock(AlarmDao.class);

        m_statusProvider = new LldpLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);

        m_edgeProvider = EasyMock.createMock(EdgeProvider.class);

    }

    @Test
    public void testGetLldpLinkStatus() {
        List<Integer> linkIds = new ArrayList<Integer>();
        linkIds.add(1);
        linkIds.add(2);
        EasyMock.expect(
                m_alarmDao.getLldpEdgeAlarmSummaries(linkIds)).andReturn(createLldpLinkStatusSummary());

        EasyMock.replay(m_alarmDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("up", status.computeStatus());

    }

    @Test
    public void testGetLldpLinkStatusDown(){
        List<Integer> linkIds = new ArrayList<Integer>();
        linkIds.add(1);
        linkIds.add(2);
        EasyMock.expect(
                m_alarmDao.getLldpEdgeAlarmSummaries(linkIds)).andReturn(createDownLldpStatusSummary());

        EasyMock.replay(m_alarmDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("down", status.computeStatus());
    }

    private List<EdgeAlarmStatusSummary> createDownLldpStatusSummary() {
        EdgeAlarmStatusSummary summary = new EdgeAlarmStatusSummary(1,2, EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        return Arrays.asList(summary);
    }

    private List<EdgeAlarmStatusSummary> createLldpLinkStatusSummary() {
        EdgeAlarmStatusSummary summary = new EdgeAlarmStatusSummary(1,2, EventConstants.TOPOLOGY_LINK_UP_EVENT_UEI);
        return Arrays.asList(summary);
    }


    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex targetVertex = new AbstractVertex("nodes", "2", "target");
        EdgeRef edge = new AbstractEdge("nodes", "1|2", sourceVertex, targetVertex);
        return Arrays.asList(edge);
    }

}
