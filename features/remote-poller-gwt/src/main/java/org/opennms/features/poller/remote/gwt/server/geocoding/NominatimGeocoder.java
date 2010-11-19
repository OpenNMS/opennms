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
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

/**
 * <p>NominatimGeocoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NominatimGeocoder implements Geocoder {
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
				LogUtils.warnf(this, "more than one location returned for query: %s", geolocation);
			} else if (places.size() == 0) {
				throw new GeocoderException("Nominatim returned an OK status code, but no places");
			}
			final ElementTree place = places.get(0);

			Double latitude = Double.valueOf(place.getAttribute("lat"));
			Double longitude = Double.valueOf(place.getAttribute("lon"));
			return new GWTLatLng(latitude, longitude);
		} catch (GeocoderException e) {
			throw e;
		} catch (Exception e) {
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
