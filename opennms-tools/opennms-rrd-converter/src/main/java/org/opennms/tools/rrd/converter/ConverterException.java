package org.opennms.tools.rrd.converter;

public class ConverterException extends Exception {
    private static final long serialVersionUID = -9213323545762452287L;

    public ConverterException() {
        super();
    }

    public ConverterException(final String message) {
        super(message);
    }

    public ConverterException(final Throwable cause) {
        super(cause);
    }

    public ConverterException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
