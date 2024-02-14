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
package org.opennms.netmgt.provision.detector.registry.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ServiceDetectorRegistryImpl implements ServiceDetectorRegistry, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceDetectorRegistryImpl.class);

    @Autowired(required=false)
    ServiceRegistry m_serviceRegistry;

    @Autowired(required=false)
    Set<ServiceDetectorFactory<?>> m_detectorFactories;

    private final Map<String, String> m_classNameByServiceName = new LinkedHashMap<>();
    private final Map<String, ServiceDetectorFactory<? extends ServiceDetector>> m_factoriesByClassName = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // Register all of the @Autowired ServiceDetectorFactory implementations
        if (m_detectorFactories != null) {
            for (ServiceDetectorFactory<?> factory : m_detectorFactories) {
                // Determine the implementation type
                Map<String, String> props = new HashMap<>();
                ServiceDetector detector = factory.createDetector(props);
                // Register the factory
                onBind(factory, props);
                // Add the detector to the service registry
                addAllExtensions(Collections.singleton(detector), ServiceDetector.class);
            }
        }
    }

    private <T> void addAllExtensions(Collection<T> extensions, Class<?>... extensionPoints) {
        if (extensions == null || extensions.isEmpty()) {
            LOG.info("Found NO Extensions for ExtensionPoints {}", Arrays.toString(extensionPoints));
            return;
        }
        for(T extension : extensions) {
            LOG.info("Register Extension {} for ExtensionPoints {}", extension, Arrays.toString(extensionPoints));
            m_serviceRegistry.register(extension, extensionPoints);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void onBind(ServiceDetectorFactory factory, Map properties) {
        LOG.debug("bind called with {}: {}", factory, properties);
        if (factory != null) {
            final String serviceName = getServiceName(factory);
            final String className = factory.getDetectorClass().getCanonicalName();
            m_classNameByServiceName.put(serviceName, className);
            m_factoriesByClassName.put(className, factory);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void onUnbind(ServiceDetectorFactory factory, Map properties) {
        LOG.debug("unbind called with {}: {}", factory, properties);
        if (factory != null) {
            final String serviceName = getServiceName(factory);
            final String className = factory.getDetectorClass().getCanonicalName();
            m_factoriesByClassName.remove(className);
            m_classNameByServiceName.remove(serviceName);
        }
    }

    @Override
    public Map<String, String> getTypes() {
        return ImmutableMap.copyOf(m_classNameByServiceName);
    }

    @Override
    public synchronized Set<String> getServiceNames() {
        return ImmutableSet.copyOf(m_classNameByServiceName.keySet());
    }

    @Override
    public String getDetectorClassNameFromServiceName(String serviceName) {
        return m_classNameByServiceName.get(serviceName);
    }

    @Override
    public Class<?> getDetectorClassByServiceName(String serviceName) {
        String className = m_classNameByServiceName.get(serviceName);
        return m_factoriesByClassName.get(className).getDetectorClass();
    }

    @Override
    public Set<String> getClassNames() {
        return ImmutableSet.copyOf(m_factoriesByClassName.keySet());
    }

    @Override
    public ServiceDetector getDetectorByClassName(String className, Map<String, String> properties) {
        ServiceDetectorFactory<? extends ServiceDetector> factory = getDetectorFactoryByClassName(className);
        return createDetector(factory, properties);
    }

    @Override
    public ServiceDetectorFactory<?> getDetectorFactoryByClassName(String className) {
        return m_factoriesByClassName.get(className);
    }

    private static ServiceDetector createDetector(ServiceDetectorFactory<? extends ServiceDetector> factory, Map<String, String> properties) {
        return factory == null? null: factory.createDetector(properties);
    }

    private static String getServiceName(ServiceDetectorFactory<? extends ServiceDetector> factory) {
        return factory.createDetector(new HashMap<>()).getServiceName();
    }
}
