package org.opennms.netmgt.icmp;

import java.io.IOException;

public class IcmpMessengerIOException extends IOException {
    private static final long serialVersionUID = 4658370128592224097L;

    public IcmpMessengerIOException(final String message) {
        super(message);
    }
    
    public IcmpMessengerIOException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
    
    public IcmpMessengerIOException(final Throwable throwable) {
        super(throwable);
    }
}
