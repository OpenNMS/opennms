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
    private LinkedList<ConfigurationChangedListener> m_configChangeListeners = new LinkedList<ConfigurationChangedListener>();
    private boolean m_initialized;
    private boolean m_started;

    
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
        m_initialized = true;

        if (isRegistered()) {
            initializePollState();
        }
        
	}

    private void assertInitialized() {
        Assert.isTrue(m_initialized, "afterProperties set has not been called");
    }

    private void initializePollState() {
        Date oldTime = (m_pollerConfiguration == null ? null : m_pollerConfiguration.getConfigurationTimestamp());
        
        if (!isStarted()) {
            start();
        }
        
        m_pollerConfiguration = m_backEnd.getPollerConfiguration(getMonitorId());
        
        int i = 0;
        for (PolledService service : m_pollerConfiguration.getPolledServices()) {
            m_pollState.put(service.getServiceId(), new ServicePollState(service, i++));
        }
        
        fireConfigurationChange(oldTime, m_pollerConfiguration.getConfigurationTimestamp());
    }

    private void start() {
        assertRegistered();
        m_backEnd.pollerStarting(getMonitorId());
        m_started = true;
    }

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Collection<PolledService> getPolledServices() {
        assertRegistered();
		return Arrays.asList(m_pollerConfiguration.getPolledServices());
	}

    private void assertRegistered() {
        assertInitialized();
        Assert.state(isRegistered(), "The poller must be registered before we can poll or get its configuration");
    }

    public boolean isRegistered() {
        return m_pollerSettings.getMonitorId() != null;
    }

    public void register(String monitoringLocation) {
        assertInitialized();
        int monitorId = m_backEnd.registerLocationMonitor(monitoringLocation);
        m_pollerSettings.setMonitorId(monitorId);
        initializePollState();
        firePropertyChange("registered", false, true);
    }

    public void setInitialPollTime(Integer polledServiceId, Date initialPollTime) {
        assertRegistered();
        ServicePollState pollState = getServicePollState(polledServiceId);
        pollState.setInitialPollTime(initialPollTime);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
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
        assertRegistered();

        ServicePollState pollState = getServicePollState(polledServiceId);
        pollState.setLastPoll(result);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
    }

    private PollStatus doPoll(Integer polledServiceId) {
        assertRegistered();

        PolledService polledService = getPolledService(polledServiceId);
        PollStatus result = m_pollService.poll(polledService);
        return result;
    }

    private PolledService getPolledService(Integer polledServiceId) {
        assertRegistered();

        return getServicePollState(polledServiceId).getPolledService();
    }

    private int getMonitorId() {
        return m_pollerSettings.getMonitorId();
    }

    
    private void fireServicePollStateChanged(PolledService polledService, int index) {
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
        if (!isRegistered()) {
            // no reason to check if we aren't registerd
            return;
        }
            
        assertRegistered();
        if (m_backEnd.pollerCheckingIn(getMonitorId(), m_pollerConfiguration.getConfigurationTimestamp())) {
            initializePollState();
        }
    }

    private void assertConfigured() {
        assertRegistered();
        Assert.notNull(m_pollerConfiguration, "The poller has not been configured");
    }

    public ServicePollState getServicePollState(int polledServiceId) {
        assertRegistered();
        return m_pollState.get(polledServiceId);
        
    }

    private void fireConfigurationChange(Date oldTime, Date newTime) {
        PropertyChangeEvent e = new PropertyChangeEvent(this, "configuration", oldTime, newTime);
        for (ConfigurationChangedListener l : m_configChangeListeners) {
            l.configurationChanged(e);
        }
    }

    public void addConfigurationChangedListener(ConfigurationChangedListener l) {
        m_configChangeListeners.addFirst(l);
    }

    public void removeConfigurationChangedListener(ConfigurationChangedListener l) {
        m_configChangeListeners.remove(l);
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

    public boolean isStarted() {
        return m_started;
    }

    public void stop() {
        m_backEnd.pollerStopping(1);
        m_started = false;
    }
    
    

}
