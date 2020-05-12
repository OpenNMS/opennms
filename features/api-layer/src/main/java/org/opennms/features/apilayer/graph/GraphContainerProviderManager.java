/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.graph;

import java.util.Map;

import org.opennms.features.apilayer.utils.InterfaceMapper;
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
