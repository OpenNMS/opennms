package org.opennms.features.poller.remote.gwt.client.remoteevents;

import java.util.Collection;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

/**
 * <p>LocationsUpdatedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationsUpdatedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private Collection<LocationInfo> m_locations;

	/**
	 * <p>Constructor for LocationsUpdatedRemoteEvent.</p>
	 */
	public LocationsUpdatedRemoteEvent() {
	}

	/**
	 * <p>Constructor for LocationsUpdatedRemoteEvent.</p>
	 *
	 * @param locations a {@link java.util.Collection} object.
	 */
	public LocationsUpdatedRemoteEvent(final Collection<LocationInfo> locations) {
		m_locations = locations;
	}

	/**
	 * <p>getLocations</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<LocationInfo> getLocations() {
		return m_locations;
	}

	/**
	 * <p>setLocations</p>
	 *
	 * @param locations a {@link java.util.Collection} object.
	 */
	public void setLocations(final Collection<LocationInfo> locations) {
		m_locations = locations;
	}

	/** {@inheritDoc} */
	public void dispatch(final MapRemoteEventHandler locationManager) {
		locationManager.updateLocations(m_locations);
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "LocationsUpdatedRemoteEvent[locations=" + m_locations + "]";
	}
}
