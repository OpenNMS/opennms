package org.opennms.features.topology.api.topo;

import java.util.Collection;

public interface EdgeListener {
	
	public void edgeSetChanged(EdgeProvider provider); 
	public void edgeSetChanged(EdgeProvider provider, Collection<? extends Edge> added, Collection<? extends Edge> updated, Collection<String> removedEdgeIds); 
}
