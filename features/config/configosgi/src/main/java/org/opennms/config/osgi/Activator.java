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

package org.opennms.config.osgi;

import java.util.Hashtable;
import java.util.Optional;

import org.apache.felix.cm.PersistenceManager;
import org.opennms.config.configservice.api.ConfigurationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

//    private ServiceReference<PersistenceManager> reference;
//    private ServiceRegistration<PersistenceManager> registration;
    
    @Override
    public void start(BundleContext context) throws Exception {
        Hashtable<String,String> config = new Hashtable<>();
        config.put("service.ranking", "1000");
        config.put("name", OpenNMSPersistenceManager.class.getName());
        System.out.printf("Registering service %s.%n", OpenNMSPersistenceManager.class.getSimpleName());

        final ConfigurationService configService = Optional.ofNullable(context.getServiceReference(ConfigurationService.class))
                .map(context::getService)
                .orElseThrow(() -> new IllegalStateException("Cannot find " + ConfigurationService.class.getName()));

        context.registerService(PersistenceManager.class, new OpenNMSPersistenceManager(context, configService), config);
//        reference = registration
//                .getReference();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println(OpenNMSPersistenceManager.class.getSimpleName() + "stopped");
    }
}
