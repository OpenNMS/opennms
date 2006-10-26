package org.opennms.netmgt.poller.remote;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.springframework.util.Assert;

public class DefaultPollService implements PollService {
	
    Collection<ServiceMonitorLocator> m_locators;
    Map<String, ServiceMonitor> m_monitors;
	
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        m_locators = locators;
        
        Map<String, ServiceMonitor> monitors = new HashMap<String, ServiceMonitor>();
        for (ServiceMonitorLocator locator : locators) {
            monitors.put(locator.getServiceName(), locator.getServiceMonitor());
        }
        
        m_monitors = monitors;
    }
    
    public void initialize(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        monitor.initialize(polledService);
    }

    public PollStatus poll(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        return monitor.poll(polledService, polledService.getMonitorConfiguration());
    }

    private ServiceMonitor getServiceMonitor(PolledService polledService) {
        Assert.notNull(m_monitors, "setServiceMonitorLocators must be called before any other operations");
        ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(polledService.getSvcName());
        Assert.notNull(monitor, "Unable to find monitor for service "+polledService.getSvcName());
        return monitor;
    }

    // FIXME: this is never called but should be
    // also monitor.release() isn't called either
    public void release(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        monitor.release(polledService);
    }




}
