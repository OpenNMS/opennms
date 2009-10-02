package org.opennms.sms.monitor.internal;

public class SequenceException extends Exception {
	private static final long serialVersionUID = 1L;

	public SequenceException() {
		super();
	}

	public SequenceException(String message) {
		super(message);
	}

	public SequenceException(Throwable cause) {
		super(cause);
	}

	public SequenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
