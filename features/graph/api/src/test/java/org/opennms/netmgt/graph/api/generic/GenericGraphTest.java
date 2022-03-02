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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.focus.FocusStrategy;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;
import org.opennms.netmgt.graph.api.validation.exception.InvalidNamespaceException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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
    public void shouldRejectEdgeWhereBothVerticesAreUnknown(){
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
    public void shouldAcceptEdgeWhereOneVertexIsUnknownFromDifferentNamespace() {
        final GenericGraphBuilder graphBuilder = TestObjectCreator.createGraphBuilder();
        final GenericVertex unknownVertex = GenericVertex.builder().namespace(TestObjectCreator.NAMESPACE + "2").id("V1000").label("Vertex 1000").build();
        final GenericVertex knownVertex = graphBuilder.getVertices().get(0);
        final GenericEdge edge1 = TestObjectCreator.createEdge(knownVertex, unknownVertex);
        final GenericEdge edge2 = TestObjectCreator.createEdge(knownVertex, unknownVertex);

        // Should work
        graphBuilder.addEdge(edge1);

        // Should work
        graphBuilder.removeEdge(edge1);
        graphBuilder.addEdge(edge2);

        // Should also work
        graphBuilder.addEdge(edge1);
    }

    @Test
    public void shouldRejectEdgeWhereOneVertexIsUnknownFromSameNamespace() {
        final GenericGraphBuilder graphBuilder = TestObjectCreator.createGraphBuilder();
        final GenericVertex unknownVertex = TestObjectCreator.createVertex();
        final GenericVertex knownVertex = graphBuilder.getVertices().get(0);
        final GenericEdge edge1 = TestObjectCreator.createEdge(knownVertex, unknownVertex);

        // add an edge with unknown vertex => throws exception
        assertThrowsException(IllegalArgumentException.class, () -> graphBuilder.addEdge(edge1));
    }
    
    @Test
    public void shouldNotAllowNamespaceChangeAfterAddingElements(){
        GenericGraphBuilder graphBuilder = GenericGraph.builder();
        
        // 1.) set namespace on "empty graph" => shouldn't be a problem
        graphBuilder.namespace("some-namespace");
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
        assertThrowsException(IllegalStateException.class, () -> graphBuilder.properties(ImmutableMap.of(GenericProperties.NAMESPACE, otherNamespace)));
    }

    @Test
    public void shouldConsiderSemanticZoomLevel() {
        final String namespace = "dummy";
        final Focus defaultFocus = new Focus(FocusStrategy.SELECTION, Lists.newArrayList(new VertexRef(namespace, "v1")));
        final GenericGraph graph = GenericGraph.builder()
                .namespace(namespace)
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1.1").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1.2").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1.1.1").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1.1.2").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1.2.1").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1.2.2").build())
                .addEdge(GenericEdge.builder().namespace(namespace).source(new VertexRef(namespace, "v1")).target(new VertexRef(namespace, "v1.1")).build())
                .addEdge(GenericEdge.builder().namespace(namespace).source(new VertexRef(namespace, "v1")).target(new VertexRef(namespace, "v1.2")).build())
                .addEdge(GenericEdge.builder().namespace(namespace).source(new VertexRef(namespace, "v1.1")).target(new VertexRef(namespace, "v1.1.1")).build())
                .addEdge(GenericEdge.builder().namespace(namespace).source(new VertexRef(namespace, "v1.1")).target(new VertexRef(namespace, "v1.1.2")).build())
                .addEdge(GenericEdge.builder().namespace(namespace).source(new VertexRef(namespace, "v1.2")).target(new VertexRef(namespace, "v1.2.1")).build())
                .addEdge(GenericEdge.builder().namespace(namespace).source(new VertexRef(namespace, "v1.2")).target(new VertexRef(namespace, "v1.2.2")).build())
                .focus(defaultFocus)
                .build();
        final List<GenericVertex> genericVertices = graph.resolveVertices(defaultFocus.getVertexIds());

        // Verify if no szl is defined, a graph only containing the default focus is returned
        ImmutableGraph<GenericVertex, GenericEdge> view = graph.getView(genericVertices, 0);
        assertNotNull(view);
        assertThat(view.getVertices(), Matchers.hasSize(1));
        assertThat(view.getEdges(), Matchers.hasSize(0));
        assertThat(view.getVertexIds(), Matchers.hasItems("v1"));

        // If provided vertices do not exist, an empty graph is returned instead
        view = graph.getView(Lists.newArrayList(GenericVertex.builder().namespace(namespace).id("UNKNOWN").build()), 0);
        assertNotNull(view);
        assertThat(view.getVertices(), Matchers.hasSize(0));
        assertThat(view.getEdges(), Matchers.hasSize(0));

        // Verify szl 1
        view = graph.getView(genericVertices, 1);
        assertThat(view.getVertices(), Matchers.hasSize(3));
        assertThat(view.getEdges(), Matchers.hasSize(2));
        assertThat(view.getVertexIds(), Matchers.hasItems("v1", "v1.1", "v1.2"));

        // Verify szl 2
        view = graph.getView(genericVertices, 2);
        assertThat(view.getVertices(), Matchers.hasSize(7));
        assertThat(view.getEdges(), Matchers.hasSize(6));
        assertThat(view.getVertexIds(), Matchers.hasItems("v1", "v1.1", "v1.2", "v1.1.1", "v1.1.2", "v1.2.1", "v1.2.2"));
    }

    @Test
    public void shouldResolveVerticesWithNodeRef() {
        final NodeRef nodeRef = NodeRef.from("test:node1");
        final String namespace = "dummy";
        final GenericGraph graph = GenericGraph.builder().namespace(namespace)
                .addVertex(GenericVertex.builder().namespace(namespace).id("v1").nodeRef("test", "node1").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v2").nodeRef("test", "node1").build())
                .addVertex(GenericVertex.builder().namespace(namespace).id("v3").build())
                .build();
        final List<GenericVertex> vertices = graph.resolveVertices(nodeRef);
        assertThat(vertices, Matchers.hasSize(2));
        assertThat(vertices, Matchers.hasItems(graph.getVertex("v1"), graph.getVertex("v2")));

        final GenericGraph emptyGraph = GenericGraph.builder().namespace(namespace).build();
        assertThat(emptyGraph.resolveVertices(nodeRef), Matchers.hasSize(0));
    }

    @Test
    public void verifyCannotSetInvalidNamespace() {
        assertThrowsException(InvalidNamespaceException.class, () -> GenericVertex.builder().namespace("$invalid$").build());
    }
}
