package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.ServiceStatus;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationInfo implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;

	private String m_name;
	private String m_pollingPackage;
	private String m_area;
	private String m_geolocation;
	private String m_coordinates;
	private ServiceStatus m_status;

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

	public ServiceStatus getStatus() {
		return m_status;
	}

	public void setStatus(final ServiceStatus status) {
		m_status = status;
	}
}
