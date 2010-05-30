/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.poller.remote.support;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.springframework.remoting.RemoteAccessException;

/**
 * ExceptionProtectedPollerBackEnd.  This turns all server side exceptions 
 * into RemoteAccessExceptions so the ServerUnreachableAdapter can handle the code correctly.
 * 
 * @author brozow
 */
public class ExceptionProtectedPollerBackEnd implements PollerBackEnd {
    
    private PollerBackEnd m_delegate;
    
    public void setDelegate(PollerBackEnd delegate) {
        m_delegate = delegate;
    }

    public void checkForDisconnectedMonitors() {
        try {
            m_delegate.checkForDisconnectedMonitors();
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public void configurationUpdated() {
        try {
            m_delegate.configurationUpdated();
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public String getMonitorName(int locationMonitorId) {
        try {
            return m_delegate.getMonitorName(locationMonitorId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        try {
            return m_delegate.getMonitoringLocations();
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public PollerConfiguration getPollerConfiguration(int locationMonitorId) {
        try {
            return m_delegate.getPollerConfiguration(locationMonitorId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        try {
            return m_delegate.getServiceMonitorLocators(context);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public MonitorStatus pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion) {
        try {
            return m_delegate.pollerCheckingIn(locationMonitorId, currentConfigurationVersion);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public boolean pollerStarting(int locationMonitorId, Map<String, String> pollerDetails) {
        try {
            return m_delegate.pollerStarting(locationMonitorId, pollerDetails);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public void pollerStopping(int locationMonitorId) {
        try {
            m_delegate.pollerStopping(locationMonitorId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public int registerLocationMonitor(String monitoringLocationId) {
        try {
            return m_delegate.registerLocationMonitor(monitoringLocationId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    public void reportResult(int locationMonitorID, int serviceId,
            PollStatus status) {
        try {
            m_delegate.reportResult(locationMonitorID, serviceId, status);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

}
