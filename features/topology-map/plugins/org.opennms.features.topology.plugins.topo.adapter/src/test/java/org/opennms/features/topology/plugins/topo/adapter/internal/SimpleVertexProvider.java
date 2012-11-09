package org.opennms.features.topology.plugins.topo.adapter.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleVertexProvider implements VertexProvider {
	
	final String m_namespace;
	final Map<String, SimpleVertex> m_vertexMap = new LinkedHashMap<String, SimpleVertex>();
	final Set<VertexListener> m_listeners = new CopyOnWriteArraySet<VertexListener>();
	
	public SimpleVertexProvider(String namespace) {
		m_namespace = namespace;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public Vertex getVertex(String id) {
		return m_vertexMap.get(id);
	}

	@Override
	public Vertex getVertex(VertexRef reference) {
		return getSimpleVertex(reference);
	}

	private SimpleVertex getSimpleVertex(VertexRef reference) {
		if (getNamespace().equals(reference.getNamespace())) {
			if (reference instanceof SimpleVertex) {
				return SimpleVertex.class.cast(reference);
			} else {
				return m_vertexMap.get(reference.getId());
			}
		} 
		return null;
	}

	@Override
	public List<? extends Vertex> getVertices() {
		return Collections.unmodifiableList(new ArrayList<SimpleVertex>(m_vertexMap.values()));
	}

	@Override
	public List<? extends Vertex> getVertices(Collection<? extends VertexRef> references) {
		List<SimpleVertex> vertices = new ArrayList<SimpleVertex>();
		for(VertexRef ref : references) {
			SimpleVertex vertex = getSimpleVertex(ref);
			if (ref != null) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

	@Override
	public List<? extends Vertex> getRootGroup() {
		throw new UnsupportedOperationException("VertexProvider.getRootGroup is not yet implemented.");
	}

	@Override
	public boolean hasChildren(VertexRef group) {
		throw new UnsupportedOperationException("VertexProvider.hasChildren is not yet implemented.");
	}

	@Override
	public Vertex getParent(VertexRef vertex) {
		throw new UnsupportedOperationException("VertexProvider.getParent is not yet implemented.");
	}

	@Override
	public List<? extends Vertex> getChildren(VertexRef group) {
		throw new UnsupportedOperationException("VertexProvider.getChildren is not yet implemented.");
	}
	
	private void fireVertexSetChanged() {
		for(VertexListener listener : m_listeners) {
			listener.vertexSetChanged(this);
		}
	}

	private void fireVerticesAdded(List<SimpleVertex> vertices) {
		for(VertexListener listener : m_listeners) {
			listener.vertexSetChanged(this, vertices, null, null);
		}
	}

	private void fireVerticesRemoved(List<SimpleVertex> vertices) {
		List<String> ids = new ArrayList<String>(vertices.size());
		for(SimpleVertex vertex : vertices) {
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
	
	private void removeVertices(List<SimpleVertex> vertices) {
		for(SimpleVertex vertex : vertices) {
			m_vertexMap.remove(vertex.getId());
		}
	}
	
	private void addVertices(List<SimpleVertex> vertices) {
		for(SimpleVertex vertex : vertices) {
			m_vertexMap.put(vertex.getId(), vertex);
		}
	}
	
	public void setVertices(List<SimpleVertex> vertices) {
		m_vertexMap.clear();
		addVertices(vertices);
		fireVertexSetChanged();
	}
	
	public void add(SimpleVertex...vertices) {
		add(Arrays.asList(vertices));
	}
	
	public void add(List<SimpleVertex> vertices) {
		addVertices(vertices);
		fireVerticesAdded(vertices);
	}
	
	public void remove(List<SimpleVertex> vertices) {
		removeVertices(vertices);
		fireVerticesRemoved(vertices);
	}
	
	public void remove(SimpleVertex... vertices) {
		remove(Arrays.asList(vertices));
	}

}
