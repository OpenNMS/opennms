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
package org.opennms.netmgt.poller.support;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * <p>
 * Aggregates {@link ServiceMonitor} implementations exposed via the {@link ServiceLoader}
 * and via the OSGi registry.
 * </p>
 *
 * <p>
 * In order to expose a service monitor via the Java Service Loader, you must include the
 * full package and class name in <em>/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor</em>
 * </p>
 *
 * <p>
 * Services monitors exposed via OSGi must include a 'type' property with the class-name
 * of the services monitor being exposed.
 * </p>
 *
 * @author jwhite
 */
public class DefaultServiceMonitorRegistry implements ServiceMonitorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceMonitorRegistry.class);

    private static final String TYPE = "type";

    private static final ServiceLoader<ServiceMonitor> s_serviceMonitorLoader = ServiceLoader.load(ServiceMonitor.class);

    private final Map<String, ServiceMonitor> m_monitorsByClassName = new HashMap<>();

    public DefaultServiceMonitorRegistry() {
        for (ServiceMonitor serviceMonitor : s_serviceMonitorLoader) {
            Map<String, String> props = new HashMap<>(1);
            props.put(TYPE, serviceMonitor.getClass().getCanonicalName());
            onBind(serviceMonitor, props);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onBind(ServiceMonitor serviceMonitor, Map properties) {
        LOG.debug("bind called with {}: {}", serviceMonitor, properties);
        if (serviceMonitor != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for monitor: {}, with properties: {}. The monitor will not be registered.",
                        serviceMonitor, properties);
                return;
            }
            m_monitorsByClassName.put(className, serviceMonitor);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onUnbind(ServiceMonitor serviceMonitor, Map properties) {
        LOG.debug("Unbind called with {}: {}", serviceMonitor, properties);
        if (serviceMonitor != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for monitor: {}, with properties: {}. The monitor will not be unregistered.",
                        serviceMonitor, properties);
                return;
            }
            m_monitorsByClassName.remove(className);
        }
    }
    
    @Override
    public synchronized ServiceMonitor getMonitorByClassName(String className) {
        return m_monitorsByClassName.get(className);
    }

    @Override
    public Set<String> getMonitorClassNames() {
        return ImmutableSet.copyOf(m_monitorsByClassName.keySet());
    }

    private static String getClassName(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String)type;
        }
        return null;
    }

}
