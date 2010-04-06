package org.opennms.features.poller.remote.gwt.client;

public class BoundsBuilder {
    
    Double neLat;
    Double neLng;
    Double swLat;
    Double swLng;
    
    public GWTBounds getBounds() {
        return isEmpty() ? new GWTBounds(-90, -180, 90, 180) : new GWTBounds(swLat, swLng, neLat, neLng);
    }
    
    private boolean isEmpty() {
        return neLat == null;
    }
    
    public void extend(double lat, double lng) {
        if (isEmpty()) {
            swLat = neLat = lat;
            swLng = neLng = lng;
       } else {
           swLat = Math.min(swLat, lat);
           neLat = Math.max(neLat, lat);
           swLng = Math.min(swLng, lng);
           neLng = Math.max(neLng, lng);
       }
    }
    
    public void extend(GWTLatLng coords) {
        extend(coords.getLatitude(), coords.getLongitude());
    }
    
    public double distanceEast(double lng) {
        return lng > neLng ? lng - neLng : lng+360.0 - neLng;
    }
    
    public double distanceWest(double lng) {
        return swLng > lng ? swLng - lng : swLng + 360.0 - lng;
    }

}
