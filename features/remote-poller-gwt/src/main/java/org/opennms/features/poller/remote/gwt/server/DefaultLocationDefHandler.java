/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        @Override
	public void start(final int size) {
	}

	/**
	 * <p>handle</p>
	 *
	 * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
	 */
        @Override
	public void handle(final OnmsMonitoringLocationDefinition def) {
		final LocationUpdatedRemoteEvent event = new LocationUpdatedRemoteEvent(m_locationDataService.getLocationInfo(def));
		getEventService().addEventUserSpecific(event);
	}
	
	/**
	 * <p>finish</p>
	 */
        @Override
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
