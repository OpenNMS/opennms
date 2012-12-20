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

package org.opennms.features.topology.api;

import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;

public class SimpleConnector implements Connector {

	private final String m_namespace;
	private final String m_id;
	private final Vertex m_vertex;
	private EdgeRef m_edge;

	/**
	 * @param namespace
	 * @param id
	 * @param vertex
	 */
	public SimpleConnector(String namespace, String id, Vertex vertex) {
		m_namespace = namespace;
		m_id = id;
		m_vertex = vertex;
	}

	/**
	 * @param namespace
	 * @param id
	 * @param vertex
	 * @param edge
	 */
	public SimpleConnector(String namespace, String id, Vertex vertex, EdgeRef edge) {
		this(namespace, id, vertex);
		m_edge = edge;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public EdgeRef getEdge() {
		return m_edge;
	}

	public void setEdge(EdgeRef edgeRef) {
		m_edge = edgeRef;
	}

	@Override
	public Vertex getVertex() {
		return m_vertex;
	}

}
