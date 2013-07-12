package org.opennms.core.test.db;

public class TemporaryDatabaseException extends Exception {
    private static final long serialVersionUID = -6365926071506893518L;

    public TemporaryDatabaseException() {
        super();
    }

    public TemporaryDatabaseException(final String message) {
        super(message);
    }

    public TemporaryDatabaseException(final Throwable cause) {
        super(cause);
    }

    public TemporaryDatabaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
