package org.opennms.netmgt.provision.adapters.link.endpoint;

public class EndPointStatusException extends Exception {
    private static final long serialVersionUID = 1L;

    public EndPointStatusException() {
        super();
    }
    
    public EndPointStatusException(String message) {
        super(message);
    }
    
    public EndPointStatusException(Throwable t) {
        super(t);
    }
    
    public EndPointStatusException(String message, Throwable t) {
        super(message, t);
    }
}
