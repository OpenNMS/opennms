package org.opennms.features.poller.remote.gwt.client.remoteevents;

import java.util.Collection;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public class LocationsUpdatedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private Collection<LocationInfo> m_locations;

	public LocationsUpdatedRemoteEvent() {
	}

	public LocationsUpdatedRemoteEvent(final Collection<LocationInfo> locations) {
		m_locations = locations;
	}

	public Collection<LocationInfo> getLocations() {
		return m_locations;
	}

	public void setLocations(final Collection<LocationInfo> locations) {
		m_locations = locations;
	}

	public void dispatch(final RemotePollerPresenter locationManager) {
		for (LocationInfo location : m_locations) {
			locationManager.updateLocation(location);
		}
	}

	public String toString() {
		return "LocationsUpdatedRemoteEvent[locations=" + m_locations + "]";
	}
}
