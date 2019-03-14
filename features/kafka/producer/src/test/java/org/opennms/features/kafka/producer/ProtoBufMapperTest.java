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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Tests for {@link ProtobufMapper}.
 */
public class ProtoBufMapperTest {

    private ProtobufMapper protobufMapper = new ProtobufMapper(mock(EventConfDao.class), mock(HwEntityDao.class),
            mock(TransactionOperations.class), mock(NodeDao.class), 1);

    /**
     * Tests that the mapper can handle related alarms.
     */
    @Test
    public void testRelatedalarms() {
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
    public void canMapTopologyEdges() {
        // Build an edge
        OnmsTopologyVertex v1 = OnmsTopologyVertex.create("v1", "label1", "address1", "icon1");
        OnmsTopologyVertex v2 = OnmsTopologyVertex.create("v2", "label2", "address2", "icon2");
        OnmsTopologyPort sourcePort = OnmsTopologyPort.create("source", v1, 1);
        sourcePort.setAddr("source port addr");
        OnmsTopologyPort targetPort = OnmsTopologyPort.create("target", v2, 2);
        targetPort.setAddr("target port addr");
        OnmsTopologyEdge edge = OnmsTopologyEdge.create("edge-id", sourcePort, targetPort);

        // Map
        OpennmsModelProtos.TopologyEdge.Builder mappedEdge = protobufMapper.toEdgeTopologyMessage(ProtocolSupported.USERDEFINED.toString(), edge);

        // Verify
        assertThat(mappedEdge.getRef().getProtocol(), equalTo(OpennmsModelProtos.TopologyRef.Protocol.USERDEFINED));
        assertThat(mappedEdge.getRef().getId(), equalTo("edge-id"));

        assertThat(mappedEdge.getSource().getVertexId(), equalTo("v1"));
        assertThat(mappedEdge.getSource().getAddress(), equalTo("source port addr"));

        assertThat(mappedEdge.getTargetPort().getVertexId(), equalTo("v2"));
        assertThat(mappedEdge.getTargetPort().getAddress(), equalTo("target port addr"));
    }
}
