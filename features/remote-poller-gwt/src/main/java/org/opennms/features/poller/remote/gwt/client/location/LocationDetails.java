package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.ApplicationState;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>LocationDetails class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationDetails implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;

	private LocationMonitorState m_locationMonitorState;
	private ApplicationState m_applicationState;

	/**
	 * <p>Constructor for LocationDetails.</p>
	 */
	public LocationDetails() { }

    /**
     * <p>getLocationMonitorState</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationMonitorState} object.
     */
    public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}

	/**
	 * <p>setLocationMonitorState</p>
	 *
	 * @param lms a {@link org.opennms.features.poller.remote.gwt.client.LocationMonitorState} object.
	 */
	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}
	
	/**
	 * <p>getApplicationState</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationState} object.
	 */
	public ApplicationState getApplicationState() {
		return m_applicationState;
	}

	/**
	 * <p>setApplicationState</p>
	 *
	 * @param applicationState a {@link org.opennms.features.poller.remote.gwt.client.ApplicationState} object.
	 */
	public void setApplicationState(final ApplicationState applicationState) {
		m_applicationState = applicationState;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "LocationDetails[locationMonitorState=" + m_locationMonitorState + ",applicationState=" + m_applicationState + "]";
	}
}
