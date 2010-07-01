package org.opennms.features.poller.remote.gwt.client.utils;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>BoundsBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class BoundsBuilder {
    
    Double neLat;
    Double neLng;
    Double swLat;
    Double swLng;
    
    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds() {
        return isEmpty() ? new GWTBounds(-90, -180, 90, 180) : new GWTBounds(swLat, swLng, neLat, neLng);
    }
    
    private boolean isEmpty() {
        return neLat == null;
    }
    
    /**
     * <p>extend</p>
     *
     * @param lat a double.
     * @param lng a double.
     */
    public void extend(double lat, double lng) {
        if (isEmpty()) {
            swLat = neLat = lat;
            swLng = neLng = lng;
       } else {
           swLat = Math.min(swLat, lat);
           neLat = Math.max(neLat, lat);
           
           if (!containsLongitude(lng)) {
               if (distanceEast(lng) < distanceWest(lng)) {
                   neLng = lng;
               } else {
                   swLng = lng;
               }
           }
           
       }
    }
    
    /**
     * <p>extend</p>
     *
     * @param coords a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public void extend(GWTLatLng coords) {
        extend(coords.getLatitude(), coords.getLongitude());
    }
    
    private boolean containsLongitude(double lng) {
        if (swLng <= neLng) {
            return swLng <= lng && lng <= neLng;
        } else {
            return !(neLng < lng && lng < swLng);
        }
    }


    
    /**
     * <p>distanceEast</p>
     *
     * @param lng a double.
     * @return a double.
     */
    public double distanceEast(double lng) {
        return lng > neLng ? lng - neLng : lng+360.0 - neLng;
    }
    
    /**
     * <p>distanceWest</p>
     *
     * @param lng a double.
     * @return a double.
     */
    public double distanceWest(double lng) {
        return swLng > lng ? swLng - lng : swLng + 360.0 - lng;
    }

}
