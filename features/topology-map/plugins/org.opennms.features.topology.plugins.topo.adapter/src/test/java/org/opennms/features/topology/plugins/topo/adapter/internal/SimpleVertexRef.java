package org.opennms.features.topology.plugins.topo.adapter.internal;

import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleVertexRef implements VertexRef {
	
	private final String m_namespace;
	private final String m_id;
	
	public SimpleVertexRef(String namespace, String id) {
		m_namespace = namespace;
		m_id = id; 
	}
	
	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public String getId() {
		return m_id;
	}
}