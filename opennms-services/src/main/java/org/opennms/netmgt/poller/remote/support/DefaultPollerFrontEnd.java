package org.opennms.netmgt.poller.remote.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.remote.ConfigurationChangedListener;
import org.opennms.netmgt.poller.remote.PollService;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.Poller;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.opennms.netmgt.poller.remote.PollerSettings;
import org.opennms.netmgt.poller.remote.ServicePollState;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedEvent;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultPollerFrontEnd implements PollerFrontEnd,  InitializingBean {
	
    private PollerBackEnd m_backEnd;
    private PollService m_pollService;
    private PollerSettings m_pollerSettings;
    
    private PollerConfiguration m_pollerConfiguration;
    
    private Map<Integer, ServicePollState> m_pollState = new HashMap<Integer, ServicePollState>();

	private LinkedList<PropertyChangeListener> m_propertyChangeListeners = new LinkedList<PropertyChangeListener>();
    private LinkedList<ServicePollStateChangedListener> m_servicePollStateChangedListeners = new LinkedList<ServicePollStateChangedListener>();

    
    public void setPollerBackEnd(PollerBackEnd backEnd) {
        m_backEnd = backEnd;
    }
	
    public void setPollerSettings(PollerSettings settings) {
        m_pollerSettings = settings;
    }

    public void setPollService(PollService pollService) {
        m_pollService = pollService;
    }
    
	public void afterPropertiesSet() throws Exception {
        assertNotNull(m_backEnd, "pollerBackEnd");
        assertNotNull(m_pollService, "pollService");
        assertNotNull(m_pollerSettings, "pollerSettings");

        if (isRegistered()) {
            initializePollState();
        }
        
	}

    private void initializePollState() {
        m_pollerConfiguration = m_backEnd.getPollerConfiguration(getMonitorId());
        
        int i = 0;
        for (PolledService service : m_pollerConfiguration.getPolledServices()) {
            m_pollState.put(service.getServiceId(), new ServicePollState(service, i++));
        }
    }

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Collection<PolledService> getPolledServices() {
        assertRegistered();
		return Arrays.asList(m_pollerConfiguration.getPolledServices());
	}

    private void assertRegistered() {
        Assert.state(isRegistered(), "The poller must be registered before we can poll or get its configuration");
    }

    public boolean isRegistered() {
        return m_pollerSettings.getMonitorId() != null;
    }

    public void register(String monitoringLocation) {
        int monitorId = m_backEnd.registerLocationMonitor(monitoringLocation);
        m_pollerSettings.setMonitorId(monitorId);
        initializePollState();
        firePropertyChange("registered", false, true);
    }

    public void setInitialPollTime(Integer polledServiceId, Date initialPollTime) {
        throw new UnsupportedOperationException("not yet implemented");
	}
	
    private PolledService findServiceWithId(Integer polledServiceId) {
        Collection<PolledService> polledServices = getPolledServices();
        for (PolledService service : polledServices) {
            if (polledServiceId.equals(service.getServiceId()))
                return service;
        }
        throw new IllegalArgumentException("Unable to find polledService with id "+polledServiceId);
    }

	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
	}
    
    private Category log() {
		return ThreadCategory.getInstance(getClass());
	}
    
    public void pollService(Integer polledServiceId) {
        assertRegistered();
        
        PollStatus result = doPoll(polledServiceId);
        
        updateServicePollState(polledServiceId, result);
        
        m_backEnd.reportResult(getMonitorId(), polledServiceId, result);
        
    }

    private void updateServicePollState(Integer polledServiceId, PollStatus result) {
        ServicePollState pollState = getServicePollState(polledServiceId);
        pollState.setLastPoll(result);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
    }

    private PollStatus doPoll(Integer polledServiceId) {
        PolledService polledService = getPolledService(polledServiceId);
        PollStatus result = m_pollService.poll(polledService);
        return result;
    }

    private PolledService getPolledService(Integer polledServiceId) {
        return getServicePollState(polledServiceId).getPolledService();
    }

    private int getMonitorId() {
        return m_pollerSettings.getMonitorId();
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        
        for (PropertyChangeListener l : m_propertyChangeListeners) {
            l.propertyChange(e);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_propertyChangeListeners.addFirst(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        m_propertyChangeListeners.remove(l);
    }
    
    public void addConfigurationChangedListener(ConfigurationChangedListener l) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void removeConfigurationChangedListener(ConfigurationChangedListener l) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    
    void fireServicePollStateChanged(PolledService polledService, int index) {
        ServicePollStateChangedEvent e = new ServicePollStateChangedEvent(polledService, index);
        
        for (ServicePollStateChangedListener l : m_servicePollStateChangedListeners) {
            l.pollStateChange(e);
        }
    }
    
    public void addServicePollStateChangedListener(ServicePollStateChangedListener l) {
        m_servicePollStateChangedListeners.addFirst(l);
    }

    public void removeServicePollStateChangedListener(ServicePollStateChangedListener l) {
        m_servicePollStateChangedListeners.remove(l);
    }

    public void checkConfig() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ServicePollState getServicePollState(int polledServiceId) {
        return m_pollState.get(polledServiceId);
    }
    

}
