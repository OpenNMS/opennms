package org.opennms.client;

/**
 * Thrown when errors are encountered when we attempt to create a user in the database.
 */
public class DatabaseUserCreationException extends Exception {
    private static final long serialVersionUID = -3565176106093629568L;

    /**
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseUserCreationException() {
        super();
    }

    public DatabaseUserCreationException(String string, Throwable e) {
        super(string, e);
    }
}
