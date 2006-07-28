package org.opennms.web.map;

/**
 * Thrown when a problem with configuration with the Maps Factories exists.
 * This error will typically be thrown when the class of a maps factory specified in
 * the property file 'appmap.properties' cannot be found or instantiated.
 */
public class MapsFactoryConfigurationError extends Error {

    /**
     * Create a new MapsFactoryConfigurationError with no detail mesage.
     */
    public MapsFactoryConfigurationError() {
    }

    /**
     * Create a new MapsFactoryConfigurationError with the String specified as an error message.
     * @param msg   The error message for the exception.
     */
    public MapsFactoryConfigurationError(String msg) {
        super(msg);
    }

    /**
     * Create a new MapsFactoryConfigurationError with the given Exception base cause and detail message.
     * @param msg   The detail message.
     * @param e     The exception to be encapsulated in a FactoryConfigurationError
     */
    public MapsFactoryConfigurationError(String msg, Exception e) {
        super(msg, e);
    }

    /**
     * Create a new MapsFactoryConfigurationError with a given Exception base cause of the error.
     * @param e     The exception to be encapsulated in a FactoryConfigurationError
     */
    public MapsFactoryConfigurationError(Exception e) {
        super(e);
    }

}
