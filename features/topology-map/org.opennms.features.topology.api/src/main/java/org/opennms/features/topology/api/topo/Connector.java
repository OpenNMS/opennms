package org.opennms.features.topology.api.topo;

public interface Connector extends ConnectorRef {
	
	EdgeRef getEdge();
	
	VertexRef getVertex();
}
