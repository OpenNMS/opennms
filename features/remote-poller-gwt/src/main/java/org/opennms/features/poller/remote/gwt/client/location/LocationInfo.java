package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;
import org.opennms.features.poller.remote.gwt.client.Status;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationInfo implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;

	private String m_name;
	private String m_pollingPackage;
	private String m_area;
	private String m_geolocation;
	private String m_coordinates;
	private GWTMarkerState m_marker;
	private Status m_status;
	private Set<String> m_tags;
	
	public LocationInfo() {
	}

	public LocationInfo(final LocationInfo info) {
		this(info.getName(), info.getPollingPackageName(), info.getArea(), info.getGeolocation(), info.getCoordinates(), info.getMarker(), info.getStatus(), info.getTags());
	}

	public LocationInfo(final String name, final String pollingPackageName, final String area, final String geolocation, final String coordinates, GWTMarkerState marker, Status status, final Set<String> tags) {
		setName(name);
		setPollingPackageName(pollingPackageName);
		setArea(area);
		setGeolocation(geolocation);
		setCoordinates(coordinates);
		setTags(tags);
		setMarker(marker);
		setStatus(status);
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

	public Set<String> getTags() {
		return m_tags;
	}
	
	public void setTags(final Set<String> tags) {
		m_tags = tags;
	}

	public Status getStatus() {
		return m_status;
	}

	public void setStatus(final Status status) {
		m_status = status;
		if (m_marker != null) {
			m_marker.setStatus(status);
		}
	}

	public GWTMarkerState getMarker() {
		return m_marker;
	}

	public void setMarker(final GWTMarkerState marker) {
		m_marker = marker;
	}

	public GWTLatLng getLatLng() {
		return GWTLatLng.fromCoordinates(getCoordinates());
	}

	public boolean isVisible(final GWTBounds bounds) {
		return bounds.contains(getLatLng());
	}

	public String toString() {
		return "LocationInfo[name=" + m_name + ",polling package=" + m_pollingPackage
			+ ",area=" + m_area + ",geolocation=" + m_geolocation
			+ ",coordinates=" + m_coordinates
			+ ",status=" + m_status
			+ ",marker=" + m_marker + "]";
	}

    public String getMarkerImageURL() {
    	return m_marker.getImageURL();
    }
}
