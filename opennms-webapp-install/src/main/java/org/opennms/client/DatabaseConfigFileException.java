package org.opennms.client;

/**
 * Thrown when the configuration file that contains the database settings
 * is corrupt, missing, or cannot be read properly.
 */
public class DatabaseConfigFileException extends Exception {
    private static final long serialVersionUID = -2326544832848106581L;

    /**
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseConfigFileException() {
        super();
    }

    public DatabaseConfigFileException(String string) {
        super(string);
    }
}
