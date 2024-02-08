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
package org.opennms.features.apilayer.common.utils;

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
