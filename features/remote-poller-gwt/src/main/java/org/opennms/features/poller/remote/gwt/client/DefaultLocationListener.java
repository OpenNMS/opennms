package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;

import com.google.gwt.core.client.GWT;

import de.novanic.eventservice.client.event.Event;

/**
 * <p>DefaultLocationListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultLocationListener implements LocationListener {

	private final RemotePollerPresenter m_locationManager;

	/** {@inheritDoc} */
	public void apply(final Event event) {
		if (event == null) return;
		if (event instanceof MapRemoteEvent) {
			((MapRemoteEvent)event).dispatch(m_locationManager);
		} else {
			onEvent(event);
		}
	}

	/**
	 * <p>Constructor for DefaultLocationListener.</p>
	 *
	 * @param manager a {@link org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter} object.
	 */
	public DefaultLocationListener(final RemotePollerPresenter manager) {
		m_locationManager = manager;
	}

	/** {@inheritDoc} */
	public void onEvent(final Event event) {
		if (event == null) {
			return;
		}
		GWT.log("unhandled location event received: " + event.toString());
	}
}
