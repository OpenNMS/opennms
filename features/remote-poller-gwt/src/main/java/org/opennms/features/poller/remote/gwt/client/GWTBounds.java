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

import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;

/**
 * <p>GWTBounds class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTBounds {
    
    GWTLatLng m_northEastCorner;
    GWTLatLng m_southWestCorner;
    
    /**
     * <p>Constructor for GWTBounds.</p>
     *
     * @param southWestCorner a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     * @param northEastCorner a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public GWTBounds(GWTLatLng southWestCorner, GWTLatLng northEastCorner) {
        m_northEastCorner = northEastCorner;
        m_southWestCorner = southWestCorner;
    }
    
    /**
     * <p>Constructor for GWTBounds.</p>
     *
     * @param swLat a double.
     * @param swLng a double.
     * @param neLat a double.
     * @param neLng a double.
     */
    public GWTBounds(double swLat, double swLng, double neLat, double neLng) {
        this(new GWTLatLng(swLat, swLng), new GWTLatLng(neLat, neLng));
    }

    /**
     * <p>contains</p>
     *
     * @param coords a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     * @return a boolean.
     */
    public boolean contains(GWTLatLng coords) {
         return containsLongitude(coords.getLongitude()) && containsLatitude(coords.getLatitude());
    }

    private boolean containsLongitude(Double longitude) {
        if (m_southWestCorner.getLongitude() <= m_northEastCorner.getLongitude()) {
            return m_southWestCorner.getLongitude() <= longitude && longitude <= m_northEastCorner.getLongitude();
        } else {
            return !(m_northEastCorner.getLongitude() < longitude && longitude < m_southWestCorner.getLongitude());
        }
    }

    private boolean containsLatitude(Double latitude) {
        return m_southWestCorner.getLatitude() <= latitude && latitude <= m_northEastCorner.getLatitude();
    }
        
    /**
     * <p>contains</p>
     *
     * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     * @return a boolean.
     */
    public boolean contains(GWTBounds bounds) {
        return contains(bounds.getNorthEastCorner()) && contains(bounds.getSouthWestCorner());
    }

    /**
     * <p>getSouthWestCorner</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public GWTLatLng getSouthWestCorner() {
        return m_southWestCorner;
    }

    /**
     * <p>getNorthEastCorner</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public GWTLatLng getNorthEastCorner() {
        return m_northEastCorner;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof GWTBounds) {
            GWTBounds b = (GWTBounds)o;
            return m_southWestCorner.equals(b.m_southWestCorner) && m_northEastCorner.equals(b.m_northEastCorner);
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
    	return new HashCodeBuilder()
    		.append(m_southWestCorner)
    		.append(m_northEastCorner)
    		.toHashcode();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("((");
        bldr.append(m_southWestCorner.getCoordinates());
        bldr.append("), (");
        bldr.append(m_northEastCorner.getCoordinates());
        bldr.append("))");
        return bldr.toString();
    }
}
