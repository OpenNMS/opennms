package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.novanic.eventservice.service.EventExecutorService;
import de.novanic.eventservice.service.EventExecutorServiceFactory;
import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class LocationStatusServiceImpl extends RemoteEventServiceServlet implements LocationStatusService {
    private static final long serialVersionUID = 1L;

    private static final int UPDATE_PERIOD = 1000 * 60; // 1 minute
    private static final int PADDING_TIME = 2000;
    private static final int APPLICATION_UPDATE_OFFSET = 1; // how many update periods to wait before updating application data

    private static volatile Timer m_timer;
    private static volatile Date m_lastUpdated;
    private static volatile AtomicBoolean m_initializationComplete;

    private static volatile Set<String> m_activeApplications = new HashSet<String>();

    private static volatile WebApplicationContext m_context;
    private static volatile LocationDataService m_locationDataService;
    private static volatile LocationBroadcastProcessor m_locationBroadcastProcessor;

    private void initialize() {
        Logger.getLogger("com.google.gwt.user.client.rpc").setLevel(Level.TRACE);

        if (m_context == null) {
            LogUtils.infof(this, "initializing context");
            m_context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        }
        if (m_locationDataService == null) {
            LogUtils.infof(this, "initializing location data service");
            m_locationDataService = m_context.getBean(LocationDataService.class);
        }
        if (m_initializationComplete == null) {
            m_initializationComplete = new AtomicBoolean(false);
        }
        if (m_locationBroadcastProcessor == null) {
            m_locationBroadcastProcessor = m_context.getBean(LocationBroadcastProcessor.class);
            m_locationBroadcastProcessor.setEventHandler(new LocationEventHandler() {
                public void sendEvent(final MapRemoteEvent event) {
                    addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, event);
                }
            });
        }
    }

    public void start() {
        LogUtils.debugf(this, "starting location status service");
        initialize();
        final EventExecutorService service = EventExecutorServiceFactory.getInstance().getEventExecutorService(this.getRequest().getSession());

        if (m_timer == null) {
            m_timer = new Timer();
            m_timer.schedule(new TimerTask() {
                CountDownLatch m_latch = new CountDownLatch(APPLICATION_UPDATE_OFFSET);

                @Override
                public void run() {
                    if (!m_initializationComplete.get()) {
                        return;
                    }
                    if (m_lastUpdated == null) {
                        return;
                    }
                    LogUtils.debugf(this, "pushing monitor status updates");
                    final Date endDate = new Date();
                    addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(m_locationDataService.getUpdatedLocationsBetween(m_lastUpdated, endDate)));
                    LogUtils.debugf(this, "finished pushing monitor status updates");

                    // Every 5 minutes, update the application list too
                    synchronized(m_latch) {
                        m_latch.countDown();
                        if (m_latch.getCount() == 0) {
                            LogUtils.debugf(this, "pushing application updates");
                            final Collection<ApplicationHandler> appHandlers = new ArrayList<ApplicationHandler>();
                            final DefaultApplicationHandler applicationHandler = new DefaultApplicationHandler(m_locationDataService, service, true, m_activeApplications);
                            appHandlers.add(applicationHandler);
                            m_locationDataService.handleAllApplications(appHandlers);
                            synchronized(m_activeApplications) {
                                m_activeApplications.clear();
                                m_activeApplications.addAll(applicationHandler.getApplicationNames());
                            }
                            m_latch = new CountDownLatch(APPLICATION_UPDATE_OFFSET);
                            LogUtils.debugf(this, "finished pushing application updates");
                        }
                    }

                    m_lastUpdated = endDate;
                }
            }, UPDATE_PERIOD, UPDATE_PERIOD);
        }

        final TimerTask initializedTask = new TimerTask() {
            @Override
            public void run() {
                pushInitialData(service);
                service.addEventUserSpecific(new UpdateCompleteRemoteEvent());
                m_lastUpdated = new Date();
                m_initializationComplete.set(true);
            }
        };

        /*
         * final TimerTask uninitializedTask = new TimerTask() {
         * @Override public void run() { pushUninitializedLocations(service);
         * service.addEventUserSpecific(new UpdateCompleteRemoteEvent());
         * m_timer.schedule(initializedTask, PADDING_TIME); } };
         */

        m_timer.schedule(initializedTask, PADDING_TIME);
    }

    public LocationInfo getLocationInfo(final String locationName) {
        return m_locationDataService.getLocationInfo(locationName);
    }

    public LocationDetails getLocationDetails(final String locationName) {
        return m_locationDataService.getLocationDetails(locationName);
    }

    public ApplicationInfo getApplicationInfo(final String applicationName) {
        return m_locationDataService.getApplicationInfo(applicationName);
    }

    public ApplicationDetails getApplicationDetails(final String applicationName) {
        return m_locationDataService.getApplicationDetails(applicationName);
    }

    private void pushInitialData(final EventExecutorService service) {
        LogUtils.debugf(this, "pushing initialized locations");
        final LocationDefHandler locationHandler = new DefaultLocationDefHandler(m_locationDataService, service, true);
        m_locationDataService.handleAllMonitoringLocationDefinitions(Collections.singleton(locationHandler));
        LogUtils.debugf(this, "finished pushing initialized locations");

        LogUtils.debugf(this, "pushing initialized applications");
        final Collection<ApplicationHandler> appHandlers = new ArrayList<ApplicationHandler>();
        appHandlers.add(new UserSpecificApplicationHandler(m_locationDataService, service, true));
        m_locationDataService.handleAllApplications(appHandlers);
        LogUtils.debugf(this, "finished pushing initialized applications");
    }

}
