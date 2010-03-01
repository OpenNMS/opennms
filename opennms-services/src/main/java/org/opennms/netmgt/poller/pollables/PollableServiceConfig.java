/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 3, 2005
 *
 * Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller.pollables;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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
    private Service findService(Package pkg) {
        for (Service s : m_pkg.getServiceCollection()) {
            if (s.getName().equalsIgnoreCase(m_service.getSvcName())) {
                return s;
            }
        }

        throw new RuntimeException("Service name not part of package!");
        
    }

    public PollStatus poll() {
        try {
            ServiceMonitor monitor = getServiceMonitor();
            ThreadCategory.getInstance(getClass()).debug("Polling "+m_service+" using pkg "+m_pkg.getName());
            PollStatus result = monitor.poll(m_service, getParameters());
            ThreadCategory.getInstance(getClass()).debug("Finish polling "+m_service+" using pkg "+m_pkg.getName()+" result ="+result);
            return result;
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).error("Unexpected exception while polling "+m_service+". Marking service as DOWN", e);
            return PollStatus.down("Unexpected exception while polling "+m_service+". "+e);
        } catch (Error e) {
            ThreadCategory.getInstance(getClass()).error("Unexpected error while polling "+m_service+". Marking service as DOWN", e);
            return PollStatus.down("Unexpected error while polling "+m_service+". "+e);
        }
    }

	private ServiceMonitor getServiceMonitor() {
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
        Map<String,Object> m = Collections.synchronizedMap(new TreeMap<String,Object>());
        for (Parameter p : svc.getParameterCollection()) {
            String val = p.getValue();
            if (val == null) {
            	val = (p.getAnyObject() == null ? "" : p.getAnyObject().toString());
            }

           m.put(p.getKey(), val);
        }
        return m;
    }
    

    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    public long getInterval() {
        
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
    


    public boolean scheduledSuspension() {
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
