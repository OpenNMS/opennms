package org.opennms.web.svclayer;

import org.opennms.netmgt.model.PollResult;

public interface DemandPollService {
	
	 PollResult pollMonitoredService(int nodeid, String ipAddr, int ifIndex, int serviceId);
	 
	 PollResult getUpdatedResults(int resultId);

}
