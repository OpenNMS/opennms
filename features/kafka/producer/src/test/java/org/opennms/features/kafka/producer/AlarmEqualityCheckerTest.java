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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

/**
 * Tests for {@link AlarmEqualityChecker}.
 */
public class AlarmEqualityCheckerTest {
    /**
     * Tests that two alarms match except for their excluded fields.
     */
    @Test
    public void testMatchingAfterDefaultExclusions() {
        AlarmEqualityChecker alarmEqualityChecker =
                AlarmEqualityChecker.with(AlarmEqualityChecker.Exclusions::defaultExclusions);

        // These alarms (and their related alarms) will have different counts and last events but will otherwise be the
        // same so they should match after exclusions are applied
        OpennmsModelProtos.Alarm.Builder alarmA = OpennmsModelProtos.Alarm.newBuilder()
                .setLastEvent(OpennmsModelProtos.Event.newBuilder()
                        .setLogMessage("test.a"))
                .setCount(1)
                .setLastEventTime(1)
                .addRelatedAlarm(OpennmsModelProtos.Alarm.newBuilder()
                        .setLastEvent(OpennmsModelProtos.Event.newBuilder()
                                .setLogMessage("test.a"))
                        .setLastEventTime(1)
                        .setCount(1));
        OpennmsModelProtos.Alarm.Builder alarmB = OpennmsModelProtos.Alarm.newBuilder()
                .setLastEvent(OpennmsModelProtos.Event.newBuilder()
                        .setLogMessage("test.b"))
                .setCount(2)
                .setLastEventTime(2)
                .addRelatedAlarm(OpennmsModelProtos.Alarm.newBuilder()
                        .setLastEvent(OpennmsModelProtos.Event.newBuilder()
                                .setLogMessage("test.b"))
                        .setLastEventTime(2)
                        .setCount(2));

        assertThat(alarmEqualityChecker.equalsExcludingOnBoth(alarmA, alarmB), is(equalTo(true)));
    }
}
