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
package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.NamespaceAware;

/**
 * Graph interface which are provided by a {@link org.opennms.features.topology.api.topo.GraphProvider}.
 * Name is {@link BackendGraph} as there is already a {@link org.opennms.features.topology.api.Graph} used in the UI.
 */
public interface BackendGraph extends NamespaceAware {

    /**
     * @deprecated Use {@link #containsVertexId(VertexRef, Criteria...)} instead.
     */
    @Deprecated
    boolean containsVertexId(String id);

    boolean containsVertexId(VertexRef id, Criteria... criteria);

    Vertex getVertex(String namespace, String id);

    Vertex getVertex(VertexRef reference, Criteria... criteria);

    /**
     * Return an immutable list of vertices that match the criteria.
     */
    List<Vertex> getVertices(Criteria... criteria);

    List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria);

    void addVertexListener(VertexListener vertexListener);

    void removeVertexListener(VertexListener vertexListener);

    void clearVertices();

    int getVertexTotalCount();

    void addVertices(Vertex... vertices);

    void removeVertex(VertexRef... vertexId);

    Edge getEdge(String namespace, String id);

    Edge getEdge(EdgeRef reference);

    /**
     * Return an immutable list of edges that match the criteria.
     */
    List<Edge> getEdges(Criteria... criteria);

    /**
     * Return an immutable list of all edges that match this set of references.
     */
    List<Edge> getEdges(Collection<? extends EdgeRef> references);

    void addEdgeListener(EdgeListener listener);

    void removeEdgeListener(EdgeListener listener);

    void clearEdges();

    int getEdgeTotalCount();

    EdgeRef[] getEdgeIdsForVertex(VertexRef vertex);

    /**
     * This function can be used for efficiency when you need the {@link EdgeRef}
     * instances for a large number of vertices.
     */
    Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertices);

    void addEdges(Edge... edges);

    void removeEdges(EdgeRef... edges);

    Edge connectVertices(String edgeId, VertexRef sourceVertextId, VertexRef targetVertextId);

    void resetContainer();
}
