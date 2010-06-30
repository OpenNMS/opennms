package org.opennms.netmgt.provision.persist;

/**
 * <p>RuntimePersistenceException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RuntimePersistenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public RuntimePersistenceException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public RuntimePersistenceException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public RuntimePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
