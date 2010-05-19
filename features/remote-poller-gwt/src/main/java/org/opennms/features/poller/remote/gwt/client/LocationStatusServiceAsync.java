package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LocationStatusServiceAsync {
    void start(AsyncCallback<Void> anAsyncCallback);

	void getLocationInfo(final String locationName, final AsyncCallback<LocationInfo> callback);

	void getLocationDetails(final String locationName, final AsyncCallback<LocationDetails> callback);

	void getApplicationInfo(final String applicationName, final AsyncCallback<ApplicationInfo> callback);

	void getApplicationDetails(final String applicationName, final AsyncCallback<ApplicationDetails> callback);
}