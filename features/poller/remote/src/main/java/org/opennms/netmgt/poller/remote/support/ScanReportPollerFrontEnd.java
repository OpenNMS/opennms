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
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.netmgt.model.ScanReportLog;
import org.opennms.netmgt.model.ScanReportPollResult;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.remote.ConfigurationChangedListener;
import org.opennms.netmgt.poller.remote.PollService;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.opennms.netmgt.poller.remote.ServicePollState;
import org.opennms.netmgt.poller.remote.ServicePollStateChangedListener;
import org.opennms.netmgt.poller.remote.TimeAdjustment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * <p>ScanReportPollerFrontEnd class.</p>
 *
 * @author Seth
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class ScanReportPollerFrontEnd implements PollerFrontEnd, InitializingBean, DisposableBean {

    private static final PolledService[] POLLED_SERVICE_ARRAY = new PolledService[0];

    public static enum ScanReportProperties {
        percentageComplete
    }

    private static final Logger LOG = LoggerFactory.getLogger(ScanReportPollerFrontEnd.class);

    private class Initial extends PollerFrontEndState {
        @Override
        public void initialize() {
            // Change to the 'registering' state
            setState(new Registering());
        }

        @Override
        public boolean isRegistered() { return false; }

        @Override
        public boolean isStarted() { return false; }
    }

    private class Registering extends PollerFrontEndState {
        @Override
        public boolean isRegistered() { return false; }

        @Override
        public boolean isStarted() { return false; }

        @Override
        public void register(final String location) {
            try {
                // Set the location value
                doRegister(location);
                // Change the state to running so we're ready to execute polls
                setState(new Running());
                // Execute the scans
                performServiceScans();
            } catch (final Throwable e) {
                LOG.warn("Unable to register.", e);
                setState(new FatalExceptionOccurred(e));
            }
        }
    }

    private class Running extends PollerFrontEndState {
        @Override
        public boolean isRegistered() { return true; }

        @Override
        public boolean isStarted() { return true; }

        @Override
        public void pollService(final Integer polledServiceId) {
            // Don't do scheduled polls
        }
    }

    private PollerFrontEndState m_state = new Initial();

    // injected dependencies
    private PollerBackEnd m_backEnd;

    private PollService m_pollService;

    private TimeAdjustment m_timeAdjustment;

    // listeners
    private List<PropertyChangeListener> m_propertyChangeListeners = new LinkedList<>();

    // current configuration
    private PollerConfiguration m_pollerConfiguration;

    private Map<String, String> m_metadata = Collections.emptyMap();

    private Set<String> m_selectedApplications = null;

    private String m_location;

    /** {@inheritDoc} */
    @Override
    public void addConfigurationChangedListener(ConfigurationChangedListener l) {
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        m_propertyChangeListeners.add(0, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addServicePollStateChangedListener(final ServicePollStateChangedListener listener) {
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
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() {
        // Do nothing
    }

    /**
     * <p>doRegister</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    private void doRegister(final String location) {
        m_location = location;
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
     * <p>getMonitorName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getMonitorName() {
        return null;
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
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public ServicePollState getServicePollState(int polledServiceId) {
        return null;
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
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        m_propertyChangeListeners.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeServicePollStateChangedListener(final ServicePollStateChangedListener listener) {
    }

    /** {@inheritDoc} */
    @Override
    public void setInitialPollTime(final Integer polledServiceId, final Date initialPollTime) {
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

    private static void assertNotNull(final Object propertyValue, final String propertyName) {
        Assert.state(propertyValue != null, propertyName + " must be set for instances of " + ScanReportPollerFrontEnd.class.getName());
    }

    /**
     * This method can be used to filter PolledServices based on their assigned Applications. 
     * 
     * @param service
     * @param applicationNames
     * @return
     */
    private static boolean matchesApplications(PolledService service, Collection<String> applicationNames) {
        if (applicationNames == null || applicationNames.size() < 1) {
            return true;
        }
        for (String application : service.getApplications()) {
            if (applicationNames.contains(application)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform all scans for a given location and return the results as a {@link ScanReport}
     * object. To filter by application, specify the selected applications by using the
     * {@link #setSelectedApplications(Set)} method.
     */
    private void performServiceScans() {

        firePropertyChange(ScanReportProperties.percentageComplete.toString(), null, 0.0);

        ScanReport scanReport = new ScanReport();
        scanReport.setLocation(m_location);
        //scanReport.addProperty("monitoring-system-id", getMonitoringSystemId());
        scanReport.setTimestamp(new Date());

        // Add all of the OS and connection metadata to the scan report
        for (final Map.Entry<String,String> entry : getDetails().entrySet()) {
            scanReport.addProperty(entry);
        }
        // Add all of the metadata to the scan report
        for (final Map.Entry<String,String> entry : m_metadata.entrySet()) {
            scanReport.addProperty(entry);
        }
        // Add the selected applications as scan report metadata
        if (m_selectedApplications != null && m_selectedApplications.size() > 0) {
            scanReport.addProperty("applications", m_selectedApplications.stream().collect(Collectors.joining(", ")));
        }

        // Create a log appender that will capture log output to the root logger
        Log4j2StringAppender appender = Log4j2StringAppender.createAppender();
        appender.start();

        try {
            m_pollService.setServiceMonitorLocators(m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR));
            m_pollerConfiguration = retrieveLatestConfiguration();

            appender.addToLogger(LogManager.ROOT_LOGGER_NAME, Level.DEBUG);

            Set<PolledService> polledServices = getPolledServices().stream().filter(s -> matchesApplications(s, m_selectedApplications)).collect(Collectors.toSet());
            PolledService[] services = polledServices.toArray(POLLED_SERVICE_ARRAY);

            LOG.debug("Polling {} services.", services.length);
            for (int i = 0; i < services.length; i++) {
                PolledService service = services[i];

                try {
                    final PollStatus result = doPoll(service);
                    if (result == null) {
                        LOG.warn("Null poll result for service {}", service.getServiceId());
                    } else {
                        LOG.info(
                                 new ToStringBuilder(this)
                                 .append("statusName", result.getStatusName())
                                 .append("reason", result.getReason())
                                 .toString()
                                );
                        scanReport.addPollResult(new ScanReportPollResult(service.getSvcName(), service.getServiceId(), service.getNodeLabel(), service.getNodeId(), service.getIpAddr(), result));
                    }
                } catch (Throwable e) {
                    LOG.error("Unexpected exception occurred while polling service ID {}", service.getServiceId(), e);
                    setState(new FatalExceptionOccurred(e));
                }

                firePropertyChange(ScanReportProperties.percentageComplete.toString(), null, ((double)i / (double)services.length));
            }
        } catch (final Throwable e) {
            LOG.error("Error while performing scan", e);
        } finally {
            // Remove the log appender from the root logger
            appender.removeFromLogger(LogManager.ROOT_LOGGER_NAME);
        }

        // Set the percentage complete to 100%
        firePropertyChange(ScanReportProperties.percentageComplete.toString(), null, 1.0);

        scanReport.setLog(new ScanReportLog(scanReport.getId(), appender.getOutput()));

        LOG.debug("Returning scan report: {}", scanReport);

        /*
        LOG.debug("=============== Scan report log START ===============");
        LOG.debug("Scan report log: '{}'", appender.getOutput());
        LOG.debug("=============== Scan report log END ===============");
         */

        // Fire an exitNecessary event with the scanReport as the parameter
        firePropertyChange(PollerFrontEndStates.exitNecessary.toString(), null, scanReport);
    }

    private PollerConfiguration retrieveLatestConfiguration() {
        PollerConfiguration config = m_backEnd.getPollerConfigurationForLocation(m_location);
        m_timeAdjustment.setMasterTime(config.getServerTime());
        return config;
    }

    private PollStatus doPoll(final PolledService polledService) {
        return m_pollService.poll(polledService);
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

    private void setState(final PollerFrontEndState newState) {
        final boolean started = isStarted();
        final boolean registered = isRegistered();
        m_state = newState;
        firePropertyChange(PollerFrontEndStates.started.toString(), started, isStarted());
        firePropertyChange(PollerFrontEndStates.registered.toString(), registered, isRegistered());

    }

    public void setMetadata(final Map<String,String> metadata) {
        m_metadata = metadata;
    }

    public void setSelectedApplications(final Set<String> applications) {
        m_selectedApplications = applications;
    }

    @Override
    public void checkConfig() {
    }

    @Override
    public void initialize() {
        m_state.initialize();
    }

    @Override
    public boolean isExitNecessary() {
        return false;
    }

    @Override
    public boolean isDisconnected() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void stop() {
        // Do nothing
    }
}
