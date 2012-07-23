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

import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class TreeLayoutAlgorithm implements LayoutAlgorithm {

	public void updateLayout(GraphContainer graph) {
		
		Graph g = new Graph(graph);
		
		int szl = g.getSemanticZoomLevel();
		
		
		DirectedSparseGraph<Vertex, Edge> jungGraph = new DirectedSparseGraph<Vertex, Edge>();
		
		
		List<Vertex> vertices = g.getVertices(szl);
		
		for(Vertex v : vertices) {
			jungGraph.addVertex(v);
		}
		
		List<Edge> edges = g.getEdges(szl);
		
		for(Edge e : edges) {
			jungGraph.addEdge(e, e.getSource(), e.getTarget());
		}
		
		DelegateForest<Vertex, Edge> forest = new DelegateForest<Vertex, Edge>(jungGraph);

		RadialTreeLayout<Vertex,Edge> layout = new RadialTreeLayout<Vertex, Edge>(forest);
		layout.setInitializer(new Transformer<Vertex, Point2D>() {
			public Point2D transform(Vertex v) {
				return new Point(v.getX(), v.getY());
			}
		});
		layout.setSize(new Dimension(750,750));
		
		for(Vertex v : vertices) {
			Point2D point = layout.transform(v);
			v.setX((int)point.getX());
			v.setY((int)point.getY());
		}
	}

}
