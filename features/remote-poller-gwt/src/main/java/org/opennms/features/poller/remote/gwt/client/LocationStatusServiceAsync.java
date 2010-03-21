package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LocationStatusServiceAsync {
    void start(AsyncCallback<Void> anAsyncCallback);

	void getLocation(String locationName, AsyncCallback<Location> callback);

	void getApiKey(AsyncCallback<String> callback);
}
