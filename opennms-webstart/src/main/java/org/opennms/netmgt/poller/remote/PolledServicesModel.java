package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.PollStatus;

public interface PolledServicesModel {

	public PolledService[] getPolledServices();

	public void setInitialPollTime(String polledServiceId, Date initialPollTime);

	public void updateServiceStatus(String polledServiceId, PollStatus pollStatus, Date pollTime);
	
	public void addConfigurationChangedListener(ConfigurationChangedListener l);
	public void removeConfigurationChangedListener(ConfigurationChangedListener l);

    public void addPolledServiceChangedListener(PolledServiceChangedListener l);
    public void removePolledServiceChangedListener(PolledServiceChangedListener l);
	

}
