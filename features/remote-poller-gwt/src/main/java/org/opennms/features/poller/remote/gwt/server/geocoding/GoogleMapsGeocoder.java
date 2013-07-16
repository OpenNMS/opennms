/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.server.geocoding;

import geo.google.GeoAddressStandardizer;
import geo.google.datamodel.GeoAddress;
import geo.google.datamodel.GeoCoordinate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>GoogleMapsGeocoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GoogleMapsGeocoder implements Geocoder {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleMapsGeocoder.class);
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
        @Override
	public GWTLatLng geocode(String geolocation) throws GeocoderException {
		try {
			List<GeoAddress> addresses = m_standardizer.standardizeToGeoAddresses(geolocation);
			if (addresses.size() > 0) {
				if (addresses.size() > 1) {
					LOG.warn("received more than one address for geolocation '{}', returning the first", geolocation);
				}
				return getLatLng(addresses.get(0).getCoordinate());
			}
			throw new GeocoderException("unable to find latitude/longitude for geolocation '" + geolocation + "'");
		} catch (Throwable e) {
			LOG.info("unable to convert geolocation '{}'", geolocation, e);
			throw new GeocoderException(e);
		}
	}

	private GWTLatLng getLatLng(final GeoCoordinate geoCoordinate) {
		return new GWTLatLng(geoCoordinate.getLatitude(), geoCoordinate.getLongitude());
	}

}
