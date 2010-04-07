package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public class BaseLocation implements Event, IsSerializable, Location {
	private static final long serialVersionUID = 3L;

	private LocationInfo m_locationInfo = new LocationInfo();
	private LocationDetails m_locationDetails = new LocationDetails();

	private LocationMonitorState m_locationMonitorState;

	public BaseLocation() {
	}

	public BaseLocation(final String name, final String pollingPackageName, final String area, final String geolocation) {
		m_locationInfo.setName(name);
		m_locationInfo.setPollingPackageName(pollingPackageName);
		m_locationInfo.setArea(area);
		m_locationInfo.setGeolocation(geolocation);
	}

	public BaseLocation(String name, String pollingPackageName, String area, String geolocation, String coordinates, ServiceStatus status) {
		this(name, pollingPackageName, area, geolocation);
		m_locationInfo.setCoordinates(coordinates);
		m_locationInfo.setStatus(status);
	}
	public BaseLocation(String name, String pollingPackageName, String area, String geolocation, GWTLatLng latLng, LocationMonitorState lms) {
		this(name, pollingPackageName, area, geolocation, latLng.getCoordinates(), lms.getStatus());
		m_locationMonitorState = lms;
	}

	public String getName() {
		return m_locationInfo.getName();
	}

	public void setName(final String name) {
		m_locationInfo.setName(name);
	}

	public String getPollingPackageName() {
		return m_locationInfo.getPollingPackageName();
	}

	public void setPollingPackageName(final String pollingPackageName) {
		m_locationInfo.setPollingPackageName(pollingPackageName);
	}

	public String getArea() {
		return m_locationInfo.getArea();
	}

	public void setArea(final String area) {
		m_locationInfo.setArea(area);
	}

	public String getGeolocation() {
		return m_locationInfo.getGeolocation();
	}

	public void setGeolocation(final String geolocation) {
		m_locationInfo.setGeolocation(geolocation);
	}

	public LocationMonitorState getLocationMonitorState() {
		return m_locationMonitorState;
	}

	public void setLocationMonitorState(final LocationMonitorState lms) {
		m_locationMonitorState = lms;
	}
	
	public GWTLatLng getLatLng() {
		return GWTLatLng.fromCoordinates(m_locationInfo.getCoordinates());
	}

	public void setLatLng(GWTLatLng latLng) {
		m_locationInfo.setCoordinates(latLng.getCoordinates());
	}

	public String getImageURL() {
		return null;
	}

	public void setImageUrl(String image) {
	}

	public LocationInfo getLocationInfo() {
		return m_locationInfo;
	}

	public void setLocationInfo(final LocationInfo info) {
		m_locationInfo = info;
	}
	
	public LocationDetails getLocationDetails() {
		return m_locationDetails;
	}

	public void setLocationDetails(final LocationDetails details) {
		m_locationDetails = details;
	}

	protected String getAttributeText() {
		return "name=" + getName() + ",pollingPackage=" + getPollingPackageName() + ",area=" + getArea() + ",geolocation=" + getGeolocation() + ",lat/lng=" + getLatLng() + ",locationMonitorState=" + m_locationMonitorState;
	}

	public String toString() {
		return "BaseLocation["+getAttributeText()+"]";
	}

	public int compareTo(Location o) {
		return this.getName().compareTo(o.getName());
	}

}
