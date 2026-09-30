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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.xml.eventconf.Event;

/**
 * Covers saving programmatic event definitions without a database. The database-backed behaviour of the
 * rest of the service is covered by {@link EventConfPersistenceServiceIT}.
 */
public class EventConfPersistenceServiceTest {

    private static final String KNOWN_UEI = "uei.opennms.org/example/known";
    private static final String NEW_UEI = "uei.opennms.org/example/new";

    private EventConfPersistenceService service;
    private EventConfSourceDao eventConfSourceDao;
    private EventConfEventDao eventConfEventDao;
    private EventConfSource source;

    @Before
    public void setUp() throws Exception {
        service = new EventConfPersistenceService();
        eventConfSourceDao = mock(EventConfSourceDao.class);
        eventConfEventDao = mock(EventConfEventDao.class);
        setField(service, "eventConfSourceDao", eventConfSourceDao);
        setField(service, "eventConfEventDao", eventConfEventDao);

        source = new EventConfSource();
        source.setId(42L);
        when(eventConfSourceDao.findByName("opennms.programmatic.events")).thenReturn(source);
        when(eventConfSourceDao.lockForUpdate(42L)).thenReturn(source);
        when(eventConfEventDao.findByUeiAndSourceId(eq(KNOWN_UEI), eq(42L))).thenReturn(List.of(new EventConfEvent()));
        when(eventConfEventDao.findByUeiAndSourceId(eq(NEW_UEI), eq(42L))).thenReturn(List.of());
        when(eventConfEventDao.countBySourceId(42L)).thenReturn(2);
    }

    @After
    public void tearDown() {
        service.shutdown();
    }

    @Test
    public void savesAllEventsUnderOneLockAndSkipsUeisTheSourceAlreadyHolds() {
        // The caller checks the in-memory eventconf, which lags the database until the asynchronous reload,
        // so a second save shortly after the first must not add the same UEI again.
        final int saved = service.saveProgrammaticEvents(List.of(event(KNOWN_UEI), event(NEW_UEI)), "Web UI");

        assertEquals(1, saved);
        verify(eventConfSourceDao, times(1)).lockForUpdate(42L);

        final ArgumentCaptor<EventConfEvent> captor = ArgumentCaptor.forClass(EventConfEvent.class);
        verify(eventConfEventDao, times(1)).save(captor.capture());
        assertEquals(NEW_UEI, captor.getValue().getUei());
        assertEquals(Integer.valueOf(2), source.getEventCount());
        verify(eventConfSourceDao).save(source);
    }

    @Test
    public void leavesTheSourceAloneWhenEverythingIsAlreadyThere() {
        assertEquals(0, service.saveProgrammaticEvents(List.of(event(KNOWN_UEI)), "Web UI"));

        verify(eventConfEventDao, never()).save(any(EventConfEvent.class));
        verify(eventConfSourceDao, never()).save(any(EventConfSource.class));
    }

    @Test
    public void doesNotTouchTheDatabaseForAnEmptyList() {
        assertEquals(0, service.saveProgrammaticEvents(List.of(), "Web UI"));

        verify(eventConfSourceDao, never()).findByName(any());
        verify(eventConfSourceDao, never()).lockForUpdate(anyLong());
    }

    private static Event event(final String uei) {
        final Event event = new Event();
        event.setUei(uei);
        event.setEventLabel("label");
        event.setSeverity("Warning");
        return event;
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
