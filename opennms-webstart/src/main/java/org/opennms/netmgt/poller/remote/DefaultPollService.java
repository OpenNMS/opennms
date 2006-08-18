package org.opennms.netmgt.poller.remote;

import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.springframework.util.Assert;

public class DefaultPollService implements PollService {
	
	Map m_monitors;
	
	public void setMonitors(Map monitors) {
		m_monitors = monitors;
	}

	public PollStatus poll(OnmsMonitoredService monitoredService, Map monitorConfiguration) {
		ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(monitoredService.getServiceName());
		Assert.notNull(monitor, "Unable to find monitor for service "+monitoredService.getServiceName());
		
		
		
		//monitor.poll(monitoredService, monitorConfiguration, null);
		return null;
	}

}
