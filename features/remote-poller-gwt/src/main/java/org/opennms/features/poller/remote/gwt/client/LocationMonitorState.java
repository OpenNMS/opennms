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

/**
 * <p>LocationMonitorState class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;
public class LocationMonitorState implements Serializable, IsSerializable {

	private static final long serialVersionUID = -7846127073655210004L;

	private Set<GWTLocationMonitor> m_monitorsStarted = new HashSet<GWTLocationMonitor>();
	private Set<GWTLocationMonitor> m_monitorsStopped = new HashSet<GWTLocationMonitor>();
	private Set<GWTLocationMonitor> m_monitorsDisconnected = new HashSet<GWTLocationMonitor>();

	private Collection<GWTLocationSpecificStatus> m_locationStatuses;
	private Set<String> m_serviceNames = new HashSet<String>();
	private Set<Integer> m_serviceIds = new  HashSet<Integer>();

	private StatusDetails m_statusDetails;

	/**
	 * <p>Constructor for LocationMonitorState.</p>
	 */
	public LocationMonitorState() { }

	/**
	 * <p>Constructor for LocationMonitorState.</p>
	 *
	 * @param statuses a {@link java.util.Collection} object.
	 */
	public LocationMonitorState(Collection<GWTLocationSpecificStatus> statuses) {
		initializeStatuses(statuses);
	}

	/**
	 * <p>Constructor for LocationMonitorState.</p>
	 *
	 * @param monitors a {@link java.util.Collection} object.
	 * @param statuses a {@link java.util.Collection} object.
	 */
	public LocationMonitorState(Collection<GWTLocationMonitor> monitors, Collection<GWTLocationSpecificStatus> statuses) {
		initializeStatuses(statuses);
		initializeMonitors(monitors);
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

	private void initializeMonitors(Collection<GWTLocationMonitor> monitors) {
		for (GWTLocationMonitor monitor : monitors) {
			handleMonitor(monitor);
		}
	}

	private void initializeStatuses(Collection<GWTLocationSpecificStatus> statuses) {
		if (statuses != null) {
			for (final GWTLocationSpecificStatus status : statuses) {
				handleMonitor(status.getLocationMonitor());
				m_serviceNames.add(status.getMonitoredService().getServiceName());
				m_serviceIds.add(status.getMonitoredService().getId());
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

	/**
	 * <p>allMonitorsStarted</p>
	 *
	 * @return a boolean.
	 */
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

	/**
	 * <p>atLeastOneMonitorStarted</p>
	 *
	 * @return a boolean.
	 */
	public boolean atLeastOneMonitorStarted() {
		if (m_monitorsStarted.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * <p>allButOneMonitorsDisconnected</p>
	 *
	 * @return a boolean.
	 */
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

	/**
	 * <p>allMonitorsDisconnected</p>
	 *
	 * @return a boolean.
	 */
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

	/**
	 * <p>noMonitorsStarted</p>
	 *
	 * @return a boolean.
	 */
	public boolean noMonitorsStarted() {
		if (m_monitorsStarted.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * <p>getMonitorsStarted</p>
	 *
	 * @return a int.
	 */
	public int getMonitorsStarted() {
		return m_monitorsStarted.size();
	}

	/**
	 * <p>getMonitorsStopped</p>
	 *
	 * @return a int.
	 */
	public int getMonitorsStopped() {
		return m_monitorsStopped.size();
	}
	
	/**
	 * <p>getMonitorsDisconnected</p>
	 *
	 * @return a int.
	 */
	public int getMonitorsDisconnected() {
		return m_monitorsDisconnected.size();
	}

	/**
	 * <p>getServices</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<GWTMonitoredService> getServices() {
	    final Set<GWTMonitoredService> services = new TreeSet<GWTMonitoredService>();
	    for (final GWTLocationSpecificStatus status : m_locationStatuses) {
	        services.add(status.getMonitoredService());
	    }
	    return services;
	}

	/**
	 * <p>getServicesDown</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<String> getServicesDown() {
		final Set<String> servicesDown = new HashSet<String>();
		for (final GWTLocationSpecificStatus status : m_locationStatuses) {
			final GWTMonitoredService service = status.getMonitoredService();
			final GWTPollResult result = status.getPollResult();
			if (result.isDown()) {
				servicesDown.add(service.getServiceName());
			}
		}
		return servicesDown;
	}

	/**
	 * <p>getMonitorsWithServicesDown</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<GWTLocationMonitor> getMonitorsWithServicesDown() {
		final Set<GWTLocationMonitor> monitors = new HashSet<GWTLocationMonitor>();
		for (final GWTLocationSpecificStatus status : m_locationStatuses) {
			final GWTPollResult result = status.getPollResult();
			if (result.isDown()) {
				monitors.add(status.getLocationMonitor());
			}
		}
		return monitors;
	}

	public boolean noMonitorsExist() {
	    return (
	        m_monitorsStopped.size() == 0 &&
	        m_monitorsDisconnected.size() == 0 &&
	        m_monitorsStarted.size() == 0
	    );
	}

	private StatusDetails getStatusDetailsUncached() {
        // white/uninitialized: If no monitors exist for a location
	    if (noMonitorsExist()) {
	        return StatusDetails.unknown("No monitors exist for this location.");
	    }
	    
	    // white/uninitialized: if no monitors have reported in
		if (m_locationStatuses == null || m_locationStatuses.size() == 0) {
			return StatusDetails.unknown("No monitors have reported in for this location.");
		}

		// orange/disconnected: all monitors are disconnected
		if (allMonitorsDisconnected()) {
		    return StatusDetails.disconnected("All monitors have disconnected.");
		}

        // grey/stopped: If no monitors are started for a location
		if (noMonitorsStarted()) {
            return StatusDetails.stopped("No monitors are started for this location.");
        }

		// yellow/marginal: If all but 1 non-stopped monitors are disconnected
		if (allButOneMonitorsDisconnected()) {
			return StatusDetails.marginal("Only 1 monitor is started, the rest are disconnected.");
		}

		Set<Integer> anyDown = new HashSet<Integer>();
		Set<Integer> services = new HashSet<Integer>();
		Set<Integer> servicesDown = new HashSet<Integer>();
		for (Integer serviceId : m_serviceIds) {
			boolean serviceAllDown = true;
			boolean foundService = false;
			for (GWTLocationSpecificStatus status : m_locationStatuses) {
				final GWTMonitoredService monitoredService = status.getMonitoredService();
				if (monitoredService.getId().equals(serviceId)) {
					foundService = true;
					services.add(serviceId);
					final GWTPollResult pollResult = status.getPollResult();
					if (pollResult.getStatus().equalsIgnoreCase("down")) {
						anyDown.add(serviceId);
					} else {
						serviceAllDown = false;
					}
				}
			}
			if (foundService && serviceAllDown) {
				servicesDown.add(serviceId);
			}
		}

		if (servicesDown.size() > 0) {
			if (servicesDown.size() == services.size()) {
				// red/down: If all started monitors report "down" for all services
				return StatusDetails.down("All services are down on all started monitors.");
			} else {
				// red/down: If all started monitors report "down" for the same service
				if (servicesDown.size() == 1) {
					return StatusDetails.down(getServiceName(servicesDown.iterator().next()) + " has been reported down by all monitors.");
				} else {
					return StatusDetails.down("The following services are reported down by all monitors: " + getServiceNames(servicesDown) + ".");
				}
			}
		}
		
		// yellow/marginal: If some (but not all) started monitors report "down" for the same service
		if (anyDown.size() > 0) {
			return StatusDetails.marginal("The following services are reported down by at least one monitor: " + getServiceNames(anyDown) + ".");
		}

		return StatusDetails.up();
	}
	
	private String getServiceNames(Set<Integer> serviceIds) {
	    StringBuilder buf = new StringBuilder();
	    
	    boolean first = true;
	    for(Integer serviceId : serviceIds) {
	        if (first) {
	            first = false;
	        } else {
	            buf.append(", ");
	        }
	        buf.append(getServiceName(serviceId));
	    }
	    return buf.toString();
	}
	
	private String getServiceName(Integer serviceId) {
	    for(GWTLocationSpecificStatus status : m_locationStatuses) {
	        if (serviceId.equals(status.getMonitoredService().getId())) {
	            return status.getMonitoredService().getServiceName();
	        }
	    }
	    return null;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "LocationMonitorState[started=" + m_monitorsStarted + ",stopped=" + m_monitorsStopped + ",disconnected=" + m_monitorsDisconnected + ",statuses="+m_locationStatuses+",services="+m_serviceNames+"]";
	}


}
