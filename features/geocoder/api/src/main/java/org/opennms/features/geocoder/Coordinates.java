/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
