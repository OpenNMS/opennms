package org.opennms.web.svclayer;

import org.opennms.netmgt.model.DemandPoll;

public interface DemandPollService {
	
	 DemandPoll pollMonitoredService(int nodeid, String ipAddr, int ifIndex, int serviceId);
	 
	 DemandPoll getUpdatedResults(int resultId);

}
