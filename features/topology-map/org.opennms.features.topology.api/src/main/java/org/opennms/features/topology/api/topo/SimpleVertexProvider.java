package org.opennms.features.topology.api.topo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.LoggerFactory;

public class SimpleVertexProvider implements VertexProvider {
	
	private final String m_namespace;
	private final Map<String,Vertex> m_vertexMap = new LinkedHashMap<String,Vertex>();
	private final Set<VertexListener> m_listeners = new CopyOnWriteArraySet<VertexListener>();
	private final Map<VertexRef, VertexRef> m_parents= new TreeMap<VertexRef, VertexRef>(new VertexRefComparator());
	private final Map<VertexRef, List<VertexRef>> m_children = new TreeMap<VertexRef, List<VertexRef>>(new VertexRefComparator());

	/**
	 * This comparator only cares about the primary key of the Vertex: the tuple of
	 * namespace and id.
	 */
	public static class VertexRefComparator implements Comparator<VertexRef> {

		@Override
		public int compare(VertexRef a, VertexRef b) {
			if (a == null) {
				if (b == null) {
					return 0;
				} else {
					return 1;
				}
			} else if (b == null) {
				return -1;
			} else {
				if (a.getNamespace() == null) {
					if (b.getNamespace() == null) {
						if (a.getId() == null) {
							if (b.getId() == null) {
								return 0;
							} else {
								return 1;
							}
						} else if (b.getId() == null) {
							return -1;
						} else {
							return a.getId().compareTo(b.getId());
						}
					} else {
						return 1;
					}
				} else if (b.getNamespace() == null) {
					return -1;
				} else {
					int comparison = a.getNamespace().compareTo(b.getNamespace());
					if (comparison == 0) {
						if (a.getId() == null) {
							if (b.getId() == null) {
								return 0;
							} else {
								return 1;
							}
						} else if (b.getId() == null) {
							return -1;
						} else {
							return a.getId().compareTo(b.getId());
						}
					} else {
						return comparison;
					}
				}
			}
		}

	}
	
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
