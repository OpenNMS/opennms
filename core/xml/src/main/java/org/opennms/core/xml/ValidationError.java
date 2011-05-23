package org.opennms.core.xml;

public class ValidationError extends Exception {
	private static final long serialVersionUID = -2589727726619172738L;

	public ValidationError() {
		super();
	}

	public ValidationError(final String message) {
		super(message);
	}

	public ValidationError(final Throwable throwable) {
		super(throwable);
	}

	public ValidationError(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
