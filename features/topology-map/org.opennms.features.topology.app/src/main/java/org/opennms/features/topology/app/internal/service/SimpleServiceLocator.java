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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opennms.features.topology.api.support.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleServiceLocator implements ServiceLocator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleServiceLocator.class);

    private final Map<Class<?>, List> services = new HashMap<>();

    public SimpleServiceLocator(Object... services) {
        if (services != null) {
            Arrays.stream(services).forEach(o -> addService(o));
        }
    }

    public void addService(Object service) {
        // for now we do it the simple way
        for (Class<?> eachInterface : service.getClass().getInterfaces()) {
            services.putIfAbsent(eachInterface, new ArrayList<>());
            services.get(eachInterface).add(service);
        }
    }

    @Override
    public <T> T findSingleService(Class<T> clazz, Predicate<T> postFilter, String bundleContextFilter) {
        final List<T> services = findServices(clazz, bundleContextFilter);
        if (postFilter != null) {
            LOG.warn("PostFilter is set, but is not supported by this service locator. Ignoring postFilter");
        }
        if (!services.isEmpty()) {
            return services.get(0);
        }
        return null;
    }

    @Override
    public <T> List<T> findServices(Class<T> clazz, String query) {
        if (query != null) {
            LOG.warn("Query is set, but not supported by this service locator. Ignoring query");
        }
        final List serviceList = services.get(clazz);
        if (serviceList == null) {
            return new ArrayList<>();
        }
        return serviceList;
    }
}
