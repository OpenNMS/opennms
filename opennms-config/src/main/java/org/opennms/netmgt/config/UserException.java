package org.opennms.netmgt.config;

public class UserException extends Exception {
    private static final long serialVersionUID = -8070050126812541985L;

    public UserException(final String message) {
        super(message);
    }

    public UserException(final String message, final Throwable t) {
        super(message, t);
    }
    
    public UserException(final Throwable t) {
        super(t);
    }
}
