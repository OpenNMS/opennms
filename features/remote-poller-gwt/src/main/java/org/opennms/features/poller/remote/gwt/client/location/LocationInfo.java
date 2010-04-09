package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.Status;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationInfo implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;

	private String m_name;
	private String m_pollingPackage;
	private String m_area;
	private String m_geolocation;
	private String m_coordinates;
	private Status m_monitorStatus;
	private Status m_applicationStatus;
	
	public LocationInfo() {
	}
	
	public LocationInfo(String name, String pollingPackage, String area, String geolocation, String coordinates) {
	    m_name = name;
	    m_pollingPackage = pollingPackage;
	    m_area = area;
	    m_geolocation = geolocation;
	    m_coordinates = coordinates;
	}
	
	public LocationInfo(LocationInfo info) {
	    this(info.getName(), info.getPollingPackageName(), info.getArea(), info.getGeolocation(), info.getCoordinates());
	    setMonitorStatus(info.getMonitorStatus());
	    setApplicationStatus(info.getApplicationStatus());
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

	public void setPollingPackageName(final String pollingPackageName) {
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

	public String getCoordinates() {
		return m_coordinates;
	}

	public void setCoordinates(final String coordinates) {
		m_coordinates = coordinates;
	}

	public Status getMonitorStatus() {
		return m_monitorStatus;
	}

	public void setMonitorStatus(final Status status) {
		m_monitorStatus = status;
	}
	
	public Status getApplicationStatus() {
		return m_applicationStatus;
	}
	
	public void setApplicationStatus(final Status status) {
		m_applicationStatus = status;
	}

	public GWTLatLng getLatLng() {
		return GWTLatLng.fromCoordinates(getCoordinates());
	}

	public String toString() {
		return "LocationInfo[name=" + m_name + ",polling package=" + m_pollingPackage
			+ ",area=" + m_area + ",geolocation=" + m_geolocation
			+ ",coordinates=" + m_coordinates
			+ ",monitorStatus=" + m_monitorStatus + ",applicationStatus=" + m_applicationStatus + "]";
	}
}
