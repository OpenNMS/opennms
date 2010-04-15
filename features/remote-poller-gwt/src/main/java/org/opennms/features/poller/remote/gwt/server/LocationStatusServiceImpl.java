package org.opennms.features.poller.remote.gwt.server;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.BaseLocation;
import org.opennms.features.poller.remote.gwt.client.Location;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.features.poller.remote.gwt.server.geocoding.Geocoder;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class LocationStatusServiceImpl extends RemoteEventServiceServlet implements LocationStatusService {
	private static final long serialVersionUID = 1L;
	private static final int UPDATE_PERIOD = 1000 * 60; // 1 minute
	private static final int MAX_LOCATIONS_PER_EVENT = 50;

	private WebApplicationContext m_context;

	private volatile Geocoder m_geocoder;
	private static AtomicInteger m_inProgress = new AtomicInteger(0);

	private static volatile Timer myLocationTimer;
	private static volatile Date lastUpdated;
	private static volatile LocationDataService m_locationDataService;
	private static volatile String m_apiKey = null;

	private void initializeContext() {
		if (m_context == null) {
			LogUtils.infof(this, "initializing context");
			m_context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		}
	}

	private void initializeDaos() {
		if (m_locationDataService == null) {
			LogUtils.infof(this, "initializing location data service");
			m_locationDataService = m_context.getBean(LocationDataService.class);
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

	private class InitialSenderTimerTask extends TimerTask {

		@Override
		public void run() {
			m_inProgress.incrementAndGet();
			LogUtils.debugf(this, "pushing initial data");
			lastUpdated = new Date();

			final Collection<LocationInfo> locations = new ArrayList<LocationInfo>();
			final Collection<OnmsMonitoringLocationDefinition> definitions = m_locationDataService.findAllMonitoringLocationDefinitions();
			for (OnmsMonitoringLocationDefinition def : definitions) {
				final LocationInfo location = m_locationDataService.getLocationInfo(def);
				locations.add(location);
				LogUtils.debugf(this, "pushing location: %s", def.getName());
//				addEventUserSpecific(getLocation(def));
//				addEvent(Location.LOCATION_EVENT_DOMAIN, getLocation(def));
				if (locations.size() >= MAX_LOCATIONS_PER_EVENT) {
					addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(new ArrayList<LocationInfo>(locations)));
					locations.clear();
				}
			}
			if (locations.size() > 0) {
				addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(locations));
			}
			m_locationDataService.saveMonitoringLocationDefinitions(definitions);
			addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new UpdateCompleteRemoteEvent());
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

			addEvent(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(m_locationDataService.getUpdatedLocationsBetween(startDate, endDate)));

			lastUpdated = endDate;
			m_inProgress.decrementAndGet();
		}
	}

}