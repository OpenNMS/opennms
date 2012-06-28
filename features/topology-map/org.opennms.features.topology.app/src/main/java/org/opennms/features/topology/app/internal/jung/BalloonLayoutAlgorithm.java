package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.app.internal.Edge;
import org.opennms.features.topology.app.internal.Graph;
import org.opennms.features.topology.app.internal.Vertex;

import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.graph.Tree;

public class BalloonLayoutAlgorithm implements LayoutAlgorithm {
	
	Object m_rootItemId;

	public BalloonLayoutAlgorithm(Object rootItemId) {
		m_rootItemId = rootItemId;
	}

	public void updateLayout(GraphContainer graph) {
		
		Graph g = new Graph(graph);

		int szl = g.getSemanticZoomLevel();

		Vertex rootItem = g.getDisplayVertex(g.getVertexByItemId(m_rootItemId), szl);
		
		Tree<Vertex, Edge> tree = new OrderedKAryTree<Vertex, Edge>(50);
		
		Queue<Vertex> q = new LinkedList<Vertex>();
		Set<Vertex> found = new HashSet<Vertex>();
		
		q.add(rootItem);
		
		tree.addVertex(rootItem);
		
		Vertex v;
		while((v = q.poll()) != null) {
			List<Edge> edges = g.getEdgesForVertex(v, szl);
			for(Edge e : edges) {
				Vertex neighbor = e.getSource() != v ? e.getSource() : e.getTarget();
				tree.addEdge(e, v, neighbor);
				if (!found.contains(neighbor)) {
					q.add(neighbor);
				}
			}
		}
			
		
		BalloonLayout<Vertex, Edge> layout = new BalloonLayout<Vertex, Edge>(tree);
		layout.setInitializer(new Transformer<Vertex, Point2D>() {
			public Point2D transform(Vertex v) {
				return new Point(v.getX(), v.getY());
			}
		});
		
		layout.setSize(new Dimension(750,750));
		
		List<Vertex> vertices = g.getVertices(szl);
		for(Vertex vertex : vertices) {
			Point2D point = layout.transform(v);
			vertex.setX((int)point.getX());
			vertex.setY((int)point.getY());
		}
		
	}

}
