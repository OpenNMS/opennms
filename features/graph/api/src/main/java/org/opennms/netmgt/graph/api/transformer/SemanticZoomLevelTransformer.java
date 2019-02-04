/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.Vertex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SemanticZoomLevelTransformer<V extends Vertex, E extends Edge, G extends Graph<V, E>> {
    private final Collection<V> verticesInFocus;
    private final int szl;

    public SemanticZoomLevelTransformer(Collection<V> verticesInFocus, int szl) {
        Preconditions.checkArgument(szl >= 0, "Semantic Zoom Level must be >= 0");
        this.verticesInFocus = Objects.requireNonNull(verticesInFocus);
        this.szl = szl;
    }

    public G transform(G sourceGraph, Supplier<G> snapshotGraphFactory) {
        final G snapshot = snapshotGraphFactory.get();
        snapshot.addVertices(verticesInFocus);

        final List<Vertex> alreadyProcessedVertices = new ArrayList<>();

        // Determine all vertices according to szl
        final List<V> verticesToProcess = Lists.newArrayList(verticesInFocus);
        for (int i=0; i<szl; i++) {
            final List<V> tmpVertices = new ArrayList<>();
            for (V eachVertex : verticesToProcess) {
                final Collection<V> neighbors = sourceGraph.getNeighbors(eachVertex);
                snapshot.addVertices(neighbors);

                // Mark for procession
                for (V eachNeighbor : neighbors) {
                    // but only if not already processed or are processing in this iteration
                    if (!alreadyProcessedVertices.contains(eachNeighbor) && !verticesToProcess.contains(eachNeighbor)) {
                        tmpVertices.add(eachNeighbor);
                    }
                }
            }
            alreadyProcessedVertices.addAll(verticesToProcess);
            verticesToProcess.clear();
            verticesToProcess.addAll(tmpVertices);
        }

        // Add all edges now
        // First determine all edges
        final List<E> edges = new ArrayList<>();
        for (V eachVertex : snapshot.getVertices()) {
            edges.addAll(sourceGraph.getConnectingEdges(eachVertex));
        }

        // Second remove all edges which are "on the edge"
        final List<E> edgesToAdd = edges.stream().filter(e -> snapshot.getVertex(e.getSource().getId()) != null)
                .filter(e -> snapshot.getVertex(e.getTarget().getId()) != null)
                .collect(Collectors.toList());
        snapshot.addEdges(edgesToAdd);
        return snapshot;
    }
}
