package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.ApplicationState;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationDetails implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;

	private LocationMonitorState m_locationMonitorState;
	private ApplicationState m_applicationState;

	public LocationDetails() { }

    public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}

	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}
	
	public ApplicationState getApplicationState() {
		return m_applicationState;
	}

	public void setApplicationState(final ApplicationState applicationState) {
		m_applicationState = applicationState;
	}

	public String toString() {
		return "LocationDetails[locationMonitorState=" + m_locationMonitorState + ",applicationState=" + m_applicationState + "]";
	}
}
