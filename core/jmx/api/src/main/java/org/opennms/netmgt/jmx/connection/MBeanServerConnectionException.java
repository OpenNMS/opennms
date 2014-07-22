package org.opennms.netmgt.jmx.connection;

import java.io.IOException;

public class MBeanServerConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    public MBeanServerConnectionException(final String errorMessage) {
        super(errorMessage);
    }

    public MBeanServerConnectionException(final IOException ioException) {
        super(ioException);
    }

    public MBeanServerConnectionException(String errorMessage, Exception exception) {
        super(errorMessage, exception);
    }
}
