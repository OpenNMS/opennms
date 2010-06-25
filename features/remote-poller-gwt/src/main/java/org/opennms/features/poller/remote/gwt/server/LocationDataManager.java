package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;

import de.novanic.eventservice.service.EventExecutorService;

public class LocationDataManager { //implements LocationStatusService {
    private LocationDataService m_locationDataService;
    private Set<String> m_activeApplications;
    private Timer m_timer = new Timer();
    public static final int PADDING_TIME = 2000;
    
    public void setLocationDataService(final LocationDataService locationDataService) {
        m_locationDataService = locationDataService;
    }

    public LocationDataService getLocationDataService() {
        return m_locationDataService;
    }

    public LocationInfo getLocationInfo(final String locationName) {
        return getLocationDataService().getLocationInfo(locationName);
    }

    public LocationDetails getLocationDetails(final String locationName) {
        return getLocationDataService().getLocationDetails(locationName);
    }

    public ApplicationInfo getApplicationInfo(final String applicationName) {
        return getLocationDataService().getApplicationInfo(applicationName);
    }

    public ApplicationDetails getApplicationDetails(final String applicationName) {
        return getLocationDataService().getApplicationDetails(applicationName);
    }

    public void setActiveApplications(final Set<String> activeApplications) {
        synchronized(m_activeApplications) {
            m_activeApplications.clear();
            m_activeApplications.addAll(activeApplications);
        }
    }

    public Timer getTimer() {
        return m_timer;
    }
    
    public void setTimer(Timer timer) {
        m_timer = timer;
    }
    
    public Set<String> getActiveApplications() {
        return m_activeApplications;
    }

    private void pushApplicationData(final EventExecutorService service) {
        LogUtils.debugf(this, "pushing initialized applications");
        
        final List<ApplicationInfo> appInfos = getLocationDataService().getInfoForAllApplications();
        
        for (final ApplicationInfo appInfo : appInfos) {
            service.addEventUserSpecific(new ApplicationUpdatedRemoteEvent(appInfo));
        }
        
        LogUtils.debugf(this, "finished pushing initialized applications");
    }

    private void pushLocationData(final EventExecutorService service) {
        LogUtils.debugf(this, "pushing initialized locations");
        
        List<LocationInfo> locations = getLocationDataService().getInfoForAllLocations();
        
        for (final LocationInfo locationInfo : locations ) {
            final LocationUpdatedRemoteEvent event = new LocationUpdatedRemoteEvent(locationInfo);
            service.addEventUserSpecific(event);
        }
        
        LogUtils.debugf(this, "finished pushing initialized locations");
    }

    void doInitialize(EventExecutorService service) {
        pushLocationData(service);
        pushApplicationData(service);
        service.addEventUserSpecific(new UpdateCompleteRemoteEvent());
    }

    void doUpdate(final Date startDate, final Date endDate, final EventExecutorService service) {
        LogUtils.debugf(this, "pushing monitor status updates");
        service.addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(getLocationDataService().getUpdatedLocationsBetween(startDate, endDate)));
        LogUtils.debugf(this, "finished pushing monitor status updates");
    
        // Every 5 minutes, update the application list too
        LogUtils.debugf(this, "pushing application updates");
        final Collection<ApplicationHandler> appHandlers = new ArrayList<ApplicationHandler>();
        final DefaultApplicationHandler applicationHandler = new DefaultApplicationHandler(getLocationDataService(), service, true, getActiveApplications());
        appHandlers.add(applicationHandler);
        getLocationDataService().handleAllApplications(appHandlers);
        setActiveApplications(applicationHandler.getApplicationNames());
        LogUtils.debugf(this, "finished pushing application updates");
    }

    void start(final EventExecutorService service) {
        getTimer().schedule(new InitializeTask(service, this, getTimer()), LocationDataManager.PADDING_TIME);
    }

}
