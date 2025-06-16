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
package org.opennms.netmgt.graph.api.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;

public class ChangeSetTest {

    private static final String NAMESPACE = "test";

    @Test
    public void verifyNoChanges() {
        final GenericGraph graph = GenericGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v2").label("Vertex 2").build())
                .build();
        ChangeSet changeSet = ChangeSet.builder(graph, graph).build();
        assertThat(changeSet.hasChanges(), Matchers.is(false));

        changeSet = ChangeSet.builder(graph, GenericGraph.from(graph).build()).build();
        assertThat(changeSet.hasChanges(), Matchers.is(false));
    }

    // Verifies that when a complete new graph was added,
    // everything from the new graph is the change
    @Test
    public void verifyNoOldGraph() {
        final GenericGraph graph = GenericGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v2").label("Vertex 2").build())
                .addEdge(GenericEdge.builder().namespace(NAMESPACE).id("e1")
                        .source(new VertexRef(NAMESPACE, "v1"))
                        .target(new VertexRef(NAMESPACE, "v2"))
                        .build())
                .build();
        final ChangeSet<GenericGraph, GenericVertex, GenericEdge> changeSet = ChangeSet.builder(null, graph).build();
        assertThat(changeSet.getNamespace(), Matchers.is(NAMESPACE));
        assertThat(changeSet.hasGraphInfoChanged(), Matchers.is(true));
        assertThat(changeSet.hasFocusChanged(), Matchers.is(false));
        assertThat(changeSet.getVerticesAdded(), Matchers.hasSize(2));
        assertThat(changeSet.getVerticesRemoved(), Matchers.hasSize(0));
        assertThat(changeSet.getVerticesUpdated(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesAdded(), Matchers.hasSize(1));
        assertThat(changeSet.getEdgesRemoved(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesUpdated(), Matchers.hasSize(0));
        assertThat(changeSet.getGraphInfo(), Matchers.not(Matchers.is(Matchers.nullValue())));
    }

    // Verifies graph updates can be detected
    @Test
    public void verifyUpdate() {
        // Graph with vertices (1,2,3)
        final GenericGraph oldGraph = GenericGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("1").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("2").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("3").build())
                .addEdge(GenericEdge.builder().namespace(NAMESPACE).id("e1").source(NAMESPACE, "1").target(NAMESPACE, "2").build())
                .addEdge(GenericEdge.builder().namespace(NAMESPACE).id("e2").source(NAMESPACE, "2").target(NAMESPACE, "3").build())
                .build();

        // Graph with vertices (3,4)
        final GenericGraph newGraph = GenericGraph.builder()
                .namespace(NAMESPACE)
                .label("Some Label")
                .description("Some Description")
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("3").label("Three").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("4").build())
                .addEdge(GenericEdge.builder().namespace(NAMESPACE).id("e2").source(NAMESPACE, "4").target(NAMESPACE, "3").build())
                .addEdge(GenericEdge.builder().namespace(NAMESPACE).id("e3").source(NAMESPACE, "3").target(NAMESPACE, "4").build())
                .build();

        // Detect changes
        // Changes are (removed vertices: 1, 2. Added Vertices: 4, Updated Vertices: 3
        final ChangeSet<GenericGraph, GenericVertex, GenericEdge> changeSet = ChangeSet.builder(oldGraph, newGraph).build();
        assertThat(changeSet.getNamespace(), Matchers.is(NAMESPACE)); // Ensure the namespace was detected successful

        // Verify change Flags
        assertThat(changeSet.hasGraphInfoChanged(), Matchers.is(true));
        assertThat(changeSet.hasFocusChanged(), Matchers.is(false));
        assertThat(changeSet.getVerticesAdded(), Matchers.hasSize(1));
        assertThat(changeSet.getVerticesRemoved(), Matchers.hasSize(2));
        assertThat(changeSet.getVerticesUpdated(), Matchers.hasSize(1));
        assertThat(changeSet.getEdgesRemoved(), Matchers.hasSize(1));
        assertThat(changeSet.getEdgesUpdated(), Matchers.hasSize(1));
        assertThat(changeSet.getEdgesAdded(), Matchers.hasSize(1));

        // Verify changes
        assertEquals("4", changeSet.getVerticesAdded().get(0).getId());
        assertEquals("1", changeSet.getVerticesRemoved().get(0).getId());
        assertEquals("2", changeSet.getVerticesRemoved().get(1).getId());
        assertEquals("3", changeSet.getVerticesUpdated().get(0).getId());
        assertEquals("e3", changeSet.getEdgesAdded().get(0).getId());
        assertEquals("e1", changeSet.getEdgesRemoved().get(0).getId());
        assertEquals("e2", changeSet.getEdgesUpdated().get(0).getId());
        assertEquals(new DefaultGraphInfo(NAMESPACE)
                .withDescription("Some Description")
                .withLabel("Some Label"), changeSet.getGraphInfo());
    }

    @Test
    public void verifyFocusUpdate() {
        final GenericGraph.GenericGraphBuilder builder = GenericGraph.builder()
                .namespace(NAMESPACE)
                .label("Dummy Graph")
                .description("A simple dummy graph")
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build());
        builder.focus().empty().apply();
        final GenericGraph graph1 = builder.build();
        builder.focus().all().apply();
        final GenericGraph graph2 = builder.build();

        // Detect changes
        final ChangeSet<GenericGraph, GenericVertex, GenericEdge> changeSet = ChangeSet.builder(graph1, graph2).build();

        // Verify change flags
        assertThat(changeSet.hasChanges(), Matchers.is(true));
        assertThat(changeSet.hasFocusChanged(), Matchers.is(true));
        assertThat(changeSet.hasGraphInfoChanged(), Matchers.is(false));
        assertThat(changeSet.getVerticesAdded(), Matchers.hasSize(0));
        assertThat(changeSet.getVerticesRemoved(), Matchers.hasSize(0));
        assertThat(changeSet.getVerticesUpdated(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesAdded(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesRemoved(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesUpdated(), Matchers.hasSize(0));

        // Verify changes
        assertThat(changeSet.getFocus().getVertexIds(), Matchers.hasItem("v1"));
    }

    // Verifies that graphs from different namespaces cannot be detected.
    // In theory this might be possible, but it does not make sense from a domain point.
    @Test
    public void verifyNamespaceCannotChange() {
        final GenericGraph oldGraph = GenericGraph.builder().namespace(NAMESPACE).build();
        final GenericGraph newGraph = GenericGraph.builder().namespace(NAMESPACE + ".opennms").build();
        try {
            ChangeSet.builder(oldGraph, newGraph).build();
            fail("Expected an exception to be thrown, but succeeded. Bailing");
        } catch (IllegalStateException ex) {
            // expected, as namespace changes are not supported
        }
    }
}