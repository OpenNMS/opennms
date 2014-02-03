package org.opennms.core.config.api;

public class ConfigurationResourceException extends Exception {
    private static final long serialVersionUID = -2814962912339861566L;

    public ConfigurationResourceException() {
    }

    public ConfigurationResourceException(final String message) {
        super(message);
    }

    public ConfigurationResourceException(final Throwable cause) {
        super(cause);
    }

    public ConfigurationResourceException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
