package org.opennms.features.poller.remote.gwt.client.utils;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;

public abstract class GoogleMapsUtils {

	public static GWTBounds toGWTBounds(LatLngBounds bounds) {
        return new GWTBounds(GoogleMapsUtils.toGWTLatLng(bounds.getSouthWest()), GoogleMapsUtils.toGWTLatLng(bounds.getNorthEast()));
    }
    
    public static LatLngBounds toLatLngBounds(GWTBounds bounds) {
        return LatLngBounds.newInstance(toLatLng(bounds.getSouthWestCorner()), toLatLng(bounds.getNorthEastCorner()));
    }

    public static LatLng toLatLng(final GWTLatLng latLng) {
    	return LatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
    }

    public static GWTLatLng toGWTLatLng(final LatLng latLng) {
        return new GWTLatLng(latLng.getLatitude(), latLng.getLongitude());
    }

}
