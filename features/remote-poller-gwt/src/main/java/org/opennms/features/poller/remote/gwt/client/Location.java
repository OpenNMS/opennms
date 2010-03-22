package org.opennms.features.poller.remote.gwt.client;


import java.io.Serializable;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public class Location implements Event, Serializable {
	private static final long serialVersionUID = 2L;
	public static final Domain LOCATION_EVENT_DOMAIN = DomainFactory.getDomain("location_event");
	private String m_name;
	private String m_area;
	private String m_pollingPackage;
	private String m_geolocation;
	private LocationMonitorState m_locationMonitorState;
	private transient LatLng m_latLng;
	private transient Marker m_marker;
	
	public Location() {
	}

	public Location(final String name, final String pollingPackageName, final String area, final String geolocation) {
		m_name = name;
		m_pollingPackage = pollingPackageName;
		m_area = area;
		m_geolocation = geolocation;
	}
	
	public Location(String name, String pollingPackageName, String area, String geolocation, LocationMonitorState lms) {
		this(name, pollingPackageName, area, geolocation);
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

	protected String getAttributeText() {
		return "name=" + getName() + ",pollingPackage=" + getPollingPackageName() + ",area=" + getArea() + ",geolocation=" + getGeolocation() + ",lat/lng=" + getLatLng() + ",locationMonitorState=" + m_locationMonitorState;
	}

	public String toString() {
		return "Location["+getAttributeText()+"]";
	}
}
