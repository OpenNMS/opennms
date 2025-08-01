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
package org.opennms.netmgt.graph.provider.legacy;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public class LegacyGraphContainer implements ImmutableGraphContainer<LegacyGraph> {
    private final MetaTopologyProvider delegate;
    private final String id;
    private final String label;
    private final String description;
    
    public LegacyGraphContainer(MetaTopologyProvider delegate, String id, String label, String description) {
        this.delegate = delegate;
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public List<LegacyGraph> getGraphs() {
        return delegate.getGraphProviders().stream().map(LegacyGraph::getLegacyGraphFromTopoGraphProvider).collect(Collectors.toList());
    }

    @Override
    public LegacyGraph getGraph(String namespace) {
        return LegacyGraph.getLegacyGraphFromTopoGraphProvider(delegate.getGraphProviderBy(namespace));
    }

    @Override
    public GenericGraphContainer asGenericGraphContainer() {
        GenericGraphContainer.GenericGraphContainerBuilder builder = GenericGraphContainer.builder()
            .id(id)
            .label(label)
            .description(description);
        for (LegacyGraph graph: getGraphs()) {
            builder.addGraph(graph.asGenericGraph());
        }
        return builder.build();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getNamespaces() {
        return delegate.getGraphProviders().stream().map(GraphProvider::getNamespace).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        return LegacyGraph.getGraphInfo(delegate.getGraphProviderBy(namespace));
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return LegacyGraph.getGraphInfo(delegate.getDefaultGraphProvider());
    }

    @Override
    public List<GraphInfo> getGraphInfos() {
        return delegate.getGraphProviders().stream().map(LegacyGraph::getGraphInfo).collect(Collectors.toList());
    }
}
