package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("locationStatus")
public interface LocationStatusService extends RemoteService {
	void start();
	public LocationInfo getLocationInfo(final String locationName);
	public LocationDetails getLocationDetails(final String locationName);
	public ApplicationInfo getApplicationInfo(final String applicationName);
	public ApplicationDetails getApplicationDetails(final String applicationName);
}
