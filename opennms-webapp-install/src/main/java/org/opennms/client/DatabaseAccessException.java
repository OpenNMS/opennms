package org.opennms.client;

/**
 * Thrown when errors are encountered while fetching data out of the database.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseAccessException extends Exception {

    private static final long serialVersionUID = 8422075723564280674L;

    /**
     * <p>Constructor for DatabaseAccessException.</p>
     *
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseAccessException() {
        super();
    }

    /**
     * <p>Constructor for DatabaseAccessException.</p>
     *
     * @param string a {@link java.lang.String} object.
     */
    public DatabaseAccessException(String string) {
        super(string);
    }

    /**
     * <p>Constructor for DatabaseAccessException.</p>
     *
     * @param string a {@link java.lang.String} object.
     * @param e a {@link java.lang.Throwable} object.
     */
    public DatabaseAccessException(String string, Throwable e) {
        super(string, e);
    }
}
