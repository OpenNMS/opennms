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

package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * <p>LocationStatusService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@RemoteServiceRelativePath("locationStatus")
public interface LocationStatusService extends RemoteService {
	/**
	 * <p>start</p>
	 */
	void start();
	/**
	 * <p>getLocationInfo</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public LocationInfo getLocationInfo(final String locationName);
	/**
	 * <p>getLocationDetails</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
	 */
	public LocationDetails getLocationDetails(final String locationName);
	/**
	 * <p>getApplicationInfo</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 */
	public ApplicationInfo getApplicationInfo(final String applicationName);
	/**
	 * <p>getApplicationDetails</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
	 */
	public ApplicationDetails getApplicationDetails(final String applicationName);
}
