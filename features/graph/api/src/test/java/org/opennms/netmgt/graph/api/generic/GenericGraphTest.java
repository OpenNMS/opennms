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

import static org.junit.Assert.assertTrue;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import org.junit.Test;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;

public class GenericGraphTest {

    @Test
    public void shouldRejectEdgesWithWrongNamespace(){
        GenericGraphBuilder graphBuilder = TestObjectCreator.createGraphBuilder();
        GenericVertex vertex = TestObjectCreator.createVertex();
        graphBuilder.addVertex(vertex);
        GenericVertex vertexWithOtherNamespace = TestObjectCreator.createVertex("unknownNamespace", "v1");

        final GenericEdge validEdge = TestObjectCreator.createEdge(vertex, vertexWithOtherNamespace);
        graphBuilder.addEdge(validEdge); // should throw no exception

        final GenericEdge invalidEdge = TestObjectCreator.createEdge("unknownNamespace", vertex, vertexWithOtherNamespace);
        assertThrowsException(IllegalArgumentException.class, () -> graphBuilder.addEdge(invalidEdge));
    }

    @Test
    public void shouldRejectEdgesWithUnknownVertices(){
        GenericGraphBuilder graphBuilder = TestObjectCreator.createGraphBuilder();
        GenericVertex vertex = TestObjectCreator.createVertex();
        final GenericEdge edge = TestObjectCreator.createEdge(vertex, vertex);

        // add an edge with unknown vertex => throws exception
        assertThrowsException(IllegalArgumentException.class, () -> graphBuilder.addEdge(edge));

        // now add the vertex and the exception should be avoided:
        graphBuilder.addVertex(vertex);
        graphBuilder.addEdge(edge); // should throw no exception
    }
    
    @Test
    public void shouldNotAllowNamespaceChangeAfterAddingElements(){
        GenericGraphBuilder graphBuilder =  GenericGraph.builder();
        
        // 1.) set namespace on "empty graph" => shouldn't be a problem
        graphBuilder.namespace("some namespace");
        graphBuilder.namespace(TestObjectCreator.NAMESPACE); // should be ok as long as we haven't added an edge
        
        // 2.) set same namespace on a filled graph => should be possible
        GenericVertex vertex = TestObjectCreator.createVertex();
        graphBuilder.addVertex(vertex);
        graphBuilder.addEdge(TestObjectCreator.createEdge(vertex, vertex));
        graphBuilder.namespace(TestObjectCreator.NAMESPACE);
        
        // 2.) set other namespace on a filled graph => should not be possible anymore
        String otherNamespace = graphBuilder.getNamespace()+"v2";   

        assertThrowsException(IllegalStateException.class, () -> graphBuilder.namespace(otherNamespace));
        assertThrowsException(IllegalStateException.class, () -> graphBuilder.property(GenericProperties.NAMESPACE, otherNamespace));
        assertThrowsException(IllegalStateException.class, () -> graphBuilder.properties(new MapBuilder<String, Object>()
                .withProperty(GenericProperties.NAMESPACE, otherNamespace).build()));
    }
}
