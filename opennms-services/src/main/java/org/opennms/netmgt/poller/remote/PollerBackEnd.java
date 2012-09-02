/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

/**
 * <p>PollerBackEnd interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollerBackEnd {
    
    /**
     * Return the set of available MonitoringLocationDefinitions
     *
     * @returns the set of monitoring loat
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations();
    
    /**
     * Register a new location monitor
     *
     * @param monitoringLocationId the id of the monitoredLocation to associate with
     * this location monitor
     * @return the id of the new locations monitor
     */
    int registerLocationMonitor(String monitoringLocationId);
    
    /**
     * Get monitor name
     *
     * @param locationMonitorId a int.
     * @return a {@link java.lang.String} object.
     */
    String getMonitorName(int locationMonitorId);
    
    /**
     * Get service monitor locators for creating serviceMonitors for the poller.
     *
     * @param context a {@link org.opennms.netmgt.poller.DistributionContext} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context);

    /**
     * Notifies the backend that a registered poller is starting
     *
     * @param locationMonitorId the id of the requesting location monitor
     * @param pollerDetails TODO
     * @returns true if and only if the server recognizes this locationMonitor
     * @return a boolean.
     */
    boolean pollerStarting(int locationMonitorId, Map<String, String> pollerDetails);
    
    /**
     * Notifies the backend that a registered poller is stopping
     *
     * @param locationMonitorId the id of the requesting location monitor
     */
    void pollerStopping(int locationMonitorId);
    
 
    /**
     * Checkin with the backend to let it know that the poller is still alive and to find
     * out if there are any configuration changes.
     *
     * @param locationMonitorId the id of the location monitor that is checking in
     * @param currentConfigurationVersion the version of the configuration that the location monitor is currently using
     * @return true if the configuration should be updated.
     */
    MonitorStatus pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion);
    
    /**
     * Gets the poller configuration assigned to this monitoring location
     *
     * @param locationMonitorId the id of the requesting location monitor
     * @return the PollerConfiguration for the indicicated location monitor
     */
    PollerConfiguration getPollerConfiguration(int locationMonitorId);
    
    /**
     * Report a poll result from the client to the server.
     *
     * @param locationMonitorID the id of the location monitor that did the poll
     * @param serviceId the id of the service that was polled
     * @param status a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    void reportResult(int locationMonitorID, int serviceId, PollStatus status);


    /**
     * <p>configurationUpdated</p>
     */
    void configurationUpdated();

    /**
     * <p>checkForDisconnectedMonitors</p>
     */
    void checkForDisconnectedMonitors();

    /**
     * <p>saveResponseTimeData</p>
     *
     * @param locationMonitor a {@link java.lang.String} object.
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @param responseTime a double.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    void saveResponseTimeData(String locationMonitor, OnmsMonitoredService monSvc, double responseTime, Package pkg);
}
