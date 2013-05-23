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
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>LocationInfo class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationInfo implements IsSerializable, Serializable, Comparable<LocationInfo> {

	private static final long serialVersionUID = 2001265865152467286L;

	private String m_name;
	private String m_area;
	private String m_geolocation;
	private String m_coordinates;
	private Long m_priority = 100L;
	private GWTMarkerState m_markerState;
	private StatusDetails m_statusDetails;
	private Set<String> m_tags;

	/**
	 * <p>Constructor for LocationInfo.</p>
	 */
	public LocationInfo() {}

	/**
	 * <p>Constructor for LocationInfo.</p>
	 *
	 * @param info a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public LocationInfo(final LocationInfo info) {
		this(info.getName(), info.getArea(), info.getGeolocation(), info.getCoordinates(), info.getPriority(), info.getMarkerState(), info.getStatusDetails(), info.getTags());
	}

	/**
	 * <p>Constructor for LocationInfo.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param area a {@link java.lang.String} object.
	 * @param geolocation a {@link java.lang.String} object.
	 * @param coordinates a {@link java.lang.String} object.
	 * @param priority a {@link java.lang.Long} object.
	 * @param marker a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
	 * @param statusDetails a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 * @param tags a {@link java.util.Set} object.
	 */
	public LocationInfo(final String name, final String area, final String geolocation, final String coordinates, final Long priority, final GWTMarkerState marker, final StatusDetails statusDetails, final Set<String> tags) {
		setName(name);
		setArea(area);
		setGeolocation(geolocation);
		setCoordinates(coordinates);
		setPriority(priority);
		setMarkerState(marker == null ? new GWTMarkerState(name, getLatLng(), statusDetails.getStatus()) : marker);
		setStatusDetails(statusDetails);
        setTags(tags);
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
	 * <p>getArea</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getArea() {
		return m_area;
	}

	/**
	 * <p>setArea</p>
	 *
	 * @param area a {@link java.lang.String} object.
	 */
	public void setArea(final String area) {
		m_area = area;
	}

	/**
	 * <p>getGeolocation</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGeolocation() {
		return m_geolocation;
	}

	/**
	 * <p>setGeolocation</p>
	 *
	 * @param geolocation a {@link java.lang.String} object.
	 */
	public void setGeolocation(final String geolocation) {
		m_geolocation = geolocation;
	}

	/**
	 * <p>getCoordinates</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCoordinates() {
		return m_coordinates;
	}

	/**
	 * <p>setCoordinates</p>
	 *
	 * @param coordinates a {@link java.lang.String} object.
	 */
	public void setCoordinates(final String coordinates) {
		m_coordinates = coordinates;
	}

	/**
	 * <p>getPriority</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getPriority() {
		return m_priority;
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
	 * <p>getTags</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getTags() {
		return m_tags;
	}
	
	/**
	 * <p>setTags</p>
	 *
	 * @param tags a {@link java.util.Set} object.
	 */
	public void setTags(final Set<String> tags) {
		m_tags = tags;
	}

	/**
	 * <p>getStatusDetails</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public StatusDetails getStatusDetails() {
		if (m_statusDetails == null) {
			return StatusDetails.unknown();
		}
		return m_statusDetails;
	}

	/**
	 * <p>setStatusDetails</p>
	 *
	 * @param status a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public void setStatusDetails(final StatusDetails status) {
		m_statusDetails = status;
		if (m_markerState != null && status != null) {
			m_markerState.setStatus(status.getStatus());
		}
	}

	/**
	 * <p>getMarkerState</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
	 */
	public GWTMarkerState getMarkerState() {
		return m_markerState;
	}

	/**
	 * <p>setMarkerState</p>
	 *
	 * @param markerState a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
	 */
	public void setMarkerState(final GWTMarkerState markerState) {
		m_markerState = markerState;
	}

	/**
	 * <p>getLatLng</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
	 */
	public GWTLatLng getLatLng() {
		return GWTLatLng.fromCoordinates(getCoordinates());
	}

	/**
	 * <p>isVisible</p>
	 *
	 * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
	 * @return a boolean.
	 */
	public boolean isVisible(final GWTBounds bounds) {
		return bounds.contains(getLatLng());
	}

	/** {@inheritDoc} */
        @Override
	public boolean equals(Object aThat) {
		if (this == aThat) return true;
		if (!(aThat instanceof LocationInfo)) return false;
		LocationInfo that = (LocationInfo)aThat;
		return EqualsUtil.areEqual(this.getName(), that.getName());
	}

	/**
	 * <p>hashCode</p>
	 *
	 * @return a int.
	 */
        @Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.getName())
			.toHashcode();
	}

	/**
	 * <p>compareTo</p>
	 *
	 * @param that a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 * @return a int.
	 */
        @Override
	public int compareTo(final LocationInfo that) {
		return new CompareToBuilder()
			.append(this.getStatusDetails(), that.getStatusDetails())
			.append(this.getPriority(), that.getPriority())
			.append(this.getName(), that.getName())
			.toComparison();
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "LocationInfo[name=" + m_name
			+ ",area=" + m_area
			+ ",geolocation=" + m_geolocation
			+ ",coordinates=" + m_coordinates
			+ ",priority=" + m_priority
			+ ",status=" + m_statusDetails.toString()
			+ ",marker=" + m_markerState
			+ ",tags=[" + StringUtils.join(m_tags, ",")
			+ "]";
	}

    /**
     * <p>getMarkerImageURL</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMarkerImageURL() {
    	return m_markerState.getImageURL();
    }

    public boolean hasTag(String selectedTag) {
        return getTags() != null && getTags().contains(selectedTag);
    }

    public Status getStatus() {
        return getStatusDetails().getStatus();
    }
}
