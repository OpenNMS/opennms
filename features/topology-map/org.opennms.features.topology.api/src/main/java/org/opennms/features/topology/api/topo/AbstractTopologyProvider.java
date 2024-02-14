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
package org.opennms.features.topology.api.topo;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.simple.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTopologyProvider implements GraphProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTopologyProvider.class);

    protected final BackendGraph graph;

    protected TopologyProviderInfo topologyProviderInfo = new DefaultTopologyProviderInfo();

    public AbstractTopologyProvider(String namespace) {
        this(new SimpleGraph(Objects.requireNonNull(namespace)));
    }

    public AbstractTopologyProvider(BackendGraph graph) {
        this.graph = Objects.requireNonNull(graph);
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), graph.getNamespace());
    }

    @Override
    public String getNamespace() {
        return graph.getNamespace();
    }

    public TopologyProviderInfo getTopologyProviderInfo() {
        return topologyProviderInfo;
    }

    public void setTopologyProviderInfo(TopologyProviderInfo topologyProviderInfo) {
        this.topologyProviderInfo = topologyProviderInfo;
    }

    @Override
    public BackendGraph getCurrentGraph() {
        return graph;
    }

    @Override
    public abstract void refresh();

    protected static SelectionChangedListener.Selection getSelection(String namespace, List<VertexRef> selectedVertices, ContentType type) {
        final Set<Integer> nodeIds = selectedVertices.stream()
                .filter(v -> namespace.equals(v.getNamespace()))
                .filter(v -> v instanceof AbstractVertex)
                .map(v -> (AbstractVertex) v)
                .map(v -> v.getNodeID())
                .filter(nodeId -> nodeId != null)
                .collect(Collectors.toSet());
        if (type == ContentType.Alarm) {
            return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
        }
        if (type == ContentType.Node) {
            return new SelectionChangedListener.IdSelection<>(nodeIds);
        }
        return SelectionChangedListener.Selection.NONE;
    }
}
