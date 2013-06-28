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

/**
 * 
 */
package org.opennms.features.poller.remote.gwt.server;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.novanic.eventservice.service.EventExecutorService;

final class UpdateTask extends TimerTask {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateTask.class);
    private final EventExecutorService m_service;
    private final LocationDataManager m_locationDataManager;
    private Date m_lastUpdated;

    /**
     * <p>Constructor for UpdateTask.</p>
     *
     * @param service a {@link de.novanic.eventservice.service.EventExecutorService} object.
     * @param lastUpdated a {@link java.util.Date} object.
     * @param locationDataManager a {@link org.opennms.features.poller.remote.gwt.server.LocationDataManager} object.
     */
    public UpdateTask(final EventExecutorService service, final Date lastUpdated, LocationDataManager locationDataManager) {
        m_service = service;
        m_lastUpdated = lastUpdated;
        m_locationDataManager = locationDataManager;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            final Date endDate = new Date();
            m_locationDataManager.doUpdate(m_lastUpdated, endDate, m_service);
    		m_lastUpdated = endDate;
    	} catch (final Exception e) {
    		LOG.warn("An error occurred while pushing monitor and application status updates.", e);
    	}
    }
}
