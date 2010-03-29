package org.opennms.features.poller.remote.gwt.server.geocoding;

public class GeocoderLookupException extends Exception {
	private static final long serialVersionUID = 1L;

	public GeocoderLookupException() {
		super();
	}

	public GeocoderLookupException(String message) {
		super(message);
	}

	public GeocoderLookupException(Throwable cause) {
		super(cause);
	}

	public GeocoderLookupException(String message, Throwable cause) {
		super(message, cause);
	}

}
