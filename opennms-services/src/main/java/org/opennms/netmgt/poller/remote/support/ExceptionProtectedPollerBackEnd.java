/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.OnmsMonitoredService;
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
 * @version $Id: $
 */
public class ExceptionProtectedPollerBackEnd implements PollerBackEnd {
    
    private PollerBackEnd m_delegate;
    
    /**
     * <p>setDelegate</p>
     *
     * @param delegate a {@link org.opennms.netmgt.poller.remote.PollerBackEnd} object.
     */
    public void setDelegate(PollerBackEnd delegate) {
        m_delegate = delegate;
    }

    /**
     * <p>checkForDisconnectedMonitors</p>
     */
    @Override
    public void checkForDisconnectedMonitors() {
        try {
            m_delegate.checkForDisconnectedMonitors();
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /**
     * <p>configurationUpdated</p>
     */
    @Override
    public void configurationUpdated() {
        try {
            m_delegate.configurationUpdated();
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getMonitorName(int locationMonitorId) {
        try {
            return m_delegate.getMonitorName(locationMonitorId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /**
     * <p>getMonitoringLocations</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        try {
            return m_delegate.getMonitoringLocations();
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public PollerConfiguration getPollerConfiguration(int locationMonitorId) {
        try {
            return m_delegate.getPollerConfiguration(locationMonitorId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        try {
            return m_delegate.getServiceMonitorLocators(context);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public MonitorStatus pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion) {
        try {
            return m_delegate.pollerCheckingIn(locationMonitorId, currentConfigurationVersion);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean pollerStarting(int locationMonitorId, Map<String, String> pollerDetails) {
        try {
            return m_delegate.pollerStarting(locationMonitorId, pollerDetails);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void pollerStopping(int locationMonitorId) {
        try {
            m_delegate.pollerStopping(locationMonitorId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int registerLocationMonitor(String monitoringLocationId) {
        try {
            return m_delegate.registerLocationMonitor(monitoringLocationId);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reportResult(int locationMonitorID, int serviceId,
            PollStatus status) {
        try {
            m_delegate.reportResult(locationMonitorID, serviceId, status);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

    @Override
    public void saveResponseTimeData(String locationMonitor, OnmsMonitoredService monSvc, double responseTime, Package pkg) {
        try {
            m_delegate.saveResponseTimeData(locationMonitor, monSvc, responseTime, pkg);
        } catch (Throwable t) {
            LogUtils.errorf(this, t, "Unexpected exception thrown in remote poller backend.");
            throw new RemoteAccessException("Unexpected Exception Occurred on the server.", t);
        }
    }

}
