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
