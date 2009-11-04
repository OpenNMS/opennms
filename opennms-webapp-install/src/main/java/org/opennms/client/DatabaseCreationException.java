package org.opennms.client;

public class DatabaseCreationException extends Exception {
    public DatabaseCreationException() {
        super();
    }

    public DatabaseCreationException(String string, Throwable e) {
        super(string, e);
    }
}
