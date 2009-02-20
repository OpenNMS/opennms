package org.opennms.netmgt.provision.persist;

import org.springframework.dao.DataAccessException;

public class ForeignSourceRepositoryException extends DataAccessException {
    private static final long serialVersionUID = 1L;

    public ForeignSourceRepositoryException(String message) {
        super(message);
    }

    public ForeignSourceRepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
