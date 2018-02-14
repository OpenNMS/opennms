/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.remote.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.poller.remote.PollerTheme;
import org.opennms.netmgt.poller.remote.metadata.MetadataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteAccessException;

/**
 * <p>ServerUnreachableAdaptor class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ServerUnreachableAdaptor implements PollerBackEnd {
    private static final Logger LOG = LoggerFactory.getLogger(ServerUnreachableAdaptor.class);
    
    private String m_monitorName;
    private PollerBackEnd m_remoteBackEnd;
    private boolean m_serverUnresponsive = false;
    
    /**
     * <p>setRemoteBackEnd</p>
     *
     * @param remoteBackEnd a {@link org.opennms.netmgt.poller.remote.PollerBackEnd} object.
     */
    public void setRemoteBackEnd(final PollerBackEnd remoteBackEnd) {
        m_remoteBackEnd = remoteBackEnd;
    }


    /**
     * <p>checkForDisconnectedMonitors</p>
     */
    @Override
    public void checkForDisconnectedMonitors() {
        // this is a server side only method
    }

    /**
     * <p>configurationUpdated</p>
     */
    @Override
    public void configurationUpdated() {
        // this is a server side only method
    }

    /**
     * <p>getMonitoringLocations</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsMonitoringLocation> getMonitoringLocations() {
        // leave this method as it is a 'before registration' method and we want errors to occur?
        return m_remoteBackEnd.getMonitoringLocations();
    }

    /** {@inheritDoc} */
    @Override
    public PollerConfiguration getPollerConfiguration(final String locationMonitorId) {
        if (m_serverUnresponsive) {
            return new EmptyPollerConfiguration();
        }
        try {
            final PollerConfiguration config = m_remoteBackEnd.getPollerConfiguration(locationMonitorId);
            m_serverUnresponsive = false;
            return config;
        } catch (final RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return new EmptyPollerConfiguration();
        }
    }

    /** {@inheritDoc} */
    @Override
    public PollerConfiguration getPollerConfigurationForLocation(final String location) {
        if (m_serverUnresponsive) {
            return new EmptyPollerConfiguration();
        }
        try {
            final PollerConfiguration config = m_remoteBackEnd.getPollerConfigurationForLocation(location);
            m_serverUnresponsive = false;
            return config;
        } catch (final RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return new EmptyPollerConfiguration();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getApplicationsForLocation(final String location) {
        if (m_serverUnresponsive) {
            return Collections.emptySet();
        }
        try {
            final Set<String> applications = m_remoteBackEnd.getApplicationsForLocation(location);
            m_serverUnresponsive = false;
            return applications;
        } catch (final RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return Collections.emptySet();
        }
    }

    /** {@inheritDoc} */
    @Override
    public MonitorStatus pollerCheckingIn(final String locationMonitorId, final Date currentConfigurationVersion) {
        // if we check in and get a remote exception then we switch to the EmptyConfiguration
        try {
            final MonitorStatus result = m_remoteBackEnd.pollerCheckingIn(locationMonitorId, currentConfigurationVersion);
            m_serverUnresponsive = false;
            return result;
        } catch (final RemoteAccessException e) {
            // we have failed to check in properly with the server
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return MonitorStatus.DISCONNECTED;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean pollerStarting(final String locationMonitorId, final Map<String, String> pollerDetails) {
        try {
            final boolean pollerStarting = m_remoteBackEnd.pollerStarting(locationMonitorId, pollerDetails);
            // If false was returned, this location monitor doesn't exist on the server, so we can't get its name
            if (pollerStarting) {
                m_monitorName = m_remoteBackEnd.getMonitorName(locationMonitorId);
            }
            m_serverUnresponsive = false;
            return pollerStarting;
            
        } catch (final RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void pollerStopping(final String locationMonitorId) {
        m_remoteBackEnd.pollerStopping(locationMonitorId);
    }

    /** {@inheritDoc} */
    @Override
    public String registerLocationMonitor(final String monitoringLocationId) {
        // leave this method as it is a 'before registration' method and we want errors to occur?
    	return m_remoteBackEnd.registerLocationMonitor(monitoringLocationId);
    }

    /** {@inheritDoc} */
    @Override
    public void reportResult(final String locationMonitorID, final int serviceId, final PollStatus status) {
        if (!m_serverUnresponsive) {
            try {
                m_remoteBackEnd.reportResult(locationMonitorID, serviceId, status);
            } catch (RemoteAccessException e) {
                m_serverUnresponsive = true;
                LOG.warn("Server is unable to respond due to the following exception.", e);
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        try {
            return m_remoteBackEnd.getServiceMonitorLocators(context);
        } catch (RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return Collections.emptyList();
        }
    }


    /** {@inheritDoc} */
    @Override
    public String getMonitorName(String locationMonitorId) {
        try {
            return m_remoteBackEnd.getMonitorName(locationMonitorId);
        } catch (RemoteAccessException e) {
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return (m_monitorName == null ? ""+locationMonitorId : m_monitorName);
        }
    }


    @Override
    public void saveResponseTimeData(String locationMonitor, OnmsMonitoredService monSvc, double responseTime, Package pkg) {
        try {
            m_remoteBackEnd.saveResponseTimeData(locationMonitor, monSvc, responseTime, pkg);
        } catch (RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
        }
    }

    @Override
    public void reportSingleScan(final ScanReport report) {
        try {
            m_remoteBackEnd.reportSingleScan(report);
        } catch (RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
        }
    }

    @Override
    public Set<MetadataField> getMetadataFields() {
        try {
            return m_remoteBackEnd.getMetadataFields();
        } catch (final RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return Collections.emptySet();
        }
    }


    @Override
    public PollerTheme getTheme() {
        try {
            return m_remoteBackEnd.getTheme();
        } catch (final RemoteAccessException e) {
            m_serverUnresponsive = true;
            LOG.warn("Server is unable to respond due to the following exception.", e);
            return new PollerTheme();
        }
    }
}
