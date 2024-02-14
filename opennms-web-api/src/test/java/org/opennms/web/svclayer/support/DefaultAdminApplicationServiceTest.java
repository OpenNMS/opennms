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
package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.mock.MockApplicationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.xml.event.Event;

public class DefaultAdminApplicationServiceTest {

    @Test
    public void shouldSendEventsForApplication() throws EventProxyException {

        OnmsMonitoredService monitoredService = Mockito.mock(OnmsMonitoredService.class);
        DefaultAdminApplicationService service = new DefaultAdminApplicationService();
        EventProxy proxy = Mockito.mock(EventProxy.class);
        ApplicationDao dao = new MockApplicationDao();
        MonitoredServiceDao monitoredServiceDao = Mockito.mock(MonitoredServiceDao.class);
        when(monitoredServiceDao.get(anyInt())).thenReturn(monitoredService);
        monitoredServiceDao.save(monitoredService);
        service.setEventProxy(proxy);
        service.setApplicationDao(dao);
        service.setMonitoredServiceDao(monitoredServiceDao);

        // Create
        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
        service.addNewApplication("testApp");
        verify(proxy).send(argument.capture());
        assertEquals(EventConstants.APPLICATION_CREATED_EVENT_UEI, argument.getValue().getUei());

        // Edit: no actual change
        service.performEditServices("1", "Add", new String[0], new String[0]);
        verifyNoMoreInteractions(proxy);

        // Edit: adding a service
        service.performEditServices("1", "Add", new String[]{"1"}, new String[0]);
        verify(proxy, times(2)).send(argument.capture());
        assertEquals(EventConstants.APPLICATION_CHANGED_EVENT_UEI, argument.getValue().getUei());

        // Delete
        service.removeApplication("1");
        verify(proxy, times(3)).send(argument.capture());
        assertEquals(EventConstants.APPLICATION_DELETED_EVENT_UEI, argument.getValue().getUei());
    }
}
