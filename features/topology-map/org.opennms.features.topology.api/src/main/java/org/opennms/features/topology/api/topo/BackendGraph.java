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
