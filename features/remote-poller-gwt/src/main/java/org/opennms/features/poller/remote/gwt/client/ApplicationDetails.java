/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.Interval;
import org.opennms.features.poller.remote.gwt.client.utils.IntervalUtils;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>ApplicationDetails class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationDetails implements Serializable, IsSerializable {

	private static final long serialVersionUID = -3213261172620899769L;

	private String m_name;

    private ApplicationInfo m_application;

    private Map<Integer,GWTLocationMonitor> m_monitors = new HashMap<Integer,GWTLocationMonitor>();

    private Map<Integer,GWTMonitoredService> m_services = new HashMap<Integer,GWTMonitoredService>();

    private List<GWTLocationSpecificStatus> m_locationSpecificStatuses;

    private Date m_startTime;

    private Date m_endTime;

    private StatusDetails m_statusDetails;

    private Map<Integer, Map<Integer, List<GWTServiceOutage>>> m_outages;

    /**
     * <p>Constructor for ApplicationDetails.</p>
     */
    public ApplicationDetails() {
        m_name = null;
        setLocationSpecificStatuses(null);
        setStartTime(null);
        setEndTime(null);
    }

    /**
     * <p>Constructor for ApplicationDetails.</p>
     *
     * @param application a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     * @param from a {@link java.util.Date} object.
     * @param to a {@link java.util.Date} object.
     * @param monitors a {@link java.util.Collection} object.
     * @param statuses a {@link java.util.List} object.
     */
    public ApplicationDetails(final ApplicationInfo application, final Date from, final Date to, final Collection<GWTLocationMonitor> monitors, final List<GWTLocationSpecificStatus> statuses) {
        m_name = application.getName();
        setApplicationInfo(application);
        setStartTime(from);
        setEndTime(to);
        if (monitors != null) {
            for (final GWTLocationMonitor monitor : monitors) {
                getMonitors().put(monitor.getId(), monitor);
            }
        }
        setLocationSpecificStatuses(statuses);
        if (getLocationSpecificStatuses() != null) {
            Collections.sort(getLocationSpecificStatuses(), new LocationSpecificStatusComparator());
            for (final GWTLocationSpecificStatus status : getLocationSpecificStatuses()) {
                final GWTMonitoredService monitoredService = status.getMonitoredService();
                m_services.put(monitoredService.getId(), monitoredService);
            }
        }
    }

    private Map<Integer, Map<Integer, List<GWTServiceOutage>>> getOutages() {
        if (m_outages == null) {
            m_outages = getOutagesUncached();
        }
        return m_outages;
    }

    private Map<Integer, Map<Integer, List<GWTServiceOutage>>> getOutagesUncached() {
        // service id -> location id -> outages
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = new HashMap<Integer, Map<Integer, List<GWTServiceOutage>>>();
        if (getLocationSpecificStatuses() == null) {
            return outages;
        }

        for (final GWTLocationSpecificStatus status : getLocationSpecificStatuses()) {
            final Integer serviceId = status.getMonitoredService().getId();
            final Integer monitorId = status.getLocationMonitor().getId();
            GWTServiceOutage lastOutage = null;
            Map<Integer, List<GWTServiceOutage>> serviceOutages = outages.get(serviceId);
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
                        serviceOutages = new HashMap<Integer, List<GWTServiceOutage>>();
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

        for (final Integer serviceId : outages.keySet()) {
            for (final Integer monitorId : outages.get(serviceId).keySet()) {
                for (GWTServiceOutage outage : outages.get(serviceId).get(monitorId)) {
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

    /**
     * <p>getStatusDetails</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public StatusDetails getStatusDetails() {
        if (m_statusDetails == null) {
            m_statusDetails = getStatusDetailsUncached();
        }
        return m_statusDetails;
    }

    private StatusDetails getStatusDetailsUncached() {
        return new AppStatusDetailsComputer(getStartTime(), getEndTime(), getMonitors().values(), getApplicationInfo().getServices(), getLocationSpecificStatuses()).compute();
    }

    /**
     * <p>getAvailability</p>
     *
     * @param service a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
     * @return a {@link java.lang.Double} object.
     */
    public Double getAvailability(final GWTMonitoredService service) {
        final Set<Interval> serviceOutages = getServiceOutageIntervals(service.getId());
        return computeAvailabilityForOutageIntervals(serviceOutages);
    }

    /**
     * <p>getAvailability</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getAvailability() {
        if (getStartTime() == null || getLocationSpecificStatuses() == null) {
            return null;
        }

        // service id -> location id -> outages
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();

        Set<Interval> serviceOutageIntervals = IntervalUtils.getIntervalSet();

        for (final Integer serviceId : outages.keySet()) {
            serviceOutageIntervals.addAll(getServiceOutageIntervals(serviceId));
        }

        return computeAvailabilityForOutageIntervals(IntervalUtils.normalize(serviceOutageIntervals));
    }

    private Double computeAvailabilityForOutageIntervals(final Set<Interval> intervals) {
        Long timeAvailable = 0L;
        final Set<Interval> upIntervals = IntervalUtils.invert(getStartTime(), getEndTime(), intervals);
        for (final Interval i : upIntervals) {
            timeAvailable += (i.getEndMillis() - i.getStartMillis());
        }

        final Long totalTime = getEndTime().getTime() - getStartTime().getTime();
        final double availability = timeAvailable.doubleValue() / totalTime.doubleValue() * 100;
        return availability;
    }

    private Set<Interval> getServiceOutageIntervals(final Integer serviceId) {
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();
        final Set<Interval> serviceUpIntervals = IntervalUtils.getIntervalSet();
        final Map<Integer, List<GWTServiceOutage>> serviceOutages = outages.get(serviceId);
        if (serviceOutages != null && serviceOutages.size() != 0) {
            for (final GWTLocationMonitor monitor : getMonitors().values()) {
                final Integer locationId = monitor.getId();
                Set<Interval> locationIntervals = IntervalUtils.getIntervalSet();
                if (serviceOutages.containsKey(locationId)) {
                    for (final GWTServiceOutage outage : serviceOutages.get(locationId)) {
                        locationIntervals.add(new Interval(outage.getFrom().getTime(), outage.getTo().getTime()));
                    }
                }
                locationIntervals = IntervalUtils.invert(getStartTime(), getEndTime(), IntervalUtils.normalize(locationIntervals));
                serviceUpIntervals.addAll(locationIntervals);
            }
            return IntervalUtils.invert(getStartTime(), getEndTime(), IntervalUtils.normalize(serviceUpIntervals));
        }
        return IntervalUtils.getIntervalSet();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "ApplicationDetails[name=" + m_name + ",range=" + getStartTime() + "-" + getEndTime() + ",statuses=" + getLocationSpecificStatuses() + "]";
    }

    /**
     * <p>getApplicationName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplicationName() {
        return m_name;
    }

    /**
     * <p>getDetailsAsString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDetailsAsString() {
        // service id -> location id -> outages
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();

        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"applicationDetails\">\n");
        sb.append("<dl class=\"statusContents\">\n");

        Set<GWTMonitoredService> services = new TreeSet<GWTMonitoredService>(new Comparator<GWTMonitoredService>() {
            @Override
            public int compare(final GWTMonitoredService a, final GWTMonitoredService b) {
                if (a == null) {
                    if (b == null) return 0;
                    return 1;
                }
                if (b == null) return -1;
                return new CompareToBuilder()
                    .append(a.getServiceName(), b.getServiceName())
                    .append(a.getId(), b.getId())
                    .toComparison();
            }
        });
        services.addAll(m_services.values());
        for (final GWTMonitoredService service : services) {
            final Integer serviceId = service.getId();
            final double serviceAvailability = getAvailability(service);

            String styleName = Status.UNKNOWN.getStyle();
            List<GWTLocationMonitor> locationsNotReporting = new ArrayList<GWTLocationMonitor>();

            if (serviceAvailability == 100.0) {
                styleName = Status.UP.getStyle();
                Map<Integer,List<GWTServiceOutage>> serviceOutages = outages.get(serviceId);
                if (serviceOutages != null) {
                    for (final Integer locationId : serviceOutages.keySet()) {
                        final List<GWTServiceOutage> locationOutages = serviceOutages.get(locationId);
                        if (locationOutages != null) {
                            for (final GWTServiceOutage outage : locationOutages) {
                                if (outage.getTo().equals(getEndTime()) || outage.getTo().after(getEndTime())) {
                                    locationsNotReporting.add(getMonitors().get(locationId));
                                    styleName = Status.MARGINAL.getStyle();
                                    continue;
                                }
                            }
                        }
                    }
                }
            } else {
                List<Interval> serviceOutageIntervals = new ArrayList<Interval>(getServiceOutageIntervals(serviceId));
                final int size = serviceOutageIntervals.size();
                if (size > 0) {
                    if (serviceOutageIntervals.get(size - 1).getEndMillis() == getEndTime().getTime()) {
                        styleName = Status.DOWN.getStyle();
                    } else {
                        styleName = Status.MARGINAL.getStyle();
                    }
                }
                styleName = Status.DOWN.getStyle();
            }

            final List<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>(getMonitors().values());
            Collections.sort(monitors);

            sb.append("<dt class=\"").append(styleName).append(" statusDt\">").append(getSummary(service)).append("</dt>\n");
            sb.append("<dd class=\"").append(styleName).append(" statusDd\">");
            sb.append("Availability: ").append(Double.valueOf(serviceAvailability).intValue()).append("%");
            if (locationsNotReporting.size() > 0) {
                final List<String> locationString = new ArrayList<String>();
                for (final GWTLocationMonitor monitor : locationsNotReporting) {
                    locationString.add(monitor.getName());
                }
                sb.append("<br>\n").append("Location");
                if (locationsNotReporting.size() > 1) sb.append("s");
                sb.append(" with outages: ").append(StringUtils.join(locationString));
            }
            sb.append("</dd>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    private String getSummary(final GWTMonitoredService service) {
        final StringBuilder sb = new StringBuilder();
        sb.append(service.getServiceName()).append(" (Node ").append(service.getNodeId()).append(")");
        if (service.getHostname() != null) {
            sb.append("<br>\n").append(service.getHostname());
            if (service.getIpAddress() != null && !service.getIpAddress().equals(service.getHostname())) {
                sb.append("/").append(service.getIpAddress());
            }
        }
        return sb.toString();
    }

    /**
     * @return the monitors
     */
    private Map<Integer,GWTLocationMonitor> getMonitors() {
        return m_monitors;
    }

    /**
     * @param locationSpecificStatuses the locationSpecificStatuses to set
     */
    private void setLocationSpecificStatuses(List<GWTLocationSpecificStatus> locationSpecificStatuses) {
        m_locationSpecificStatuses = locationSpecificStatuses;
    }

    /**
     * @return the locationSpecificStatuses
     */
    private List<GWTLocationSpecificStatus> getLocationSpecificStatuses() {
        return m_locationSpecificStatuses;
    }

    /**
     * @param application the application to set
     */
    private void setApplicationInfo(ApplicationInfo application) {
        m_application = application;
    }

    /**
     * <p>getApplicationInfo</p>
     *
     * @return the application
     */
    public ApplicationInfo getApplicationInfo() {
        return m_application;
    }

    /**
     * @param statusTo the statusTo to set
     */
    private void setEndTime(Date endTime) {
        m_endTime = endTime;
    }

    /**
     * @return the statusTo
     */
    private Date getEndTime() {
        return m_endTime;
    }

    /**
     * @param startTime the startTime to set
     */
    private void setStartTime(Date startTime) {
        m_startTime = startTime;
    }

    /**
     * @return the startTime
     */
    private Date getStartTime() {
        return m_startTime;
    }
}
