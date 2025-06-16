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

import javax.xml.bind.annotation.XmlID;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.VertexRef;

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

	public SimpleConnector(String namespace, String id, VertexRef vertex) {
		this(namespace, id, namespace + ":" + id, vertex);
	}

	@Override
	public SimpleConnector clone() {
		SimpleConnector retval = new SimpleConnector(getNamespace(), getId(), getLabel(), getVertex());
		// This will infinite loop... so it's not a completely accurate clone()
		/*
		if (m_edge != null) {
			retval.setEdge(m_edge.clone());
		}
		 */
		return retval;
	}

	public SimpleConnector(String namespace, String id, String label, VertexRef vertex, AbstractEdge edge) {
		this(namespace, id, label, vertex);
		m_edge = edge;
	}

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
	public void setVertex(VertexRef vertex) {
		m_vertex = vertex;
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
