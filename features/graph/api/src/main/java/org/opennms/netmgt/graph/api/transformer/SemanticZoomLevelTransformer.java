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
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SemanticZoomLevelTransformer {
    private final Collection<GenericVertex> verticesInFocus;
    private final int szl;

    public SemanticZoomLevelTransformer(Collection<GenericVertex> verticesInFocus, int szl) {
        Preconditions.checkArgument(szl >= 0, "Semantic Zoom Level must be >= 0");
        this.verticesInFocus = Objects.requireNonNull(verticesInFocus);
        this.szl = szl;
    }

    public GenericGraph transform(GenericGraph sourceGraph) {
        // Determine vertices that are in focus but also actually known by the source graph
        final List<VertexRef> vertexRefsInFocus = verticesInFocus.stream().map(v -> new VertexRef(v.getNamespace(), v.getId())).collect(Collectors.toList());
        final List<GenericVertex> knownVerticesInFocus = sourceGraph.resolveVertexRefs(vertexRefsInFocus);

        // Now build the view
        final GenericGraphBuilder graphBuilder = GenericGraph.builder()
                .graphInfo(sourceGraph)
                .properties(sourceGraph.getProperties())
                .addVertices(knownVerticesInFocus);

        // Determine all vertices according to szl
        final List<Vertex> alreadyProcessedVertices = new ArrayList<>();
        final List<GenericVertex> verticesToProcess = Lists.newArrayList(knownVerticesInFocus);
        for (int i=0; i<szl; i++) {
            final List<GenericVertex> tmpVertices = new ArrayList<>();
            for (GenericVertex eachVertex : verticesToProcess) {
                final Collection<GenericVertex> neighbors = sourceGraph.getNeighbors(eachVertex);
                graphBuilder.addVertices(neighbors);

                // Mark for procession
                for (GenericVertex eachNeighbor : neighbors) {
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
        final List<GenericEdge> edges = new ArrayList<>();
        for (GenericVertex eachVertex : graphBuilder.getVertices()) {
            edges.addAll(sourceGraph.getConnectingEdges(eachVertex));
        }

        // Second remove all edges which are "on the edge"
        final List<GenericEdge> edgesToAdd = edges.stream().filter(e -> graphBuilder.getVertex(e.getSource().getId()) != null)
                .filter(e -> graphBuilder.getVertex(e.getTarget().getId()) != null)
                .collect(Collectors.toList());
        graphBuilder.addEdges(edgesToAdd);
        return graphBuilder.build();
    }
}
