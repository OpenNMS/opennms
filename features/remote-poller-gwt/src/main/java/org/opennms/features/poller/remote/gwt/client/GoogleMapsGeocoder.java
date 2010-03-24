package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GoogleMapsGeocoder implements org.opennms.features.poller.remote.gwt.client.Geocoder {
	private final Geocoder m_geocoder = new Geocoder();

	public void getLatLng(final String address, final AsyncCallback<GWTLatLng> callback) {
		m_geocoder.getLatLng(address, new LatLngCallback() {
			public void onSuccess(LatLng latLng) {
				final GWTLatLng gLatLng = new GWTLatLng(latLng.getLatitude(), latLng.getLongitude());
				callback.onSuccess(gLatLng);
			}
			public void onFailure() {
				callback.onFailure(new Throwable("unable to look up LatLng"));
			}
		});
	}

}
