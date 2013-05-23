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

package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.ApplicationState;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>LocationDetails class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationDetails implements Serializable, IsSerializable {

	private static final long serialVersionUID = -3516138717790564429L;

	private LocationMonitorState m_locationMonitorState;
	private ApplicationState m_applicationState;

	/**
	 * <p>Constructor for LocationDetails.</p>
	 */
	public LocationDetails() { }

    /**
     * <p>getLocationMonitorState</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationMonitorState} object.
     */
    public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}

	/**
	 * <p>setLocationMonitorState</p>
	 *
	 * @param lms a {@link org.opennms.features.poller.remote.gwt.client.LocationMonitorState} object.
	 */
	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}
	
	/**
	 * <p>getApplicationState</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationState} object.
	 */
	public ApplicationState getApplicationState() {
		return m_applicationState;
	}

	/**
	 * <p>setApplicationState</p>
	 *
	 * @param applicationState a {@link org.opennms.features.poller.remote.gwt.client.ApplicationState} object.
	 */
	public void setApplicationState(final ApplicationState applicationState) {
		m_applicationState = applicationState;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "LocationDetails[locationMonitorState=" + m_locationMonitorState + ",applicationState=" + m_applicationState + "]";
	}
}
