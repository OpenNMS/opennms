/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm, LayoutConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLayoutAlgorithm.class);

	@Override
	public abstract void updateLayout(Graph graph);

	protected static Dimension selectLayoutSize(Graph graph) {
	    int vertexCount = graph.getDisplayVertices().size();

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
