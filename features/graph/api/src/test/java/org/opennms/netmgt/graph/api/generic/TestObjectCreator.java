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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;

public class TestObjectCreator {

    public final static String NAMESPACE = TestObjectCreator.class.getSimpleName();
    private final static AtomicInteger ID_SUPPLIER = new AtomicInteger(1);

    public static GenericVertex createVertex() {
        return createVertex(NAMESPACE, Integer.toString(ID_SUPPLIER.getAndIncrement()));
    }

    public static GenericVertex createVertex(String namespace, String id) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);
        return GenericVertex.builder()
        		.namespace(namespace)
        		.id(id)
        		.label("GenericVertex-" + namespace + "-" + id)
        		.build();
    }

    public static GenericEdge createEdge(GenericVertex sourceVertex, GenericVertex targetVertex) {
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);
        return createEdge(NAMESPACE, sourceVertex, targetVertex);
    }

    public static GenericEdge createEdge(String namespace, GenericVertex sourceVertex, GenericVertex targetVertex) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);
        GenericEdge edge = GenericEdge.builder()
        		.namespace(namespace)
        		.source(sourceVertex.getVertexRef())
        		.target(targetVertex.getVertexRef())
        		.label("GenericEdge-" + namespace + "-" + sourceVertex.getVertexRef() + "->" + targetVertex.getVertexRef())
        		.build();
        return edge;
    }

    public static GenericGraphBuilder createGraphBuilder() {
        GenericVertex vertex1 = createVertex();
        GenericVertex vertex2 = createVertex();
        GenericVertex vertex3 = createVertex();
        GenericEdge edge1 = createEdge(vertex1, vertex2);
        GenericEdge edge2 = createEdge(vertex1, vertex3);

        return createGraphBuilderEmpty()
            .addVertex(vertex1)
            .addVertex(vertex2)
            .addVertex(vertex3)
            .focus().first().apply()
            .addEdge(edge1)
            .addEdge(edge2);
    }

    public static GenericGraphBuilder createGraphBuilderEmpty() {
        final String id = UUID.randomUUID().toString();
        return GenericGraph.builder()
                .namespace(NAMESPACE)
                .id("GraphId" + id)
                .description("GraphDescription" + id)
                .label("GraphLabel" + id)
                .property("someProperty", "someProperty" + id);
    }
}
