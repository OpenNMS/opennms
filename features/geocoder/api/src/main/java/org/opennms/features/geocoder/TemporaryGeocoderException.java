package org.opennms.features.geocoder;

public class TemporaryGeocoderException extends GeocoderException {
    private static final long serialVersionUID = 3223954497897809589L;

    public TemporaryGeocoderException() {
        super();
    }

    public TemporaryGeocoderException(final String message) {
        super(message);
    }

    public TemporaryGeocoderException(final Throwable throwable) {
        super(throwable);
    }

    public TemporaryGeocoderException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
