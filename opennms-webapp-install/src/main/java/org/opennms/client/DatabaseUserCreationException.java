package org.opennms.client;

public class DatabaseUserCreationException extends Exception {
    private static final long serialVersionUID = -3565176106093629568L;

    public DatabaseUserCreationException() {
        super();
    }

    public DatabaseUserCreationException(String string, Throwable e) {
        super(string, e);
    }
}
