package org.opennms.web.map;




/**
 * Signals that an attempt to obtain the map denoted by a specified identifier has failed.
 */
public class MapNotFoundException extends MapsException {

    /**
     * Create a new MapNotFoundException with no detail message.
     */
    public MapNotFoundException() {
        super();
    }

    /**
     * Create a new MapNotFoundException with the String specified as an error message.
     * @param msg   The error message for the exception.
     */
    public MapNotFoundException(String msg) {
        super(msg);
    }

}
