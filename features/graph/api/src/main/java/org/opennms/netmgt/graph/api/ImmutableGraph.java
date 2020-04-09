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

package org.opennms.netmgt.graph.api;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public interface ImmutableGraph<V extends Vertex, E extends Edge> extends GraphInfo {

    List<V> getVertices();

    List<E> getEdges();

    V getVertex(String id);

    E getEdge(String id);

    List<String> getVertexIds();

    List<String> getEdgeIds();

    ImmutableGraph<V, E> getView(Collection<V> verticesInFocus, int szl);

    List<V> resolveVertices(Collection<String> vertexIds);

    List<V> resolveVertices(NodeRef nodeRef);

    V resolveVertex(VertexRef vertexRef);

    List<E> resolveEdges(Collection<String> edgeIds);

    Collection<V> getNeighbors(V eachVertex);

    Collection<E> getConnectingEdges(V eachVertex);

    Focus getDefaultFocus();

    GenericGraph asGenericGraph();

}
