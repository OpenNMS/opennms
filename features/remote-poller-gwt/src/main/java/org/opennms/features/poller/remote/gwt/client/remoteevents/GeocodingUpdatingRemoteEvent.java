package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

import com.google.gwt.user.client.Window;

public class GeocodingUpdatingRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private int m_count = 0;
	private int m_size = 0;

	public GeocodingUpdatingRemoteEvent() {}

	public GeocodingUpdatingRemoteEvent(final int count, final int size) {
		m_count = count;
		m_size = size;
	}

	public void dispatch(final RemotePollerPresenter presenter) {
//		Window.alert("updating geocoding: " + m_count + "/" + m_size);
	}

}
