/**
 * 
 */
package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.service.EventExecutorService;

class DefaultLocationDefHandler implements LocationDefHandler {
	private LocationDataService m_locationDataService;
	private EventExecutorService m_eventService;


	public DefaultLocationDefHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
		m_locationDataService = locationDataService;
		m_eventService = eventService;
	}
	
	public void start(final int size) {
	}

	public void handle(final OnmsMonitoringLocationDefinition def) {
		final LocationUpdatedRemoteEvent event = new LocationUpdatedRemoteEvent(m_locationDataService.getLocationInfo(def));
		getEventService().addEventUserSpecific(event);
	}
	
	public void finish() {
	}

	protected void sendEvent(final Event event) {
		getEventService().addEventUserSpecific(event);
	}

	protected EventExecutorService getEventService() {
		return m_eventService;
	}


}