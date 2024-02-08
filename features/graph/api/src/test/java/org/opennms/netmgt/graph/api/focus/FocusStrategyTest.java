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