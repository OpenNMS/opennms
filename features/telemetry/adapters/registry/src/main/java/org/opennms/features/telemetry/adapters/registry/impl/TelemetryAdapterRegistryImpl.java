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

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.telemetry.adapters.factory.api.AdapterFactory;
import org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelemetryAdapterRegistryImpl implements TelemetryAdapterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryAdapterRegistry.class);

    private final Map<String, AdapterFactory> m_adapterFactoryByClassName = new HashMap<>();

    private static final String TYPE = "type";

    @Override
    public AdapterFactory getAdapterFactoryByClassName(String className) {

        return m_adapterFactoryByClassName.get(className);
    }

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

}
