package org.opennms.jicmp.jna;

public class UnsupportedPlatformException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnsupportedPlatformException() {
        super();
    }

    public UnsupportedPlatformException(final String message) {
        super(message);
    }

    public UnsupportedPlatformException(final Throwable throwable) {
        super(throwable);
    }

    public UnsupportedPlatformException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
