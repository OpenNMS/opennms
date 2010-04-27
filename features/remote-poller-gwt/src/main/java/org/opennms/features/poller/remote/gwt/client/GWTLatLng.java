/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTLatLng implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;

	private Double m_latitude;
	private Double m_longitude;

	public final static GWTLatLng getDefault() {
		return new GWTLatLng(35.7174,-79.1619);
	}

	public GWTLatLng() {
	    this(0.0, 0.0);
	}
	
	public GWTLatLng(Double latitude, Double longitude) {
		m_latitude = latitude;
		m_longitude = longitude;
	}


	public static GWTLatLng fromCoordinates(String coordinates) {
		final String[] coords = coordinates.split(",");
		return new GWTLatLng(Double.valueOf(coords[0]), Double.valueOf(coords[1]));
	}

	Double getLatitude() {
		return m_latitude;
	}
	Double getLongitude() {
		return m_longitude;
	}

	public String getCoordinates() {
		return m_latitude + "," + m_longitude;
	}

	public String toString() {
		return "GWTLatLng[lat=" + m_latitude + ",lon=" + m_longitude +"]";
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) return false;
		if (!(o instanceof GWTLatLng)) return false;
		final GWTLatLng that = (GWTLatLng)o;
		return (
			(this.getLongitude().equals(that.getLongitude())) &&
			(this.getLatitude().equals(that.getLatitude()))
		);
	}
	
	@Override
	public int hashCode() {
		return (m_latitude + "," + m_longitude).hashCode();
	}
}