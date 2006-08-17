package org.opennms.netmgt.poller.remote;

import javax.swing.table.TableModel;

import org.quartz.Scheduler;
import org.springframework.beans.factory.InitializingBean;

public class DefaultPollerScheduleService implements PollerScheduleService, InitializingBean {

	private PollerConfiguration m_pollerConfiguration;
	private PollService m_pollService;
	private String m_pollerName;
	private boolean m_initialized;

	public void setPollerConfiguration(PollerConfiguration pollerConfiguration) {
		m_pollerConfiguration = pollerConfiguration;
	}
	
	public void setPollService(PollService pollService) {
		m_pollService = pollService;
	}

	public void afterPropertiesSet() throws Exception {
		if (m_pollerConfiguration == null)
			throw new IllegalStateException("DefaultPollerScheduleService must have its pollerConfiguration property set");
		
		if (m_pollService == null)
			throw new IllegalStateException("DefaultPollerScheduleService must have its pollService propety set");
		
		m_initialized = true;
	}

	public TableModel getScheduleTableModel() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public void scheduleServicePolls(Scheduler scheduler) throws Exception {
		if (!m_initialized)
			afterPropertiesSet();
		
		ServicePollConfiguration[] servicePollConfigurations = m_pollerConfiguration.getConfigurationForPoller(m_pollerName);
		for (int i = 0; i < servicePollConfigurations.length; i++) {
			ServicePollConfiguration configuration = servicePollConfigurations[i];
			MonitorServicePollDetails pollDetails = new MonitorServicePollDetails(configuration, m_pollService);
			scheduler.scheduleJob(pollDetails, pollDetails.getTrigger());
		}
	}

	public String getPollerName() {
		return m_pollerName;
	}

	public void setPollerName(String pollerName) {
		m_pollerName = pollerName;
	}

}
