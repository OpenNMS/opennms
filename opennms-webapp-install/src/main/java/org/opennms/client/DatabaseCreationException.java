package org.opennms.client;

/**
 * Thrown when errors are encountered when we attempt to create a database.
 */
public class DatabaseCreationException extends Exception {
    private static final long serialVersionUID = 3610317296650991559L;

    /**
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseCreationException() {
        super();
    }

    public DatabaseCreationException(String string, Throwable e) {
        super(string, e);
    }
}
