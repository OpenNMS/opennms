package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;

import com.vaadin.server.PaintException;

public class BaseGraphVisitor implements GraphVisitor {

	@Override
	public void visitGraph(Graph graph) throws PaintException {}

	@Override
	public void visitVertex(Vertex vertex) throws PaintException {}

	@Override
	public void visitEdge(Edge edge) throws PaintException {}

	@Override
	public void completeGraph(Graph graph) throws PaintException {}

}
