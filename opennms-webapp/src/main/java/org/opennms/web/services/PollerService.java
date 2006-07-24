package org.opennms.web.services;

import org.opennms.netmgt.model.OnmsMonitoredService;

public interface PollerService {

	void poll(OnmsMonitoredService monSvc, int demandPollId);

}
