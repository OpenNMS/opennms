package org.opennms.core.test;

public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfigurationException() { }

    public ConfigurationException(final String message) {
        super(message);
    }

    public ConfigurationException(final String message, final Throwable t) {
        super(message, t);
    }
}
