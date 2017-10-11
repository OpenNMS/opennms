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

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Criteria.LockType;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.LtRestriction;
import org.opennms.core.criteria.restrictions.NotNullRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.ScanReportDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.netmgt.model.ScanReportPollResult;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.OnmsPollModel;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.poller.remote.PollerTheme;
import org.opennms.netmgt.poller.remote.RemoteHostThreadLocal;
import org.opennms.netmgt.poller.remote.metadata.MetadataField;
import org.opennms.netmgt.poller.remote.metadata.MetadataFieldReader;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>DefaultPollerBackEnd class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional
public class DefaultPollerBackEnd implements PollerBackEnd, SpringServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollerBackEnd.class);

    public static final int HEARTBEAT_STEP_MULTIPLIER = 2;

    public static final String PARM_SCAN_REPORT_ID = "scanReportId";
    public static final String PARM_SCAN_REPORT_LOCATION = "scanReportLocation";
    public static final String PARM_SCAN_REPORT_FAILURE_MESSAGE = "scanReportFailureMessage";

    private final MetadataFieldReader m_metadataFieldReader = new MetadataFieldReader();

    private static class SimplePollerConfiguration implements PollerConfiguration, Serializable {

        /**
         * DO NOT CHANGE!
         * This class is serialized by remote poller communications.
         */
        private static final long serialVersionUID = 2L;

        private Date m_timestamp;
        private PolledService[] m_polledServices;
        private long m_serverTime = 0;

        SimplePollerConfiguration(final Date timestamp, final PolledService[] polledSvcs) {
            m_timestamp = timestamp;
            m_polledServices = Arrays.copyOf(polledSvcs, polledSvcs.length);
            m_serverTime = System.currentTimeMillis();
        }

        /**
         * This construct uses the existing data but updates the server timestamp
         */
        public SimplePollerConfiguration(SimplePollerConfiguration... pollerConfiguration) {
            this(getNewestTimestamp(pollerConfiguration), combinePolledServices(pollerConfiguration));
        }

        private static Date getNewestTimestamp(SimplePollerConfiguration... pollerConfigurations) {
            if (pollerConfigurations == null || pollerConfigurations.length < 1) {
                return new Date(0);
            }
            Date retval = new Date(0);
            for (SimplePollerConfiguration config : pollerConfigurations) {
                Date current = config.getConfigurationTimestamp();
                if (retval.before(current)) {
                    retval = current;
                }
            }
            return retval;
        }

        private static PolledService[] combinePolledServices(SimplePollerConfiguration... pollerConfigurations) {
            if (pollerConfigurations == null || pollerConfigurations.length < 1) {
                return new PolledService[0];
            }
            Set<PolledService> retval = new TreeSet<>();
            for (SimplePollerConfiguration config : pollerConfigurations) {
                PolledService[] services = config.getPolledServices();
                retval.addAll(Arrays.asList(services == null ? new PolledService[0] : services));
            }
            return retval.toArray(new PolledService[0]);
        }

        @Override
        public Date getConfigurationTimestamp() {
            return m_timestamp;
        }

        @Override
        public PolledService[] getPolledServices() {
            return m_polledServices;
        }

        @Override
        public long getServerTime() {
            return m_serverTime;
        }
    }
    private MonitoringLocationDao m_monitoringLocationDao;
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private ScanReportDao m_scanReportDao;
    private EventIpcManager m_eventIpcManager;
    private PollerConfig m_pollerConfig;
    private TimeKeeper m_timeKeeper;
    private int m_disconnectedTimeout;

    @Autowired
    private PersisterFactory m_persisterFactory;

    private long m_minimumConfigurationReloadInterval;

    private final AtomicReference<Date> m_configurationTimestamp = new AtomicReference<>();
    private final AtomicReference<ConcurrentHashMap<String, SimplePollerConfiguration>> m_configCache = new AtomicReference<ConcurrentHashMap<String,SimplePollerConfiguration>>();

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_locMonDao, "The LocationMonitorDao must be set");
        Assert.notNull(m_monSvcDao, "The MonitoredServiceDao must be set");
        Assert.notNull(m_pollerConfig, "The PollerConfig must be set");
        Assert.notNull(m_timeKeeper, "The timeKeeper must be set");
        Assert.notNull(m_eventIpcManager, "The eventIpcManager must be set");
        Assert.state(m_disconnectedTimeout > 0, "the disconnectedTimeout property must be set");
        Assert.notNull(m_persisterFactory, "The persisterFactory must be set");

        m_minimumConfigurationReloadInterval = Long.getLong("opennms.pollerBackend.minimumConfigurationReloadInterval", 300000L).longValue();

        configurationUpdated();
    }

    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void start() {
        // Nothing to do: job scheduling and RMI export is done externally
    }

    /**
     * <p>destroy</p>
     */
    @Override
    public void destroy() {
        // Nothing to do
    }

    /**
     * <p>checkForDisconnectedMonitors</p>
     */
    @Override
    public void checkForDisconnectedMonitors() {

        LOG.debug("Checking for disconnected monitors: disconnectedTimeout = {}", m_disconnectedTimeout);

        try {
            final Date now = m_timeKeeper.getCurrentDate();
            final Date earliestAcceptable = new Date(now.getTime() - m_disconnectedTimeout);

            final Criteria criteria = new Criteria(OnmsLocationMonitor.class);
            criteria.addRestriction(new EqRestriction("status", MonitorStatus.STARTED));
            criteria.addRestriction(new NotNullRestriction("lastUpdated"));
            criteria.addRestriction(new LtRestriction("lastUpdated", earliestAcceptable));
            // Lock all of the records for update since we will be marking them as DISCONNECTED
            criteria.setLockType(LockType.PESSIMISTIC_READ);

            final Collection<OnmsLocationMonitor> monitors = m_locMonDao.findMatching(criteria);

            LOG.debug("Found {} monitor(s) that are transitioning to disconnected state", monitors.size());

            for (final OnmsLocationMonitor monitor : monitors) {
                LOG.debug("Monitor {} has stopped responding", monitor.getName());
                monitor.setStatus(MonitorStatus.DISCONNECTED);
                m_locMonDao.update(monitor);

                sendDisconnectedEvent(monitor);
            }
        } catch (final Throwable e) {
            LOG.warn("An error occurred checking for disconnected monitors.", e);
        }
    }

    private MonitorStatus checkForGlobalConfigChange(final Date currentConfigurationVersion) {
        if (configurationUpdateIsNeeded(currentConfigurationVersion)) {
            return MonitorStatus.CONFIG_CHANGED;
        } else {
            return MonitorStatus.STARTED;
        }
    }

    private boolean configurationUpdateIsNeeded(final Date currentConfigurationVersion) {
        if (configIntervalExceedsMinimalInterval(currentConfigurationVersion)) {
            return m_configurationTimestamp.get().after(currentConfigurationVersion);
        } else {
            return false;
        }
    }

    private boolean configIntervalExceedsMinimalInterval(final Date currentConfigurationVersion) {
        return m_minimumConfigurationReloadInterval > 0 && (m_timeKeeper.getCurrentTime() - currentConfigurationVersion.getTime()) > m_minimumConfigurationReloadInterval;
    }

    /**
     * <p>configurationUpdated</p>
     */
    @Override
    public void configurationUpdated() {
        m_configurationTimestamp.set(m_timeKeeper.getCurrentDate());
        m_configCache.set(new ConcurrentHashMap<String, SimplePollerConfiguration>());
    }

    private static EventBuilder createEventBuilder(final OnmsLocationMonitor mon, final String uei) {
        final EventBuilder eventBuilder = new EventBuilder(uei, "PollerBackEnd")
                .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, mon.getId())
                .addParam(EventConstants.PARM_LOCATION, mon.getLocation());
        return eventBuilder;
    }

    private boolean databaseStatusChanged(final OnmsLocationSpecificStatus currentStatus, final OnmsLocationSpecificStatus newStatus) {
        return currentStatus == null || !currentStatus.getPollResult().equals(newStatus.getPollResult());
    }

    private Date getConfigurationTimestamp() {
        return m_configurationTimestamp.get();
    }

    /**
     * <p>getMonitoringLocations</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Transactional(readOnly=true)
    @Override
    public Collection<OnmsMonitoringLocation> getMonitoringLocations() {
        return m_monitoringLocationDao.findAll();
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public String getMonitorName(final String locationMonitorId) {
        final OnmsLocationMonitor locationMonitor = m_locMonDao.load(locationMonitorId);
        return locationMonitor.getName();
    }

    protected static Map<String, Object> getParameterMap(final Service serviceConfig) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        for (final Parameter serviceParm : serviceConfig.getParameters()) {
            String value = serviceParm.getValue();
            if (value == null) {
                final Object o = serviceParm.getAnyObject();
                if (o == null) {
                    value = "";
                } else if (o instanceof PageSequence) {
                    // The PageSequenceMonitor uses PageSequence type parameters in the service definition
                    // These need to be marshalled to XML before being sent to the PollerFrontEnd
                    value = JaxbUtils.marshal(o);
                } else {
                    value = o.toString();
                }
            }

            paramMap.put(serviceParm.getKey(), value);
        }
        return paramMap;
    }

    /**
     * @deprecated Use {@link #getPoller(OnmsMonitoringLocation)} instead.
     * 
     * @see http://issues.opennms.org/browse/PB-36
     */
    @Transactional(readOnly=true)
    @Override
    public PollerConfiguration getPollerConfiguration(final String locationMonitorId) {
        final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            LOG.warn("No location monitor found for location monitor ID {}", locationMonitorId);
            // the monitor has been deleted we'll pick this in up on the next config check
            return new EmptyPollerConfiguration();
        }

        return getPollerConfigurationForLocation(mon.getLocation());
    }

    @Transactional(readOnly=true)
    @Override
    public PollerConfiguration getPollerConfigurationForLocation(final String location) {
        try {
            List<String> pollingPackageNames = getPackageNameForLocation(location);
            LOG.debug("Location {} has polling packages: {}", location, pollingPackageNames);

            List<SimplePollerConfiguration> addMe = new ArrayList<>();
            for (String pollingPackageName : pollingPackageNames) {
                ConcurrentHashMap<String, SimplePollerConfiguration> cache = m_configCache.get();
                SimplePollerConfiguration pollerConfiguration = cache.get(pollingPackageName);
                if (pollerConfiguration == null) {
                    pollerConfiguration = createPollerConfiguration(pollingPackageName);
                    SimplePollerConfiguration configInCache = cache.putIfAbsent(pollingPackageName, pollerConfiguration);
                    // Make sure that we get the up-to-date value out of the ConcurrentHashMap
                    if (configInCache != null) {
                        pollerConfiguration = configInCache;
                    }
                }
                addMe.add(pollerConfiguration);
            }

            // construct a copy so the serverTime gets updated (and avoid threading issues)
            return new SimplePollerConfiguration(addMe.toArray(new SimplePollerConfiguration[0]));
        } catch (final Exception e) {
            LOG.warn("An error occurred retrieving the poller configuration for location {}", location, e);
            return new EmptyPollerConfiguration();
        }
    }

    @Transactional(readOnly=true)
    @Override
    public Set<String> getApplicationsForLocation(final String location) {
        final Set<String> retval = new HashSet<>();
        PollerConfiguration config = getPollerConfigurationForLocation(location);
        for (PolledService service : config.getPolledServices()) {
            retval.addAll(service.getApplications());
        }
        return Collections.unmodifiableSet(retval);
    }

    private SimplePollerConfiguration createPollerConfiguration(String pollingPackageName) {
        final Package pkg = getPollingPackage(pollingPackageName);

        final ServiceSelector selector = m_pollerConfig.getServiceSelectorForPackage(pkg);
        final Collection<OnmsMonitoredService> services = m_monSvcDao.findMatchingServices(selector);
        final List<PolledService> configs = new ArrayList<PolledService>(services.size());

        LOG.debug("Found {} services in polling package {}", services.size(), pollingPackageName);

        for (final OnmsMonitoredService monSvc : services) {
            final Service serviceConfig = m_pollerConfig.getServiceInPackage(monSvc.getServiceName(), pkg);
            final long interval = serviceConfig.getInterval();
            final Map<String, Object> parameters = getParameterMap(serviceConfig);

            if (LOG.isTraceEnabled()) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    LOG.trace("Service {} has parameter {} with type {} and value: {}",
                              monSvc.getServiceName(), entry.getKey(), entry.getValue() != null ? entry.getValue().getClass().getCanonicalName() : "null", entry.getValue());
                }
            }

            configs.add(new PolledService(monSvc, parameters, new OnmsPollModel(interval)));
        }

        Collections.sort(configs);
        return new SimplePollerConfiguration(getConfigurationTimestamp(), configs.toArray(new PolledService[configs.size()]));
    }

    private Package getPollingPackageForMonitorAndService(final OnmsLocationMonitor mon, OnmsMonitoredService monSvc) {
        List<String> pollingPackageNames = getPackageName(mon);
        for (String pollingPackageName : pollingPackageNames) {
            Package pkg = getPollingPackage(pollingPackageName);
            if (m_pollerConfig.getServiceInPackage(monSvc.getServiceName(), pkg) != null) {
                return pkg;
            }
        }
        throw new IllegalStateException("Could not find package from monitor " + mon.getName() + " that contains service " + monSvc.getServiceName());
    }

    private Package getPollingPackage(String pollingPackageName) {
        final Package pkg = m_pollerConfig.getPackage(pollingPackageName);
        if (pkg == null) {
            throw new IllegalStateException("Package "+pollingPackageName+" does not exist");
        }
        return pkg;
    }

    /**
     * @deprecated Use {@link #getPackageName(OnmsMonitoringLocation)} instead.
     * 
     * @see http://issues.opennms.org/browse/PB-36
     */
    private List<String> getPackageName(final OnmsLocationMonitor mon) {
        return getPackageNameForLocation(mon.getLocation());
    }

    private List<String> getPackageNameForLocation(final String location) {
        final OnmsMonitoringLocation def = m_monitoringLocationDao.get(location);
        if (def == null) {
            throw new IllegalStateException("Location definition '" + location + "' could not be found");
        }
        return def.getPollingPackageNames();
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(final DistributionContext context) {
        try {
            final List<ServiceMonitorLocator> locators = new ArrayList<>();
            final List<String> ex = Arrays.asList(System.getProperty("excludeServiceMonitorsFromRemotePoller", "").trim().split("\\s*,\\s*"));

            for (final ServiceMonitorLocator locator : m_pollerConfig.getServiceMonitorLocators(context)) {
                if (!ex.contains(locator.getServiceName())) {
                    locators.add(locator);
                }
            }

            LOG.debug("getServiceMonitorLocators: Returning {} locators", locators.size());
            return locators;
        } catch (final Exception e) {
            LOG.warn("An error occurred getting the service monitor locators for distribution context: {}", context, e);
            return Collections.emptyList();
        }
    }

    private boolean logicalStatusChanged(final OnmsLocationSpecificStatus currentStatus, final OnmsLocationSpecificStatus newStatus) {
        return currentStatus != null || (!newStatus.getPollResult().isAvailable());
    }


    /** {@inheritDoc} */
    @Override
    public MonitorStatus pollerCheckingIn(final String locationMonitorId, final Date currentConfigurationVersion) {
        try {
            final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
            if (mon == null) {
                LOG.debug("Deleted monitor checked in with ID {}", locationMonitorId);
                return MonitorStatus.DELETED;
            }

            return updateMonitorState(mon, currentConfigurationVersion);
        } catch (final Throwable e) {
            LOG.warn("An error occurred while checking in.", e);
            return MonitorStatus.DISCONNECTED;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean pollerStarting(final String locationMonitorId, final Map<String, String> pollerDetails) {
        final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            return false;
        }
        mon.setStatus(MonitorStatus.STARTED);
        mon.setLastUpdated(m_timeKeeper.getCurrentDate());

        updateConnectionHostDetails(mon, pollerDetails);

        m_locMonDao.update(mon);

        sendMonitorStartedEvent(mon);

        return true;
    }

    protected void updateConnectionHostDetails(final OnmsLocationMonitor mon, final Map<String, String> pollerDetails) {
        final Map<String,String> allDetails = new HashMap<String,String>();
        if (pollerDetails != null) {
            allDetails.putAll(pollerDetails);
        }

        String oldConnectionHostAddress = allDetails.get(PollerBackEnd.CONNECTION_HOST_ADDRESS_KEY);
        String newConnectionHostAddress = null;

        // This value can be either an IP address or a hostname
        String remoteHost = RemoteHostThreadLocal.INSTANCE.get();
        if (remoteHost != null) {
            remoteHost = remoteHost.trim();
            allDetails.put(PollerBackEnd.CONNECTION_HOST_NAME_KEY, remoteHost);
            try {
                InetAddress addr = InetAddressUtils.getInetAddress(remoteHost);
                newConnectionHostAddress = InetAddressUtils.str(addr);
                // Look up the IP address for the name
                allDetails.put(PollerBackEnd.CONNECTION_HOST_ADDRESS_KEY, newConnectionHostAddress);
                // Reverse-lookup the name (in case the value was an IP address before)
                if (remoteHost.equals(newConnectionHostAddress)) {
                    allDetails.put(PollerBackEnd.CONNECTION_HOST_NAME_KEY, addr.getHostName());
                }
            } catch (Throwable e) {
                // In case there is an UnknownHostException
            }
        }
        mon.setProperties(allDetails);

        if (oldConnectionHostAddress == null) {
            if (newConnectionHostAddress != null) {
                sendMonitorRemoteAddressChangedEvent(mon, oldConnectionHostAddress, newConnectionHostAddress);
            }
        } else {
            if (!oldConnectionHostAddress.equals(newConnectionHostAddress)) {
                sendMonitorRemoteAddressChangedEvent(mon, oldConnectionHostAddress, newConnectionHostAddress);
            }
        }
    }

    private void sendMonitorRemoteAddressChangedEvent(OnmsLocationMonitor mon, String oldRemoteHostAddress, String newRemoteHostAddress) {
        m_eventIpcManager.sendNow(createEventBuilder(mon, EventConstants.LOCATION_MONITOR_CONNECTION_ADDRESS_CHANGED_UEI)
                                  .addParam("oldConnectionHostAddress", oldRemoteHostAddress)
                                  .addParam("newConnectionHostAddress", newRemoteHostAddress).getEvent()
                );
    }

    /** {@inheritDoc} */
    @Override
    public void pollerStopping(final String locationMonitorId) {
        final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            LOG.info("pollerStopping was called for location monitor ID {} which does not exist", locationMonitorId);
            return;
        }

        if (mon.getStatus() != MonitorStatus.PAUSED)
        {
            mon.setStatus(MonitorStatus.STOPPED);
        }
        mon.setLastUpdated(m_timeKeeper.getCurrentDate());
        m_locMonDao.update(mon);

        sendMonitorStoppedEvent(mon);
    }

    private void processStatusChange(final OnmsLocationSpecificStatus currentStatus, final OnmsLocationSpecificStatus newStatus) {
        if (databaseStatusChanged(currentStatus, newStatus)) {
            m_locMonDao.saveStatusChange(newStatus);

            final PollStatus pollResult = newStatus.getPollResult();

            // if we don't know the current status only send an event if it is not up
            if (logicalStatusChanged(currentStatus, newStatus)) {
                sendRegainedOrLostServiceEvent(newStatus, pollResult);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String registerLocationMonitor(final String monitoringLocationId) {
        final OnmsMonitoringLocation def = m_monitoringLocationDao.get(monitoringLocationId);
        if (def == null) {
            throw new ObjectRetrievalFailureException(OnmsMonitoringLocation.class, monitoringLocationId, "Location monitor definition with the id '" + monitoringLocationId + "' not found", null);
        }
        final OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setId(UUID.randomUUID().toString());
        mon.setLocation(def.getLocationName());
        mon.setStatus(MonitorStatus.REGISTERED);

        m_locMonDao.save(mon);

        sendMonitorRegisteredEvent(mon);
        return mon.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void reportResult(final String locationMonitorId, final int serviceId, final PollStatus pollResult) {
        final OnmsLocationMonitor locationMonitor;
        try {
            locationMonitor = m_locMonDao.get(locationMonitorId);
        } catch (final Exception e) {
            LOG.info("Unable to report result for location monitor ID {}: Location monitor does not exist.", locationMonitorId, e);
            return;
        }
        if (locationMonitor == null) {
            LOG.info("Unable to report result for location monitor ID {}: Location monitor does not exist.", locationMonitorId);
            return;
        }

        final OnmsMonitoredService monSvc;
        try {
            monSvc = m_monSvcDao.get(serviceId);
        } catch (final Exception e) {
            LOG.warn("Unable to report result for location monitor ID {}, monitored service ID {}: Monitored service does not exist.", locationMonitorId, serviceId, e);
            return;
        }
        if (monSvc == null) {
            LOG.warn("Unable to report result for location monitor ID {}, monitored service ID {}: Monitored service does not exist.", locationMonitorId, serviceId);
            return;
        }
        if (pollResult == null) {
            LOG.warn("Unable to report result for location monitor ID {}, monitored service ID {}: Poll result is null!", locationMonitorId, serviceId);
            return;
        }

        final OnmsLocationSpecificStatus newStatus = new OnmsLocationSpecificStatus(locationMonitor, monSvc, pollResult);

        try {
            if (newStatus.getPollResult().getResponseTime() != null) {
                final Package pkg = getPollingPackageForMonitorAndService(locationMonitor, monSvc);
                saveResponseTimeData(locationMonitorId, monSvc, newStatus.getPollResult().getResponseTime(), pkg);
            }
        } catch (final Exception e) {
            LOG.error("Unable to save response time data for location monitor ID {}, monitored service ID {}.", locationMonitorId, serviceId, e);
        }

        try {
            final OnmsLocationSpecificStatus currentStatus = m_locMonDao.getMostRecentStatusChange(locationMonitor, monSvc);
            processStatusChange(currentStatus, newStatus);
        } catch (final Exception e) {
            LOG.error("Unable to save result for location monitor ID {}, monitored service ID {}.", locationMonitorId, serviceId, e);
        }
    }

    /**
     * <p>saveResponseTimeData</p>
     *
     * @param locationMonitorId a {@link java.lang.String} object.
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @param responseTime a double.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    @Override
    public void saveResponseTimeData(final String locationMonitorId, final OnmsMonitoredService monSvc, final double responseTime, final Package pkg) {
        final String svcName = monSvc.getServiceName();
        final Service svc = m_pollerConfig.getServiceInPackage(svcName, pkg);

        String dsName = getServiceParameter(svc, "ds-name");
        if (dsName == null) {
            dsName = PollStatus.PROPERTY_RESPONSE_TIME;
        }

        String rrdBaseName = getServiceParameter(svc, "rrd-base-name");
        if (rrdBaseName == null) {
            rrdBaseName = dsName;
        }

        final String rrdRepository = getServiceParameter(svc, "rrd-repository");
        if (rrdRepository == null) {
            return;
        }

        RrdRepository repository = new RrdRepository();
        repository.setStep(m_pollerConfig.getStep(pkg));
        repository.setHeartBeat(repository.getStep() * HEARTBEAT_STEP_MULTIPLIER);
        repository.setRraList(m_pollerConfig.getRRAList(pkg));
        repository.setRrdBaseDir(new File(rrdRepository));

        DistributedLatencyCollectionResource distributedLatencyResource = new DistributedLatencyCollectionResource(locationMonitorId, InetAddressUtils.toIpAddrString(monSvc.getIpAddress()));
        DistributedLatencyCollectionAttributeType distributedLatencyType = new DistributedLatencyCollectionAttributeType(rrdBaseName, dsName);
        distributedLatencyResource.addAttribute(new DistributedLatencyCollectionAttribute(distributedLatencyResource,
                                                                                          distributedLatencyType, responseTime));

        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        CollectionSetVisitor persister = m_persisterFactory.createPersister(params, repository, false, true, true);

        SingleResourceCollectionSet collectionSet = new SingleResourceCollectionSet(distributedLatencyResource, new Date());
        collectionSet.setStatus(CollectionStatus.SUCCEEDED);
        collectionSet.visit(persister);
    }

    private String getServiceParameter(final Service svc, final String key) {
        for(final Parameter parm : m_pollerConfig.parameters(svc)) {
            if (key.equals(parm.getKey())) {
                if (parm.getValue() != null) {
                    return parm.getValue();
                } else if (parm.getAnyObject() != null) {
                    return parm.getAnyObject().toString();
                }
            }
        }
        return null;
    }

    private void sendMonitorRegisteredEvent(final OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_REGISTERED_UEI);
    }

    private void sendDisconnectedEvent(final OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI);
    }

    private void sendEvent(final OnmsLocationMonitor mon, final String uei) {
        m_eventIpcManager.sendNow(createEventBuilder(mon, uei).getEvent());
    }

    private void sendMonitorStartedEvent(final OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_STARTED_UEI);
    }

    private void sendMonitorStoppedEvent(final OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_STOPPED_UEI);
    }

    private void sendReconnectedEvent(final OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_RECONNECTED_UEI);
    }

    private void sendSuccessfulScanReportEvent(final String reportId, final String locationName) {
        final EventBuilder eventBuilder = new EventBuilder(EventConstants.REMOTE_SUCCESSFUL_SCAN_REPORT_UEI, "PollerBackEnd");
        eventBuilder.addParam(PARM_SCAN_REPORT_ID, reportId);
        eventBuilder.addParam(PARM_SCAN_REPORT_LOCATION, locationName);

        m_eventIpcManager.sendNow(eventBuilder.getEvent());
    }

    private void sendUnsuccessfulScanReportEvent(final String reportId, final String locationName, final String failureMessage) {
        final EventBuilder eventBuilder = new EventBuilder(EventConstants.REMOTE_UNSUCCESSFUL_SCAN_REPORT_UEI, "PollerBackEnd");
        eventBuilder.addParam(PARM_SCAN_REPORT_ID, reportId);
        eventBuilder.addParam(PARM_SCAN_REPORT_LOCATION, locationName);
        eventBuilder.addParam(PARM_SCAN_REPORT_FAILURE_MESSAGE, failureMessage);

        m_eventIpcManager.sendNow(eventBuilder.getEvent());
    }

    private void sendRegainedOrLostServiceEvent(final OnmsLocationSpecificStatus newStatus, final PollStatus pollResult) {
        final String uei = pollResult.isAvailable() ? EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI : EventConstants.REMOTE_NODE_LOST_SERVICE_UEI;

        final EventBuilder builder = createEventBuilder(newStatus.getLocationMonitor(), uei)
                .setMonitoredService(newStatus.getMonitoredService());

        if (!pollResult.isAvailable() && pollResult.getReason() != null) {
            builder.addParam(EventConstants.PARM_LOSTSERVICE_REASON, pollResult.getReason());
        }

        m_eventIpcManager.sendNow(builder.getEvent());
    }

    /**
     * <p>setDisconnectedTimeout</p>
     *
     * @param disconnectedTimeout a int.
     */
    public void setDisconnectedTimeout(final int disconnectedTimeout) {
        m_disconnectedTimeout = disconnectedTimeout;

    }

    /**
     * <p>setMinimumConfigurationReloadInterval</p>
     *
     * @param value
     */
    public void setMinimumConfigurationReloadInterval(final long value) {
        m_minimumConfigurationReloadInterval = value;
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventIpcManager(final EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void setMonitoringLocationDao(final MonitoringLocationDao monitoringLocationDao) {
        m_monitoringLocationDao = monitoringLocationDao;
    }

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locMonDao a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(final LocationMonitorDao locMonDao) {
        m_locMonDao = locMonDao;
    }

    /**
     * <p>setMonitoredServiceDao</p>
     *
     * @param monSvcDao a {@link org.opennms.netmgt.dao.api.MonitoredServiceDao} object.
     */
    public void setMonitoredServiceDao(final MonitoredServiceDao monSvcDao) {
        m_monSvcDao = monSvcDao;
    }

    public void setScanReportDao(final ScanReportDao scanReportDao) {
        m_scanReportDao = scanReportDao;
    }

    /**
     * <p>setPollerConfig</p>
     *
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public void setPollerConfig(final PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    /**
     * <p>setTimeKeeper</p>
     *
     * @param timeKeeper a {@link org.opennms.netmgt.collection.api.TimeKeeper} object.
     */
    public void setTimeKeeper(final TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }

    public void setPersisterFactory(final PersisterFactory persisterFactory) {
        m_persisterFactory = persisterFactory;
    }

    private MonitorStatus updateMonitorState(final OnmsLocationMonitor mon, final Date currentConfigurationVersion) {
        try {
            switch(mon.getStatus()) {
            case DISCONNECTED:
                sendReconnectedEvent(mon);
                mon.setStatus(MonitorStatus.STARTED);
                return checkForGlobalConfigChange(currentConfigurationVersion);

            case STARTED:
                mon.setStatus(MonitorStatus.STARTED);
                return checkForGlobalConfigChange(currentConfigurationVersion);

            case PAUSED:
                mon.setStatus(MonitorStatus.PAUSED);
                return MonitorStatus.PAUSED;

            case CONFIG_CHANGED: 
                mon.setStatus(MonitorStatus.STARTED);
                return MonitorStatus.CONFIG_CHANGED;

            default:
                LOG.error("Unexpected monitor state for monitor: {}", mon);
                throw new IllegalStateException("Unexpected monitor state for monitor: "+mon);

            }
        } finally {
            mon.setLastUpdated(m_timeKeeper.getCurrentDate());
            updateConnectionHostDetails(mon, mon.getProperties());
            m_locMonDao.update(mon);
        }
    }

    public static final String FAILURE_SUMMARY_MESSAGE_FORMAT = "<p>%d out of %d service polls failed for the following reasons:</p>";
    public static final String FAILED_POLL_RESULT_MESSAGE_FORMAT = "<li><b>%s: %s: %s:</b> %s</li>";

    @Override
    public void reportSingleScan(final ScanReport report) {
        if (report == null) {
            throw new IllegalArgumentException("ScanReport cannot be null");
        }
        LOG.info("Scan report complete: {}", report);
        m_scanReportDao.save(report);

        if (report.getPollResults().stream().allMatch(a -> { return a.getPollStatus().isAvailable(); } )) {
            // If all polls returned 'available' then send the success event
            sendSuccessfulScanReportEvent(report.getId(), report.getLocation());
        } else {
            // Otherwise send the unsuccessful event
            int total = 0;
            int failed = 0;
            final StringBuilder failedPollResults = new StringBuilder();
            for (ScanReportPollResult result : report.getPollResults()) {
                total++;
                if (!result.getPollStatus().isAvailable()) {
                    failed++;
                    failedPollResults.append(String.format(FAILED_POLL_RESULT_MESSAGE_FORMAT, result.getNodeLabel(), result.getIpAddress(), result.getServiceName(), result.getPollStatus().getReason()));
                }
            }

            StringBuffer finalMessage = new StringBuffer();
            finalMessage.append(String.format(FAILURE_SUMMARY_MESSAGE_FORMAT, failed, total));
            finalMessage.append("<ul>");
            finalMessage.append(failedPollResults);
            finalMessage.append("</ul>");

            sendUnsuccessfulScanReportEvent(report.getId(), report.getLocation(), finalMessage.toString());
        }
    }

    @Override
    public PollerTheme getTheme() {
        return m_metadataFieldReader.getTheme();
    }

    @Override
    public Set<MetadataField> getMetadataFields() {
        try {
            return m_metadataFieldReader.getMetadataFields();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOG.warn("Failed to read metadata fields.", e);
            return Collections.emptySet();
        }
    }
}
