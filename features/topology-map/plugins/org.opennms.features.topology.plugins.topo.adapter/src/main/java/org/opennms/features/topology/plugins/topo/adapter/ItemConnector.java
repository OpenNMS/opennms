package org.opennms.features.topology.plugins.topo.adapter;

import org.opennms.features.topology.api.topo.Connector;

class ItemConnector implements Connector {
	private final String m_namespace;
	private final String m_id;
	private final ItemVertex m_vertex;
	private final ItemEdge m_edge;
	
	public ItemConnector(String namespace, String id, ItemVertex vertex, ItemEdge edge) {
		m_namespace = namespace;
		m_id = id;
		m_vertex = vertex;
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
	public ItemEdge getEdge() {
		return m_edge;
	}
	@Override
	public ItemVertex getVertex() {
		return m_vertex;
	}
}