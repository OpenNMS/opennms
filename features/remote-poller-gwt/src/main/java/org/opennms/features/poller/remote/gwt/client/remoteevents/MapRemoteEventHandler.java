/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public interface MapRemoteEventHandler {

    /** Constant <code>LOCATION_EVENT_DOMAIN</code> */
    public static final Domain LOCATION_EVENT_DOMAIN = DomainFactory.getDomain("location_event");

    /**
     * This action is used to respond to server-side events when a location is updated.
     *
     * @param locationInfo a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    public abstract void updateLocation(final LocationInfo locationInfo);

    public abstract void updateLocations(Collection<LocationInfo> locations);

    /**
     * <p>removeApplication</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     */
    public abstract void removeApplication(final String applicationName);

    /**
     * <p>updateApplication</p>
     *
     * @param applicationInfo a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public abstract void updateApplication(final ApplicationInfo applicationInfo);

    /**
     * This action is used to respond to server-side events sent when all initial location updates are complete.
     */
    public abstract void updateComplete();

}