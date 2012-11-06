package org.opennms.features.topology.api.topo;

public interface Connector {
	
	EdgeRef getEdge();
	
	VertexRef getVertex();
}
