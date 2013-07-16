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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.remoteevents.GeocodingFinishedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.GeocodingUpdatingRemoteEvent;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

import de.novanic.eventservice.service.EventExecutorService;

class GeocodingHandler implements LocationDefHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GeocodingHandler.class);
	private final LocationDataService m_locationDataService;
	private final EventExecutorService m_eventService;
	private int m_size;
	private Date m_date;
	private int m_count;
	
	/**
	 * <p>Constructor for GeocodingHandler.</p>
	 *
	 * @param locationDataService a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
	 * @param eventService a {@link de.novanic.eventservice.service.EventExecutorService} object.
	 */
	public GeocodingHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
		m_locationDataService = locationDataService;
		m_eventService = eventService;
		m_date = new Date();
		m_count = 0;
	}
	
	/** {@inheritDoc} */
        @Override
	public void start(final int size) {
		m_size = size;
		m_eventService.addEventUserSpecific(new GeocodingUpdatingRemoteEvent(0, size));
	}

	/**
	 * <p>handle</p>
	 *
	 * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
	 */
        @Override
	public void handle(final OnmsMonitoringLocationDefinition def) {
		final GWTLatLng latLng = m_locationDataService.getLatLng(def, false);
		if (latLng != null) {
			def.setCoordinates(latLng.getCoordinates());
		}
		final Date now = new Date();
		if (now.getTime() - m_date.getTime() >= 500) {
			m_eventService.addEventUserSpecific(new GeocodingUpdatingRemoteEvent(m_count, m_size));
			LOG.debug("initializing locations ({}/{})", m_count, m_size);
			m_date = now;
		}
		m_count++;
	}
	
	/**
	 * <p>finish</p>
	 */
        @Override
	public void finish() {
		m_eventService.addEventUserSpecific(new GeocodingFinishedRemoteEvent(m_size));
	}
}
