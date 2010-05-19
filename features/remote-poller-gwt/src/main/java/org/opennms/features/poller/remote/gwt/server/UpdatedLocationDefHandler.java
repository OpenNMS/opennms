package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.service.EventExecutorService;

public class UpdatedLocationDefHandler extends DefaultLocationDefHandler {

	public UpdatedLocationDefHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
		super(locationDataService, eventService, true);
	}

	@Override
	protected void sendEvent(final Event event) {
		getEventService().addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, event);
	}
}
