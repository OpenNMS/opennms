package org.opennms.features.poller.remote.gwt.server.geocoding;

import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>Geocoder interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface Geocoder {

	/**
	 * <p>geocode</p>
	 *
	 * @param geolocation a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
	 * @throws org.opennms.features.poller.remote.gwt.server.geocoding.GeocoderException if any.
	 */
	public GWTLatLng geocode(String geolocation) throws GeocoderException;

}
