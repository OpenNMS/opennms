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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApplicationHandler.class);
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
    @Override
    public void start(final int size) {
    }

    /**
     * <p>handle</p>
     *
     * @param application a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    @Override
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
    @Override
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
        LOG.debug("sending event: {}", event);
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
