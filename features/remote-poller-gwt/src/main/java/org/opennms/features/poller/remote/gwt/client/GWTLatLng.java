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
 * <p>GWTLatLng class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
public class GWTLatLng implements IsSerializable, Serializable {

	private static final long serialVersionUID = -7197792607157902176L;

	private Double m_latitude;
	private Double m_longitude;

	/**
	 * <p>getDefault</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
	 */
	public final static GWTLatLng getDefault() {
		return new GWTLatLng(35.7174,-79.1619);
	}

	/**
	 * <p>Constructor for GWTLatLng.</p>
	 */
	public GWTLatLng() {
	    this(0.0, 0.0);
	}
	
	/**
	 * <p>Constructor for GWTLatLng.</p>
	 *
	 * @param latitude a {@link java.lang.Double} object.
	 * @param longitude a {@link java.lang.Double} object.
	 */
	public GWTLatLng(Double latitude, Double longitude) {
		m_latitude = latitude;
		m_longitude = longitude;
	}


	/**
	 * <p>fromCoordinates</p>
	 *
	 * @param coordinates a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
	 */
	public static GWTLatLng fromCoordinates(String coordinates) {
		final String[] coords = coordinates.split(",");
		return new GWTLatLng(Double.valueOf(coords[0]), Double.valueOf(coords[1]));
	}

	/**
	 * <p>getLatitude</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getLatitude() {
		return m_latitude;
	}
	/**
	 * <p>getLongitude</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getLongitude() {
		return m_longitude;
	}

	/**
	 * <p>getCoordinates</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCoordinates() {
		return m_latitude + "," + m_longitude;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "GWTLatLng[lat=" + m_latitude + ",lon=" + m_longitude +"]";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o == null) return false;
		if (!(o instanceof GWTLatLng)) return false;
		final GWTLatLng that = (GWTLatLng)o;
		return (
			(this.getLongitude().equals(that.getLongitude())) &&
			(this.getLatitude().equals(that.getLatitude()))
		);
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return (m_latitude + "," + m_longitude).hashCode();
	}
}
