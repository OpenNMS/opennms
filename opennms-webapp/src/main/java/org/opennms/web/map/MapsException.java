package org.opennms.web.map;


/**
 * Generic maps exception.
 */
public class MapsException extends Exception {

    /**
     * Create a new MapsException with no detail mesage.
     */
    public MapsException() {
        super();
    }

    /**
     * Create a new MapsException with the String specified as an error message.
     * @param msg   The error message for the exception.
     */
    public MapsException(String msg) {
        super(msg);
    }

    /**
     * Create a new MapsException with the given Exception base cause and detail message.
     * @param msg   The detail message.
     * @param e     The exception to be encapsulated in a MapsException
     */
    public MapsException(String msg, Exception e) {
        super(msg, e);
    }

    /**
     * Create a new MapsException with a given Exception base cause of the exception.
     * @param e     The exception to be encapsulated in a MapsException
     */
    public MapsException(Exception e) {
        super(e);
    }

}
