package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor;
import org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus;
import org.opennms.features.poller.remote.gwt.client.GWTMonitoredService;
import org.opennms.features.poller.remote.gwt.client.GWTPollResult;
import org.opennms.features.poller.remote.gwt.client.Location;
import org.opennms.features.poller.remote.gwt.client.LocationManager;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.UpdateComplete;
import org.opennms.features.poller.remote.gwt.client.UpdateLocation;
import org.opennms.features.poller.remote.gwt.client.UpdateLocations;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class LocationStatusServiceImpl extends RemoteEventServiceServlet implements LocationStatusService {
	private static final long serialVersionUID = 1L;
	private static final int UPDATE_PERIOD = 1000 * 60; // 1 minute
	private static final int MAX_LOCATIONS_PER_EVENT = 500;

	private volatile Map<String,MonitorStatus> m_monitorStatuses = new HashMap<String,MonitorStatus>(); // NOPMD by ranger on 3/18/10 9:39 AM

	private static volatile Timer myLocationTimer; // NOPMD by ranger on 3/18/10 9:40 AM
	private static volatile Date lastUpdated; // NOPMD by ranger on 3/18/10 9:40 AM
	private static volatile LocationMonitorDao m_locationDao; // NOPMD by ranger on 3/18/10 9:40 AM
	private static volatile String m_apiKey = null;

	private void initializeLocationDao() {
		if (m_locationDao == null) {
			final WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			m_locationDao = context.getBean(LocationMonitorDao.class);
		}
	}

	private void initializeApiKey() {
		if (m_apiKey == null) {
			final WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			m_apiKey = context.getBean("apiKey", String.class);
		}
	}

	public void start() {
		LogUtils.debugf(this, "starting location status service");
		initializeLocationDao();

		if (myLocationTimer == null) {
			LogUtils.debugf(this, "starting update timer");
			myLocationTimer = new Timer();
			myLocationTimer.schedule(new LocationUpdateSenderTimerTask(), UPDATE_PERIOD, UPDATE_PERIOD);
		}
		
		myLocationTimer.schedule(new InitialSenderTimerTask(), 1000);
	}

	public String getApiKey() {
		initializeApiKey();
		LogUtils.debugf(this, "returing API key " + m_apiKey);
		return m_apiKey;
	}

	public Location getLocation(final String locationName) {
		return getLocation(m_locationDao.findMonitoringLocationDefinition(locationName));
	}

	private class InitialSenderTimerTask extends TimerTask {

		@Override
		public void run() {
			LogUtils.debugf(this, "pushing initial data");
			lastUpdated = new Date();
			
			final Collection<Location> locations = new ArrayList<Location>();
			for (OnmsMonitoringLocationDefinition def : m_locationDao.findAllMonitoringLocationDefinitions()) {
				locations.add(getLocation(def));
				LogUtils.debugf(this, "pushing location: %s", def.getName());
//				addEventUserSpecific(getLocation(def));
//				addEvent(Location.LOCATION_EVENT_DOMAIN, getLocation(def));
				if (locations.size() >= MAX_LOCATIONS_PER_EVENT) {
					addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new UpdateLocations(new ArrayList<Location>(locations)));
					locations.clear();
				}
			}
			if (locations.size() > 0) {
				addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new UpdateLocations(locations));
			}
			addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new UpdateComplete());
		}
		
	}

	private class LocationUpdateSenderTimerTask extends TimerTask {
		@Override
		public void run() {
			LogUtils.debugf(this, "checking for monitor status updates");
			final Date startDate = lastUpdated;
			final Date endDate   = new Date();

			final Map<String,OnmsMonitoringLocationDefinition> definitions = new HashMap<String,OnmsMonitoringLocationDefinition>();

			// check for any monitors that have changed status
			for (OnmsMonitoringLocationDefinition def : m_locationDao.findAllMonitoringLocationDefinitions()) {
				for (OnmsLocationMonitor mon : m_locationDao.findByLocationDefinition(def)) {
					final MonitorStatus status = m_monitorStatuses.get(mon.getDefinitionName());
					if (status == null || !status.equals(mon.getStatus())) {
						definitions.put(def.getName(), def);
						m_monitorStatuses.put(def.getName(), mon.getStatus());
					}
				}
			}

			// check for any definitions that have status updates
			for (final OnmsLocationSpecificStatus status : m_locationDao.getStatusChangesBetween(startDate, endDate)) {
				final String definitionName = status.getLocationMonitor().getDefinitionName();
				if (!definitions.containsKey(definitionName)) {
					definitions.put(definitionName, m_locationDao.findMonitoringLocationDefinition(definitionName));
				}
			}

			final Collection<Location> locations = new ArrayList<Location>();
			for (final OnmsMonitoringLocationDefinition def : definitions.values()) {
				final Location location = getLocation(def);
				LogUtils.debugf(this, "pushing location update: %s", location.getName());
				locations.add(location);
//				addEvent(Location.LOCATION_EVENT_DOMAIN, location);
			}
			addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new UpdateLocations(locations));

			lastUpdated = endDate;
		}
	}

	private Location getLocation(final OnmsMonitoringLocationDefinition def) {

		final LocationUpdateTracker tracker = new LocationUpdateTracker(def.getName());

		final Collection<GWTLocationMonitor> monitors = new HashSet<GWTLocationMonitor>();

		for (OnmsLocationMonitor mon : m_locationDao.findByLocationDefinition(def)) {
			monitors.add(transformLocationMonitor(mon));
		}
		for (OnmsLocationSpecificStatus status : m_locationDao.getAllMostRecentStatusChanges()) {
			tracker.onStatus(status);
		}

		final LocationMonitorState lms = new LocationMonitorState(monitors, tracker.drain());

		if (def.getGeolocation() == null || def.getGeolocation().equals("")) {
			// OpenNMS World HQ
			def.setGeolocation("35.715751,-79.16262");
		}

		final Location loc = new UpdateLocation(def.getName(), def.getPollingPackageName(), def.getArea(), def.getGeolocation(), lms);
		LogUtils.debugf(this, "getLocation(OnmsMonitoringLocationDefinition) returning %s", loc.toString());
		return loc;
	}

	private GWTLocationMonitor transformLocationMonitor(final OnmsLocationMonitor monitor) {
		final GWTLocationMonitor gMonitor = new GWTLocationMonitor();
		gMonitor.setId(monitor.getId());
		gMonitor.setDefinitionName(monitor.getDefinitionName());
		gMonitor.setName(monitor.getName());
		gMonitor.setStatus(monitor.getStatus().toString());
		gMonitor.setLastCheckInTime(monitor.getLastCheckInTime());
		return gMonitor;
	}

	private GWTLocationSpecificStatus transformLocationSpecificStatus(final OnmsLocationSpecificStatus status) {
		final GWTLocationSpecificStatus gStatus = new GWTLocationSpecificStatus();
		gStatus.setId(status.getId());
		gStatus.setLocationMonitor(transformLocationMonitor(status.getLocationMonitor()));
		gStatus.setPollResult(transformPollResult(status.getPollResult()));
		gStatus.setMonitoredService(transformMonitoredService(status.getMonitoredService()));
		return gStatus;
	}

	private GWTMonitoredService transformMonitoredService(final OnmsMonitoredService monitoredService) {
		final GWTMonitoredService service = new GWTMonitoredService();
		service.setId(monitoredService.getId());
		/* FIXME: why does this get exceptions?!?
		final OnmsIpInterface ipi = monitoredService.getIpInterface();
		if (ipi != null) {
			service.setIpInterfaceId(ipi.getId());
			if (ipi.getNode() != null) {
				service.setNodeId(ipi.getNode().getId());
			}
			service.setIpAddress(ipi.getIpAddress());
			service.setHostname(ipi.getIpHostName());
			final OnmsSnmpInterface snmpi = ipi.getSnmpInterface();
			if (snmpi != null) {
				service.setIfIndex(snmpi.getIfIndex());
			}
		}
		*/
		service.setServiceName(monitoredService.getServiceName());
		return service;
	}

	private GWTPollResult transformPollResult(final PollStatus pollStatus) {
		final GWTPollResult gResult = new GWTPollResult();
		gResult.setReason(pollStatus.getReason());
		gResult.setResponseTime(pollStatus.getResponseTime());
		gResult.setStatus(pollStatus.getStatusName());
		gResult.setTimestamp(pollStatus.getTimestamp());
		return gResult;
	}

	public void setLocationMonitorDao(final LocationMonitorDao dao) {
		m_locationDao = dao;
	}

	private class LocationUpdateTracker {
		private transient final Set<OnmsLocationSpecificStatus> m_statuses = new HashSet<OnmsLocationSpecificStatus>();
		private transient final String m_locationName;

		public LocationUpdateTracker(final String locationName) {
			m_locationName = locationName;
		}

		public void onStatus(final OnmsLocationSpecificStatus status) {
			if (status.getLocationMonitor().getDefinitionName().equals(m_locationName)) {
				LogUtils.tracef(this, "(added) status code for %s/%s is %d", status.getLocationMonitor().getDefinitionName(), status.getMonitoredService().getServiceName(), status.getStatusCode());
				m_statuses.add(status);
			} else {
				LogUtils.tracef(this, "(skipped) status code for %s/%s is %d", status.getLocationMonitor().getDefinitionName(), status.getMonitoredService().getServiceName(), status.getStatusCode());
			}
		}
		
		public Collection<GWTLocationSpecificStatus> drain() {
			final Collection<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
			synchronized(m_statuses) {
				for (OnmsLocationSpecificStatus status : m_statuses) {
					statuses.add(transformLocationSpecificStatus(status));
				}
				m_statuses.clear();
			}
			return statuses;
		}
	}


}