package org.opennms.netmgt.poller.remote;

import java.util.Collection;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

public interface PollService {
    
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators);
    
    public void initialize(PolledService polledService);
	
	public PollStatus poll(PolledService polledService);
    
    public void release(PolledService polledService);
    

}
