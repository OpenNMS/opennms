package org.opennms.netmgt.poller.remote;

import java.text.ParseException;

public class DefaultPollerScheduleService implements PollerScheduleService {

	private PollerConfiguration m_pollerConfiguration;

	public MonitorServicePollDetails[] getServicePollDetails() throws ParseException {
		
		ServicePollConfiguration[] svcPollConfig = m_pollerConfiguration.getConfigurationForPoller("poller");
		
		MonitorServicePollDetails[] pollDetails = new MonitorServicePollDetails[svcPollConfig.length];
	
		for(int i = 0; i < svcPollConfig.length; i++) {
			pollDetails[i] = new MonitorServicePollDetails(svcPollConfig[i]);
		}
		return pollDetails;
	}

	public void setPollerConfiguration(PollerConfiguration pollerConfiguration) {
		m_pollerConfiguration = pollerConfiguration;
	}

}
