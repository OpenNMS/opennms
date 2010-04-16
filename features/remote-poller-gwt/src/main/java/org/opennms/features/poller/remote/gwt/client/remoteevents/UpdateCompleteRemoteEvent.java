package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

public class UpdateCompleteRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;

	public UpdateCompleteRemoteEvent() {
	}

	public void dispatch(RemotePollerPresenter locationManager) {
		locationManager.updateComplete();
	}

	public String toString() {
		return "UpdateCompleteRemoteEvent[]";
	}
}
