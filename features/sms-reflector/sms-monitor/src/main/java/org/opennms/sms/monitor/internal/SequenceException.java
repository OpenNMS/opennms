package org.opennms.sms.monitor.internal;

/**
 * <p>SequenceException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SequenceException extends Exception {
	/**
     * 
     */
    private static final long serialVersionUID = -6580922516386346610L;

    /**
	 * <p>Constructor for SequenceException.</p>
	 */
	public SequenceException() {
		super();
	}

	/**
	 * <p>Constructor for SequenceException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public SequenceException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for SequenceException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public SequenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor for SequenceException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public SequenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
