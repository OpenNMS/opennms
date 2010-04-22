package org.opennms.features.poller.remote.gwt.client;


public class GWTMarker {
    
    private Location m_location;
    

    public GWTMarker(Location location) {
        setLocation(location);
    }


    private void setLocation(Location location) {
        m_location = location;
    }


    public Location getLocation() {
        return m_location;
    }


    String getImageURL() {
        return getLocation().getLocationInfo().getMarkerImageURL();
    }


    GWTLatLng getLatLng() {
        return getLocation().getLocationInfo().getLatLng();
    }


    String getName() {
        return getLocation().getLocationInfo().getName();
    }


    String getArea() {
        return getLocation().getLocationInfo().getArea();
    }


    Status getStatus() {
        return getLocation().getLocationInfo().getStatus();
    }
    
    
}
