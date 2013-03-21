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

import javax.xml.bind.annotation.XmlID;

public class SimpleConnector implements Connector {

	// Required
	private String m_namespace;
	// Required
	private String m_id;
	// Required
	private String m_label;
	// Required
	private VertexRef m_vertex;
	private AbstractEdge m_edge;

	/**
	 * No-arg constructor for JAXB.
	 */
	public SimpleConnector() {}

	/**
	 * @param namespace
	 * @param id
	 * @param label
	 * @param vertex
	 */
	public SimpleConnector(String namespace, String id, String label, VertexRef vertex) {
		if (namespace == null) {
			throw new IllegalArgumentException("Namespace is null");
		} else if (id == null) {
			throw new IllegalArgumentException("ID is null");
		} else if (label == null) {
			throw new IllegalArgumentException("Label is null");
		} else if (vertex == null) {
			throw new IllegalArgumentException("Vertex is null");
		}
		m_namespace = namespace;
		m_id = id;
		m_label = label;
		m_vertex = vertex;
	}

	/**
	 * @param namespace
	 * @param id
	 * @param vertex
	 */
	public SimpleConnector(String namespace, String id, VertexRef vertex) {
		this(namespace, id, namespace + ":" + id, vertex);
	}

	/**
	 * @param namespace
	 * @param id
	 * @param vertex
	 * @param edge
	 */
	public SimpleConnector(String namespace, String id, String label, VertexRef vertex, AbstractEdge edge) {
		this(namespace, id, label, vertex);
		m_edge = edge;
	}

	/**
	 * @param namespace
	 * @param id
	 * @param label
	 * @param vertex
	 * @param edge
	 */
	public SimpleConnector(String namespace, String id, VertexRef vertex, AbstractEdge edge) {
		this(namespace, id, namespace + ":" + id, vertex, edge);
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@XmlID
	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public String getLabel() {
		return m_label;
	}

	@Override
	public AbstractEdge getEdge() {
		return m_edge;
	}

	public void setEdge(AbstractEdge edgeRef) {
		m_edge = edgeRef;
	}

	@Override
	public VertexRef getVertex() {
		return m_vertex;
	}

	@Override
	public int compareTo(Ref o) {
		if (this.equals(o)) {
			return 0;
		} else {
			// Order by namespace, then ID
			if (this.getNamespace().equals(o.getNamespace())) {
				if (this.getId().equals(o.getId())) {
					// Shouldn't happen because equals() should return true
					throw new IllegalStateException("equals() was inaccurate in " + this.getClass().getName());
				} else {
					return this.getId().compareTo(o.getId());
				}
			} else {
				return this.getNamespace().compareTo(o.getNamespace());
			}
		}
	}
}
