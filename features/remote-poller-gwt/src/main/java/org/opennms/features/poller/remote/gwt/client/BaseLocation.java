package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public class BaseLocation implements Event, IsSerializable, Location {
	private static final long serialVersionUID = 3L;

	private LocationInfo m_locationInfo;
	private LocationDetails m_locationDetails;

	public BaseLocation() {
	    this(new LocationInfo(), new LocationDetails());
	}
	
	public BaseLocation(LocationInfo locationInfo, LocationDetails locationDetails) {
	    m_locationInfo = locationInfo;
	    m_locationDetails = locationDetails;
	}

	public BaseLocation(LocationInfo info) {
	    this(info, new LocationDetails());
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

	public GWTLatLng getLatLng() {
		return GWTLatLng.fromCoordinates(m_locationInfo.getCoordinates());
	}

	public void setLatLng(GWTLatLng latLng) {
		m_locationInfo.setCoordinates(latLng.getCoordinates());
	}

	public String getImageURL() {
		return m_locationInfo.getImageURL();
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
		return "locationInfo=" + getLocationInfo() + ",locationDetails=" + getLocationDetails();
	}

	public String toString() {
		return this.getClass().getName()+"["+getAttributeText()+"]";
	}

	public int compareTo(Location o) {
		return this.getLocationInfo().getName().compareTo(o.getLocationInfo().getName());
	}

    protected boolean isVisible(GWTBounds bounds) {
        return bounds.contains(getLocationInfo().getLatLng());
    }

}
