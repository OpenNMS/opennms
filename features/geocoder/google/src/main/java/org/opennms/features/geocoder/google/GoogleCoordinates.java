package org.opennms.features.geocoder.google;

import org.opennms.features.geocoder.CoordinateParseException;
import org.opennms.features.geocoder.Coordinates;

import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

public class GoogleCoordinates extends Coordinates {
    private static final long serialVersionUID = 5665827436870286281L;
    public GoogleCoordinates() {}
    public GoogleCoordinates(final GeocoderResult result) throws CoordinateParseException {
        super();

        if (result == null) {
            throw new CoordinateParseException("No valid geocoder result found!");
        }

        final GeocoderGeometry geometry = result.getGeometry();
        if (geometry == null) {
            throw new CoordinateParseException("No geometry found in Google geocoding response!");
        }

        final LatLng latLng = geometry.getLocation();
        if (latLng == null) {
            throw new CoordinateParseException("No latitude/longitude found in Google geocoding response!");
        }

        final String latLngString = latLng.toUrlValue();
        setCoordinates(latLngString);
    }
}
