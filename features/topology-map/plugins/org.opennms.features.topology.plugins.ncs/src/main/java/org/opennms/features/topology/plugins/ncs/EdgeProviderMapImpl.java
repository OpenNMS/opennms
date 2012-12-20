package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;

public class EdgeProviderMapImpl implements EdgeProvider {

	private Map<String,Edge> m_edges = new HashMap<String,Edge>();
	final Set<EdgeListener> m_listeners = new CopyOnWriteArraySet<EdgeListener>();

	public EdgeProviderMapImpl() {
	}

	@Override
	public void addEdgeListener(EdgeListener edgeListener) {
		m_listeners.add(edgeListener);
	}

	private Edge getEdge(String id) {
		return m_edges.get(id);
	}
	
	@Override
	public Edge getEdge(String namespace, String id) {
		return getEdge(id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return getEdge(reference.getId());
	}

	@Override
	public List<Edge> getEdges() {
		return Collections.unmodifiableList(Collections.list(Collections.enumeration(m_edges.values())));
	}

	@Override
	public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
		List<Edge> retval = new ArrayList<Edge>();
		for (EdgeRef reference : references) {
			Edge edge = getEdge(reference);
			if (edge != null) {
				retval.add(edge);
			}
		}
		return Collections.unmodifiableList(retval);
	}

	@Override
	public String getEdgeNamespace() {
		return "ncs";
	}
	
	@Override
	public boolean contributesTo(String namespace) {
		return "nodes".equals(namespace);
	}

	@Override
	public void removeEdgeListener(EdgeListener vertexListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Edge> getEdges(Criteria criteria) {
		throw new UnsupportedOperationException("EdgeProvider.getEdges is not yet implemented.");
	}

	@Override
	public boolean matches(EdgeRef edgeRef, Criteria criteria) {
		throw new UnsupportedOperationException("EdgeProvider.matches is not yet implemented.");
	}


	@Override
	public void clearEdges() {
		List<Edge> all = new ArrayList<Edge>(m_edges.size()); 
		all.addAll(getEdges());
		m_edges.clear();
		fireEdgesRemoved(all);
	}

	private void fireEdgesRemoved(List<Edge> edges) {
		List<String> ids = new ArrayList<String>(edges.size());
		for(Edge e : edges) {
			ids.add(e.getId());
		}
		for(EdgeListener listener : m_listeners) {
			listener.edgeSetChanged(this, null, null, ids);
		}
	}

}
