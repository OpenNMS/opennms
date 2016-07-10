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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class ServiceDetectorRegistryImpl implements ServiceDetectorRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceDetectorRegistryImpl.class);

	public static final String IMPLEMENTATION_KEY = "implementation";

    private final Map<String, ServiceDetectorFactory<? extends ServiceDetector>> m_factoriesByServiceName = new HashMap<>();
    private final Map<String, ServiceDetectorFactory<? extends ServiceDetector>> m_factoriesByClassName = new HashMap<>();

    public ServiceDetectorRegistryImpl() {
        // Register all of the ServiceDetectorFactory implementations available via the ServiceLoader
        @SuppressWarnings("rawtypes")
        ServiceLoader<ServiceDetectorFactory> loader = ServiceLoader.load(ServiceDetectorFactory.class);
        for (ServiceDetectorFactory<?> factory : loader) {
            // Determine the implementation type
            Map<String, String> props = new HashMap<>();
            props.put(IMPLEMENTATION_KEY, factory.createDetector().getClass().getCanonicalName());
            // Register the factory
            onBind(factory, props);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void onBind(ServiceDetectorFactory factory, Map properties) {
        LOG.debug("bind called with {}: {}", factory, properties);
        m_factoriesByServiceName.put(getServiceName(factory), factory);
        m_factoriesByClassName.put(getClassName(properties), factory);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void onUnbind(ServiceDetectorFactory factory, Map properties) {
        LOG.debug("unbind called with {}: {}", factory, properties);
        m_factoriesByServiceName.remove(getServiceName(factory), factory);
        m_factoriesByClassName.remove(getClassName(properties), factory);
    }

    @Override
    public Set<String> getServiceNames() {
        return m_factoriesByServiceName.keySet();
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
    public Set<String> getClassNames() {
        return m_factoriesByClassName.keySet();
    }

    @Override
    public ServiceDetector getDetectorByClassName(String className) {
        return getDetectorByClassName(className, Collections.emptyMap());
    }

    @Override
    public ServiceDetector getDetectorByClassName(String className, Map<String, String> properties) {
        return createDetector(m_factoriesByClassName.get(className), properties);
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

    private static String getClassName(Map<?,?> properties) {
        final Object implementation = properties.get(IMPLEMENTATION_KEY);
        if (implementation == null || !(implementation instanceof String)) {
            LOG.error("ServiceDetectorFactory was registered without an '{}' property.", IMPLEMENTATION_KEY);
            throw new IllegalArgumentException("ServiceDetectorFactory was registered without an '" + IMPLEMENTATION_KEY + "' property.");
        }
        return (String)implementation;
    }
}
