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

package org.opennms.netmgt.alarmd.examples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

/**
 * Verify that the Drools engine can be extended by with additional rules
 * to generate new "nag" events from alarms on an interval.
 *
 * A copy of these rules is maintained in $OPENNMS_HOME/etc/examples/alarmd/drools.rules.d/nag.drl.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml"
})
@JUnitConfigurationEnvironment
public class NaggingNotificationsIT extends DroolsExampleIT {

    @Override
    public String getRulesFile() {
        return "nag.drl";
    }

    @Test
    public void canTriggerNag() {
        OnmsAlarm trigger = createNagAlarmTrigger(1, "uei.opennms.org/nag/notification");
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 16, TimeUnit.SECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        // t = 00:16.0000
        dac.tick();

        // Advance the clock only 1 ms and verify that no nagging events have been generated yet
        dac.getClock().advanceTime( 1, TimeUnit.MILLISECONDS );
        // t = 00:16.0001
        dac.tick();
        verify(eventForwarder, times(0)).sendNow(any(Event.class));

        // Advance the clock sufficiently for a first nag event to generate
        dac.getClock().advanceTime( 45, TimeUnit.SECONDS );
        // t = 01:01.0001
        dac.tick();

        // Verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder).sendNow(eventCaptor.capture());
        Event event = eventCaptor.getValue();
        assertThat(event.getTime().getTime(), equalTo(61001L));
        assertThat(event.getUei(), equalTo("uei.opennms.org/nag/notification"));
        reset(eventForwarder);

        // Advance the clock sufficiently for a second nag event to generate
        dac.getClock().advanceTime( 30, TimeUnit.SECONDS );
        // t = 01:31.0001
        dac.tick();

        // Verify
        verify(eventForwarder).sendNow(eventCaptor.capture());
        event = eventCaptor.getValue();
        assertThat(event.getTime().getTime(), equalTo(91001L));
        assertThat(event.getUei(), equalTo("uei.opennms.org/nag/notification"));
        reset(eventForwarder);

        // Advance the clock only 1 ms and verify that no nagging events have been generated yet
        dac.getClock().advanceTime( 1, TimeUnit.MILLISECONDS );
        // t = 01:31.0002
        dac.tick();
        verify(eventForwarder, times(0)).sendNow(any(Event.class));

        // Delete the alarm
        dac.handleDeletedAlarm(trigger.getId(), trigger.getReductionKey());

        // Tick tock far in the future
        dac.getClock().advanceTime( 1, TimeUnit.SECONDS );
        dac.tick();
        dac.getClock().advanceTime( 1, TimeUnit.DAYS );
        dac.tick();

        // No mo' calls
        verify(eventForwarder, times(0)).sendNow(any(Event.class));
    }

    /**
     * Verifies that we can generate different "nag" events for different alarms.
     */
    @Test
    public void canTriggerDifferentNagEventsForDifferentAlarms() {
        OnmsAlarm a1 = createNagAlarmTrigger(1, "uei.opennms.org/nag/notification");
        dac.handleNewOrUpdatedAlarm(a1);
        OnmsAlarm a2 = createNagAlarmTrigger(2, "uei.opennms.org/nag/other/notification");
        dac.handleNewOrUpdatedAlarm(a2);

        dac.getClock().advanceTime( 16, TimeUnit.SECONDS );
        // t = 00:16.0000
        dac.tick();

        // No calls yet
        verify(eventForwarder, times(0)).sendNow(any(Event.class));

        // Advance the clock sufficiently for a first nag event to generate
        dac.getClock().advanceTime( 45, TimeUnit.SECONDS );
        // t = 01:01.0001
        dac.tick();

        // Verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder, times(2)).sendNow(eventCaptor.capture());
        List<Event> events = eventCaptor.getAllValues();
        // We should have 2 nag events
        assertThat(events, hasSize(2));

        // Validate the nag for a1
        Event nagForA1 = getNagEventFor(events, a1);
        assertThat(nagForA1.getTime().getTime(), equalTo(61000L));
        assertThat(nagForA1.getUei(), equalTo("uei.opennms.org/nag/notification"));
        reset(eventForwarder);

        // Validate the nag for a2
        Event nagForA2 = getNagEventFor(events, a2);
        assertThat(nagForA2.getTime().getTime(), equalTo(61000L));
        assertThat(nagForA2.getUei(), equalTo("uei.opennms.org/nag/other/notification"));
        reset(eventForwarder);
    }

    private OnmsAlarm createNagAlarmTrigger(int id, String nagUei) {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.WARNING);
        alarm.setReductionKey("uei.opennms.org/nag/alarm:" + id);
        alarm.setFirstEventTime(new Date(15 * 1000));

        final OnmsEvent event = new OnmsEvent();
        event.setEventParametersFromEvent(new EventBuilder("test", "test")
                .addParam("nagUei", nagUei).getEvent());
        event.setEventTime(new Date(16 * 1000));
        alarm.setLastEvent(event);

        when(alarmDao.get(alarm.getId())).thenReturn(alarm);
        return alarm;
    }

    private static Event getNagEventFor(List<Event> events, OnmsAlarm alarm) {
        return events.stream().
                filter(e -> e.getParm("nagReductionKey").getValue().getContent().equals(alarm.getReductionKey()))
                .findFirst().orElseThrow(() -> new RuntimeException("No nag event found for: " + alarm));
    }
}
