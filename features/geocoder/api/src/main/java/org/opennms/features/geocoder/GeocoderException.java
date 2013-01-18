package org.opennms.features.geocoder;

public class GeocoderException extends Exception {
    private static final long serialVersionUID = -7626277979425116924L;

    public GeocoderException() {
        super();
    }

    public GeocoderException(final String message) {
        super(message);
    }

    public GeocoderException(final Throwable t) {
        super(t);
    }

    public GeocoderException(final String messages, final Throwable t) {
        super(messages, t);
    }

}
