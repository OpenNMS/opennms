package org.opennms.netmgt.poller;

public interface PollerAPI {

	void poll(int nodeId, String ipAddr, int ifIndex, int serviceId, int pollResultId);

}
