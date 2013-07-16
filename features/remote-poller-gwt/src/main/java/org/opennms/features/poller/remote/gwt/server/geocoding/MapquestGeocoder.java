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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import net.simon04.jelementtree.ElementTree;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>MapquestGeocoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MapquestGeocoder implements Geocoder {
    private static final Logger LOG = LoggerFactory.getLogger(MapquestGeocoder.class);

	public enum Quality {
		COUNTRY,
		STATE,
		ZIP,
		COUNTY,
		ZIP_EXTENDED,
		CITY,
		STREET,
		INTERSECTION,
		ADDRESS,
		POINT
	}

	private static final String GEOCODE_URL = "http://www.mapquestapi.com/geocoding/v1/address?callback=renderGeocode&outFormat=xml";
	private static final HttpClient m_httpClient = new DefaultHttpClient();
	private String m_apiKey;
	private Quality m_minimumQuality;
	private String m_referer;


	/**
	 * <p>Constructor for MapquestGeocoder.</p>
	 */
	public MapquestGeocoder() {
		m_apiKey = System.getProperty("gwt.apikey");
		m_referer = System.getProperty("gwt.geocoder.referer");
		final String minimumQuality = System.getProperty("gwt.geocoder.minimumQuality");
		if (minimumQuality != null) {
			m_minimumQuality = Quality.valueOf(minimumQuality);
		}
	}

	/**
	 * <p>Constructor for MapquestGeocoder.</p>
	 *
	 * @param apiKey a {@link java.lang.String} object.
	 */
	public MapquestGeocoder(String apiKey) {
		this();
		m_apiKey = apiKey;
	}

	/** {@inheritDoc} */
        @Override
	public GWTLatLng geocode(final String geolocation) throws GeocoderException {
		final HttpUriRequest method = new HttpGet(getUrl(geolocation));
		method.addHeader("User-Agent", "OpenNMS-MapQuestGeocoder/1.0");
		method.addHeader("Referer", m_referer);

		try {
			InputStream responseStream = m_httpClient.execute(method).getEntity().getContent();
			final ElementTree tree = ElementTree.fromStream(responseStream);
			if (tree == null) {
				throw new GeocoderException("an error occurred connecting to the MapQuest geocoding service (no XML tree was found)");
			}

			final ElementTree statusCode = tree.find("//statusCode");
			if (statusCode == null || !statusCode.getText().equals("0")) {
				final String code = (statusCode == null? "unknown" : statusCode.getText());
				final ElementTree messageTree = tree.find("//message");
				final String message = (messageTree == null? "unknown" : messageTree.getText());
				throw new GeocoderException(
					"an error occurred when querying MapQuest (statusCode=" + code + ", message=" + message + ")"
				);
			}

			final List<ElementTree> locations = tree.findAll("//location");
			if (locations.size() > 1) {
				LOG.warn("more than one location returned for query: {}", geolocation);
			} else if (locations.size() == 0) {
				throw new GeocoderException("MapQuest returned an OK status code, but no locations");
			}
			final ElementTree location = locations.get(0);

			// first, check the quality
			if (m_minimumQuality != null) {
				final Quality geocodeQuality = Quality.valueOf(location.find("//geocodeQuality").getText().toUpperCase());
				if (geocodeQuality.compareTo(m_minimumQuality) < 0) {
					throw new GeocoderException("response did not meet minimum quality requirement (" + geocodeQuality + " is less specific than " + m_minimumQuality + ")");
				}
			}

			// then, extract the lat/lng
			final ElementTree latLng = location.find("//latLng");
			Double latitude = Double.valueOf(latLng.find("//lat").getText());
			Double longitude = Double.valueOf(latLng.find("//lng").getText());
			return new GWTLatLng(latitude, longitude);
		} catch (GeocoderException e) {
			throw e;
		} catch (Throwable e) {
			throw new GeocoderException("unable to get lat/lng from MapQuest", e);
		}
	}

	private String getUrl(String geolocation) throws GeocoderException {
		try {
			return GEOCODE_URL + "&key=" + m_apiKey + "&location=" + URLEncoder.encode(geolocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeocoderException("unable to URL-encode query string", e);
		}
	}

}
