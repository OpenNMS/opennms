/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.jung;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.opennms.features.topology.app.internal.TestGraph;
import org.opennms.features.topology.app.internal.TestVertex;

public class HierarchyLayoutAlgorithmTest {

    // NMS-8703
    @Test
    public void verifyHierarchyLayoutFallBack() {
        // Vertices
        final List<Vertex> vertices = new ArrayList<>();
        vertices.add(new TestVertex("root"));
        vertices.add(new TestVertex("v1"));
        vertices.add(new TestVertex("v2"));
        vertices.add(new TestVertex("v3"));

        // Edges
        final List<Edge> edges = new ArrayList<>();
        edges.add(new AbstractEdge("test", "rootEdge", vertices.get(0), vertices.get(1)));
        edges.add(new AbstractEdge("test","e1", vertices.get(1), vertices.get(2)));
        edges.add(new AbstractEdge("test","e2", vertices.get(2), vertices.get(3)));

        // Circle
        edges.add(new AbstractEdge("test","e3", vertices.get(3), vertices.get(1)));

        // Mock ALL the things
        final GraphContainer mockGraphContainer = Mockito.mock(GraphContainer.class);
        final DefaultLayout layout = new DefaultLayout();
        final TestGraph testGraph = new TestGraph(layout, vertices, edges);
        Mockito.when(mockGraphContainer.getGraph()).thenReturn(testGraph);

        // Update layouts and ensure no exception is thrown
        new HierarchyLayoutAlgorithm().updateLayout(testGraph);
    }
}
