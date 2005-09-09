package org.opennms.web.map.view;

import org.opennms.web.map.MapsException;

/**
 * Signals that an attempt to obtain the node denoted by a specified identifier has failed.
 */
public class VElementNotFoundException extends MapsException {

    /**
     * Create a new NetworkNodeNotFoundException with no detail message.
     */
    public VElementNotFoundException() {
        super();
    }

    /**
     * Create a new NetworkNodeNotFoundException with the String specified as an error message.
     * @param msg   The error message for the exception.
     */
    public VElementNotFoundException(String msg) {
        super(msg);
    }

}
