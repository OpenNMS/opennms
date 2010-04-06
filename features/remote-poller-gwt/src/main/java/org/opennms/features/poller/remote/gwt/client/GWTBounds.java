package org.opennms.features.poller.remote.gwt.client;

public class GWTBounds {
    
    GWTLatLng m_northEastCorner;
    GWTLatLng m_southWestCorner;
    
    public GWTBounds(GWTLatLng southWestCorner, GWTLatLng northEastCorner) {
        m_northEastCorner = northEastCorner;
        m_southWestCorner = southWestCorner;
    }
    
    public GWTBounds(double swLat, double swLng, double neLat, double neLng) {
        this(new GWTLatLng(swLat, swLng), new GWTLatLng(neLat, neLng));
    }

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
        
    public boolean contains(GWTBounds bounds) {
        return contains(bounds.getNorthEastCorner()) && contains(bounds.getSouthWestCorner());
    }

    public GWTLatLng getSouthWestCorner() {
        return m_southWestCorner;
    }

    public GWTLatLng getNorthEastCorner() {
        return m_northEastCorner;
    }
    
    public boolean equals(Object o) {
        if (o instanceof GWTBounds) {
            GWTBounds b = (GWTBounds)o;
            return m_southWestCorner.equals(b.m_southWestCorner) && m_northEastCorner.equals(b.m_northEastCorner);
        }
        return false;
    }
    
    public int hashCode() {
        return m_southWestCorner.hashCode() * 31 + m_northEastCorner.hashCode();
    }
    
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
