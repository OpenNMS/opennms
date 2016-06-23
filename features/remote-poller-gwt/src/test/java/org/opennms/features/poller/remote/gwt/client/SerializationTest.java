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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.GeocodingFinishedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.GeocodingUpdatingRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;

import com.google.gwt.user.server.rpc.impl.LegacySerializationPolicy;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;

public class SerializationTest {
	private static final ServerSerializationStreamWriter writer = new ServerSerializationStreamWriter(LegacySerializationPolicy.getInstance());

	@Test
	public void testGWTLatLng() throws Exception {
		GWTLatLng point = new GWTLatLng(1D,1D);
		writer.writeObject(point);
	}

	@Test
	public void testGWTLocationMonitor() throws Exception {
		GWTLocationMonitor monitor = getMonitor();
		writer.writeObject(monitor);
	}

	@Test
	public void testGWTMonitoredService() throws Exception {
		GWTMonitoredService service = getMonitoredService();
		writer.writeObject(service);
	}

	@Test
	public void testGWTLocationSpecificStatus() throws Exception {
		GWTLocationSpecificStatus status = getLocationSpecificStatus();
		writer.writeObject(status);
	}

	@Test
	public void testLocationInfo() throws Exception {
		LocationInfo location = getLocationInfo();
		writer.writeObject(location);
	}

	@Test
	public void testLocationDetails() throws Exception {
		LocationDetails l = new LocationDetails();
		l.setLocationMonitorState(getLocationMonitorState());
		writer.writeObject(l);
	}

	@Test
	public void testApplicationStatus() throws Exception {
		ApplicationState state = getApplicationState();
		writer.writeObject(state);
	}

	@Test
	public void testApplicationInfo() throws Exception {
		final ApplicationInfo info = getApplicationInfo();
		writer.writeObject(info);
	}

	@Test
	public void testEvents() throws Exception {
		final ApplicationUpdatedRemoteEvent aure = new ApplicationUpdatedRemoteEvent(getApplicationInfo());
		writer.writeObject(aure);
		final GeocodingUpdatingRemoteEvent gure = new GeocodingUpdatingRemoteEvent(0, 15);
		writer.writeObject(gure);
		final GeocodingFinishedRemoteEvent gfre = new GeocodingFinishedRemoteEvent(15);
		writer.writeObject(gfre);
		final LocationUpdatedRemoteEvent lure = new LocationUpdatedRemoteEvent(getLocationInfo());
		writer.writeObject(lure);
		Collection<LocationInfo> locations = new ArrayList<LocationInfo>();
		locations.add(getLocationInfo());
		final LocationsUpdatedRemoteEvent lsure = new LocationsUpdatedRemoteEvent(locations);
		writer.writeObject(lsure);
		final UpdateCompleteRemoteEvent ucre = new UpdateCompleteRemoteEvent();
		writer.writeObject(ucre);
	}

	private LocationInfo getLocationInfo() {
		LocationInfo location = new LocationInfo();
		location.setName("Bob");
		location.setArea("East Coast");
		location.setGeolocation("RDU");
		location.setCoordinates("0.0,0.0");
		location.setStatusDetails(StatusDetails.up());
		location.setMarkerState(getMarker(location));
		return location;
	}

	private GWTMarkerState getMarker(LocationInfo info) {
		GWTMarkerState marker = new GWTMarkerState(info.getName(), info.getLatLng(), info.getStatusDetails().getStatus());
		return marker;
	}
	private ApplicationInfo getApplicationInfo() {
		final Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
		final Set<String> locationNames = new TreeSet<String>();

		services.add(getMonitoredService());
		locationNames.add(getMonitor().getDefinitionName());
		final ApplicationInfo info = new ApplicationInfo(1, "TestApp1", services, locationNames, StatusDetails.unknown());
		return info;
	}
	private ApplicationState getApplicationState() {
		final Collection<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
		final List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
		final Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
		final Set<String> locationNames = new TreeSet<String>();
		final Map<String,List<GWTLocationSpecificStatus>> applicationStatuses = new HashMap<String,List<GWTLocationSpecificStatus>>();

		services.add(getMonitoredService());
		locationNames.add(getMonitor().getDefinitionName());
		applications.add(new ApplicationInfo(1, "TestApp1", services, locationNames, StatusDetails.unknown()));
		applicationStatuses.put("TestApp1", statuses);
		statuses.add(getLocationSpecificStatus());
		final Date to = new Date();
		final Date from = new Date(to.getTime() - (1000 * 60 * 60 * 24));
		List<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();
		monitors.add(getMonitor());
		return new ApplicationState(to, from, applications, monitors, applicationStatuses);
	}

	private LocationMonitorState getLocationMonitorState() {
		Collection<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();
		Collection<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
		monitors.add(getMonitor());
		statuses.add(getLocationSpecificStatus());
		return new LocationMonitorState(monitors, statuses);
	}

	private GWTLocationSpecificStatus getLocationSpecificStatus() {
		GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
		status.setId(1);
		status.setLocationMonitor(getMonitor());
		status.setMonitoredService(getMonitoredService());
		status.setPollResult(getPollResult());
		return status;
	}

	private GWTPollResult getPollResult() {
		GWTPollResult result = new GWTPollResult();
		result.setReason("because!");
		result.setResponseTime(300D);
		result.setStatus("Up");
		result.setTimestamp(new Date());
		return result;
	}

	private GWTMonitoredService getMonitoredService() {
		GWTMonitoredService service = new GWTMonitoredService();
		service.setHostname("localhost");
		service.setId(1);
		service.setIfIndex(0);
		service.setIpAddress("127.0.0.1");
		service.setIpInterfaceId(2);
		service.setNodeId(3);
		service.setServiceName("HTTP");
		service.setApplications(getAppNames());
		return service;
	}

	private Set<String> getAppNames() {
		Set<String> appNames = new TreeSet<String>();
		appNames.add("TestApp1");
		appNames.add("TestApp3");
		return appNames;
	}
	private GWTLocationMonitor getMonitor() {
		GWTLocationMonitor monitor = new GWTLocationMonitor();
		monitor.setDefinitionName("blah");
		monitor.setId(Integer.toString(1));
		monitor.setLastCheckInTime(new Date());
		monitor.setName("foo");
		monitor.setStatus("STARTED");
		return monitor;
	}
}
