package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.service.EventExecutorService;

/**
 * <p>UpdatedLocationDefHandler class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class UpdatedLocationDefHandler extends DefaultLocationDefHandler {

	/**
	 * <p>Constructor for UpdatedLocationDefHandler.</p>
	 *
	 * @param locationDataService a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
	 * @param eventService a {@link de.novanic.eventservice.service.EventExecutorService} object.
	 */
	public UpdatedLocationDefHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
		super(locationDataService, eventService);
	}

	/** {@inheritDoc} */
	@Override
	protected void sendEvent(final Event event) {
		getEventService().addEvent(MapRemoteEventHandler.LOCATION_EVENT_DOMAIN, event);
	}
}
