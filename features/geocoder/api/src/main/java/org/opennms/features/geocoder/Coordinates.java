/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.geocoder;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = -7242869661237596209L;

    public static final String BAD_COORDINATES = Integer.MIN_VALUE + "," + Integer.MIN_VALUE;

    private Double m_longitude;
    private Double m_latitude;

    public Coordinates() {}
    public Coordinates(final String lonLat) throws GeocoderException {
        setCoordinates(lonLat);
    }
    public Coordinates(final double longitude, final double latitude) {
        setCoordinates(longitude, latitude);
    }

    public Coordinates(final float longitude, final float latitude) {
        setCoordinates(longitude, latitude);
    }

    public static Double[] splitCommaSeparatedDoubles(final String coordinateString) throws GeocoderException {
        final String[] separated = coordinateString.split(",");
        final Double[] coordinates;
        try {
            coordinates = new Double[] { Double.valueOf(separated[0]), Double.valueOf(separated[1]) };
        } catch (final NumberFormatException e) {
            throw new GeocoderException("Failed to parse coordinate string '" + coordinateString + "'", e);
        }
        return coordinates;
    }

    protected void setCoordinates(final String lonLat) throws GeocoderException {
        if (lonLat == null) {
            throw new GeocoderException("Attempt to initialize a Coordinate with a null lon/lat string!");
        }

        final Double[] coordinates = splitCommaSeparatedDoubles(lonLat);
        m_longitude = coordinates[0];
        m_latitude  = coordinates[1];
    }

    protected void setCoordinates(final double longitude, final double latitude) {
        m_longitude = longitude;
        m_latitude = latitude;
    }

    protected void setCoordinates(final float longitude, final float latitude) {
        m_longitude = Float.valueOf(longitude).doubleValue();
        m_latitude = Float.valueOf(latitude).doubleValue();
    }

    public double getLongitude() {
        return m_longitude == null ? 0.0 : m_longitude.doubleValue();
    }

    public double getLatitude() {
        return m_latitude == null ? 0.0 : m_latitude.doubleValue();
    }

    @Override
    public String toString() {
        return m_longitude + "," + m_latitude;
    }

    @Override
    public int hashCode() {
        final long prime = 31;
        long result = 1;
        result = prime * result + Double.doubleToLongBits(m_longitude);
        result = prime * result + Double.doubleToLongBits(m_latitude);
        return Long.valueOf(result).intValue();
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Coordinates)) {
            return false;
        }
        final Coordinates other = (Coordinates) obj;
        if (Double.doubleToLongBits(m_longitude) != Double.doubleToLongBits(other.m_longitude)) return false;
        if (Double.doubleToLongBits(m_latitude)  != Double.doubleToLongBits(other.m_latitude))  return false;
        return true;
    }
}
