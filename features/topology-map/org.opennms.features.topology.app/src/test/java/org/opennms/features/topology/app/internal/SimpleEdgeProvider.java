package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;

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
	final Map<String, SimpleEdge> m_edgeMap = new LinkedHashMap<String, SimpleEdge>();
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
	public String getNamespace() {
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

	private SimpleEdge getSimpleEdge(EdgeRef reference) {
		if (getNamespace().equals(reference.getNamespace())) {
			if (reference instanceof SimpleEdge) {
				return SimpleEdge.class.cast(reference);
			} else {
				return m_edgeMap.get(reference.getId());
			}
		} 
		return null;
	}

	@Override
	public List<? extends Edge> getEdges() {
		return Collections.unmodifiableList(new ArrayList<SimpleEdge>(m_edgeMap.values()));
	}

	@Override
	public List<? extends Edge> getEdges(Collection<? extends EdgeRef> references) {
		List<SimpleEdge> edges = new ArrayList<SimpleEdge>();
		for(EdgeRef ref : references) {
			SimpleEdge edge = getSimpleEdge(ref);
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

	private void fireEdgesAdded(List<SimpleEdge> edges) {
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, edges, null, null);
		}
	}

	private void fireEdgesRemoved(List<SimpleEdge> edges) {
		List<String> ids = new ArrayList<String>(edges.size());
		for(SimpleEdge e : edges) {
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
	
	private void removeEdges(List<SimpleEdge> edges) {
		for(SimpleEdge edge : edges) {
			m_edgeMap.remove(edge.getId());
		}
	}
	
	private void addEdges(List<SimpleEdge> edges) {
		for(SimpleEdge edge : edges) {
			m_edgeMap.put(edge.getId(), edge);
		}
	}
	
	public void setEdges(List<SimpleEdge> edges) {
		m_edgeMap.clear();
		addEdges(edges);
		fireEdgeSetChanged();
	}
	
	public void add(SimpleEdge...edges) {
		add(Arrays.asList(edges));
	}
	
	public void add(List<SimpleEdge> edges) {
		addEdges(edges);
		fireEdgesAdded(edges);
	}
	
	public void remove(List<SimpleEdge> edges) {
		removeEdges(edges);
		fireEdgesRemoved(edges);
	}
	
	public void remove(SimpleEdge... edges) {
		remove(Arrays.asList(edges));
	}

	@Override
	public List<? extends Edge> getEdges(Criteria c) {
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

}
