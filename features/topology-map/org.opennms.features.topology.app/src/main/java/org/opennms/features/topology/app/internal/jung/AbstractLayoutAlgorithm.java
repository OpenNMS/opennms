package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.app.internal.Graph;

public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm, LayoutConstants {

	@Override
	abstract public void updateLayout(GraphContainer graph);

	protected Dimension selectLayoutSize(Graph g) {
		int vertexCount = g.getVertices(g.getSemanticZoomLevel()).size();
		
		 double height = .75*Math.sqrt(vertexCount)*ELBOW_ROOM;
		 double width = height*16/9;
		 
		 return new Dimension((int)width, (int)height);
		 
		
	}

}
