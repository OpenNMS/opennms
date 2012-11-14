package org.opennms.features.topology.app.internal;

public class BaseGraphVisitor implements GraphVisitor {

	@Override
	public void visitGraph(TopoGraph graph) throws Exception {}

	@Override
	public void completeGraph(TopoGraph graph) throws Exception {}

	@Override
	public void visitVertex(TopoVertex vertex) throws Exception {}

	@Override
	public void completeVertex(TopoVertex vertex) throws Exception {}

	@Override
	public void visitEdge(TopoEdge edge) throws Exception {}

	@Override
	public void completeEdge(TopoEdge edge) throws Exception {}

}
