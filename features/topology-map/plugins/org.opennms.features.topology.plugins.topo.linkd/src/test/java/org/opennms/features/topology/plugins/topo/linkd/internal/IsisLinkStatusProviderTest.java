/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;

public class IsisLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private IsIsLinkDao m_isIsLinkDao;
    private IsIsLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;

    @Before
    public void setUp() {
        m_node1 = new OnmsNode();
        m_node1.setId(1);

        IsIsElement element1 = new IsIsElement();
        element1.setId(1);
        element1.setNode(m_node1);
        element1.setIsisSysID("000110088500");

        m_node1.setIsisElement(element1);

        m_node2 = new OnmsNode();
        m_node2.setId(2);

        IsIsElement element2 = new IsIsElement();
        element2.setId(2);
        element2.setNode(m_node2);
        element2.setIsisSysID("000110255062");

        m_node2.setIsisElement(element2);


        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_isIsLinkDao = EasyMock.createMock(IsIsLinkDao.class);

        m_statusProvider = new IsIsLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setIsisLinkDao(m_isIsLinkDao);

        m_edgeProvider = EasyMock.createMock(EdgeProvider.class);

    }

    @Test
    public void testGetIsisLinkStatus() {

        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createAlarm());
        EasyMock.expect(m_isIsLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createIsIsLinks());

        EasyMock.replay(m_alarmDao, m_isIsLinkDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("up", status.computeStatus());

    }

    @Test
    public void testGetIsisLinkStatusDown(){
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createDownAlarm());
        EasyMock.expect(m_isIsLinkDao.findMatching(EasyMock.<org.opennms.core.criteria.Criteria>anyObject())).andReturn(createIsIsLinks());

        EasyMock.replay(m_alarmDao, m_isIsLinkDao);

        List<EdgeRef> edges = createEdges();
        Map<EdgeRef, Status> statusMap = m_statusProvider.getStatusForEdges(m_edgeProvider, edges, new Criteria[0]);

        assertEquals(1, statusMap.size());
        assertEquals(edges.get(0), new ArrayList<EdgeRef>(statusMap.keySet()).get(0));
        Status status = statusMap.get(edges.get(0));
        assertEquals("down", status.computeStatus());
    }

    private List<EdgeRef> createEdges() {

        Vertex sourceVertex = new AbstractVertex("nodes", "1", "source");
        Vertex targetVertex = new AbstractVertex("nodes", "2", "target");
        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.ISIS_EDGE_NAMESPACE, "1|2", sourceVertex, targetVertex);
        return Arrays.asList(edge);
    }


    private List<IsIsLink> createIsIsLinks() {
        List<IsIsLink> links = new ArrayList<IsIsLink>();

        IsIsLink link = createIsIsLink(m_node1, 599, 599, 1, 1, "001f12accbf1", "000110255062");
        link.setId(1);
        links.add(link);

        IsIsLink link2 = createIsIsLink(m_node2, 578, 578, 1, 1, "0021590e47c1", "000110088500");
        link2.setId(2);
        links.add(link2);

        return links;
    }

    private List<OnmsAlarm> createAlarm() {
        return Collections.emptyList();
    }

    private List<OnmsAlarm> createDownAlarm(){
        List<OnmsAlarm> alarms = new ArrayList<OnmsAlarm>();

        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(m_node1);
        alarm1.setIfIndex(599);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

    private IsIsLink createIsIsLink(OnmsNode node, int isisCircIndex, int isisCircIfindex, int isisAdjIndex, int isisCircAdmin, String isisIsAdjNeighsnpaadress, String isisisAdjNeighSysid) {
        final IsIsLink isisLink = new IsIsLink();
        isisLink.setNode(node);
        isisLink.setIsisCircIndex(isisCircIndex);
        isisLink.setIsisCircIfIndex(isisCircIfindex);
        isisLink.setIsisISAdjIndex(isisAdjIndex);
        isisLink.setIsisCircAdminState(IsIsElement.IsisAdminState.get(isisCircAdmin));
        isisLink.setIsisISAdjNeighSNPAAddress(isisIsAdjNeighsnpaadress);
        isisLink.setIsisISAdjNeighSysID(isisisAdjNeighSysid);
        return isisLink;

    }
}
