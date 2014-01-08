package org.opennms.netmgt.config.api;

public class ConfigurationException extends Exception {
    private static final long serialVersionUID = -2814962912339861566L;

    public ConfigurationException() {
    }

    public ConfigurationException(final String message) {
        super(message);
    }

    public ConfigurationException(final Throwable cause) {
        super(cause);
    }

    public ConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
