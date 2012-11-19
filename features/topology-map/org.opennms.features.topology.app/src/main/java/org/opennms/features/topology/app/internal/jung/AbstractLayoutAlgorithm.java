package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.topo.VertexRef;

public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm, LayoutConstants {

	@Override
	abstract public void updateLayout(GraphContainer graph);

	protected Dimension selectLayoutSize(GraphContainer g) {
		int vertexCount = g.getGraph().getDisplayVertices().size();
		
		 double height = .75*Math.sqrt(vertexCount)*ELBOW_ROOM;
		 double width = height*16/9;
		 
		 return new Dimension((int)width, (int)height);
		 
		
	}

	protected Transformer<VertexRef, Point2D> initializer(final Layout graphLayout) {
		return new Transformer<VertexRef, Point2D>() {
			public Point2D transform(VertexRef v) {
				return new Point(graphLayout.getLocation(v).getX(), graphLayout.getLocation(v).getY());
			}
		};
	}

	protected Transformer<VertexRef, Point2D> initializer(final Layout graphLayout, final int xOffset, final int yOffset) {
		return new Transformer<VertexRef, Point2D>() {
			public Point2D transform(VertexRef v) {
				return new Point(graphLayout.getLocation(v).getX()-xOffset, graphLayout.getLocation(v).getY()-yOffset);
			}
		};
	}

}
