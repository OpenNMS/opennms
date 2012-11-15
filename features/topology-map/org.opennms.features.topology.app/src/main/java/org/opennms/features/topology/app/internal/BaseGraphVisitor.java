package org.opennms.features.topology.app.internal;

import com.vaadin.terminal.PaintException;

public class BaseGraphVisitor implements GraphVisitor {

	@Override
	public void visitGraph(TopoGraph graph) throws PaintException {}

	@Override
	public void completeGraph(TopoGraph graph) throws PaintException {}

	@Override
	public void visitVertex(TopoVertex vertex) throws PaintException {}

	@Override
	public void completeVertex(TopoVertex vertex) throws PaintException {}

	@Override
	public void visitEdge(TopoEdge edge) throws PaintException {}

	@Override
	public void completeEdge(TopoEdge edge) throws PaintException {}

}
