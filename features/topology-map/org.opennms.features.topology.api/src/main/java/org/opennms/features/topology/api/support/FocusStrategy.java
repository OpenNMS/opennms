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
package org.opennms.features.topology.api.support;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;

import com.google.common.collect.Lists;

/**
 * Different strategies to determine the vertices in Focus.
 *
 * @author mvrueden
 */
public enum FocusStrategy {

    /**
     * Empty focus
     */
    EMPTY((FocusStrategyImplementation) (graph, arguments) -> Lists.newArrayList()),

    /**
     * Adds all Vertices to focus.
     */
    ALL((FocusStrategyImplementation) (graph, arguments) -> graph.getVertices().stream().map(DefaultVertexHopCriteria::new).collect(Collectors.toList())),

    /**
     * First element is added to focus.
     */
    FIRST((FocusStrategyImplementation) (topologyProvider, arguments) -> {
        List<VertexHopCriteria> collected = topologyProvider.getVertices().stream()
                .map(DefaultVertexHopCriteria::new)
                .collect(Collectors.toList());
        if (!collected.isEmpty()) {
            return collected.subList(0, 1);
        }
        return Lists.newArrayList();
    }),

    /**
     * The provided list of IDs is added to focus.
     */
    SPECIFIC((FocusStrategyImplementation) (graph, arguments) -> {
        Objects.requireNonNull(arguments);

        List<VertexHopCriteria> collected = Arrays.stream(arguments)
                .map(eachArgument -> new DefaultVertexRef(graph.getNamespace(), eachArgument))
                .map(eachVertexRef -> graph.getVertex(eachVertexRef))
                .filter(Objects::nonNull)
                .map(DefaultVertexHopCriteria::new)
                .collect(Collectors.toList());
        return collected;
    });

    private final FocusStrategyImplementation implementation;

    FocusStrategy(FocusStrategyImplementation implementation) {
        this.implementation = implementation;
    }

    public List<VertexHopCriteria> getFocusCriteria(BackendGraph graph, String... arguments) {
        return implementation.determine(graph, arguments);
    }

    public static FocusStrategy getStrategy(String input, FocusStrategy defaultValue) {
        for (FocusStrategy eachStrategy : FocusStrategy.values()) {
            if (eachStrategy.name().equalsIgnoreCase(input)) {
                return eachStrategy;
            }
        }
        return defaultValue;
    }
}
