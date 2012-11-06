package org.opennms.features.topology.api.topo;

import java.util.List;

public interface EdgeListener {
	
	public void edgeSetChanged(EdgeProvider provider); 
	
	public void edgesAdded(EdgeProvider provider, List<? extends Edge> edges);
	
	public void edgesUpdated(EdgeProvider provider, List<? extends Edge> edges);
	
	public void edgesRemoved(EdgeProvider provider, List<? extends Edge> edges);
	
}
