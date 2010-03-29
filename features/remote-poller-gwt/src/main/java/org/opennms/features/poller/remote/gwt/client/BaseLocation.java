package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public class BaseLocation implements Event, IsSerializable, Location {
	private static final long serialVersionUID = 3L;
	private String m_name;
	private String m_area;
	private String m_pollingPackage;
	private GWTLatLng m_latLng;
	private String m_geolocation;
	private LocationMonitorState m_locationMonitorState;

	public BaseLocation() {
	}

	public BaseLocation(final String name, final String pollingPackageName, final String area, final String geolocation) {
		m_name = name;
		m_pollingPackage = pollingPackageName;
		m_area = area;
		m_geolocation = geolocation;
	}

	public BaseLocation(String name, String pollingPackageName, String area, String geolocation, GWTLatLng latLng, LocationMonitorState lms) {
		this(name, pollingPackageName, area, geolocation);
		m_latLng = latLng;
		m_locationMonitorState = lms;
	}

	public String getName() {
		return m_name;
	}
	public void setName(final String name) {
		m_name = name;
	}
	public String getPollingPackageName() {
		return m_pollingPackage;
	}
	public void setPollingPackageName(String pollingPackageName) {
		m_pollingPackage = pollingPackageName;
	}
	public String getArea() {
		return m_area;
	}
	public void setArea(final String area) {
		m_area = area;
	}
	public String getGeolocation() {
		return m_geolocation;
	}
	public void setGeolocation(final String geolocation) {
		m_geolocation = geolocation;
	}
	public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}
	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}
	public GWTLatLng getLatLng() {
		return m_latLng;
	}
	public void setLatLng(GWTLatLng latLng) {
		m_latLng = latLng;
	}

	public String getImageURL() {
		return null;
	}

	public void setImageUrl(String image) {
	}

	protected String getAttributeText() {
		return "name=" + getName() + ",pollingPackage=" + getPollingPackageName() + ",area=" + getArea() + ",geolocation=" + getGeolocation() + ",locationMonitorState=" + m_locationMonitorState;
	}

	public String toString() {
		return "BaseLocation["+getAttributeText()+"]";
	}
}
