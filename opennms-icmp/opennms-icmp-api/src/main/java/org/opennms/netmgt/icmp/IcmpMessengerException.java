package org.opennms.netmgt.icmp;

public class IcmpMessengerException extends Exception {
    private static final long serialVersionUID = -4047232911308767171L;

    public IcmpMessengerException(final String message) {
        super(message);
    }
    
    public IcmpMessengerException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
    
    public IcmpMessengerException(final Throwable throwable) {
        super(throwable);
    }
}
