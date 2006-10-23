//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Apr 27: Added support for pathOutageEnabled
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

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
import org.opennms.netmgt.poller.remote.OnmsPollModel;
import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.opennms.netmgt.utils.EventBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class DefaultPollerBackEnd implements PollerBackEnd, InitializingBean {

    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private EventIpcManager m_eventIpcManager;
    private PollerConfig m_pollerConfig;
    private TimeKeeper m_timeKeeper;
    private int m_unresponsiveTimeout;
    private Date m_configurationTimestamp = null;

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        return m_locMonDao.findAllMonitoringLocationDefinitions();
    }

    public PollerConfiguration getPollerConfiguration(int locationMonitorId) {
        
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        Package pkg = getPollingPackageForMonitor(mon);
        
        ServiceSelector selector = m_pollerConfig.getServiceSelectorForPackage(pkg);

        Collection<OnmsMonitoredService> services = m_monSvcDao.findMatchingServices(selector);
        
        List<PolledService> configs = new ArrayList<PolledService>(services.size());
        
        for (OnmsMonitoredService monSvc : services) {
            Service serviceConfig = m_pollerConfig.getServiceInPackage(monSvc.getServiceName(), pkg);
            long interval = serviceConfig.getInterval();
            Map parameters = getParameterMap(serviceConfig);
            configs.add(new PolledService(monSvc, parameters, new OnmsPollModel(interval)));
        }
        
        PolledService[] polledSvcs = (PolledService[]) configs.toArray(new PolledService[configs.size()]);
        return new SimplePollerConfiguration(getConfigurationTimestamp(), polledSvcs);
        
        
    }

    private Package getPollingPackageForMonitor(OnmsLocationMonitor mon) {
        OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(mon.getDefinitionName());
        String pollingPackageName = def.getPollingPackageName();
        
        Package pkg = m_pollerConfig.getPackage(pollingPackageName);
        if (pkg == null) {
            throw new IllegalStateException("Package "+pollingPackageName+" does not exist as defined for monitoring location "+mon.getDefinitionName());
        }
        return pkg;
    }
    
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
    
    @SuppressWarnings("unchecked")
    private Map getParameterMap(Service serviceConfig) {
        Map<String, String> paramMap = new HashMap<String, String>();
        Enumeration<Parameter> serviceParms = serviceConfig.enumerateParameter();
        while(serviceParms.hasMoreElements()) {
            Parameter serviceParm = serviceParms.nextElement();
            paramMap.put(serviceParm.getKey(), serviceParm.getValue());
        }
        return paramMap;
    }

    public boolean pollerCheckingIn(int locationMonitorId, Date currentConfigurationVersion) {
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            return false;
        }
        MonitorStatus oldStatus = mon.getStatus();
        mon.setStatus(MonitorStatus.STARTED);
        mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
        m_locMonDao.update(mon);
        
        if (MonitorStatus.UNRESPONSIVE.equals(oldStatus)) {
            // the monitor has reconnected!
            EventBuilder eventBuilder = createEventBuilder(locationMonitorId, EventConstants.LOCATION_MONITOR_RECONNECTED_UEI);
            m_eventIpcManager.sendNow(eventBuilder.getEvent());

        }
        
        return m_configurationTimestamp.after(currentConfigurationVersion);
    }

    public boolean pollerStarting(int locationMonitorId) {
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        if (mon == null) {
            return false;
        }
        mon.setStatus(MonitorStatus.STARTED);
        mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
        m_locMonDao.update(mon);
        
        EventBuilder eventBuilder = createEventBuilder(locationMonitorId, EventConstants.LOCATION_MONITOR_STARTED_UEI);
        m_eventIpcManager.sendNow(eventBuilder.getEvent());
        
        return true;
    }

    private EventBuilder createEventBuilder(int locationMonitorId, String uei) {
        EventBuilder eventBuilder = new EventBuilder(uei, "PollerBackEnd")
            .addParam(EventConstants.PARM_LOCATION_MONITOR_ID, locationMonitorId);
        return eventBuilder;
    }

    public void pollerStopping(int locationMonitorId) {
        System.err.println("Looking up monitor "+locationMonitorId);
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        System.err.println("Found monitor "+mon);
        mon.setStatus(MonitorStatus.STOPPED);
        mon.setLastCheckInTime(m_timeKeeper.getCurrentDate());
        m_locMonDao.update(mon);
        
        EventBuilder eventBuilder = createEventBuilder(locationMonitorId, EventConstants.LOCATION_MONITOR_STOPPED_UEI);
        m_eventIpcManager.sendNow(eventBuilder.getEvent());

    }

    public int registerLocationMonitor(String monitoringLocationId) {
        
        OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(monitoringLocationId);
        OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setDefinitionName(def.getName());
        mon.setStatus(MonitorStatus.REGISTERED);
        
        m_locMonDao.save(mon);
        
        return mon.getId();
       
        
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_locMonDao, "The LocationMonitorDao must be set");
        Assert.notNull(m_monSvcDao, "The MonitoredServiceDao must be set");
        Assert.notNull(m_pollerConfig, "The PollerConfig must be set");
        Assert.notNull(m_timeKeeper, "The timeKeeper must be set");
        Assert.notNull(m_eventIpcManager, "The eventIpcManager must be set");
        Assert.state(m_unresponsiveTimeout > 0, "the unresponsiveTimeout property must be set");
        
        m_configurationTimestamp = m_timeKeeper.getCurrentDate();
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
    
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void reportResult(int locationMonitorID, int serviceId, PollStatus pollResult) {
        
        OnmsLocationMonitor locationMonitor = m_locMonDao.get(locationMonitorID);
        OnmsMonitoredService monSvc = m_monSvcDao.get(serviceId);
        OnmsLocationSpecificStatus newStatus = new OnmsLocationSpecificStatus(locationMonitor, monSvc, pollResult);
        
        if (newStatus.getPollResult().getResponseTime() >= 0) {
            Package pkg = getPollingPackageForMonitor(locationMonitor);
            m_pollerConfig.saveResponseTimeData(Integer.toString(locationMonitorID), monSvc, newStatus.getPollResult().getResponseTime(), pkg);
        }
        
        OnmsLocationSpecificStatus currentStatus = m_locMonDao.getMostRecentStatusChange(locationMonitor, monSvc);
        
        processStatusChange(currentStatus, newStatus);
    }

    private void processStatusChange(OnmsLocationSpecificStatus currentStatus, OnmsLocationSpecificStatus newStatus) {
        
        if (databaseStatusChanged(currentStatus, newStatus)) {
            m_locMonDao.saveStatusChange(newStatus);
            
            // if we don't know the current status only send an event if it is not up
            if (logicalStatusChanged(currentStatus, newStatus)) {
                String uei = newStatus.getPollResult().isAvailable()
                             ? EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI
                             : EventConstants.REMOTE_NODE_LOST_SERVICE_UEI;
                
                EventBuilder builder = createEventBuilder(newStatus.getLocationMonitor().getId(), uei)
                    .setMonitoredService(newStatus.getMonitoredService());

                m_eventIpcManager.sendNow(builder.getEvent());
            }
        }
    }

    private boolean logicalStatusChanged(OnmsLocationSpecificStatus currentStatus, OnmsLocationSpecificStatus newStatus) {
        return currentStatus != null || (currentStatus == null && !newStatus.getPollResult().isAvailable());
    }

    private boolean databaseStatusChanged(OnmsLocationSpecificStatus currentStatus, OnmsLocationSpecificStatus newStatus) {
        return currentStatus == null || !currentStatus.getPollResult().equals(newStatus.getPollResult());
    }

    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }

    public void checkforUnresponsiveMonitors() {
        
        log().debug("Checking for Unresponsive monitors: UnresponsiveTimeout = "+m_unresponsiveTimeout);
        
        Date now = m_timeKeeper.getCurrentDate();
        Date earliestAcceptable = new Date(now.getTime() - m_unresponsiveTimeout);
        
        Collection<OnmsLocationMonitor> monitors = m_locMonDao.findAll();
        log().debug("Found "+monitors.size()+" monitors");
        
        for (OnmsLocationMonitor monitor : monitors) {
            if (monitor.getStatus() == MonitorStatus.STARTED && monitor.getLastCheckInTime().before(earliestAcceptable)) {
                log().debug("Monitor "+monitor.getName()+" has stopped responding");
                monitor.setStatus(MonitorStatus.UNRESPONSIVE);
                m_locMonDao.update(monitor);
                
                EventBuilder eventBuilder = createEventBuilder(monitor.getId(), EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI);
                m_eventIpcManager.sendNow(eventBuilder.getEvent());
                
            } else {
                log().debug("Monitor "+monitor.getName()+"("+monitor.getStatus()+") last responded at "+monitor.getLastCheckInTime());
            }
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void setUnresponsiveTimeout(int unresponsiveTimeout) {
        m_unresponsiveTimeout = unresponsiveTimeout;
        
    }
    
    private Date getConfigurationTimestamp() {
        return m_configurationTimestamp;
    }

    public void configurationUpdated() {
        m_configurationTimestamp = m_timeKeeper.getCurrentDate();
    }

}
