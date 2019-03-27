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

package org.opennms.netmgt.alarmd.examples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

/**
 * Verify the example rules provided in $OPENNMS_HOME/etc/examples/alarmd/drools.rules.d/misc.drl.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml"
})
@JUnitConfigurationEnvironment
public class MiscExamplesIT extends DroolsExampleIT {

    @Override
    public String getRulesFile() {
        return "misc.drl";
    }

    @Test
    public void canEscalateAlarmsForNodesInCategory() {
        // Create an alarm that we know will trigger our escalate rule
        OnmsAlarm trigger = createNodeDownAlarm(1, "EMERGENCY_F0");
        OnmsSeverity originalSeverity = trigger.getSeverity();

        // Tick
        dac.getClock().advanceTime( 16, TimeUnit.SECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        // t = 00:16.0000
        dac.tick();

        // Capture the updated alarm
        ArgumentCaptor<OnmsAlarm> alarmCaptor = ArgumentCaptor.forClass(OnmsAlarm.class);
        verify(alarmDao).update(alarmCaptor.capture());
        OnmsAlarm alarm = alarmCaptor.getValue();

        // Verify that the severity was updated
        OnmsSeverity updatedSeverity = alarm.getSeverity();
        assertThat(updatedSeverity, greaterThan(originalSeverity));

        // Create an alarm that should not trigger our escalate rule
        reset(alarmDao);
        trigger = createNodeDownAlarm(2, "NOT_EMERGENCY_F0");

        // Tick
        dac.getClock().advanceTime( 1, TimeUnit.SECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        // t = 00:17.0000
        dac.tick();

        // No updates
        verify(alarmDao, times(0)).update(any(OnmsAlarm.class));
    }

    private OnmsAlarm createNodeDownAlarm(int id, String nodeCategory) {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.MAJOR);
        alarm.setReductionKey(EventConstants.NODE_DOWN_EVENT_UEI + id);
        alarm.setFirstEventTime(new Date(15 * 1000));

        final OnmsNode node = new OnmsNode();
        node.addCategory(new OnmsCategory(nodeCategory, ""));
        alarm.setNode(node);

        final OnmsEvent event = new OnmsEvent();
        event.setEventUei(EventConstants.NODE_DOWN_EVENT_UEI);
        event.setEventTime(new Date(16 * 1000));
        alarm.setLastEvent(event);

        when(alarmDao.get(alarm.getId())).thenReturn(alarm);
        return alarm;
    }
}
