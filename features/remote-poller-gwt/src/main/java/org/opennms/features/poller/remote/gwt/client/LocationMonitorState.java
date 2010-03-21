/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationMonitorState implements Serializable {
	private static final long serialVersionUID = 1L;
	private Set<GWTLocationMonitor> m_monitorsStarted = new HashSet<GWTLocationMonitor>();
	private Set<GWTLocationMonitor> m_monitorsStopped = new HashSet<GWTLocationMonitor>();
	private Set<GWTLocationMonitor> m_monitorsDisconnected = new HashSet<GWTLocationMonitor>();

	private Collection<GWTLocationSpecificStatus> m_locationStatuses;
	private Set<String> m_services = new HashSet<String>();

	private transient ServiceStatus m_status;

	public static enum ServiceStatus {
		UP,
		MARGINAL,
		DOWN,
		UNKNOWN;

		public String getColor() {
			String color;
			if (this.equals(ServiceStatus.UP)){
				color = "#00ff00";
			} else if (this.equals(ServiceStatus.MARGINAL)) {
				color = "#ffff00";
			} else if (this.equals(ServiceStatus.DOWN)) {
				color = "#ff0000";
			} else {
				color = "#0000ff";
			}
			return color;
		}
		
		public String getStyle() {
			String cssClass;
			if (this.equals(ServiceStatus.UP)) {
				cssClass = "statusUp";
			} else if (this.equals(ServiceStatus.MARGINAL)) {
				cssClass = "statusMarginal";
			} else if (this.equals(ServiceStatus.DOWN)) {
				cssClass = "statusDown";
			} else {
				cssClass = "statusUnknown";
			}
			return cssClass;
		}
	}
	
	public LocationMonitorState() { }

	public LocationMonitorState(Collection<GWTLocationSpecificStatus> statuses) {
		initializeStatuses(statuses);
		m_status = getStatusUncached();
	}

	public LocationMonitorState(Collection<GWTLocationMonitor> monitors, Collection<GWTLocationSpecificStatus> statuses) {
		initializeStatuses(statuses);
		initializeMonitors(monitors);
		m_status = getStatusUncached();
	}

	private void initializeMonitors(Collection<GWTLocationMonitor> monitors) {
		for (GWTLocationMonitor monitor : monitors) {
			handleMonitor(monitor);
		}
	}

	private void initializeStatuses(Collection<GWTLocationSpecificStatus> statuses) {
		if (statuses != null) {
			for (final GWTLocationSpecificStatus status : statuses) {
				handleMonitor(status.getLocationMonitor());
				m_services.add(status.getMonitoredService().getServiceName());
			}
			m_locationStatuses = statuses;
		}
	}

	private void handleMonitor(final GWTLocationMonitor monitor) {
		if (m_monitorsStarted.contains(monitor) || m_monitorsStopped.contains(monitor) || m_monitorsDisconnected.contains(monitor)) {
			return;
		}
		final String statusText = monitor.getStatus();
		if (statusText.equalsIgnoreCase("CONFIG_CHANGED")
				|| statusText.equalsIgnoreCase("STARTED")) {
			m_monitorsStarted.add(monitor);
		} else if (statusText.equalsIgnoreCase("DISCONNECTED")) {
			m_monitorsDisconnected.add(monitor);
		} else if (statusText.equalsIgnoreCase("REGISTERED")
				|| statusText.equalsIgnoreCase("PAUSED")
				|| statusText.equalsIgnoreCase("STOPPED")) {
			m_monitorsStopped.add(monitor);
		} else {
			throw new RuntimeException("unknown monitor status: " + statusText);
		}
	}

	public boolean allMonitorsStarted() {
		if (m_monitorsStarted.size() == 0) {
			return false;
		}
		if (m_monitorsStopped.size() > 0) {
			return false;
		}
		if (m_monitorsDisconnected.size() > 0) {
			return false;
		}
		return true;
	}

	public boolean atLeastOneMonitorStarted() {
		if (m_monitorsStarted.size() > 0) {
			return true;
		}
		return false;
	}

	public boolean allButOneMonitorsDisconnected() {
		if (m_monitorsDisconnected.size() < 2) {
			return false;
		}
		if (m_monitorsStarted.size() > 1) {
			return false;
		}
		if (m_monitorsStarted.size() == 0) {
			return false;
		}
		return true;
	}

	public boolean allMonitorsDisconnected() {
		if (m_monitorsDisconnected.size() == 0) {
			return false;
		}
		if (m_monitorsStarted.size() > 0) {
			return false;
		}
		if (m_monitorsStopped.size() > 0) {
			return false;
		}
		return true;
	}

	public boolean noMonitorsStarted() {
		if (m_monitorsStarted.size() == 0) {
			return true;
		}
		return false;
	}

	public ServiceStatus getStatus() {
		if (m_status == null) {
			m_status = getStatusUncached();
		}
		return m_status;
	}

	public int getMonitorsStarted() {
		return m_monitorsStarted.size();
	}

	public int getMonitorsStopped() {
		return m_monitorsStopped.size();
	}
	
	public int getMonitorsDisconnected() {
		return m_monitorsDisconnected.size();
	}

	public Collection<String> getServiceNames() {
		final List<String> serviceNames = Collections.list(Collections.enumeration(m_services));
		Collections.sort(serviceNames);
		return serviceNames;
	}

	public Collection<String> getServicesDown() {
		final Set<String> servicesDown = new HashSet<String>();
		for (GWTLocationSpecificStatus status : m_locationStatuses) {
			final GWTMonitoredService service = status.getMonitoredService();
			final GWTPollResult result = status.getPollResult();
			if (result.isDown()) {
				servicesDown.add(service.getServiceName());
			}
		}
		return servicesDown;
	}

	public Collection<GWTLocationMonitor> getMonitorsWithServicesDown() {
		final Set<GWTLocationMonitor> monitors = new HashSet<GWTLocationMonitor>();
		for (GWTLocationSpecificStatus status : m_locationStatuses) {
			final GWTPollResult result = status.getPollResult();
			if (result.isDown()) {
				monitors.add(status.getLocationMonitor());
			}
		}
		return monitors;
	}

	protected ServiceStatus getStatusUncached() {
		// blue/unknown: If no monitors are started for a location
		if (noMonitorsStarted()) {
			return ServiceStatus.UNKNOWN;
		}

		// yellow/marginal: If all but 1 non-stopped monitors are disconnected
		if (allButOneMonitorsDisconnected()) {
			return ServiceStatus.MARGINAL;
		}
		
		boolean anyDown = false;
		Set<String> servicesDown = new HashSet<String>();
		for (String serviceName : m_services) {
			boolean serviceAllDown = true;
			boolean foundService = false;
			for (GWTLocationSpecificStatus status : m_locationStatuses) {
				final GWTMonitoredService monitoredService = status.getMonitoredService();
				if (monitoredService.getServiceName().equals(serviceName)) {
					foundService = true;
					final GWTPollResult pollResult = status.getPollResult();
					if (pollResult.getStatus().equalsIgnoreCase("down")) {
						anyDown = true;
					} else {
						serviceAllDown = false;
					}
				}
			}
			if (foundService && serviceAllDown) {
				servicesDown.add(serviceName);
			}
		}

		// red/down: If all started monitors report "down" for all services
		// red/down: If all started monitors report "down" for the same service
		if (servicesDown.size() > 0) {
			return ServiceStatus.DOWN;
		}
		
		// yellow/marginal: If some (but not all) started monitors report "down" for the same service
		if (anyDown) {
			return ServiceStatus.MARGINAL;
		}

		return ServiceStatus.UP;
	}

	public String toString() {
		return "LocationMonitorState[started=" + m_monitorsStarted + ",stopped=" + m_monitorsStopped + ",disconnected=" + m_monitorsDisconnected + ",statuses="+m_locationStatuses+",services="+m_services+"]";
	}

}