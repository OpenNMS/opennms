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
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>ApplicationInfo class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationInfo implements Serializable, IsSerializable, Comparable<ApplicationInfo> {
	private static final long serialVersionUID = -9015282422845077117L;
	private Integer m_id;
	private String m_name;
	private Set<GWTMonitoredService> m_services;
	private Set<String> m_locations;
	private StatusDetails m_statusDetails = StatusDetails.unknown();
	private Long m_priority = null;

	/**
	 * <p>Constructor for ApplicationInfo.</p>
	 */
	public ApplicationInfo() {
	}

	/**
	 * <p>Constructor for ApplicationInfo.</p>
	 *
	 * @param id a int.
	 * @param name a {@link java.lang.String} object.
	 * @param services a {@link java.util.Set} object.
	 * @param locationNames a {@link java.util.Set} object.
	 * @param statusDetails a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public ApplicationInfo(final int id, final String name, final Set<GWTMonitoredService> services, final Set<String> locationNames, final StatusDetails statusDetails) {
		m_id = id;
		m_name = name;
		m_services = services;
		m_locations = locationNames;
		m_statusDetails = statusDetails;
	}

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
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(final String name) {
		m_name = name;
	}
	/**
	 * <p>getServices</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<GWTMonitoredService> getServices() {
		return m_services;
	}
	/**
	 * <p>setServices</p>
	 *
	 * @param services a {@link java.util.Set} object.
	 */
	public void setServices(final Set<GWTMonitoredService> services) {
		m_services = services;
	}
	/**
	 * <p>getLocations</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getLocations() {
		return m_locations;
	}
	/**
	 * <p>setLocations</p>
	 *
	 * @param locations a {@link java.util.Set} object.
	 */
	public void setLocations(final Set<String> locations) {
		m_locations = locations;
	}
	/**
	 * <p>addLocation</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void addLocation(final String name) {
		m_locations.add(name);
	}
	/**
	 * <p>getStatusDetails</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public StatusDetails getStatusDetails() {
		return m_statusDetails;
	}
	/**
	 * <p>setStatusDetails</p>
	 *
	 * @param statusDetails a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public void setStatusDetails(final StatusDetails statusDetails) {
		m_statusDetails = statusDetails;
	}
	/**
	 * <p>getPriority</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getPriority() {
		return m_priority == null? 0L : m_priority;
	}
	/**
	 * <p>setPriority</p>
	 *
	 * @param priority a {@link java.lang.Long} object.
	 */
	public void setPriority(final Long priority) {
		m_priority = priority;
	}
	/**
	 * <p>getMarkerState</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
	 */
	public GWTMarkerState getMarkerState() {
		return new GWTMarkerState(m_name, null, m_statusDetails == null? Status.UNKNOWN : m_statusDetails.getStatus());
	}

	/** {@inheritDoc} */
        @Override
	public boolean equals(Object aThat) {
		if (this == aThat) return true;
		if (!(aThat instanceof ApplicationInfo)) return false;
		ApplicationInfo that = (ApplicationInfo)aThat;
		return
			EqualsUtil.areEqual(this.getId(), that.getId()) &&
			EqualsUtil.areEqual(this.getName(), that.getName())
		;
	}

	/**
	 * <p>hashCode</p>
	 *
	 * @return a int.
	 */
        @Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.getId())
			.append(this.getName())
			.toHashcode();
	}

	/**
	 * <p>compareTo</p>
	 *
	 * @param that a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 * @return a int.
	 */
        @Override
	public int compareTo(final ApplicationInfo that) {
		return new CompareToBuilder()
			.append(this.getStatusDetails(), that.getStatusDetails())
			.append(this.getPriority(), that.getPriority())
			.append(this.getName(), that.getName())
			.append(this.getId(), that.getId())
			.toComparison();
	}

	/**
	 * <p>summary</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String summary() {
		return "ApplicationInfo[id=" + m_id + ",name=" + m_name + "]";
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "ApplicationInfo[id=" + m_id
			+ ",name=" + m_name
			+ ",services=[" + StringUtils.join(m_services, ", ")
			+ "],locations=[" + StringUtils.join(m_locations, ", ")
			+ "],status=" + getStatusDetails()
			+ ",priority=" + getPriority() + "]";
	}
}
