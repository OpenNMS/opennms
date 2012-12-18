package org.opennms.features.topology.api;

import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;

public class SimpleConnector implements Connector {

	private final String m_namespace;
	private final String m_id;
	private final Vertex m_vertex;
	private EdgeRef m_edge;

	/**
	 * @param namespace
	 * @param id
	 * @param vertex
	 */
	public SimpleConnector(String namespace, String id, Vertex vertex) {
		m_namespace = namespace;
		m_id = id;
		m_vertex = vertex;
	}

	/**
	 * @param namespace
	 * @param id
	 * @param vertex
	 * @param edge
	 */
	public SimpleConnector(String namespace, String id, Vertex vertex, EdgeRef edge) {
		this(namespace, id, vertex);
		m_edge = edge;
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
	public Vertex getVertex() {
		return m_vertex;
	}

}
