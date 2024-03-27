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
package org.opennms.netmgt.provision.service.requisition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.poller.support.DefaultServiceMonitorRegistry;
import org.opennms.netmgt.provision.persist.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * A registry of all available {@link RequisitionProviderRegistry} implementations
 * exposed in the OSGi registry.
 *
 * @author jwhite
 */
public class DefaultRequisitionProviderRegistry implements RequisitionProviderRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceMonitorRegistry.class);

    private static final String TYPE = "type";

    private final Map<String, RequisitionProvider> m_providersByType = new HashMap<>();

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onBind(RequisitionProvider provider, Map properties) {
        LOG.debug("bind called with {}: {}", provider, properties);
        if (provider != null) {
            final String type = getType(properties);
            if (type == null) {
                LOG.warn("Unable to determine the type for provider: {}, with properties: {}. The provider will not be registered.",
                        provider, properties);
                return;
            }
            m_providersByType.put(type, provider);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public synchronized void onUnbind(RequisitionProvider provider, Map properties) {
        LOG.debug("Unbind called with {}: {}", provider, properties);
        if (provider != null) {
            final String type = getType(properties);
            if (type == null) {
                LOG.warn("Unable to determine the class name for provider: {}, with properties: {}. The provider will not be unregistered.",
                        provider, properties);
                return;
            }
            m_providersByType.remove(type, provider);
        }
    }

    @Override
    public RequisitionProvider getProviderByType(String type) {
        return m_providersByType.get(type);
    }

    @Override
    public Set<String> getTypes() {
        return ImmutableSet.copyOf(m_providersByType.keySet());
    }

    private static String getType(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String)type;
        }
        return null;
    }

}
