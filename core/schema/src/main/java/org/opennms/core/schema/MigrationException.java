package org.opennms.core.schema;

/**
 * <p>MigrationException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MigrationException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for MigrationException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public MigrationException(String message) {
        super(message);
    }
    
    /**
     * <p>Constructor for MigrationException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public MigrationException(String message, Throwable t) {
        super(message, t);
    }
}
