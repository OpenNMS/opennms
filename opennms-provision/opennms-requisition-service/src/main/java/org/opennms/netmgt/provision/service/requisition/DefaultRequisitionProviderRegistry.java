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

package org.opennms.netmgt.provision.service.requisition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.poller.support.DefaultServiceMonitorRegistry;
import org.opennms.netmgt.provision.persist.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return m_providersByType.keySet();
    }

    private static String getType(Map<?, ?> properties) {
        final Object type = properties.get(TYPE);
        if (type != null && type instanceof String) {
            return (String)type;
        }
        return null;
    }

}
