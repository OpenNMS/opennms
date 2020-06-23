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
