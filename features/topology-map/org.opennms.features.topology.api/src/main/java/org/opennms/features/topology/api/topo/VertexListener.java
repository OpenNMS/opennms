package org.opennms.features.topology.api.topo;

import java.util.Collection;

public interface VertexListener {
	
	public void vertexSetChanged(VertexProvider provider); 
	public void vertexSetChanged(VertexProvider provider, Collection<? extends Vertex> added, Collection<? extends Vertex> update, Collection<String> removedVertexIds); 
	
}
