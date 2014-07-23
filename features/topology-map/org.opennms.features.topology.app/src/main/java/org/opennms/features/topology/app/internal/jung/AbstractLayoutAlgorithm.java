package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm, LayoutConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLayoutAlgorithm.class);

	@Override
	public abstract void updateLayout(GraphContainer graph);

	protected static Dimension selectLayoutSize(GraphContainer g) {
	    int vertexCount = g.getGraph().getDisplayVertices().size();

	    double height = 1.5*Math.sqrt(vertexCount)*ELBOW_ROOM;
	    double width = height*16.0/9.0;

	    Dimension dim = new Dimension((int)width, (int)height);
	    
	    LOG.debug("selectLayoutSize: vertexCount={}, returm dim={}", vertexCount, dim);
	    
	    return dim;
	}

	protected static Transformer<VertexRef, Point2D> initializer(final Layout graphLayout) {
		return new Transformer<VertexRef, Point2D>() {
			@Override
			public Point2D transform(VertexRef v) {
				if (v == null) {
					LOG.warn("Algorithm tried to layout a null vertex");
					return new Point(0,0);
				}
				org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
				return new Point2D.Double(location.getX(), location.getY());
			}
		};
	}

	protected static Transformer<VertexRef, Point2D> initializer(final Layout graphLayout, final int xOffset, final int yOffset) {
		return new Transformer<VertexRef, Point2D>() {
			@Override
			public Point2D transform(VertexRef v) {
				org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
				return new Point2D.Double(location.getX()-xOffset, location.getY()-yOffset);
			}
		};
	}

}
