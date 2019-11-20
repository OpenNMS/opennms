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

package org.opennms.netmgt.graph.updates.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainGraph;
import org.opennms.netmgt.graph.simple.SimpleDomainVertex;

// TODO MVR add test for focus change
// TODO MVR add test for edge changes
public class ChangeSetTest {

    private static final String NAMESPACE = "test";

    @Test
    public void verifyNoChanges() {
        final SimpleDomainGraph graph = SimpleDomainGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build())
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v2").label("Vertex 2").build())
                .build();
        ChangeSet changeSet = new ChangeSet(graph, graph);
        assertThat(changeSet.hasChanges(), Matchers.is(false));

        changeSet = new ChangeSet(graph, SimpleDomainGraph.from(graph.asGenericGraph()));
        assertThat(changeSet.hasChanges(), Matchers.is(false));
    }

    // Verifies that when a complete new graph was added,
    // everything from the new graph is the change
    @Test
    public void verifyNoOldGraph() {
        final SimpleDomainGraph graph = SimpleDomainGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build())
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("v2").label("Vertex 2").build())
                .addEdge(SimpleDomainEdge.builder().namespace(NAMESPACE).id("e1")
                        .source(new VertexRef(NAMESPACE, "v1"))
                        .target(new VertexRef(NAMESPACE, "v2"))
                        .build())
                .build();
        final ChangeSet<SimpleDomainGraph, SimpleDomainVertex, SimpleDomainEdge> changeSet = new ChangeSet(null, graph);
        assertThat(changeSet.getNamespace(), Matchers.is(NAMESPACE));
        assertThat(changeSet.hasGraphInfoChanged(), Matchers.is(true));
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
        final SimpleDomainGraph oldGraph = SimpleDomainGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("1").build())
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("2").build())
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("3").build())
                .build();

        // Graph with vertices (3,4)
        final SimpleDomainGraph newGraph = SimpleDomainGraph.builder()
                .namespace(NAMESPACE)
                .label("Some Label")
                .description("Some Description")
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("3").label("Three").build())
                .addVertex(SimpleDomainVertex.builder().namespace(NAMESPACE).id("4").build())
                .build();

        // Detect changes
        // Changes are (removed vertices: 1, 2. Added Vertices: 4, Updated Vertices: 3
        final ChangeSet<SimpleDomainGraph, SimpleDomainVertex, SimpleDomainEdge> changeSet = new ChangeSet(oldGraph, newGraph);
        assertThat(changeSet.getNamespace(), Matchers.is(NAMESPACE)); // Ensure the namespace was detected successful

        // Verify change Flags
        assertThat(changeSet.hasGraphInfoChanged(), Matchers.is(true));
        assertThat(changeSet.getVerticesAdded(), Matchers.hasSize(1));
        assertThat(changeSet.getVerticesRemoved(), Matchers.hasSize(2));
        assertThat(changeSet.getVerticesUpdated(), Matchers.hasSize(1));
        assertThat(changeSet.getEdgesAdded(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesRemoved(), Matchers.hasSize(0));
        assertThat(changeSet.getEdgesUpdated(), Matchers.hasSize(0));

        // Verify changes
        assertEquals("4", changeSet.getVerticesAdded().get(0).getId());
        assertEquals("1", changeSet.getVerticesRemoved().get(0).getId());
        assertEquals("2", changeSet.getVerticesRemoved().get(1).getId());
        assertEquals("3", changeSet.getVerticesUpdated().get(0).getId());
        assertEquals(new DefaultGraphInfo(NAMESPACE, SimpleDomainVertex.class)
                .withDescription("Some Description")
                .withLabel("Some Label"), changeSet.getGraphInfo());
    }

    // Verifies that graphs from different namespaces cannot be detected.
    // In theory this might be possible, but it does not make sense from a domain point.
    @Test
    public void verifyNamespaceCannotChange() {
        final SimpleDomainGraph oldGraph = SimpleDomainGraph.builder().namespace(NAMESPACE).build();
        final SimpleDomainGraph newGraph = SimpleDomainGraph.builder().namespace(NAMESPACE + ".opennms").build();
        try {
            new ChangeSet(oldGraph, newGraph);
            fail("Expected an exception to be thrown, but succeeded. Bailing");
        } catch (IllegalStateException ex) {
            // expected, as namespace changes are not supported
        }
    }
}