package org.opennms.netmgt.provision.adapters.link;

/**
 * <p>EndPointStatusException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EndPointStatusException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2737843487100746888L;

    /**
     * <p>Constructor for EndPointStatusException.</p>
     */
    public EndPointStatusException() {
        super();
    }
    
    /**
     * <p>Constructor for EndPointStatusException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public EndPointStatusException(String message) {
        super(message);
    }
    
    /**
     * <p>Constructor for EndPointStatusException.</p>
     *
     * @param t a {@link java.lang.Throwable} object.
     */
    public EndPointStatusException(Throwable t) {
        super(t);
    }
    
    /**
     * <p>Constructor for EndPointStatusException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public EndPointStatusException(String message, Throwable t) {
        super(message, t);
    }
}
