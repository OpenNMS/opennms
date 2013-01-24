package org.opennms.features.topology.api.topo;

import java.util.List;

public interface VertexListener {
	
	public void vertexSetChanged(VertexProvider provider); 
	public void vertexSetChanged(VertexProvider provider, List<? extends Vertex> added, List<? extends Vertex> update, List<String> removedVertexIds); 
	
}
