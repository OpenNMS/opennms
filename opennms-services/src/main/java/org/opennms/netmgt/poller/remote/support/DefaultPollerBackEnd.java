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
 * Created: October 11, 2006
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
package org.opennms.netmgt.poller.remote.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.remote.OnmsPollModel;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional
public class DefaultPollerBackEnd implements PollerBackEnd, SpringServiceDaemon {

    private static class SimplePollerConfiguration implements PollerConfiguration, Serializable {

        private static final long serialVersionUID = 1L;

        private Date m_timestamp;
        private PolledService[] m_polledServices;

        SimplePollerConfiguration(final Date timestamp, final PolledService[] polledSvcs) {
            m_timestamp = timestamp;
            m_polledServices = polledSvcs;
        }

        public Date getConfigurationTimestamp() {
            return m_timestamp;
        }

        public PolledService[] getPolledServices() {
            return m_polledServices;
        }

    }
    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private EventIpcManager m_eventIpcManager;
    private PollerConfig m_pollerConfig;
    private TimeKeeper m_timeKeeper;
    private int m_disconnectedTimeout;

    private Date m_configurationTimestamp = null;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_locMonDao, "The LocationMonitorDao must be set");
        Assert.notNull(m_monSvcDao, "The MonitoredServiceDao must be set");
        Assert.notNull(m_pollerConfig, "The PollerConfig must be set");
        Assert.notNull(m_timeKeeper, "The timeKeeper must be set");
        Assert.notNull(m_eventIpcManager, "The eventIpcManager must be set");
        Assert.state(m_disconnectedTimeout > 0, "the disconnectedTimeout property must be set");

        m_configurationTimestamp = m_timeKeeper.getCurrentDate();
    }
    
    public void start() throws Exception {
        // Nothing to do: job scheduling and RMI export is done externally
    }

    public void checkForDisconnectedMonitors() {

        LogUtils.debugf(this, "Checking for disconnected monitors: disconnectedTimeout = %d", m_disconnectedTimeout);

        try {
	        final Date now = m_timeKeeper.getCurrentDate();
	        final Date earliestAcceptable = new Date(now.getTime() - m_disconnectedTimeout);
	
	        final Collection<OnmsLocationMonitor> monitors = m_locMonDao.findAll();
	        LogUtils.debugf(this, "Found %d monitors", monitors.size());
	
	        for (final OnmsLocationMonitor monitor : monitors) {
	            if (monitor.getStatus() == MonitorStatus.STARTED && monitor.getLastCheckInTime().before(earliestAcceptable)) {
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
        if (m_configurationTimestamp.after(currentConfigurationVersion)) {
            return MonitorStatus.CONFIG_CHANGED;
        } else {
            return MonitorStatus.STARTED;
        }
    }

    public void configurationUpdated() {
        m_configurationTimestamp = m_timeKeeper.getCurrentDate();
    }

    private EventBuilder createEventBuilder(final OnmsLocationMonitor mon, final String uei) {
        final EventBuilder eventBuilder = new EventBuilder(uei, "PollerBackEnd")
            .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, mon.getId());
        return eventBuilder;
    }

    private boolean databaseStatusChanged(final OnmsLocationSpecificStatus currentStatus, final OnmsLocationSpecificStatus newStatus) {
        return currentStatus == null || !currentStatus.getPollResult().equals(newStatus.getPollResult());
    }

    private Date getConfigurationTimestamp() {
        return m_configurationTimestamp;
    }

    @Transactional(readOnly=true)
    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        return m_locMonDao.findAllMonitoringLocationDefinitions();
    }

    @Transactional(readOnly=true)
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

    @Transactional(readOnly=true)
    public PollerConfiguration getPollerConfiguration(final int locationMonitorId) {
        try {
			final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
			if (mon == null) {
			    // the monitor has been deleted we'll pick this in up on the next config check
			    return new EmptyPollerConfiguration();
			}

			final Package pkg = getPollingPackageForMonitor(mon);
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
			return new SimplePollerConfiguration(getConfigurationTimestamp(), configs.toArray(new PolledService[configs.size()]));
		} catch (final Exception e) {
			LogUtils.warnf(this, e, "An error occurred retrieving the poller configuration for location monitor ID %d", locationMonitorId);
			return new EmptyPollerConfiguration();
		}
    }

    private Package getPollingPackageForMonitor(final OnmsLocationMonitor mon) {
        final OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(mon.getDefinitionName());
        if (def == null) {
            throw new IllegalStateException("Location definition '" + mon.getDefinitionName() + "' could not be found for location monitor ID " + mon.getId());
        }
        String pollingPackageName = def.getPollingPackageName();

        final Package pkg = m_pollerConfig.getPackage(pollingPackageName);
        if (pkg == null) {
            throw new IllegalStateException("Package "+pollingPackageName+" does not exist as defined for monitoring location "+mon.getDefinitionName());
        }
        return pkg;
    }

    @Transactional(readOnly=true)
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(final DistributionContext context) {
    	try {
	        final Collection<ServiceMonitorLocator> locators = m_pollerConfig.getServiceMonitorLocators(context);
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

    public void pollerStopping(final int locationMonitorId) {
        final OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            LogUtils.infof(this, "pollerStopping was called for location monitor ID %d which does not exist", locationMonitorId);
            return;
        }

        mon.setStatus(MonitorStatus.STOPPED);
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

    public int registerLocationMonitor(final String monitoringLocationId) {
        final OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(monitoringLocationId);
        if (def == null) {
            throw new ObjectRetrievalFailureException(OnmsMonitoringLocationDefinition.class, monitoringLocationId, "Location monitor definition with the id '" + monitoringLocationId + "' not found", null);
        }
        final OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setDefinitionName(def.getName());
        mon.setStatus(MonitorStatus.REGISTERED);

        m_locMonDao.save(mon);

        return mon.getId();
    }

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
			    m_pollerConfig.saveResponseTimeData(Integer.toString(locationMonitorId), monSvc, newStatus.getPollResult().getResponseTime(), pkg);
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

    public void setDisconnectedTimeout(final int disconnectedTimeout) {
        m_disconnectedTimeout = disconnectedTimeout;

    }

    public void setEventIpcManager(final EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void setLocationMonitorDao(final LocationMonitorDao locMonDao) {
        m_locMonDao = locMonDao;
    }

    public void setMonitoredServiceDao(final MonitoredServiceDao monSvcDao) {
        m_monSvcDao = monSvcDao;
    }

    public void setPollerConfig(final PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

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
