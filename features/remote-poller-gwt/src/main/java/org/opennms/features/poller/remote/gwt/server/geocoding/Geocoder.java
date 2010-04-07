package org.opennms.features.poller.remote.gwt.server.geocoding;

import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

public interface Geocoder {

	public GWTLatLng geocode(String geolocation) throws GeocoderException;

}
