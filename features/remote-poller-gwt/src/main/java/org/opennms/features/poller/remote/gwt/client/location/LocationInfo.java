package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationInfo implements IsSerializable, Serializable, Comparable<LocationInfo> {
	private static final long serialVersionUID = 1L;

	private String m_name;
	private String m_area;
	private String m_geolocation;
	private String m_coordinates;
	private Long m_priority = 100L;
	private GWTMarkerState m_markerState;
	private StatusDetails m_statusDetails;
	private Set<String> m_tags;

	public LocationInfo() {}

	public LocationInfo(final LocationInfo info) {
		this(info.getName(), info.getArea(), info.getGeolocation(), info.getCoordinates(), info.getPriority(), info.getMarkerState(), info.getStatusDetails(), info.getTags());
	}

	public LocationInfo(final String name, final String area, final String geolocation, final String coordinates, final Long priority, final GWTMarkerState marker, final StatusDetails statusDetails, final Set<String> tags) {
		setName(name);
		setArea(area);
		setGeolocation(geolocation);
		setCoordinates(coordinates);
		setPriority(priority);
		setMarkerState(marker);
		setStatusDetails(statusDetails);
        setTags(tags);
	}

	public String getName() {
		return m_name;
	}

	public void setName(final String name) {
		m_name = name;
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

	public Long getPriority() {
		return m_priority;
	}

	public void setPriority(final Long priority) {
		m_priority = priority;
	}

	public Set<String> getTags() {
		return m_tags;
	}
	
	public void setTags(final Set<String> tags) {
		m_tags = tags;
	}

	public StatusDetails getStatusDetails() {
		if (m_statusDetails == null) {
			return StatusDetails.uninitialized();
		}
		return m_statusDetails;
	}

	public void setStatusDetails(final StatusDetails status) {
		m_statusDetails = status;
		if (m_markerState != null && status != null) {
			m_markerState.setStatus(status.getStatus());
		}
	}

	public GWTMarkerState getMarkerState() {
		return m_markerState;
	}

	public void setMarkerState(final GWTMarkerState markerState) {
		m_markerState = markerState;
	}

	public GWTLatLng getLatLng() {
		return GWTLatLng.fromCoordinates(getCoordinates());
	}

	public boolean isVisible(final GWTBounds bounds) {
		return bounds.contains(getLatLng());
	}

	public boolean equals(Object aThat) {
		if (this == aThat) return true;
		if (!(aThat instanceof LocationInfo)) return false;
		LocationInfo that = (LocationInfo)aThat;
		return EqualsUtil.areEqual(this.getName(), that.getName());
	}

	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.getName())
			.toHashcode();
	}

	public int compareTo(final LocationInfo that) {
		return new CompareToBuilder()
			.append(this.getStatusDetails(), that.getStatusDetails())
			.append(this.getPriority(), that.getPriority())
			.append(this.getName(), that.getName())
			.toComparison();
	}

	public String toString() {
		return "LocationInfo[name=" + m_name
			+ ",area=" + m_area
			+ ",geolocation=" + m_geolocation
			+ ",coordinates=" + m_coordinates
			+ ",priority=" + m_priority
			+ ",status=" + m_statusDetails.toString()
			+ ",marker=" + m_markerState
			+ ",tags=[" + StringUtils.join(m_tags, ",")
			+ "]";
	}

    public String getMarkerImageURL() {
    	return m_markerState.getImageURL();
    }
}
