/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ApplicationInitializedEvent;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class ComponentTracker {
    private final Logger LOG = Logger.getLogger(getClass().getName());
    private OpenNMSEventManager m_eventManager;
    private Set<String> m_expectedComponents = new HashSet<>();

    public ComponentTracker(final OpenNMSEventManager eventManager) {
        m_eventManager = eventManager;
    }

    public void track(final Class<?> clazz) {
        LOG.warning("ComponentTracker: Watching for " + clazz.getName() + " initialization.");
        m_expectedComponents.add(clazz.getName());
    }

    public void ready(final Class<?> clazz) {
        LOG.warning("ComponentTracker: Component " + clazz.getName() + " is ready.");
        m_expectedComponents.remove(clazz.getName());
        onReady();
    }

    private void onReady() {
        if (m_expectedComponents.size() == 0) {
            LOG.warning("ComponentTracker: All anticipated components are ready.  Firing ApplicationInitializedEvent.");
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override public void execute() {
                    m_eventManager.fireEvent(new ApplicationInitializedEvent());
                }
            });
        } else {
            LOG.warning("ComponentTracker: Waiting for " + m_expectedComponents.size() + " more components to initialize.");
            LOG.info("ComponentTracker: Components remaining: " + m_expectedComponents);
        }
    }

}
