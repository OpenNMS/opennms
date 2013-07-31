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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.LocationStatusService;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
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
    private static final Logger LOG = LoggerFactory.getLogger(LocationStatusServiceImpl.class);

	private static final long serialVersionUID = 7152723329766982720L;

	volatile Set<String> m_activeApplications = new HashSet<String>();

    private ApplicationContext m_context;
    @SuppressWarnings("unused")
    private LocationBroadcastProcessor m_locationBroadcastProcessor;
    private LocationDataManager m_locationDataManager;

    private void initialize() {
        if (m_context == null) {
            LOG.info("initializing context");
            m_context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        }

        if (m_locationDataManager == null) {
            LOG.info("initializing location data manager");
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
        @Override
    public void start() {
        LOG.debug("starting location status service");
        initialize();
        m_locationDataManager.start(EventExecutorServiceFactory.getInstance().getEventExecutorService(this.getRequest().getSession()));
    }

    /** {@inheritDoc} */
        @Override
    public LocationInfo getLocationInfo(final String locationName) {
        return m_locationDataManager.getLocationInfo(locationName);
    }

    /** {@inheritDoc} */
        @Override
    public LocationDetails getLocationDetails(final String locationName) {
        return m_locationDataManager.getLocationDetails(locationName);
    }

    /** {@inheritDoc} */
        @Override
    public ApplicationInfo getApplicationInfo(final String applicationName) {
        return m_locationDataManager.getApplicationInfo(applicationName);
    }

    /** {@inheritDoc} */
        @Override
    public ApplicationDetails getApplicationDetails(final String applicationName) {
        return m_locationDataManager.getApplicationDetails(applicationName);
    }

}
