package org.opennms.client;

/**
 * Thrown when errors are encountered when we attempt to create a user in the database.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseUserCreationException extends Exception {
    private static final long serialVersionUID = -3565176106093629568L;

    /**
     * <p>Constructor for DatabaseUserCreationException.</p>
     *
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseUserCreationException() {
        super();
    }

    /**
     * <p>Constructor for DatabaseUserCreationException.</p>
     *
     * @param string a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public DatabaseUserCreationException(String string, Throwable e) {
        super(string, e);
    }
}
