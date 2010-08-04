package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

/**
 * <p>LocationUpdatedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationUpdatedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;

	private LocationInfo m_locationInfo;

	/**
	 * <p>Constructor for LocationUpdatedRemoteEvent.</p>
	 */
	public LocationUpdatedRemoteEvent() {
	}

	/**
	 * <p>Constructor for LocationUpdatedRemoteEvent.</p>
	 *
	 * @param locationInfo a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public LocationUpdatedRemoteEvent(final LocationInfo locationInfo) {
		m_locationInfo = locationInfo;
	}

	/**
	 * <p>getLocationInfo</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public LocationInfo getLocationInfo() {
		return m_locationInfo;
	}

	/** {@inheritDoc} */
	public void dispatch(final MapRemoteEventHandler locationManager) {
		locationManager.updateLocation(m_locationInfo);
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "LocationUpdatedRemoteEvent[locationInfo=" + m_locationInfo + "]";
	}
}
