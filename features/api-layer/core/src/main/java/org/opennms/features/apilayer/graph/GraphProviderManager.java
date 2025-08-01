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
import org.opennms.integration.api.v1.graph.Graph;
import org.opennms.integration.api.v1.graph.GraphInfo;
import org.opennms.integration.api.v1.graph.GraphProvider;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.osgi.framework.BundleContext;

public class GraphProviderManager extends InterfaceMapper<GraphProvider, org.opennms.netmgt.graph.api.service.GraphProvider> {

    public GraphProviderManager(BundleContext bundleContext) {
        super(org.opennms.netmgt.graph.api.service.GraphProvider.class, bundleContext);
    }

    @Override
    public org.opennms.netmgt.graph.api.service.GraphProvider map(GraphProvider extension) {
        return new org.opennms.netmgt.graph.api.service.GraphProvider() {

            @Override
            public ImmutableGraph<?, ?> loadGraph() {
                final Graph extensionGraph = extension.loadGraph();
                final GenericGraph convertedGraph = new GraphMapper().map(extensionGraph, extension.getGraphConfiguration());
                return convertedGraph;
            }

            @Override
            public org.opennms.netmgt.graph.api.info.GraphInfo getGraphInfo() {
                final GraphInfo extensionGraphInfo = extension.getGraphInfo();
                return new GraphMapper().map(extensionGraphInfo);
            }
        };
    }

    @Override
    public Map<String, Object> getServiceProperties(GraphProvider extension) {
        return GraphContainerProviderManager.getServiceProperties(extension.getTopologyConfiguration(), extension.getGraphConfiguration());
    }
}
