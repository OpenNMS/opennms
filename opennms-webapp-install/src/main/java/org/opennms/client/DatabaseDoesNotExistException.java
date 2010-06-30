package org.opennms.client;

/**
 * This exception is used to signal the webapp that the database has not yet
 * been created in the database.
 *
 * @author ranger
 * @version $Id: $
 */
public class DatabaseDoesNotExistException extends Exception {
    private static final long serialVersionUID = 4078366981102852326L;
}
