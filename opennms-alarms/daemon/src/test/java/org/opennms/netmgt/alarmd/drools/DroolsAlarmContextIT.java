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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.alarmd.AlarmMatchers.hasSeverity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.dao.support.AlarmEntityNotifierImpl;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Sets;

/**
 * Used to isolate and trigger specific Drools rules in the default ruleset for Alarmd.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml"
})
@JUnitConfigurationEnvironment
public class DroolsAlarmContextIT {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsAlarmContextIT.class);

    private DroolsAlarmContext dac;
    private AlarmDao alarmDao;
    private AcknowledgmentDao acknowledgmentDao;

    private MockTicketer ticketer = new MockTicketer();

    @Before
    public void setUp() throws InterruptedException, IOException {
        dac = new DroolsAlarmContext(AlarmdTestUtil.enableDisabledRules());
        dac.setUsePseudoClock(true);
        dac.setUseManualTick(true);
        dac.setAlarmTicketerService(ticketer);

        MockTransactionTemplate transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();
        dac.setTransactionTemplate(transactionTemplate);

        alarmDao = mock(AlarmDao.class);
        when(alarmDao.findAll()).thenReturn(Collections.emptyList());
        dac.setAlarmDao(alarmDao);

        DefaultAlarmService alarmService = new DefaultAlarmService();
        alarmService.setAlarmDao(alarmDao);

        acknowledgmentDao = mock(AcknowledgmentDao.class);
        when(acknowledgmentDao.findLatestAckForRefId(any(Integer.class))).thenReturn(Optional.empty());
        alarmService.setAcknowledgmentDao(acknowledgmentDao);

        EventForwarder eventForwarder = mock(EventForwarder.class);
        alarmService.setEventForwarder(eventForwarder);

        AlarmEntityNotifierImpl alarmEntityNotifier = mock(AlarmEntityNotifierImpl.class);
        alarmService.setAlarmEntityNotifier(alarmEntityNotifier);
        dac.setAlarmService(alarmService);
        dac.setAcknowledgmentDao(acknowledgmentDao);

        dac.start();

        // Wait
        dac.waitForInitialSeedToBeSubmitted();
    }

    @After
    public void tearDown() {
        if (dac != null) {
            dac.stop();
        }
    }

    @Test
    public void canClearAlarm() {
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        OnmsAlarm clear = new OnmsAlarm();
        clear.setId(2);
        clear.setAlarmType(2);
        clear.setSeverity(OnmsSeverity.CLEARED);
        clear.setReductionKey("clear:n1:oops");
        clear.setClearKey("n1:oops");
        clear.setLastEventTime(new Date(101));
        when(alarmDao.get(clear.getId())).thenReturn(clear);
        dac.getClock().advanceTime( 101, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(clear);
        dac.tick();

        assertThat(trigger, hasSeverity(OnmsSeverity.CLEARED));
    }

    @Test
    public void canDeleteAlarm() {
        final OnmsAlarm toDelete = new OnmsAlarm();
        toDelete.setId(2);
        toDelete.setAlarmType(2);
        toDelete.setSeverity(OnmsSeverity.CLEARED);
        toDelete.setReductionKey("clear:n1:oops");
        toDelete.setClearKey("n1:oops");
        toDelete.setLastEventTime(new Date(101));
        when(alarmDao.get(toDelete.getId())).thenReturn(toDelete);

        final AtomicBoolean gotDelete = new AtomicBoolean();
        doAnswer(invocation -> {
            gotDelete.set(true);
            return null;
        }).when(alarmDao).delete(toDelete);
        dac.getClock().advanceTime( 101, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(toDelete);
        dac.tick();

        // The alarm should not be immediately deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 10, TimeUnit.MINUTES );
        dac.tick();

        // Validate
        assertThat(gotDelete.get(), equalTo(true));
    }

    @Test
    public void canDeleteAcknowledgedAlarm() {
        final OnmsAlarm toDelete = new OnmsAlarm();
        toDelete.setId(2);
        toDelete.setAlarmType(2);
        toDelete.setSeverity(OnmsSeverity.CLEARED);
        toDelete.setReductionKey("clear:n1:oops");
        toDelete.setClearKey("n1:oops");
        toDelete.setLastEventTime(new Date(101));
        // "Ack" the alarm
        toDelete.setAlarmAckTime(new Date(110));
        toDelete.setAlarmAckUser("me");
        when(alarmDao.get(toDelete.getId())).thenReturn(toDelete);

        final AtomicBoolean gotDelete = new AtomicBoolean();
        doAnswer(invocation -> {
            gotDelete.set(true);
            return null;
        }).when(alarmDao).delete(toDelete);
        dac.getClock().advanceTime( 110, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(toDelete);
        dac.tick();

        // The alarm should not be immediately deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 10, TimeUnit.MINUTES );
        dac.tick();

        // Still not deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 1, TimeUnit.DAYS );
        dac.tick();

        assertThat(gotDelete.get(), equalTo(true));
    }

    @Test
    public void canGarbageCollectAlarm() {
        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        final AtomicBoolean gotDelete = new AtomicBoolean();
        doAnswer(invocation -> {
            gotDelete.set(true);
            return null;
        }).when(alarmDao).delete(trigger);
        dac.getClock().advanceTime( 1, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // The alarm should not be immediately deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 1, TimeUnit.HOURS );
        dac.tick();

        // Still not deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 3, TimeUnit.DAYS );
        dac.tick();

        assertThat(gotDelete.get(), equalTo(true));
    }

    @Test
    public void canGarbageCollectAcknowledgedAlarm() {
        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        // Ack the problem
        trigger.setAlarmAckTime(new Date(110));
        trigger.setAlarmAckUser("me");
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);

        final AtomicBoolean gotDelete = new AtomicBoolean();
        doAnswer(invocation -> {
            gotDelete.set(true);
            return null;
        }).when(alarmDao).delete(trigger);
        dac.getClock().advanceTime( 110, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // The alarm should not be immediately deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 1, TimeUnit.HOURS );
        dac.tick();

        // Still not deleted
        assertThat(gotDelete.get(), equalTo(false));

        // Advance the clock and tick
        dac.getClock().advanceTime( 8, TimeUnit.DAYS );
        dac.tick();

        assertThat(gotDelete.get(), equalTo(true));
    }

    @Test
    public void canUnclearAlarm() {
        final OnmsEvent event = new OnmsEvent();
        event.setEventTime(new Date(101));
        event.setEventSeverity(OnmsSeverity.WARNING.getId());

        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(1);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.CLEARED);
        alarm.setReductionKey("n1:oops");
        alarm.setLastAutomationTime(new Date(event.getEventTime().getTime() - 1));
        alarm.setLastEvent(event);
        alarm.setLastEventTime(event.getEventTime());
        when(alarmDao.get(alarm.getId())).thenReturn(alarm);
        dac.getClock().advanceTime( 101, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(alarm);
        dac.tick();

        // The severity should be updated
        assertThat(alarm, hasSeverity(OnmsSeverity.WARNING));
    }

    @Test
    @Ignore("This rule is disabled by default")
    public void canEscalateAlarm() {
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));

        OnmsServiceType serviceType = new OnmsServiceType();
        serviceType.setName("ICMP");
        trigger.setServiceType(serviceType);

        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // Advance > 1 hours
        dac.getClock().advanceTime( 120, TimeUnit.MINUTES );
        dac.tick();

        // The severity should be updated
        assertThat(trigger, hasSeverity(OnmsSeverity.MINOR));
    }

    @Test
    public void canCreateTicket() {
        ticketer.setEnabled(true);

        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // No ticket yet
        assertThat(ticketer.getCreates(), hasSize(0));

        // Advance the clock and tick
        dac.getClock().advanceTime( 20, TimeUnit.MINUTES );
        dac.tick();

        // Ticket!
        assertThat(ticketer.getCreates(), contains(trigger.getId()));
    }

    @Test
    public void canCreateTicketForCriticalAlarm() {
        ticketer.setEnabled(true);

        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.CRITICAL);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // No ticket yet
        assertThat(ticketer.getCreates(), hasSize(0));

        // Advance the clock and tick
        dac.getClock().advanceTime( 6, TimeUnit.MINUTES );
        dac.tick();

        // Ticket!
        assertThat(ticketer.getCreates(), contains(trigger.getId()));
    }

    @Test
    public void canUpdateTicket() {
        ticketer.setEnabled(true);

        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 101, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // No ticket yet
        assertThat(ticketer.didCreatedTicketFor(trigger), equalTo(false));
        assertThat(ticketer.getNumUpdatesFor(trigger), equalTo(0));

        // Advance the clock and tick
        dac.getClock().advanceTime( 20, TimeUnit.MINUTES );
        dac.tick();

        // Ticket, but no update yet
        assertThat(ticketer.didCreatedTicketFor(trigger), equalTo(true));
        assertThat(ticketer.getNumUpdatesFor(trigger), equalTo(0));

        // Advance the clock and tick
        dac.getClock().advanceTime( 60, TimeUnit.MINUTES );
        dac.tick();
        assertThat(ticketer.getNumUpdatesFor(trigger), equalTo(1));

        // Advance the clock and tick
        dac.getClock().advanceTime( 20, TimeUnit.MINUTES );
        dac.tick();
        assertThat(ticketer.getNumUpdatesFor(trigger), equalTo(2));
    }

    @Test
    public void canCloseTicket() {
        ticketer.setEnabled(true);

        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // No ticket yet
        assertThat(ticketer.getCreates(), hasSize(0));

        // Advance the clock and tick
        dac.getClock().advanceTime( 20, TimeUnit.MINUTES );
        dac.tick();
        // Verify that there is another
        assertThat(ticketer.getCreates(), contains(trigger.getId()));

        // Inject a clear
        OnmsAlarm clear = new OnmsAlarm();
        clear.setId(2);
        clear.setAlarmType(2);
        clear.setSeverity(OnmsSeverity.CLEARED);
        clear.setReductionKey("clear:n1:oops");
        clear.setClearKey("n1:oops");
        clear.setLastEventTime(new Date(101));
        dac.getClock().advanceTime( 1, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(clear);
        dac.tick();

        // The trigger should be cleared
        assertThat(trigger, hasSeverity(OnmsSeverity.CLEARED));

        // Advance the clock and tick
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.getClock().advanceTime( 1, TimeUnit.MINUTES );
        dac.tick();
        dac.getClock().advanceTime( 20, TimeUnit.MINUTES );
        dac.tick();
        assertThat(ticketer.didCloseTicketFor(trigger), equalTo(true));
    }

    @Test
    public void canClearAlarmForClosedTicket() {
        ticketer.setEnabled(true);

        // Trigger some problem
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        // Pretend there is a closed ticket associated with this alarm
        trigger.setTTicketState(TroubleTicketState.CLOSED);
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // The trigger should be cleared
        assertThat(trigger, hasSeverity(OnmsSeverity.CLEARED));
    }

    @Test
    public void canDeleteRelatedAlarm() {
        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setId(1);
        alarm1.setAlarmType(1);
        alarm1.setSeverity(OnmsSeverity.WARNING);
        alarm1.setReductionKey("n1:oops1");
        alarm1.setLastEventTime(new Date(1000));
        OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setId(2);
        alarm2.setAlarmType(1);
        alarm2.setSeverity(OnmsSeverity.WARNING);
        alarm2.setReductionKey("n1:oops2");
        alarm2.setLastEventTime(new Date(1000));
        OnmsAlarm situation = new OnmsAlarm();
        situation.setId(3);
        situation.setAlarmType(1);
        situation.setSeverity(OnmsSeverity.WARNING);
        situation.setReductionKey("n1:situation");
        situation.setLastEventTime(new Date(2000));
        situation.setRelatedAlarms(Sets.newHashSet(alarm1, alarm2));

        when(alarmDao.get(alarm1.getId())).thenReturn(alarm1);
        when(alarmDao.get(alarm2.getId())).thenReturn(alarm2);
        when(alarmDao.get(situation.getId())).thenReturn(situation);
        dac.getClock().advanceTime(1000, TimeUnit.MILLISECONDS);
        dac.handleNewOrUpdatedAlarm(alarm1);
        dac.handleNewOrUpdatedAlarm(alarm2);
        dac.tick();

        dac.getClock().advanceTime(1000, TimeUnit.MILLISECONDS);
        dac.handleNewOrUpdatedAlarm(situation);
        dac.tick();

        // Now remove the 2nd alarm from the situation
        situation.setLastEventTime(new Date(3000));
        situation.setRelatedAlarms(Sets.newHashSet(alarm1));

        dac.getClock().advanceTime(1000, TimeUnit.MILLISECONDS);
        dac.handleNewOrUpdatedAlarm(situation);
        dac.tick();

    }

    private OnmsAlarm generateAlarm(int id) {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setAlarmType(1);
        alarm.setSeverity(OnmsSeverity.WARNING);
        alarm.setReductionKey("n" + id + ":oops");
        alarm.setFirstEventTime(new Date(id));
        alarm.setLastEventTime(new Date(id + 1));
        return alarm;
    }

    /**
     * Tests that the ack cache is updated with default acks when it can't find an ack in the db.
     */
    @Test
    public void testAckCachingNoDBHit() {
        OnmsAlarm alarm1 = generateAlarm(1);
        OnmsAlarm alarm2 = generateAlarm(2);
        dac.handleAlarmSnapshot(Arrays.asList(alarm1, alarm2));
        verify(acknowledgmentDao, times(1)).findLatestAcks(any(Date.class));
        dac.tick();
        assertThat(dac.getAckByAlarmId(alarm1.getId()).getAckAction(), equalTo(AckAction.UNACKNOWLEDGE));
        assertThat(dac.getAckByAlarmId(alarm2.getId()).getAckAction(), equalTo(AckAction.UNACKNOWLEDGE));

        dac.handleNewOrUpdatedAlarm(alarm1);
        verify(acknowledgmentDao, times(1)).findLatestAckForRefId(alarm1.getId());
        dac.tick();
        assertThat(dac.getAckByAlarmId(alarm1.getId()).getAckAction(), equalTo(AckAction.UNACKNOWLEDGE));
    }

    /**
     * Tests that the ack cache is updated with the result acks from the db when present.
     */
    @Test
    public void testAckCachingWithDBHit() {
        OnmsAlarm alarm1 = generateAlarm(1);
        OnmsAcknowledgment ack1 = new OnmsAcknowledgment(alarm1, DefaultAlarmService.DEFAULT_USER,
                alarm1.getFirstEventTime());
        ack1.setAckAction(AckAction.ACKNOWLEDGE);
        OnmsAlarm alarm2 = generateAlarm(2);
        OnmsAcknowledgment ack2 = new OnmsAcknowledgment(alarm2, DefaultAlarmService.DEFAULT_USER,
                alarm2.getFirstEventTime());
        ack2.setAckAction(AckAction.ESCALATE);

        when(acknowledgmentDao.findLatestAcks(any(Date.class))).thenReturn(Arrays.asList(ack1, ack2));
        dac.handleAlarmSnapshot(Arrays.asList(alarm1, alarm2));
        verify(acknowledgmentDao, times(1)).findLatestAcks(any(Date.class));
        dac.tick();
        assertThat(dac.getAckByAlarmId(alarm1.getId()).getAckAction(), equalTo(ack1.getAckAction()));
        assertThat(dac.getAckByAlarmId(alarm2.getId()).getAckAction(), equalTo(ack2.getAckAction()));

        when(acknowledgmentDao.findLatestAckForRefId(alarm1.getId())).thenReturn(Optional.of(ack1));
        dac.handleNewOrUpdatedAlarm(alarm1);
        verify(acknowledgmentDao, times(1)).findLatestAckForRefId(alarm1.getId());
        dac.tick();
        assertThat(dac.getAckByAlarmId(alarm1.getId()).getAckAction(), equalTo(ack1.getAckAction()));
    }

    public void canReloadEngine() {
        // Create a trigger alarm
        OnmsAlarm trigger = new OnmsAlarm();
        trigger.setId(1);
        trigger.setAlarmType(1);
        trigger.setSeverity(OnmsSeverity.WARNING);
        trigger.setReductionKey("n1:oops");
        trigger.setLastEventTime(new Date(100));
        when(alarmDao.get(trigger.getId())).thenReturn(trigger);
        dac.getClock().advanceTime( 100, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(trigger);
        dac.tick();

        // Update the mock to return the alarm we just created, so that the initial
        // seed includes it
        when(alarmDao.findAll()).thenReturn(Arrays.asList(trigger));

        // Reload the context
        dac.reload();

        // Create a clear alarm
        OnmsAlarm clear = new OnmsAlarm();
        clear.setId(2);
        clear.setAlarmType(2);
        clear.setSeverity(OnmsSeverity.CLEARED);
        clear.setReductionKey("clear:n1:oops");
        clear.setClearKey("n1:oops");
        clear.setLastEventTime(new Date(101));
        when(alarmDao.get(clear.getId())).thenReturn(clear);
        dac.getClock().advanceTime( 101, TimeUnit.MILLISECONDS );
        dac.handleNewOrUpdatedAlarm(clear);
        dac.tick();

        // The trigger should have been cleared
        assertThat(trigger, hasSeverity(OnmsSeverity.CLEARED));
    }

    private void printAlarmDetails(OnmsAlarm alarm) {
        // Useful for debugging
        System.out.printf("Pseudo Clock: %s\n", new Date(dac.getClock().getCurrentTime()));
        System.out.printf("Alarm (%d)\n", alarm.getId());
        System.out.printf("\tReduction Key: %s\n", alarm.getReductionKey());
        System.out.printf("\tSeverity: %s\n", alarm.getSeverity());
        System.out.printf("\tType: %s\n", alarm.getType());
        System.out.printf("\tTicket state: %s\n", alarm.getTTicketState());
        System.out.printf("\tLast event time: %s\n", alarm.getLastEventTime());
        System.out.printf("\tLast automation time: %s\n", alarm.getLastAutomationTime());
        System.out.printf("\tLast update time: %s\n", alarm.getLastUpdateTime());
    }

    private class MockTicketer implements AlarmTicketerService {
        private boolean enabled = false;
        private List<Integer> creates = new ArrayList<>();
        private List<Integer> closes = new ArrayList<>();

        private Map<Integer, Integer> updates = new LinkedHashMap<>();

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isTicketingEnabled() {
            return enabled;
        }

        @Override
        public void createTicket(OnmsAlarm alarm, Date now) {
            LOG.info("Creating ticket on {}", alarm);
            // Create ticket
            alarm.setTTicketState(TroubleTicketState.OPEN);
            alarm.setTTicketId("test");
            // Update the lastAutomationTime
            alarm.setLastAutomationTime(now);
            creates.add(alarm.getId());
            dac.handleNewOrUpdatedAlarm(alarm);
        }

        @Override
        public void updateTicket(OnmsAlarm alarm, Date now) {
            LOG.info("Updating ticket on {}", alarm);
            // Update the lastAutomationTime
            alarm.setLastAutomationTime(now);
            updates.compute(alarm.getId(), (k, v) -> (v == null) ? 1 : v + 1);
            dac.handleNewOrUpdatedAlarm(alarm);
        }

        @Override
        public void closeTicket(OnmsAlarm alarm, Date now) {
            LOG.info("Closing ticket on {}", alarm);
            // Close ticket
            alarm.setTTicketState(TroubleTicketState.CLOSED);
            closes.add(alarm.getId());
            dac.handleNewOrUpdatedAlarm(alarm);
        }

        public List<Integer> getCreates() {
            return creates;
        }

        public List<Integer> getCloses() {
            return closes;
        }

        public int getNumUpdatesFor(OnmsAlarm alarm) {
            return updates.computeIfAbsent(alarm.getId(), k -> 0);
        }

        public boolean didCreatedTicketFor(OnmsAlarm alarm) {
            return creates.contains(alarm.getId());
        }

        public boolean didCloseTicketFor(OnmsAlarm alarm) {
            return closes.contains(alarm.getId());
        }
    }
}
