package org.opennms.features.poller.remote.gwt.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.novanic.eventservice.service.EventExecutorServiceFactory;
import de.novanic.eventservice.service.RemoteEventServiceServlet;

/**
 * <p>LocationStatusServiceImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationStatusServiceImpl extends RemoteEventServiceServlet implements LocationStatusService {
    private static final long serialVersionUID = 1L;

    volatile Set<String> m_activeApplications = new HashSet<String>();

    private ApplicationContext m_context;
    private LocationBroadcastProcessor m_locationBroadcastProcessor;
    private LocationDataManager m_locationDataManager;

    private void initialize() {
        Logger.getLogger("com.google.gwt.user.client.rpc").setLevel(Level.TRACE);

        if (m_context == null) {
            LogUtils.infof(this, "initializing context");
            m_context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        }

        if (m_locationDataManager == null) {
            LogUtils.infof(this, "initializing location data manager");
            m_locationDataManager = m_context.getBean(LocationDataManager.class);
        }

        // Don't do event handling since we update location and app status on a schedule
//        if (m_locationBroadcastProcessor == null) {
//            m_locationBroadcastProcessor = m_context.getBean(LocationBroadcastProcessor.class);
//            m_locationBroadcastProcessor.setEventHandler(new LocationEventHandler() {
//                public void sendEvent(final MapRemoteEvent event) {
//                    addEvent(MapRemoteEventHandler.LOCATION_EVENT_DOMAIN, event);
//                }
//            });
//        }
    }

    /**
     * <p>start</p>
     */
    public void start() {
        LogUtils.debugf(this, "starting location status service");
        initialize();
        m_locationDataManager.start(EventExecutorServiceFactory.getInstance().getEventExecutorService(this.getRequest().getSession()));
    }

    /** {@inheritDoc} */
    public LocationInfo getLocationInfo(final String locationName) {
        return m_locationDataManager.getLocationInfo(locationName);
    }

    /** {@inheritDoc} */
    public LocationDetails getLocationDetails(final String locationName) {
        return m_locationDataManager.getLocationDetails(locationName);
    }

    /** {@inheritDoc} */
    public ApplicationInfo getApplicationInfo(final String applicationName) {
        return m_locationDataManager.getApplicationInfo(applicationName);
    }

    /** {@inheritDoc} */
    public ApplicationDetails getApplicationDetails(final String applicationName) {
        return m_locationDataManager.getApplicationDetails(applicationName);
    }

}
