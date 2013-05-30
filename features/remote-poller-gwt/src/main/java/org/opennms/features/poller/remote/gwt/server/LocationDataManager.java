/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;

import de.novanic.eventservice.service.EventExecutorService;

/**
 * <p>LocationDataManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationDataManager { //implements LocationStatusService {
    private LocationDataService m_locationDataService;
    private Set<String> m_activeApplications = new HashSet<String>();
    private Timer m_timer = new Timer();
    /** Constant <code>PADDING_TIME=2000</code> */
    public static final int PADDING_TIME = 2000;
    
    /**
     * <p>setLocationDataService</p>
     *
     * @param locationDataService a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
     */
    public void setLocationDataService(final LocationDataService locationDataService) {
        m_locationDataService = locationDataService;
    }

    /**
     * <p>getLocationDataService</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.server.LocationDataService} object.
     */
    public LocationDataService getLocationDataService() {
        return m_locationDataService;
    }

    /**
     * <p>getLocationInfo</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    public LocationInfo getLocationInfo(final String locationName) {
        return getLocationDataService().getLocationInfo(locationName);
    }

    /**
     * <p>getLocationDetails</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     */
    public LocationDetails getLocationDetails(final String locationName) {
        return getLocationDataService().getLocationDetails(locationName);
    }

    /**
     * <p>getApplicationInfo</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public ApplicationInfo getApplicationInfo(final String applicationName) {
        return getLocationDataService().getApplicationInfo(applicationName);
    }

    /**
     * <p>getApplicationDetails</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetails getApplicationDetails(final String applicationName) {
        return getLocationDataService().getApplicationDetails(applicationName);
    }

    /**
     * <p>setActiveApplications</p>
     *
     * @param activeApplications a {@link java.util.Set} object.
     */
    public void setActiveApplications(final Set<String> activeApplications) {
        synchronized(m_activeApplications) {
            if (m_activeApplications == activeApplications) return;
            m_activeApplications.clear();
            m_activeApplications.addAll(activeApplications);
        }
    }

    /**
     * <p>getTimer</p>
     *
     * @return a {@link java.util.Timer} object.
     */
    public Timer getTimer() {
        return m_timer;
    }
    
    /**
     * <p>setTimer</p>
     *
     * @param timer a {@link java.util.Timer} object.
     */
    public void setTimer(Timer timer) {
        m_timer = timer;
    }
    
    /**
     * <p>getActiveApplications</p>
     *
     * @return a {@link java.util.Set} object.
     */
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
        
        for (final LocationInfo locationInfo : locations) {
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
        service.addEvent(MapRemoteEventHandler.LOCATION_EVENT_DOMAIN, new LocationsUpdatedRemoteEvent(getLocationDataService().getUpdatedLocationsBetween(startDate, endDate)));
        LogUtils.debugf(this, "finished pushing monitor status updates");
    
        // Every 5 minutes, update the application list too
        LogUtils.debugf(this, "pushing application updates");
        final Collection<ApplicationHandler> appHandlers = new ArrayList<ApplicationHandler>();
        final DefaultApplicationHandler applicationHandler = new DefaultApplicationHandler(getLocationDataService(), service, getActiveApplications());
        appHandlers.add(applicationHandler);
        getLocationDataService().handleAllApplications(appHandlers);
        setActiveApplications(applicationHandler.getApplicationNames());
        LogUtils.debugf(this, "finished pushing application updates");
    }

    void start(final EventExecutorService service) {
        getTimer().schedule(new InitializeTask(service, this, getTimer()), LocationDataManager.PADDING_TIME);
    }

}
