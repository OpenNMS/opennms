package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleConnector implements Connector {
	
	private final String m_namespace;
	private final String m_id;
	private final VertexRef m_vertex;
	private EdgeRef m_edge;

	public SimpleConnector(String namespace, String id, VertexRef vertex) {
		m_namespace = namespace;
		m_id = id;
		m_vertex = vertex;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public EdgeRef getEdge() {
		return m_edge;
	}
	
	public void setEdge(EdgeRef edgeRef) {
		m_edge = edgeRef;
	}

	@Override
	public VertexRef getVertex() {
		return m_vertex;
	}

}
