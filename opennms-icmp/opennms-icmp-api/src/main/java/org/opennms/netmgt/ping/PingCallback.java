package org.opennms.netmgt.ping;

public interface PingCallback {

	void received(int id, int sequenceNumber, long startNanos, long endNanos);
	void timeout(int id, int sequenceNumber, long startNanos);
	void error(int id, int sequenceNumber, long startNanos, Throwable t);

}
