package org.opennms.features.poller.remote.gwt.server.geocoding;

import geo.google.GeoAddressStandardizer;
import geo.google.datamodel.GeoAddress;
import geo.google.datamodel.GeoCoordinate;

import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>GoogleMapsGeocoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GoogleMapsGeocoder implements Geocoder {
	private static final long DEFAULT_RATE = 10;
	private final GeoAddressStandardizer m_standardizer;

	/**
	 * <p>Constructor for GoogleMapsGeocoder.</p>
	 */
	public GoogleMapsGeocoder() {
		final String apiKey = System.getProperty("gwt.apikey");
		String rate = System.getProperty("gwt.geocoder.rate");
		if (rate != null) {
			m_standardizer = new GeoAddressStandardizer(apiKey, Long.valueOf(rate));
		} else {
			m_standardizer = new GeoAddressStandardizer(apiKey, DEFAULT_RATE);
		}

	}

	/** {@inheritDoc} */
	public GWTLatLng geocode(String geolocation) throws GeocoderException {
		try {
			List<GeoAddress> addresses = m_standardizer.standardizeToGeoAddresses(geolocation);
			if (addresses.size() > 0) {
				if (addresses.size() > 1) {
					LogUtils.warnf(this, "received more than one address for geolocation '%s', returning the first", geolocation);
				}
				return getLatLng(addresses.get(0).getCoordinate());
			}
			throw new GeocoderException("unable to find latitude/longitude for geolocation '" + geolocation + "'");
		} catch (Exception e) {
			LogUtils.infof(this, e, "unable to convert geolocation '%s'", geolocation);
			throw new GeocoderException(e);
		}
	}

	private GWTLatLng getLatLng(final GeoCoordinate geoCoordinate) {
		return new GWTLatLng(geoCoordinate.getLatitude(), geoCoordinate.getLongitude());
	}

}
