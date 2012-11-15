/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.app.internal.TopoEdge;
import org.opennms.features.topology.app.internal.TopoGraph;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.SparseGraph;

public class RealUltimateLayoutAlgorithm extends AbstractLayoutAlgorithm {

	public void updateLayout(GraphContainer graphContainer) {
		
		TopoGraph g = (TopoGraph) graphContainer.getGraph();
		
		int szl = graphContainer.getSemanticZoomLevel();
		
		final Layout graphLayout = g.getLayout();
		
		SparseGraph<Object, TopoEdge> jungGraph = new SparseGraph<Object, TopoEdge>();

		Collection<Object> vertices = graphContainer.getDisplayVertexIds(szl);		
		
		for(Object v : vertices) {
			jungGraph.addVertex(v);
		}
		
		List<TopoEdge> edges = g.getEdges(szl);
		
		for(TopoEdge e : edges) {
			jungGraph.addEdge(e, e.getSource().getItemId(), e.getTarget().getItemId());
		}
		
		Dimension size = selectLayoutSize(graphContainer);
		Dimension paddedSize = new Dimension((int)(size.getWidth()*.75), (int)(size.getHeight()*75));
		
		doISOMLayout(graphLayout, jungGraph, size);
		doSpringLayout(graphLayout, jungGraph, size, LAYOUT_REPULSION);
		doFRLayout(graphLayout, jungGraph, paddedSize, (int)(size.getWidth()/8), (int)(size.getHeight()/8));
		doSpringLayout(graphLayout, jungGraph, size, LAYOUT_REPULSION);

		
	}

	private void doSpringLayout(final Layout graphLayout, SparseGraph<Object, TopoEdge> jungGraph, Dimension size, int repulsion) {
		SpringLayout<Object, TopoEdge> layout = new SpringLayout<Object, TopoEdge>(jungGraph);
		layout.setInitializer(new Transformer<Object, Point2D>() {
			public Point2D transform(Object v) {
				return new Point(graphLayout.getX(v), graphLayout.getY(v));
			}
		});
		
		layout.setSize(size);
		layout.setRepulsionRange(repulsion);

		int count = 0;
		while(!layout.done() && count < 700) {
			layout.step();
			count++;
		}
		
		for(Object v : jungGraph.getVertices()) {
			graphLayout.setX(v, (int)layout.getX(v));
			graphLayout.setY(v, (int)layout.getY(v));
		}
	}
	
	private void doFRLayout(final Layout graphLayout, SparseGraph<Object, TopoEdge> jungGraph, Dimension size, final int xOffset, final int yOffset) {
		FRLayout<Object, TopoEdge> layout = new FRLayout<Object, TopoEdge>(jungGraph);
		layout.setInitializer(new Transformer<Object, Point2D>() {
			public Point2D transform(Object v) {
				return new Point(graphLayout.getX(v)-xOffset, graphLayout.getY(v)-yOffset);
			}
		});
		layout.setSize(size);
		
		while(!layout.done()) {
			layout.step();
		}
		
		
		for(Object v : jungGraph.getVertices()) {
			graphLayout.setX(v, (int)layout.getX(v)+xOffset);
			graphLayout.setY(v, (int)layout.getY(v)+yOffset);
		}
		
	}

	private void doISOMLayout(final Layout graphLayout, SparseGraph<Object, TopoEdge> jungGraph, Dimension size) {
		ISOMLayout<Object, TopoEdge> layout = new ISOMLayout<Object, TopoEdge>(jungGraph);
		layout.setInitializer(new Transformer<Object, Point2D>() {
			public Point2D transform(Object v) {
				return new Point(graphLayout.getX(v), graphLayout.getY(v));
			}
		});
		layout.setSize(size);
		
		while(!layout.done()) {
			layout.step();
		}
		
		
		for(Object v : jungGraph.getVertices()) {
			graphLayout.setX(v, (int)layout.getX(v));
			graphLayout.setY(v, (int)layout.getY(v));
		}
		
	}


}
