/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import java.util.HashSet;
import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.junit.Test;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.topologies.service.api.EdgeMockUtil;

/**
 * Tests for {@link ProtobufMapper}.
 */
public class ProtoBufMapperTest {

    private ProtobufMapper protobufMapper = new ProtobufMapper(mock(EventConfDao.class), mock(HwEntityDao.class),
            mock(SessionUtils.class), mock(NodeDao.class), 1);

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
    }

    private OnmsAlarm generateTestAlarm() {
        OnmsAlarm testAlarm = new OnmsAlarm();
        testAlarm.setCounter(1);
        testAlarm.setSeverity(OnmsSeverity.MAJOR);
        testAlarm.setAlarmType(1);

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
