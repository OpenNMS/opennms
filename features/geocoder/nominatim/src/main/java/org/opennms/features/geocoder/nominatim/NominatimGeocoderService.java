package org.opennms.features.geocoder.nominatim;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import net.simon04.jelementtree.ElementTree;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NominatimGeocoderService implements GeocoderService {
    private static final String GEOCODE_URL = "http://open.mapquestapi.com/nominatim/v1/search?format=xml";
    private static final HttpClient m_httpClient = new DefaultHttpClient();

    private String m_emailAddress;
    private String m_referer;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    public NominatimGeocoderService() {
    }
    
    public void onInit() {
        if (m_emailAddress == null || "".equals(m_emailAddress)) {
            throw new UnsupportedOperationException("You must specify an email address for the Nominatim geocoder!");
        }
    }

    @Override
    public Coordinates getCoordinates(final String address) throws GeocoderException {
        final HttpUriRequest method = new HttpGet(getUrl(address));
        method.addHeader("User-Agent", "OpenNMS-NominatimGeocoderService/1.0");
        if (m_referer != null && !"".equals(m_referer)) {
            method.addHeader("Referer", m_referer);
        }

        InputStream responseStream = null;
        try {
            responseStream = m_httpClient.execute(method).getEntity().getContent();
            final ElementTree tree = ElementTree.fromStream(responseStream);
            if (tree == null) {
                throw new GeocoderException("an error occurred connecting to the Nominatim geocoding service (no XML tree was found)");
            }

            final List<ElementTree> places = tree.findAll("//place");
            if (places.size() > 1) {
                m_log.warn("More than one location returned for query: {}", address);
            } else if (places.size() == 0) {
                throw new GeocoderException("Nominatim returned an OK status code, but no places");
            }
            final ElementTree place = places.get(0);

            final Float longitude = Float.valueOf(place.getAttribute("lon"));
            final Float latitude  = Float.valueOf(place.getAttribute("lat"));
            return new Coordinates(longitude, latitude);
        } catch (final GeocoderException e) {
            throw e;
        } catch (final Throwable e) {
            throw new GeocoderException("unable to get lon/lat from Nominatim", e);
        } finally {
            IOUtils.closeQuietly(responseStream);
        }
    }

    private String getUrl(final String geolocation) throws GeocoderException {
        try {
            return GEOCODE_URL + "&q=" + URLEncoder.encode(geolocation, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new GeocoderException("unable to URL-encode query string", e);
        }
    }

    public String getEmailAddress() {
        return m_emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        m_emailAddress = emailAddress;
    }

    public String getReferer() {
        return m_referer;
    }
    
    public void setReferer(final String referer) {
        m_referer = referer;
    }
}
