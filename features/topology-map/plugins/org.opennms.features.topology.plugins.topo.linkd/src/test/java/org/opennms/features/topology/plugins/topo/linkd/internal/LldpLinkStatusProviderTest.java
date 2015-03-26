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
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

public class LldpLinkStatusProviderTest {

    private AlarmDao m_alarmDao;
    private LldpLinkDao m_lldpLinkDao;
    private LldpLinkStatusProvider m_statusProvider;
    private EdgeProvider m_edgeProvider;
    private OnmsNode m_node1;
    private OnmsNode m_node2;

    @Before
    public void setUp() {
        m_node1 = new OnmsNode();
        m_node1.setId(1);
        m_node1.setLldpElement(new LldpElement(m_node1, "node1ChassisId", "node1SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

        m_node2 = new OnmsNode();
        m_node2.setId(2);
        m_node2.setLldpElement(new LldpElement(m_node2, "node2ChassisId", "node2SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL));

        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_lldpLinkDao = EasyMock.createMock(LldpLinkDao.class);

        m_statusProvider = new LldpLinkStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);
        m_statusProvider.setLldpLinkDao(m_lldpLinkDao);

        m_edgeProvider = EasyMock.createMock(EdgeProvider.class);

    }

    @Test
    public void testGetLldpLinkStatus() {
        List<Integer> linkIds = new ArrayList<Integer>();
        linkIds.add(1);
        linkIds.add(2);
        EasyMock.expect(
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createAlarm());
        EasyMock.expect(m_lldpLinkDao.findLinksForIds(linkIds)).andReturn(createLldpLinks());

        EasyMock.replay(m_alarmDao, m_lldpLinkDao);

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
                m_alarmDao.findMatching(EasyMock.anyObject(org.opennms.core.criteria.Criteria.class))).andReturn(createDownAlarm());
        EasyMock.expect(m_lldpLinkDao.findLinksForIds(linkIds)).andReturn(createLldpLinks());

        EasyMock.replay(m_alarmDao, m_lldpLinkDao);

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
        EdgeRef edge = new AbstractEdge(EnhancedLinkdTopologyProvider.LLDP_EDGE_NAMESPACE, "1|2", sourceVertex, targetVertex);
        return Arrays.asList(edge);
    }


    private List<LldpLink> createLldpLinks() {
        List<LldpLink> links = new ArrayList<LldpLink>();

        LldpLink link = new LldpLink(m_node1, 12, 1, "node1PortId", "node1PortDescr", LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,
                "node2ChassisId", "node2SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL, "node2PortId",
                LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, "node2PortDescr");
        link.setId(1);
        links.add(link);

        LldpLink link2 = new LldpLink(m_node2, 21, 2, "node2PortId", "node2PortDescr", LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,
                "node1ChassisId", "node1SysName", LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL, "node1PortId",
                LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, "node1PortDescr");
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
        alarm1.setIfIndex(1);
        alarm1.setUei(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI);
        alarms.add(alarm1);

        return alarms;
    }

}
