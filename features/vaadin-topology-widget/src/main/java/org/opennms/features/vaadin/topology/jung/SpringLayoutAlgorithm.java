package org.opennms.features.vaadin.topology.jung;

import java.awt.Dimension;
import java.util.List;

import org.opennms.features.vaadin.topology.Edge;
import org.opennms.features.vaadin.topology.Graph;
import org.opennms.features.vaadin.topology.GraphContainer;
import org.opennms.features.vaadin.topology.LayoutAlgorithm;
import org.opennms.features.vaadin.topology.Vertex;

import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.SparseGraph;

public class SpringLayoutAlgorithm implements LayoutAlgorithm {

	@Override
	public void updateLayout(GraphContainer graph) {
		
		Graph g = new Graph(graph);
		
		int szl = g.getSemanticZoomLevel();
		
		
		SparseGraph<Vertex, Edge> jungGraph = new SparseGraph<Vertex, Edge>();
		
		
		List<Vertex> vertices = g.getVertices(szl);
		
		for(Vertex v : vertices) {
			jungGraph.addVertex(v);
		}
		
		List<Edge> edges = g.getEdges(szl);
		
		for(Edge e : edges) {
			jungGraph.addEdge(e, e.getSource(), e.getTarget());
		}
		

		
		SpringLayout2<Vertex, Edge> layout = new SpringLayout2<Vertex, Edge>(jungGraph);
		layout.setSize(new Dimension(750,750));
		layout.setRepulsionRange(250);
		
		// run it 1000 times to get the layout
		for(int i = 0; i < 100; i++) {
			layout.step();
		}
		
		
		for(Vertex v : vertices) {
			v.setX((int)layout.getX(v));
			v.setY((int)layout.getY(v));
		}
		
		
		
		
	}

}
