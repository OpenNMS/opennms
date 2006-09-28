package org.opennms.netmgt.poller.remote;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;

public interface PollerBackEnd {
    
    /**
     * Return the set of available MonitoringLocationDefinitions
     * 
     * @returns the set of monitoring loat
     */
    public abstract Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations();

    /**
     * Register a new location monitor
     * 
     * @param monitoringLocationId the id of the monitoredLocation to associate with
     * this location monitor
     * @return the id of the new locations monitor
     */
    public abstract int registerLocationMonitor(String monitoringLocationId);
    
    /**
     * Notifies the backend that a registered poller is starting
     * 
     * @param locationMonitorId the id of the requesting location monitor
     * @returns true if and only if the server recognizes this locationMonitor
     */
    public abstract boolean pollerStarting(int locationMonitorId);
    
    /**
     * Notifies the backend that a registered poller is stopping
     * 
     * @param locationMonitorId the id of the requesting location monitor
     * 
     */
    public abstract void pollerStopping(int locationMonitorId);
    
 
    /**
     * Checkin with the backend to let it know that the poller is still alive and to found
     * out if there are any configuration changes.
     * 
     * @param locationMonitorId the id of the location monitor that is checking in
     * @param currentConfigurationVersion the version of the configuration that the location monitor is currently using
     * @return true if the configuration should be updated.
     */
    public abstract boolean pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion);
    
    /**
     * Gets the poller configuration assigned to this monitoring location
     * 
     * @param locationMonitorId the id of the requesting location monitor
     * @return the PollerConfiguration for the indicicated location monitor
     */
    public abstract PollerConfiguration getPollerConfiguration(int locationMonitorId);
    
    /**
     * Report a poll result from the client to the server.
     * 
     * @param locationMonitorID the id of the location monitor that did the poll
     * @param serviceId the id of the service that was polled
     * @param pollStatus the result of the poll
     */
    public abstract void reportResult(int locationMonitorID, int serviceId, PollStatus status);
}
