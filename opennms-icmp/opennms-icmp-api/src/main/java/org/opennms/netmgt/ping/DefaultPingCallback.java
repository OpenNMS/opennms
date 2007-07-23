package org.opennms.netmgt.ping;

public class DefaultPingCallback implements PingCallback {

	public void error(int id, int sequenceNumber, long startNanos, Throwable t) {
		throw new UnsupportedOperationException("error not yet implemented");
	}

	public void received(int id, int sequenceNumber, long startNanos,
			long endNanos) {
		throw new UnsupportedOperationException("received not yet implemented");
	}

	public void timeout(int id, int sequenceNumber, long startNanos) {
		throw new UnsupportedOperationException("timeout not yet implemented");
	}

}
