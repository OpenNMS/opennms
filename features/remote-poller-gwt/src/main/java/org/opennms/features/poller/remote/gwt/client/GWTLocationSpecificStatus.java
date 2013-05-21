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

package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>GWTLocationSpecificStatus class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTLocationSpecificStatus implements Serializable, IsSerializable {

	private static final long serialVersionUID = 3244726263014312000L;
	private Integer m_id;
	private GWTLocationMonitor m_locationMonitor;
	private GWTPollResult m_pollResult;
	private GWTMonitoredService m_monitoredService;
	
	/**
	 * <p>Constructor for GWTLocationSpecificStatus.</p>
	 */
	public GWTLocationSpecificStatus() {}

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
		return m_id;
	}
	/**
	 * <p>setId</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 */
	public void setId(final Integer id) {
		m_id = id;
	}
	/**
	 * <p>getLocationMonitor</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
	 */
	public GWTLocationMonitor getLocationMonitor() {
		return m_locationMonitor;
	}
	/**
	 * <p>setLocationMonitor</p>
	 *
	 * @param locationMonitor a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
	 */
	public void setLocationMonitor(final GWTLocationMonitor locationMonitor) {
		m_locationMonitor = locationMonitor;
	}
	/**
	 * <p>getMonitoredService</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
	 */
	public GWTMonitoredService getMonitoredService() {
		return m_monitoredService;
	}
	/**
	 * <p>setMonitoredService</p>
	 *
	 * @param monitoredService a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
	 */
	public void setMonitoredService(final GWTMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	/**
	 * <p>getPollResult</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 */
	public GWTPollResult getPollResult() {
		return m_pollResult;
	}
	/**
	 * <p>setPollResult</p>
	 *
	 * @param pollResult a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 */
	public void setPollResult(final GWTPollResult pollResult) {
		m_pollResult = pollResult;
	}
	/**
	 * <p>getPollTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	public Date getPollTime() {
		return m_pollResult == null? null : m_pollResult.getTimestamp();
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "GWTLocationSpecificStatus[id=" + m_id + ",locationMonitor=" + m_locationMonitor + ",monitoredService=" + m_monitoredService + ",pollResult=" + m_pollResult + "]";
	}
}
