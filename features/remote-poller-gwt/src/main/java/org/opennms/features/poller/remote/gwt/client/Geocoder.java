package org.opennms.features.poller.remote.gwt.client;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface Geocoder {
	public void getLatLng(String address, AsyncCallback<GWTLatLng> callback);
}
