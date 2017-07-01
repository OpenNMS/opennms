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

package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

public class PollerTest {

    @Test
    public void testSchedule() throws Exception {
        testSchedule(false, getMonitoredService());
    }

    @Test
    public void testReschedule() throws Exception {
        testSchedule(true, getMonitoredService());
    }

    @Test
    public void testIPv6Schedule() throws Exception {
        testSchedule(false, getIPv6MonitoredService());
    }

    @Test
    public void testIPv6Reschedule() throws Exception {
        testSchedule(true, getIPv6MonitoredService());
    }

	public void testSchedule(boolean reschedule, OnmsMonitoredService svc) throws Exception {
		
		Scheduler scheduler = createMock(Scheduler.class);
		PollService pollService = createNiceMock(PollService.class);
		PollerFrontEnd pollerFrontEnd = createMock(PollerFrontEnd.class);
		
        svc.setId(7);
		
		PollConfiguration pollConfig = new PollConfiguration(svc, new HashMap<String,Object>(), 300000);
		
		PolledService polledService = new PolledService(pollConfig.getMonitoredService(), pollConfig.getMonitorConfiguration(), pollConfig.getPollModel());
		
		Set<PolledService> polledServices = Collections.singleton(polledService);

        Poller poller = new Poller();

        pollerFrontEnd.addConfigurationChangedListener(poller);
        pollerFrontEnd.addPropertyChangeListener(poller);
		expect(pollerFrontEnd.getPolledServices()).andReturn(polledServices);
        expect(pollerFrontEnd.isStarted()).andReturn(true);
        
        expect(scheduler.deleteJob(new JobKey(polledService.toString(), PollJobDetail.GROUP))).andReturn(reschedule);
        
		pollerFrontEnd.setInitialPollTime(eq(svc.getId()), isA(Date.class));
		expect(scheduler.scheduleJob(isA(PollJobDetail.class), isA(Trigger.class))).andReturn(new Date());
		
		replay(scheduler, pollService, pollerFrontEnd);
		
		poller.setScheduler(scheduler);
		poller.setPollerFrontEnd(pollerFrontEnd);
		
		poller.afterPropertiesSet();
		
		verify(scheduler, pollService, pollerFrontEnd);
		
	}
	
	private OnmsMonitoredService getMonitoredService() {
		OnmsNode node = new OnmsNode();
		OnmsMonitoringLocation location = new OnmsMonitoringLocation();
		location.setLocationName("MINION");
		node.setId(1);
		node.setLocation(location);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
		return svc;
	}

    private OnmsMonitoredService getIPv6MonitoredService() {
        OnmsNode node = new OnmsNode();
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("MINION");
        node.setId(1);
        node.setLocation(location);
        OnmsIpInterface iface = new OnmsIpInterface("::1", node);
        // Make sure that the address is being converted into fully-qualified format
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", str(iface.getIpAddress()));
        OnmsServiceType svcType = new OnmsServiceType("HTTP");
        OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
        return svc;
    }
}
