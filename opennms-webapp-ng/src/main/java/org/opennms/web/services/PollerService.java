package org.opennms.web.services;

public interface PollerService {

	void poll(int nodeId, String ipAddr, int ifIndex, int serviceId, int demandPollId);

}
