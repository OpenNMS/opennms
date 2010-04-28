package org.opennms.features.poller.remote.gwt.client.location;

import java.io.Serializable;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationInfo implements IsSerializable, Serializable, Comparable<LocationInfo> {
	private static final long serialVersionUID = 1L;

	private String m_name;
	private String m_pollingPackage;
	private String m_area;
	private String m_geolocation;
	private String m_coordinates;
	private Long m_priority = 100L;
	private GWTMarkerState m_markerState;
	private Status m_status;
	private String m_reason;
	private Set<String> m_tags;

	public LocationInfo() {}

	public LocationInfo(final LocationInfo info) {
		this(info.getName(), info.getPollingPackageName(), info.getArea(), info.getGeolocation(), info.getCoordinates(), info.getPriority(), info.getMarkerState(), info.getStatus(), info.getReason(), info.getTags());
	}

	public LocationInfo(final String name, final String pollingPackageName, final String area, final String geolocation, final String coordinates, final Long priority, final GWTMarkerState marker, final Status status, final String reason, final Set<String> tags) {
		setName(name);
		setPollingPackageName(pollingPackageName);
		setArea(area);
		setGeolocation(geolocation);
		setCoordinates(coordinates);
		setPriority(priority);
		setTags(tags);
		setMarkerState(marker);
		setStatus(status);
		setReason(reason);
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

	public Status getStatus() {
		return m_status;
	}

	public void setStatus(final Status status) {
		m_status = status;
		if (m_markerState != null) {
			m_markerState.setStatus(status);
		}
	}

	public String getReason() {
		return m_reason;
	}

	public void setReason(final String reason) {
		m_reason = reason;
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
			.append(this.getStatus(), that.getStatus())
			.append(this.getPriority(), that.getPriority())
			.append(this.getName(), that.getName())
			.toComparison();
	}

	public String toString() {
		return "LocationInfo[name=" + m_name
			+ ",polling package=" + m_pollingPackage
			+ ",area=" + m_area
			+ ",geolocation=" + m_geolocation
			+ ",coordinates=" + m_coordinates
			+ ",priority=" + m_priority
			+ ",status=" + m_status.toString() + (m_status == null? "" : ("(" + m_reason + ")"))
			+ ",marker=" + m_markerState
			+ ",tags=[" + StringUtils.join(m_tags, ",")
			+ "]";
	}

    public String getMarkerImageURL() {
    	return m_markerState.getImageURL();
    }
}
