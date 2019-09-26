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
