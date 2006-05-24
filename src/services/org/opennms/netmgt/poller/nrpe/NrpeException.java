package org.opennms.netmgt.poller.nrpe;

public class NrpeException extends Exception {
	public NrpeException() {
		super();
	}
	
	public NrpeException(String message) {
		super(message);
	}
}
