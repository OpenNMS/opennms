package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;

import com.google.gwt.core.client.GWT;

import de.novanic.eventservice.client.event.Event;

public class DefaultLocationListener implements LocationListener {

	private final LocationManager m_locationManager;

	public void apply(final Event event) {
		if (event == null) return;
		if (event instanceof MapRemoteEvent) {
			((MapRemoteEvent)event).dispatch(m_locationManager);
		} else {
			onEvent(event);
		}
	}

	public DefaultLocationListener(final LocationManager manager) {
		m_locationManager = manager;
	}

	public void onEvent(final Event event) {
		if (event == null) {
			return;
		}
		GWT.log("unhandled location event received: " + event.toString());
	}
}
