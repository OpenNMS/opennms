package org.opennms.client;

public class DatabaseAccessException extends Exception {

    private static final long serialVersionUID = 8422075723564280674L;

    public DatabaseAccessException() {
        super();
    }

    public DatabaseAccessException(String string, Throwable e) {
        super(string, e);
    }
}
