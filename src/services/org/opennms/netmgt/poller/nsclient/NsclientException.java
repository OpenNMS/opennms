package org.opennms.netmgt.poller.nsclient;

/**
 * This object implements the internal exceptions used by the
 * <code>NsclientManager</code> system.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 */
public class NsclientException extends Exception {
    /**
     * Constructor.
     */
    public NsclientException() {
        super();
    }

    /**
     * Constructor, sets the message pertaining to the exception problem.
     * 
     * @param message
     *            the message pertaining to the exception problem.
     */
    public NsclientException(String message) {
        super(message);
    }

    /**
     * Constructor, sets the message pertaining to the exception problem and
     * the root cause exception (if applicable.)
     * 
     * @param message
     *            the message pertaining to the exception problem.
     * @param cause
     *            the exception that caused this exception to be generated.
     */
    public NsclientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor, sets the exception that caused this exception to be
     * generated.
     * 
     * @param cause
     *            the exception that caused this exception to be generated.
     */
    public NsclientException(Throwable cause) {
        super(cause);
    }

}
