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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
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
public class MinecraftExampleIT extends DroolsExampleIT {
    private static final String PLAYER_ENTERED_ZONE_UEI = "uei.opennms.org/devjam/2020/minecraft/playerEnteredZone";
    private static final String ZONE_OVER_CAPACITY_UEI = "uei.opennms.org/devjam/2020/minecraft/zoneOverCapacity";

    @Override
    public String getRulesFile() {
        return "minecraft.drl";
    }

    @Test
    public void canTriggerZoneOverCapacity() {
        // Tick
        dac.getClock().advanceTime( 16, TimeUnit.SECONDS );
        List<OnmsAlarm> ghostsInMousebar = new LinkedList<>();
        for (int i = 1; i <= 10; i++) {
            OnmsAlarm ghostInMousebar = createPlayerEnteredZoneAlarm(i, "mousebar", "Ghost #" + i);
            ghostsInMousebar.add(ghostInMousebar);
            dac.handleNewOrUpdatedAlarm(ghostInMousebar);
        }
        // t = 00:16.0000
        dac.tick();

        // Verify
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder).sendNow(eventCaptor.capture());
        Event event = eventCaptor.getValue();
        assertThat(event.getUei(), equalTo(ZONE_OVER_CAPACITY_UEI));
        reset(eventForwarder);

        // Inject the zone over capacity alarm
        OnmsAlarm zoneOverCapacityAlarm = createZoneOverCapacityAlarm(99, "mousebar");
        dac.handleNewOrUpdatedAlarm(zoneOverCapacityAlarm);
        dac.getClock().advanceTime( 1, TimeUnit.SECONDS );
        // t = 00:17.0000
        dac.tick();

        // Now remove the ghosts
        for (OnmsAlarm ghostInMousebar : ghostsInMousebar) {
            dac.handleDeletedAlarm(ghostInMousebar.getId(), ghostInMousebar.getReductionKey());
        }
        dac.getClock().advanceTime( 1, TimeUnit.SECONDS );
        // t = 00:18.0000
        dac.tick();

        // Verify
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder).sendNow(eventCaptor.capture());
        event = eventCaptor.getValue();
        assertThat(event.getUei(), equalTo("uei.opennms.org/devjam/2020/minecraft/zoneUnderCapacity"));
        reset(eventForwarder);
    }

    private OnmsAlarm createPlayerEnteredZoneAlarm(int id, String zone, String player) {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.WARNING);
        alarm.setReductionKey(PLAYER_ENTERED_ZONE_UEI + id);
        alarm.setFirstEventTime(new Date(15 * 1000));

        final OnmsEvent event = new OnmsEvent();
        event.setEventUei(PLAYER_ENTERED_ZONE_UEI);
        event.setEventTime(new Date(16 * 1000));
        event.addEventParameter(new OnmsEventParameter(event, "zone", zone, "string"));
        event.addEventParameter(new OnmsEventParameter(event, "player", player, "string"));
        alarm.setLastEvent(event);

        when(alarmDao.get(alarm.getId())).thenReturn(alarm);
        return alarm;
    }

    private OnmsAlarm createZoneOverCapacityAlarm(int id, String zone) {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.WARNING);
        alarm.setReductionKey(ZONE_OVER_CAPACITY_UEI + id);
        alarm.setFirstEventTime(new Date(15 * 1000));

        final OnmsEvent event = new OnmsEvent();
        event.setEventUei(ZONE_OVER_CAPACITY_UEI);
        event.setEventTime(new Date(16 * 1000));
        event.addEventParameter(new OnmsEventParameter(event, "zone", zone, "string"));
        alarm.setLastEvent(event);

        when(alarmDao.get(alarm.getId())).thenReturn(alarm);
        return alarm;
    }
}
