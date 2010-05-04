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

import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationMonitorState implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;
	private Set<GWTLocationMonitor> m_monitorsStarted = new HashSet<GWTLocationMonitor>();
	private Set<GWTLocationMonitor> m_monitorsStopped = new HashSet<GWTLocationMonitor>();
	private Set<GWTLocationMonitor> m_monitorsDisconnected = new HashSet<GWTLocationMonitor>();

	private Collection<GWTLocationSpecificStatus> m_locationStatuses;
	private Set<String> m_services = new HashSet<String>();

	private StatusDetails m_statusDetails;

	public LocationMonitorState() { }

	public LocationMonitorState(Collection<GWTLocationSpecificStatus> statuses) {
		initializeStatuses(statuses);
	}

	public LocationMonitorState(Collection<GWTLocationMonitor> monitors, Collection<GWTLocationSpecificStatus> statuses) {
		initializeStatuses(statuses);
		initializeMonitors(monitors);
	}

	public StatusDetails getStatusDetails() {
		if (m_statusDetails == null) {
			m_statusDetails = getStatusDetailsUncached();
		}
		return m_statusDetails;
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
		if (m_monitorsDisconnected.size() == 0) {
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

	protected int getMonitorsStarted() {
		return m_monitorsStarted.size();
	}

	protected int getMonitorsStopped() {
		return m_monitorsStopped.size();
	}
	
	protected int getMonitorsDisconnected() {
		return m_monitorsDisconnected.size();
	}

	protected Collection<String> getServiceNames() {
		final List<String> serviceNames = Collections.list(Collections.enumeration(m_services));
		Collections.sort(serviceNames);
		return serviceNames;
	}

	protected Collection<String> getServicesDown() {
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

	protected Collection<GWTLocationMonitor> getMonitorsWithServicesDown() {
		final Set<GWTLocationMonitor> monitors = new HashSet<GWTLocationMonitor>();
		for (GWTLocationSpecificStatus status : m_locationStatuses) {
			final GWTPollResult result = status.getPollResult();
			if (result.isDown()) {
				monitors.add(status.getLocationMonitor());
			}
		}
		return monitors;
	}

	protected StatusDetails getStatusDetailsUncached() {
		// blue/unknown: If no monitors are started for a location
		if (noMonitorsStarted()) {
			return StatusDetails.unknown("No monitors are started for this location.");
		}

		// blue/unknown: If no monitors have reported for a location
		if (m_locationStatuses == null || m_locationStatuses.size() == 0) {
			return StatusDetails.unknown("No monitors have reported for this location.");
		}

		// yellow/marginal: If all but 1 non-stopped monitors are disconnected
		if (allButOneMonitorsDisconnected()) {
			return StatusDetails.marginal("Only 1 monitor is started, the rest are disconnected.");
		}

		Set<String> anyDown = new HashSet<String>();
		Set<String> services = new HashSet<String>();
		Set<String> servicesDown = new HashSet<String>();
		for (String serviceName : m_services) {
			boolean serviceAllDown = true;
			boolean foundService = false;
			for (GWTLocationSpecificStatus status : m_locationStatuses) {
				final GWTMonitoredService monitoredService = status.getMonitoredService();
				if (monitoredService.getServiceName().equals(serviceName)) {
					foundService = true;
					services.add(serviceName);
					final GWTPollResult pollResult = status.getPollResult();
					if (pollResult.getStatus().equalsIgnoreCase("down")) {
						anyDown.add(serviceName);
					} else {
						serviceAllDown = false;
					}
				}
			}
			if (foundService && serviceAllDown) {
				servicesDown.add(serviceName);
			}
		}

		if (servicesDown.size() > 0) {
			if (servicesDown.size() == services.size()) {
				// red/down: If all started monitors report "down" for all services
				return StatusDetails.down("All services are down on all started monitors.");
			} else {
				// red/down: If all started monitors report "down" for the same service
				if (servicesDown.size() == 1) {
					return StatusDetails.down(servicesDown.iterator().next() + " has been reported down by all monitors.");
				} else {
					return StatusDetails.down("The following services are reported down by all monitors: " + StringUtils.join(servicesDown, ", ") + ".");
				}
			}
		}
		
		// yellow/marginal: If some (but not all) started monitors report "down" for the same service
		if (anyDown.size() > 0) {
			return StatusDetails.marginal("The following services are reported down by at least one monitor: " + StringUtils.join(anyDown, ", ") + ".");
		}

		return StatusDetails.up();
	}

	public String toString() {
		return "LocationMonitorState[started=" + m_monitorsStarted + ",stopped=" + m_monitorsStopped + ",disconnected=" + m_monitorsDisconnected + ",statuses="+m_locationStatuses+",services="+m_services+"]";
	}

}