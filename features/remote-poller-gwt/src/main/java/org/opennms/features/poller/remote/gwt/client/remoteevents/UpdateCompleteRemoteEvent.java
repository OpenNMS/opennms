package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.LocationManager;

public class UpdateCompleteRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;

	public UpdateCompleteRemoteEvent() {
	}

	public void dispatch(LocationManager locationManager) {
		locationManager.updateComplete();
	}

	public String toString() {
		return "UpdateCompleteRemoteEvent[]";
	}
}
