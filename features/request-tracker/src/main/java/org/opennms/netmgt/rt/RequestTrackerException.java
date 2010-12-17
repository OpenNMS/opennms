package org.opennms.netmgt.rt;

public class RequestTrackerException extends Exception {
    private static final long serialVersionUID = 1L;

    public RequestTrackerException() {
        super();
    }

    public RequestTrackerException(final String message) {
        super(message);
    }

    public RequestTrackerException(Throwable t) {
        super(t);
    }

    public RequestTrackerException(String message, Throwable t) {
        super(message, t);
    }

}
