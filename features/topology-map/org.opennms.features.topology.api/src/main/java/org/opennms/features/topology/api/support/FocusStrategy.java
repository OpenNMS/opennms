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

package org.opennms.features.topology.api.support;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;

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
    EMPTY((FocusStrategyImplementation) (topologyProvider, arguments) -> Lists.newArrayList()),

    /**
     * Adds all Vertices to focus.
     */
    ALL((FocusStrategyImplementation) (topologyProvider, arguments) -> topologyProvider.getVertices().stream().map(VertexHopGraphProvider.DefaultVertexHopCriteria::new).collect(Collectors.toList())),

    /**
     * First element is added to focus.
     */
    FIRST((FocusStrategyImplementation) (topologyProvider, arguments) -> {
        List<VertexHopCriteria> collected = topologyProvider.getVertices().stream()
                .map(VertexHopGraphProvider.DefaultVertexHopCriteria::new)
                .collect(Collectors.toList());
        if (!collected.isEmpty()) {
            return collected.subList(0, 1);
        }
        return Lists.newArrayList();
    }),

    /**
     * The provided list of IDs is added to focus.
     */
    SPECIFIC((FocusStrategyImplementation) (topologyProvider, arguments) -> {
        Objects.requireNonNull(arguments);

        List<VertexHopCriteria> collected = Arrays.stream(arguments)
                .map(eachArgument -> new DefaultVertexRef(topologyProvider.getNamespace(), eachArgument))
                .map(eachVertexRef -> topologyProvider.getVertex(eachVertexRef))
                .filter(Objects::nonNull)
                .map(VertexHopGraphProvider.DefaultVertexHopCriteria::new)
                .collect(Collectors.toList());
        return collected;
    });

    private final FocusStrategyImplementation implementation;

    FocusStrategy(FocusStrategyImplementation implementation) {
        this.implementation = implementation;
    }

    public List<VertexHopCriteria> getFocusCriteria(GraphProvider topologyProvider, String... arguments) {
        return implementation.determine(topologyProvider, arguments);
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
