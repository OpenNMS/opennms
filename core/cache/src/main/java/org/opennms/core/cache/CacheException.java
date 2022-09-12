package org.opennms.core.cache;

public class CacheException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CacheException(final String message, final Exception cause) {
        super(message, cause);
    }

}
