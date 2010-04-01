package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("locationStatus")
public interface LocationStatusService extends RemoteService {
	void start();
	public String getApiKey();
	public Location getLocation(String locationName);
}
