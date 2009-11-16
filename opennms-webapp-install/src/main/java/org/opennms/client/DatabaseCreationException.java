package org.opennms.client;

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
