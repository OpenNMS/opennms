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

package org.opennms.netmgt.collection.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Map<String, ServiceCollector> m_collectorsByClassName = new HashMap<>();

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
                LOG.warn("Unable to determine the class name for collector: {}, with properties: {}. The monitor will not be registered.",
                        serviceCollector, properties);
                return;
            }
            m_collectorsByClassName.put(className, serviceCollector);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onUnbind(ServiceCollector serviceCollector, Map properties) {
        LOG.debug("Unbind called with {}: {}", serviceCollector, properties);
        if (serviceCollector != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn("Unable to determine the class name for collector: {}, with properties: {}. The monitor will not be unregistered.",
                        serviceCollector, properties);
                return;
            }
            m_collectorsByClassName.remove(className, serviceCollector);
        }
    }

    @Override
    public ServiceCollector getCollectorByClassName(String className) {
        return m_collectorsByClassName.get(className);
    }

    @Override
    public Set<String> getCollectorClassNames() {
        return Collections.unmodifiableSet(m_collectorsByClassName.keySet());
    }

    private static String getClassName(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String)type;
        }
        return null;
    }

}
