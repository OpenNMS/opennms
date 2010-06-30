package org.opennms.client;

/**
 * Thrown when the database driver cannot be loaded. This usually indicates
 * a classpath problem where the database driver JAR has not been properly
 * included.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseDriverException extends Exception {
    private static final long serialVersionUID = -4320228591412167279L;
}
