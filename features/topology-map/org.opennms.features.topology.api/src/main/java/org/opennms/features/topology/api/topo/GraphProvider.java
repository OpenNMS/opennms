/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.topo;

import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

public interface GraphProvider extends VertexProvider, EdgeProvider {

	void save();

	void load(String filename) throws MalformedURLException, JAXBException;

    public void refresh();

	void resetContainer();

	void addVertices(Vertex... vertices);

	void removeVertex(VertexRef... vertexId);

	/**
	 * @deprecated Convert calls to this to addVertices
	 */
	Vertex addVertex(int x, int y);

	Vertex addGroup(String label, String iconKey);

	EdgeRef[] getEdgeIdsForVertex(VertexRef vertex);

	void addEdges(Edge... edges);

	void removeEdges(EdgeRef... edges);

        @Override
	boolean setParent(VertexRef vertexId, VertexRef parentId);

	Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId);

}
