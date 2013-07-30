/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.osgi;

import java.util.List;

public class EventConsumerScopeImpl implements EventConsumerScope {

    private final OnmsServiceManager serviceManager;
    private final VaadinApplicationContext applicationContext;

    protected EventConsumerScopeImpl(OnmsServiceManager serviceManager, VaadinApplicationContext applicationContext) {
        this.serviceManager = serviceManager;
        this.applicationContext = applicationContext;
    }

    /**
     * Fires an event and notifies all {@link EventListener} registered in the EventRegistry.
     * Be aware that only {@linkplain EventListener}s within session-scope and those listeners who 
     * listens to events of type T gets notified.
     * 
     * @see {@link EventListener}
     */
    @Override
    public <T> void fireEvent(T eventObject) {
        if (eventObject == null) return;
        List<EventListener> eventListeners = serviceManager.getServices(EventListener.class, applicationContext, EventListener.getProperties(eventObject.getClass()));
        for (EventListener eachListener : eventListeners) {
            eachListener.invoke(eventObject);
        }
    }

    @Override
    public <T> void addPossibleEventConsumer(T possibleEventConsumer) {
        serviceManager.getEventRegistry().addPossibleEventConsumer(possibleEventConsumer, applicationContext);
    }
}
