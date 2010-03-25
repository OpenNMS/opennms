package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

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
	public void testUpdateLocation() throws Exception {
		UpdateLocation location = new UpdateLocation();
		location.setArea("East Coast");
		location.setGeolocation("RDU");
		location.setLatLng(new GWTLatLng(1D, 1D));
		location.setLocationMonitorState(getLocationMonitorState());
		location.setName("Bob");
		location.setPollingPackageName("Harry");
		writer.writeObject(location);
		
		Collection<Location> locations = new ArrayList<Location>();
		locations.add(location);
		UpdateLocations updateLocations = new UpdateLocations(locations);
		writer.writeObject(updateLocations);
	}

	@Test
	public void testBaseLocation() throws Exception {
		BaseLocation location = new BaseLocation();
		location.setArea("East Coast");
		location.setGeolocation("RDU");
		location.setLatLng(new GWTLatLng(1D, 1D));
		location.setLocationMonitorState(getLocationMonitorState());
		location.setName("Bob");
		location.setPollingPackageName("Harry");
		writer.writeObject(location);
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
		return service;
	}

	private GWTLocationMonitor getMonitor() {
		GWTLocationMonitor monitor = new GWTLocationMonitor();
		monitor.setDefinitionName("blah");
		monitor.setId(1);
		monitor.setLastCheckInTime(new Date());
		monitor.setName("foo");
		monitor.setStatus("STARTED");
		return monitor;
	}
}
