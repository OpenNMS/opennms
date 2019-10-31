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

// TODO MVR make this test work again
public class ContainerChangeSetTest {

//    private static final String NAMESPACE = "dummy";
//
//    @Test
//    public void verifyDetectChanges() {
//        // Define two graphs
//        // Graph with vertices (1,2,3)
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> graph1 = new SimpleGraph<>(NAMESPACE);
//        graph1.addVertex(new SimpleVertex(NAMESPACE, "1"));
//        graph1.addVertex(new SimpleVertex(NAMESPACE, "2"));
//        graph1.addVertex(new SimpleVertex(NAMESPACE, "3"));
//
//        // Graph with vertices (3,4)
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> graph2 = new SimpleGraph<>(NAMESPACE);
//        graph2.setDescription("Some Description");
//        graph2.setLabel("Some Label");
//        graph2.addVertex(new SimpleVertex(NAMESPACE, "3"));
//        graph2.addVertex(new SimpleVertex(NAMESPACE, "4"));
//        graph2.getVertex("3").setLabel("Three");
//
//        final SimpleGraphContainer oldGraphContainer = new SimpleGraphContainer("old-container");
//        final SimpleGraphContainer newGraphContainer = new SimpleGraphContainer("new-container");
//
//        /*
//         * Verify no changes
//         */
//        final Date changeSetDate = new Date();
//        ContainerChangeSet changeSet = new ContainerChangeSet(oldGraphContainer, newGraphContainer, changeSetDate);
//        Assert.assertEquals(Boolean.FALSE, changeSet.hasChanges());
//        Assert.assertEquals(changeSetDate, changeSet.getChangeSetDate());
//
//        /*
//         * Verify adding a graph
//         */
//        newGraphContainer.addGraph(graph2);
//        changeSet = new ContainerChangeSet(oldGraphContainer, newGraphContainer, changeSetDate);
//        Assert.assertEquals(Boolean.TRUE, changeSet.hasChanges());
//        Assert.assertSame(graph2, changeSet.getGraphsAdded().get(0));
//
//        /*
//         * Verify removing a graph
//         */
//        final Graph graph3 = new SimpleGraph<>(NAMESPACE + ".old");
//        oldGraphContainer.addGraph(graph3);
//        changeSet = new ContainerChangeSet(oldGraphContainer, newGraphContainer, changeSetDate);
//        Assert.assertEquals(Boolean.TRUE, changeSet.hasChanges());
//        Assert.assertSame(graph2, changeSet.getGraphsAdded().get(0));
//        Assert.assertSame(graph3, changeSet.getGraphsRemoved().get(0));
//
//        /*
//         * Verify updating a graph
//         */
//        oldGraphContainer.addGraph(graph1);
//        changeSet = new ContainerChangeSet(oldGraphContainer, newGraphContainer, changeSetDate);
//        Assert.assertEquals(Boolean.TRUE, changeSet.hasChanges());
//
//        // Get Graph changes and verify
//        final ChangeSet<?, ?, ?> graphChangeSet = changeSet.getGraphsUpdated().get(0);
//        assertEquals(NAMESPACE, graphChangeSet.getNamespace()); // Ensure the namespace was detected successful
//
//        // Verify Change Flags
//        assertEquals(Boolean.TRUE, graphChangeSet.hasGraphInfoChanged());
//        assertEquals(Boolean.FALSE, graphChangeSet.getVerticesAdded().isEmpty());
//        assertEquals(Boolean.FALSE, graphChangeSet.getVerticesRemoved().isEmpty());
//        assertEquals(Boolean.FALSE, graphChangeSet.getVerticesUpdated().isEmpty());
//        assertEquals(Boolean.TRUE, graphChangeSet.getEdgesAdded().isEmpty());
//        assertEquals(Boolean.TRUE, graphChangeSet.getEdgesRemoved().isEmpty());
//        assertEquals(Boolean.TRUE, graphChangeSet.getEdgesUpdated().isEmpty());
//
//        // Verify changes
//        assertEquals("4", graphChangeSet.getVerticesAdded().get(0).getId());
//        assertEquals("1", graphChangeSet.getVerticesRemoved().get(0).getId());
//        assertEquals("2", graphChangeSet.getVerticesRemoved().get(1).getId());
//        assertEquals("3", graphChangeSet.getVerticesUpdated().get(0).getId());
//        assertEquals(new DefaultGraphInfo(NAMESPACE, SimpleVertex.class)
//                .withDescription("Some Description")
//                .withLabel("Some Label"), graphChangeSet.getGraphInfo());
//    }


}
