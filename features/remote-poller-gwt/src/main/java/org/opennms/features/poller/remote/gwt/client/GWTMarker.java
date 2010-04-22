package org.opennms.features.poller.remote.gwt.client;


public class GWTMarker {
    
    private Location m_location;
    private Status m_status;
    private String m_name;
    private GWTLatLng m_latLng;
    

    public GWTMarker(Location location) {
        setLocation(location);
        setLatLng(location.getLocationInfo().getLatLng());
        setName(location.getLocationInfo().getName());
        setStatus(location.getLocationInfo().getStatus());
    }
    

    private void setStatus(Status status) {
        m_status = status;
    }
    
    public Status getStatus() {
        return m_status;
    }


    private void setName(String name) {
        m_name = name;
    }


    private void setLatLng(GWTLatLng latLng) {
        m_latLng = latLng;
    }


    private void setLocation(Location location) {
        m_location = location;
    }


    public Location getLocation() {
        return m_location;
    }


    public String getImageURL() {
        return "images/icon-" + getStatus() + ".png";
    }


    public GWTLatLng getLatLng() {
        return m_latLng;
    }


    public String getName() {
        return m_name;
    }
    
}
