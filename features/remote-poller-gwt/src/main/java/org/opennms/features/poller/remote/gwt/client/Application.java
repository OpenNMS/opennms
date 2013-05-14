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

package org.opennms.features.poller.remote.gwt.client;


import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.event.shared.HandlerManager;

import de.novanic.eventservice.client.event.RemoteEventService;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Application implements LocationsUpdatedEventHandler {

    private LocationManager m_locationManager;
    private final HandlerManager m_eventBus;

    ApplicationView m_view;

    

    public Application(HandlerManager eventBus) {
        m_eventBus = eventBus;
    }

    public void initialize(ApplicationView view, LocationStatusServiceAsync remoteService, RemoteEventService remoteEventService, CommandExecutor executor) {
        // Register for all relevant events thrown by the UI components
        getEventBus().addHandler(LocationsUpdatedEvent.TYPE, this);
        
        // Log.setUncaughtExceptionHandler();
        m_view = view;
        
        m_locationManager = new DefaultLocationManager(getEventBus(), m_view, remoteService, remoteEventService, executor);
        
        m_view.initialize();
        
    }

    public void onApplicationViewSelected() {
        m_locationManager.applicationClicked();
    }
    
    public void onLocationViewSelected() {
        m_locationManager.locationClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void onLocationsUpdated(LocationsUpdatedEvent e) {
        m_view.updateTimestamp();
    }
    
    private HandlerManager getEventBus() {
        return m_eventBus;
    }
}
