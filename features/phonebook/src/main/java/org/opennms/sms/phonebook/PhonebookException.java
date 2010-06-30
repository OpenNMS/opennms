package org.opennms.sms.phonebook;

/**
 * <p>PhonebookException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PhonebookException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for PhonebookException.</p>
     */
    public PhonebookException() {
        super();
    }

    /**
     * <p>Constructor for PhonebookException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public PhonebookException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for PhonebookException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public PhonebookException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for PhonebookException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public PhonebookException(String message, Throwable cause) {
        super(message, cause);
    }

}
