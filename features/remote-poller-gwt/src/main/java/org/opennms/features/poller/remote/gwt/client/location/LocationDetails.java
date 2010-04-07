package org.opennms.features.poller.remote.gwt.client.location;

import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationDetails implements IsSerializable {

    private LocationMonitorState m_locationMonitorState;

//	public LocationDetails(LocationDetails locationDetails) {
//        m_locationMonitorState = locationDetails.getLocationMonitorState();
//    }

    public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}

	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}

	public String toString() {
		return "LocationDetails[locationMonitorState=" + m_locationMonitorState + "]";
	}
}
