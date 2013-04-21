package org.opennms.features.geocoder.google;

import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderService;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;

public class GoogleGeocoderService implements GeocoderService {
    final private Geocoder m_geocoder = new Geocoder();

    @Override
    public Coordinates getCoordinates(final String address) throws GeocoderException {
        final GeocoderRequest request = new GeocoderRequestBuilder().setAddress(address).setLanguage("en").getGeocoderRequest();
        final GeocodeResponse response = m_geocoder.geocode(request);

        switch (response.getStatus()) {
            case OK:
                return new GoogleCoordinates(response.getResults().get(0));
        case ERROR:
        case INVALID_REQUEST:
        case OVER_QUERY_LIMIT:
        case REQUEST_DENIED:
        case UNKNOWN_ERROR:
        case ZERO_RESULTS:
        default:
            throw new GeocoderException("Failed to get coordinates for " + address + " using Google Geocoder.  Response was: " + response.getStatus().toString());
        }
    }
}
