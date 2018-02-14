/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

public class LocationMonitorStateTest {
	private static int count = 0;

	@Test
	public void testAllMonitorsStarted() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			monitors.add(getMonitor("STARTED"));
		}
		LocationMonitorState lms = new LocationMonitorState(monitors, null);
		assertTrue("all location monitors should be STARTED", lms.allMonitorsStarted());

		monitors.add(getMonitor("CONFIG_CHANGED"));
		lms = new LocationMonitorState(monitors, null);
		assertTrue("all location monitors should be STARTED or CONFIG_CHANGED", lms.allMonitorsStarted());
		
		monitors.add(getMonitor("DISCONNECTED"));
		lms = new LocationMonitorState(monitors, null);
		assertFalse("at least one monitor is not STARTED or CONFIG_CHANGED", lms.allMonitorsStarted());
	}

	@Test
	public void testAtLeastOneMonitorStarted() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			monitors.add(getMonitor("DISCONNECTED"));
		}
		monitors.add(getMonitor("CONFIG_CHANGED"));
		LocationMonitorState lms = new LocationMonitorState(monitors, null);
		assertTrue("at least one monitor is CONFIG_CHANGED or STARTED", lms.atLeastOneMonitorStarted());

		monitors.add(getMonitor("STARTED"));
		lms = new LocationMonitorState(monitors, null);
		assertTrue("at least one monitor is CONFIG_CHANGED or STARTED", lms.atLeastOneMonitorStarted());
	}
	
	@Test
	public void testAllButOneMonitorsDisconnected() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			monitors.add(getMonitor("DISCONNECTED"));
		}
		LocationMonitorState lms = new LocationMonitorState(monitors, null);
		assertFalse("all monitors should be DISCONNECTED", lms.allButOneMonitorsDisconnected());
		
		monitors.add(getMonitor("STARTED"));
		lms = new LocationMonitorState(monitors, null);
		assertTrue("all but one monitors are DISCONNECTED", lms.allButOneMonitorsDisconnected());
		
		monitors.add(getMonitor("CONFIG_CHANGED"));
		lms = new LocationMonitorState(monitors, null);
		assertFalse("more than one monitor is STARTED or CONFIG_CHANGED", lms.allButOneMonitorsDisconnected());
	}

	@Test
	public void testMarkerStatusGreen() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<>();
		for (int i = 0; i< 5; i++) {
			GWTLocationMonitor monitor = getMonitor("STARTED");
			monitors.add(monitor);

			GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "HTTP"));
			status.setPollResult(GWTPollResult.available(100));
			statuses.add(status);
			
			status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "FTP"));
			status.setPollResult(GWTPollResult.available(150));
			statuses.add(status);
		}
		
		LocationMonitorState lms = new LocationMonitorState(monitors, statuses);
		assertEquals("status should be up", Status.UP, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusAllButOneNonStoppedDisconnected() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<>();

		GWTLocationMonitor monitor = getMonitor("STARTED");
		monitors.add(monitor);
		monitors.add(getMonitor("DISCONNECTED"));
		monitors.add(getMonitor("DISCONNECTED"));
		monitors.add(getMonitor("DISCONNECTED"));

		GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
		status.setId(++count);
		status.setLocationMonitor(monitor);
		status.setMonitoredService(getService(++count, "HTTP"));
		status.setPollResult(GWTPollResult.available(100));
		statuses.add(status);
		
		LocationMonitorState lms = new LocationMonitorState(monitors, statuses);
		assertEquals("status should be marginal if only one monitor started, and more than one disconnected", Status.MARGINAL, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusSomeReportDownStatus() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<>();
		
		int httpServiceId = ++count;
		int ftpServiceId = ++count;

		GWTLocationMonitor monitor = getMonitor("STARTED");
		monitors.add(monitor);

		GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
		status.setId(++count);
		status.setLocationMonitor(monitor);
		status.setMonitoredService(getService(httpServiceId, "HTTP"));
		status.setPollResult(GWTPollResult.available(100));
		statuses.add(status);

		status = new GWTLocationSpecificStatus();
		status.setId(++count);
		status.setLocationMonitor(monitor);
		status.setMonitoredService(getService(ftpServiceId, "FTP"));
		status.setPollResult(GWTPollResult.down("failure to yield to oncoming traffic"));
		statuses.add(status);

		monitor = getMonitor("STARTED");
		monitors.add(monitor);

		status = new GWTLocationSpecificStatus();
		status.setId(++count);
		status.setLocationMonitor(monitor);
		status.setMonitoredService(getService(httpServiceId, "HTTP"));
		status.setPollResult(GWTPollResult.down("trouble in paradise"));
		statuses.add(status);

		status = new GWTLocationSpecificStatus();
		status.setId(++count);
		status.setLocationMonitor(monitor);
		status.setMonitoredService(getService(ftpServiceId, "FTP"));
		status.setPollResult(GWTPollResult.available(150));
		statuses.add(status);

		LocationMonitorState lms = new LocationMonitorState(monitors, statuses);
		assertEquals("status should be marginal when some services are down", Status.MARGINAL, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusOneOfTwoServicesDown() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<>();
		for (int i = 0; i< 5; i++) {
			GWTLocationMonitor monitor = getMonitor("STARTED");
			monitors.add(monitor);

			GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "HTTP"));
			status.setPollResult(GWTPollResult.available(100));
			statuses.add(status);
			
			status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "FTP"));
			status.setPollResult(GWTPollResult.down("totally busted!"));
			statuses.add(status);
		}
		
		LocationMonitorState lms = new LocationMonitorState(monitors, statuses);
		assertEquals("status should be down when one service is down across all monitors", Status.DOWN, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusOneServiceDown() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<>();
		for (int i = 0; i< 5; i++) {
			GWTLocationMonitor monitor = getMonitor("STARTED");
			monitors.add(monitor);

			GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "HTTP"));
			status.setPollResult(GWTPollResult.down("completely wacked"));
			statuses.add(status);
		}
		
		LocationMonitorState lms = new LocationMonitorState(monitors, statuses);
		assertEquals("status should be down when one (of one) service is down across all monitors", Status.DOWN, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusTwoOfTwoServicesDown() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<>();
		for (int i = 0; i< 5; i++) {
			GWTLocationMonitor monitor = getMonitor("STARTED");
			monitors.add(monitor);

			GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "HTTP"));
			status.setPollResult(GWTPollResult.down("exploded"));
			statuses.add(status);
			
			status = new GWTLocationSpecificStatus();
			status.setId(++count);
			status.setLocationMonitor(monitor);
			status.setMonitoredService(getService(++count, "FTP"));
			status.setPollResult(GWTPollResult.down("casters up"));
			statuses.add(status);
		}
		
		LocationMonitorState lms = new LocationMonitorState(monitors, statuses);
		assertEquals("status should be down when two services (of two) are down across all monitors", Status.DOWN, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusAllNonStoppedMonitorsDisconnected() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		monitors.add(getMonitor("STOPPED"));
		monitors.add(getMonitor("STOPPED"));
		monitors.add(getMonitor("DISCONNECTED"));
		monitors.add(getMonitor("DISCONNECTED"));
		monitors.add(getMonitor("DISCONNECTED"));

		LocationMonitorState lms = new LocationMonitorState(monitors, null);
		assertEquals("status should be unknown if all monitors are either disconnected or stopped", Status.UNKNOWN, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusAllRegisteredMonitorsDisconnected() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		monitors.add(getMonitor("DISCONNECTED"));
		monitors.add(getMonitor("DISCONNECTED"));
		monitors.add(getMonitor("DISCONNECTED"));

		LocationMonitorState lms = new LocationMonitorState(monitors, null);
		assertEquals("status should be unknown if all registered monitors are disconnected", Status.UNKNOWN, lms.getStatusDetails().getStatus());
	}

	@Test
	public void testMarkerStatusOnlyOneRegisteredMonitorIsStopped() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<>();
		monitors.add(getMonitor("STOPPED"));
		
		LocationMonitorState lms = new LocationMonitorState(monitors, null);
		assertEquals("single stopped monitor should be unknown", Status.UNKNOWN, lms.getStatusDetails().getStatus());
	}

	private GWTLocationMonitor getMonitor(String status) {
		GWTLocationMonitor monitor = new GWTLocationMonitor();
		monitor.setDefinitionName("RDU");
		monitor.setId(Integer.toString(count++));
		monitor.setLastCheckInTime(new Date());
		monitor.setStatus(status);
		return monitor;
	}

	private GWTMonitoredService getService(int id, String serviceName) {
        GWTMonitoredService service = new GWTMonitoredService();
        service.setId(id);
		service.setServiceName(serviceName);
		return service;
    }
}
