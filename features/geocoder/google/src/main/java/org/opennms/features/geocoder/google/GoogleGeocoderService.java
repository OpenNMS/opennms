package org.opennms.features.geocoder.google;

import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geocoder.TemporaryGeocoderException;

import com.google.code.geocoder.AdvancedGeoCoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;

public class GoogleGeocoderService implements GeocoderService {
    final private AdvancedGeoCoder m_geocoder = new AdvancedGeoCoder();

    public GoogleGeocoderService() {
        final String proxyHost = System.getProperty("http.proxyHost");
        final Integer httpProxyPort = Integer.getInteger("http.proxyPort");

        if (proxyHost != null && httpProxyPort != null) {
            m_geocoder.getHttpClient().getHostConfiguration().setProxy(proxyHost, httpProxyPort);
        }
    }

    @Override
    public synchronized Coordinates getCoordinates(final String address) throws GeocoderException {
        final GeocoderRequest request = new GeocoderRequestBuilder().setAddress(address).setLanguage("en").getGeocoderRequest();
        final GeocodeResponse response = m_geocoder.geocode(request);

        switch (response.getStatus()) {
            case OK:
                return new GoogleCoordinates(response.getResults().get(0));
            case OVER_QUERY_LIMIT:
                throw new TemporaryGeocoderException("Failed to get coordinates for " + address + " using the Google Geocoder.  You have exceeded the daily usage limit.");
            case ERROR:
            case INVALID_REQUEST:
            case REQUEST_DENIED:
            case UNKNOWN_ERROR:
            case ZERO_RESULTS:
            default:
                throw new GeocoderException("Failed to get coordinates for " + address + " using Google Geocoder.  Response was: " + response.getStatus().toString());
        }
    }
}
