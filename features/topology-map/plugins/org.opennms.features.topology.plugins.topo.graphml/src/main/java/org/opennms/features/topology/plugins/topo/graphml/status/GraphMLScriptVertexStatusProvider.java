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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLVertex;
import org.opennms.features.topology.plugins.topo.graphml.internal.AlarmSummaryWrapper;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.features.topology.plugins.topo.graphml.internal.Scripting;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import com.google.common.collect.Lists;

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
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(final BackendGraph graph,
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
