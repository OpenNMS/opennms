package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

public interface PollerFrontEnd {
    
    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations();

	public Collection<PolledService> getPolledServices();

    /**
     * Is the poller currently registered with the server.
     * @return true if and only if the server has been registered
     */
    public boolean isRegistered();
    
    /**
     * Register the poller if it has not been registered before.
     * 
     * @param monitoringLocationName The name of the monitoring Location definition under
     * which to register this monitor
     */
    public void register(String monitoringLocationName);


    /**
     * Set the initial poll time for a polledService
     * @param polledServiceId the id of the polledService whose pollTime we are setting
     * @param initialPollTime the time to set its initialPollTime to
     */
	public void setInitialPollTime(Integer polledServiceId, Date initialPollTime);
    
    /**
     * Poll the service with id polledServiceId and report the results to the server
     * 
     * @param polledServiceId The serviceid of the polledService that needs to be polled
     * 
     */
    public void pollService(Integer polledServiceId);
    
    /**
     * Returns whether or not the poller has been started
     */
    public boolean isStarted();
    
    /**
     * Stop polling.  This should be called before the system exits.
     *
     */
    public void stop();
    
    /**
     * Returns the state of polling in this monitor.  
     * @param polledServiceId
     * @return
     */
    public ServicePollState getServicePollState(int polledServiceId);


    /**
     * Register a listener to listen for events indication a change
     * in the poller configuration
     * @param l
     */
	public void addConfigurationChangedListener(ConfigurationChangedListener l);
    
    /**
     * Remove a config change listener
     * @param l
     */
	public void removeConfigurationChangedListener(ConfigurationChangedListener l);

    /**
     * Register a property change listener. (for exampe the 'registered' property)
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Register a listener for changes in an attribute of a PolledService
     * @param l
     */
    public void addServicePollStateChangedListener(ServicePollStateChangedListener l);
    public void removeServicePollStateChangedListener(ServicePollStateChangedListener l);
	

}
