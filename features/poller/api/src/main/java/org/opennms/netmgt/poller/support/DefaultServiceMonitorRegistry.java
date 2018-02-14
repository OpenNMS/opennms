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

package org.opennms.netmgt.poller.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            m_monitorsByClassName.remove(className, serviceMonitor);
        }
    }

    @Override
    public ServiceMonitor getMonitorByClassName(String className) {
        return m_monitorsByClassName.get(className);
    }

    @Override
    public Set<String> getMonitorClassNames() {
        return Collections.unmodifiableSet(m_monitorsByClassName.keySet());
    }

    private static String getClassName(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String)type;
        }
        return null;
    }

}
