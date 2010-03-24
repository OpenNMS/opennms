package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.GWT;

import de.novanic.eventservice.client.event.Event;

public class DefaultLocationListener extends BaseLocationListener {

	private final LocationManager m_locationManager;

	public DefaultLocationListener(final LocationManager manager) {
		m_locationManager = manager;
	}
	
	public void onLocationDelete(final DeleteLocation location) {
		if (location == null) {
			return;
		}

		m_locationManager.removeLocation(location);
	}

	public void onLocationUpdate(final UpdateLocations locations) {
		if (locations == null) {
			return;
		}
		m_locationManager.updateLocations(locations.getLocations());
	}

	public void onLocationUpdate(final UpdateLocation location) {
		if (location == null) {
			return;
		}
		m_locationManager.updateLocation(location);
	}
	
	public void onUpdateComplete(final UpdateComplete event) {
		if (event == null) {
			return;
		}
		m_locationManager.updateComplete();
	}
	public void onEvent(final Event event) {
		if (event == null) {
			return;
		}
		GWT.log("unhandled location event received: " + event.toString());
	}
}
