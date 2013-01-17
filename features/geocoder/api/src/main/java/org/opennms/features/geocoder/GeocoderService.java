package org.opennms.features.geocoder;

public interface GeocoderService {
    public Coordinates getCoordinates(final String address) throws GeocoderException;
}
