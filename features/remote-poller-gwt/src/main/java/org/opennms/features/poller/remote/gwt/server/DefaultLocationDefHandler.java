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


	/**
	 * <p>Constructor for DefaultLocationDefHandler.</p>
	 *
	 * @param locationDataService a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
	 * @param eventService a {@link de.novanic.eventservice.service.EventExecutorService} object.
	 */
	public DefaultLocationDefHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
		m_locationDataService = locationDataService;
		m_eventService = eventService;
	}
	
	/** {@inheritDoc} */
	public void start(final int size) {
	}

	/**
	 * <p>handle</p>
	 *
	 * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
	 */
	public void handle(final OnmsMonitoringLocationDefinition def) {
		final LocationUpdatedRemoteEvent event = new LocationUpdatedRemoteEvent(m_locationDataService.getLocationInfo(def));
		getEventService().addEventUserSpecific(event);
	}
	
	/**
	 * <p>finish</p>
	 */
	public void finish() {
	}

	/**
	 * <p>sendEvent</p>
	 *
	 * @param event a {@link de.novanic.eventservice.client.event.Event} object.
	 */
	protected void sendEvent(final Event event) {
		getEventService().addEventUserSpecific(event);
	}

	/**
	 * <p>getEventService</p>
	 *
	 * @return a {@link de.novanic.eventservice.service.EventExecutorService} object.
	 */
	protected EventExecutorService getEventService() {
		return m_eventService;
	}


}
