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

package org.opennms.features.telemetry.adapters.registry.impl;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.telemetry.adapters.factory.api.AdapterFactory;
import org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelemetryAdapterRegistryImpl implements TelemetryAdapterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryAdapterRegistry.class);

    private static final int LOOKUP_DELAY_MS = 5 * 1000;
    private static final int GRACE_PERIOD_MS = 3 * 60 * 1000;

    private final Map<String, AdapterFactory> m_adapterFactoryByClassName = new HashMap<>();

    private static final String TYPE = "type";

    @SuppressWarnings("rawtypes")
    public synchronized void onBind(AdapterFactory adapterFactory, Map properties) {
        LOG.debug("bind called with {}: {}", adapterFactory, properties);
        if (adapterFactory != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn(
                        "Unable to determine the class name for AdapterFactory: {}, with properties: {}. The adapter will not be registered.",
                        adapterFactory, properties);
                return;
            }
            m_adapterFactoryByClassName.put(className, adapterFactory);
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onUnbind(AdapterFactory adapterFactory, Map properties) {
        LOG.debug("Unbind called with {}: {}", adapterFactory, properties);
        if (adapterFactory != null) {
            final String className = getClassName(properties);
            if (className == null) {
                LOG.warn(
                        "Unable to determine the class name for AdapterFactory: {}, with properties: {}. The adapter will not be unregistered.",
                        adapterFactory, properties);
                return;
            }
            m_adapterFactoryByClassName.remove(className, adapterFactory);
        }
    }

    private static String getClassName(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String) type;
        }
        return null;
    }

    @Override
    public Adapter getAdapter(String className, Protocol protocol, Map<String, String> properties) {
        AdapterFactory adapterFactory = m_adapterFactoryByClassName.get(className);
        while ((adapterFactory == null) && ManagementFactory.getRuntimeMXBean().getUptime() < GRACE_PERIOD_MS) {
            try {
                Thread.sleep(LOOKUP_DELAY_MS);
            } catch (InterruptedException e) {
                LOG.error(
                        "Interrupted while waiting for adapter factory to become available in the service registry. Aborting.");
                return null;
            }
            adapterFactory = m_adapterFactoryByClassName.get(className);
        }
        Adapter adapter = null;
        if (adapterFactory != null) {
            adapter = adapterFactory.createAdapter(protocol, properties);
        }

        return adapter;
    }

}
