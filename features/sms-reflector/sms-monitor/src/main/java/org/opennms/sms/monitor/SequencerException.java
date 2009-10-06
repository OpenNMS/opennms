package org.opennms.sms.monitor;

public class SequencerException extends Exception {
	private static final long serialVersionUID = 1L;

	public SequencerException() {
		super();
	}
	
	public SequencerException(String message) {
		super(message);
	}
	
	public SequencerException(String message, Throwable t) {
		super(t);
	}
}
