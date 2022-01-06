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
import java.util.concurrent.CompletableFuture;

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
    private final Map<String, CompletableFuture<ServiceDetectorFactory<? extends ServiceDetector>>> m_factoriesByClassName = new LinkedHashMap<>();

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
            CompletableFuture<ServiceDetectorFactory<? extends ServiceDetector>> clzNameFactoryFuture = m_factoriesByClassName.get(className);
            if(clzNameFactoryFuture == null) {
                clzNameFactoryFuture = new CompletableFuture<>();
                m_factoriesByClassName.put(className, clzNameFactoryFuture);
            }
            clzNameFactoryFuture.complete(factory);
            m_classNameByServiceName.put(serviceName, className);
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
        return m_factoriesByClassName.get(className).join().getDetectorClass();
    }

    @Override
    public Set<String> getClassNames() {
        return ImmutableSet.copyOf(m_factoriesByClassName.keySet());
    }

    @Override
    public CompletableFuture<ServiceDetector> getDetectorFutureByClassName(String className, Map<String, String> properties) {
        CompletableFuture<ServiceDetectorFactory<? extends ServiceDetector>> factoryFuture = m_factoriesByClassName.get(className);
        if(factoryFuture == null) {
            factoryFuture = new CompletableFuture<>();
            m_factoriesByClassName.put(className, factoryFuture);
        }
        return createDetector(factoryFuture, properties);
    }

    @Override
    public CompletableFuture<ServiceDetectorFactory<?>> getDetectorFactoryFutureByClassName(String className) {
        return m_factoriesByClassName.get(className);
    }




    private static CompletableFuture<ServiceDetector> createDetector(CompletableFuture<ServiceDetectorFactory<? extends ServiceDetector>> factoryFuture, Map<String, String> properties) {
        return factoryFuture.thenApplyAsync(f->f.createDetector(properties));
    }

    private static String getServiceName(ServiceDetectorFactory<? extends ServiceDetector> factory) {
        return factory.createDetector(new HashMap<>()).getServiceName();
    }
}
