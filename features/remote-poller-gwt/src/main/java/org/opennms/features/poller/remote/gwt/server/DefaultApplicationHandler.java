package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationRemovedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;
import org.opennms.netmgt.model.OnmsApplication;

import de.novanic.eventservice.service.EventExecutorService;

public class DefaultApplicationHandler implements ApplicationHandler {
    private LocationDataService m_locationDataService;

    private EventExecutorService m_eventService;

    private boolean m_includeStatus = false;

    private Set<String> m_oldApplicationNames = null;
    private Set<String> m_foundApplicationNames = new HashSet<String>();

    public DefaultApplicationHandler() {}

    public DefaultApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService, final boolean includeStatus) {
        setLocationDataService(locationDataService);
        m_eventService = eventService;
        m_includeStatus = includeStatus;
    }

    public DefaultApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService, final boolean includeStatus, final Collection<String> currentApplications) {
        this(locationDataService, eventService, includeStatus);
        if (currentApplications != null) {
            m_oldApplicationNames = new HashSet<String>(currentApplications);
        }
    }

    public void start(final int size) {
    }

    public void handle(final OnmsApplication application) {
        final ApplicationInfo applicationInfo = getLocationDataService().getApplicationInfo(application, m_includeStatus);
        final ApplicationUpdatedRemoteEvent event = new ApplicationUpdatedRemoteEvent(applicationInfo);
        sendEvent(event);
        if (m_oldApplicationNames != null) {
            m_oldApplicationNames.remove(application.getName());
        }
        m_foundApplicationNames.add(application.getName());
    }

    public void finish() {
        if (m_oldApplicationNames != null) {
            for (final String appName : m_oldApplicationNames) {
                sendEvent(new ApplicationRemovedRemoteEvent(appName));
            }
        }
    }

    public Set<String> getApplicationNames() {
        return m_foundApplicationNames;
    }

    protected void sendEvent(final MapRemoteEvent event) {
        LogUtils.debugf(this, "sending event: %s", event);
        getEventService().addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, event);
    }

    protected EventExecutorService getEventService() {
        return m_eventService;
    }

    /**
     * @param locationDataService the locationDataService to set
     */
    public void setLocationDataService(LocationDataService locationDataService) {
        m_locationDataService = locationDataService;
    }

    /**
     * @return the locationDataService
     */
    public LocationDataService getLocationDataService() {
        return m_locationDataService;
    }

}
