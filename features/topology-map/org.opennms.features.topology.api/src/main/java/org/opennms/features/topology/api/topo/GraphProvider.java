/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.browsers.SelectionAware;

public interface GraphProvider extends VertexProvider, EdgeProvider, SelectionAware {

	void refresh();

	void resetContainer();

	void addVertices(Vertex... vertices);

	void removeVertex(VertexRef... vertexId);

	/**
	 * @deprecated Convert calls to this to addVertices
	 */
	Vertex addVertex(int x, int y);

	Vertex addGroup(String label, String iconKey);

	EdgeRef[] getEdgeIdsForVertex(VertexRef vertex);

	/**
	 * This function can be used for efficiency when you need the {@link EdgeRef}
	 * instances for a large number of vertices.
	 */
	Map<VertexRef,Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertex);

	void addEdges(Edge... edges);

	void removeEdges(EdgeRef... edges);

	Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId);

	Defaults getDefaults();

	TopologyProviderInfo getTopologyProviderInfo();
}
