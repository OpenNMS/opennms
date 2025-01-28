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
package org.opennms.features.kafka.producer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.events.api.EventConstants.TROUBLETICKET_CREATE_UEI;
import static org.opennms.topologies.service.api.EdgeMockUtil.PROTOCOL;
import static org.opennms.topologies.service.api.EdgeMockUtil.createEdge;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.hibernate.ObjectNotFoundException;
import org.junit.Test;
import org.opennms.core.utils.LocationUtils;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.topologies.service.api.EdgeMockUtil;

/**
 * Tests for {@link ProtobufMapper}.
 */
public class ProtoBufMapperTest {

    private final ProtobufMapper protobufMapper = new ProtobufMapper(mock(EventConfDao.class), mock(HwEntityDao.class),
            mock(SessionUtils.class), mock(NodeDao.class),  1);

    private final String FOREIGN_ID = "foreignId";
    private final String FOREIGN_SOURCE = "foreignSource";
    private final String NODE_LABEL = "test";

    /**
     * Tests that the mapper can handle related alarms.
     */
    @Test
    public void testRelatedAlarms() {
        OnmsAlarm parentAlarm = generateTestAlarm();
        parentAlarm.setId(0);
        parentAlarm.setUei("parent");

        OnmsAlarm childAlarm = generateTestAlarm();
        int childId = 1;
        childAlarm.setId(childId);
        String childUei = "child";
        childAlarm.setUei(childUei);
        String reductionKey = "test key";
        childAlarm.setReductionKey(reductionKey);
        String childLogMsg = "test msg";
        childAlarm.setLogMsg(childLogMsg);

        parentAlarm.setRelatedAlarms(new HashSet<>(Collections.singletonList(childAlarm)));

        OpennmsModelProtos.Alarm.Builder mappedAlarm = protobufMapper.toAlarm(parentAlarm);
        List<OpennmsModelProtos.Alarm> relatedAlarms = mappedAlarm.getRelatedAlarmList();
        
        assertEquals(1, relatedAlarms.size());
        OpennmsModelProtos.Alarm childProtoAlarm = relatedAlarms.get(0);
        assertEquals(childUei, childProtoAlarm.getUei());
        assertEquals(childId, childProtoAlarm.getId());
        assertEquals(reductionKey, childProtoAlarm.getReductionKey());
        assertEquals(childLogMsg, childProtoAlarm.getLogMessage());
        assertEquals(FOREIGN_ID, mappedAlarm.getNodeCriteria().getForeignId());
        assertEquals(FOREIGN_SOURCE, mappedAlarm.getNodeCriteria().getForeignSource());
        assertEquals(NODE_LABEL, mappedAlarm.getNodeCriteria().getNodeLabel());
        assertEquals(LocationUtils.DEFAULT_LOCATION_NAME, mappedAlarm.getNodeCriteria().getLocation());
    }

    private OnmsAlarm generateTestAlarm() {
        OnmsAlarm testAlarm = new OnmsAlarm();
        testAlarm.setCounter(1);
        testAlarm.setSeverity(OnmsSeverity.MAJOR);
        testAlarm.setAlarmType(1);

        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel(NODE_LABEL);
        node.setForeignId(FOREIGN_ID);
        node.setForeignSource(FOREIGN_SOURCE);
        node.setLocation(new OnmsMonitoringLocation(LocationUtils.DEFAULT_LOCATION_NAME, LocationUtils.DEFAULT_LOCATION_NAME));
        testAlarm.setNode(node);
        return testAlarm;
    }

    @Test
    public void testAlarmWithTroubleTicket() {

        OnmsAlarm testAlarm = generateTestAlarm();
        testAlarm.setId(1);
        testAlarm.setUei(TROUBLETICKET_CREATE_UEI);
        testAlarm.setTTicketId("NMS-12725");
        testAlarm.setTTicketState(TroubleTicketState.CREATE_PENDING);

        OpennmsModelProtos.Alarm.Builder mappedAlarm = protobufMapper.toAlarm(testAlarm);
        assertNotNull(mappedAlarm);
        assertEquals(mappedAlarm.getTroubleTicketId(), testAlarm.getTTicketId());
        assertEquals(mappedAlarm.getTroubleTicketState().getNumber(), testAlarm.getTTicketState().getValue());

    }

    @Test
    public void testEventMappingWithSnmp() {
        Event event = createEvent();
        protobufMapper.getNodeIdToCriteriaCache().put(1L, OpennmsModelProtos.NodeCriteria.newBuilder()
                .setId(1).build());
        OpennmsModelProtos.Event mappedEvent = protobufMapper.toEvent(event).build();
        assertNotNull(mappedEvent.getSnmpInfo());
        assertEquals("OpenNMS", mappedEvent.getSnmpInfo().getCommunity());
        assertEquals(23, mappedEvent.getSnmpInfo().getSpecific());
    }

    private Event createEvent() {
        Event event = new Event();
        event.setUuid(UUID.randomUUID().toString());
        event.setUei("newSuspectEvent");
        event.setDbid(254L);
        event.setNodeid(1L);
        event.setSource("kafka-producer-test");
        event.setDistPoller("systemId1");
        event.setCreationTime(new Date());
        Snmp snmp = new Snmp();
        snmp.setId("id1");
        snmp.setVersion("v3");
        snmp.setSpecific(23);
        snmp.setGeneric(45);
        snmp.setCommunity("OpenNMS");
        event.setSnmp(snmp);
        return event;
    }

    @Test
    public void canCatchObjectNotFoundExceptionsWhenMappingEvents() {
        OnmsEvent e = mock(OnmsEvent.class);
        when(e.getId()).thenThrow(new ObjectNotFoundException("oops", OnmsEvent.class.getCanonicalName()));
        // Expect a null value back when mapping
        assertThat(protobufMapper.toEvent(e), nullValue());
    }

    @Test
    public void canMapNodeToNodeEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        EdgeMockUtil.addNode(true, mockEdge);
        EdgeMockUtil.addNode(false, mockEdge);
        OpennmsModelProtos.TopologyEdge mappedEdge =
                protobufMapper.toEdgeTopologyMessage(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getRef().getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getRef().getProtocol().name(), equalTo(PROTOCOL));

        assertThat(mappedEdge.getSourceCase(), equalTo(OpennmsModelProtos.TopologyEdge.SourceCase.SOURCENODE));
        assertThat(mappedEdge.getTargetCase(), equalTo(OpennmsModelProtos.TopologyEdge.TargetCase.TARGETNODE));

        assertThat(mappedEdge.getSourceNode().getId(), equalTo((long) EdgeMockUtil.SOURCE_NODE_ID));
        assertThat(mappedEdge.getTargetNode().getId(), equalTo((long) EdgeMockUtil.TARGET_NODE_ID));
    }

    @Test
    public void canMapPortToPortEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        EdgeMockUtil.addPort(true, mockEdge);
        EdgeMockUtil.addPort(false, mockEdge);
        OpennmsModelProtos.TopologyEdge mappedEdge =
                protobufMapper.toEdgeTopologyMessage(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getRef().getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getRef().getProtocol().name(), equalTo(PROTOCOL));

        assertThat(mappedEdge.getSourceCase(), equalTo(OpennmsModelProtos.TopologyEdge.SourceCase.SOURCEPORT));
        assertThat(mappedEdge.getTargetCase(), equalTo(OpennmsModelProtos.TopologyEdge.TargetCase.TARGETPORT));

        assertThat(mappedEdge.getSourcePort().getIfIndex(), equalTo((long) EdgeMockUtil.SOURCE_IFINDEX));
        assertThat(mappedEdge.getSourcePort().getVertexId(), equalTo(EdgeMockUtil.SOURCE_VERTEX_ID));
        assertThat(mappedEdge.getSourcePort().getNodeCriteria().getId(), equalTo((long) EdgeMockUtil.SOURCE_NODE_ID));

        assertThat(mappedEdge.getTargetPort().getIfIndex(), equalTo((long) EdgeMockUtil.TARGET_IFINDEX));
        assertThat(mappedEdge.getTargetPort().getVertexId(), equalTo(EdgeMockUtil.TARGET_VERTEX_ID));
        assertThat(mappedEdge.getTargetPort().getNodeCriteria().getId(), equalTo((long) EdgeMockUtil.TARGET_NODE_ID));
    }

    @Test
    public void canMapSegmentToSegmentEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        EdgeMockUtil.addSegment(true, mockEdge);
        EdgeMockUtil.addSegment(false, mockEdge);
        OpennmsModelProtos.TopologyEdge mappedEdge =
                protobufMapper.toEdgeTopologyMessage(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getRef().getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getRef().getProtocol().name(), equalTo(PROTOCOL));

        assertThat(mappedEdge.getSourceCase(), equalTo(OpennmsModelProtos.TopologyEdge.SourceCase.SOURCESEGMENT));
        assertThat(mappedEdge.getTargetCase(), equalTo(OpennmsModelProtos.TopologyEdge.TargetCase.TARGETSEGMENT));

        assertThat(mappedEdge.getSourceSegment().getRef().getId(), equalTo(EdgeMockUtil.SOURCE_ID));
        assertThat(mappedEdge.getSourceSegment().getRef().getProtocol().name(), equalTo(EdgeMockUtil.PROTOCOL));

        assertThat(mappedEdge.getTargetSegment().getRef().getId(), equalTo(EdgeMockUtil.TARGET_ID));
        assertThat(mappedEdge.getTargetSegment().getRef().getProtocol().name(), equalTo(EdgeMockUtil.PROTOCOL));
    }

    @Test
    public void canMapPortToSegmentEdge() {
        OnmsTopologyEdge mockEdge = createEdge();
        EdgeMockUtil.addPort(true, mockEdge);
        EdgeMockUtil.addSegment(false, mockEdge);
        OpennmsModelProtos.TopologyEdge mappedEdge =
                protobufMapper.toEdgeTopologyMessage(OnmsTopologyProtocol.create(PROTOCOL), mockEdge);

        assertThat(mappedEdge.getRef().getId(), equalTo(EdgeMockUtil.EDGE_ID));
        assertThat(mappedEdge.getRef().getProtocol().name(), equalTo(PROTOCOL));

        assertThat(mappedEdge.getSourceCase(), equalTo(OpennmsModelProtos.TopologyEdge.SourceCase.SOURCEPORT));
        assertThat(mappedEdge.getTargetCase(), equalTo(OpennmsModelProtos.TopologyEdge.TargetCase.TARGETSEGMENT));

        assertThat(mappedEdge.getSourcePort().getIfIndex(), equalTo((long) EdgeMockUtil.SOURCE_IFINDEX));
        assertThat(mappedEdge.getSourcePort().getVertexId(), equalTo(EdgeMockUtil.SOURCE_VERTEX_ID));
        assertThat(mappedEdge.getSourcePort().getNodeCriteria().getId(), equalTo((long) EdgeMockUtil.SOURCE_NODE_ID));

        assertThat(mappedEdge.getTargetSegment().getRef().getId(), equalTo(EdgeMockUtil.TARGET_ID));
        assertThat(mappedEdge.getTargetSegment().getRef().getProtocol().name(), equalTo(EdgeMockUtil.PROTOCOL));
    }

}
