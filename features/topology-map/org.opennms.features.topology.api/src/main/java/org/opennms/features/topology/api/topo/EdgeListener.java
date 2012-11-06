package org.opennms.features.topology.api.topo;

import java.util.List;

public interface EdgeListener {
	
	public void edgeSetChanged(VertexProvider provider); 
	
	public void edgeAdded(VertexProvider provider, List<Vertex> vertices);
	
	public void edgeUpdated(VertexProvider provider, List<Vertex> vertices);
	
	public void edgeRemoved(VertexProvider provider, List<Vertex> vertices);
	
}
