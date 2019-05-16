/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.generic;

import static org.junit.Assert.*;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import org.junit.Test;

public class GenericGraphTest {

    @Test
    public void shouldCloneCorrectly() {
        GenericGraph original = TestObjectCreator.createGraph();
        GenericGraph clone = new GenericGraph(original);
        assertEquals(original, clone);
        assertNotSame(original, clone);
    }

    @Test
    public void shouldRejectEdgesWithWrongNamespace(){
        GenericGraph graph = TestObjectCreator.createGraph();
        GenericVertex vertex = TestObjectCreator.createVertex();
        graph.addVertex(vertex);
        GenericVertex vertexWithOtherNamespace = TestObjectCreator.createVertex("unknownNamespace", "v1");

        final GenericEdge validEdge = TestObjectCreator.createEdge(vertex, vertexWithOtherNamespace);
        graph.addEdge(validEdge); // should throw no exception

        final GenericEdge invalidEdge = TestObjectCreator.createEdge("unknownNamespace", vertex, vertexWithOtherNamespace);
        assertThrowsException(IllegalArgumentException.class, () -> graph.addEdge(invalidEdge));
    }

    @Test
    public void shouldRejectEdgesWithUnknownVertices(){
        GenericGraph graph = TestObjectCreator.createGraph();
        GenericVertex vertex = TestObjectCreator.createVertex();
        final GenericEdge edge = TestObjectCreator.createEdge(vertex, vertex);

        // add an edge with unknown vertex => throws exception
        assertThrowsException(IllegalArgumentException.class, () -> graph.addEdge(edge));

        // now add the vertex and the exception should be avoided:
        graph.addVertex(vertex);
        graph.addEdge(edge); // should throw no exception
    }
}
