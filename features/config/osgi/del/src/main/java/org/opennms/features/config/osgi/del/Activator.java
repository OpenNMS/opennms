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

package org.opennms.features.config.osgi.del;

import static org.opennms.features.config.osgi.del.LogUtil.logInfo;

import java.util.Hashtable;

import org.apache.felix.cm.PersistenceManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    private ServiceRegistration<PersistenceManager> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        Hashtable<String, Object> config = new Hashtable<>();
        config.put("name", CmPersistenceManagerDelegator.class.getName());

        logInfo( "Registering service {0}.", CmPersistenceManagerDelegator.class.getSimpleName());

        CmPersistenceManagerDelegator persistenceManager = new CmPersistenceManagerDelegator(context);
        registration = context.registerService(PersistenceManager.class, persistenceManager, config);

        logInfo( "{0} started.", CmPersistenceManagerDelegator.class.getSimpleName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
        logInfo("{0} stopped.", CmPersistenceManagerDelegator.class.getSimpleName());
    }
}