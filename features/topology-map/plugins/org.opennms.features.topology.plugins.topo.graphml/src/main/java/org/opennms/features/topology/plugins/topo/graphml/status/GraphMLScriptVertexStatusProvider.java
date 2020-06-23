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

import com.google.common.collect.Lists;
import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLVertex;
import org.opennms.features.topology.plugins.topo.graphml.internal.AlarmSummaryWrapper;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.features.topology.plugins.topo.graphml.internal.Scripting;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GraphMLScriptVertexStatusProvider implements StatusProvider {

    private final String namespace;
    private final AlarmSummaryWrapper alarmSummaryWrapper;

    private final GraphMLServiceAccessor serviceAccessor;
    private final Scripting<GraphMLVertex, GraphMLVertexStatus> scripting;

    public GraphMLScriptVertexStatusProvider(final String namespace,
                                             final AlarmSummaryWrapper alarmSummaryWrapper,
                                             final ScriptEngineManager scriptEngineManager,
                                             final GraphMLServiceAccessor serviceAccessor,
                                             final Path scriptPath) {
        this.namespace = Objects.requireNonNull(namespace);
        this.alarmSummaryWrapper = Objects.requireNonNull(alarmSummaryWrapper);

        this.serviceAccessor = Objects.requireNonNull(serviceAccessor);

        this.scripting = new Scripting<>(scriptPath,
                                         scriptEngineManager,
                                         GraphMLVertexStatus::new,
                                         GraphMLVertexStatus::merge);
    }

    public GraphMLScriptVertexStatusProvider(final String namespace,
                                             final AlarmSummaryWrapper alarmSummaryWrapper,
                                             final ScriptEngineManager scriptEngineManager,
                                             final GraphMLServiceAccessor serviceAccessor) {
        this(namespace,
             alarmSummaryWrapper,
             scriptEngineManager,
             serviceAccessor,
             Paths.get(System.getProperty("opennms.home"), "etc", "graphml-vertex-status"));
    }

    @Override
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(final VertexProvider vertexProvider,
                                                                           final Collection<VertexRef> vertices,
                                                                           final Criteria[] criteria) {
        // All vertices for the current vertexProvider
        final List<GraphMLVertex> graphMLVertices = vertices.stream()
                                                            .filter(eachVertex -> contributesTo(eachVertex.getNamespace()) && eachVertex instanceof GraphMLVertex)
                                                            .map(eachVertex -> (GraphMLVertex) eachVertex)
                                                            .collect(Collectors.toList());

        // Alarm summary for each node id
        final Map<Integer, AlarmSummary> nodeIdToAlarmSummaryMap = alarmSummaryWrapper.getAlarmSummaries(Lists.transform(graphMLVertices, AbstractVertex::getNodeID))
                                                                                      .stream()
                                                                                      .collect(Collectors.toMap(AlarmSummary::getNodeId, Function.identity()));

        // Calculate status via scripts
        return serviceAccessor.getTransactionOperations().execute(
                t -> this.scripting.compute(graphMLVertices.stream(),
                                            (vertex) -> {
                                                final SimpleBindings bindings = new SimpleBindings();
                                                bindings.put("vertex", vertex);

                                                if (vertex.getNodeID() != null) {
                                                    bindings.put("node", serviceAccessor.getNodeDao().get(vertex.getNodeID()));
                                                    bindings.put("alarmSummary", nodeIdToAlarmSummaryMap.get(vertex.getNodeID()));
                                                }

                                                bindings.put("measurements", new MeasurementsWrapper(serviceAccessor.getMeasurementsService()));
                                                bindings.put("nodeDao", serviceAccessor.getNodeDao());
                                                bindings.put("snmpInterfaceDao", serviceAccessor.getSnmpInterfaceDao());
                                                return bindings;
                                            }));
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return this.getNamespace().equals(namespace);
    }
}
