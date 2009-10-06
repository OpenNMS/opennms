package org.opennms.sms.phonebook;

public class PhonebookException extends Exception {
    private static final long serialVersionUID = 1L;

    public PhonebookException() {
        super();
    }

    public PhonebookException(String message) {
        super(message);
    }

    public PhonebookException(Throwable cause) {
        super(cause);
    }

    public PhonebookException(String message, Throwable cause) {
        super(message, cause);
    }

}
