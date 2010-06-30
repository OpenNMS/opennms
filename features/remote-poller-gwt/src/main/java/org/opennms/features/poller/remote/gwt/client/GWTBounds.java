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
