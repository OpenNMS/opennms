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

public class SimpleEdgeProvider implements EdgeProvider {
	
	private static abstract class MatchingCriteria implements Criteria {
		
		private String m_namespace;
		
		public MatchingCriteria(String namespace) {
			m_namespace = namespace;
		}

		@Override
		public ElementType getType() {
			return ElementType.EDGE;
		}

		@Override
		public String getNamespace() {
			return m_namespace;
		}
		
		public abstract boolean matches(Edge edge);
		
	}
	
	public static Criteria labelMatches(String namespace, final String regex) {
		return new MatchingCriteria(namespace) {
			
			@Override
			public boolean matches(Edge edge) {
				return edge.getLabel().matches(regex);
			}
		}; 
	}
	
	final String m_namespace;
	final Map<String, Edge> m_edgeMap = new LinkedHashMap<String, Edge>();
	final Set<EdgeListener> m_listeners = new CopyOnWriteArraySet<EdgeListener>();
	final String m_contributesTo;
	
	public SimpleEdgeProvider(String namespace, String contributesTo) {
		m_namespace = namespace;
		m_contributesTo = contributesTo;
	}
	
	public SimpleEdgeProvider(String namespace) {
		this(namespace, null);
	}

	@Override
	public String getEdgeNamespace() {
		return m_namespace;
	}
	
	@Override
	public boolean contributesTo(String namespace) {
		return m_contributesTo != null && m_contributesTo.equals(namespace);
	}
	
	private Edge getEdge(String id) {
		return m_edgeMap.get(id);
	}
	
	@Override
	public Edge getEdge(String namespace, String id) {
		return getEdge(id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return getSimpleEdge(reference);
	}

	private Edge getSimpleEdge(EdgeRef reference) {
		if (getEdgeNamespace().equals(reference.getNamespace())) {
			if (reference instanceof Edge) {
				return Edge.class.cast(reference);
			} else {
				return m_edgeMap.get(reference.getId());
			}
		} 
		return null;
	}

	@Override
	public List<Edge> getEdges() {
		return Collections.unmodifiableList(new ArrayList<Edge>(m_edgeMap.values()));
	}

	@Override
	public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
		List<Edge> edges = new ArrayList<Edge>();
		for(EdgeRef ref : references) {
			Edge edge = getSimpleEdge(ref);
			if (ref != null) {
				edges.add(edge);
			}
		}
		return edges;
	}

	private void fireEdgeSetChanged() {
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, null, null, null);
		}
	}

	private void fireEdgesAdded(List<Edge> edges) {
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, edges, null, null);
		}
	}

	private void fireEdgesRemoved(List<? extends EdgeRef> edges) {
		List<String> ids = new ArrayList<String>(edges.size());
		for(EdgeRef e : edges) {
			ids.add(e.getId());
		}
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, null, null, ids);
		}
	}

	@Override
	public void addEdgeListener(EdgeListener edgeListener) {
		m_listeners.add(edgeListener);
	}

	@Override
	public void removeEdgeListener(EdgeListener edgeListener) {
		m_listeners.remove(edgeListener);
	}
	
	private void removeEdges(List<? extends EdgeRef> edges) {
		for(EdgeRef edge : edges) {
			m_edgeMap.remove(edge.getId());
		}
	}
	
	private void addEdges(List<Edge> edges) {
		for(Edge edge : edges) {
			if (edge.getNamespace() == null || edge.getId() == null) {
				LoggerFactory.getLogger(this.getClass()).warn("Discarding invalid edge: {}", edge);
				continue;
			}
			LoggerFactory.getLogger(this.getClass()).debug("Adding edge: {}", edge);
			m_edgeMap.put(edge.getId(), edge);
		}
	}
	
	public void setEdges(List<Edge> edges) {
		m_edgeMap.clear();
		addEdges(edges);
		fireEdgeSetChanged();
	}
	
	public void add(Edge...edges) {
		add(Arrays.asList(edges));
	}
	
	public void add(List<Edge> edges) {
		addEdges(edges);
		fireEdgesAdded(edges);
	}
	
	public void remove(List<EdgeRef> edges) {
		removeEdges(edges);
		fireEdgesRemoved(edges);
	}
	
	public void remove(EdgeRef... edges) {
		remove(Arrays.asList(edges));
	}

	@Override
	public List<Edge> getEdges(Criteria c) {
		MatchingCriteria criteria = (MatchingCriteria) c;
		
		List<Edge> edges = new ArrayList<Edge>();
		
		for(Edge e : getEdges()) {
			if (criteria.matches(e)) {
				edges.add(e);
			}
		}
		return edges;

	}

	@Override
	public boolean matches(EdgeRef edgeRef, Criteria c) {
		MatchingCriteria criteria = (MatchingCriteria)c;
		
		return criteria.matches(getEdge(edgeRef));
	}

	@Override
	public void clearEdges() {
		List<Edge> all = getEdges();
		removeEdges(all);
		fireEdgesRemoved(all);
	}

}
