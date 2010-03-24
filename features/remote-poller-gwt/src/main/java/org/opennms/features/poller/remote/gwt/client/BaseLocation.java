package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import de.novanic.eventservice.client.event.Event;

public class BaseLocation implements Event, Serializable, Location {
	private static final long serialVersionUID = 3L;
	private String m_name;
	private String m_area;
	private String m_pollingPackage;
	private String m_geolocation;
	private LocationMonitorState m_locationMonitorState;
//	private transient LatLng m_latLng;
//	private transient Marker m_marker;
	
	public BaseLocation() {
	}

	public BaseLocation(final String name, final String pollingPackageName, final String area, final String geolocation) {
		m_name = name;
		m_pollingPackage = pollingPackageName;
		m_area = area;
		m_geolocation = geolocation;
	}
	
	public BaseLocation(String name, String pollingPackageName, String area, String geolocation, LocationMonitorState lms) {
		this(name, pollingPackageName, area, geolocation);
		m_locationMonitorState = lms;
	}

	/* (non-Javadoc)
	 * @see org.opennms.features.poller.remote.gwt.client.Location#getName()
	 */
	public String getName() {
		return m_name;
	}
	public void setName(final String name) {
		m_name = name;
	}
	/* (non-Javadoc)
	 * @see org.opennms.features.poller.remote.gwt.client.Location#getPollingPackageName()
	 */
	public String getPollingPackageName() {
		return m_pollingPackage;
	}
	public void setPollingPackageName(String pollingPackageName) {
		m_pollingPackage = pollingPackageName;
	}
	/* (non-Javadoc)
	 * @see org.opennms.features.poller.remote.gwt.client.Location#getArea()
	 */
	public String getArea() {
		return m_area;
	}
	public void setArea(final String area) {
		m_area = area;
	}
	/* (non-Javadoc)
	 * @see org.opennms.features.poller.remote.gwt.client.Location#getGeolocation()
	 */
	public String getGeolocation() {
		return m_geolocation;
	}
	public void setGeolocation(final String geolocation) {
		m_geolocation = geolocation;
	}
	/* (non-Javadoc)
	 * @see org.opennms.features.poller.remote.gwt.client.Location#getLocationMonitorState()
	 */
	public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}
	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}

	/*
	public LatLng getLatLng() {
		return m_latLng;
	}
	public void setLatLng(final LatLng latLng) {
		m_latLng = latLng;
	}
	public void setMarker(final Marker marker) {
		m_marker = marker;
	}
	public Marker getMarker() {
		return m_marker;
	}
	*/

	/* (non-Javadoc)
	 * @see org.opennms.features.poller.remote.gwt.client.Location#getImageURL()
	 */
	public String getImageURL() {
		return null;
	}

	protected String getAttributeText() {
		return "name=" + getName() + ",pollingPackage=" + getPollingPackageName() + ",area=" + getArea() + ",geolocation=" + getGeolocation() + ",locationMonitorState=" + m_locationMonitorState;
	}

	public String toString() {
		return "BaseLocation["+getAttributeText()+"]";
	}
}
