package org.opennms.netmgt.jmx.connection;

import java.io.IOException;

public class JmxServerConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    public JmxServerConnectionException(final String errorMessage) {
        super(errorMessage);
    }

    public JmxServerConnectionException(final IOException ioException) {
        super(ioException);
    }

    public JmxServerConnectionException(String errorMessage, Exception exception) {
        super(errorMessage, exception);
    }
}
