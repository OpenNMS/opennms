package org.opennms.netmgt.provision.persist;

import org.springframework.dao.DataAccessException;

/**
 * <p>ForeignSourceRepositoryException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ForeignSourceRepositoryException extends DataAccessException {
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for ForeignSourceRepositoryException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ForeignSourceRepositoryException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for ForeignSourceRepositoryException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param throwable a {@link java.lang.Throwable} object.
     */
    public ForeignSourceRepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
