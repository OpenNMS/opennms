package org.opennms.features.poller.remote.gwt.server.geocoding;

import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>NullGeocoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NullGeocoder implements Geocoder {

	/** {@inheritDoc} */
	public GWTLatLng geocode(String geolocation) throws GeocoderException {
		return GWTLatLng.getDefault();
	}

}
