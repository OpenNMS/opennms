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

package org.opennms.netmgt.poller.pollables;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;

/**
 * Represents a PollableServiceConfig
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollableServiceConfig implements PollConfig, ScheduleInterval {

    private PollerConfig m_pollerConfig;
    private PollOutagesConfig m_pollOutagesConfig;
    private PollableService m_service;
    private Map<String,Object> m_parameters = null;
    private Package m_pkg;
    private Timer m_timer;
    private Service m_configService;
	private ServiceMonitor m_serviceMonitor;

    /**
     * <p>Constructor for PollableServiceConfig.</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     * @param pollOutagesConfig a {@link org.opennms.netmgt.config.PollOutagesConfig} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @param timer a {@link org.opennms.netmgt.scheduler.Timer} object.
     */
    public PollableServiceConfig(PollableService svc, PollerConfig pollerConfig, PollOutagesConfig pollOutagesConfig, Package pkg, Timer timer) {
        m_service = svc;
        m_pollerConfig = pollerConfig;
        m_pollOutagesConfig = pollOutagesConfig;
        m_pkg = pkg;
        m_timer = timer;
        m_configService = findService(pkg);
        
        ServiceMonitor monitor = getServiceMonitor();
        monitor.initialize(m_service);
    }

    /**
     * @param pkg
     * @return
     */
    private synchronized Service findService(Package pkg) {
        for (Service s : m_pkg.getServiceCollection()) {
            if (s.getName().equalsIgnoreCase(m_service.getSvcName())) {
                return s;
            }
        }

        throw new RuntimeException("Service name not part of package!");
        
    }

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    @Override
    public PollStatus poll() {
        String packageName = null;
        synchronized(this) {
            packageName = m_pkg.getName();
        }
        try {
            ServiceMonitor monitor = getServiceMonitor();
            ThreadCategory.getInstance(getClass()).debug("Polling "+m_service+" using pkg " + packageName);
            PollStatus result = monitor.poll(m_service, getParameters());
            ThreadCategory.getInstance(getClass()).debug("Finish polling "+m_service+" using pkg " + packageName + " result = "+result);
            return result;
        } catch (Throwable e) {
            ThreadCategory.getInstance(getClass()).error("Unexpected exception while polling "+m_service+". Marking service as DOWN", e);
            return PollStatus.down("Unexpected exception while polling "+m_service+". "+e);
        }
    }

	private synchronized ServiceMonitor getServiceMonitor() {
		if (m_serviceMonitor == null) {
			ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(m_service.getSvcName());
			m_serviceMonitor = new LatencyStoringServiceMonitorAdaptor(monitor, m_pollerConfig, m_pkg);
			
		}
		return m_serviceMonitor;
	}
    
    /**
     * Uses the existing package name to try and re-obtain the package from the poller config factory.
     * Should be called when the poller config has been reloaded.
     */
    @Override
    public synchronized void refresh() {
        Package newPkg = m_pollerConfig.getPackage(m_pkg.getName());
        if (newPkg == null) {
            ThreadCategory.getInstance(PollableServiceConfig.class).warn("Package named "+m_pkg.getName()+" no longer exists.");
        }
        m_pkg = newPkg;
        m_configService = findService(m_pkg);
        m_parameters = null;
        
    }
    
    /**
     * Should be called when thresholds configuration has been reloaded
     */
    @Override
    public synchronized void refreshThresholds() {
        ((LatencyStoringServiceMonitorAdaptor)getServiceMonitor()).refreshThresholds();
    }


    /**
     * @return
     */
    private synchronized Map<String,Object> getParameters() {
        if (m_parameters == null) {
            
            m_parameters = createPropertyMap(m_configService);
        }
        return m_parameters;
    }

    private Map<String,Object> createPropertyMap(Service svc) {
        Map<String,Object> m = new ConcurrentSkipListMap<String,Object>();
        for (Parameter p : svc.getParameterCollection()) {
            String val = p.getValue();
            if (val == null) {
            	val = (p.getAnyObject() == null ? "" : p.getAnyObject().toString());
            }

           m.put(p.getKey(), val);
        }
        return m;
    }
    

    /**
     * <p>getCurrentTime</p>
     *
     * @return a long.
     */
    @Override
    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    /**
     * <p>getInterval</p>
     *
     * @return a long.
     */
    @Override
    public synchronized long getInterval() {
        
        if (m_service.isDeleted())
            return -1;
        
        long when = m_configService.getInterval();

        if (m_service.getStatus().isDown()) {
            long downSince = m_timer.getCurrentTime() - m_service.getStatusChangeTime();
            boolean matched = false;
            for (Downtime dt : m_pkg.getDowntimeCollection()) {
                if (dt.getBegin() <= downSince) {
                    if (dt.getDelete() != null && (dt.getDelete().equals("yes") || dt.getDelete().equals("true"))) {
                        when = -1;
                        matched = true;
                    }
                    else if (dt.hasEnd() && dt.getEnd() > downSince) {
                        // in this interval
                        //
                        when = dt.getInterval();
                        matched = true;
                    } else // no end
                    {
                        when = dt.getInterval();
                        matched = true;
                    }
                }
            }
            if (!matched) {
                ThreadCategory.getInstance(getClass()).warn("getInterval: Could not locate downtime model, throwing runtime exception");
                throw new RuntimeException("Downtime model is invalid, cannot schedule service " + m_service);
            }
        }
        
        if (when < 0) {
            m_service.sendDeleteEvent();
        }
        
        return when;
    }
    


    /**
     * <p>scheduledSuspension</p>
     *
     * @return a boolean.
     */
    @Override
    public synchronized boolean scheduledSuspension() {
        long nodeId=m_service.getNodeId();
        for (String outageName : m_pkg.getOutageCalendarCollection()) {
            // Does the outage apply to the current time?
            if (m_pollOutagesConfig.isTimeInOutage(m_timer.getCurrentTime(), outageName)) {
                // Does the outage apply to this interface?
    
                if (m_pollOutagesConfig.isNodeIdInOutage(nodeId, outageName) || 
                    (m_pollOutagesConfig.isInterfaceInOutage(m_service.getIpAddr(), outageName)) || 
                        (m_pollOutagesConfig.isInterfaceInOutage("match-any", outageName))) {
                    if (ThreadCategory.getInstance(getClass()).isDebugEnabled())
                        ThreadCategory.getInstance(getClass()).debug("scheduledOutage: configured outage '" + outageName + "' applies, " + m_configService + " will not be polled.");
                    return true;
                }
            }
        }
    
        //return outageFound;
        return false;
    }

}
