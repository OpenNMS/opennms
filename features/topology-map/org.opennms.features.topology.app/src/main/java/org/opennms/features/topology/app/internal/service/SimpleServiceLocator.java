/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
