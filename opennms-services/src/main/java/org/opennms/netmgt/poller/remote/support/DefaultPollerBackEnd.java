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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
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
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultPollerBackEnd implements PollerBackEnd, SpringServiceDaemon {

    private static class SimplePollerConfiguration implements PollerConfiguration, Serializable {

        private static final long serialVersionUID = 1L;

        private Date m_timestamp;
        private PolledService[] m_polledServices;

        SimplePollerConfiguration(Date timestamp, PolledService[] polledSvcs) {
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

        log().debug("Checking for disconnected monitors: disconnectedTimeout = "+m_disconnectedTimeout);

        Date now = m_timeKeeper.getCurrentDate();
        Date earliestAcceptable = new Date(now.getTime() - m_disconnectedTimeout);

        Collection<OnmsLocationMonitor> monitors = m_locMonDao.findAll();
        log().debug("Found "+monitors.size()+" monitors");

        for (OnmsLocationMonitor monitor : monitors) {
            if (monitor.getStatus() == MonitorStatus.STARTED && monitor.getLastCheckInTime().before(earliestAcceptable)) {
                log().debug("Monitor "+monitor.getName()+" has stopped responding");
                monitor.setStatus(MonitorStatus.DISCONNECTED);
                m_locMonDao.update(monitor);

                sendDisconnectedEvent(monitor);

            } else {
                log().debug("Monitor "+monitor.getName()+"("+monitor.getStatus()+") last responded at "+monitor.getLastCheckInTime());
            }
        }
    }

    private MonitorStatus checkForGlobalConfigChange(Date currentConfigurationVersion) {
        if (m_configurationTimestamp.after(currentConfigurationVersion)) {
            return MonitorStatus.CONFIG_CHANGED;
        } else {
            return MonitorStatus.STARTED;
        }
    }

    public void configurationUpdated() {
        m_configurationTimestamp = m_timeKeeper.getCurrentDate();
    }

    private EventBuilder createEventBuilder(OnmsLocationMonitor mon, String uei) {
        EventBuilder eventBuilder = new EventBuilder(uei, "PollerBackEnd")
        .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, mon.getId());
        return eventBuilder;
    }

    private boolean databaseStatusChanged(OnmsLocationSpecificStatus currentStatus, OnmsLocationSpecificStatus newStatus) {
        return currentStatus == null || !currentStatus.getPollResult().equals(newStatus.getPollResult());
    }

    private Date getConfigurationTimestamp() {
        return m_configurationTimestamp;
    }

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        return m_locMonDao.findAllMonitoringLocationDefinitions();
    }

    public String getMonitorName(int locationMonitorId) {
        OnmsLocationMonitor locationMonitor = m_locMonDao.load(locationMonitorId);
        return locationMonitor.getName();
    }

    private Map<String, Object> getParameterMap(Service serviceConfig) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Enumeration<Parameter> serviceParms = serviceConfig.enumerateParameter();
        while(serviceParms.hasMoreElements()) {
            Parameter serviceParm = serviceParms.nextElement();

            String value = serviceParm.getValue();
            if (value == null) {
                value = (serviceParm.getAnyObject() == null ? "" : serviceParm.getAnyObject().toString());
            }

            paramMap.put(serviceParm.getKey(), value);
        }
        return paramMap;
    }

    public PollerConfiguration getPollerConfiguration(int locationMonitorId) {

        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            /* the monitor has been deleted we'll pick this in up on the next
             * config check
             */
            return new EmptyPollerConfiguration();
        }

        Package pkg = getPollingPackageForMonitor(mon);

        ServiceSelector selector = m_pollerConfig.getServiceSelectorForPackage(pkg);

        Collection<OnmsMonitoredService> services = m_monSvcDao.findMatchingServices(selector);
        log().debug("found " + services.size() + " services");

        List<PolledService> configs = new ArrayList<PolledService>(services.size());

        for (OnmsMonitoredService monSvc : services) {
            Service serviceConfig = m_pollerConfig.getServiceInPackage(monSvc.getServiceName(), pkg);
            long interval = serviceConfig.getInterval();
            Map<String, Object> parameters = getParameterMap(serviceConfig);
            configs.add(new PolledService(monSvc, parameters, new OnmsPollModel(interval)));
        }

        PolledService[] polledSvcs = configs.toArray(new PolledService[configs.size()]);
        return new SimplePollerConfiguration(getConfigurationTimestamp(), polledSvcs);


    }

    private Package getPollingPackageForMonitor(OnmsLocationMonitor mon) {
        OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(mon.getDefinitionName());
        if (def == null) {
            throw new IllegalStateException("Location definition '" + mon.getDefinitionName() + "' could not be found for location monitor ID " + mon.getId());
        }
        String pollingPackageName = def.getPollingPackageName();

        Package pkg = m_pollerConfig.getPackage(pollingPackageName);
        if (pkg == null) {
            throw new IllegalStateException("Package "+pollingPackageName+" does not exist as defined for monitoring location "+mon.getDefinitionName());
        }
        return pkg;
    }

    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(DistributionContext context) {
        Collection<ServiceMonitorLocator> locators = m_pollerConfig.getServiceMonitorLocators(context);
        log().debug("getServiceMonitorLocators: Returning " + locators.size() + " locators");
        return locators;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private boolean logicalStatusChanged(OnmsLocationSpecificStatus currentStatus, OnmsLocationSpecificStatus newStatus) {
        return currentStatus != null || (currentStatus == null && !newStatus.getPollResult().isAvailable());
    }


    public MonitorStatus pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion) {
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            log().debug("Deleted monitor checked in with ID " + locationMonitorId);
            return MonitorStatus.DELETED;
        }

        return updateMonitorState(mon, currentConfigurationVersion);

    }

    public boolean pollerStarting(int locationMonitorId, Map<String, String> pollerDetails) {
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
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

    public void pollerStopping(int locationMonitorId) {
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            log().info("pollerStopping was called for location monitor ID " + locationMonitorId + " which does not exist");
            return;
        }

        mon.setStatus(MonitorStatus.STOPPED);
        mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
        m_locMonDao.update(mon);

        sendMonitorStoppedEvent(mon);

    }

    private void processStatusChange(OnmsLocationSpecificStatus currentStatus, OnmsLocationSpecificStatus newStatus) {

        if (databaseStatusChanged(currentStatus, newStatus)) {
            m_locMonDao.saveStatusChange(newStatus);

            PollStatus pollResult = newStatus.getPollResult();

            // if we don't know the current status only send an event if it is not up
            if (logicalStatusChanged(currentStatus, newStatus)) {
                sendRegainedOrLostServiceEvent(newStatus, pollResult);
            }
        }
    }

    public int registerLocationMonitor(String monitoringLocationId) {
        OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(monitoringLocationId);
        if (def == null) {
            throw new ObjectRetrievalFailureException(OnmsMonitoringLocationDefinition.class, monitoringLocationId, "Location monitor definition with the id '" + monitoringLocationId + "' not found", null);
        }
        OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setDefinitionName(def.getName());
        mon.setStatus(MonitorStatus.REGISTERED);

        m_locMonDao.save(mon);

        return mon.getId();
    }

    public void reportResult(int locationMonitorID, int serviceId, PollStatus pollResult) {
        if (pollResult == null) {
            throw new IllegalArgumentException("pollResult argument cannot be null");
        }

        OnmsLocationMonitor locationMonitor = m_locMonDao.get(locationMonitorID);
        if (locationMonitor == null) {
            log().info("reportResult was called for location monitor ID " + locationMonitorID + " which does not exist");
            return;
        }

        OnmsMonitoredService monSvc = m_monSvcDao.get(serviceId);
        if (monSvc == null) {
            log().info("reportResult was called for service " + serviceId + " which does not exist on location monitor ID " + locationMonitorID);
            return;
        }

        OnmsLocationSpecificStatus newStatus = new OnmsLocationSpecificStatus(locationMonitor, monSvc, pollResult);

        if (newStatus.getPollResult().getResponseTime() != null) {
            Package pkg = getPollingPackageForMonitor(locationMonitor);
            m_pollerConfig.saveResponseTimeData(Integer.toString(locationMonitorID), monSvc, newStatus.getPollResult().getResponseTime(), pkg);
        }

        OnmsLocationSpecificStatus currentStatus = m_locMonDao.getMostRecentStatusChange(locationMonitor, monSvc);

        processStatusChange(currentStatus, newStatus);
    }

    private void sendDisconnectedEvent(OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI);
    }

    private void sendEvent(OnmsLocationMonitor mon, String uei) {
        EventBuilder eventBuilder = createEventBuilder(mon, uei);
        m_eventIpcManager.sendNow(eventBuilder.getEvent());
    }

    private void sendMonitorStartedEvent(OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_STARTED_UEI);
    }

    private void sendMonitorStoppedEvent(OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_STOPPED_UEI);
    }

    private void sendReconnectedEvent(OnmsLocationMonitor mon) {
        sendEvent(mon, EventConstants.LOCATION_MONITOR_RECONNECTED_UEI);
    }

    private void sendRegainedOrLostServiceEvent(OnmsLocationSpecificStatus newStatus, PollStatus pollResult) {
        String uei = pollResult.isAvailable()
        ? EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI
            : EventConstants.REMOTE_NODE_LOST_SERVICE_UEI;

        EventBuilder builder = createEventBuilder(newStatus.getLocationMonitor(), uei)
        .setMonitoredService(newStatus.getMonitoredService());

        if (!pollResult.isAvailable() && pollResult.getReason() != null) {
            builder.addParam(EventConstants.PARM_LOSTSERVICE_REASON, pollResult.getReason());
        }

        m_eventIpcManager.sendNow(builder.getEvent());
    }

    public void setDisconnectedTimeout(int disconnectedTimeout) {
        m_disconnectedTimeout = disconnectedTimeout;

    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void setLocationMonitorDao(LocationMonitorDao locMonDao) {
        m_locMonDao = locMonDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monSvcDao) {
        m_monSvcDao = monSvcDao;
    }

    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }

    private MonitorStatus updateMonitorState(OnmsLocationMonitor mon, Date currentConfigurationVersion) {
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
                log().error("Unexpected monitor state for monitor: "+mon);
            throw new IllegalStateException("Unexpected monitor state for monitor: "+mon);

            }

        } finally {
            mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
            m_locMonDao.update(mon);
        }
    }
}
