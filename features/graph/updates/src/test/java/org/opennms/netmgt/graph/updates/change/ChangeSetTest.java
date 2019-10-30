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
public class ChangeSetTest {

//    private static final String NAMESPACE = "simple";
//
//    // Verifies that when a complete new graph was added,
//    // everything from the new graph is the change
//    @Test
//    public void verifyNoOldGraph() {
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> newGraph = new SimpleGraph<>(NAMESPACE);
//        final ChangeSet changeSet = new ChangeSet(null, newGraph);
//        assertEquals(NAMESPACE, changeSet.getNamespace());
//
//        assertEquals(Boolean.TRUE, changeSet.hasGraphInfoChanged());
//        assertEquals(Boolean.TRUE, changeSet.getVerticesAdded().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getVerticesRemoved().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getVerticesUpdated().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getEdgesAdded().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getEdgesRemoved().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getEdgesUpdated().isEmpty());
//    }
//
//    // Verifies graph updates can be detected
//    @Test
//    public void verifyUpdate() {
//        // Graph with vertices (1,2,3)
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> oldGraph = new SimpleGraph<>(NAMESPACE);
//        oldGraph.addVertex(new SimpleVertex(NAMESPACE, "1"));
//        oldGraph.addVertex(new SimpleVertex(NAMESPACE, "2"));
//        oldGraph.addVertex(new SimpleVertex(NAMESPACE, "3"));
//
//        // Graph with vertices (3,4)
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> newGraph = new SimpleGraph<>(NAMESPACE);
//        newGraph.setDescription("Some Description");
//        newGraph.setLabel("Some Label");
//        newGraph.addVertex(new SimpleVertex(NAMESPACE, "3"));
//        newGraph.addVertex(new SimpleVertex(NAMESPACE, "4"));
//        newGraph.getVertex("3").setLabel("Three");
//
//        // Detect Changes
//        // Changes are (removed vertices: 1, 2. Added Vertices: 4, Updated Vertices: 3
//        final ChangeSet<SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>>, SimpleVertex, SimpleEdge<SimpleVertex>> changeSet = new ChangeSet(oldGraph, newGraph);
//        assertEquals(NAMESPACE, changeSet.getNamespace()); // Ensure the namespace was detected successful
//
//        // Verify Change Flags
//        assertEquals(Boolean.TRUE, changeSet.hasGraphInfoChanged());
//        assertEquals(Boolean.FALSE, changeSet.getVerticesAdded().isEmpty());
//        assertEquals(Boolean.FALSE, changeSet.getVerticesRemoved().isEmpty());
//        assertEquals(Boolean.FALSE, changeSet.getVerticesUpdated().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getEdgesAdded().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getEdgesRemoved().isEmpty());
//        assertEquals(Boolean.TRUE, changeSet.getEdgesUpdated().isEmpty());
//
//        // Verify changes
//        assertEquals("4", changeSet.getVerticesAdded().get(0).getId());
//        assertEquals("1", changeSet.getVerticesRemoved().get(0).getId());
//        assertEquals("2", changeSet.getVerticesRemoved().get(1).getId());
//        assertEquals("3", changeSet.getVerticesUpdated().get(0).getId());
//        assertEquals(new DefaultGraphInfo(NAMESPACE, SimpleVertex.class)
//                .withDescription("Some Description")
//                .withLabel("Some Label"), changeSet.getGraphInfo());
//    }
//
//    // Verifies that graphs from different namespaces cannot be detected.
//    // In theory this might be possible, but it does not make sense from a domain point.
//    @Test
//    public void verifyNamespaceCannotChange() {
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> oldGraph = new SimpleGraph<>(NAMESPACE);
//        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> newGraph = new SimpleGraph<>(NAMESPACE + ".opennms");
//        try {
//            new ChangeSet(oldGraph, newGraph);
//            fail("Expected an exception to be thrown, but succeeded. Bailing");
//        } catch (IllegalStateException ex) {
//            // expected, as namespace changes are not supported
//        }
//    }
}