/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.operations;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaTopologySelector {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTopologySelector.class);

    private BundleContext m_bundleContext;
    private final Map<MetaTopologyProvider, MetaTopologySelectorOperation> m_operations = new HashMap<>();
    private final Map<MetaTopologyProvider, ServiceRegistration<CheckedOperation>> m_registrations = new HashMap<>();

    public void setBundleContext(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    public synchronized void addMetaTopologyProvider(MetaTopologyProvider metaTopologyProvider, Map<?, ?> metaData) {
        try {
            LOG.debug("Adding meta topology provider: " + metaTopologyProvider);

            MetaTopologySelectorOperation operation = new MetaTopologySelectorOperation(metaTopologyProvider, metaData);

            m_operations.put(metaTopologyProvider, operation);

            Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put("operation.menuLocation", "View");
            properties.put("operation.label", operation.getLabel() + "?group=topology");

            ServiceRegistration<CheckedOperation> reg = m_bundleContext.registerService(CheckedOperation.class,
                    operation, properties);

            m_registrations.put(metaTopologyProvider, reg);
        } catch (Throwable e) {
            LOG.warn("Exception during addMetaTopologyProvider()", e);
        }
    }

    public synchronized void removeMetaTopologyProvider(MetaTopologyProvider metaTopologyProvider, Map<?, ?> metaData) {
        try {
            LOG.debug("Removing meta topology provider: {}", metaTopologyProvider);

            m_operations.remove(metaTopologyProvider);
            ServiceRegistration<CheckedOperation> reg = m_registrations.remove(metaTopologyProvider);
            if (reg != null) {
                reg.unregister();
            }
        } catch (Throwable e) {
            LOG.warn("Exception during removeMetaTopologyProvider()", e);
        }
    }
}
