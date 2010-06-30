package org.opennms.client;

/**
 * Thrown when the configuration file that contains the database settings
 * is corrupt, missing, or cannot be read properly.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseConfigFileException extends Exception {
    private static final long serialVersionUID = -2326544832848106581L;

    /**
     * <p>Constructor for DatabaseConfigFileException.</p>
     *
     * @deprecated Only for use by GWT serialization.
     */
    public DatabaseConfigFileException() {
        super();
    }

    /**
     * <p>Constructor for DatabaseConfigFileException.</p>
     *
     * @param string a {@link java.lang.String} object.
     */
    public DatabaseConfigFileException(String string) {
        super(string);
    }
}
