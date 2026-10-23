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
package org.opennms.features.grpc.exporter.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.plugin.grpc.proto.spog.Alarm;
import org.opennms.plugin.grpc.proto.spog.AlarmUpdateList;

public class AlarmMapperTest {

    private static OnmsAlarm alarm(Integer id, Integer alarmType, Integer ifIndex) {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setUei("uei.opennms.org/test");
        alarm.setAlarmType(alarmType);
        alarm.setIfIndex(ifIndex);
        alarm.setCounter(id);
        alarm.setSeverity(OnmsSeverity.MAJOR);
        return alarm;
    }

    @Test
    public void shouldMapIfIndexAsIs() {
        final Alarm mapped = AlarmMapper.INSTANCE.toAlarmUpdate(alarm(6, OnmsAlarm.PROBLEM_TYPE, 6));

        assertEquals(6, mapped.getIfIndex());
        assertEquals(6, mapped.getId());
        assertEquals(6, mapped.getCount());
    }

    @Test
    public void shouldMapNullIfIndexToDefault() {
        final Alarm mapped = AlarmMapper.INSTANCE.toAlarmUpdate(alarm(1, OnmsAlarm.PROBLEM_TYPE, null));

        assertEquals(0, mapped.getIfIndex());
    }

    @Test
    public void shouldMapAlarmType() {
        final Object[][] cases = {
                // alarm type, expected type
                {OnmsAlarm.PROBLEM_TYPE, Alarm.Type.PROBLEM_WITH_CLEAR},
                {OnmsAlarm.RESOLUTION_TYPE, Alarm.Type.CLEAR},
                {OnmsAlarm.PROBLEM_WITHOUT_RESOLUTION_TYPE, Alarm.Type.PROBLEM_WITHOUT_CLEAR},
                {null, Alarm.Type.PROBLEM_WITHOUT_CLEAR},
                {99, Alarm.Type.PROBLEM_WITHOUT_CLEAR},
        };
        for (final Object[] c : cases) {
            final Alarm mapped = AlarmMapper.INSTANCE.toAlarmUpdate(alarm(1, (Integer) c[0], null));
            assertEquals("alarm type " + c[0], ((Alarm.Type) c[1]).getNumber(), mapped.getType());
        }
    }

    @Test
    public void shouldMapSnapshotContainingInterfaceAlarm() {
        final List<OnmsAlarm> alarms = Arrays.asList(
                alarm(1, OnmsAlarm.PROBLEM_TYPE, null),
                alarm(2, OnmsAlarm.PROBLEM_TYPE, 32));

        final AlarmUpdateList mapped = AlarmMapper.INSTANCE.toAlarmUpdatesList(alarms, null, "test", true);

        assertTrue(mapped.getSnapshot());
        assertEquals(2, mapped.getAlarmsCount());
        assertEquals(32, mapped.getAlarms(1).getIfIndex());
    }
}
