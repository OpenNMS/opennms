package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultPolledServicesModel implements PolledServicesModel,  InitializingBean {
	
	private PollerConfiguration m_pollerConfiguration;
	private PolledService[] m_polledServices;
	
	private LinkedList m_polledServiceChangedListeners;
	
	
	public void setPollerConfiguration(PollerConfiguration pollerConfiguration) {
		m_pollerConfiguration = pollerConfiguration;
	}
	
	public void afterPropertiesSet() throws Exception {
		assertNotNull(m_pollerConfiguration, "pollerConfiguration");
		
		PollConfiguration[] pollConfiguration = m_pollerConfiguration.getConfigurationForPoller();
		PolledService[] polledServices = new PolledService[pollConfiguration.length];
		
		for (int i = 0; i < pollConfiguration.length; i++) {
			PollConfiguration configuration = pollConfiguration[i];
			
			OnmsMonitoredService monSvc = configuration.getMonitoredService();
			Map monConfig = configuration.getMonitorConfiguration();
			OnmsPollModel pollModel = configuration.getPollModel();
			String polledServiceId = i+":"+monSvc.getNodeId()+":"+monSvc.getIpAddress()+":"+monSvc.getServiceName();
			
			PolledService polledService = new PolledService(polledServiceId, monSvc, monConfig, pollModel);
			
			polledServices[i] = polledService;
			
		}
		
		m_polledServices = polledServices;
		
		m_polledServiceChangedListeners = new LinkedList();
	}

	public PolledService[] getPolledServices() {
		return m_polledServices;
	}

	public void setInitialPollTime(String polledServiceId, Date initialPollTime) {
		int index = getServiceIndexWithId(polledServiceId);
		PolledService polledService = m_polledServices[index];
		
		polledService.setNextPollTime(initialPollTime);
		
		firePolledServiceChanged(polledService, index);
	}
	
	private int getServiceIndexWithId(String polledServiceId) {
		for (int i = 0; i < m_polledServices.length; i++) {
			PolledService polledService = m_polledServices[i];
			if (polledService.getId().equals(polledServiceId))
				return i;
		}
		throw new IllegalArgumentException("PolledService with id '"+polledServiceId+"' does not exist.");
	}

	public void updateServiceStatus(String polledServiceId, PollStatus pollStatus, Date pollTime) {
		System.err.println("Updating Status for "+polledServiceId+" to "+pollStatus+" at "+pollTime);
		
		int index = getServiceIndexWithId(polledServiceId);
		PolledService polledService = m_polledServices[index];

		polledService.updateStatus(pollStatus, pollTime);
		
		firePolledServiceChanged(polledService, index);
	}
	
	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
	}
	
	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public void addConfigurationChangedListener(ConfigurationChangedListener l) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public void removeConfigurationChangedListener(ConfigurationChangedListener l) {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	private void firePolledServiceChanged(PolledService polledService, int index) {
		PolledServiceChangedEvent e = new PolledServiceChangedEvent(polledService, index);
		
		for(ListIterator iter = m_polledServiceChangedListeners.listIterator(); iter.hasNext();) {
			PolledServiceChangedListener l = (PolledServiceChangedListener)iter.next();
			l.polledServiceChanged(e);
		}
	}

	public void addPolledServiceChangedListener(PolledServiceChangedListener l) {
		m_polledServiceChangedListeners.addFirst(l);
	}

	public void removePolledServiceChangedListener(PolledServiceChangedListener l) {
		m_polledServiceChangedListeners.remove(l);
	}


}
