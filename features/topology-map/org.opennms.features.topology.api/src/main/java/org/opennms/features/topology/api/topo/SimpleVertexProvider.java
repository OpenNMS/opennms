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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.LoggerFactory;

public class SimpleVertexProvider implements VertexProvider {

	private final String m_namespace;
	private final Map<String,Vertex> m_vertexMap = new LinkedHashMap<String,Vertex>();
	private final Set<VertexListener> m_listeners = new CopyOnWriteArraySet<VertexListener>();

	public SimpleVertexProvider(String namespace) {
		m_namespace = namespace;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public boolean contributesTo(String namespace) {
		return false;
	}

	@Override
	public Vertex getVertex(String namespace, String id) {
		return getVertex(new DefaultVertexRef(namespace, id));
	}

	@Override
	public Vertex getVertex(VertexRef reference, Criteria... criteria) {
		return getSimpleVertex(reference);
	}

	@Override
	// TODO MVR ??
	public List<Vertex> getVertices(CollapsibleRef collapsibleRef, Criteria... criteria) {
		return new ArrayList<>();
	}

	private Vertex getSimpleVertex(VertexRef reference) {
		if (reference != null && getNamespace().equals(reference.getNamespace())) {
			return m_vertexMap.get(reference.getId());
		}
		return null;
	}

	@Override
	public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		for(VertexRef ref : references) {
			Vertex vertex = getSimpleVertex(ref);
			if (vertex != null) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	private void fireVertexSetChanged() {
		for(VertexListener listener : m_listeners) {
			listener.vertexSetChanged(this);
		}
	}

	private void fireVerticesAdded(Collection<Vertex> vertices) {
		for(VertexListener listener : m_listeners) {
			listener.vertexSetChanged(this, vertices, null, null);
		}
	}

	private void fireVerticesRemoved(List<? extends VertexRef> all) {
		List<String> ids = new ArrayList<String>(all.size());
		for(VertexRef vertex : all) {
			ids.add(vertex.getId());
		}
		for(VertexListener listener : m_listeners) {
			listener.vertexSetChanged(this, null, null, ids);
		}
	}

	@Override
	public void addVertexListener(VertexListener vertexListener) {
		m_listeners.add(vertexListener);
	}

	@Override
	public void removeVertexListener(VertexListener vertexListener) {
		m_listeners.remove(vertexListener);
	}

	private void removeVertices(List<? extends VertexRef> vertices) {
		for(VertexRef vertex : vertices) {
			LoggerFactory.getLogger(this.getClass()).trace("Removing vertex: {}", vertex);
			// Remove the vertex from the main map
			m_vertexMap.remove(vertex.getId());
		}
	}

	private void addVertices(Collection<Vertex> vertices) {
		for(Vertex vertex : vertices) {
			if (vertex.getNamespace() == null || vertex.getId() == null) {
				LoggerFactory.getLogger(this.getClass()).warn("Discarding invalid vertex: {}", vertex);
				continue;
			}
			LoggerFactory.getLogger(this.getClass()).trace("Adding vertex: {}", vertex);
			m_vertexMap.put(vertex.getId(), vertex);
		}
	}

	public void setVertices(List<Vertex> vertices) {
		clearVertices();
		addVertices(vertices);
		fireVertexSetChanged();
	}

	public void add(Vertex...vertices) {
		add(Arrays.asList(vertices));
	}

	public void add(Collection<Vertex> vertices) {
		addVertices(vertices);
		fireVerticesAdded(vertices);
	}

	public void remove(List<VertexRef> vertices) {
		removeVertices(vertices);
		fireVerticesRemoved(vertices);
	}

	public void remove(VertexRef... vertices) {
		remove(Arrays.asList(vertices));
	}

	@Override
	public List<Vertex> getVertices(Criteria... criteria) {
		// TODO: Change code to properly filter on Criteria
		return Collections.unmodifiableList(new ArrayList<>(m_vertexMap.values()));
	}

	@Override
	public int getSemanticZoomLevel(VertexRef vertex) {
		// TODO MVR ??
		return 0;
//		Vertex parent = getParent(vertex);
//		return parent == null ? 0 : 1 + getSemanticZoomLevel(parent);
	}

	@Override
	public void clearVertices() {
		List<? extends Vertex> all = getVertices();
		removeVertices(all);
		fireVerticesRemoved(all);
	}

    @Override
    public int getVertexTotalCount() {
        return m_vertexMap.size();
    }

    /**
	 * @deprecated You should search by the namespace and ID tuple instead
	 */
	@Override
	public boolean containsVertexId(String id) {
		return containsVertexId(new DefaultVertexRef(getNamespace(), id));
	}

	@Override
	public boolean containsVertexId(VertexRef id, Criteria... criteria) {
		return getVertex(id, criteria) != null;
	}

}
