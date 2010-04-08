package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.maps.client.overlay.Marker;

public class GoogleMapsLocation extends BaseLocation {
	private static final long serialVersionUID = 1L;
	private Marker m_marker;
	
	public GoogleMapsLocation() {
		super();
	}

	public GoogleMapsLocation(Location location) {
		super(location.getLocationInfo(), location.getLocationDetails());
	}

	@Override
	public String getImageURL() {
		if (m_marker != null) {
			return m_marker.getIcon().getImageURL();
		}
		return super.getImageURL();
	}

	public Marker getMarker() {
		return m_marker;
	}

	public void setMarker(Marker marker) {
		m_marker = marker;
		setLatLng(new GWTLatLng(marker.getLatLng().getLatitude(), marker.getLatLng().getLongitude()));
	}
	
	@Override
    protected String getAttributeText() {
    	return super.getAttributeText() + ",imageUrl=" + getImageURL() + ",marker=" + getMarker();
    }

    public String toString() {
		return "GoogleMapsLocation["+getAttributeText()+"]";
	}
}