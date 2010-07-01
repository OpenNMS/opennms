
/**
 * <p>InitializationException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;
public class InitializationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for InitializationException.</p>
	 */
	public InitializationException() {
		super();
	}

	/**
	 * <p>Constructor for InitializationException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor for InitializationException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public InitializationException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for InitializationException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public InitializationException(Throwable cause) {
		super(cause);
	}
	
}
