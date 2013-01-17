package org.opennms.features.geocoder;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 3358715493486703594L;
    private float m_latitude;
    private float m_longitude;

    public Coordinates() {}
    public Coordinates(final String latLng) throws GeocoderException {
        setCoordinates(latLng);
    }
    public Coordinates(final float latitude, final float longitude) {
        setCoordinates(latitude, longitude);
    }

    protected void setCoordinates(final String latLng) throws GeocoderException {
        if (latLng == null) {
            throw new GeocoderException("Attempt to initialize a Coordinate with a null lat/lng string!");
        }

        final String[] separated = latLng.split(",");
        try {
            m_latitude = Float.valueOf(separated[0]).floatValue();
            m_longitude = Float.valueOf(separated[1]).floatValue();
        } catch (final NumberFormatException e) {
            throw new GeocoderException("Failed to parse lat/lng string '" + latLng + "'", e);
        }
    }

    protected void setCoordinates(final float latitude, final float longitude) {
        m_latitude = latitude;
        m_longitude = longitude;
    }

    public float getLatitude() {
        return m_latitude;
    }
    
    public float getLongitude() {
        return m_longitude;
    }

    @Override
    public String toString() {
        return m_latitude + "," + m_longitude;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(m_latitude);
        result = prime * result + Float.floatToIntBits(m_longitude);
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
        if (Float.floatToIntBits(m_latitude)  != Float.floatToIntBits(other.m_latitude))  return false;
        if (Float.floatToIntBits(m_longitude) != Float.floatToIntBits(other.m_longitude)) return false;
        return true;
    }

}
