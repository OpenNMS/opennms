package org.opennms.client;

/**
 * Thrown when errors are encountered when we attempt to create a database.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseCreationException extends Exception {
    private static final long serialVersionUID = 3610317296650991559L;

    /**
     * <p>Constructor for DatabaseCreationException.</p>
     *
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseCreationException() {
        super();
    }

    /**
     * <p>Constructor for DatabaseCreationException.</p>
     *
     * @param string a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public DatabaseCreationException(String string, Throwable e) {
        super(string, e);
    }
}
