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
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.Interval;
import org.opennms.features.poller.remote.gwt.client.utils.IntervalUtils;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApplicationDetails implements Serializable, IsSerializable {
    private static final long serialVersionUID = 1L;

    private String m_name;

    private ApplicationInfo m_application;

    private Collection<GWTLocationMonitor> m_locationMonitors;

    private List<GWTLocationSpecificStatus> m_locationSpecificStatuses;

    private Date m_statusFrom;

    private Date m_statusTo;

    private StatusDetails m_statusDetails;

    public ApplicationDetails() {
        m_name = null;
        m_locationMonitors = null;
        m_locationSpecificStatuses = null;
        m_statusFrom = null;
        m_statusTo = null;
    }

    public ApplicationDetails(final ApplicationInfo application, final Date from, final Date to, final Collection<GWTLocationMonitor> monitors, final List<GWTLocationSpecificStatus> statuses) {
        m_name = application.getName();
        m_application = application;
        m_statusFrom = from;
        m_statusTo = to;
        m_locationMonitors = monitors;
        m_locationSpecificStatuses = statuses;
        if (m_locationSpecificStatuses != null)
            Collections.sort(m_locationSpecificStatuses, new LocationSpecificStatusComparator());
    }

    private Map<Integer, Map<Integer, List<GWTServiceOutage>>> getOutages() {
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

        if (m_locationMonitors == null || m_locationMonitors.size() == 0) {
            return StatusDetails.unknown("No location monitors are currently reporting.");
        }

        final Set<Integer> monitorIds = new HashSet<Integer>();
        final Map<String,Integer> serviceNameOutageCounts = new HashMap<String,Integer>();
        final Map<String,Integer> serviceNameDownCounts = new HashMap<String,Integer>();
        final Set<GWTMonitoredService> servicesWithOutages = new HashSet<GWTMonitoredService>();
        final Set<GWTMonitoredService> servicesDown = new HashSet<GWTMonitoredService>();

        boolean foundActiveMonitor = false;
        for (final GWTLocationMonitor monitor : m_locationMonitors) {
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
                locationOutages.addAll(outages.get(serviceId).get(monitorId));
            }

            GWTMonitoredService service = null;
            if (locationOutages.size() > 0) {
                 service = locationOutages.iterator().next().getService();
            } else {
                return StatusDetails.unknown("No locations reporting for service ID " + serviceId);
            }

            final Set<GWTLocationMonitor> monitorsFailing = new HashSet<GWTLocationMonitor>();
            final Set<GWTLocationMonitor> monitorsPassing = new HashSet<GWTLocationMonitor>();
            
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
            for (final String key : serviceNameDownCounts.keySet()) {
                final Integer count = serviceNameDownCounts.get(key);
                names.add((count > 1)? key + " (" + count + ")" : key);
            }
            return StatusDetails.down("The following services were reported as down by all monitors: " + StringUtils.join(names, ","));
        }

        if (servicesWithOutages.size() == m_application.getServices().size()) {
            final Set<String> names = new TreeSet<String>();
            for (final String key : serviceNameOutageCounts.keySet()) {
                final Integer count = serviceNameOutageCounts.get(key);
                names.add((count > 1)? key + " (" + count + ")" : key);
            }
            return StatusDetails.marginal("The following services were reported to have outages in this application: " + StringUtils.join(names, ", "));
        }

        return StatusDetails.up();
    }



    public Double getAvailability() {
        if (m_statusFrom == null || m_locationSpecificStatuses == null) {
            return null;
        }

        // service id -> location id -> outages
        final Map<Integer, Map<Integer, List<GWTServiceOutage>>> outages = getOutages();

        Set<Interval> serviceOutageIntervals = IntervalUtils.getIntervalSet();

        for (final Integer serviceId : outages.keySet()) {
            final Set<Interval> serviceUpIntervals = IntervalUtils.getIntervalSet();
            final Map<Integer, List<GWTServiceOutage>> serviceOutage = outages.get(serviceId);
            if (serviceOutage.size() == 0) continue;
            for (final GWTLocationMonitor monitor : m_locationMonitors) {
                final Integer locationId = monitor.getId();
                Set<Interval> locationIntervals = IntervalUtils.getIntervalSet();
                if (serviceOutage.containsKey(locationId)) {
                    for (final GWTServiceOutage outage : serviceOutage.get(locationId)) {
                        locationIntervals.add(new Interval(outage.getFrom().getTime(), outage.getTo().getTime()));
                    }
                }
                locationIntervals = IntervalUtils.invert(m_statusFrom, m_statusTo, IntervalUtils.normalize(locationIntervals));
                serviceUpIntervals.addAll(locationIntervals);
            }
            final Set<Interval> normalized = IntervalUtils.normalize(serviceUpIntervals);
            final Set<Interval> downIntervals = IntervalUtils.invert(m_statusFrom, m_statusTo, normalized);
            serviceOutageIntervals.addAll(downIntervals);
        }

        serviceOutageIntervals = IntervalUtils.normalize(serviceOutageIntervals);

        Long timeAvailable = 0L;
        final Set<Interval> upIntervals = IntervalUtils.invert(m_statusFrom, m_statusTo, serviceOutageIntervals);
        for (final Interval i : upIntervals) {
            timeAvailable += (i.getEndMillis() - i.getStartMillis());
        }

        final Long totalTime = m_statusTo.getTime() - m_statusFrom.getTime();
        final double availability = timeAvailable.doubleValue() / totalTime.doubleValue() * 100;
        return availability;
    }

    public String toString() {
        return "ApplicationDetails[name=" + m_name + ",range=" + m_statusFrom + "-" + m_statusTo + ",statuses=" + m_locationSpecificStatuses + "]";
    }

    public class GWTServiceOutage implements Serializable, IsSerializable, Comparable<GWTServiceOutage> {
        private static final long serialVersionUID = 1L;

        private GWTLocationMonitor m_monitor;

        private GWTMonitoredService m_service;

        private Date m_from;

        private Date m_to;

        public GWTServiceOutage() {
        }

        public GWTServiceOutage(final GWTLocationMonitor monitor, final GWTMonitoredService service) {
            m_monitor = monitor;
            m_service = service;
        }

        public Date getFrom() {
            return m_from;
        }

        public void setFrom(final Date from) {
            m_from = from;
        }

        public Date getTo() {
            return m_to;
        }

        public void setTo(final Date to) {
            m_to = to;
        }

        public GWTLocationMonitor getMonitor() {
            return m_monitor;
        }

        public void setMonitor(final GWTLocationMonitor monitor) {
            m_monitor = monitor;
        }

        public GWTMonitoredService getService() {
            return m_service;
        }

        public void setService(final GWTMonitoredService service) {
            m_service = service;
        }

        public boolean equals(Object o) {
            if (!(o instanceof GWTServiceOutage))
                return false;
            GWTServiceOutage that = (GWTServiceOutage) o;
            final GWTLocationMonitor thisMonitor = this.getMonitor();
            final GWTLocationMonitor thatMonitor = that.getMonitor();
            final GWTMonitoredService thisService = this.getService();
            final GWTMonitoredService thatService = that.getService();
            return EqualsUtil.areEqual(
                thisMonitor == null? null : thisMonitor.getId(),
                thatMonitor == null? null : thatMonitor.getId()
            ) && EqualsUtil.areEqual(
                thisService == null? null : thisService.getId(),
                thatService == null? null : thatService.getId()
            ) && EqualsUtil.areEqual(this.getFrom(), that.getFrom())
              && EqualsUtil.areEqual(this.getTo(), that.getTo());
        }

        public int hashCode() {
            return new HashCodeBuilder().append(this.getMonitor()).append(this.getService()).append(this.getFrom()).append(this.getTo()).toHashcode();
        }

        public String toString() {
            return "GWTServiceOutage[monitor=" + m_monitor + ",service=" + m_service + ",from=" + m_from + ",to=" + m_to + "]";
        }

        public int compareTo(final GWTServiceOutage that) {
            if (that == null) return -1;
            return new CompareToBuilder()
                .append(this.getService(), that.getService())
                .append(this.getFrom(), that.getFrom())
                .append(this.getMonitor(), that.getMonitor())
                .append(this.getTo(),that.getTo())
                .toComparison();
        }

    }

    private static class LocationSpecificStatusComparator implements Comparator<GWTLocationSpecificStatus> {
        public int compare(final GWTLocationSpecificStatus a, final GWTLocationSpecificStatus b) {
            return new CompareToBuilder()
                .append(a.getMonitoredService(), b.getMonitoredService())
                .append(
                     a.getLocationMonitor() == null? null : a.getLocationMonitor().getDefinitionName(),
                     b.getLocationMonitor() == null? null : b.getLocationMonitor().getDefinitionName()
                )
                .append(a.getPollTime(), b.getPollTime())
                .append(a.getLocationMonitor(), b.getLocationMonitor())
                .toComparison();
        }
    }

    public String getApplicationName() {
        return m_name;
    }

    public String getDetailsAsString() {
        // TODO Change this up for nice details to print out.
        return toString();
    }

    public Date getStart() {
        return m_statusFrom;
    }

    public Date getEnd() {
        return m_statusTo;
    }
}
