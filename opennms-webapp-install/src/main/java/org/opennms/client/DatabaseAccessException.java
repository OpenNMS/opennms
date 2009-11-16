package org.opennms.client;

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
