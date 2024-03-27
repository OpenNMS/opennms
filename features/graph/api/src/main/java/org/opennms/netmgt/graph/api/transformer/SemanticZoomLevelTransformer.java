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
