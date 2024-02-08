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
