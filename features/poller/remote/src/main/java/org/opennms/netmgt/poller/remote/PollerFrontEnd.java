/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>PollerFrontEnd interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollerFrontEnd {

    public static enum PollerFrontEndStates {
        exitNecessary,
        started,
        registered,
        paused,
        disconnected
    }

    public void checkConfig();

    /**
     * <p>getPolledServices</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<PolledService> getPolledServices();

    /**
     * Is the poller currently registered with the server.
     *
     * @return true if and only if the server has been registered
     */
    boolean isRegistered();

    /**
     * Return the monitor name of the poller or null if none exist
     *
     * @return a {@link java.lang.String} object.
     */
    String getMonitorName();

    /**
     * Register the poller if it has not been registered before.
     *
     * @param monitoringLocationName The name of the monitoring Location definition under
     * which to register this monitor
     */
    void register(String monitoringLocationName);


    /**
     * Set the initial poll time for a polledService
     *
     * @param polledServiceId the id of the polledService whose pollTime we are setting
     * @param initialPollTime the time to set its initialPollTime to
     */
    void setInitialPollTime(Integer polledServiceId, Date initialPollTime);

    /**
     * Poll the service with id polledServiceId and report the results to the server
     *
     * @param polledServiceId The serviceid of the polledService that needs to be polled
     */
    void pollService(Integer polledServiceId);

    /**
     * Returns whether or not the poller has been started
     *
     * @return a boolean.
     */
    boolean isStarted();

    /**
     * Returns whether or not the poller has been paused
     *
     * @return a boolean.
     */
    boolean isPaused();

    /**
     * Returns whether or not the poller has been disconnected
     *
     * @return a boolean.
     */
    boolean isDisconnected();

    /**
     * Returns whether some error occurred and an exit is necessary
     *
     * @return a boolean.
     */
    boolean isExitNecessary();

    /**
     * Initialize the poller front end.  This will include reading the 
     * configuration and then starting up the front end.
     */
    void initialize();

    /**
     * Stop polling.  This should be called before the system exits.
     */
    void stop();

    /**
     * Returns the state of polling in this monitor.
     *
     * @param polledServiceId a int.
     * @return a {@link org.opennms.netmgt.poller.remote.ServicePollState} object.
     */
    ServicePollState getServicePollState(int polledServiceId);


    /**
     * <p>getPollerPollState</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<ServicePollState> getPollerPollState();
    /**
     * Register a listener to listen for events indication a change
     * in the poller configuration
     *
     * @param l a {@link org.opennms.netmgt.poller.remote.ConfigurationChangedListener} object.
     */
    void addConfigurationChangedListener(ConfigurationChangedListener l);

    /**
     * Remove a config change listener
     *
     * @param l a {@link org.opennms.netmgt.poller.remote.ConfigurationChangedListener} object.
     */
    void removeConfigurationChangedListener(ConfigurationChangedListener l);

    /**
     * Register a property change listener. (for exampe the 'registered' property)
     *
     * @param l a {@link java.beans.PropertyChangeListener} object.
     */
    void addPropertyChangeListener(PropertyChangeListener l);
    /**
     * <p>removePropertyChangeListener</p>
     *
     * @param l a {@link java.beans.PropertyChangeListener} object.
     */
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Register a listener for changes in an attribute of a PolledService
     *
     * @param l a {@link org.opennms.netmgt.poller.remote.ServicePollStateChangedListener} object.
     */
    void addServicePollStateChangedListener(ServicePollStateChangedListener l);
    /**
     * <p>removeServicePollStateChangedListener</p>
     *
     * @param l a {@link org.opennms.netmgt.poller.remote.ServicePollStateChangedListener} object.
     */
    void removeServicePollStateChangedListener(ServicePollStateChangedListener l);

}
