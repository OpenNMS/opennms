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

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTApplicationStatus implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;
	private String m_name;
	private GWTApplication m_application;
	private List<GWTLocationSpecificStatus> m_locationSpecificStatuses;
	private Date m_statusFrom;
	private Date m_statusTo;

	public GWTApplicationStatus() {
		m_name = null;
		m_locationSpecificStatuses = null;
		m_statusFrom = null;
		m_statusTo = null;
	}

	public GWTApplicationStatus(final GWTApplication application, final Date from, final Date to, final List<GWTLocationSpecificStatus> statuses) {
		m_name = application.getName();
		m_application = application;
		m_statusFrom = from;
		m_statusTo = to;
		m_locationSpecificStatuses = statuses;
		if (m_locationSpecificStatuses != null) {
			Collections.sort(m_locationSpecificStatuses, new LocationSpecificStatusComparator());
		}
	}

	private Map<Integer,Map<Integer,List<GWTServiceOutage>>> getOutages() {
		// service id -> location id -> outages
		final Map<Integer,Map<Integer,List<GWTServiceOutage>>> outages = new HashMap<Integer,Map<Integer,List<GWTServiceOutage>>>();

		for (final GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
			final Integer serviceId = status.getMonitoredService().getId();
			final Integer monitorId = status.getLocationMonitor().getId();
			GWTServiceOutage lastOutage = null;
			Map<Integer,List<GWTServiceOutage>> serviceOutages = outages.get(serviceId);
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
						serviceOutages = new HashMap<Integer,List<GWTServiceOutage>>();
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
					if (outage.getTo() == null) {
						outage.setTo(m_statusTo);
					}
				}
			}
		}

		return outages;
	}

	@SuppressWarnings("unused")
	private boolean intersects(final GWTServiceOutage outage1, final GWTServiceOutage outage2) {
		long highestFrom = 0;
		long lowestTo = Long.MAX_VALUE;

		long current;
		
		current = outage1.getFrom().getTime();
		if (current > highestFrom) highestFrom = current;

		current = outage2.getFrom().getTime();
		if (current > highestFrom) highestFrom = current;
		
		current = outage1.getTo().getTime();
		if (current < lowestTo) lowestTo = current;

		current = outage2.getTo().getTime();
		if (current < lowestTo) lowestTo = current;

		if (highestFrom < lowestTo) {
			return true;
		} else {
			return false;
		}
	}

	private Collection<GWTMonitoredService> getAllServices() {
		final Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
		for (final GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
			services.add(status.getMonitoredService());
		}
		return services;
	}

	public Status getStatus() {
		if (m_locationSpecificStatuses == null || m_locationSpecificStatuses.size() == 0) {
			return Status.unknown("No status updates for application " + m_name);
		}

		final Set<Integer> monitorIds = new HashSet<Integer>();
		final Set<String> serviceNamesWithOutages = new HashSet<String>();
		final Set<String> serviceNamesDown = new HashSet<String>();
		final Set<GWTMonitoredService> servicesWithOutages = new HashSet<GWTMonitoredService>();
		final Set<GWTMonitoredService> servicesDown = new HashSet<GWTMonitoredService>();

		for (final GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
			monitorIds.add(status.getLocationMonitor().getId());
		}

		Map<Integer,Map<Integer,List<GWTServiceOutage>>> outages = getOutages();
		for (final Integer serviceId : outages.keySet()) {
			final Map<Integer,GWTServiceOutage> currentOutages = new HashMap<Integer,GWTServiceOutage>();
			final List<GWTServiceOutage> locationOutages = new ArrayList<GWTServiceOutage>();
			final Set<Integer> monitorIdsWithoutOutages = new HashSet<Integer>(monitorIds);
			for (final Integer monitorId : outages.get(serviceId).keySet()) {
				locationOutages.addAll(outages.get(serviceId).get(monitorId));
			}
			Collections.sort(locationOutages);

			for (final GWTServiceOutage outage : locationOutages) {
				// any outage means we're marginal
				serviceNamesWithOutages.add(outage.getService().getServiceName());
				servicesWithOutages.add(outage.getService());

				currentOutages.put(outage.getMonitor().getId(), outage);
				monitorIdsWithoutOutages.remove(outage.getMonitor().getId());
				long from = outage.getFrom().getTime();
				long to = outage.getTo().getTime();

				for (final Integer monitorId : monitorIds) {
					// don't compare to ourselves
					if (monitorId == outage.getMonitor().getId()) {
						continue;
					}

					final GWTServiceOutage o = currentOutages.get(monitorId);
					if (o == null) {
						continue;
					}

					// only compare (outage) for the same monitoring location definition
					if (!o.getMonitor().getDefinitionName().equals(outage.getMonitor().getDefinitionName())) {
						continue;
					}

					if (o.getFrom().getTime() > from) from = o.getFrom().getTime();
					if (o.getTo().getTime()   < to  ) to   = o.getTo().getTime();
				}
				if (from < to && monitorIdsWithoutOutages.size() == 0) {
					// if from is still less than to, all monitors in a location overlap by (to - from) milliseconds
					serviceNamesDown.add(outage.getService().getServiceName());
					servicesDown.add(outage.getService());
				}
			}
		}
		outages = null;

		Set<String> allServiceNames = new HashSet<String>();
		for (final GWTMonitoredService service : m_application.getServices()) {
			allServiceNames.add(service.getServiceName());
		}
		Set<String> unmonitoredServiceNames = new HashSet<String>(allServiceNames);
		for (final GWTMonitoredService service : getAllServices()) {
			unmonitoredServiceNames.remove(service.getServiceName());
		}

		if (unmonitoredServiceNames.size() > 0) {
			return Status.unknown("The following services were not being reported on by any monitor: " + Utils.join(unmonitoredServiceNames, ", "));
		}
		
		if (servicesDown.size() > 0) {
			return Status.down("The following services were reported as down by all monitors: " + Utils.join(serviceNamesDown, ","));
		}

		if (servicesWithOutages.size() == m_application.getServices().size()) {
			return Status.marginal("The following services were reported to have outages in this application: " + Utils.join(serviceNamesWithOutages, ", "));
		}

		return Status.UP;
	}

	public Double getAvailability() {
		if (m_statusFrom == null) {
			return null;
		}

		long timeAvailable = 0;
		boolean available = true;
		Date lastTime = m_statusFrom;
		for (GWTLocationSpecificStatus status : m_locationSpecificStatuses) {
			final Date pollTime = status.getPollTime();
			if (available) {
				timeAvailable += (pollTime.getTime() - lastTime.getTime());
			}
			lastTime = pollTime;
			available = !status.getPollResult().isDown();
		}
		if (available) {
			timeAvailable += (m_statusTo.getTime() - lastTime.getTime());
		}

		final long totalTime = m_statusTo.getTime() - m_statusFrom.getTime();
		return Double.valueOf(timeAvailable / totalTime * 100.0);
	}

	public String toString() {
		return "GWTApplicationStatus[name=" + m_name + ",range=" + m_statusFrom + "-" + m_statusTo + ",statuses=" + m_locationSpecificStatuses + "]";
	}

	public class GWTServiceOutage implements Serializable, IsSerializable, Comparable<GWTServiceOutage> {
		private static final long serialVersionUID = 1L;
		private GWTLocationMonitor m_monitor;
		private GWTMonitoredService m_service;
		private Date m_from;
		private Date m_to;

		public GWTServiceOutage() {}
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
			if (!(o instanceof GWTServiceOutage)) return false;
			GWTServiceOutage that = (GWTServiceOutage)o;
			if (this.getMonitor().getId().equals(that.getMonitor().getId())
				&& this.getService().getId().equals(that.getService().getId())
				&& this.getFrom().equals(that.getFrom())
				&& this.getTo().equals(that.getTo())
			) return true;
			return false;
		}

		public int hashCode() {
			return 3 * this.getMonitor().hashCode() + this.getService().hashCode() + this.getFrom().hashCode() + this.getTo().hashCode();
		}

		public String toString() {
			return "GWTServiceOutage[monitor=" + m_monitor + ",service=" + m_service + ",from=" + m_from + ",to=" + m_to + "]";
		}

		public int compareTo(GWTServiceOutage that) {
			int lastCompare;
			lastCompare = this.getService().compareTo(that.getService());
			if (lastCompare != 0) return lastCompare;
			lastCompare = this.getFrom().compareTo(that.getFrom());
			if (lastCompare != 0) return lastCompare;
			lastCompare = this.getMonitor().compareTo(that.getMonitor());
			if (lastCompare != 0) return lastCompare;
			lastCompare = this.getTo().compareTo(that.getTo());
			return lastCompare;
		}
		
		
	}

	private static class LocationSpecificStatusComparator implements Comparator<GWTLocationSpecificStatus> {

		public int compare(GWTLocationSpecificStatus a, GWTLocationSpecificStatus b) {
			int lastCompare;
			lastCompare = a.getMonitoredService().compareTo(b.getMonitoredService());
			if (lastCompare != 0) return lastCompare;
			lastCompare = a.getLocationMonitor().getDefinitionName().compareTo(b.getLocationMonitor().getDefinitionName());
			if (lastCompare != 0) return lastCompare;
			lastCompare = a.getPollTime().compareTo(b.getPollTime());
			if (lastCompare != 0) return lastCompare;
			lastCompare = a.getLocationMonitor().compareTo(b.getLocationMonitor());
			if (lastCompare != 0) return lastCompare;
			lastCompare = a.getPollResult().compareTo(b.getPollResult());
			return lastCompare;
		}

	}

}
