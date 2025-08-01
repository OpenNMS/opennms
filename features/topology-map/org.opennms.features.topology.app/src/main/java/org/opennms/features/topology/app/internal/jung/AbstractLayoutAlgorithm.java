/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import com.google.common.base.Function;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm, LayoutConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLayoutAlgorithm.class);

    protected AbstractLayoutAlgorithm() {}

	protected static Dimension selectLayoutSize(Graph graph) {
	    int vertexCount = graph.getDisplayVertices().size();

	    double height = 1.5*Math.sqrt(vertexCount)*ELBOW_ROOM;
	    double width = height*16.0/9.0;

	    Dimension dim = new Dimension((int)width, (int)height);
	    
	    LOG.debug("selectLayoutSize: vertexCount={}, returm dim={}", vertexCount, dim);
	    
	    return dim;
	}

	protected static Function<VertexRef, Point2D> initializer(final Layout graphLayout) {
		return (final VertexRef v) -> {
			if (v == null) {
				LOG.warn("Algorithm tried to layout a null vertex");
				return new Point(0,0);
			}
			org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
			return new Point2D.Double(location.getX(), location.getY());
		};
	}

	protected static Function<VertexRef, Point2D> initializer(final Layout graphLayout, final int xOffset, final int yOffset) {
		return (final VertexRef v) -> {
			org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
			return new Point2D.Double(location.getX()-xOffset, location.getY()-yOffset);
		};
	}

}
