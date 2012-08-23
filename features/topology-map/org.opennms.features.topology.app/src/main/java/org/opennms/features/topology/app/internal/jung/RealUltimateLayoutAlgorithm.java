package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.app.internal.Edge;
import org.opennms.features.topology.app.internal.Graph;
import org.opennms.features.topology.app.internal.Vertex;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.SparseGraph;

public class RealUltimateLayoutAlgorithm implements LayoutAlgorithm, LayoutConstants {

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
		
		
		doISOMLayout(jungGraph, LAYOUT_WIDTH, LAYOUT_HEIGHT);
		doSpringLayout(jungGraph, LAYOUT_WIDTH, LAYOUT_HEIGHT, LAYOUT_REPULSION);
		doFRLayout(jungGraph, LAYOUT_WIDTH*3/4, LAYOUT_HEIGHT*3/4, LAYOUT_WIDTH/8, LAYOUT_HEIGHT/8);
		doSpringLayout(jungGraph, LAYOUT_WIDTH, LAYOUT_HEIGHT, LAYOUT_REPULSION);

		
	}

	private void doSpringLayout(SparseGraph<Vertex, Edge> jungGraph, int width, int height, int repulsion) {
		SpringLayout<Vertex, Edge> layout = new SpringLayout<Vertex, Edge>(jungGraph);
		layout.setInitializer(new Transformer<Vertex, Point2D>() {
			public Point2D transform(Vertex v) {
				return new Point(v.getX(), v.getY());
			}
		});
		
		layout.setSize(new Dimension(width, height));
		layout.setRepulsionRange(repulsion);

		int count = 0;
		while(!layout.done() && count < 700) {
			layout.step();
			count++;
		}
		
		for(Vertex v : jungGraph.getVertices()) {
			v.setX((int)layout.getX(v));
			v.setY((int)layout.getY(v));
		}
	}
	
	private void doFRLayout(SparseGraph<Vertex, Edge> jungGraph, int width, int height, final int xOffset, final int yOffset) {
		FRLayout<Vertex, Edge> layout = new FRLayout<Vertex, Edge>(jungGraph);
		layout.setInitializer(new Transformer<Vertex, Point2D>() {
			public Point2D transform(Vertex v) {
				return new Point(v.getX()-xOffset, v.getY()-yOffset);
			}
		});
		layout.setSize(new Dimension(width,height));
		
		while(!layout.done()) {
			layout.step();
		}
		
		
		for(Vertex v : jungGraph.getVertices()) {
			v.setX((int)layout.getX(v)+xOffset);
			v.setY((int)layout.getY(v)+yOffset);
		}
		
	}

	private void doISOMLayout(SparseGraph<Vertex, Edge> jungGraph, int width, int height) {
		ISOMLayout<Vertex, Edge> layout = new ISOMLayout<Vertex, Edge>(jungGraph);
		layout.setInitializer(new Transformer<Vertex, Point2D>() {
			public Point2D transform(Vertex v) {
				return new Point(v.getX(), v.getY());
			}
		});
		layout.setSize(new Dimension(width,height));
		
		while(!layout.done()) {
			layout.step();
		}
		
		
		for(Vertex v : jungGraph.getVertices()) {
			v.setX((int)layout.getX(v));
			v.setY((int)layout.getY(v));
		}
		
	}


}
