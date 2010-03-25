/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTLatLng implements IsSerializable {
	private static final long serialVersionUID = 1L;

	private Double m_latitude;
	private Double m_longitude;

	public final static GWTLatLng getDefault() {
		return new GWTLatLng(35.7174,-79.1619);
	}

	public GWTLatLng() {}
	
	public GWTLatLng(Double latitude, Double longitude) {
		m_latitude = latitude;
		m_longitude = longitude;
	}
	Double getLatitude() {
		return m_latitude;
	}
	Double getLongitude() {
		return m_longitude;
	}
}