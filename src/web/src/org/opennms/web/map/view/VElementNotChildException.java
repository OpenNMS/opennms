package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;

/**
 * Signals that an attempt to obtain the node denoted by a specified identifier has failed.
 */
public class VElementNotChildException extends MapsException {

    /**
     * Create a new NetworkNodeNotFoundException with no detail message.
     */
    public VElementNotChildException() {
        super();
    }

    /**
     * Create a new NetworkNodeNotFoundException with the String specified as an error message.
     * @param msg   The error message for the exception.
     */
    public VElementNotChildException(String msg) {
        super(msg);
    }

}
