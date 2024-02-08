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
package org.opennms.netmgt.alarmd.drools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsAlarm;

public class DroolsAlarmContextTest {

    @Test
    public void canDetermineWhenAlarmShouldBeUpdatedForSnapshot() {
        // Mock alarms to be able to test all the code branches
        OnmsAlarm a1 = mock(OnmsAlarm.class);
        when(a1.getLastEventTime()).thenReturn(new Date(0));
        when(a1.getAckTime()).thenReturn(null);
        OnmsAlarm a2 = mock(OnmsAlarm.class);
        when(a2.getLastEventTime()).thenReturn(new Date(1));
        when(a1.getAckTime()).thenReturn(null);
        OnmsAlarm a3 = mock(OnmsAlarm.class);
        when(a3.getLastEventTime()).thenReturn(new Date(0));
        when(a3.getAckTime()).thenReturn(new Date(0));

        // Should update
        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a1, a2), equalTo(true));
        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a2, a1), equalTo(true));

        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a1, a3), equalTo(true));
        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a3, a1), equalTo(true));

        // Should not update
        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a1, a1), equalTo(false));
        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a2, a2), equalTo(false));
        assertThat(DroolsAlarmContext.shouldUpdateAlarmForSnapshot(a3, a3), equalTo(false));
    }
}
