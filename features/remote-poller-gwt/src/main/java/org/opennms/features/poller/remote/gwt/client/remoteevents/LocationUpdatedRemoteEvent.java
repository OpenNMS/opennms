package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public class LocationUpdatedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;

	private LocationInfo m_locationInfo;

	public LocationUpdatedRemoteEvent() {
	}

	public LocationUpdatedRemoteEvent(final LocationInfo locationInfo) {
		m_locationInfo = locationInfo;
	}

	public LocationInfo getLocationInfo() {
		return m_locationInfo;
	}

	public void dispatch(final RemotePollerPresenter locationManager) {
		locationManager.updateLocation(m_locationInfo);
	}

	public String toString() {
		return "LocationUpdatedRemoteEvent[locationInfo=" + m_locationInfo + "]";
	}
}
