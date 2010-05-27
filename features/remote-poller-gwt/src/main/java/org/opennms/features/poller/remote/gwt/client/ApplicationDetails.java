package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.Interval;
import org.opennms.features.poller.remote.gwt.client.utils.IntervalUtils;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApplicationDetails implements Serializable, IsSerializable {
    private static final long serialVersionUID = 1L;

    private String m_name;

    private ApplicationInfo m_application;

    private Map<Integer,GWTLocationMonitor> m_monitors = new HashMap<Integer,GWTLocationMonitor>();

    private Map<Integer,GWTMonitoredService> m_services = new HashMap<Integer,GWTMonitoredService>();

    private List<GWTLocationSpecificStatus> m_locationSpecificStatuses;

    private Date m_statusFrom;

    private Date m_statusTo;

    private StatusDetails m_statusDetails;

    private Map<Integer, Map<Integer, List<GWTServiceOutage>>> m_outages;

    public ApplicationDetails() {
        m_name = null;
        m_locationSpecificStatuses = null;
        m_statusFrom = null;
        m_statusTo = null;
    }

    public ApplicationDetails(final ApplicationInfo application, final Date from, final Date to, final Collection<GWTLocationMonitor> monitors, final List<GWTLocationSpecificStatus> statuses) {
        m_name = application.getName();
        m_application = application;
        m_statusFrom = from;
        m_statusTo = to;
        if (monitors != null) {
            for (final GWTLocationMonitor monitor : monitors) {
                m_monitors.put(monitor.getId(), monitor);
            }
        }
        m_locationSpecificStatuses = statuses;
        if (m_locationSpecificStatuses != null) {
            Collections.sort(m_locationSpecificStatuses, new LocationSpecificStatusComparator());
            for (final GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
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
        if (m_locationSpecificStatuses == null) {
            return outages;
        }

        for (final GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
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
                        outage.setFrom(m_statusFrom);
                    }
                    if (outage.getTo() == null) {
                        outage.setTo(m_statusTo);
                    }
                }
            }
        }

        return outages;
    }

    private Collection<GWTMonitoredService> getAllServices() {
        final Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
        if (m_locationSpecificStatuses != null) {
            for (final GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
                services.add(status.getMonitoredService());
            }
        }
        return services;
    }

    public StatusDetails getStatusDetails() {
        if (m_statusDetails == null) {
            m_statusDetails = getStatusDetailsUncached();
        }
        return m_statusDetails;
    }

    private StatusDetails getStatusDetailsUncached() {
        if (m_locationSpecificStatuses == null || m_locationSpecificStatuses.size() == 0) {
            return StatusDetails.unknown("No locations have reported status updates.");
        }

        if (m_monitors == null || m_monitors.size() == 0) {
            return StatusDetails.unknown("No location monitors are currently reporting.");
        }

        final Set<Integer> monitorIds = new HashSet<Integer>();
        final Set<GWTMonitoredService> servicesWithOutages = new HashSet<GWTMonitoredService>();
        final Set<GWTMonitoredService> servicesDown = new HashSet<GWTMonitoredService>();

        boolean foundActiveMonitor = false;
        for (final GWTLocationMonitor monitor : m_monitors.values()) {
            if (monitor.getStatus().equals("STARTED")) {
                foundActiveMonitor = true;
                monitorIds.add(monitor.getId());
            }
        }
        if (! foundActiveMonitor) {
            return StatusDetails.unknown("No location monitors are currently reporting.");
        }

        Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();
        for (final Integer serviceId : outages.keySet()) {
            final List<GWTServiceOutage> locationOutages = new ArrayList<GWTServiceOutage>();
            for (final Integer monitorId : outages.get(serviceId).keySet()) {
                for (final GWTServiceOutage outage : outages.get(serviceId).get(monitorId)) {
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
            final Set<GWTLocationMonitor> monitorsPassing = new HashSet<GWTLocationMonitor>(m_monitors.values());

            Collections.sort(locationOutages);
            for (final GWTServiceOutage outage : locationOutages) {
                final GWTLocationMonitor monitor = outage.getMonitor();
                if (outage.getTo().compareTo(m_statusTo) >= 0) {
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
        for (final GWTMonitoredService service : m_application.getServices()) {
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
            for (final String key : unmonitoredServiceCounts.keySet()) {
                final Integer count = unmonitoredServiceCounts.get(key);
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

        if (servicesWithOutages.size() == m_application.getServices().size()) {
            final Set<String> names = new TreeSet<String>();
            for (final GWTMonitoredService service : servicesWithOutages) {
                names.add(service.getServiceName());
            }
            return StatusDetails.marginal("The following services were reported to have outages in this application: " + StringUtils.join(names, ", "));
        }

        return StatusDetails.up();
    }

    public Double getAvailability(final GWTMonitoredService service) {
        final Set<Interval> serviceOutages = getServiceOutageIntervals(service.getId());
        return computeAvailabilityForOutageIntervals(serviceOutages);
    }

    public Double getAvailability() {
        if (m_statusFrom == null || m_locationSpecificStatuses == null) {
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
        final Set<Interval> upIntervals = IntervalUtils.invert(m_statusFrom, m_statusTo, intervals);
        for (final Interval i : upIntervals) {
            timeAvailable += (i.getEndMillis() - i.getStartMillis());
        }

        final Long totalTime = m_statusTo.getTime() - m_statusFrom.getTime();
        final double availability = timeAvailable.doubleValue() / totalTime.doubleValue() * 100;
        return availability;
    }

    private Set<Interval> getServiceOutageIntervals(final Integer serviceId) {
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();
        final Set<Interval> serviceUpIntervals = IntervalUtils.getIntervalSet();
        final Map<Integer, List<GWTServiceOutage>> serviceOutages = outages.get(serviceId);
        if (serviceOutages != null && serviceOutages.size() != 0) {
            for (final GWTLocationMonitor monitor : m_monitors.values()) {
                final Integer locationId = monitor.getId();
                Set<Interval> locationIntervals = IntervalUtils.getIntervalSet();
                if (serviceOutages.containsKey(locationId)) {
                    for (final GWTServiceOutage outage : serviceOutages.get(locationId)) {
                        locationIntervals.add(new Interval(outage.getFrom().getTime(), outage.getTo().getTime()));
                    }
                }
                locationIntervals = IntervalUtils.invert(m_statusFrom, m_statusTo, IntervalUtils.normalize(locationIntervals));
                serviceUpIntervals.addAll(locationIntervals);
            }
            return IntervalUtils.invert(m_statusFrom, m_statusTo, IntervalUtils.normalize(serviceUpIntervals));
        }
        return IntervalUtils.getIntervalSet();
    }

    public String toString() {
        return "ApplicationDetails[name=" + m_name + ",range=" + m_statusFrom + "-" + m_statusTo + ",statuses=" + m_locationSpecificStatuses + "]";
    }

    public String getApplicationName() {
        return m_name;
    }

    public String getDetailsAsString() {
        // service id -> location id -> outages
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();

        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"applicationDetails\">\n");
        sb.append("<dl class=\"statusContents\">\n");

        Set<GWTMonitoredService> services = new TreeSet<GWTMonitoredService>(new Comparator<GWTMonitoredService>() {
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
                                if (outage.getTo().equals(m_statusTo) || outage.getTo().after(m_statusTo)) {
                                    locationsNotReporting.add(m_monitors.get(locationId));
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
                    if (serviceOutageIntervals.get(size - 1).getEndMillis() == m_statusTo.getTime()) {
                        styleName = Status.DOWN.getStyle();
                    } else {
                        styleName = Status.MARGINAL.getStyle();
                    }
                }
                styleName = Status.DOWN.getStyle();
            }

            final List<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>(m_monitors.values());
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
        sb.append(service.getServiceName()).append(" (node ").append(service.getNodeId()).append(")");
        if (service.getHostname() != null) {
            sb.append("<br>\n").append(service.getHostname());
            if (service.getIpAddress() != null && !service.getIpAddress().equals(service.getHostname())) {
                sb.append("/").append(service.getIpAddress());
            }
        }
        return sb.toString();
    }

    public Date getStart() {
        return m_statusFrom;
    }

    public Date getEnd() {
        return m_statusTo;
    }
}
