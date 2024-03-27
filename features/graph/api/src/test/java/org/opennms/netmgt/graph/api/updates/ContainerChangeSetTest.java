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

import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;

public class ContainerChangeSetTest {

    private static final String CONTAINER_ID = "test";
    private static final String NAMESPACE = "dummy";

    @Test
    public void verifyDetectChanges() {
        // Define two graphs
        // Graph with vertices (1,2,3)
        final GenericGraph graph1 = GenericGraph.builder().namespace(NAMESPACE)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("1").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("2").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("3").build())
                .build();

        // Graph with vertices (3,4)
        final GenericGraph graph2 = GenericGraph.builder().namespace(NAMESPACE)
                .description("Some Description")
                .label("Some Label")
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("3").label("Three").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("4").build())
                .build();

        /*
         * Verify no changes
         */
        final Date changeSetDate = new Date();
        ContainerChangeSet changeSet = ContainerChangeSet.builder(
                GenericGraphContainer.builder().id(CONTAINER_ID).build(),
                GenericGraphContainer.builder().id(CONTAINER_ID).build(),
                changeSetDate).build();
        assertEquals(Boolean.FALSE, changeSet.hasChanges());
        assertEquals(changeSetDate, changeSet.getChangeSetDate());

        /*
         * Verify adding a graph
         */
        final GenericGraphContainer containerWithGraph2 = GenericGraphContainer.builder().id(CONTAINER_ID)
                .addGraph(graph2)
                .build();
        changeSet = ContainerChangeSet.builder(GenericGraphContainer.builder().id(CONTAINER_ID).build(), containerWithGraph2).build();
        assertThat(changeSet.hasChanges(), Matchers.is(true));
        assertThat(changeSet.getGraphsUpdated(), Matchers.hasSize(0));
        assertThat(changeSet.getGraphsRemoved(), Matchers.hasSize(0));
        assertEquals(graph2, changeSet.getGraphsAdded().get(0));

        /*
         * Verify removing a graph
         */
        final GenericGraph graph3 = GenericGraph.builder().namespace(NAMESPACE + ".old").build();
        final GenericGraphContainer containerWithGraph3 = GenericGraphContainer.builder()
                .id(CONTAINER_ID)
                .addGraph(graph3)
                .build();
        changeSet = ContainerChangeSet.builder(containerWithGraph3, containerWithGraph2).build();
        assertThat(changeSet.hasChanges(), Matchers.is(true));
        assertThat(changeSet.getGraphsUpdated(), Matchers.hasSize(0));
        assertEquals(graph2, changeSet.getGraphsAdded().get(0));
        assertEquals(graph3, changeSet.getGraphsRemoved().get(0));

        /*
         * Verify updating a graph
         */
        final GenericGraphContainer containerWithGraph1 = GenericGraphContainer.builder()
                .id(CONTAINER_ID)
                .addGraph(graph1)
                .build();
        changeSet = ContainerChangeSet.builder(containerWithGraph1, containerWithGraph2).build();
        assertThat(changeSet.hasChanges(), Matchers.is(true));
        assertThat(changeSet.getGraphsUpdated(), Matchers.hasSize(1));
        assertThat(changeSet.getGraphsAdded(), Matchers.hasSize(0));
        assertThat(changeSet.getGraphsRemoved(), Matchers.hasSize(0));

        // Get Graph changes and verify verify briefly.
        // For more detailed test check out the ChangeSetTest
        final ChangeSet<?, ?, ?> graphChangeSet = changeSet.getGraphsUpdated().get(0);
        assertEquals(NAMESPACE, graphChangeSet.getNamespace()); // Ensure the namespace was detected successful

        // Verify Change Flags
        assertEquals(Boolean.TRUE, graphChangeSet.hasGraphInfoChanged());
        assertEquals(Boolean.FALSE, graphChangeSet.getVerticesAdded().isEmpty());
        assertEquals(Boolean.FALSE, graphChangeSet.getVerticesRemoved().isEmpty());
        assertEquals(Boolean.FALSE, graphChangeSet.getVerticesUpdated().isEmpty());
        assertEquals(Boolean.TRUE, graphChangeSet.getEdgesAdded().isEmpty());
        assertEquals(Boolean.TRUE, graphChangeSet.getEdgesRemoved().isEmpty());
        assertEquals(Boolean.TRUE, graphChangeSet.getEdgesUpdated().isEmpty());

        // Verify changes
        assertEquals("4", graphChangeSet.getVerticesAdded().get(0).getId());
        assertEquals("1", graphChangeSet.getVerticesRemoved().get(0).getId());
        assertEquals("2", graphChangeSet.getVerticesRemoved().get(1).getId());
        assertEquals("3", graphChangeSet.getVerticesUpdated().get(0).getId());
        assertEquals(new DefaultGraphInfo(NAMESPACE)
                .withDescription("Some Description")
                .withLabel("Some Label"), graphChangeSet.getGraphInfo());
    }

    // Verifies that changes from different containers cannot be detected.
    // In theory this might be possible, but it does not make sense from a domain point.
    @Test
    public void verifyContainerIdCannotChange() {
        final GenericGraphContainer oldContainer = GenericGraphContainer.builder().id(CONTAINER_ID).build();
        final GenericGraphContainer newContainer = GenericGraphContainer.builder().id(CONTAINER_ID + ".opennms").build();
        try {
            ContainerChangeSet.builder(oldContainer, newContainer).build();
            fail("Expected an exception to be thrown, but succeeded. Bailing");
        } catch (IllegalStateException ex) {
            // expected, as container id changes are not supported
        }
    }
}
