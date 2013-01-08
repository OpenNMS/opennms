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
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.LoggerFactory;

public class SimpleVertexProvider implements VertexProvider {
	
	final String m_namespace;
	final Map<String,Vertex> m_vertexMap = new LinkedHashMap<String,Vertex>();
	final Set<VertexListener> m_listeners = new CopyOnWriteArraySet<VertexListener>();
	final Map<VertexRef, VertexRef> m_parents= new HashMap<VertexRef, VertexRef>();
	final Map<VertexRef, List<VertexRef>> m_children = new HashMap<VertexRef, List<VertexRef>>();
	
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
		if (getVertexNamespace().equals(reference.getNamespace())) {
			/*
			if (reference instanceof Vertex) {
				return Vertex.class.cast(reference);
			} else {
			*/
				return m_vertexMap.get(reference.getId());
			//}
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
		m_parents.put(child, parent);
		
		List<VertexRef> children = m_children.get(parent);
		if (children == null) {
			children = new ArrayList<VertexRef>();
			m_children.put(parent, children);
		}
		boolean retval = children.add(child);
		fireVertexSetChanged();
		return retval;
	}

	@Override
	public List<Vertex> getChildren(VertexRef group) {
		List<VertexRef> children = m_children.get(group);
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
			m_vertexMap.remove(vertex.getId());
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
		m_vertexMap.clear();
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

	@Override
	public boolean containsVertexId(String id) {
		return containsVertexId(new AbstractVertexRef(getVertexNamespace(), id));
	}

	@Override
	public boolean containsVertexId(VertexRef id) {
		return getVertex(id) != null;
	}

}
