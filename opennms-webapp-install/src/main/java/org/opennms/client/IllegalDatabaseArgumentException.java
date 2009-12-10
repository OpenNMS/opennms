package org.opennms.client;

/**
 * Thrown when database connection parameters fail server-side validation.
 */
public class IllegalDatabaseArgumentException extends Exception {
    private static final long serialVersionUID = 163110775752127867L;

    /**
     * @deprecated Only for use by GWT serialization.
     */
    public IllegalDatabaseArgumentException() {
        super();
    }

    public IllegalDatabaseArgumentException(String string) {
        super(string);
    }
}
