/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 23, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.remote.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.springframework.remoting.RemoteLookupFailureException;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ServerUnreachableAdaptor implements PollerBackEnd {
    
    private String m_monitorName;
    private PollerBackEnd m_remoteBackEnd;
    private boolean m_serverUnresponsive = false;
    
    public void setRemoteBackEnd(PollerBackEnd remoteBackEnd) {
        m_remoteBackEnd = remoteBackEnd;
    }


    public void checkForDisconnectedMonitors() {
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
        try {
            return m_remoteBackEnd.getPollerConfiguration(locationMonitorId);
        } catch (RemoteLookupFailureException e) {
            m_serverUnresponsive = true;
            return new EmptyPollerConfiguration();
        }
    }

    public MonitorStatus pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion) {
        // if we check in and get a remote exception then we switch to the EmptyConfiguration
        try {
            MonitorStatus result = m_remoteBackEnd.pollerCheckingIn(locationMonitorId, currentConfigurationVersion);
            m_serverUnresponsive = false;
            return result;
        } catch (RemoteLookupFailureException e) {
            // we have failed to check in properly with the server
            m_serverUnresponsive = true;
            return MonitorStatus.DISCONNECTED;
        }
    }

    public boolean pollerStarting(int locationMonitorId, Map<String, String> pollerDetails) {
        try {
            
            boolean pollerStarting = m_remoteBackEnd.pollerStarting(locationMonitorId, pollerDetails);
            // If false was returned, this location monitor doesn't exist on the server, so we can't get its name
            if (pollerStarting) {
                m_monitorName = m_remoteBackEnd.getMonitorName(locationMonitorId);
            }
            m_serverUnresponsive = false;
            return pollerStarting;
            
        } catch (RemoteLookupFailureException e) {
            m_serverUnresponsive = true;
            return true;
        }
    }

    public void pollerStopping(int locationMonitorId) {
        m_remoteBackEnd.pollerStopping(locationMonitorId);
    }

    public int registerLocationMonitor(String monitoringLocationId) {
        // leave this method as it is a 'before registration' method and we want errors to occur?
        return m_remoteBackEnd.registerLocationMonitor(monitoringLocationId);
    }

    public void reportResult(int locationMonitorID, int serviceId, PollStatus status) {
        if (!m_serverUnresponsive) {
            try {
                m_remoteBackEnd.reportResult(locationMonitorID, serviceId, status);
            } catch (RemoteLookupFailureException e) {
                m_serverUnresponsive = true;
            }
        }
    }


    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        try {
            return m_remoteBackEnd.getServiceMonitorLocators(context);
        } catch (RemoteLookupFailureException e) {
            m_serverUnresponsive = true;
            return Collections.emptyList();
        }
    }


    public String getMonitorName(int locationMonitorId) {
        try {
            return m_remoteBackEnd.getMonitorName(locationMonitorId);
        } catch (RemoteLookupFailureException e) {
            return (m_monitorName == null ? ""+locationMonitorId : m_monitorName);
        }
    }


}
