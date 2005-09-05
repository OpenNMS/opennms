package org.opennms.netmgt.utils;

public class EventProxyException extends Exception {
    public EventProxyException() {
	super();
    }
    public EventProxyException(String message) {
	super(message);
    }
    public EventProxyException(String message, Throwable cause) {
	super(message, cause);
    }
    public EventProxyException(Throwable cause) {
	super(cause);
    }
}
