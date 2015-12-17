package org.opennms.jicmp.jna;

public class InvalidSocketException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidSocketException() {
		super();
	}

	public InvalidSocketException(final Throwable t) {
		super(t);
	}

	public InvalidSocketException(final String message) {
		super(message);
	}

	public InvalidSocketException(final String message, final Throwable t) {
		super(message, t);
	}
}
