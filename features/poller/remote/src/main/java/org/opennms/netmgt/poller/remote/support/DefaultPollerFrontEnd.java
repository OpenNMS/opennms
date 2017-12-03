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

package org.opennms.netmgt.poller.remote.support;

import static org.opennms.netmgt.poller.remote.PollerBackEnd.HOST_ADDRESS_KEY;
import static org.opennms.netmgt.poller.remote.PollerBackEnd.HOST_NAME_KEY;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.remote.ConfigurationChangedListener;
import org.opennms.netmgt.poller.remote.PollService;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.opennms.netmgt.poller.remote.PollerSettings;
import org.opennms.netmgt.poller.remote.ServicePollState;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedEvent;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedListener;
import org.opennms.netmgt.poller.remote.TimeAdjustment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * <p>DefaultPollerFrontEnd class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultPollerFrontEnd implements PollerFrontEnd, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollerFrontEnd.class);

    private class Disconnected extends RunningState {

        @Override
        public boolean isDisconnected() {
            return true;
        }

        @Override
        protected void onConfigChanged() {
            setState(new Running());
            doLoadConfig();
        }

        @Override
        protected void onPaused() {
            doPause();
            setState(new Paused());
        }

        @Override
        protected void onStarted() {
            setState(new Running());
            doLoadConfig();
        }

    }

    private static class Stopped extends PollerFrontEndState {

        @Override
        public boolean isExitNecessary() {
            return true;
        }

    }

    private class Initial extends PollerFrontEndState {
        @Override
        public void initialize() {
            try {
                final String monitorId = doInitialize();

                // If the monitor isn't registered yet...
                if (monitorId == null) {
                    // Change to the 'registering' state
                    setState(new Registering());
                } else if (doPollerStart()) {
                    // Change the state to running so we're ready to execute polls
                    setState(new Running());
                    // Load the configuration for the scheduler
                    doLoadConfig();
                } else {
                    // the poller has been deleted
                    doDelete();
                    setState(new Registering());
                }
            } catch (final Throwable e) {
                setState(new FatalExceptionOccurred(e));

                // rethrow the exception on initialize so we exit if we fail to initialize
                throw e;
            }
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

    }

    private class Paused extends RunningState {

        @Override
        protected void onConfigChanged() {
            doLoadConfig();
        }

        @Override
        public boolean isPaused() {
            return true;
        }

        @Override
        protected void onDisconnected() {
            doDisconnected();
            setState(new Disconnected());
        }

        @Override
        protected void onStarted() {
            doResume();
            setState(new Running());
        }

    }

    private class Registering extends PollerFrontEndState {
        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public void register(final String location) {
            try {
                // Create the location entry
                doRegister(location);
                // TODO: Check return value?
                doPollerStart();
                // Change the state to running so we're ready to execute polls
                setState(new Running());
                // Load the configuration for the scheduler
                doLoadConfig();
            } catch (final Throwable e) {
                LOG.warn("Unable to register.", e);
                setState(new Disconnected());
            }
        }
    }

    private class RunningState extends PollerFrontEndState {
        @Override
        public void pollService(final Integer serviceId) {
            /* most running states do nothing here */
        }

        @Override
        public void checkIn() {
            try {
                final MonitorStatus status = doCheckIn();
                switch (status) {
                    case CONFIG_CHANGED:
                        onConfigChanged();
                        break;
                    case DELETED:
                        onDeleted();
                        break;
                    case DISCONNECTED:
                        onDisconnected();
                        break;
                    case PAUSED:
                        onPaused();
                        break;
                    case STARTED:
                        onStarted();
                        break;
                    case STOPPED:
                        LOG.info("State set to STOPPED; stopping");
                        stop();
                        break;
                    default:
                        LOG.debug("Unhandled status on checkIn(): {}", status);
                        break;
                }
            } catch (final Throwable e) {
                LOG.error("Unexpected exception occurred while checking in.", e);
                setState(new FatalExceptionOccurred(e));
            }
            final String killSwitchFileName = System.getProperty("opennms.poller.killSwitch.resource");
            if (!"".equals(killSwitchFileName) && killSwitchFileName != null) {
                final File killSwitch = new File(killSwitchFileName);
                if (!killSwitch.exists()) {
                    LOG.info("Kill-switch file {} does not exist; stopping.", killSwitchFileName);
                    stop();
                }
            }
        }

        @Override
        public boolean isStarted() {
            return true;
        }

        protected void onConfigChanged() {
            /* do nothing be default */
        }

        protected void onDeleted() {
            doDelete();
            setState(new Registering());
        }

        protected void onDisconnected() {
            /* do nothing be default */
        }

        protected void onPaused() {
            /* do nothing be default */
        }

        protected void onStarted() {
            /* do nothing be default */
        }

    }

    private class Running extends RunningState {

        @Override
        public void pollService(final Integer polledServiceId) {
            try {
                doPollService(polledServiceId);
            } catch (Throwable e) {
                LOG.error("Unexpected exception occurred while polling service ID {}.", polledServiceId, e);
                setState(new FatalExceptionOccurred(e));
            }
        }

        @Override
        protected void onConfigChanged() {
            doLoadConfig();
        }

        @Override
        protected void onDisconnected() {
            doDisconnected();
            setState(new Disconnected());
        }

        @Override
        protected void onPaused() {
            doPause();
            setState(new Paused());
        }

    }

    private PollerFrontEndState m_state = new Initial();

    // injected dependencies
    private PollerBackEnd m_backEnd;

    private PollerSettings m_pollerSettings;

    private PollService m_pollService;
    
    private TimeAdjustment m_timeAdjustment;

    // listeners
    private LinkedList<PropertyChangeListener> m_propertyChangeListeners = new LinkedList<>();

    private LinkedList<ServicePollStateChangedListener> m_servicePollStateChangedListeners = new LinkedList<>();

    private LinkedList<ConfigurationChangedListener> m_configChangeListeners = new LinkedList<>();

    // current configuration
    private PollerConfiguration m_pollerConfiguration;

    /**
     * Current state of polled services. The map key is the monitored service ID.
     */
    private Map<Integer, ServicePollState> m_pollState = new LinkedHashMap<Integer, ServicePollState>();

    /** {@inheritDoc} */
    @Override
    public void addConfigurationChangedListener(ConfigurationChangedListener l) {
        m_configChangeListeners.addFirst(l);
    }

    /**
     * <p>doResume</p>
     */
    private void doResume() {
        doLoadConfig();
    }

    /**
     * <p>doPause</p>
     */
    private void doPause() {
        // do I need to do anything here?
    }

    /**
     * <p>doDisconnected</p>
     */
    private void doDisconnected() {
        doLoadConfig();
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        m_propertyChangeListeners.addFirst(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addServicePollStateChangedListener(final ServicePollStateChangedListener listener) {
        m_servicePollStateChangedListeners.addFirst(listener);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() {
        assertNotNull(m_timeAdjustment, "timeAdjustment");
        assertNotNull(m_backEnd, "pollerBackEnd");
        assertNotNull(m_pollService, "pollService");
        assertNotNull(m_pollerSettings, "pollerSettings");
    }

    /**
     * <p>checkConfig</p>
     */
    public void checkConfig() {
        m_state.checkIn();
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() {
        stop();
    }

    /**
     * <p>doCheckIn</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus} object.
     */
    private MonitorStatus doCheckIn() {
        return m_backEnd.pollerCheckingIn(getMonitoringSystemId(), getCurrentConfigTimestamp());
    }

    /**
     * <p>doDelete</p>
     */
    private void doDelete() {
        setMonitoringSystemId(null);
    }

    /**
     * <p>doInitialize</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    private String doInitialize() {
        assertNotNull(m_backEnd, "pollerBackEnd");
        assertNotNull(m_pollService, "pollService");
        assertNotNull(m_pollerSettings, "pollerSettings");

        return getMonitoringSystemId();
    }

    /**
     * <p>doPollerStart</p>
     *
     * @return a boolean.
     */
    private boolean doPollerStart() {
        // True if the monitor exists, false if the monitor has been deleted on the server
        return m_backEnd.pollerStarting(getMonitoringSystemId(), getDetails());
    }

    /**
     * <p>doPollService</p>
     *
     * @param polledServiceId a {@link java.lang.Integer} object.
     */
    private void doPollService(final Integer polledServiceId) {
        final PollStatus result = doPoll(polledServiceId);
        if (result == null)
            return;

        updateServicePollState(polledServiceId, result);

        m_backEnd.reportResult(getMonitoringSystemId(), polledServiceId, result);
    }

    /**
     * <p>doRegister</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    private void doRegister(final String location) {

        String monitoringSystemId = m_backEnd.registerLocationMonitor(location);

        try {
            setMonitoringSystemId(monitoringSystemId);
        } catch (Throwable e) {
            // TODO: Should we start anyway? I guess so.
            LOG.warn("Unable to set monitoring system ID: " + e.getMessage(), e);
        }

    }

    /**
     * <p>doStop</p>
     */
    private void doStop() {
        m_backEnd.pollerStopping(getMonitoringSystemId());
    }

    /**
     * Construct a list of certain system properties and metadata about this
     * monitoring system that will be relayed back to the {@link PollerBackEnd}.
     *
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, String> getDetails() {
        final HashMap<String, String> details = new HashMap<String, String>();
        final Properties p = System.getProperties();

        for (final Map.Entry<Object, Object> e : p.entrySet()) {
            if (e.getKey().toString().startsWith("os.") && e.getValue() != null) {
                details.put(e.getKey().toString(), e.getValue().toString());
            }
        }

        final InetAddress us = InetAddressUtils.getLocalHostAddress();
        details.put(HOST_ADDRESS_KEY, InetAddressUtils.str(us));
        details.put(HOST_NAME_KEY, us.getHostName());

        return Collections.unmodifiableMap(details);
    }

    /**
     * <p>getMonitoringSystemId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMonitoringSystemId() {
        return m_pollerSettings.getMonitoringSystemId();
    }

    /**
     * <p>getMonitorName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getMonitorName() {
        return (isRegistered() ? m_backEnd.getMonitorName(getMonitoringSystemId()) : "");
    }

    /**
     * <p>getPolledServices</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<PolledService> getPolledServices() {
        return Arrays.asList(m_pollerConfiguration.getPolledServices());
    }

    /**
     * <p>getPollerPollState</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<ServicePollState> getPollerPollState() {
        synchronized (m_pollState) {
            return new LinkedList<ServicePollState>(m_pollState.values());
        }
    }

    /** {@inheritDoc} */
    @Override
    public ServicePollState getServicePollState(int polledServiceId) {
        synchronized (m_pollState) {
            return m_pollState.get(polledServiceId);
        }
    }

    /**
     * <p>isRegistered</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isRegistered() {
        return m_state.isRegistered();
    }

    /**
     * <p>isStarted</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isStarted() {
        return m_state.isStarted();
    }

    /** {@inheritDoc} */
    @Override
    public void pollService(final Integer polledServiceId) {
        m_state.pollService(polledServiceId);
    }

    /** {@inheritDoc} */
    @Override
    public void register(final String monitoringLocation) {
        m_state.register(monitoringLocation);
    }

    /** {@inheritDoc} */
    @Override
    public void removeConfigurationChangedListener(final ConfigurationChangedListener listener) {
        m_configChangeListeners.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        m_propertyChangeListeners.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeServicePollStateChangedListener(final ServicePollStateChangedListener listener) {
        m_servicePollStateChangedListeners.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void setInitialPollTime(final Integer polledServiceId, final Date initialPollTime) {
        final ServicePollState pollState = getServicePollState(polledServiceId);
        if (pollState == null) {
            return;
        }

        pollState.setInitialPollTime(initialPollTime);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
    }

    /**
     * <p>setMonitoringSystemId</p>
     *
     * @param monitorId a {@link java.lang.String} object.
     */
    private void setMonitoringSystemId(final String monitorId) {
        m_pollerSettings.setMonitoringSystemId(monitorId);
    }

    /**
     * <p>setPollerBackEnd</p>
     *
     * @param backEnd a {@link org.opennms.netmgt.poller.remote.PollerBackEnd} object.
     */
    public void setPollerBackEnd(final PollerBackEnd backEnd) {
        m_backEnd = backEnd;
    }

    /**
     * <p>setPollerSettings</p>
     *
     * @param settings a {@link org.opennms.netmgt.poller.remote.PollerSettings} object.
     */
    public void setPollerSettings(final PollerSettings settings) {
        m_pollerSettings = settings;
    }

    /**
     * @param timeAdjustment the timeAdjustment to set
     */
    public void setTimeAdjustment(TimeAdjustment timeAdjustment) {
        m_timeAdjustment = timeAdjustment;
    }

    /**
     * <p>setPollService</p>
     *
     * @param pollService a {@link org.opennms.netmgt.poller.remote.PollService} object.
     */
    public void setPollService(final PollService pollService) {
        m_pollService = pollService;
    }

    @Override
    public void initialize() {
        m_state.initialize();
    }

    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        doStop();
        setState(new Stopped());
    }

    private static void assertNotNull(final Object propertyValue, final String propertyName) {
        Assert.state(propertyValue != null, propertyName + " must be set for instances of " + DefaultPollerFrontEnd.class.getName());
    }

    private void doLoadConfig() {
        Date oldTime = getCurrentConfigTimestamp();

        try {
            m_pollService.setServiceMonitorLocators(m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR));
            m_pollerConfiguration = retrieveLatestConfiguration();

            synchronized (m_pollState) {

                int i = 0;
                m_pollState.clear();
                // Initialize the monitor for the service
                for (final PolledService service : getPolledServices()) {
                    m_pollState.put(service.getServiceId(), new ServicePollState(service, i++));
                }
            }

            fireConfigurationChange(oldTime, getCurrentConfigTimestamp());
        } catch (final Throwable e) {
            LOG.warn("Unable to get updated poller configuration.", e);
            if (m_pollerConfiguration == null) {
                m_pollerConfiguration = new EmptyPollerConfiguration();
            }
        }
    }

    private PollerConfiguration retrieveLatestConfiguration() {
        PollerConfiguration config = m_backEnd.getPollerConfiguration(getMonitoringSystemId());
        m_timeAdjustment.setMasterTime(config.getServerTime());
        return config;
    }

    private PollStatus doPoll(final Integer polledServiceId) {

        final PolledService polledService = getPolledService(polledServiceId);
        if (polledService == null) {
            return null;
        }
        return m_pollService.poll(polledService);
    }

    private void fireConfigurationChange(final Date oldTime, final Date newTime) {
        final PropertyChangeEvent e = new PropertyChangeEvent(this, "configuration", oldTime, newTime);
        for (final ConfigurationChangedListener listener : m_configChangeListeners) {
            listener.configurationChanged(e);
        }
    }

    private void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        if (nullSafeEquals(oldValue, newValue)) {
            // no change no event
            return;

        }
        final PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);

        for (final PropertyChangeListener listener : m_propertyChangeListeners) {
            listener.propertyChange(event);
        }
    }

    private static boolean nullSafeEquals(final Object oldValue, final Object newValue) {
        return (oldValue == newValue ? true : ObjectUtils.nullSafeEquals(oldValue, newValue));
    }

    private void fireServicePollStateChanged(final PolledService polledService, final int index) {
        final ServicePollStateChangedEvent event = new ServicePollStateChangedEvent(polledService, index);

        for (final ServicePollStateChangedListener listener : m_servicePollStateChangedListeners) {
            listener.pollStateChange(event);
        }
    }

    private Date getCurrentConfigTimestamp() {
        return (m_pollerConfiguration == null ? null : m_pollerConfiguration.getConfigurationTimestamp());
    }

    private PolledService getPolledService(final Integer polledServiceId) {
        final ServicePollState servicePollState = getServicePollState(polledServiceId);
        return (servicePollState == null ? null : servicePollState.getPolledService());
    }

    private void setState(final PollerFrontEndState newState) {
        final boolean started = isStarted();
        final boolean registered = isRegistered();
        final boolean paused = isPaused();
        final boolean disconnected = isDisconnected();
        final boolean exitNecessary = isExitNecessary();
        m_state = newState;
        firePropertyChange(PollerFrontEndStates.exitNecessary.toString(), exitNecessary, isExitNecessary());
        firePropertyChange(PollerFrontEndStates.started.toString(), started, isStarted());
        firePropertyChange(PollerFrontEndStates.registered.toString(), registered, isRegistered());
        firePropertyChange(PollerFrontEndStates.paused.toString(), paused, isPaused());
        firePropertyChange(PollerFrontEndStates.disconnected.toString(), disconnected, isDisconnected());

    }

    @Override
    public boolean isDisconnected() {
        return m_state.isDisconnected();
    }

    @Override
    public boolean isPaused() {
        return m_state.isPaused();
    }

    /**
     * <p>isExitNecessary</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isExitNecessary() {
        return m_state.isExitNecessary();
    }

    private void updateServicePollState(final Integer polledServiceId, final PollStatus result) {
        final ServicePollState pollState = getServicePollState(polledServiceId);
        if (pollState == null) {
            return;
        }
        pollState.setLastPoll(result);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
    }

}
