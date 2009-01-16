package org.opennms.netmgt.provision.persist;

public class ForeignSourceRepositoryException extends Exception {
    private static final long serialVersionUID = 1L;

    public ForeignSourceRepositoryException() {
        super();
    }

    public ForeignSourceRepositoryException(String message) {
        super(message);
    }

    public ForeignSourceRepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ForeignSourceRepositoryException(Throwable throwable) {
        super(throwable);
    }

}
