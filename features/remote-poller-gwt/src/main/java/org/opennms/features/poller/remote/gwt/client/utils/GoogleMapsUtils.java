package org.opennms.features.poller.remote.gwt.client.utils;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;

/**
 * <p>Abstract GoogleMapsUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class GoogleMapsUtils {

	/**
	 * <p>toGWTBounds</p>
	 *
	 * @param bounds a {@link com.google.gwt.maps.client.geom.LatLngBounds} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
	 */
	public static GWTBounds toGWTBounds(LatLngBounds bounds) {
        return new GWTBounds(GoogleMapsUtils.toGWTLatLng(bounds.getSouthWest()), GoogleMapsUtils.toGWTLatLng(bounds.getNorthEast()));
    }
    
    /**
     * <p>toLatLngBounds</p>
     *
     * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     * @return a {@link com.google.gwt.maps.client.geom.LatLngBounds} object.
     */
    public static LatLngBounds toLatLngBounds(GWTBounds bounds) {
        return LatLngBounds.newInstance(toLatLng(bounds.getSouthWestCorner()), toLatLng(bounds.getNorthEastCorner()));
    }

    /**
     * <p>toLatLng</p>
     *
     * @param latLng a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     * @return a {@link com.google.gwt.maps.client.geom.LatLng} object.
     */
    public static LatLng toLatLng(final GWTLatLng latLng) {
    	return LatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
    }

    /**
     * <p>toGWTLatLng</p>
     *
     * @param latLng a {@link com.google.gwt.maps.client.geom.LatLng} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public static GWTLatLng toGWTLatLng(final LatLng latLng) {
        return new GWTLatLng(latLng.getLatitude(), latLng.getLongitude());
    }

}
