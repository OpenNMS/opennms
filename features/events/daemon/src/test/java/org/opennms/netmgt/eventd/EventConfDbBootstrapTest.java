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
package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfGlobalSecurityDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfGlobalSecurity;

public class EventConfDbBootstrapTest {

    private EventConfDbBootstrap bootstrap(final EventConfEventDao events,
                                           final EventConfGlobalSecurityDao security,
                                           final EventConfDao target) {
        final EventConfDbBootstrap bootstrap = new EventConfDbBootstrap();
        bootstrap.setEventConfEventDao(events);
        bootstrap.setEventConfGlobalSecurityDao(security);
        bootstrap.setEventConfDao(target);
        return bootstrap;
    }

    @Test
    public void loadsTheConfigurationBeforeAnythingReadsIt() {
        final EventConfEventDao events = mock(EventConfEventDao.class);
        final EventConfGlobalSecurityDao security = mock(EventConfGlobalSecurityDao.class);
        final EventConfDao target = mock(EventConfDao.class);
        final List<EventConfEvent> enabled = List.of(new EventConfEvent());
        final List<EventConfGlobalSecurity> globals = List.of(new EventConfGlobalSecurity());
        when(events.findEnabledEvents()).thenReturn(enabled);
        when(security.findAll()).thenReturn(globals);

        bootstrap(events, security, target).afterPropertiesSet();

        verify(target).loadEventsFromDB(enabled, globals);
    }

    @Test
    public void doesNothingWhereThereIsNoDatabaseBackedConfiguration() {
        final EventConfDao target = mock(EventConfDao.class);

        bootstrap(null, null, target).afterPropertiesSet();

        verify(target, never()).loadEventsFromDB(any(), any());
    }

    @Test
    public void asksNothingWhenOnlySomeOfTheConfigurationIsAvailable() {
        final EventConfEventDao events = mock(EventConfEventDao.class);
        final EventConfDao target = mock(EventConfDao.class);

        bootstrap(events, null, target).afterPropertiesSet();

        verify(events, never()).findEnabledEvents();
        verify(target, never()).loadEventsFromDB(any(), any());
    }

    @Test
    public void startsAnywayWhenTheDatabaseCannotBeRead() {
        final EventConfEventDao events = mock(EventConfEventDao.class);
        final EventConfGlobalSecurityDao security = mock(EventConfGlobalSecurityDao.class);
        final EventConfDao target = mock(EventConfDao.class);
        doThrow(new IllegalStateException("no connection")).when(events).findEnabledEvents();

        bootstrap(events, security, target).afterPropertiesSet();

        verify(target, never()).loadEventsFromDB(any(), any());
    }

    @Test
    public void keepsWhateverTheDatabaseReturned() {
        final EventConfEventDao events = mock(EventConfEventDao.class);
        final EventConfGlobalSecurityDao security = mock(EventConfGlobalSecurityDao.class);
        final EventConfDao target = mock(EventConfDao.class);
        when(events.findEnabledEvents()).thenReturn(List.of());
        when(security.findAll()).thenReturn(List.of());

        bootstrap(events, security, target).afterPropertiesSet();

        verify(target).loadEventsFromDB(List.of(), List.of());
        assertEquals(0, events.findEnabledEvents().size());
    }
}
