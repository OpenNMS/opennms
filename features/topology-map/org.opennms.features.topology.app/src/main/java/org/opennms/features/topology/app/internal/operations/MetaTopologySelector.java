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
package org.opennms.features.topology.app.internal.operations;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.TopologyCache;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaTopologySelector {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTopologySelector.class);

    private BundleContext m_bundleContext;
    private final TopologyCache m_topologyCache;
    private final Map<MetaTopologyProvider, MetaTopologySelectorOperation> m_operations = new HashMap<>();
    private final Map<MetaTopologyProvider, ServiceRegistration<CheckedOperation>> m_registrations = new HashMap<>();

    public MetaTopologySelector(final BundleContext bundleContext, final TopologyCache topologyCache) {
        m_bundleContext = bundleContext;
        m_topologyCache = topologyCache;
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
            // Ensure the TopologyCache is invalidated properly
            metaTopologyProvider.getGraphProviders().forEach(gp -> m_topologyCache.invalidate(metaTopologyProvider.getId(), gp.getNamespace()));
        } catch (Throwable e) {
            LOG.warn("Exception during removeMetaTopologyProvider()", e);
        }
    }
}
