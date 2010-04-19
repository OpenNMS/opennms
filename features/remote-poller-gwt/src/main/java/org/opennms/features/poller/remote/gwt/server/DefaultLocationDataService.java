package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationState;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor;
import org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus;
import org.opennms.features.poller.remote.gwt.client.GWTMonitoredService;
import org.opennms.features.poller.remote.gwt.client.GWTPollResult;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class DefaultLocationDataService implements LocationDataService, InitializingBean {
	private static final int AVAILABILITY_MS = 1000 * 60 * 60 * 24; // 1 day

	@Autowired
	private LocationMonitorDao m_locationDao;
	@Autowired
	private ApplicationDao m_applicationDao;
	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;
	@Autowired
	private Geocoder m_geocoder;

	private volatile Map<String,MonitorStatus> m_monitorStatuses = new HashMap<String,MonitorStatus>();

	public DefaultLocationDataService() {
	}

	public void afterPropertiesSet() {
		Assert.notNull(m_locationDao);
		Assert.notNull(m_applicationDao);
		Assert.notNull(m_monitoredServiceDao);
		Assert.notNull(m_geocoder);

//		initialize();
	}

	public void initialize() {
		new InitializationThread().start();
	}

	private class InitializationThread extends Thread {
		public InitializationThread() {}
		
		public void run() {
			LogUtils.infof(this, "geolocating monitoring location definitions");
			final Collection<OnmsMonitoringLocationDefinition> definitions = m_locationDao.findAllMonitoringLocationDefinitions();
			for (final OnmsMonitoringLocationDefinition def : definitions) {
				final GWTLatLng latLng = getLatLng(def);
				if (latLng != null) {
					def.setCoordinates(latLng.getCoordinates());
				}
			}
			m_locationDao.saveMonitoringLocationDefinitions(definitions);
			LogUtils.infof(this, "finished geolocating monitoring location definitions");
		}
	}

	@Transactional
	public LocationInfo getLocationInfo(final String locationName) {
		final OnmsMonitoringLocationDefinition def = m_locationDao.findMonitoringLocationDefinition(locationName);
		if (def == null) {
			LogUtils.warnf(this, "no monitoring location found for name %s", locationName);
			return null;
		}
		return getLocationInfo(m_locationDao.findMonitoringLocationDefinition(locationName), null);
	}

	@Transactional
	public LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def, boolean includeStatus) {
		return includeStatus ? getLocationInfo(def, null) : getLocationInfo(def, Status.UNINITIALIZED);
	}

	@Transactional
	private LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def, final Status status) {
		if (def == null) {
			LogUtils.warnf(this, "no location definition specified");
			return null;
		}
		
		GWTLatLng latLng = getLatLng(def);
		
		if (latLng == null) {
			LogUtils.debugf(this, "no geolocation or coordinates found, using OpenNMS World HQ");
			latLng = new GWTLatLng(35.715751, -79.16262);
		} else {
			def.setCoordinates(latLng.getCoordinates());
		}
		
		final LocationInfo locationInfo = new LocationInfo(def.getName(), def.getPollingPackageName(), def.getArea(), def.getGeolocation(), latLng.getCoordinates());

		if (status == null) {
			final LocationDetails ld = getLocationDetails(def);
			locationInfo.setStatus(ld.getLocationMonitorState().getStatus());
		} else {
			locationInfo.setStatus(status);
		}
		LogUtils.debugf(this, "getLocation(" + def.getName() + ") returning %s", locationInfo.toString());
		return locationInfo;
	}

	@Transactional
	public LocationDetails getLocationDetails(final String locationName) {
		final OnmsMonitoringLocationDefinition def = m_locationDao.findMonitoringLocationDefinition(locationName);
		if (def == null) {
			LogUtils.warnf(this, "no monitoring location found for name %s", locationName);
			return null;
		}
		return getLocationDetails(m_locationDao.findMonitoringLocationDefinition(locationName));
	}

	@Transactional
	public LocationDetails getLocationDetails(final OnmsMonitoringLocationDefinition def) {
		final LocationDetails ld = new LocationDetails();
		final DefaultLocationDataService.MonitorStatusTracker mst = new DefaultLocationDataService.MonitorStatusTracker(def.getName());
		final DefaultLocationDataService.ApplicationStatusTracker ast = new DefaultLocationDataService.ApplicationStatusTracker(def.getName());

		final Set<GWTLocationMonitor> monitors = new HashSet<GWTLocationMonitor>();

		for (OnmsLocationMonitor mon : m_locationDao.findByLocationDefinition(def)) {
			monitors.add(transformLocationMonitor(mon));
		}

		final Set<ApplicationInfo> applications = new HashSet<ApplicationInfo>();
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

	@Transactional
	public Collection<LocationInfo> getUpdatedLocationsBetween(final Date startDate, final Date endDate) {
		final Collection<LocationInfo> locations = new ArrayList<LocationInfo>();
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

		for (final OnmsMonitoringLocationDefinition def : definitions.values()) {
			final LocationInfo location = getLocationInfo(def, null);
			locations.add(location);
		}
		
		return locations;
	}

	@Transactional
	public GWTLatLng getLatLng(final OnmsMonitoringLocationDefinition def) {
		GWTLatLng latLng = null;
		final String coordinateMatchString = "^\\s*[\\-\\d\\.]+\\s*,\\s*[\\-\\d\\.]+\\s*$";

		// first, see if we already have coordinates
		if (def.getCoordinates() != null && def.getCoordinates().matches(coordinateMatchString)) {
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

		// otherwise, try to geocode it
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

	@Transactional
	public void handleAllMonitoringLocationDefinitions(final Collection<LocationDefHandler> handlers) {
		final Collection<OnmsMonitoringLocationDefinition> definitions = m_locationDao.findAllMonitoringLocationDefinitions();
		for (LocationDefHandler handler : handlers) {
			handler.start(definitions.size());
		}
		for (final OnmsMonitoringLocationDefinition def : definitions) {
			for (LocationDefHandler handler : handlers) {
				handler.handle(def);
			}
		}
		for (final LocationDefHandler handler : handlers) {
			handler.finish();
		}
		m_locationDao.saveMonitoringLocationDefinitions(definitions);
	}

	@Transactional
	public void handleAllApplications(final Collection<ApplicationHandler> handlers) {
		final Collection<OnmsApplication> apps = m_applicationDao.findAll();
		for (final ApplicationHandler handler : handlers) {
			handler.start(apps.size());
		}
		for (final OnmsApplication app : m_applicationDao.findAll()) {
			final Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
			final Set<String> locationNames = new HashSet<String>();
			for (OnmsMonitoredService service : m_monitoredServiceDao.findByApplication(app)) {
				services.add(transformMonitoredService(service));
			}
			for (final OnmsLocationMonitor mon : m_locationDao.findByApplication(app)) {
				locationNames.add(mon.getDefinitionName());
			}
			final ApplicationInfo gwtApp = new ApplicationInfo(app.getId(), app.getName(), services, locationNames);
			for (final ApplicationHandler handler : handlers) {
				handler.handle(gwtApp);
			}
		}
		for (final ApplicationHandler handler : handlers) {
			handler.finish();
		}
	}

	private static GWTPollResult transformPollResult(final PollStatus pollStatus) {
		final GWTPollResult gResult = new GWTPollResult();
		gResult.setReason(pollStatus.getReason());
		gResult.setResponseTime(pollStatus.getResponseTime());
		gResult.setStatus(pollStatus.getStatusName());
		gResult.setTimestamp(pollStatus.getTimestamp());
		return gResult;
	}

	private static GWTMonitoredService transformMonitoredService(final OnmsMonitoredService monitoredService) {
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

	private static GWTLocationSpecificStatus transformLocationSpecificStatus(final OnmsLocationSpecificStatus status) {
		final GWTLocationSpecificStatus gStatus = new GWTLocationSpecificStatus();
		gStatus.setId(status.getId());
		gStatus.setLocationMonitor(transformLocationMonitor(status.getLocationMonitor()));
		gStatus.setPollResult(transformPollResult(status.getPollResult()));
		gStatus.setMonitoredService(transformMonitoredService(status.getMonitoredService()));
		return gStatus;
	}

	private static GWTLocationMonitor transformLocationMonitor(final OnmsLocationMonitor monitor) {
		final GWTLocationMonitor gMonitor = new GWTLocationMonitor();
		gMonitor.setId(monitor.getId());
		gMonitor.setDefinitionName(monitor.getDefinitionName());
		gMonitor.setName(monitor.getName());
		gMonitor.setStatus(monitor.getStatus().toString());
		gMonitor.setLastCheckInTime(monitor.getLastCheckInTime());
		return gMonitor;
	}

	private static ApplicationInfo transformApplication(final Collection<OnmsMonitoredService> services, final OnmsApplication application) {
		final ApplicationInfo app = new ApplicationInfo();
		app.setId(application.getId());
		app.setName(application.getName());
		final Set<GWTMonitoredService> s = new HashSet<GWTMonitoredService>();
		for (final OnmsMonitoredService service : services) {
			s.add(transformMonitoredService(service));
		}
		app.setServices(s);
		return app;
	}

	private static class MonitorStatusTracker {
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

	private static class ApplicationStatusTracker {
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
