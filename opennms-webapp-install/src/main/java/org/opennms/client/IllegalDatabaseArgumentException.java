package org.opennms.client;

/**
 * Thrown when database connection parameters fail server-side validation.
 *
 * @author ranger
 * @version $Id: $
 */
public class IllegalDatabaseArgumentException extends Exception {
    private static final long serialVersionUID = 163110775752127867L;

    /**
     * <p>Constructor for IllegalDatabaseArgumentException.</p>
     *
     * @deprecated Only for use by GWT serialization.
     */
    public IllegalDatabaseArgumentException() {
        super();
    }

    /**
     * <p>Constructor for IllegalDatabaseArgumentException.</p>
     *
     * @param string a {@link java.lang.String} object.
     */
    public IllegalDatabaseArgumentException(String string) {
        super(string);
    }
}
