/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 10, 2006
 *
 * Copyright (C) 2006-2010 The OpenNMS Group, Inc.  All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.remote.ConfigurationChangedListener;
import org.opennms.netmgt.poller.remote.PollService;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.Poller;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.opennms.netmgt.poller.remote.PollerSettings;
import org.opennms.netmgt.poller.remote.ServicePollState;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedEvent;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultPollerFrontEnd implements PollerFrontEnd, InitializingBean, DisposableBean {

    private class Disconnected extends RunningState {

        @Override
        public boolean isDisconnected() {
            return true;
        }

        @Override
        public void stop() {
            // don't call do stop as we are disconnected from the server
            setState(new Registering());
        }

        @Override
        protected void onConfigChanged() {
            doLoadConfig();
            setState(new Running());
        }

        @Override
        protected void onPaused() {
            doPause();
            setState(new Paused());
        }

        @Override
        protected void onStarted() {
            doLoadConfig();
            setState(new Running());
        }

    }

    public class Initial extends State {
        @Override
        public void initialize() {
            try {
                final Integer monitorId = doInitialize();
                if (monitorId == null) {
                    setState(new Registering());
                } else if (doPollerStart()) {
                    setState(new Running());
                } else {
                    // the poller has been deleted
                    doDelete();
                    setState(new Registering());
                }
            } catch (final RuntimeException e) {
                setState(new FatalExceptionOccurred());

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

    private class Registering extends State {
        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public void register(final String location) {
            try {
                doRegister(location);
                setState(new Running());
            } catch (final Exception e) {
                LogUtils.warnf(this, e, "Unable to register.");
                setState(new Disconnected());
            }
        }
    }

    private class RunningState extends State {
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
                }
            } catch (final Exception e) {
                LogUtils.errorf(this, e, "Unexpected exception occurred while checking in.");
                setState(new FatalExceptionOccurred());
            }
            final String killSwitchFileName = System.getProperty("opennms.poller.killSwitch.resource");
            if (!"".equals(killSwitchFileName) && killSwitchFileName != null) {
                final File killSwitch = new File(System.getProperty("opennms.poller.killSwitch.resource"));
                if (!killSwitch.exists()) {
                    LogUtils.infof(this, "Kill-switch file %s does not exist; stopping.", killSwitch.getPath());
                    doStop();
                }
            }
        }

        @Override
        public boolean isStarted() {
            return true;
        }

        @Override
        public void stop() {
            try {
                doStop();
                setState(new Registering());
            } catch (final Exception e) {
                LogUtils.errorf(this, e, "Unexpected exception occurred while stopping.");
                setState(new FatalExceptionOccurred());
            }
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

    public class Running extends RunningState {

        @Override
        public void pollService(final Integer polledServiceId) {
            try {
                doPollService(polledServiceId);
            } catch (Exception e) {
                LogUtils.errorf(this, e, "Unexpected exception occurred while polling service ID %s.", polledServiceId);
                setState(new FatalExceptionOccurred());
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

    public class FatalExceptionOccurred extends State {
        @Override
        public boolean isExitNecessary() {
            return true;
        }
    }

    private abstract class State {
        public void checkIn() {
            // a pollerCheckingIn in any state that doesn't respond just does nothing
        }

        public IllegalStateException illegalState(final String msg) {
            return new IllegalStateException(msg + " State: " + this);
        }

        public void initialize() {
            throw illegalState("Initialize called on invalid state.");
        }

        public boolean isInitialized() {
            return true;
        }

        public boolean isRegistered() {
            return true;
        }

        public boolean isStarted() {
            return false;
        }

        public boolean isPaused() {
            return false;
        }

        public boolean isDisconnected() {
            return false;
        }

        public boolean isExitNecessary() {
            return false;
        }

        public void pollService(final Integer serviceId) {
            throw illegalState("Cannot poll from this state.");
        }

        public void register(final String location) {
            throw illegalState("Cannot register from this state.");
        }

        public void stop() {
            // do nothing here by default as the actual exit is managed by the external program
        }

        public String toString() {
            return getClass().getSimpleName();
        }

    }

    private State m_state = new Initial();

    // injected dependencies
    private PollerBackEnd m_backEnd;

    private PollerSettings m_pollerSettings;

    private PollService m_pollService;

    // listeners
    private LinkedList<PropertyChangeListener> m_propertyChangeListeners = new LinkedList<PropertyChangeListener>();

    private LinkedList<ServicePollStateChangedListener> m_servicePollStateChangedListeners = new LinkedList<ServicePollStateChangedListener>();

    private LinkedList<ConfigurationChangedListener> m_configChangeListeners = new LinkedList<ConfigurationChangedListener>();

    // current configuration
    private PollerConfiguration m_pollerConfiguration;

    // current state of polled services
    private Map<Integer, ServicePollState> m_pollState = new LinkedHashMap<Integer, ServicePollState>();

    public void addConfigurationChangedListener(ConfigurationChangedListener l) {
        m_configChangeListeners.addFirst(l);
    }

    public void doResume() {
        doLoadConfig();
    }

    public void doPause() {
        // do I need to do anything here?
    }

    public void doDisconnected() {
        doLoadConfig();
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        m_propertyChangeListeners.addFirst(listener);
    }

    public void addServicePollStateChangedListener(final ServicePollStateChangedListener listener) {
        m_servicePollStateChangedListeners.addFirst(listener);
    }

    public void afterPropertiesSet() throws Exception {
        m_state.initialize();
    }

    public void checkConfig() {
        m_state.checkIn();
    }

    public void destroy() throws Exception {
        stop();
    }

    public MonitorStatus doCheckIn() {
        return m_backEnd.pollerCheckingIn(getMonitorId(), getCurrentConfigTimestamp());
    }

    public void doDelete() {
        setMonitorId(null);
    }

    public Integer doInitialize() {
        assertNotNull(m_backEnd, "pollerBackEnd");
        assertNotNull(m_pollService, "pollService");
        assertNotNull(m_pollerSettings, "pollerSettings");

        return getMonitorId();
    }

    public boolean doPollerStart() {

        if (!m_backEnd.pollerStarting(getMonitorId(), getDetails())) {
            // the monitor has been deleted on the server
            return false;
        }

        doLoadConfig();

        return true;

    }

    public void doPollService(Integer polledServiceId) {
        final PollStatus result = doPoll(polledServiceId);
        if (result == null)
            return;

        updateServicePollState(polledServiceId, result);

        m_backEnd.reportResult(getMonitorId(), polledServiceId, result);
    }

    public void doRegister(final String location) {

        int monitorId = m_backEnd.registerLocationMonitor(location);
        setMonitorId(monitorId);

        doPollerStart();

    }

    public void doStop() {
        m_backEnd.pollerStopping(getMonitorId());
    }

    public Map<String, String> getDetails() {
        final HashMap<String, String> details = new HashMap<String, String>();
        final Properties p = System.getProperties();

        for (Map.Entry<Object, Object> e : p.entrySet()) {
            if (e.getKey().toString().startsWith("os.") && e.getValue() != null) {
                details.put(e.getKey().toString(), e.getValue().toString());
            }
        }

        try {
            final InetAddress us = InetAddress.getLocalHost();
            details.put("org.opennms.netmgt.poller.remote.hostAddress", us.getHostAddress());
            details.put("org.opennms.netmgt.poller.remote.hostName", us.getHostName());
        } catch (final UnknownHostException e) {
            LogUtils.tracef(this, e, "Unable to determine localhost.");
        }

        return details;
    }

    public Integer getMonitorId() {
        return m_pollerSettings.getMonitorId();
    }

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        assertInitialized();
        return m_backEnd.getMonitoringLocations();
    }

    public String getMonitorName() {
        return (isRegistered() ? m_backEnd.getMonitorName(getMonitorId()) : "");
    }

    public Collection<PolledService> getPolledServices() {
        return Arrays.asList(m_pollerConfiguration.getPolledServices());
    }

    public List<ServicePollState> getPollerPollState() {
        synchronized (m_pollState) {
            return new LinkedList<ServicePollState>(m_pollState.values());
        }
    }

    public ServicePollState getServicePollState(int polledServiceId) {
        synchronized (m_pollState) {
            return m_pollState.get(polledServiceId);
        }
    }

    public String getStatus() {
        return m_state.toString();
    }

    public boolean isRegistered() {
        return m_state.isRegistered();
    }

    public boolean isStarted() {
        return m_state.isStarted();
    }

    public void pollService(final Integer polledServiceId) {
        m_state.pollService(polledServiceId);
    }

    public void register(final String monitoringLocation) {
        m_state.register(monitoringLocation);
    }

    public void removeConfigurationChangedListener(final ConfigurationChangedListener listener) {
        m_configChangeListeners.remove(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        m_propertyChangeListeners.remove(listener);
    }

    public void removeServicePollStateChangedListener(final ServicePollStateChangedListener listener) {
        m_servicePollStateChangedListeners.remove(listener);
    }

    public void setInitialPollTime(final Integer polledServiceId, final Date initialPollTime) {
        final ServicePollState pollState = getServicePollState(polledServiceId);
        pollState.setInitialPollTime(initialPollTime);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
    }

    public void setMonitorId(final Integer monitorId) {
        m_pollerSettings.setMonitorId(monitorId);
    }

    public void setPollerBackEnd(final PollerBackEnd backEnd) {
        m_backEnd = backEnd;
    }

    public void setPollerSettings(final PollerSettings settings) {
        m_pollerSettings = settings;
    }

    public void setPollService(final PollService pollService) {
        m_pollService = pollService;
    }

    public void stop() {
        m_state.stop();
    }

    private void assertInitialized() {
        Assert.isTrue(isInitialized(), "afterProperties set has not been called");
    }

    private void assertNotNull(final Object propertyValue, final String propertyName) {
        Assert.state(propertyValue != null, propertyName + " must be set for instances of " + Poller.class);
    }

    @SuppressWarnings("unused")
    private void assertRegistered() {
        Assert.state(isRegistered(), "The poller must be registered before we can poll or get its configuration");
    }

    private void doLoadConfig() {
        Date oldTime = getCurrentConfigTimestamp();

        try {
            m_pollService.setServiceMonitorLocators(m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR));
            m_pollerConfiguration = m_backEnd.getPollerConfiguration(getMonitorId());

            synchronized (m_pollState) {

                int i = 0;
                m_pollState.clear();
                for (PolledService service : getPolledServices()) {
                    m_pollService.initialize(service);
                    m_pollState.put(service.getServiceId(), new ServicePollState(service, i++));
                }
            }

            fireConfigurationChange(oldTime, getCurrentConfigTimestamp());
        } catch (final Exception e) {
            LogUtils.warnf(this, e, "Unable to get updated poller configuration.");
            if (m_pollerConfiguration == null) {
                m_pollerConfiguration = new EmptyPollerConfiguration();
            }
        }
    }

    private PollStatus doPoll(final Integer polledServiceId) {

        final PolledService polledService = getPolledService(polledServiceId);
        if (polledService == null) {
            return null;
        }
        final PollStatus result = m_pollService.poll(polledService);
        return result;
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

    private boolean nullSafeEquals(final Object oldValue, final Object newValue) {
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

    private boolean isInitialized() {
        return m_state.isInitialized();
    }

    private void setState(final State newState) {
        final boolean started = isStarted();
        final boolean registered = isRegistered();
        final boolean paused = isPaused();
        final boolean disconnected = isDisconnected();
        final boolean exitNecessary = isExitNecessary();
        m_state = newState;
        firePropertyChange("exitNecessary", exitNecessary, isExitNecessary());
        firePropertyChange("started", started, isStarted());
        firePropertyChange("registered", registered, isRegistered());
        firePropertyChange("paused", paused, isPaused());
        firePropertyChange("disconnected", disconnected, isDisconnected());

    }

    private boolean isDisconnected() {
        return m_state.isDisconnected();
    }

    private boolean isPaused() {
        return m_state.isPaused();
    }

    public boolean isExitNecessary() {
        return m_state.isExitNecessary();
    }

    private void updateServicePollState(final Integer polledServiceId, final PollStatus result) {
        final ServicePollState pollState = getServicePollState(polledServiceId);
        pollState.setLastPoll(result);
        fireServicePollStateChanged(pollState.getPolledService(), pollState.getIndex());
    }

}
