package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;

public interface PollService {
	
	PollStatus poll(OnmsMonitoredService monitoredService);

}
