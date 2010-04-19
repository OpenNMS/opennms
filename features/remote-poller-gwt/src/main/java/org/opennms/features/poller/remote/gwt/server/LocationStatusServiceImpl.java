package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.BaseLocation;
import org.opennms.features.poller.remote.gwt.client.Location;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
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
	private WebApplicationContext m_context;

	private static volatile Timer m_timer;
	private static volatile Date m_lastUpdated;
	private static volatile LocationDataService m_locationDataService;
	private static volatile AtomicBoolean m_initializationComplete;

	private void initialize() {
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
	}

	public void start() {
		LogUtils.debugf(this, "starting location status service");
		initialize();
		final EventExecutorService service = EventExecutorServiceFactory.getInstance().getEventExecutorService(this.getRequest().getSession());

		if (m_timer == null) {
			m_timer = new Timer();
			m_timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!m_initializationComplete.get()) {
						return;
					}
					if (m_lastUpdated == null) {
						return;
					}
					LogUtils.debugf(this, "checking for monitor status updates");
					final Date endDate = new Date();
					addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(m_locationDataService.getUpdatedLocationsBetween(m_lastUpdated, endDate)));
					m_lastUpdated = endDate;
				}
			}, UPDATE_PERIOD, UPDATE_PERIOD);
		}

		final TimerTask initializedTask = new TimerTask() {
			@Override
			public void run() {
				pushInitializedLocations(service);
				m_lastUpdated = new Date();
				m_initializationComplete.set(true);
			}
		};
		final TimerTask uninitializedTask = new TimerTask() {
			@Override
			public void run() {
				pushUninitializedLocations(service);
				service.addEventUserSpecific(new UpdateCompleteRemoteEvent());
				m_timer.schedule(initializedTask, PADDING_TIME);
			}
		};

		m_timer.schedule(uninitializedTask, PADDING_TIME);
	}

	public Location getLocation(final String locationName) {
		initialize();
		final BaseLocation l = new BaseLocation();
		l.setLocationInfo(m_locationDataService.getLocationInfo(locationName));
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

	private void pushUninitializedLocations(final EventExecutorService service) {
		final Collection<LocationDefHandler> handlers = new ArrayList<LocationDefHandler>();
		handlers.add(new InitialLocationDefHandler(m_locationDataService, service, false));
		handlers.add(new GeocodingHandler(m_locationDataService, service));
		m_locationDataService.handleAllMonitoringLocationDefinitions(handlers);
	}

	private void pushInitializedLocations(final EventExecutorService service) {
		final LocationDefHandler locationHandler = new InitialLocationDefHandler(m_locationDataService, service, true);
		m_locationDataService.handleAllMonitoringLocationDefinitions(Collections.singleton(locationHandler));
	}

}
