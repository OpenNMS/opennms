package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.maps.client.overlay.Marker;

public class GoogleMapsLocation extends BaseLocation {
	private static final long serialVersionUID = 1L;
	private Marker m_marker;
	public GoogleMapsLocation() {
		super();
	}

	public GoogleMapsLocation(Location location) {
		super(location.getName(), location.getPollingPackageName(), location.getArea(), location.getGeolocation(), location.getLatLng(), location.getLocationMonitorState());
	}

	public GoogleMapsLocation(final String name, final String pollingPackageName, final String area, final String geolocation) {
		super(name, pollingPackageName, area, geolocation);
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

}