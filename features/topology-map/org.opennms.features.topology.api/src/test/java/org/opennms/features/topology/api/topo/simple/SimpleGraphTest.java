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
package org.opennms.features.topology.api.topo.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleGraphTest {

    @Test
    public void testSimpleGraphCreation() {
        final SimpleGraph graph = new SimpleGraph("test");
        for (int i=0; i<10; i++) {
            graph.addVertices(new SimpleLeafVertex(graph.getNamespace(), "v" + i, 0, i));
        }
        for (int i=0; i<2; i++) {
            graph.addEdges(new AbstractEdge("test", "e" + i, graph.getVertices().get(i), graph.getVertices().get(i + 1)));
        }
        assertEquals(10, graph.getVertexTotalCount());
        assertEquals(10, graph.getVertices().size());
        assertEquals(2, graph.getEdgeTotalCount());
        assertEquals(2,  graph.getEdges().size());
    }

    @Test
    public void test() throws Exception {
        final BackendGraph simpleGraph = new SimpleGraph("test");
        assertEquals(0, simpleGraph.getVertices().size());

        final SimpleLeafVertex vertexA = new SimpleLeafVertex(simpleGraph.getNamespace(), "v0", 50, 100);
        vertexA.setIpAddress("10.0.0.4");
        simpleGraph.addVertices(vertexA);
        assertEquals(1, simpleGraph.getVertices().size());
        assertTrue(simpleGraph.containsVertexId(vertexA));
        assertTrue(simpleGraph.containsVertexId("v0"));
        assertFalse(simpleGraph.containsVertexId("v1"));
        final String namespace = simpleGraph.getNamespace();
        VertexRef ref0 = new DefaultVertexRef(namespace, "v0", namespace + ":v0");
        VertexRef ref1 = new DefaultVertexRef(namespace, "v1", namespace + ":v0");
        assertEquals(1, simpleGraph.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(0, simpleGraph.getVertices(Collections.singletonList(ref1)).size());

        final Vertex vertexB = new SimpleLeafVertex(simpleGraph.getNamespace(), "v1", 100, 50);
        simpleGraph.addVertices(vertexB);
        assertTrue(simpleGraph.containsVertexId(vertexB));
        assertTrue(simpleGraph.containsVertexId("v1"));
        assertEquals(1, simpleGraph.getVertices(Collections.singletonList(ref1)).size());

        final Vertex vertexC = new SimpleLeafVertex(simpleGraph.getNamespace(), "v2", 100, 150);
        final Vertex vertexD = new SimpleLeafVertex(simpleGraph.getNamespace(), "v3", 150, 100);
        final Vertex vertexE = new SimpleLeafVertex(simpleGraph.getNamespace(), "v4", 200, 200);
        simpleGraph.addVertices(vertexC, vertexD, vertexE);
        assertEquals(5, simpleGraph.getVertices().size());

        simpleGraph.connectVertices("e0", vertexA, vertexB);
        simpleGraph.connectVertices("e1", vertexA, vertexC);
        simpleGraph.connectVertices("e2", vertexB, vertexC);
        simpleGraph.connectVertices("e3", vertexB, vertexD);
        simpleGraph.connectVertices("e4", vertexC, vertexD);
        simpleGraph.connectVertices("e5", vertexA, vertexE);
        simpleGraph.connectVertices("e6", vertexD, vertexE);

        assertEquals(1, simpleGraph.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(1, simpleGraph.getVertices(Collections.singletonList(ref1)).size());
        assertEquals(5, simpleGraph.getVertices().size());
        assertEquals(3, simpleGraph.getEdgeIdsForVertex(simpleGraph.getVertex(ref0)).length);
        assertEquals(3, simpleGraph.getEdgeIdsForVertex(simpleGraph.getVertex(ref1)).length);

        simpleGraph.resetContainer();

        // Ensure that the topology provider has been erased
        assertEquals(0, simpleGraph.getVertices(Collections.singletonList(ref0)).size());
        assertEquals(0, simpleGraph.getVertices(Collections.singletonList(ref1)).size());
        assertEquals(0, simpleGraph.getVertices().size());
        assertEquals(0, simpleGraph.getEdgeIdsForVertex(simpleGraph.getVertex(ref0)).length);
        assertEquals(0, simpleGraph.getEdgeIdsForVertex(simpleGraph.getVertex(ref1)).length);
    }

    @Test
    public void testConnectVertices() {
        final BackendGraph simpleGraph = new SimpleGraph("test");
        simpleGraph.addVertices(new SimpleLeafVertex(simpleGraph.getNamespace(), "v0", 0, 0));
        assertEquals(1, simpleGraph.getVertices().size());

        simpleGraph.addVertices(new SimpleLeafVertex(simpleGraph.getNamespace(), "v1", 0, 0));
        assertEquals(2, simpleGraph.getVertices().size());

        final VertexRef vertex0 = simpleGraph.getVertex(simpleGraph.getNamespace(), "v0");
        final VertexRef vertex1 = simpleGraph.getVertex(simpleGraph.getNamespace(), "v1");
        final Edge edgeId = simpleGraph.connectVertices("e0", vertex0, vertex1);
        assertEquals(1, simpleGraph.getEdges().size());
        SimpleLeafVertex sourceLeafVert = (SimpleLeafVertex) edgeId.getSource().getVertex();
        SimpleLeafVertex targetLeafVert = (SimpleLeafVertex) edgeId.getTarget().getVertex();

        assertEquals("v0", sourceLeafVert.getId());
        assertEquals("v1", targetLeafVert.getId());

        final EdgeRef[] edgeIds = simpleGraph.getEdgeIdsForVertex(vertex0);
        assertEquals(1, edgeIds.length);
        assertEquals(edgeId, edgeIds[0]);
    }

    @Test
    public void testVertexListener() {
        final SimpleGraph simpleGraph = new SimpleGraph("test");
        final AtomicInteger eventsReceived = new AtomicInteger(0);
        simpleGraph.addVertexListener(new VertexListener() {
            @Override
            public void vertexSetChanged(BackendGraph graph, Collection<? extends Vertex> added, Collection<? extends Vertex> update, Collection<String> removedVertexIds) {
                eventsReceived.incrementAndGet();
            }

            @Override
            public void vertexSetChanged(BackendGraph graph) {
                eventsReceived.incrementAndGet();
            }
        });

        simpleGraph.addVertices(new AbstractVertex(simpleGraph.getNamespace(), "v0"));
        simpleGraph.addVertices(new AbstractVertex(simpleGraph.getNamespace(), "v1"));
        assertEquals(2, eventsReceived.get());
    }
}
