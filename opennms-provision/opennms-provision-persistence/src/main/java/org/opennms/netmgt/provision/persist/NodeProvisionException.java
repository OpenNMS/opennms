package org.opennms.netmgt.provision.persist;

public class NodeProvisionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NodeProvisionException() {
        super();
    }
    
    public NodeProvisionException(String message, Throwable cause) {
        super(message, cause);
    }

}
