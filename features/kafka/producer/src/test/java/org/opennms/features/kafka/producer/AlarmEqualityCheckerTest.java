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
