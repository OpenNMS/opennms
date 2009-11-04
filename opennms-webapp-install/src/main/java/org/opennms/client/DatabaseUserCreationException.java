package org.opennms.client;

public class DatabaseUserCreationException extends Exception {
    public DatabaseUserCreationException() {
        super();
    }

    public DatabaseUserCreationException(String string, Throwable e) {
        super(string, e);
    }
}
