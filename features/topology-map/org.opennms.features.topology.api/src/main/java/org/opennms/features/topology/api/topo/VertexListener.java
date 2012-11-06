package org.opennms.features.topology.api.topo;

import java.util.List;

public interface VertexListener {
	
	public void vertexSetChanged(VertexProvider provider); 
	
	public void verticesAdded(VertexProvider provider, List<Vertex> vertices);
	
	public void verticesUpdated(VertexProvider provider, List<Vertex> vertices);
	
	public void verticesRemoved(VertexProvider provider, List<Vertex> vertices);
	
}
