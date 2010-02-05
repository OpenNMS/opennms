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
 * Created: August 17, 2006
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
package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface PollerFrontEnd {
    
    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations();
    
    public Collection<PolledService> getPolledServices();
    
    /**
     * Is the poller currently registered with the server.
     * @return true if and only if the server has been registered
     */
    public boolean isRegistered();
    
    /**
     * Return the monitor name of the poller or null if none exist
     */
    public String getMonitorName();
    
    /**
     * Authenticate with the given username and password, if necessary.
     */
    public void authenticate(String username, String password);

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
     * Returns whether some error occurred and an exit is necessary
     */
    public boolean isExitNecessary();
    
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


    public List<ServicePollState> getPollerPollState();
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
