package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.AppStatusDetailsComputer;
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


/**
 * <p>DefaultLocationDataService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultLocationDataService implements LocationDataService, InitializingBean {
    /**
     * MonitorTracker
     *
     * @author brozow
     */
    public class MonitorTracker {
        
        Map<String, List<OnmsLocationMonitor>> m_monitors = new HashMap<String, List<OnmsLocationMonitor>>();
        
        public void onMonitor(OnmsLocationMonitor locationMon) {
            List<OnmsLocationMonitor> monitors = getMonitorList(locationMon.getDefinitionName());
            monitors.add(locationMon);
        }
        
        private List<OnmsLocationMonitor> getMonitorList(String definitionName) {
            List<OnmsLocationMonitor> monitors = m_monitors.get(definitionName);
            if (monitors == null) {
                monitors = new ArrayList<OnmsLocationMonitor>();
                m_monitors.put(definitionName, monitors);
            }
            return monitors;
        }

        public Collection<GWTLocationMonitor> drain(String defName) {
            final Collection<GWTLocationMonitor> gwtMonitors = new ArrayList<GWTLocationMonitor>();
            if (!m_monitors.containsKey(defName)) return gwtMonitors;
            
            List<OnmsLocationMonitor> monitors = m_monitors.get(defName);
            
            for (OnmsLocationMonitor monitor : monitors) {
                gwtMonitors.add(transformLocationMonitor(monitor));
            }
            
            return gwtMonitors;
        }

    }

    private static final int AVAILABILITY_MS = 1000 * 60 * 60 * 24; // 1 day

    @Autowired
    private LocationMonitorDao m_locationDao;

    @Autowired
    private ApplicationDao m_applicationDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private Geocoder m_geocoder;

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(final LocationMonitorDao dao) {
        m_locationDao = dao;
    }

    /**
     * <p>setApplicationDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.ApplicationDao} object.
     */
    public void setApplicationDao(final ApplicationDao dao) {
        m_applicationDao = dao;
    }

    /**
     * <p>setMonitoredServiceDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.MonitoredServiceDao} object.
     */
    public void setMonitoredServiceDao(final MonitoredServiceDao dao) {
        m_monitoredServiceDao = dao;
    }

    /**
     * <p>setGeocoder</p>
     *
     * @param geocoder a {@link org.opennms.features.poller.remote.gwt.server.geocoding.Geocoder} object.
     */
    public void setGeocoder(final Geocoder geocoder) {
        m_geocoder = geocoder;
    }

    private CountDownLatch m_initializationLatch = new CountDownLatch(1);

    private volatile Map<String, MonitorStatus> m_monitorStatuses = new HashMap<String, MonitorStatus>();

    // whether to save monitoring-locations.xml changes
    public boolean m_save = true;

    /**
     * <p>Constructor for DefaultLocationDataService.</p>
     */
    public DefaultLocationDataService() {
    }

    /**
     * <p>setSave</p>
     *
     * @param save a boolean.
     */
    public void setSave(final boolean save) {
        m_save = save;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.notNull(m_locationDao);
        Assert.notNull(m_applicationDao);
        Assert.notNull(m_monitoredServiceDao);
        Assert.notNull(m_geocoder);

        initialize();
    }

    /**
     * <p>initialize</p>
     */
    public void initialize() {
        new InitializationThread().start();
    }

    private class InitializationThread extends Thread {
        public InitializationThread() {
        }

        public void run() {
            updateGeolocations();
        }
    }

    /** {@inheritDoc} */
    @Transactional
    public LocationInfo getLocationInfo(final String locationName) {
        waitForGeocoding("getLocationInfo");

        final OnmsMonitoringLocationDefinition def = m_locationDao.findMonitoringLocationDefinition(locationName);
        if (def == null) {
            LogUtils.warnf(this, "no monitoring location found for name %s", locationName);
            return null;
        }
        return getLocationInfo(def);
    }

    /**
     * <p>getLocationInfo</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    @Transactional
    public LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def) {
        waitForGeocoding("getLocationInfo");
        
        if (def == null) {
            LogUtils.warnf(this, "no location definition specified");
            return null;
        }
        
        final StatusDetails monitorStatus = getStatusDetailsForLocation(def);
        
        return getLocationInfo(def, monitorStatus);
    }

    private LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def, final StatusDetails monitorStatus) {
        GWTLatLng latLng = getLatLng(def, false);
        
        if (latLng == null) {
            LogUtils.debugf(this, "no geolocation or coordinates found, using OpenNMS World HQ");
            latLng = new GWTLatLng(35.715751, -79.16262);
        }
        
        final GWTMarkerState state = new GWTMarkerState(def.getName(), latLng, Status.UNINITIALIZED);
        final LocationInfo locationInfo = new LocationInfo(
            def.getName(),
            def.getArea(),
            def.getGeolocation(),
            latLng.getCoordinates(),
            def.getPriority(),
            state,
            null,
            def.getTags()
        );
        
        state.setStatus(monitorStatus.getStatus());
        locationInfo.setStatusDetails(monitorStatus);
        
        //LogUtils.debugf(this, "getLocationInfo(%s) returning %s", def.getName(), locationInfo.toString());
        return locationInfo;
    }

    /** {@inheritDoc} */
    @Transactional
    public StatusDetails getStatusDetailsForLocation(final OnmsMonitoringLocationDefinition def) {
        waitForGeocoding("getStatusDetails");
        
        final DefaultLocationDataService.MonitorStatusTracker mst = new DefaultLocationDataService.MonitorStatusTracker(def.getName());

        final List<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();

        for (OnmsLocationMonitor mon : m_locationDao.findByLocationDefinition(def)) {
            monitors.add(transformLocationMonitor(mon));
        }

        for (OnmsLocationSpecificStatus status : m_locationDao.getMostRecentStatusChangesForLocation(def.getName())) {
            mst.onStatus(status);
        }

        LocationMonitorState monitorState = new LocationMonitorState(monitors, mst.drain());
        StatusDetails statusDetails = monitorState.getStatusDetails();
        LogUtils.debugf(this, "getStatusDetails(%s) returning %s", def.getName(), statusDetails);
        return statusDetails;
    }

    /** {@inheritDoc} */
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

        return getLocationInfo(def);
    }

    /** {@inheritDoc} */
    @Transactional
    public ApplicationInfo getApplicationInfo(final String applicationName) {
        waitForGeocoding("getApplicationInfo");

        final OnmsApplication app = m_applicationDao.findByName(applicationName);
        if (app == null) {
            LogUtils.warnf(this, "no application found with name '%s'", applicationName);
        }
        return getApplicationInfo(app);
    }

    /**
     * <p>getApplicationInfo</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    @Transactional
    public ApplicationInfo getApplicationInfo(final OnmsApplication app) {
        waitForGeocoding("getApplicationInfo");
        
        ApplicationInfo info = null;
        if (app == null) {
            LogUtils.warnf(this, "no application specified");
        } else {
            info = getApplicationInfo(app, getStatusDetailsForApplication(app));
        }
        
        return info;
    }

    /** {@inheritDoc} */
    @Transactional
    public StatusDetails getStatusDetailsForApplication(final OnmsApplication app) {
        waitForGeocoding("getStatusDetailsForApplication");
        
        List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
        
        final Date to = new Date();
        final Date from = new Date(to.getTime() - AVAILABILITY_MS);
        
        final Collection<OnmsMonitoredService> services = m_monitoredServiceDao.findByApplication(app);
        final Set<GWTLocationMonitor> monitors = new LinkedHashSet<GWTLocationMonitor>();
        final Set<GWTMonitoredService> gwtServices = new LinkedHashSet<GWTMonitoredService>(services.size());
        
        for (final OnmsMonitoredService service : services) {
            gwtServices.add(transformMonitoredService(service));
        }
        
        for (OnmsLocationSpecificStatus status : m_locationDao.getStatusChangesForApplicationBetween(to, from, app.getName())) {
            monitors.add(transformLocationMonitor(status.getLocationMonitor()));
            statuses.add(transformLocationSpecificStatus(status));
        }
        
        StatusDetails statusDetails = new AppStatusDetailsComputer(from, to, monitors, gwtServices, statuses).compute();
        LogUtils.warnf(this, "getStatusDetailsForApplication(%s) returning %s", app.getName(), statusDetails);
        return statusDetails;
    }


    /**
     * <p>getStatusDetailsForApplicationOld</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    @Transactional
    public StatusDetails getStatusDetailsForApplicationOld(final OnmsApplication app) {
        waitForGeocoding("getStatusDetailsForApplication");
        
        List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
        
        final Date to = new Date();
        final Date from = new Date(to.getTime() - AVAILABILITY_MS);
        
        final Collection<OnmsMonitoredService> services = m_monitoredServiceDao.findByApplication(app);
        final List <GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();
        final Set<GWTMonitoredService> gwtServices = new LinkedHashSet<GWTMonitoredService>(services.size());
        
        for (final OnmsMonitoredService service : services) {
            gwtServices.add(transformMonitoredService(service));
        }
        
        for (final OnmsLocationMonitor monitor : m_locationDao.findByApplication(app)) {
            monitors.add(transformLocationMonitor(monitor));
            for (final OnmsLocationSpecificStatus locationSpecificStatus : m_locationDao.getStatusChangesForLocationBetween(from, to, monitor.getDefinitionName())) {
                if (services.contains(locationSpecificStatus.getMonitoredService())) {
                    statuses.add(transformLocationSpecificStatus(locationSpecificStatus));
                }
            }
        }
        
        StatusDetails statusDetails = new AppStatusDetailsComputer(from, to, monitors, gwtServices, statuses).compute();
        LogUtils.warnf(this, "getStatusDetailsForApplication(%s) returning %s", app.getName(), statusDetails);
        return statusDetails;
    }

    /** {@inheritDoc} */
    @Transactional 
    public ApplicationInfo getApplicationInfo(final OnmsApplication app, final StatusDetails status) {
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
        
        LogUtils.debugf(this, "getApplicationInfo(%s) returning %s", app.getName(), applicationInfo.toString());
        return applicationInfo;
    }

    /** {@inheritDoc} */
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

    /**
     * <p>getLocationDetails</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     */
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
//        final Map<String, Set<OnmsMonitoredService>> services = new HashMap<String, Set<OnmsMonitoredService>>();

        for (final OnmsApplication application : m_applicationDao.findAll()) {
            applications.add(transformApplication(m_monitoredServiceDao.findByApplication(application), application));
        }

//        for (final OnmsMonitoredService service : m_monitoredServiceDao.findAll()) {
//            for (final OnmsApplication app : service.getApplications()) {
//                final String appName = app.getName();
//                Set<OnmsMonitoredService> serv = services.get(appName);
//                if (serv == null) {
//                    serv = new HashSet<OnmsMonitoredService>();
//                    services.put(appName, serv);
//                }
//                serv.add(service);
//            }
//        }

        final Date to = new Date();
        final Date from = new Date(to.getTime() - AVAILABILITY_MS);
        for (OnmsLocationSpecificStatus status : m_locationDao.getMostRecentStatusChangesForLocation(def.getName())) {
            mst.onStatus(status);
            ast.onStatus(status);
        }

        ld.setLocationMonitorState(new LocationMonitorState(monitors, mst.drain()));
        ld.setApplicationState(new ApplicationState(from, to, applications, monitors, ast.drainStatuses()));
        LogUtils.debugf(this, "getLocationDetails(%s) returning %s", def.getName(), ld);
        return ld;
    }

    /** {@inheritDoc} */
    @Transactional
    public ApplicationDetails getApplicationDetails(final String applicationName) {
        waitForGeocoding("getApplicationDetails");

        final OnmsApplication app = m_applicationDao.findByName(applicationName);
        return getApplicationDetails(app);
    }

    /**
     * <p>getApplicationDetails</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    @Transactional
    public ApplicationDetails getApplicationDetails(final OnmsApplication app) {
        waitForGeocoding("getApplicationDetails");

        final ApplicationInfo applicationInfo = getApplicationInfo(app, StatusDetails.uninitialized());
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

    /** {@inheritDoc} */
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
            final LocationInfo location = getLocationInfo(def);
            locations.add(location);
        }

        return locations;
    }

    /** {@inheritDoc} */
    @Transactional
    public GWTLatLng getLatLng(final OnmsMonitoringLocationDefinition def, boolean x) {
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

    /** {@inheritDoc} */
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
                /*
                 * final LocationUpdatedRemoteEvent event = new LocationUpdatedRemoteEvent(m_locationDataService.getLocationInfo(def, m_includeStatus));
                 * getEventService().addEventUserSpecific(event);
                 */
            }
        }
        for (final LocationDefHandler handler : handlers) {
            handler.finish();
        }
        m_locationDao.saveMonitoringLocationDefinitions(definitions);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Transactional
    public Collection<ApplicationInfo> getApplicationsForLocation(final LocationInfo locationInfo) {
        waitForGeocoding("getApplicationsForLocation");

        final Map<String,ApplicationInfo> apps = new HashMap<String,ApplicationInfo>();
        for (final OnmsLocationSpecificStatus status : m_locationDao.getMostRecentStatusChangesForLocation(locationInfo.getName())) {
            for (final OnmsApplication app : status.getMonitoredService().getApplications()) {
                if (!apps.containsKey(app.getName())) {
                    apps.put(app.getName(), getApplicationInfo(app));
                }
            }
        }
        return apps.values();
    }

    void waitForGeocoding(final String method) {
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

    /**
     * <p>updateGeolocations</p>
     */
    public void updateGeolocations() {
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

        updateGeolocationsComplete();
    }

    /**
     * <p>updateGeolocationsComplete</p>
     */
    public void updateGeolocationsComplete() {
        m_initializationLatch.countDown();
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
    
    private static class AllMonitorStatusTracker implements StatusTracker {
        private final Map<String, MonitorStatusTracker> m_trackers = new HashMap<String, MonitorStatusTracker>();
        
        public void onStatus(final OnmsLocationSpecificStatus status) {
            String defName = status.getLocationMonitor().getDefinitionName();
            
            MonitorStatusTracker t = getMonitorStatusTracker(defName);
            t.onStatus(status);
        }
        
        MonitorStatusTracker getMonitorStatusTracker(String defName) {
            MonitorStatusTracker t = m_trackers.get(defName);
            if (t == null) {
                t = new MonitorStatusTracker(defName);
                m_trackers.put(defName, t);
            }
            return t;
        }
        
        public Collection<GWTLocationSpecificStatus> drain(String defName) {
            if (!m_trackers.containsKey(defName)) {
                return Collections.emptyList();
            }
            
            return m_trackers.get(defName).drain();

        }
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

    /**
     * <p>getLocationMonitorDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public LocationMonitorDao getLocationMonitorDao() {
             return m_locationDao;
    }

    /**
     * <p>getInfoForAllLocations</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Transactional
    public List<LocationInfo> getInfoForAllLocations() {
        waitForGeocoding("getInfoForAllLocations");
        
        Map<String, StatusDetails> statusDetails = getStatusDetailsForAllLocations();
        
        List<LocationInfo> locations = new ArrayList<LocationInfo>();

        for(Map.Entry<String, StatusDetails> entry : statusDetails.entrySet()) {

            OnmsMonitoringLocationDefinition def = getLocationMonitorDao().findMonitoringLocationDefinition(entry.getKey());
            LocationInfo locationInfo = this.getLocationInfo(def, entry.getValue());
            locations.add(locationInfo);
            
        }
        
        return locations;
    }

    private Map<String, StatusDetails> getStatusDetailsForAllLocations() {
        final Collection<OnmsMonitoringLocationDefinition> definitions = getLocationMonitorDao().findAllMonitoringLocationDefinitions();
        
        AllMonitorStatusTracker tracker = new AllMonitorStatusTracker();
        MonitorTracker monTracker = new MonitorTracker();
        
        for (OnmsLocationSpecificStatus status : m_locationDao.getAllMostRecentStatusChanges()) {
            tracker.onStatus(status);
        }
        
        for(OnmsLocationMonitor monitor : m_locationDao.findAll()) {
            monTracker.onMonitor(monitor);
        }
        
        
        Map<String, StatusDetails> statusDetails = new LinkedHashMap<String, StatusDetails>();
        for (final OnmsMonitoringLocationDefinition def : definitions) {
            LocationMonitorState monitorState = new LocationMonitorState(monTracker.drain(def.getName()), tracker.drain(def.getName()));
            final StatusDetails monitorStatus = monitorState.getStatusDetails();
            statusDetails.put(def.getName(), monitorStatus);
        }
        return statusDetails;
    }

    /**
     * <p>getInfoForAllApplications</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Transactional
    public List<ApplicationInfo> getInfoForAllApplications() {
        waitForGeocoding("handleAllApplications");

        Map<OnmsApplication, StatusDetails> statusDetails = getStatusDetailsForAllApplications();

        final List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
        for (Map.Entry<OnmsApplication, StatusDetails> entry : statusDetails.entrySet()) {
            final ApplicationInfo appInfo = getApplicationInfo(entry.getKey(), entry.getValue());
            appInfos.add(appInfo);
        }
        return appInfos;
    }

    private Map<OnmsApplication, StatusDetails> getStatusDetailsForAllApplications() {
        final Collection<OnmsApplication> apps = m_applicationDao.findAll();
        
        Map<OnmsApplication, StatusDetails> statusDetails = new LinkedHashMap<OnmsApplication, StatusDetails>();

        for (final OnmsApplication app : apps) {
            StatusDetails appStatusDetails = getStatusDetailsForApplication(app);
            statusDetails.put(app, appStatusDetails);
        }
        return statusDetails;
    }

}
