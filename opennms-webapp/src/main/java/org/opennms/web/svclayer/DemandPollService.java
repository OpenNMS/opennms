package org.opennms.web.svclayer;

import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.utils.EventProxyException;

public interface DemandPollService {
	
	 DemandPoll pollMonitoredService(int nodeid, String ipAddr, int ifIndex, int serviceId) throws EventProxyException;
	 
	 DemandPoll getUpdatedResults(int resultId);

}
