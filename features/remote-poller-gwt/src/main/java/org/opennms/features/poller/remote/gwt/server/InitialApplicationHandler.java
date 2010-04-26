package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;

import de.novanic.eventservice.service.EventExecutorService;

public class InitialApplicationHandler implements ApplicationHandler {
//	private LocationDataService m_locationDataService;
	private EventExecutorService m_eventService;

	public InitialApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
//		m_locationDataService = locationDataService;
		m_eventService = eventService;
	}

	public void start(final int size) {
	}

	public void handle(final ApplicationInfo applicationInfo) {
		final ApplicationUpdatedRemoteEvent event = new ApplicationUpdatedRemoteEvent(applicationInfo);
		m_eventService.addEventUserSpecific(event);
	}

	public void finish() {
	}

}
