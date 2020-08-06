/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
    public void shouldSentEventsForApplication() throws EventProxyException {

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
        service.performEdit("1", "Add", new String[0], new String[0]);
        verifyNoMoreInteractions(proxy);

        // Edit: adding a service
        service.performEdit("1", "Add", new String[]{"1"}, new String[0]);
        verify(proxy, times(2)).send(argument.capture());
        assertEquals(EventConstants.APPLICATION_CHANGED_EVENT_UEI, argument.getValue().getUei());

        // Delete
        service.removeApplication("1");
        verify(proxy, times(3)).send(argument.capture());
        assertEquals(EventConstants.APPLICATION_DELETED_EVENT_UEI, argument.getValue().getUei());
    }
}
