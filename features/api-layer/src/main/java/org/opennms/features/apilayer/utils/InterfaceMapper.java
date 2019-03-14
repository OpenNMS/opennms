/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.utils;

import java.util.HashMap;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to consume one type of interface from the OSGi registry, map this to another
 * interface, and expose the mapped type.
 *
 * @author jwhite
 * @param <S> input interface
 * @param <T> mapped interface
 */
public abstract class InterfaceMapper<S,T> {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceMapper.class);

    protected final Class<T> clazz;
    protected final BundleContext bundleContext;

    protected final Map<S, ServiceRegistration<T>> extServiceRegistrationMap = new LinkedHashMap<>();

    public InterfaceMapper(Class<T> clazz, BundleContext bundleContext) {
        this.clazz = Objects.requireNonNull(clazz);
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onBind(S extension, Map properties) {
        LOG.debug("bind called with {}: {}", extension, properties);
        if (extension != null) {
            extServiceRegistrationMap.computeIfAbsent(extension, (ext) -> {
                final T mappedExt = map(ext);
                final Hashtable<String,Object> props = new Hashtable<>();
                // Add any service specific properties
                getServiceProperties(extension).forEach(props::put);
                // Make the service available to any Spring-based listeners
                props.put("registration.export", Boolean.TRUE.toString());
                props.putAll(getServiceProperties(extension));
                return bundleContext.registerService(clazz, mappedExt, props);
            });
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onUnbind(S extension, Map properties) {
        LOG.debug("unbind called with {}: {}", extension, properties);
        if (extension != null) {
            final ServiceRegistration<T> registration = extServiceRegistrationMap.remove(extension);
            if (registration != null) {
                registration.unregister();
            }
        }
    }

    //Implementations should override if specific service properties needs to be added.
    public Map<String, Object> getServiceProperties(S extension) {
        return Collections.emptyMap();
    }

    public abstract T map(S ext);
}
