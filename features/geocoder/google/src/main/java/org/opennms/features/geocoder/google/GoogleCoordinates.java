package org.opennms.features.geocoder.google;

import java.math.BigDecimal;

import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;

import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

public class GoogleCoordinates extends Coordinates {
    private static final long serialVersionUID = 5665827436870286281L;

    private static final int ROUND_HALF_EVEN = BigDecimal.ROUND_HALF_EVEN;
    private static final int PRECISION = 6;

    public GoogleCoordinates() {}
    public GoogleCoordinates(final GeocoderResult result) throws GeocoderException {
        super();

        if (result == null) {
            throw new GeocoderException("No valid geocoder result found!");
        }

        final GeocoderGeometry geometry = result.getGeometry();
        if (geometry == null) {
            throw new GeocoderException("No geometry found in Google geocoding response!");
        }

        final LatLng latLng = geometry.getLocation();
        if (latLng == null) {
            throw new GeocoderException("No latitude/longitude found in Google geocoding response!");
        }

        final String lonLatString = latLng.getLng().setScale(PRECISION, ROUND_HALF_EVEN).toString() + "," + latLng.getLat().setScale(6, ROUND_HALF_EVEN).toString();
        setCoordinates(lonLatString);
    }
}
