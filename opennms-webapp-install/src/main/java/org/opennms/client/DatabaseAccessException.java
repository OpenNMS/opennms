package org.opennms.client;

/**
 * Thrown when errors are encountered while fetching data out of the database.
 */
public class DatabaseAccessException extends Exception {

    private static final long serialVersionUID = 8422075723564280674L;

    /**
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseAccessException() {
        super();
    }

    public DatabaseAccessException(String string) {
        super(string);
    }

    public DatabaseAccessException(String string, Throwable e) {
        super(string, e);
    }
}
