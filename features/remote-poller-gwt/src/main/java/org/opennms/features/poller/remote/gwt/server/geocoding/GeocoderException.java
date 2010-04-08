package org.opennms.features.poller.remote.gwt.server.geocoding;

public class GeocoderException extends Exception {
	private static final long serialVersionUID = 1L;

	public GeocoderException() {
		super();
	}

	public GeocoderException(String message) {
		super(message);
	}

	public GeocoderException(Throwable cause) {
		super(cause);
	}

	public GeocoderException(String message, Throwable cause) {
		super(message, cause);
	}

}
