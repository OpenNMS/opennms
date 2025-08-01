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
package org.opennms.netmgt.bsm.service.model.graph.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.internal.DefaultBusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndex;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Lists;

public class GraphAlgorithms {

    public static List<GraphVertex> calculateRootCause(BusinessServiceGraph graph, GraphVertex vertex) {
        if (vertex == null || vertex.getStatus().isLessThanOrEqual(Status.NORMAL)) {
            return Collections.emptyList();
        }

        // Gather the list of child vertices that impact the current vertex
        final List<GraphVertex> childVerticesWithImpact = calculateImpacting(graph, vertex)
                .stream()
                .map(e -> graph.getOpposite(vertex, e))
                .sorted()
                .collect(Collectors.toList());

        // Recurse
        final List<GraphVertex> causes = Lists.newArrayList(childVerticesWithImpact);
        for (GraphVertex childVertexWithImpact : childVerticesWithImpact) {
            causes.addAll(calculateRootCause(graph, childVertexWithImpact));
        }
        return causes;
    }

    public static List<GraphVertex> calculateImpact(BusinessServiceGraph graph, GraphVertex vertex) {
        if (vertex == null) {
            return Collections.emptyList();
        }

        // Gather the list of parent vertices that are impacted by the current vertex
        final List<GraphVertex> impactedParentVertices = graph.getInEdges(vertex).stream()
                .filter(e -> calculateImpacting(graph, graph.getOpposite(vertex, e)).contains(e))
                .map(e -> graph.getOpposite(vertex, e))
                .sorted()
                .collect(Collectors.toList());

        // Recurse
        final List<GraphVertex> impacts = Lists.newArrayList(impactedParentVertices);
        for (GraphVertex impactedParentVertex : impactedParentVertices) {
            impacts.addAll(calculateImpact(graph, impactedParentVertex));
        }
        return impacts;
    }

    public static Set<GraphEdge> calculateImpacting(BusinessServiceGraph graph, GraphVertex parent) {
        // Grab all of the child edges
        List<GraphEdge> childEdges = graph.getOutEdges(parent).stream()
                .collect(Collectors.toList());

        // Weigh and reduce the statuses
        List<StatusWithIndex> statuses = DefaultBusinessServiceStateMachine.weighEdges(childEdges);
        Optional<StatusWithIndices> reducedStatus = parent.getReductionFunction().reduce(statuses);

        if (!reducedStatus.isPresent()) {
            return Collections.emptySet();
        } else {
            return reducedStatus.get().getIndices().stream()
                .map(childEdges::get)
                .collect(Collectors.toSet());
        }
    }
}
