package org.opennms.netmgt.poller.remote;

import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.springframework.util.Assert;

public class DefaultPollService implements PollService {
	
	Map m_monitors;
	
	public void setMonitors(Map monitors) {
		m_monitors = monitors;
	}

	public PollStatus poll(PolledService polledService) {
        ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(polledService.getSvcName());
        Assert.notNull(monitor, "Unable to find monitor for service "+polledService.getSvcName());
        
        return monitor.poll(polledService, polledService.getMonitorConfiguration(), null);
    }

}
