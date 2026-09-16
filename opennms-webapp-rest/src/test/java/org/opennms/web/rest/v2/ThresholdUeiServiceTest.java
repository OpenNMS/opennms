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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;

/**
 * Covers the event definitions generated for a threshold's custom UEIs. Without them a custom UEI produces
 * an event that raises no alarm and fires no notification, and nothing tells the user.
 */
public class ThresholdUeiServiceTest {

    private static final String CUSTOM_TRIGGER_UEI = "uei.opennms.org/example/highCpuExceeded";
    private static final String CUSTOM_REARM_UEI = "uei.opennms.org/example/highCpuRearmed";

    private ThresholdUeiService thresholdUeiService;
    private EventConfDao eventConfDao;
    private EventConfPersistenceService eventConfPersistenceService;

    @Before
    public void setUp() throws Exception {
        thresholdUeiService = new ThresholdUeiService();
        eventConfDao = mock(EventConfDao.class);
        eventConfPersistenceService = mock(EventConfPersistenceService.class);
        setField(thresholdUeiService, "eventConfDao", eventConfDao);
        setField(thresholdUeiService, "eventConfPersistenceService", eventConfPersistenceService);
    }

    @Test
    public void createsNothingWhenTheThresholdUsesTheStandardUeis() {
        thresholdUeiService.ensureUeisInEventConf(threshold(ThresholdType.HIGH, null, null));

        verify(eventConfPersistenceService, never()).saveProgrammaticEvent(any(), anyString());
    }

    @Test
    public void createsNothingWhenTheCustomUeiIsAlreadyKnown() {
        when(eventConfDao.getEvents(CUSTOM_TRIGGER_UEI)).thenReturn(List.of(new Event()));

        thresholdUeiService.ensureUeisInEventConf(threshold(ThresholdType.HIGH, CUSTOM_TRIGGER_UEI, null));

        verify(eventConfPersistenceService, never()).saveProgrammaticEvent(any(), anyString());
    }

    @Test
    public void clonesTheBuiltInEventForAnUnknownTriggerUei() {
        when(eventConfDao.getEvents(EventConstants.HIGH_THRESHOLD_EVENT_UEI))
                .thenReturn(List.of(sourceEvent("uei.opennms.org/threshold/highThresholdExceeded:%uei%:%dsname%")));
        when(eventConfDao.getEvents(CUSTOM_TRIGGER_UEI)).thenReturn(List.of());

        thresholdUeiService.ensureUeisInEventConf(threshold(ThresholdType.HIGH, CUSTOM_TRIGGER_UEI, null));

        final Event created = captureSavedEvent();
        assertEquals(CUSTOM_TRIGGER_UEI, created.getUei());
        assertEquals("Source description", created.getDescr());
        assertEquals("Minor", created.getSeverity());
        assertEquals("Do the thing", created.getOperinstruct());
        assertEquals(LogDestType.LOGNDISPLAY, created.getLogmsg().getDest());
        assertEquals("Source log message", created.getLogmsg().getContent());
        assertNotNull(created.getAlarmData());
        assertEquals(Integer.valueOf(1), created.getAlarmData().getAlarmType());
        // A trigger event carries no clear key; only the rearm event clears the alarm the trigger raised.
        assertNull(created.getAlarmData().getClearKey());
        verify(eventConfPersistenceService, times(1)).reloadEventsIntoMemory();
    }

    @Test
    public void fallsBackToWarningDefaultsWhenTheBuiltInEventIsMissing() {
        when(eventConfDao.getEvents(anyString())).thenReturn(List.of());

        thresholdUeiService.ensureUeisInEventConf(threshold(ThresholdType.HIGH, CUSTOM_TRIGGER_UEI, null));

        final Event created = captureSavedEvent();
        assertEquals("Warning", created.getSeverity());
        assertEquals(LogDestType.LOGNDISPLAY, created.getLogmsg().getDest());
        assertTrue(created.getDescr(), created.getDescr().startsWith("Threshold exceeded for %service%"));
        assertNull(created.getAlarmData());
    }

    @Test
    public void givesTheRearmEventAClearKeyBuiltFromTheTriggerReductionKey() {
        when(eventConfDao.getEvents(EventConstants.HIGH_THRESHOLD_EVENT_UEI))
                .thenReturn(List.of(sourceEvent("reduction:%uei%:tail")));
        when(eventConfDao.getEvents(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI))
                .thenReturn(List.of(sourceEvent("rearm-reduction:%uei%:tail")));
        when(eventConfDao.getEvents(CUSTOM_TRIGGER_UEI)).thenReturn(List.of());
        when(eventConfDao.getEvents(CUSTOM_REARM_UEI)).thenReturn(List.of());

        thresholdUeiService.ensureUeisInEventConf(threshold(ThresholdType.HIGH, CUSTOM_TRIGGER_UEI, CUSTOM_REARM_UEI));

        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventConfPersistenceService, times(2)).saveProgrammaticEvent(captor.capture(), eq("Web UI"));

        final Event rearmEvent = captor.getAllValues().get(1);
        assertEquals(CUSTOM_REARM_UEI, rearmEvent.getUei());
        assertEquals("reduction:" + CUSTOM_TRIGGER_UEI + ":tail", rearmEvent.getAlarmData().getClearKey());
    }

    @Test
    public void createsNoRearmEventForThresholdTypesThatNeverRearm() {
        // The mapper already drops the rearmed UEI for these types; this pins the behaviour end to end.
        when(eventConfDao.getEvents(anyString())).thenReturn(List.of());

        final Threshold threshold = threshold(ThresholdType.RELATIVE_CHANGE, CUSTOM_TRIGGER_UEI, null);
        thresholdUeiService.ensureUeisInEventConf(threshold);

        verify(eventConfPersistenceService, times(1)).saveProgrammaticEvent(any(), anyString());
    }

    @Test
    public void resolvesTheBuiltInEventPerThresholdTypeAndDirection() {
        when(eventConfDao.getEvents(anyString())).thenAnswer(invocation -> {
            final Event event = new Event();
            event.setUei(invocation.getArgument(0));
            return List.of(event);
        });

        assertEquals(EventConstants.HIGH_THRESHOLD_EVENT_UEI,
                thresholdUeiService.getSourceEvent(ThresholdType.HIGH, true).getUei());
        assertEquals(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI,
                thresholdUeiService.getSourceEvent(ThresholdType.HIGH, false).getUei());
        assertEquals(EventConstants.LOW_THRESHOLD_EVENT_UEI,
                thresholdUeiService.getSourceEvent(ThresholdType.LOW, true).getUei());
        assertEquals(EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI,
                thresholdUeiService.getSourceEvent(ThresholdType.LOW, false).getUei());
        assertEquals(EventConstants.REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI,
                thresholdUeiService.getSourceEvent(ThresholdType.REARMING_ABSOLUTE_CHANGE, true).getUei());
        assertEquals(EventConstants.REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI,
                thresholdUeiService.getSourceEvent(ThresholdType.REARMING_ABSOLUTE_CHANGE, false).getUei());

        // The change thresholds have no rearm event at all.
        assertNull(thresholdUeiService.getSourceEvent(ThresholdType.RELATIVE_CHANGE, false));
        assertNull(thresholdUeiService.getSourceEvent(ThresholdType.ABSOLUTE_CHANGE, false));
    }

    @Test
    public void ignoresADefinitionWithoutAType() {
        thresholdUeiService.ensureUeisInEventConf(null);
        thresholdUeiService.ensureUeisInEventConf(new Threshold());

        verify(eventConfPersistenceService, never()).saveProgrammaticEvent(any(), anyString());
    }

    // ------------------------------------------------------------------ helpers

    private Event captureSavedEvent() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventConfPersistenceService).saveProgrammaticEvent(captor.capture(), eq("Web UI"));
        return captor.getValue();
    }

    private static Threshold threshold(final ThresholdType type, final String triggeredUei, final String rearmedUei) {
        final Threshold threshold = new Threshold();
        threshold.setType(type);
        threshold.setTriggeredUEI(triggeredUei);
        threshold.setRearmedUEI(rearmedUei);
        return threshold;
    }

    private static Event sourceEvent(final String reductionKey) {
        final Event event = new Event();
        event.setDescr("Source description");
        event.setSeverity("Minor");
        event.setOperinstruct("Do the thing");

        final Logmsg logmsg = new Logmsg();
        logmsg.setDest(LogDestType.LOGNDISPLAY);
        logmsg.setContent("Source log message");
        event.setLogmsg(logmsg);

        final AlarmData alarmData = new AlarmData();
        alarmData.setAlarmType(1);
        alarmData.setAutoClean(Boolean.FALSE);
        alarmData.setReductionKey(reductionKey);
        event.setAlarmData(alarmData);

        return event;
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
