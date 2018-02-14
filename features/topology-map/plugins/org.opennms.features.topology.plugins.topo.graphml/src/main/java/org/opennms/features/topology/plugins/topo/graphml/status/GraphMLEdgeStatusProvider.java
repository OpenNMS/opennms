/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml.status;

import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLEdge;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.features.topology.plugins.topo.graphml.internal.Scripting;
import org.opennms.netmgt.model.OnmsNode;

import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

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
    public Map<? extends EdgeRef, ? extends Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
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
