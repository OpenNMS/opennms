package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;

public class EdgeProviderMapImpl implements EdgeProvider {

	private Map<String,Edge> m_edges = new HashMap<String,Edge>();

	public EdgeProviderMapImpl() {
	}

	@Override
	public void addEdgeListener(EdgeListener vertexListener) {
		throw new UnsupportedOperationException();
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
	public List<? extends Edge> getEdges() {
		return Collections.unmodifiableList(Collections.list(Collections.enumeration(m_edges.values())));
	}

	@Override
	public List<? extends Edge> getEdges(Collection<? extends EdgeRef> references) {
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
	public String getNamespace() {
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
	public List<? extends Edge> getEdges(Criteria criteria) {
		throw new UnsupportedOperationException("EdgeProvider.getEdges is not yet implemented.");
	}

	@Override
	public boolean matches(EdgeRef edgeRef, Criteria criteria) {
		throw new UnsupportedOperationException("EdgeProvider.matches is not yet implemented.");
	}
}
