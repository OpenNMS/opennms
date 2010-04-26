/**
 * 
 */
package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.service.EventExecutorService;

class DefaultLocationDefHandler implements LocationDefHandler {
	private LocationDataService m_locationDataService;
	private EventExecutorService m_eventService;
	private boolean m_includeStatus;
	private Collection<LocationInfo> m_locations = new ArrayList<LocationInfo>();
	static final int MAX_LOCATIONS_PER_EVENT = 50;

	public DefaultLocationDefHandler(final LocationDataService locationDataService, final EventExecutorService eventService, boolean includeStatus) {
		m_locationDataService = locationDataService;
		m_eventService = eventService;
		m_includeStatus = includeStatus;
	}
	
	public void start(final int size) {
	}

	public void handle(final OnmsMonitoringLocationDefinition def) {
		m_locations.add(m_locationDataService.getLocationInfo(def, m_includeStatus));
		if (m_locations.size() >= MAX_LOCATIONS_PER_EVENT) {
			sendLocations();
		}
	}
	
	public void finish() {
		sendLocations();
	}

	protected void sendEvent(final Event event) {
		getEventService().addEventUserSpecific(event);
	}

	protected EventExecutorService getEventService() {
		return m_eventService;
	}

	protected void sendLocations() {
		if (m_locations.size() > 0) {
			final LocationsUpdatedRemoteEvent event = new LocationsUpdatedRemoteEvent(m_locations);
			m_locations = new ArrayList<LocationInfo>();
			sendEvent(event);
			getEventService().addEventUserSpecific(event);
		}
	}

}