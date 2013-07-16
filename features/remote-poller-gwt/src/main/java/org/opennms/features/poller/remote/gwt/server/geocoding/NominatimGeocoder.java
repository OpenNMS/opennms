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
 * <p>NominatimGeocoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NominatimGeocoder implements Geocoder {
    private static final Logger LOG = LoggerFactory.getLogger(NominatimGeocoder.class);
	private static final String GEOCODE_URL = "http://open.mapquestapi.com/nominatim/v1/search?format=xml";
	private static final HttpClient m_httpClient = new DefaultHttpClient();
	private String m_emailAddress;
	private String m_referer = null;

	/**
	 * <p>Constructor for NominatimGeocoder.</p>
	 *
	 * @throws org.opennms.features.poller.remote.gwt.server.geocoding.GeocoderException if any.
	 */
	public NominatimGeocoder() throws GeocoderException {
		this(System.getProperty("gwt.geocoder.email"));
	}
	
	/**
	 * <p>Constructor for NominatimGeocoder.</p>
	 *
	 * @param emailAddress a {@link java.lang.String} object.
	 * @throws org.opennms.features.poller.remote.gwt.server.geocoding.GeocoderException if any.
	 */
	public NominatimGeocoder(final String emailAddress) throws GeocoderException {
		m_emailAddress = emailAddress;
		m_referer = System.getProperty("gwt.geocoder.referer");

		if (m_emailAddress == null || m_emailAddress.equals("")) {
			throw new GeocoderException("you must configure gwt.geocoder.email to comply with the Nominatim terms of service (see http://wiki.openstreetmap.org/wiki/Nominatim)");
		}
	}

	/** {@inheritDoc} */
        @Override
	public GWTLatLng geocode(final String geolocation) throws GeocoderException {
		final HttpUriRequest method = new HttpGet(getUrl(geolocation));
		method.addHeader("User-Agent", "OpenNMS-MapquestGeocoder/1.0");
		if (m_referer != null) {
			method.addHeader("Referer", m_referer);
		}

		try {
			InputStream responseStream = m_httpClient.execute(method).getEntity().getContent();
			final ElementTree tree = ElementTree.fromStream(responseStream);
			if (tree == null) {
				throw new GeocoderException("an error occurred connecting to the Nominatim geocoding service (no XML tree was found)");
			}
			
			final List<ElementTree> places = tree.findAll("//place");
			if (places.size() > 1) {
				LOG.warn("more than one location returned for query: {}", geolocation);
			} else if (places.size() == 0) {
				throw new GeocoderException("Nominatim returned an OK status code, but no places");
			}
			final ElementTree place = places.get(0);

			Double latitude = Double.valueOf(place.getAttribute("lat"));
			Double longitude = Double.valueOf(place.getAttribute("lon"));
			return new GWTLatLng(latitude, longitude);
		} catch (GeocoderException e) {
			throw e;
		} catch (Throwable e) {
			throw new GeocoderException("unable to get lat/lng from Nominatim", e);
		}
	}

	private String getUrl(String geolocation) throws GeocoderException {
		try {
			return GEOCODE_URL + "&q=" + URLEncoder.encode(geolocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeocoderException("unable to URL-encode query string", e);
		}
	}

}
