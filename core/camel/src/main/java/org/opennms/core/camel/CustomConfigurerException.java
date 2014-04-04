package org.opennms.core.camel;

public class CustomConfigurerException extends RuntimeException {
    private static final long serialVersionUID = 1507647478264045129L;

    public CustomConfigurerException() {
    }

    public CustomConfigurerException(final String message) {
        super(message);
    }

    public CustomConfigurerException(final Throwable t) {
        super(t);
    }

    public CustomConfigurerException(final String message, final Throwable t) {
        super(message, t);
    }

}
