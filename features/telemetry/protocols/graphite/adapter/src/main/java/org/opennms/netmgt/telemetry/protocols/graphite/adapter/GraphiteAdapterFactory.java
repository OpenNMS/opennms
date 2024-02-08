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
package org.opennms.netmgt.telemetry.protocols.graphite.adapter;

import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapterFactory;
import org.osgi.framework.BundleContext;

public class GraphiteAdapterFactory extends AbstractCollectionAdapterFactory {

    public GraphiteAdapterFactory() {
        super(null);
    }

    public GraphiteAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return GraphiteAdapter.class;
    }

    @Override
    public Adapter createBean(AdapterDefinition adapterConfig) {
        final GraphiteAdapter adapter = new GraphiteAdapter(adapterConfig, getTelemetryRegistry().getMetricRegistry());
        adapter.setCollectionAgentFactory(getCollectionAgentFactory());
        adapter.setInterfaceToNodeCache(getInterfaceToNodeCache());
        adapter.setFilterDao(getFilterDao());
        adapter.setNodeDao(getNodeDao());
        adapter.setPersisterFactory(getPersisterFactory());
        adapter.setThresholdingService(getThresholdingService());
        adapter.setBundleContext(getBundleContext());

        return adapter;
    }
}
