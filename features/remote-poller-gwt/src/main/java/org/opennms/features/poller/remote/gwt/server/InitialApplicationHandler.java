package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.netmgt.model.OnmsApplication;

import de.novanic.eventservice.service.EventExecutorService;

public class InitialApplicationHandler implements ApplicationHandler {
	private LocationDataService m_locationDataService;
	private EventExecutorService m_eventService;
	private boolean m_includeStatus = false;

	public InitialApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService, final boolean includeStatus) {
		m_locationDataService = locationDataService;
		m_eventService = eventService;
		m_includeStatus = includeStatus;
	}

	public void start(final int size) {
	}

	public void handle(final OnmsApplication application) {
		final ApplicationInfo applicationInfo = m_locationDataService.getApplicationInfo(application, m_includeStatus);
		final ApplicationUpdatedRemoteEvent event = new ApplicationUpdatedRemoteEvent(applicationInfo);
		m_eventService.addEventUserSpecific(event);
	}

	public void finish() {
	}

}
