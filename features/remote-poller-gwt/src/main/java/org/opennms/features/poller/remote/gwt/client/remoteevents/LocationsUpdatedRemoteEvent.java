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

package org.opennms.features.poller.remote.gwt.client.remoteevents;

import java.util.Collection;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

/**
 * <p>LocationsUpdatedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationsUpdatedRemoteEvent implements MapRemoteEvent {

	private static final long serialVersionUID = 7235016593748265464L;

	private Collection<LocationInfo> m_locations;

	/**
	 * <p>Constructor for LocationsUpdatedRemoteEvent.</p>
	 */
	public LocationsUpdatedRemoteEvent() {
	}

	/**
	 * <p>Constructor for LocationsUpdatedRemoteEvent.</p>
	 *
	 * @param locations a {@link java.util.Collection} object.
	 */
	public LocationsUpdatedRemoteEvent(final Collection<LocationInfo> locations) {
		m_locations = locations;
	}

	/**
	 * <p>getLocations</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<LocationInfo> getLocations() {
		return m_locations;
	}

	/**
	 * <p>setLocations</p>
	 *
	 * @param locations a {@link java.util.Collection} object.
	 */
	public void setLocations(final Collection<LocationInfo> locations) {
		m_locations = locations;
	}

	/** {@inheritDoc} */
        @Override
	public void dispatch(final MapRemoteEventHandler locationManager) {
		locationManager.updateLocations(m_locations);
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "LocationsUpdatedRemoteEvent[locations=" + m_locations + "]";
	}
}
