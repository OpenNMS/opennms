package org.opennms.features.geocoder;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 2079876989978267336L;
    public static final String BAD_COORDINATES = Integer.MIN_VALUE + "," + Integer.MIN_VALUE;

    private float m_longitude;
    private float m_latitude;

    public Coordinates() {}
    public Coordinates(final String lonLat) throws GeocoderException {
        setCoordinates(lonLat);
    }
    public Coordinates(final float longitude, final float latitude) {
        setCoordinates(longitude, latitude);
    }

    public static Float[] splitCommaSeparatedFloats(final String coordinateString) throws GeocoderException {
        final String[] separated = coordinateString.split(",");
        final Float[] coordinates;
        try {
            coordinates = new Float[] { Float.valueOf(separated[0]), Float.valueOf(separated[1]) };
        } catch (final NumberFormatException e) {
            throw new GeocoderException("Failed to parse coordinate string '" + coordinateString + "'", e);
        }
        return coordinates;
    }

    protected void setCoordinates(final String lonLat) throws GeocoderException {
        if (lonLat == null) {
            throw new GeocoderException("Attempt to initialize a Coordinate with a null lon/lat string!");
        }

        final Float[] coordinates = splitCommaSeparatedFloats(lonLat);
        m_longitude = coordinates[0].floatValue();
        m_latitude  = coordinates[1].floatValue();
    }

    protected void setCoordinates(final float longitude, final float latitude) {
        m_longitude = longitude;
        m_latitude = latitude;
    }

    public float getLongitude() {
        return m_longitude;
    }

    public float getLatitude() {
        return m_latitude;
    }
    
    @Override
    public String toString() {
        return m_longitude + "," + m_latitude;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(m_longitude);
        result = prime * result + Float.floatToIntBits(m_latitude);
        return result;
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Coordinates)) {
            return false;
        }
        final Coordinates other = (Coordinates) obj;
        if (Float.floatToIntBits(m_longitude) != Float.floatToIntBits(other.m_longitude)) return false;
        if (Float.floatToIntBits(m_latitude)  != Float.floatToIntBits(other.m_latitude))  return false;
        return true;
    }

}
