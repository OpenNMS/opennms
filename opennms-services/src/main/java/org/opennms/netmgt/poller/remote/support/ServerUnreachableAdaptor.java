package org.opennms.netmgt.poller.remote.support;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.springframework.remoting.RemoteLookupFailureException;

public class ServerUnreachableAdaptor implements PollerBackEnd {
    
    private PollerBackEnd m_remoteBackEnd;
    private boolean m_serverUnresponsive = false;
    
    private static class EmptyPollerConfiguration implements PollerConfiguration {

        public Date getConfigurationTimestamp() {
            return new Date(0);
        }

        public PolledService[] getPolledServices() {
            return new PolledService[0];
        }
        
    }
    
    public void setRemoteBackEnd(PollerBackEnd remoteBackEnd) {
        m_remoteBackEnd = remoteBackEnd;
    }


    public void checkforUnresponsiveMonitors() {
        // this is a server side only method
    }

    public void configurationUpdated() {
        // this is a server side only method
    }

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        // leave this method as it is a 'before registration' method and we want errors to occur?
        return m_remoteBackEnd.getMonitoringLocations();
    }

    public PollerConfiguration getPollerConfiguration(int locationMonitorId) {
        if (m_serverUnresponsive) {
            return new EmptyPollerConfiguration();
        }
        return m_remoteBackEnd.getPollerConfiguration(locationMonitorId);
    }

    public boolean pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion) {
        // if we check in and get a remote exception then we switch to the EmptyConfiguration
        try {
            boolean result = m_remoteBackEnd.pollerCheckingIn(locationMonitorId, currentConfigurationVersion);
            m_serverUnresponsive = false;
            return result;
        } catch (RemoteLookupFailureException e) {
            // we have failed to check in properly with the server
            m_serverUnresponsive = true;
            return true;
        }
    }

    public boolean pollerStarting(int locationMonitorId) {
        return m_remoteBackEnd.pollerStarting(locationMonitorId);
    }

    public void pollerStopping(int locationMonitorId) {
        m_remoteBackEnd.pollerStopping(locationMonitorId);
    }

    public int registerLocationMonitor(String monitoringLocationId) {
        // leave this method as it is a 'before registration' method and we want errors to occur?
        return m_remoteBackEnd.registerLocationMonitor(monitoringLocationId);
    }

    public void reportResult(int locationMonitorID, int serviceId, PollStatus status) {
        if (!m_serverUnresponsive)
            m_remoteBackEnd.reportResult(locationMonitorID, serviceId, status);
    }


    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        return m_remoteBackEnd.getServiceMonitorLocators(context);
    }


    public String getMonitorName(int locationMonitorId) {
        return m_remoteBackEnd.getMonitorName(locationMonitorId);
    }


}
