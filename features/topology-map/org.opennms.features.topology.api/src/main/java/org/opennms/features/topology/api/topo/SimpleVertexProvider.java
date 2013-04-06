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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.LoggerFactory;

public class SimpleVertexProvider implements VertexProvider {

	private final String m_namespace;
	private final Map<String,Vertex> m_vertexMap = new LinkedHashMap<String,Vertex>();
	private final Set<VertexListener> m_listeners = new CopyOnWriteArraySet<VertexListener>();
	private final Map<VertexRef, VertexRef> m_parents= new HashMap<VertexRef, VertexRef>();
	private final Map<VertexRef, Set<VertexRef>> m_children = new HashMap<VertexRef, Set<VertexRef>>();

	public SimpleVertexProvider(String namespace) {
		m_namespace = namespace;
	}

	@Override
	public String getVertexNamespace() {
		return m_namespace;
	}

	@Override
	public boolean contributesTo(String namespace) {
		return false;
	}

	@Override
	public Vertex getVertex(String namespace, String id) {
		if (getVertexNamespace().equals(namespace)) {
			return m_vertexMap.get(id);
		}
		return null;
	}

	@Override
	public Vertex getVertex(VertexRef reference) {
		return getSimpleVertex(reference);
	}

	private Vertex getSimpleVertex(VertexRef reference) {
		if (reference != null && getVertexNamespace().equals(reference.getNamespace())) {
			return m_vertexMap.get(reference.getId());
		}
		return null;
	}

	@Override
	public List<Vertex> getVertices() {
		return Collections.unmodifiableList(new ArrayList<Vertex>(m_vertexMap.values()));
	}

	@Override
	public List<Vertex> getVertices(Collection<? extends VertexRef> references) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		for(VertexRef ref : references) {
			Vertex vertex = getSimpleVertex(ref);
			if (vertex != null) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public List<Vertex> getRootGroup() {
		List<Vertex> rootGroup = new ArrayList<Vertex>(); 
		for(Vertex vertex : m_vertexMap.values()) {
			if (getParent(vertex) == null) {
				rootGroup.add(vertex);
			}
		}
		return rootGroup;
	}

	@Override
	public boolean hasChildren(VertexRef group) {
		return m_children.containsKey(group);
	}

	@Override
	public Vertex getParent(VertexRef vertex) {
		VertexRef parentRef = m_parents.get(vertex);
		return parentRef == null ? null : getSimpleVertex(parentRef);
	}

	@Override
	public boolean setParent(VertexRef child, VertexRef parent) {
		// Set the parent value on the vertex object
		getVertex(child).setParent(parent);

		// Add a parent mapping
		if (parent == null) {
			m_parents.remove(child);
		} else {
			m_parents.put(child, parent);
		}

		// Remove the child from any existing m_children mappings
		for (Set<VertexRef> vertex : m_children.values()) {
			vertex.remove(child);
		}

		boolean retval = false;
		if (parent == null) {
			retval = true;
		} else {
			// Add the child to m_children under the new parent
			Set<VertexRef> children = m_children.get(parent);
			if (children == null) {
				children = new TreeSet<VertexRef>();
				m_children.put(parent, children);
			}
			retval = children.add(child);
		}
		fireVertexSetChanged();
		return retval;
	}

	@Override
	public List<Vertex> getChildren(VertexRef group) {
		Set<VertexRef> children = m_children.get(group);
		return children == null ? Collections.<Vertex>emptyList() : getVertices(children);
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

	private void removeVertices(List<? extends VertexRef> all) {
		for(VertexRef vertex : all) {
			LoggerFactory.getLogger(this.getClass()).debug("Removing vertex: {}", vertex);
			// Remove the vertex from the main map
			m_vertexMap.remove(vertex.getId());
			// Remove the vertex from the parent and child maps
			m_children.remove(vertex);
			m_parents.remove(vertex);
		}
	}

	private void addVertices(Collection<Vertex> vertices) {
		for(Vertex vertex : vertices) {
			if (vertex.getNamespace() == null || vertex.getId() == null) {
				LoggerFactory.getLogger(this.getClass()).warn("Discarding invalid vertex: {}", vertex);
				continue;
			}
			LoggerFactory.getLogger(this.getClass()).debug("Adding vertex: {}", vertex);
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
	public List<Vertex> getVertices(Criteria criteria) {
		throw new UnsupportedOperationException("VertexProvider.getVertices is not yet implemented.");
	}

	@Override
	public int getSemanticZoomLevel(VertexRef vertex) {
		Vertex parent = getParent(vertex);
		return parent == null ? 0 : 1 + getSemanticZoomLevel(parent);
	}

	@Override
	public void clearVertices() {
		List<? extends Vertex> all = getVertices();
		removeVertices(all);
		fireVerticesRemoved(all);
	}

	/**
	 * @deprecated You should search by the namespace and ID tuple instead
	 */
	@Override
	public boolean containsVertexId(String id) {
		return containsVertexId(new AbstractVertexRef(getVertexNamespace(), id));
	}

	@Override
	public boolean containsVertexId(VertexRef id) {
		return getVertex(id) != null;
	}

}
