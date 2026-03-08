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
package org.opennms.core.daemon.loader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local ServiceCollectorRegistry for standalone daemon containers.
 * Discovers ServiceCollector implementations via Java ServiceLoader.
 */
public class LocalServiceCollectorRegistry implements ServiceCollectorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(LocalServiceCollectorRegistry.class);

    private final Map<String, ServiceCollector> collectorsByClassName = new HashMap<>();

    public LocalServiceCollectorRegistry() {
        for (ServiceCollector collector : ServiceLoader.load(ServiceCollector.class)) {
            final String className = collector.getClass().getCanonicalName();
            LOG.info("Registered service collector: {}", className);
            collectorsByClassName.put(className, collector);
        }
        LOG.info("Loaded {} service collectors via ServiceLoader", collectorsByClassName.size());
    }

    @Override
    public CompletableFuture<ServiceCollector> getCollectorFutureByClassName(String className) {
        final ServiceCollector collector = collectorsByClassName.get(className);
        if (collector != null) {
            return CompletableFuture.completedFuture(collector);
        }
        return CompletableFuture.failedFuture(
                new IllegalArgumentException("Collector not found: " + className));
    }

    @Override
    public Set<String> getCollectorClassNames() {
        return Collections.unmodifiableSet(collectorsByClassName.keySet());
    }
}
