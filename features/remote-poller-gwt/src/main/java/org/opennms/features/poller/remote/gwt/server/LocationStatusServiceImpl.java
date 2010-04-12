package org.opennms.features.poller.remote.gwt.server;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationState;
import org.opennms.features.poller.remote.gwt.client.BaseLocation;
import org.opennms.features.poller.remote.gwt.client.GWTApplication;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor;
import org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus;
import org.opennms.features.poller.remote.gwt.client.GWTMonitoredService;
import org.opennms.features.poller.remote.gwt.client.GWTPollResult;
import org.opennms.features.poller.remote.gwt.client.Location;
import org.opennms.features.poller.remote.gwt.client.LocationManager;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.features.poller.remote.gwt.server.geocoding.Geocoder;
import org.opennms.features.poller.remote.gwt.server.geocoding.GeocoderException;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
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
	private static final int AVAILABILITY_MS = 1000 * 60 * 60 * 24; // 1 day
	private static final int UPDATE_PERIOD = 1000 * 60; // 1 minute
	private static final int MAX_LOCATIONS_PER_EVENT = 50;
	private WebApplicationContext m_context;

	private volatile Map<String,MonitorStatus> m_monitorStatuses = new HashMap<String,MonitorStatus>();
	private volatile Geocoder m_geocoder;
	private static AtomicInteger m_inProgress = new AtomicInteger(0);

	private static volatile Timer myLocationTimer;
	private static volatile Date lastUpdated;
	private static volatile LocationMonitorDao m_locationDao;
	private static volatile ApplicationDao m_applicationDao;
	private static volatile MonitoredServiceDao m_monitoredServiceDao;
	private static volatile String m_apiKey = null;

	private void initializeContext() {
		if (m_context == null) {
			LogUtils.infof(this, "initializing context");
			m_context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		}
	}

	private void initializeDaos() {
		if (m_locationDao == null) {
			LogUtils.infof(this, "initializing location DAO");
			m_locationDao = m_context.getBean(LocationMonitorDao.class);
		}
		if (m_applicationDao == null) {
			LogUtils.infof(this, "initializing application DAO");
			m_applicationDao = m_context.getBean(ApplicationDao.class);
		}
		if (m_monitoredServiceDao == null) {
			LogUtils.infof(this, "initializing monitored service DAO");
			m_monitoredServiceDao = m_context.getBean(MonitoredServiceDao.class);
		}
	}

	private synchronized void initializeApiKey() {
		initializeContext();
		if (m_apiKey == null) {
			LogUtils.infof(this, "initializing API key");
			m_apiKey = m_context.getBean("apiKey", String.class);
			try {
				m_apiKey = URLEncoder.encode(m_apiKey, "UTF-8");
				m_apiKey = m_apiKey.replace("%", "//percent//");
			} catch (Exception e) {
				LogUtils.warnf(this, e, "unable to encode API key (%s)", m_apiKey);
			}
		}
	}

	private void initializeGeocoder() {
		if (m_geocoder == null) {
			LogUtils.infof(this, "initializing geocoder");
			m_geocoder = m_context.getBean("geocoder", Geocoder.class);
		}
	}

	public void start() {
		LogUtils.debugf(this, "starting location status service");
		initializeContext();
		initializeDaos();
		initializeGeocoder();

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
		initializeContext();
		initializeDaos();
		initializeGeocoder();
		final BaseLocation l = new BaseLocation();
		l.setLocationInfo(getLocation(m_locationDao.findMonitoringLocationDefinition(locationName)));
		return l;
	}

	public LocationInfo getLocationInfo(final String locationName) {
		final Location l = getLocation(locationName);
		return l.getLocationInfo();
	}

	public LocationDetails getLocationDetails(final String locationName) {
		final Location l = getLocation(locationName);
		return l.getLocationDetails();
	}

	private class InitialSenderTimerTask extends TimerTask {

		@Override
		public void run() {
			m_inProgress.incrementAndGet();
			LogUtils.debugf(this, "pushing initial data");
			lastUpdated = new Date();

			final Collection<LocationInfo> locations = new ArrayList<LocationInfo>();
			final Collection<OnmsMonitoringLocationDefinition> definitions = m_locationDao.findAllMonitoringLocationDefinitions();
			for (OnmsMonitoringLocationDefinition def : definitions) {
				final LocationInfo location = getLocation(def);
				locations.add(location);
				LogUtils.debugf(this, "pushing location: %s", def.getName());
//				addEventUserSpecific(getLocation(def));
//				addEvent(Location.LOCATION_EVENT_DOMAIN, getLocation(def));
				if (locations.size() >= MAX_LOCATIONS_PER_EVENT) {
					addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(new ArrayList<LocationInfo>(locations)));
					locations.clear();
				}
			}
			if (locations.size() > 0) {
				addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(locations));
			}
			m_locationDao.saveMonitoringLocationDefinitions(definitions);
			addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new UpdateCompleteRemoteEvent());
			m_inProgress.decrementAndGet();
		}

	}

	private class LocationUpdateSenderTimerTask extends TimerTask {
		@Override
		public void run() {
			if (m_inProgress.get() > 0) {
				LogUtils.warnf(this, "an update is already in progress, skipping");
				return;
			}
			m_inProgress.incrementAndGet();
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

			final Collection<LocationInfo> locations = new ArrayList<LocationInfo>();
			for (final OnmsMonitoringLocationDefinition def : definitions.values()) {
				final LocationInfo location = getLocation(def);
				LogUtils.debugf(this, "pushing location update: %s", location.getName());
				locations.add(location);
//				addEvent(Location.LOCATION_EVENT_DOMAIN, location);
			}
			addEvent(LocationManager.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(locations));

			lastUpdated = endDate;
			m_inProgress.decrementAndGet();
		}
	}

	private LocationInfo getLocation(final OnmsMonitoringLocationDefinition def) {

		GWTLatLng latLng = getLatLng(def);

		if (latLng == null) {
			LogUtils.debugf(this, "no geolocation or coordinates found, using OpenNMS World HQ");
			latLng = new GWTLatLng(35.715751, -79.16262);
		} else {
			def.setCoordinates(latLng.getCoordinates());
		}

		final LocationDetails ld = getLocationDetails(def);
		final LocationInfo locationInfo = new LocationInfo(def.getName(), def.getPollingPackageName(), def.getArea(), def.getGeolocation(), latLng.getCoordinates());
		locationInfo.setMonitorStatus(ld.getLocationMonitorState().getStatus());
		locationInfo.setApplicationStatus(ld.getApplicationState().getStatus());
		LogUtils.debugf(this, "getLocation(OnmsMonitoringLocationDefinition) returning %s", locationInfo.toString());
		return locationInfo;
	}

	private GWTLatLng getLatLng(final OnmsMonitoringLocationDefinition def) {
		GWTLatLng latLng = null;
		final String coordinateMatchString = "^\\s*[\\-\\d\\.]+\\s*,\\s*[\\-\\d\\.]+\\s*$";

		// first, see if we already have coordinates
		if (def.getCoordinates() != null && def.getCoordinates().matches(coordinateMatchString)) {
			LogUtils.debugf(this, "using coordinates: %s", def.getCoordinates());
			final String[] coordinates = def.getCoordinates().split(",");
			latLng = new GWTLatLng(Double.valueOf(coordinates[0]), Double.valueOf(coordinates[1]));
		}

		// if not, see if geolocation is coordinates
		if (latLng == null) {
			LogUtils.debugf(this, "using geolocation: %s", def.getGeolocation());
			if (def.getGeolocation() != null && def.getGeolocation().matches(coordinateMatchString)) {
				final String[] coordinates = def.getGeolocation().split(",");
				latLng = new GWTLatLng(Double.valueOf(coordinates[0]), Double.valueOf(coordinates[1]));
			}
		}

		if (latLng == null && def.getGeolocation() != null && !def.getGeolocation().equals("")) {
			try {
				latLng = m_geocoder.geocode(def.getGeolocation());
				LogUtils.debugf(this, "got coordinates %s for geolocation %s", latLng.getCoordinates(), def.getGeolocation());
			} catch (GeocoderException e) {
				LogUtils.warnf(this, e, "unable to geocode %s", def.getGeolocation());
			}
		}

		return latLng;
	}

	private LocationDetails getLocationDetails(OnmsMonitoringLocationDefinition def) {
		final LocationDetails ld = new LocationDetails();
		final MonitorStatusTracker mst = new MonitorStatusTracker(def.getName());
		final ApplicationStatusTracker ast = new ApplicationStatusTracker(def.getName());

		final Set<GWTLocationMonitor> monitors = new HashSet<GWTLocationMonitor>();

		for (OnmsLocationMonitor mon : m_locationDao.findByLocationDefinition(def)) {
			monitors.add(transformLocationMonitor(mon));
		}

		final Set<GWTApplication> applications = new HashSet<GWTApplication>();
		final Map<String,Set<OnmsMonitoredService>> services = new HashMap<String,Set<OnmsMonitoredService>>();
		
		for (final OnmsApplication application : m_applicationDao.findAll()) {
			applications.add(transformApplication(m_monitoredServiceDao.findByApplication(application), application));
		}

		for (final OnmsMonitoredService service : m_monitoredServiceDao.findAll()) {
			for (final OnmsApplication app : service.getApplications()) {
				final String appName = app.getName();
				Set<OnmsMonitoredService> serv = services.get(appName);
				if (serv == null) {
					serv = new HashSet<OnmsMonitoredService>();
					services.put(appName, serv);
				}
				serv.add(service);
			}
		}

		final Date to = new Date();
		final Date from = new Date(to.getTime() - AVAILABILITY_MS);
		for (OnmsLocationSpecificStatus status : m_locationDao.getStatusChangesBetween(from, to)) {
			mst.onStatus(status);
			ast.onStatus(status);
		}

		ld.setLocationMonitorState(new LocationMonitorState(monitors, mst.drain()));
		ld.setApplicationState(new ApplicationState(from, to, applications, ast.drainStatuses()));
		return ld;
	}
	
	private GWTApplication transformApplication(final Collection<OnmsMonitoredService> services, final OnmsApplication application) {
		final GWTApplication app = new GWTApplication();
		app.setId(application.getId());
		app.setName(application.getName());
		final Set<GWTMonitoredService> s = new HashSet<GWTMonitoredService>();
		for (final OnmsMonitoredService service : services) {
			s.add(transformMonitoredService(service));
		}
		app.setServices(s);
		return app;
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

	private class MonitorStatusTracker {
		private transient final Map<Integer,OnmsLocationSpecificStatus> m_statuses = new HashMap<Integer,OnmsLocationSpecificStatus>();
		private transient final String m_locationName;

		public MonitorStatusTracker(final String locationName) {
			m_locationName = locationName;
		}

		public void onStatus(final OnmsLocationSpecificStatus status) {
			if (status.getLocationMonitor().getDefinitionName().equals(m_locationName)) {
				LogUtils.tracef(this, "(added) status code for %s/%s is %d", status.getLocationMonitor().getDefinitionName(), status.getMonitoredService().getServiceName(), status.getStatusCode());
				m_statuses.put(status.getMonitoredService().getId(), status);
			} else {
				LogUtils.tracef(this, "(skipped) status code for %s/%s is %d", status.getLocationMonitor().getDefinitionName(), status.getMonitoredService().getServiceName(), status.getStatusCode());
			}
		}

		public Collection<GWTLocationSpecificStatus> drain() {
			final Collection<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
			synchronized(m_statuses) {
				for (OnmsLocationSpecificStatus status : m_statuses.values()) {
					statuses.add(transformLocationSpecificStatus(status));
				}
				m_statuses.clear();
			}
			return statuses;
		}
	}

	public class ApplicationStatusTracker {
		private String m_name;
		private Map<String,Collection<OnmsLocationSpecificStatus>> m_statuses = new HashMap<String,Collection<OnmsLocationSpecificStatus>>();
		
		public ApplicationStatusTracker(final String name) {
			m_name = name;
		}
		
		public void onStatus(final OnmsLocationSpecificStatus status) {
			if (status.getLocationMonitor().getDefinitionName().equals(m_name)) {
				LogUtils.tracef(this, "(added) status code for %s/%s is %d", status.getLocationMonitor().getDefinitionName(), status.getMonitoredService().getServiceName(), status.getStatusCode());
				for (OnmsApplication app : status.getMonitoredService().getApplications()) {
					Collection<OnmsLocationSpecificStatus> statuses = m_statuses.get(app.getName());
					if (statuses == null) {
						statuses = new ArrayList<OnmsLocationSpecificStatus>();
					}
					statuses.add(status);
					m_statuses.put(app.getName(), statuses);
				}
			} else {
				LogUtils.tracef(this, "(skipped) status code for %s/%s is %d", status.getLocationMonitor().getDefinitionName(), status.getMonitoredService().getServiceName(), status.getStatusCode());
			}
		}

		public Map<String,List<GWTLocationSpecificStatus>> drainStatuses() {
			final Map<String,List<GWTLocationSpecificStatus>> statuses = new HashMap<String,List<GWTLocationSpecificStatus>>();
			synchronized(m_statuses) {
				for (final String app : statuses.keySet()) {
					final List<GWTLocationSpecificStatus> s = new ArrayList<GWTLocationSpecificStatus>();
					for (OnmsLocationSpecificStatus status : m_statuses.get(app)) {
						s.add(transformLocationSpecificStatus(status));
					}
					statuses.put(app, s);
				}
				m_statuses.clear();
			}
			return statuses;
		}
		
	}

}