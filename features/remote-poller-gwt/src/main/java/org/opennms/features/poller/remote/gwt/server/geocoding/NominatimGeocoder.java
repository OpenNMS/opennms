package org.opennms.features.poller.remote.gwt.server.geocoding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import net.simon04.jelementtree.ElementTree;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

public class NominatimGeocoder implements Geocoder {
	private static final String GEOCODE_URL = "http://nominatim.openstreetmap.org/search?format=xml";
	private static final HttpClient m_httpClient = new HttpClient();
	private String m_emailAddress;
	private String m_referer = null;

	public NominatimGeocoder() {
		this(System.getProperty("gwt.geocoder.email"));
	}
	
	public NominatimGeocoder(final String emailAddress) {
		m_emailAddress = emailAddress;
		m_referer = System.getProperty("gwt.geocoder.referer");
	}

	public GWTLatLng geocode(final String geolocation) throws GeocoderLookupException {
		if (m_emailAddress == null || m_emailAddress.equals("")) {
			throw new GeocoderLookupException("you must configure gwt.geocoder.email to comply with the Nominatim terms of service (see http://wiki.openstreetmap.org/wiki/Nominatim)");
		}

		final HttpMethod method = new GetMethod(getUrl(geolocation));
		method.addRequestHeader("User-Agent", "OpenNMS-MapquestGeocoder/1.0");
		if (m_referer != null) {
			method.addRequestHeader("Referer", m_referer);
		}

		try {
			m_httpClient.executeMethod(method);
			final ElementTree tree = ElementTree.fromStream(method.getResponseBodyAsStream());
			if (tree == null) {
				throw new GeocoderLookupException("an error occurred connecting to the Nominatim geocoding service (no XML tree was found)");
			}
			
			final List<ElementTree> places = tree.findAll("//place");
			if (places.size() > 1) {
				LogUtils.warnf(this, "more than one location returned for query: %s", geolocation);
			} else if (places.size() == 0) {
				throw new GeocoderLookupException("Nominatim returned an OK status code, but no places");
			}
			final ElementTree place = places.get(0);

			Double latitude = Double.valueOf(place.getAttribute("lat"));
			Double longitude = Double.valueOf(place.getAttribute("lon"));
			return new GWTLatLng(latitude, longitude);
		} catch (GeocoderLookupException e) {
			throw e;
		} catch (Exception e) {
			throw new GeocoderLookupException("unable to get lat/lng from Nominatim", e);
		}
	}

	private String getUrl(String geolocation) throws GeocoderLookupException {
		try {
			return GEOCODE_URL + "&q=" + URLEncoder.encode(geolocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeocoderLookupException("unable to URL-encode query string", e);
		}
	}

}
