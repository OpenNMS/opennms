package org.opennms.features.topology.app.internal;


public interface GraphVisitor {
	
	public void visitGraph(TopoGraph graph) throws Exception;
	public void completeGraph(TopoGraph graph) throws Exception;
	
	public void visitVertex(TopoVertex vertex) throws Exception;
	public void completeVertex(TopoVertex vertex) throws Exception;
	
	public void visitEdge(TopoEdge edge) throws Exception;
	public void completeEdge(TopoEdge edge) throws Exception;

}
