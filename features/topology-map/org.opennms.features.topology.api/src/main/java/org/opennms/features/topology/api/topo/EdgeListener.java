package org.opennms.features.topology.api.topo;

import java.util.List;

public interface EdgeListener {
	
	public void edgeSetChanged(EdgeProvider provider); 
	public void edgeSetChanged(EdgeProvider provider, List<? extends Edge> added, List<? extends Edge> updated, List<String> removedEdgeIds); 
}
