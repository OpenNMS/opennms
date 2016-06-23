/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

/**
 * ComputeStatusDetails
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class AppStatusDetailsComputer {
    
    final Date m_startTime;
    final Date m_endTime;
    final Collection<GWTLocationMonitor> m_monitors; 
    final Collection<GWTMonitoredService> m_services;
    final Collection<GWTLocationSpecificStatus> m_statuses; 
    
    /**
     * <p>Constructor for AppStatusDetailsComputer.</p>
     *
     * @param startTime a {@link java.util.Date} object.
     * @param endTime a {@link java.util.Date} object.
     * @param monitors a {@link java.util.Collection} object.
     * @param services a {@link java.util.Collection} object.
     * @param statuses a {@link java.util.Collection} object.
     */
    public AppStatusDetailsComputer(Date startTime, Date endTime, Collection<GWTLocationMonitor> monitors, Collection<GWTMonitoredService> services, Collection<GWTLocationSpecificStatus> statuses) {
        m_monitors = monitors;
        m_services = services;
        m_statuses = statuses;
        m_startTime = startTime;
        m_endTime = endTime;
    }

    /**
     * <p>compute</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public StatusDetails compute() {
        if (m_statuses == null || m_statuses.size() == 0) {
            return StatusDetails.unknown("No locations have reported status updates.");
        }
    
        if (m_monitors == null || m_monitors.size() == 0) {
            return StatusDetails.unknown("No location monitors are currently reporting.");
        }
    
        final Set<String> monitorIds = new HashSet<String>();
        final Set<GWTMonitoredService> servicesWithOutages = new HashSet<GWTMonitoredService>();
        final Set<GWTMonitoredService> servicesDown = new HashSet<GWTMonitoredService>();
    
        boolean foundActiveMonitor = false;
        for (final GWTLocationMonitor monitor : m_monitors) {
            if (monitor.getStatus().equals("STARTED")) {
                foundActiveMonitor = true;
                monitorIds.add(monitor.getId());
            }
        }
        if (! foundActiveMonitor) {
            return StatusDetails.unknown("No location monitors are currently reporting.");
        }
    
        Map<Integer, Map<String, List<GWTServiceOutage>>> outages = getOutages();
        for (final Entry<Integer, Map<String, List<GWTServiceOutage>>> entry : outages.entrySet()) {
            final Integer serviceId = entry.getKey();
            final List<GWTServiceOutage> locationOutages = new ArrayList<GWTServiceOutage>();
            for (final Entry<String, List<GWTServiceOutage>> locationOutageEntry : entry.getValue().entrySet()) {
                final String monitorId = locationOutageEntry.getKey();
                for (final GWTServiceOutage outage : locationOutageEntry.getValue()) {
                    locationOutages.add(outage);
                }
                locationOutages.addAll(outages.get(serviceId).get(monitorId));
            }
    
            GWTMonitoredService service = null;
            if (locationOutages.size() > 0) {
                 service = locationOutages.iterator().next().getService();
            } else {
                return StatusDetails.unknown("No locations reporting for service ID " + serviceId);
            }
    
            final Set<GWTLocationMonitor> monitorsFailing = new HashSet<GWTLocationMonitor>();
            final Set<GWTLocationMonitor> monitorsPassing = new HashSet<GWTLocationMonitor>(m_monitors);
    
            Collections.sort(locationOutages);
            for (final GWTServiceOutage outage : locationOutages) {
                final GWTLocationMonitor monitor = outage.getMonitor();
                if (outage.getTo().compareTo(m_endTime) >= 0) {
                    monitorsFailing.add(monitor);
                    monitorsPassing.remove(monitor);
                } else {
                    monitorsPassing.add(monitor);
                    monitorsFailing.remove(monitor);
                }
            }
    
            if (monitorsFailing.size() > 0) {
                if (monitorsPassing.size() == 0) {
                    servicesDown.add(service);
                } else {
                    servicesWithOutages.add(service);
                }
            }
        }
        outages = null;
    
        Set<String> allServiceNames = new HashSet<String>();
        Map<String,Integer> unmonitoredServiceCounts = new HashMap<String,Integer>();
        for (final GWTMonitoredService service : m_services) {
            final String serviceName = service.getServiceName();
            allServiceNames.add(serviceName);
            if (!unmonitoredServiceCounts.containsKey(serviceName)) {
                unmonitoredServiceCounts.put(serviceName, 0);
            }
            unmonitoredServiceCounts.put(serviceName, unmonitoredServiceCounts.get(serviceName) + 1);
        }
        Set<String> unmonitoredServiceNames = new HashSet<String>(allServiceNames);
        for (final GWTMonitoredService service : getAllServices()) {
            final String serviceName = service.getServiceName();
            unmonitoredServiceNames.remove(serviceName);
            if (unmonitoredServiceCounts.containsKey(serviceName)) {
                final int count = unmonitoredServiceCounts.get(serviceName) - 1;
                unmonitoredServiceCounts.put(serviceName, count);
                if (count == 0) {
                    unmonitoredServiceCounts.remove(serviceName);
                }
            }
        }
    
        if (unmonitoredServiceCounts.size() > 0) {
            final Set<String> names = new TreeSet<String>();
            for (final Entry<String,Integer> entry : unmonitoredServiceCounts.entrySet()) {
                final String key = entry.getKey();
                final Integer count = entry.getValue();
                names.add((count > 1)? key + " (" + count + ")" : key);
            }
            return StatusDetails.unknown("The following services were not being reported on by any monitor: " + StringUtils.join(names, ", "));
        }
    
        if (servicesDown.size() > 0) {
            final Set<String> names = new TreeSet<String>();
            for (final GWTMonitoredService service : servicesDown) {
                names.add(service.getServiceName());
            }
            return StatusDetails.down("The following services were reported as down by all monitors: " + StringUtils.join(names, ","));
        }
    
        if (servicesWithOutages.size() == m_services.size()) {
            final Set<String> names = new TreeSet<String>();
            for (final GWTMonitoredService service : servicesWithOutages) {
                names.add(service.getServiceName());
            }
            return StatusDetails.marginal("The following services were reported to have outages in this application: " + StringUtils.join(names, ", "));
        }
    
        return StatusDetails.up();
    }

    private Collection<GWTMonitoredService> getAllServices() {
        final Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
        if (getLocationSpecificStatuses() != null) {
            for (final GWTLocationSpecificStatus status : getLocationSpecificStatuses()) {
                services.add(status.getMonitoredService());
            }
        }
        return services;
    }

    private Collection<GWTLocationSpecificStatus> getLocationSpecificStatuses() {
        return m_statuses;
    }

    private Map<Integer, Map<String, List<GWTServiceOutage>>> getOutages() {
        // service id -> location id -> outages
        final Map<Integer, Map<String, List<GWTServiceOutage>>> outages = new HashMap<Integer, Map<String, List<GWTServiceOutage>>>();
        if (m_statuses == null) {
            return outages;
        }
        
        for (final GWTLocationSpecificStatus status : m_statuses) {
            final Integer serviceId = status.getMonitoredService().getId();
            final String monitorId = status.getLocationMonitor().getId();
            GWTServiceOutage lastOutage = null;
            Map<String, List<GWTServiceOutage>> serviceOutages = outages.get(serviceId);
            if (serviceOutages != null) {
                List<GWTServiceOutage> monitorOutages = serviceOutages.get(monitorId);
                if (monitorOutages != null && monitorOutages.size() > 0) {
                    lastOutage = monitorOutages.get(monitorOutages.size() - 1);
                }
            }
        
            if (lastOutage != null && lastOutage.getTo() == null) {
                // there's an existing outage, and it's unfinished
        
                if (!status.getPollResult().isDown()) {
                    // it's back up
                    lastOutage.setTo(status.getPollResult().getTimestamp());
                    continue;
                }
                // otherwise, it's still down... leave the "to" incomplete
        
            } else {
                // there's no existing outage
        
                if (status.getPollResult().isDown()) {
                    // but the service is down on this monitor, start a new outage
                    lastOutage = new GWTServiceOutage();
                    lastOutage.setService(status.getMonitoredService());
                    lastOutage.setMonitor(status.getLocationMonitor());
                    lastOutage.setFrom(status.getPollResult().getTimestamp());
        
                    if (serviceOutages == null) {
                        serviceOutages = new HashMap<String, List<GWTServiceOutage>>();
                        outages.put(serviceId, serviceOutages);
                    }
                    List<GWTServiceOutage> monitorOutages = serviceOutages.get(monitorId);
                    if (monitorOutages == null) {
                        monitorOutages = new ArrayList<GWTServiceOutage>();
                        serviceOutages.put(monitorId, monitorOutages);
                    }
                    serviceOutages.get(monitorId).add(lastOutage);
                }
            }
        }
        
        for (final Entry<Integer, Map<String, List<GWTServiceOutage>>> entry : outages.entrySet()) {
            for (final Entry<String,List<GWTServiceOutage>> serviceOutageEntry : entry.getValue().entrySet()) {
                for (final GWTServiceOutage outage : serviceOutageEntry.getValue()) {
                    if (outage.getFrom() == null) {
                        outage.setFrom(getStartTime());
                    }
                    if (outage.getTo() == null) {
                        outage.setTo(getEndTime());
                    }
                }
            }
        }
        
        return outages;
    }

    private Date getStartTime() {
        return m_startTime;
    }

    private Date getEndTime() {
        return m_endTime;
    }


}
