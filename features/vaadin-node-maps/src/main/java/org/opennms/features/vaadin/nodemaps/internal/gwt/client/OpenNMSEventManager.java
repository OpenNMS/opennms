/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.OpenNMSEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.OpenNMSEvent.Type;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.OpenNMSEventHandler;

public class OpenNMSEventManager {
    private static final Logger LOG = Logger.getLogger(OpenNMSEventManager.class.getName());
    private Map<OpenNMSEvent.Type<? extends OpenNMSEventHandler>, Set<? extends OpenNMSEventHandler>> m_handlers = new HashMap<OpenNMSEvent.Type<? extends OpenNMSEventHandler>, Set<? extends OpenNMSEventHandler>>();

    public OpenNMSEventManager() {
    }

    @SuppressWarnings("unchecked")
    protected <T extends OpenNMSEventHandler> Set<T> getHandlersForEvent(final OpenNMSEvent<T> event) {
        return (Set<T>) m_handlers.get(event.getAssociatedType());
    }

    public <T extends OpenNMSEventHandler> void fireEvent(final OpenNMSEvent<T> event) {
        LOG.info("OpenNMSEventManager.fireEvent(" + event.toDebugString() + ")");
        final Set<T> handlers = getHandlersForEvent(event);
        if (handlers != null) {
            for (final T handler : handlers) {
                event.dispatch(handler);
            }
        }
    }

    public <H extends OpenNMSEventHandler> void addHandler(final OpenNMSEvent.Type<H> type, final H handler) {
        @SuppressWarnings("unchecked")
        Set<H> handlers = (Set<H>) m_handlers.get(type);
        if (handlers == null) {
            handlers = new HashSet<>();
            m_handlers.put(type, handlers);
        }
        handlers.add(handler);
    }

    @SuppressWarnings("unchecked")
    public <H extends OpenNMSEventHandler> void removeHandler(final Type<H> type, final H handler) {
        final Set<H> handlers = (Set<H>) m_handlers.get(type);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
}
