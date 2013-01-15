package org.opennms.features.geocoder;

public class CoordinateParseException extends Exception {
    private static final long serialVersionUID = -7626277979425116924L;

    public CoordinateParseException() {
        super();
    }

    public CoordinateParseException(final String message) {
        super(message);
    }

    public CoordinateParseException(final Throwable t) {
        super(t);
    }

    public CoordinateParseException(final String messages, final Throwable t) {
        super(messages, t);
    }

}
