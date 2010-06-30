package org.opennms.netmgt.provision.persist;

/**
 * <p>NodeProvisionException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NodeProvisionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for NodeProvisionException.</p>
     */
    public NodeProvisionException() {
        super();
    }
    
    /**
     * <p>Constructor for NodeProvisionException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public NodeProvisionException(String message, Throwable cause) {
        super(message, cause);
    }

}
