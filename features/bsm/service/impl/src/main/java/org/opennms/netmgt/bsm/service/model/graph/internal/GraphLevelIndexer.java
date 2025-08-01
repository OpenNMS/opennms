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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.uci.ics.jung.graph.Hypergraph;

/**
 * Computes the hierarchy level (or distance) of all vertices from the starting root nodes.
 * If there are multiple root nodes, than the maximum distance from each root node is used.
 * All vertices which are not connected to the provided root nodes have a level of -1.
 */
public class GraphLevelIndexer<V, E> {

    private Map<V, Integer> levelMap = new HashMap<>();

    private Set<V> verticesIndexedSet = new HashSet<>();

    public void indexLevel(Hypergraph<V,E> graph, Set<V> rootSet) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(rootSet);

        // By default initialize all vertices with -1
        graph.getVertices().forEach(eachVertex -> levelMap.put(eachVertex, -1));

        // Index
        determineLevel(0, graph, rootSet);
    }

    private void determineLevel(final int level, final Hypergraph<V, E> graph, final Collection<V> vertices) {
        for (V eachVertex : vertices) {
            if (graph.containsVertex(eachVertex)) {
                levelMap.put(eachVertex, Math.max(level, levelMap.get(eachVertex).intValue()));
                verticesIndexedSet.add(eachVertex);
                determineLevel(level + 1, graph, graph.getSuccessors(eachVertex));
            }
        }
    }

    public Map<V, Integer> getLevelMap() {
        return Collections.unmodifiableMap(levelMap);
    }
}
