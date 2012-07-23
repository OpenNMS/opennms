package org.opennms.features.vaadin.topology.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.vaadin.topology.Edge;
import org.opennms.features.vaadin.topology.Graph;
import org.opennms.features.vaadin.topology.GraphContainer;
import org.opennms.features.vaadin.topology.LayoutAlgorithm;
import org.opennms.features.vaadin.topology.Vertex;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.SparseGraph;

public class KKLayoutAlgorithm implements LayoutAlgorithm {

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
		

		KKLayout<Vertex, Edge> layout = new KKLayout<Vertex, Edge>(jungGraph);
		layout.setInitializer(new Transformer<Vertex, Point2D>() {
			public Point2D transform(Vertex v) {
				return new Point(v.getX(), v.getY());
			}
		});
		layout.setSize(new Dimension(750,750));
		
		for(Vertex v : vertices) {
			layout.lock(v, v.isLocked());
		}
		
		while(!layout.done()) {
			layout.step();
		}
		
		
		for(Vertex v : vertices) {
			v.setX((int)layout.getX(v));
			v.setY((int)layout.getY(v));
		}
		
		
		
		
	}

}
