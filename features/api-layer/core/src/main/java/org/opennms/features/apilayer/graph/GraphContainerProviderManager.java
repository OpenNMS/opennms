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
package org.opennms.features.apilayer.graph;

import java.util.Map;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.integration.api.v1.graph.GraphContainer;
import org.opennms.integration.api.v1.graph.GraphContainerProvider;
import org.opennms.integration.api.v1.graph.Properties;
import org.opennms.integration.api.v1.graph.configuration.GraphCacheStrategy;
import org.opennms.integration.api.v1.graph.configuration.GraphConfiguration;
import org.opennms.integration.api.v1.graph.configuration.TopologyConfiguration;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableMap;

public class GraphContainerProviderManager extends InterfaceMapper<GraphContainerProvider, org.opennms.netmgt.graph.api.service.GraphContainerProvider> {

    public GraphContainerProviderManager(BundleContext bundleContext) {
        super(org.opennms.netmgt.graph.api.service.GraphContainerProvider.class, bundleContext);
    }

    @Override
    public org.opennms.netmgt.graph.api.service.GraphContainerProvider map(GraphContainerProvider extension) {
        return new org.opennms.netmgt.graph.api.service.GraphContainerProvider() {

            @Override
            public ImmutableGraphContainer loadGraphContainer() {
                final GraphContainer graphContainer = extension.loadGraphContainer();
                final GenericGraphContainer convertedGraphContainer = new GraphMapper().map(graphContainer, extension.getGraphConfiguration());
                return convertedGraphContainer;
            }

            @Override
            public GraphContainerInfo getContainerInfo() {
                final org.opennms.integration.api.v1.graph.GraphContainerInfo graphContainerInfo = extension.getGraphContainerInfo();
                final GraphContainerInfo convertedGraphContainerInfo = new GraphMapper().map(graphContainerInfo);
                return convertedGraphContainerInfo;
            }
        };
    }

    @Override
    public Map<String, Object> getServiceProperties(GraphContainerProvider extension) {
        return getServiceProperties(extension.getTopologyConfiguration(), extension.getGraphConfiguration());
    }

    public static ImmutableMap<String, Object> getServiceProperties(final TopologyConfiguration topologyConfiguration, final GraphConfiguration graphConfiguration) {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder()
            .put("expose-to-topology", Boolean.toString(topologyConfiguration.isLegacyTopology()))
            .put("expose-status-provider", Boolean.toString(topologyConfiguration.getLegacyStatusStrategy() == TopologyConfiguration.LegacyStatusStrategy.Default))
            .put("resolve-node-ids", Boolean.toString(topologyConfiguration.shouldResolveNodes())) // legacy: to be removed
            .put(Properties.Enrichment.RESOLVE_NODES, Boolean.toString(topologyConfiguration.shouldResolveNodes()));;

        // In case the cache strategy is timed, it should be expose it accordingly
        if (graphConfiguration.getGraphCacheStrategy() instanceof GraphCacheStrategy.TimedGraphCacheStrategy) {
            GraphCacheStrategy.TimedGraphCacheStrategy timedGraphCacheStrategy = ((GraphCacheStrategy.TimedGraphCacheStrategy)graphConfiguration.getGraphCacheStrategy());
            if (timedGraphCacheStrategy.getCacheReloadIntervalInSeconds() > 0) {
                builder.put("cacheInvalidateInterval", Long.toString(timedGraphCacheStrategy.getCacheReloadIntervalInSeconds()));
            }
        }
        return builder.build();
    }
}
