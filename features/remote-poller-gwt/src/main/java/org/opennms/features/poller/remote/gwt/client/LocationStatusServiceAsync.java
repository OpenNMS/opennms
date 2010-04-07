package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LocationStatusServiceAsync {
    void start(AsyncCallback<Void> anAsyncCallback);

	void getLocation(String locationName, AsyncCallback<Location> callback);

	void getApiKey(AsyncCallback<String> callback);

	void getLocationInfo(String locationName, AsyncCallback<LocationInfo> callback);

	void getLocationDetails(String locationName, AsyncCallback<LocationDetails> callback);
}
