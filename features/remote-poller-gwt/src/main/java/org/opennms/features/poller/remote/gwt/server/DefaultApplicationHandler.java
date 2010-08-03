package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationRemovedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;
import org.opennms.netmgt.model.OnmsApplication;

import de.novanic.eventservice.service.EventExecutorService;

/**
 * <p>DefaultApplicationHandler class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultApplicationHandler implements ApplicationHandler {
    private LocationDataService m_locationDataService;

    private EventExecutorService m_eventService;

    private Set<String> m_oldApplicationNames = null;
    private Set<String> m_foundApplicationNames = new HashSet<String>();

    /**
     * <p>Constructor for DefaultApplicationHandler.</p>
     */
    public DefaultApplicationHandler() {}

    /**
     * <p>Constructor for DefaultApplicationHandler.</p>
     *
     * @param locationDataService a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
     * @param eventService a {@link de.novanic.eventservice.service.EventExecutorService} object.
     */
    public DefaultApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService) {
        setLocationDataService(locationDataService);
        m_eventService = eventService;
    }

    /**
     * <p>Constructor for DefaultApplicationHandler.</p>
     *
     * @param locationDataService a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
     * @param eventService a {@link de.novanic.eventservice.service.EventExecutorService} object.
     * @param currentApplications a {@link java.util.Collection} object.
     */
    public DefaultApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService, final Collection<String> currentApplications) {
        this(locationDataService, eventService);
        if (currentApplications != null) {
            m_oldApplicationNames = new HashSet<String>(currentApplications);
        }
    }

    /** {@inheritDoc} */
    public void start(final int size) {
    }

    /**
     * <p>handle</p>
     *
     * @param application a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    public void handle(final OnmsApplication application) {
        final ApplicationInfo applicationInfo = getLocationDataService().getApplicationInfo(application);
        final ApplicationUpdatedRemoteEvent event = new ApplicationUpdatedRemoteEvent(applicationInfo);
        sendEvent(event);
        if (m_oldApplicationNames != null) {
            m_oldApplicationNames.remove(application.getName());
        }
        m_foundApplicationNames.add(application.getName());
    }

    /**
     * <p>finish</p>
     */
    public void finish() {
        if (m_oldApplicationNames != null) {
            for (final String appName : m_oldApplicationNames) {
                sendEvent(new ApplicationRemovedRemoteEvent(appName));
            }
        }
    }

    /**
     * <p>getApplicationNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getApplicationNames() {
        return m_foundApplicationNames;
    }

    /**
     * <p>sendEvent</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent} object.
     */
    protected void sendEvent(final MapRemoteEvent event) {
        LogUtils.debugf(this, "sending event: %s", event);
        getEventService().addEvent(MapRemoteEventHandler.LOCATION_EVENT_DOMAIN, event);
    }

    /**
     * <p>getEventService</p>
     *
     * @return a {@link de.novanic.eventservice.service.EventExecutorService} object.
     */
    protected EventExecutorService getEventService() {
        return m_eventService;
    }

    /**
     * <p>setLocationDataService</p>
     *
     * @param locationDataService the locationDataService to set
     */
    public void setLocationDataService(LocationDataService locationDataService) {
        m_locationDataService = locationDataService;
    }

    /**
     * <p>getLocationDataService</p>
     *
     * @return the locationDataService
     */
    public LocationDataService getLocationDataService() {
        return m_locationDataService;
    }

}
