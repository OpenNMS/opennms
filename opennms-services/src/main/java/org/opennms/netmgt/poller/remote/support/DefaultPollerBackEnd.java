/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.OnmsPollModel;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.springframework.dao.PermissionDeniedDataAccessException;
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

    private static class SimplePollerConfiguration implements PollerConfiguration, Serializable {

        private static final long serialVersionUID = 2L;

        private Date m_timestamp;
        private PolledService[] m_polledServices;
        private long m_serverTime = 0;

        SimplePollerConfiguration(final Date timestamp, final PolledService[] polledSvcs) {
            m_timestamp = timestamp;
            m_polledServices = polledSvcs;
            m_serverTime = System.currentTimeMillis();
        }

        /**
         * This construct uses the existing data but updates the server timestamp
         */
        public SimplePollerConfiguration(SimplePollerConfiguration pollerConfiguration) {
            this(pollerConfiguration.getConfigurationTimestamp(), pollerConfiguration.getPolledServices());
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
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private EventIpcManager m_eventIpcManager;
    private PollerConfig m_pollerConfig;
    private TimeKeeper m_timeKeeper;
    private int m_disconnectedTimeout;

    private long m_minimumConfigurationReloadInterval;
    
    AtomicReference<Date> m_configurationTimestamp = new AtomicReference<Date>();
    AtomicReference<ConcurrentHashMap<String, SimplePollerConfiguration>> m_configCache = new AtomicReference<ConcurrentHashMap<String,SimplePollerConfiguration>>();

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_locMonDao, "The LocationMonitorDao must be set");
        Assert.notNull(m_monSvcDao, "The MonitoredServiceDao must be set");
        Assert.notNull(m_pollerConfig, "The PollerConfig must be set");
        Assert.notNull(m_timeKeeper, "The timeKeeper must be set");
        Assert.notNull(m_eventIpcManager, "The eventIpcManager must be set");
        Assert.state(m_disconnectedTimeout > 0, "the disconnectedTimeout property must be set");
        
        m_minimumConfigurationReloadInterval = Long.getLong("opennms.pollerBackend.minimumConfigurationReloadInterval", 300000L).longValue();
        
        configurationUpdated();
    }
    
    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void start() throws Exception {
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

        LogUtils.debugf(this, "Checking for disconnected monitors: disconnectedTimeout = %d", m_disconnectedTimeout);

        try {
	        final Date now = m_timeKeeper.getCurrentDate();
	        final Date earliestAcceptable = new Date(now.getTime() - m_disconnectedTimeout);
	
	        final Collection<OnmsLocationMonitor> monitors = m_locMonDao.findAll();
	        LogUtils.debugf(this, "Found %d monitors", monitors.size());
	
	        for (final OnmsLocationMonitor monitor : monitors) {
	            if (monitor.getStatus() == MonitorStatus.STARTED 
	                    && monitor.getLastCheckInTime() != null 
	                    && monitor.getLastCheckInTime().before(earliestAcceptable))
	            {
	                LogUtils.debugf(this, "Monitor %s has stopped responding", monitor.getName());
	                monitor.setStatus(MonitorStatus.DISCONNECTED);
	                m_locMonDao.update(monitor);
	
	                sendDisconnectedEvent(monitor);
	            } else {
	                LogUtils.debugf(this, "Monitor %s (%s) last responded at %s", monitor.getName(), monitor.getStatus(), monitor.getLastCheckInTime());
	            }
	        }
        } catch (final Exception e) {
        	LogUtils.warnf(this, e, "An error occurred checking for disconnected monitors.");
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

    private EventBuilder createEventBuilder(final OnmsLocationMonitor mon, final String uei) {
        final EventBuilder eventBuilder = new EventBuilder(uei, "PollerBackEnd")
            .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, mon.getId())
            .addParam(EventConstants.PARM_LOCATION, mon.getDefinitionName());
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
    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        return m_locMonDao.findAllMonitoringLocationDefinitions();
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public String getMonitorName(final int locationMonitorId) {
        final OnmsLocationMonitor locationMonitor = m_locMonDao.load(locationMonitorId);
        return locationMonitor.getName();
    }

    private Map<String, Object> getParameterMap(final Service serviceConfig) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        final Enumeration<Parameter> serviceParms = serviceConfig.enumerateParameter();
        while(serviceParms.hasMoreElements()) {
            final Parameter serviceParm = serviceParms.nextElement();

            String value = serviceParm.getValue();
            if (value == null) {
                value = (serviceParm.getAnyObject() == null ? "" : serviceParm.getAnyObject().toString());
            }

            paramMap.put(serviceParm.getKey(), value);
        }
        return paramMap;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public PollerConfiguration getPollerConfiguration(final int locationMonitorId) {
        try {
			final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
			if (mon == null) {
			    // the monitor has been deleted we'll pick this in up on the next config check
			    return new EmptyPollerConfiguration();
			}
			
            String pollingPackageName = getPackageName(mon);
            
            ConcurrentHashMap<String, SimplePollerConfiguration> cache = m_configCache.get();
            SimplePollerConfiguration pollerConfiguration = cache.get(pollingPackageName);
            if (pollerConfiguration == null) {
                pollerConfiguration = createPollerConfiguration(mon, pollingPackageName);
                SimplePollerConfiguration configInCache = cache.putIfAbsent(pollingPackageName, pollerConfiguration);
                // Make sure that we get the up-to-date value out of the ConcurrentHashMap
                if (configInCache != null) {
                    pollerConfiguration = configInCache;
                }
            }
            
            // construct a copy so the serverTime gets updated (and avoid threading issues)
            return new SimplePollerConfiguration(pollerConfiguration);
		} catch (final Exception e) {
			LogUtils.warnf(this, e, "An error occurred retrieving the poller configuration for location monitor ID %d", locationMonitorId);
			return new EmptyPollerConfiguration();
		}
    }

    private SimplePollerConfiguration createPollerConfiguration(
            final OnmsLocationMonitor mon, String pollingPackageName) {
        final Package pkg = getPollingPackage(pollingPackageName, mon.getDefinitionName());
        
        final ServiceSelector selector = m_pollerConfig.getServiceSelectorForPackage(pkg);
        final Collection<OnmsMonitoredService> services = m_monSvcDao.findMatchingServices(selector);
        final List<PolledService> configs = new ArrayList<PolledService>(services.size());

        LogUtils.debugf(this, "found %d services", services.size());

        for (final OnmsMonitoredService monSvc : services) {
            final Service serviceConfig = m_pollerConfig.getServiceInPackage(monSvc.getServiceName(), pkg);
            final long interval = serviceConfig.getInterval();
            final Map<String, Object> parameters = getParameterMap(serviceConfig);
            configs.add(new PolledService(monSvc, parameters, new OnmsPollModel(interval)));
        }

        Collections.sort(configs);
        SimplePollerConfiguration pollerConfiguration = new SimplePollerConfiguration(getConfigurationTimestamp(), configs.toArray(new PolledService[configs.size()]));
        return pollerConfiguration;
    }

    private Package getPollingPackageForMonitor(final OnmsLocationMonitor mon) {
        String pollingPackageName = getPackageName(mon);

        String definitionName = mon.getDefinitionName();
        return getPollingPackage(pollingPackageName, definitionName);
    }

    private Package getPollingPackage(String pollingPackageName,
            String definitionName) {
        final Package pkg = m_pollerConfig.getPackage(pollingPackageName);
        if (pkg == null) {
            throw new IllegalStateException("Package "+pollingPackageName+" does not exist as defined for monitoring location "+definitionName);
        }
        return pkg;
    }

    private String getPackageName(final OnmsLocationMonitor mon) {
        final OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(mon.getDefinitionName());
        if (def == null) {
            throw new IllegalStateException("Location definition '" + mon.getDefinitionName() + "' could not be found for location monitor ID " + mon.getId());
        }
        String pollingPackageName = def.getPollingPackageName();
        return pollingPackageName;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(final DistributionContext context) {
        try {
            final List<ServiceMonitorLocator> locators = new ArrayList<ServiceMonitorLocator>();
            final List<String> ex = Arrays.asList(System.getProperty("excludeServiceMonitorsFromRemotePoller", "").trim().split("\\s*,\\s*"));

            for (final ServiceMonitorLocator locator : m_pollerConfig.getServiceMonitorLocators(context)) {
                if (!ex.contains(locator.getServiceName())) {
                    locators.add(locator);
                }
            }
            
            LogUtils.debugf(this, "getServiceMonitorLocators: Returning %d locators", locators.size());
            return locators;
        } catch (final Exception e) {
            LogUtils.warnf(this, e, "An error occurred getting the service monitor locators for distribution context: %s", context);
            return Collections.emptyList();
        }
    }

    private boolean logicalStatusChanged(final OnmsLocationSpecificStatus currentStatus, final OnmsLocationSpecificStatus newStatus) {
        return currentStatus != null || (!newStatus.getPollResult().isAvailable());
    }


    /** {@inheritDoc} */
    @Override
    public MonitorStatus pollerCheckingIn(final int locationMonitorId, final Date currentConfigurationVersion) {
        try {
			final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
			if (mon == null) {
			    LogUtils.debugf(this, "Deleted monitor checked in with ID %d", locationMonitorId);
			    return MonitorStatus.DELETED;
			}

			return updateMonitorState(mon, currentConfigurationVersion);
		} catch (final Exception e) {
			LogUtils.warnf(this, e, "An error occurred while checking in.");
			return MonitorStatus.DISCONNECTED;
		}
    }

    /** {@inheritDoc} */
    @Override
    public boolean pollerStarting(final int locationMonitorId, final Map<String, String> pollerDetails) {
        final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            return false;
        }
        mon.setStatus(MonitorStatus.STARTED);
        mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
        mon.setDetails(pollerDetails);
        m_locMonDao.update(mon);

        sendMonitorStartedEvent(mon);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void pollerStopping(final int locationMonitorId) {
        final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            LogUtils.infof(this, "pollerStopping was called for location monitor ID %d which does not exist", locationMonitorId);
            return;
        }

        if (mon.getStatus() != MonitorStatus.PAUSED)
        {
        	mon.setStatus(MonitorStatus.STOPPED);
        }
        mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
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
    public int registerLocationMonitor(final String monitoringLocationId) {
        final OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(monitoringLocationId);
        if (def == null) {
            throw new ObjectRetrievalFailureException(OnmsMonitoringLocationDefinition.class, monitoringLocationId, "Location monitor definition with the id '" + monitoringLocationId + "' not found", null);
        }
        final OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setDefinitionName(def.getName());
        mon.setStatus(MonitorStatus.REGISTERED);

        m_locMonDao.save(mon);

        sendMonitorRegisteredEvent(mon);
        return mon.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void reportResult(final int locationMonitorId, final int serviceId, final PollStatus pollResult) {
        final OnmsLocationMonitor locationMonitor;
        try {
        	locationMonitor = m_locMonDao.get(locationMonitorId);
        } catch (final Exception e) {
            LogUtils.infof(this, e, "Unable to report result for location monitor ID %d: Location monitor does not exist.", locationMonitorId);
            return;
        }
        if (locationMonitor == null) {
            LogUtils.infof(this, "Unable to report result for location monitor ID %d: Location monitor does not exist.", locationMonitorId);
            return;
        }

        final OnmsMonitoredService monSvc;
        try {
        	monSvc = m_monSvcDao.get(serviceId);
        } catch (final Exception e) {
        	LogUtils.warnf(this, e, "Unable to report result for location monitor ID %d, monitored service ID %d: Monitored service does not exist.", locationMonitorId, serviceId); 
        	return;
        }
        if (monSvc == null) {
        	LogUtils.warnf(this, "Unable to report result for location monitor ID %d, monitored service ID %d: Monitored service does not exist.", locationMonitorId, serviceId); 
            return;
        }
        if (pollResult == null) {
        	LogUtils.warnf(this, "Unable to report result for location monitor ID %d, monitored service ID %d: Poll result is null!", locationMonitorId, serviceId);
        	return;
        }

        final OnmsLocationSpecificStatus newStatus = new OnmsLocationSpecificStatus(locationMonitor, monSvc, pollResult);

        try {
			if (newStatus.getPollResult().getResponseTime() != null) {
			    final Package pkg = getPollingPackageForMonitor(locationMonitor);
			    saveResponseTimeData(Integer.toString(locationMonitorId), monSvc, newStatus.getPollResult().getResponseTime(), pkg);
			}
		} catch (final Exception e) {
			LogUtils.errorf(this, e, "Unable to save response time data for location monitor ID %d, monitored service ID %d.", locationMonitorId, serviceId);
		}

		try {
	        final OnmsLocationSpecificStatus currentStatus = m_locMonDao.getMostRecentStatusChange(locationMonitor, monSvc);
	        processStatusChange(currentStatus, newStatus);
		} catch (final Exception e) {
			LogUtils.errorf(this, e, "Unable to save result for location monitor ID %d, monitored service ID %d.", locationMonitorId, serviceId);
		}
    }

    /**
     * <p>saveResponseTimeData</p>
     *
     * @param locationMonitor a {@link java.lang.String} object.
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @param responseTime a double.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    @Override
    public void saveResponseTimeData(final String locationMonitor, final OnmsMonitoredService monSvc, final double responseTime, final Package pkg) {
        final String svcName = monSvc.getServiceName();
        final Service svc = m_pollerConfig.getServiceInPackage(svcName, pkg);
        
        final String dsName = getServiceParameter(svc, "ds-name");
        if (dsName == null) {
            return;
        }
        
        final String rrdRepository = getServiceParameter(svc, "rrd-repository");
        if (rrdRepository == null) {
            return;
        }
        
        final String rrdDir = rrdRepository+File.separatorChar+"distributed"+File.separatorChar+locationMonitor+File.separator+str(monSvc.getIpAddress());

        try {
            final File rrdFile = new File(rrdDir, dsName);
            if (!rrdFile.exists()) {
                RrdUtils.createRRD(locationMonitor, rrdDir, dsName, m_pollerConfig.getStep(pkg), "GAUGE", 600, "U", "U", m_pollerConfig.getRRAList(pkg));
            }
            RrdUtils.updateRRD(locationMonitor, rrdDir, dsName, System.currentTimeMillis(), String.valueOf(responseTime));
        } catch (final RrdException e) {
            throw new PermissionDeniedDataAccessException("Unable to store rrdData from "+locationMonitor+" for service "+monSvc, e);
        }
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
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventIpcManager(final EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locMonDao a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(final LocationMonitorDao locMonDao) {
        m_locMonDao = locMonDao;
    }

    /**
     * <p>setMonitoredServiceDao</p>
     *
     * @param monSvcDao a {@link org.opennms.netmgt.dao.MonitoredServiceDao} object.
     */
    public void setMonitoredServiceDao(final MonitoredServiceDao monSvcDao) {
        m_monSvcDao = monSvcDao;
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
     * @param timeKeeper a {@link org.opennms.core.utils.TimeKeeper} object.
     */
    public void setTimeKeeper(final TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
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
                    LogUtils.errorf(this, "Unexpected monitor state for monitor: %s", mon);
                    throw new IllegalStateException("Unexpected monitor state for monitor: "+mon);

            }
        } finally {
            mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
            m_locMonDao.update(mon);
        }
    }
}
