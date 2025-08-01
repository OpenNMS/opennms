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
package org.opennms.netmgt.collection.support;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * <p>
 * Aggregates {@link ServiceCollector} implementations exposed via the {@link ServiceLoader}
 * and via the OSGi registry.
 * </p>
 *
 * <p>
 * In order to expose a service collector via the Java Service Loader, you must include the
 * full package and class name in <em>/META-INF/services/org.opennms.netmgt.collection.api.ServiceCollector</em>
 * </p>
 *
 * <p>
 * Services collectors exposed via OSGi must include a 'type' property with the class-name
 * of the services monitor being exposed.
 * </p>
 *
 * @author jwhite
 */
public class DefaultServiceCollectorRegistry implements ServiceCollectorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceCollectorRegistry.class);

    private static final String TYPE = "type";

    private static final ServiceLoader<ServiceCollector> s_serviceCollectorLoader = ServiceLoader.load(ServiceCollector.class);

    private final Map<String, CompletableFuture<ServiceCollector>> m_collectorsByClassName = new HashMap<>();

    public DefaultServiceCollectorRegistry() {
        for (ServiceCollector serviceCollector : s_serviceCollectorLoader) {
            Map<String, String> props = new HashMap<>(1);
            props.put(TYPE, serviceCollector.getClass().getCanonicalName());
            onBind(serviceCollector, props);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onBind(ServiceCollector serviceCollector, Map properties) {
        LOG.debug("bind called with {}: {}", serviceCollector, properties);
        if (serviceCollector != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for collector: {}, with properties: {}. The collector will not be registered.",
                        serviceCollector, properties);
                return;
            }
            CompletableFuture<ServiceCollector> future = m_collectorsByClassName.get(className);
            if(future == null) {
                future = new CompletableFuture<>();
                m_collectorsByClassName.put(className, future);
            }
            future.complete(serviceCollector);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onUnbind(ServiceCollector serviceCollector, Map properties) {
        LOG.debug("Unbind called with {}: {}", serviceCollector, properties);
        if (serviceCollector != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for collector: {}, with properties: {}. The collector will not be unregistered.",
                        serviceCollector, properties);
                return;
            }
            m_collectorsByClassName.remove(className);
        }
    }

    @Override
    public synchronized CompletableFuture<ServiceCollector> getCollectorFutureByClassName(String className) {
        CompletableFuture<ServiceCollector> future = m_collectorsByClassName.get(className);
        if(future == null) {
            future = new CompletableFuture<>();
            m_collectorsByClassName.put(className, future);
        }
        return future;
    }


    @Override
    public Set<String> getCollectorClassNames() {
        return ImmutableSet.copyOf(m_collectorsByClassName.keySet());
    }

    private static String getClassName(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String)type;
        }
        return null;
    }

}
