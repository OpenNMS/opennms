package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.ApplicationState;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor;
import org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus;
import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;
import org.opennms.features.poller.remote.gwt.client.GWTMonitoredService;
import org.opennms.features.poller.remote.gwt.client.GWTPollResult;
import org.opennms.features.poller.remote.gwt.client.LocationMonitorState;
import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.server.geocoding.Geocoder;
import org.opennms.features.poller.remote.gwt.server.geocoding.GeocoderException;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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

    public void setLocationMonitorDao(final LocationMonitorDao dao) {
        m_locationDao = dao;
    }

    public void setApplicationDao(final ApplicationDao dao) {
        m_applicationDao = dao;
    }

    public void setMonitoredServiceDao(final MonitoredServiceDao dao) {
        m_monitoredServiceDao = dao;
    }

    public void setGeocoder(final Geocoder geocoder) {
        m_geocoder = geocoder;
    }

    private CountDownLatch m_initializationLatch = new CountDownLatch(1);

    private volatile Map<String, MonitorStatus> m_monitorStatuses = new HashMap<String, MonitorStatus>();

    // whether to save monitoring-locations.xml changes
    public boolean m_save = true;

    public DefaultLocationDataService() {
    }

    public void setSave(final boolean save) {
        m_save = save;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_locationDao);
        Assert.notNull(m_applicationDao);
        Assert.notNull(m_monitoredServiceDao);
        Assert.notNull(m_geocoder);

        initialize();
    }

    public void initialize() {
        new InitializationThread().start();
    }

    private class InitializationThread extends Thread {
        public InitializationThread() {
        }

        public void run() {
            LogUtils.infof(this, "geolocating monitoring location definitions");
            final Collection<OnmsMonitoringLocationDefinition> definitions = m_locationDao.findAllMonitoringLocationDefinitions();
            for (final OnmsMonitoringLocationDefinition def : definitions) {
                final GWTLatLng latLng = getLatLng(def, true);
                if (latLng != null) {
                    def.setCoordinates(latLng.getCoordinates());
                }
            }
            if (m_save) {
                m_locationDao.saveMonitoringLocationDefinitions(definitions);
            }
            LogUtils.infof(this, "finished geolocating monitoring location definitions");
            m_initializationLatch.countDown();
        }
    }

    @Transactional
    public LocationInfo getLocationInfo(final String locationName) {
        waitForGeocoding("getLocationInfo");

        final OnmsMonitoringLocationDefinition def = m_locationDao.findMonitoringLocationDefinition(locationName);
        if (def == null) {
            LogUtils.warnf(this, "no monitoring location found for name %s", locationName);
            return null;
        }
        return getLocationInfo(def, null);
    }

    @Transactional
    public LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def, boolean includeStatus) {
        return includeStatus ? getLocationInfo(def, null) : getLocationInfo(def, StatusDetails.uninitialized());
    }

    @Transactional
    public LocationInfo getLocationInfoForMonitor(Integer monitorId) {
        waitForGeocoding("getLocationInfoForMonitor");

        final OnmsCriteria criteria = new OnmsCriteria(OnmsLocationMonitor.class).add(Restrictions.eq("id", monitorId));
        final List<OnmsLocationMonitor> monitors = m_locationDao.findMatching(criteria);
        if (monitors == null) {
            LogUtils.warnf(this, "unable to get location monitor list for monitor ID '%d'", monitorId);
            return null;
        }

        final String definitionName = monitors.get(0).getDefinitionName();
        final OnmsMonitoringLocationDefinition def = m_locationDao.findMonitoringLocationDefinition(definitionName);
        if (def == null) {
            LogUtils.warnf(this, "unable to find monitoring location definition for '%s'", definitionName);
            return null;
        }

        return getLocationInfo(def, null);
    }

    @Transactional
    private LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def, final StatusDetails status) {
        waitForGeocoding("getLocationInfo");

        if (def == null) {
            LogUtils.warnf(this, "no location definition specified");
            return null;
        }

        GWTLatLng latLng = getLatLng(def, false);

        if (latLng == null) {
            LogUtils.debugf(this, "no geolocation or coordinates found, using OpenNMS World HQ");
            latLng = new GWTLatLng(35.715751, -79.16262);
        } else {
            def.setCoordinates(latLng.getCoordinates());
        }

        final GWTMarkerState state = new GWTMarkerState(def.getName(), latLng, status == null ? Status.UNINITIALIZED : status.getStatus());
        final LocationInfo locationInfo = new LocationInfo(
            def.getName(),
            def.getPollingPackageName(),
            def.getArea(),
            def.getGeolocation(),
            latLng.getCoordinates(),
            def.getPriority(),
            state,
            status,
            def.getTags()
        );

        if (status == null) {
            final LocationDetails ld = getLocationDetails(def);
            final StatusDetails monitorStatus = ld.getLocationMonitorState().getStatusDetails();
            state.setStatus(monitorStatus.getStatus());
            locationInfo.setStatusDetails(monitorStatus);
        }
        LogUtils.debugf(this, "getLocationInfo(%s) returning %s", def.getName(), locationInfo.toString());
        return locationInfo;
    }

    @Transactional
    public ApplicationInfo getApplicationInfo(final String applicationName) {
        waitForGeocoding("getApplicationInfo");

        final OnmsApplication app = m_applicationDao.findByName(applicationName);
        if (app == null) {
            LogUtils.warnf(this, "no application found with name '%s'", applicationName);
        }
        return getApplicationInfo(app, null);
    }

    @Transactional
    public ApplicationInfo getApplicationInfo(final OnmsApplication app, boolean includeStatus) {
        return includeStatus ? getApplicationInfo(app, null) : getApplicationInfo(app, StatusDetails.uninitialized());
    }

    @Transactional
    private ApplicationInfo getApplicationInfo(final OnmsApplication app, final StatusDetails status) {
        waitForGeocoding("getApplicationInfo");

        if (app == null) {
            LogUtils.warnf(this, "no application specified");
            return null;
        }

        final Set<GWTMonitoredService> services = new TreeSet<GWTMonitoredService>();
        final Set<String> locationNames = new TreeSet<String>();
        for (final OnmsMonitoredService service : m_monitoredServiceDao.findByApplication(app)) {
            services.add(transformMonitoredService(service));
        }
        for (final OnmsLocationMonitor mon : m_locationDao.findByApplication(app)) {
            locationNames.add(mon.getDefinitionName());
        }

        final ApplicationInfo applicationInfo = new ApplicationInfo(app.getId(), app.getName(), services, locationNames, status);

        if (status == null) {
            final ApplicationDetails details = getApplicationDetails(app);
            applicationInfo.setStatusDetails(details.getStatusDetails());
        }
        LogUtils.debugf(this, "getApplicationInfo(%s) returning %s", app.getName(), applicationInfo.toString());
        return applicationInfo;
    }

    @Transactional
    public LocationDetails getLocationDetails(final String locationName) {
        waitForGeocoding("getLocationDetails");

        final OnmsMonitoringLocationDefinition def = m_locationDao.findMonitoringLocationDefinition(locationName);
        if (def == null) {
            LogUtils.warnf(this, "no monitoring location found for name %s", locationName);
            return null;
        }
        return getLocationDetails(def);
    }

    @Transactional
    public LocationDetails getLocationDetails(final OnmsMonitoringLocationDefinition def) {
        waitForGeocoding("getLocationDetails");

        final LocationDetails ld = new LocationDetails();
        final DefaultLocationDataService.MonitorStatusTracker mst = new DefaultLocationDataService.MonitorStatusTracker(def.getName());
        final DefaultLocationDataService.ApplicationStatusTracker ast = new DefaultLocationDataService.ApplicationStatusTracker(def.getName());

        final List<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();

        for (OnmsLocationMonitor mon : m_locationDao.findByLocationDefinition(def)) {
            monitors.add(transformLocationMonitor(mon));
        }

        final Set<ApplicationInfo> applications = new HashSet<ApplicationInfo>();
        final Map<String, Set<OnmsMonitoredService>> services = new HashMap<String, Set<OnmsMonitoredService>>();

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
        for (OnmsLocationSpecificStatus status : m_locationDao.getAllMostRecentStatusChanges()) {
            mst.onStatus(status);
            ast.onStatus(status);
        }

        ld.setLocationMonitorState(new LocationMonitorState(monitors, mst.drain()));
        ld.setApplicationState(new ApplicationState(from, to, applications, monitors, ast.drainStatuses()));
        LogUtils.debugf(this, "getLocationDetails(%s) returning %s", def.getName(), ld);
        return ld;
    }

    @Transactional
    public ApplicationDetails getApplicationDetails(final String applicationName) {
        waitForGeocoding("getApplicationDetails");

        final OnmsApplication app = m_applicationDao.findByName(applicationName);
        return getApplicationDetails(app);
    }

    @Transactional
    public ApplicationDetails getApplicationDetails(final OnmsApplication app) {
        waitForGeocoding("getApplicationDetails");

        final ApplicationInfo applicationInfo = getApplicationInfo(app, false);
        List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
        
        final Date to = new Date();
        final Date from = new Date(to.getTime() - AVAILABILITY_MS);
        
        final Collection<OnmsMonitoredService> services = m_monitoredServiceDao.findByApplication(app);
        final List <GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();
        
        for (final OnmsLocationMonitor monitor : m_locationDao.findByApplication(app)) {
            monitors.add(transformLocationMonitor(monitor));
            for (final OnmsLocationSpecificStatus locationSpecificStatus : m_locationDao.getStatusChangesForLocationBetween(from, to, monitor.getDefinitionName())) {
                if (services.contains(locationSpecificStatus.getMonitoredService())) {
                    statuses.add(transformLocationSpecificStatus(locationSpecificStatus));
                }
            }
        }

        ApplicationDetails details = new ApplicationDetails(applicationInfo, from, to, monitors, statuses);
        LogUtils.warnf(this, "getApplicationDetails(%s) returning %s", app.getName(), details);
        return details;
    }

    @Transactional
    public Collection<LocationInfo> getUpdatedLocationsBetween(final Date startDate, final Date endDate) {
        waitForGeocoding("getApplicationDetails");

        final Collection<LocationInfo> locations = new ArrayList<LocationInfo>();
        final Map<String, OnmsMonitoringLocationDefinition> definitions = new HashMap<String, OnmsMonitoringLocationDefinition>();

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
    public GWTLatLng getLatLng(final OnmsMonitoringLocationDefinition def, boolean geocode) {
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
        waitForGeocoding("handleAllMonitoringLocationDefinitions");

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
        waitForGeocoding("handleAllApplications");

        final Collection<OnmsApplication> apps = m_applicationDao.findAll();
        for (final ApplicationHandler handler : handlers) {
            handler.start(apps.size());
        }
        for (final OnmsApplication app : apps) {
            for (final ApplicationHandler handler : handlers) {
                handler.handle(app);
            }
        }
        for (final ApplicationHandler handler : handlers) {
            handler.finish();
        }
    }

    @Transactional
    public Collection<ApplicationInfo> getApplicationsForLocation(final LocationInfo locationInfo) {
        waitForGeocoding("getApplicationsForLocation");

        final Map<String,ApplicationInfo> apps = new HashMap<String,ApplicationInfo>();
        for (final OnmsLocationSpecificStatus status : m_locationDao.getMostRecentStatusChangesForLocation(locationInfo.getName())) {
            for (final OnmsApplication app : status.getMonitoredService().getApplications()) {
                if (!apps.containsKey(app.getName())) {
                    apps.put(app.getName(), getApplicationInfo(app, true));
                }
            }
        }
        return apps.values();
    }

    private void waitForGeocoding(final String method) {
        if (m_initializationLatch.getCount() > 0) {
            LogUtils.warnf(this, "%s() waiting for geocoding to finish", method);
            try {
                m_initializationLatch.await();
            } catch (InterruptedException e) {
                LogUtils.warnf(this, e, "%s() interrupted while waiting for geocoding completion", method);
                Thread.currentThread().interrupt();
            }
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
        final Set<GWTMonitoredService> s = new TreeSet<GWTMonitoredService>();
        final Set<String> locations = new TreeSet<String>();
        for (final OnmsMonitoredService service : services) {
            for (final OnmsApplication oa : service.getApplications()) {
                locations.add(oa.getName());
            }
            s.add(transformMonitoredService(service));
        }
        app.setServices(s);
        app.setLocations(locations);
        return app;
    }

    private static interface StatusTracker {
        public void onStatus(final OnmsLocationSpecificStatus status);
    }

    private static class MonitorStatusTracker implements StatusTracker {
        private transient final Map<Integer, OnmsLocationSpecificStatus> m_statuses = new HashMap<Integer, OnmsLocationSpecificStatus>();

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
            synchronized (m_statuses) {
                for (OnmsLocationSpecificStatus status : m_statuses.values()) {
                    statuses.add(transformLocationSpecificStatus(status));
                }
                m_statuses.clear();
            }
            return statuses;
        }
    }

    private static class ApplicationStatusTracker implements StatusTracker {
        private String m_name;

        private Map<String, Collection<OnmsLocationSpecificStatus>> m_statuses = new HashMap<String, Collection<OnmsLocationSpecificStatus>>();

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

        public Map<String, List<GWTLocationSpecificStatus>> drainStatuses() {
            final Map<String, List<GWTLocationSpecificStatus>> statuses = new HashMap<String, List<GWTLocationSpecificStatus>>();
            synchronized (m_statuses) {
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
