package org.opennms.sms.monitor;

/**
 * <p>SequencerException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SequencerException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for SequencerException.</p>
	 */
	public SequencerException() {
		super();
	}
	
	/**
	 * <p>Constructor for SequencerException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public SequencerException(String message) {
		super(message);
	}
	
	/**
	 * <p>Constructor for SequencerException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param t a {@link java.lang.Throwable} object.
	 */
	public SequencerException(String message, Throwable t) {
		super(t);
	}
}
