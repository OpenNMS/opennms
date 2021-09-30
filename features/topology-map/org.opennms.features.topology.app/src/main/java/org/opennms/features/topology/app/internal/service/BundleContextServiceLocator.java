/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.topology.api.support.ServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleContextServiceLocator implements ServiceLocator {

    private static final Logger LOG = LoggerFactory.getLogger(BundleContextServiceLocator.class);

    private final BundleContext bundleContext;

    public BundleContextServiceLocator(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public <T> T findSingleService(Class<T> clazz, Predicate<T> postFilter, String bundleContextFilter) {
        List<T> providers = findServices(clazz, bundleContextFilter);
        Stream<T> stream = providers.stream();
        if (postFilter != null) { // filter may be null
            stream = stream.filter(postFilter);
        }
        providers = stream.collect(Collectors.toList());
        if (providers.size() > 1) {
            LOG.warn("Found more than one {}s. This is not supported. Using 1st one in list.", clazz.getSimpleName());
        }
        if (!providers.isEmpty()) {
            return providers.iterator().next();
        }
        return null;
    }

    @Override
    public <T> List<T> findServices(Class<T> clazz, String query) {
        List<T> serviceList = new ArrayList<>();
        LOG.debug("Finding Service of type {} and additional filter criteria {} ...", clazz, query);
        try {
            ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(clazz.getName(), query);
            if (allServiceReferences != null) {
                for (ServiceReference<?> eachServiceReference : allServiceReferences) {
                    @SuppressWarnings("unchecked")
                    T statusProvider = (T) bundleContext.getService(eachServiceReference);
                    serviceList.add(statusProvider);
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Could not query BundleContext for services", e);
        }
        LOG.debug("Found {} services", serviceList.size());
        return serviceList;
    }
}
