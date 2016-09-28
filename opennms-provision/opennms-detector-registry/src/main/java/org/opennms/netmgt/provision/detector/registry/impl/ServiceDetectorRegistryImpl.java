/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceDetectorRegistryImpl implements ServiceDetectorRegistry, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceDetectorRegistryImpl.class);

    @Autowired(required=false)
    ServiceRegistry m_serviceRegistry;

    @Autowired(required=false)
    Set<ServiceDetectorFactory<?>> m_detectorFactories;

    private final Map<String, String> m_classNameByServiceName = new LinkedHashMap<>();
    private final Map<String, ServiceDetectorFactory<? extends ServiceDetector>> m_factoriesByServiceName = new LinkedHashMap<>();
    private final Map<String, ServiceDetectorFactory<? extends ServiceDetector>> m_factoriesByClassName = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // Register all of the @Autowired ServiceDetectorFactory implementations
        if (m_detectorFactories != null) {
            for (ServiceDetectorFactory<?> factory : m_detectorFactories) {
                // Determine the implementation type
                Map<String, String> props = new HashMap<>();
                ServiceDetector detector = factory.createDetector();
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
            m_factoriesByServiceName.put(serviceName, factory);
            m_factoriesByClassName.put(className, factory);
            m_classNameByServiceName.put(serviceName, className);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void onUnbind(ServiceDetectorFactory factory, Map properties) {
        LOG.debug("unbind called with {}: {}", factory, properties);
        if (factory != null) {
            final String serviceName = getServiceName(factory);
            final String className = factory.getDetectorClass().getCanonicalName();
            m_factoriesByServiceName.remove(serviceName, factory);
            m_factoriesByClassName.remove(className, factory);
            m_classNameByServiceName.remove(serviceName, className);
        }
    }

    @Override
    public Map<String, String> getTypes() {
        return Collections.unmodifiableMap(m_classNameByServiceName);
    }

    @Override
    public Set<String> getServiceNames() {
        return Collections.unmodifiableSet(m_factoriesByServiceName.keySet());
    }

    @Override
    public ServiceDetector getDetectorByServiceName(String serviceName) {
        return getDetectorByServiceName(serviceName, Collections.emptyMap());
    }

    @Override
    public ServiceDetector getDetectorByServiceName(String serviceName, Map<String, String> properties) {
        return createDetector(m_factoriesByServiceName.get(serviceName), properties);
    }

    @Override
    public ServiceDetectorFactory<?> getDetectorFactoryByServiceName(String serviceName) {
        return m_factoriesByServiceName.get(serviceName);
    }

    @Override
    public Set<String> getClassNames() {
        return Collections.unmodifiableSet(m_factoriesByClassName.keySet());
    }

    @Override
    public ServiceDetector getDetectorByClassName(String className) {
        return getDetectorByClassName(className, Collections.emptyMap());
    }

    @Override
    public ServiceDetector getDetectorByClassName(String className, Map<String, String> properties) {
        return createDetector(m_factoriesByClassName.get(className), properties);
    }

    @Override
    public ServiceDetectorFactory<?> getDetectorFactoryByClassName(String className) {
        return m_factoriesByClassName.get(className);
    }

    private static ServiceDetector createDetector(ServiceDetectorFactory<? extends ServiceDetector> factory, Map<String, String> properties) {
        if (factory == null) {
            return null;
        }
        final ServiceDetector detector = factory.createDetector();
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(detector);
        wrapper.setPropertyValues(properties);
        return detector;
    }

    private static String getServiceName(ServiceDetectorFactory<? extends ServiceDetector> factory) {
        return factory.createDetector().getServiceName();
    }
}
