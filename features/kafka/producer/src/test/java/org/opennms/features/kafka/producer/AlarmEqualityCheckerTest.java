/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Test;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.transaction.support.TransactionOperations;

public class AlarmEqualityCheckerTest {
    private final ProtobufMapper protobufMapper = new ProtobufMapper(mock(EventConfDao.class),
            mock(HwEntityDao.class), mock(TransactionOperations.class), mock(NodeDao.class), 1000);

    @Test
    public void testEqualTo() {
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setId(1);
        onmsAlarm.setAlarmType(1);

        OpennmsModelProtos.Alarm protoAlarm = OpennmsModelProtos.Alarm.newBuilder()
                .setId(1)
                .setType(OpennmsModelProtos.Alarm.Type.PROBLEM_WITH_CLEAR)
                .build();

        AlarmEqualityChecker comparison = AlarmEqualityChecker.withProtoAlarm(protoAlarm, protobufMapper);
        assertThat(comparison.equalTo(onmsAlarm), equalTo(true));
    }

    @Test
    public void testHasNonIncrementalDifferences() {
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setId(1);
        onmsAlarm.setAlarmType(1);

        OpennmsModelProtos.Alarm protoAlarm = OpennmsModelProtos.Alarm.newBuilder()
                .setId(1)
                .setType(OpennmsModelProtos.Alarm.Type.PROBLEM_WITH_CLEAR)
                .build();

        AlarmEqualityChecker comparison = AlarmEqualityChecker.withProtoAlarm(protoAlarm, protobufMapper);

        // The alarms should be equal with only Ids and types set
        assertThat(comparison.equalTo(onmsAlarm), equalTo(true));

        // By setting the event time on one alarm they should no longer be equal but they shouldn't have any
        // non-incremental differences yet
        onmsAlarm.setLastEventTime(new Date(System.currentTimeMillis()));
        assertThat(comparison.equalTo(onmsAlarm), equalTo(false));
        assertThat(comparison.hasNonIncrementalDifferences(onmsAlarm), equalTo(false));

        // By setting a UEI on one alarm they will now be non-incrementally different
        onmsAlarm.setUei("testuei");
        assertThat(comparison.hasNonIncrementalDifferences(onmsAlarm), equalTo(true));
    }
}
