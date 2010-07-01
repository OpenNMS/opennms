package org.opennms.features.poller.remote.gwt.server.geocoding;

/**
 * <p>GeocoderException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GeocoderException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for GeocoderException.</p>
	 */
	public GeocoderException() {
		super();
	}

	/**
	 * <p>Constructor for GeocoderException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public GeocoderException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for GeocoderException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public GeocoderException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor for GeocoderException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public GeocoderException(String message, Throwable cause) {
		super(message, cause);
	}

}
