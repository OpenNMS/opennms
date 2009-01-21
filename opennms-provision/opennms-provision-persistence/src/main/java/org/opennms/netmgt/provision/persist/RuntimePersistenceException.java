package org.opennms.netmgt.provision.persist;

public class RuntimePersistenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RuntimePersistenceException(String message) {
        super(message);
    }

    public RuntimePersistenceException(Throwable cause) {
        super(cause);
    }

    public RuntimePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
