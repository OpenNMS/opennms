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

package org.opennms.netmgt.graph.api.focus;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.graph.api.generic.TestObjectCreator.createGraphBuilder;
import static org.opennms.netmgt.graph.api.generic.TestObjectCreator.createGraphBuilderEmpty;
import static org.opennms.netmgt.graph.api.generic.TestObjectCreator.createVertex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

public class FocusStrategyTest {

    @Test
    public void testFocusStrategyAll() {
        // graph with vertices
        final GenericGraph graph = createGraphBuilder().focus().all().apply().build();
        final List<VertexRef> vertexRefs = graph.getDefaultFocus().getVertexRefs();
        assertThat(vertexRefs, hasSize(graph.getVertices().size()));
        assertEquals(graph.getVertices(), graph.resolveVertexRefs(vertexRefs));

        // graph without vertices
        final GenericGraph build = createGraphBuilderEmpty().focus().all().apply().build();
        assertThat(build.getDefaultFocus().getVertexRefs(), hasSize(0));
    }

    @Test
    public void testFocusStrategyEmpty() {
        // graph with vertices
        final GenericGraph graph = createGraphBuilder().focus().empty().apply().build();
        assertEquals(Collections.emptyList(), graph.getDefaultFocus().getVertexRefs());

        // graph without vertices
        final GenericGraph emptyGraph = createGraphBuilderEmpty().build();
        assertEquals(Collections.emptyList(), emptyGraph.getDefaultFocus().getVertexRefs());
    }

    @Test
    public void testFocusStrategyFirst() {
        // graph with vertices
        final GenericGraph graph = createGraphBuilder().build();
        assertThat(graph.getDefaultFocus().getVertexRefs(), hasSize(1));

        // graph without vertices
        final GenericGraph emptyGraph = createGraphBuilderEmpty().focus().first().apply().build();
        assertEquals(Collections.emptyList(), emptyGraph.getDefaultFocus().getVertexRefs());
    }

    @Test
    public void testFocusStrategySpecific() {
        final List<GenericVertex> specificVertices = Arrays.asList(createVertex(), createVertex());
        final List<VertexRef> specifiedVertexRefs = specificVertices.stream().map(v -> v.getVertexRef()).collect(Collectors.toList());

        // graph with vertices
        final GenericGraph graph = createGraphBuilder()
                .addVertices(specificVertices)
                .focus().selection(specifiedVertexRefs).apply()
                .build();
        final List<VertexRef> vertexRefs = graph.getDefaultFocus().getVertexRefs();
        assertThat(vertexRefs, hasSize(2));
        assertEquals(specificVertices, graph.resolveVertexRefs(vertexRefs));

        // ask for an unknown vertex: unknown vertices are ignored
        final GenericGraph graph2 = createGraphBuilder()
                .focus().selection(new VertexRef("unknown", "alsoUnknown")).apply()
                .build();
        assertEquals(Collections.emptyList(), graph2.getDefaultFocus().getVertexRefs());

        // graph without vertices
        final GenericGraph emptyGraph = createGraphBuilderEmpty()
                .focus().selection(Collections.emptyList()).apply()
                .build();
        assertEquals(Collections.emptyList(), emptyGraph.getDefaultFocus().getVertexRefs());
    }

}