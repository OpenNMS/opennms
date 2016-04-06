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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.internal.DefaultBusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

        // Generate the power set of all the child edges
        Set<Set<GraphEdge>> powerSet = generatePowerSet(childEdges);

        // Sort the subsets in the power set by size
        List<Set<GraphEdge>> subsetsInAscendingSize = powerSet.stream()
                .sorted((a,b)-> a.size() - b.size())
                .collect(Collectors.toList());

        // Simulate replacing the mapped severity off all the edges
        // in a given subset with the minimal severity.
        // If the resulting  reduced value is less than the current value, we'll deem this
        // particular subset as "impacting".
        // Once we find an impacting subset, only continue the simulation with other
        // subsets of that same size, since any larger subset may contain those
        // edges along with other non-impacting edges.
        List<Set<GraphEdge>> impactingSubsets = Lists.newArrayList();
        for (Set<GraphEdge> subSet : subsetsInAscendingSize) {
            if (impactingSubsets.size() > 0 && subSet.size() > impactingSubsets.iterator().next().size()) {
                // We already found one more more smaller impacting subsets, we're done
                break;
            }

            // Gather the statuses for all of the child edges
            Map<GraphEdge, Status> edgesWithStatus = childEdges.stream()
                    .collect(Collectors.toMap(Function.identity(), e -> e.getStatus()));

            // Now replace the status for the edges in the current subset with minimum severity
            for (GraphEdge edge : subSet) {
                edgesWithStatus.put(edge, DefaultBusinessServiceStateMachine.MIN_SEVERITY);
            }

            // Weigh and reduce the statuses
            List<Status> statuses = DefaultBusinessServiceStateMachine.weighStatuses(edgesWithStatus);
            Status reducedStatus = parent.getReductionFunction().reduce(statuses).orElse(DefaultBusinessServiceStateMachine.MIN_SEVERITY);

            // Did replacing the status of the edges in the current subset affect the status?
            if (reducedStatus.isLessThan(parent.getStatus())) {
                impactingSubsets.add(subSet);
            }
        }

        // Gather the edges in all of the impacting subsets by taking the union of these
        Set<GraphEdge> union = Collections.emptySet();
        for (Set<GraphEdge> impactingSubset : impactingSubsets) {
            union = Sets.union(union, impactingSubset);
        }
        return union;
    }

    /**
     * Generates the set of all possible subsets from the
     * given elements.
     *
     * @param elements S
     * @return P(S)
     */
    public static <T> Set<Set<T>> generatePowerSet(Collection<T> elements) {
        Set<Set<T>> powerSet = Sets.newConcurrentHashSet(); // Allows us to modify while iterating
        powerSet.add(Sets.newHashSet());
        for (T element : elements) {
            Iterator<Set<T>> iterator = powerSet.iterator();
            while (iterator.hasNext()) {
                Set<T> existingSubset = iterator.next();
                Set<T> newSubset = Sets.newHashSet(existingSubset);
                newSubset.add(element);
                powerSet.add(newSubset);
            }
        }
        return powerSet;
    }
}
