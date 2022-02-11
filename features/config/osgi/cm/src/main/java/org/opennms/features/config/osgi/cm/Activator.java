/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.osgi.cm;

import java.util.Hashtable;
import java.util.Optional;

import org.apache.felix.cm.PersistenceManager;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<PersistenceManager> registration;

    @Override
    public void start(BundleContext context) {

        // Register CmPersistenceManager
        Hashtable<String, Object> config = new Hashtable<>();
        config.put("name", CmPersistenceManager.class.getName());

        LOG.info("Registering service {}.", CmPersistenceManager.class.getSimpleName());
        final ConfigurationManagerService cm = findService(context, ConfigurationManagerService.class);
        CmPersistenceManager persistenceManager = new CmPersistenceManager(cm);
        registration = context.registerService(PersistenceManager.class, persistenceManager, config);
        final ConfigurationAdmin configurationAdmin = findService(context, ConfigurationAdmin.class);
        new CallbackManager().registerCallbacks(configurationAdmin, cm, persistenceManager);

        LOG.info("{} started.", CmPersistenceManager.class.getSimpleName());
    }

    private <T> T findService(BundleContext context, Class<T> clazz) {
        return Optional.ofNullable(context.getServiceReference(clazz))
                .map(context::getService)
                .orElseThrow(() -> new IllegalStateException("Cannot find " + clazz.getName()));
    }

    @Override
    public void stop(BundleContext context) {
        if (registration != null) {
            registration.unregister();
        }
        LOG.info( "{} stopped.", CmPersistenceManager.class.getSimpleName());
    }
}