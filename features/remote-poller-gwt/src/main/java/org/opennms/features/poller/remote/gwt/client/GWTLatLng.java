/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

public class GWTLatLng implements Serializable {
	private static final long serialVersionUID = 1L;

	private double m_latitude;
	private double m_longitude;

	public GWTLatLng(double latitude, double longitude) {
		m_latitude = latitude;
		m_longitude = longitude;
	}
	double getLatitude() {
		return m_latitude;
	}
	void setLatitude(double latitude) {
		m_latitude = latitude;
	}
	double getLongitude() {
		return m_longitude;
	}
	void setLongitude(double longitude) {
		m_longitude = longitude;
	}
}