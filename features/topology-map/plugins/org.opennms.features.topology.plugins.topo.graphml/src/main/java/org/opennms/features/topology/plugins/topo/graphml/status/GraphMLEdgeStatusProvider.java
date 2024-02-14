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
package org.opennms.features.topology.plugins.topo.graphml.status;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.simple.SimpleConnector;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLEdge;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.features.topology.plugins.topo.graphml.internal.Scripting;
import org.opennms.netmgt.model.OnmsNode;

public class GraphMLEdgeStatusProvider implements EdgeStatusProvider {

    private final GraphMLTopologyProvider provider;
    private final GraphMLServiceAccessor serviceAccessor;

    private final Scripting<GraphMLEdge, GraphMLEdgeStatus> scripting;

    public GraphMLEdgeStatusProvider(final GraphMLTopologyProvider provider,
                                     final ScriptEngineManager scriptEngineManager,
                                     final GraphMLServiceAccessor serviceAccessor,
                                     final Path scriptPath) {
        this.provider = Objects.requireNonNull(provider);
        this.serviceAccessor = Objects.requireNonNull(serviceAccessor);

        this.scripting = new Scripting<>(scriptPath,
                                         scriptEngineManager,
                                         GraphMLEdgeStatus::new,
                                         GraphMLEdgeStatus::merge);
    }

    public GraphMLEdgeStatusProvider(final GraphMLTopologyProvider provider,
                                     final ScriptEngineManager scriptEngineManager,
                                     final GraphMLServiceAccessor serviceAccessor) {
        this(provider,
             scriptEngineManager,
             serviceAccessor,
             Paths.get(System.getProperty("opennms.home"), "etc", "graphml-edge-status"));
    }

    @Override
    public Map<? extends EdgeRef, ? extends Status> getStatusForEdges(BackendGraph graph, Collection<EdgeRef> edges, Criteria[] criteria) {
        return serviceAccessor.getTransactionOperations().execute(
                t -> this.scripting.compute(edges.stream()
                                                 .filter(edge -> edge instanceof GraphMLEdge)
                                                 .map(edge -> (GraphMLEdge) edge),
                                            (edge) -> {
            final SimpleBindings bindings = new SimpleBindings();
            bindings.put("edge", edge);
            bindings.put("sourceNode", getNodeForEdgeVertexConnector(edge.getSource()));
            bindings.put("targetNode", getNodeForEdgeVertexConnector(edge.getTarget()));
            bindings.put("measurements", new MeasurementsWrapper(serviceAccessor.getMeasurementsService()));
            bindings.put("nodeDao", serviceAccessor.getNodeDao());
            bindings.put("snmpInterfaceDao", serviceAccessor.getSnmpInterfaceDao());
            return bindings;
        }));
    }

    @Override
    public String getNamespace() {
        return provider.getNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }

    private OnmsNode getNodeForEdgeVertexConnector(final SimpleConnector simpleConnector) {
        if (simpleConnector != null && simpleConnector.getVertex() instanceof AbstractVertex) {
            AbstractVertex abstractVertex = (AbstractVertex) simpleConnector.getVertex();
            if (abstractVertex.getNodeID() != null) {
                return serviceAccessor.getNodeDao().get(abstractVertex.getNodeID());
            }
        }
        return null;
    }
}
