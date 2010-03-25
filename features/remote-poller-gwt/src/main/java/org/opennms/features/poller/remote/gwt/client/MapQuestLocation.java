package org.opennms.features.poller.remote.gwt.client;

import com.googlecode.gwtmapquest.transaction.MQAPoi;

public class MapQuestLocation extends BaseLocation implements Location {
	private static final long serialVersionUID = 1L;
	private MQAPoi m_marker;

	public MapQuestLocation() {
		super();
	}

	public MapQuestLocation(Location location) {
		super(location.getName(), location.getPollingPackageName(), location.getArea(), location.getGeolocation(), location.getLocationMonitorState());
	}

	public MapQuestLocation(String name, String pollingPackageName, String area, String geolocation) {
		super(name, pollingPackageName, area, geolocation);
	}

	public MapQuestLocation(String name, String pollingPackageName, String area, String geolocation, LocationMonitorState lms) {
		super(name, pollingPackageName, area, geolocation, lms);
	}

	public MQAPoi getMarker() {
		return m_marker;
	}
	
	public void setMarker(MQAPoi marker) {
		m_marker = marker;
	}
}
