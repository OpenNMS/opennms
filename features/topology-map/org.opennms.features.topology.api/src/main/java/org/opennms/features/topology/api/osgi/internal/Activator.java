/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.osgi.internal;

import org.opennms.features.topology.api.osgi.EventRegistry;
import org.opennms.features.topology.api.osgi.OnmsServiceManager;
import org.ops4j.pax.vaadin.SessionListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Each opennms-bundle which uses vaadin wants to listen to Session-Events
 * (such as sessionInitialized and sessionDestroyed), In addition we need a {@link OnmsServiceManager} service.
 * This bundle registers a {@link EventRegistry} and a {@link OnmsServiceManager} to the OSGI-Container at bundle
 * activation. It also removes these services during bundle deactivation.
 */
public class Activator implements BundleActivator {

    private ServiceRegistration onmsServiceManagerService;
    private ServiceRegistration eventRegistryService;
    private ServiceRegistration sessionListenerService;

    public void start(BundleContext context) throws Exception {
        OnmsServiceManager serviceManager = new OnmsServiceManagerImpl(context);
        onmsServiceManagerService = context.registerService(OnmsServiceManager.class.getName(), serviceManager, null);
        sessionListenerService = context.registerService(SessionListener.class.getName(), serviceManager, null);
        eventRegistryService = context.registerService(EventRegistry.class.getName(), new EventRegistry(serviceManager), null);

    }

    public void stop(BundleContext context) throws Exception {
        if (onmsServiceManagerService != null)
            onmsServiceManagerService.unregister();
        if (eventRegistryService != null) {
            eventRegistryService.unregister();
        }
        if (sessionListenerService != null) {
            sessionListenerService.unregister();
        }
    }
}
