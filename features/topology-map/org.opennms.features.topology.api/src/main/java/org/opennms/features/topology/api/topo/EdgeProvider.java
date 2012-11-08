package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.List;

public interface EdgeProvider {

	public String getNamespace();
	
	public Edge getEdge(String id);
	
	public Edge getEdge(EdgeRef reference);
	
	public List<? extends Edge> getEdges();
	
	public List<? extends Edge> getEdges(Collection<? extends EdgeRef> references);
	
	public void addEdgeListener(EdgeListener vertexListener);
	
	public void removeEdgeListener(EdgeListener vertexListener);

}
