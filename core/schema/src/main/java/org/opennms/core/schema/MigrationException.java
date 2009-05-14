package org.opennms.core.schema;

public class MigrationException extends Exception {
    private static final long serialVersionUID = 1L;

    public MigrationException(String message) {
        super(message);
    }
    
    public MigrationException(String message, Throwable t) {
        super(message, t);
    }
}
