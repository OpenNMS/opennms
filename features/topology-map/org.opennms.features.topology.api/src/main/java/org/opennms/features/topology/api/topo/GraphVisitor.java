package org.opennms.features.topology.api.topo;

import org.opennms.features.topology.api.Graph;


public interface GraphVisitor {
	
	public void visitGraph(Graph graph) throws Exception;
	public void completeGraph(Graph graph) throws Exception;
	
	public void visitVertex(Vertex vertex) throws Exception;
	
	public void visitEdge(Edge edge) throws Exception;

}
