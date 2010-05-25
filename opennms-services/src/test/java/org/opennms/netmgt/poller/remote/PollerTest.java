//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.quartz.Scheduler;

public class PollerTest extends TestCase {
    
    public void testSchedule() throws Exception {
        testSchedule(false);
    }
    
    public void testReschedule() throws Exception {
        testSchedule(true);
    }
	
	public void testSchedule(boolean reschedule) throws Exception {
		
		Scheduler scheduler = createMock(Scheduler.class);
		PollService pollService = createNiceMock(PollService.class);
		PollerFrontEnd pollerFrontEnd = createMock(PollerFrontEnd.class);
		
		OnmsMonitoredService svc = getMonitoredService();
        svc.setId(7);
		
		PollConfiguration pollConfig = new PollConfiguration(svc, new HashMap<String,Object>(), 300000);
		
		PolledService polledService = new PolledService(pollConfig.getMonitoredService(), pollConfig.getMonitorConfiguration(), pollConfig.getPollModel());
		
		Set<PolledService> polledServices = Collections.singleton(polledService);

        Poller poller = new Poller();

        pollerFrontEnd.addConfigurationChangedListener(poller);
        pollerFrontEnd.addPropertyChangeListener(poller);
		expect(pollerFrontEnd.getPolledServices()).andReturn(polledServices);
        expect(pollerFrontEnd.isStarted()).andReturn(true);
        
        expect(scheduler.deleteJob(polledService.toString(), PollJobDetail.GROUP)).andReturn(reschedule);
        
		pollerFrontEnd.setInitialPollTime(eq(svc.getId()), isA(Date.class));
		expect(scheduler.scheduleJob(isA(PollJobDetail.class), isA(PolledServiceTrigger.class))).andReturn(new Date());
		
		replay(scheduler, pollService, pollerFrontEnd);
		
		poller.setScheduler(scheduler);
		poller.setPollerFrontEnd(pollerFrontEnd);
		
		poller.afterPropertiesSet();
		
		verify(scheduler, pollService, pollerFrontEnd);
		
	}
	
	private OnmsMonitoredService getMonitoredService() {
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
		return svc;
	}


}
