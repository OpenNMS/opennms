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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class DefaultLayout implements Layout {
	
	private GraphContainer m_graphContainer;
	
	private final Map<VertexRef, Point> m_locations = new HashMap<VertexRef, Point>();

	public DefaultLayout(GraphContainer graphContainer) {
		m_graphContainer = graphContainer;
	}

	@Override
	public Point getLocation(VertexRef v) {
		if (v == null) {
			throw new IllegalArgumentException("Cannot fetch location of null vertex");
		}
		// Try to find an existing location
		Point p = m_locations.get(v);
		if (p == null) {
			// If there isn't one, then try to find a neighboring vertex and use it
			// as the initial location
			for (Edge edge : m_graphContainer.getGraph().getDisplayEdges()){
				if (v.equals(edge.getSource().getVertex())) {
					Point neighbor = m_locations.get(edge.getTarget().getVertex());
					if (neighbor != null) {
						return neighbor;
					}
				} else if (v.equals(edge.getTarget().getVertex())) {
					Point neighbor = m_locations.get(edge.getSource().getVertex());
					if (neighbor != null) {
						return neighbor;
					}
				}
			}
			// If there are no neighbors, return the center of the layout
			//return getBounds().getCenter();
			return new Point(0, 0);
		} else {
			return p;
		}
	}
	
	@Override
	public Map<VertexRef,Point> getLocations() {
		return Collections.unmodifiableMap(new HashMap<VertexRef,Point>(m_locations));
	}
	
	@Override
	public void setLocation(VertexRef v, double x, double y) {
		setLocation(v, new Point(x, y));
	}

	@Override
	public void setLocation(VertexRef v, Point location) {
		if (v == null) {
			throw new IllegalArgumentException("Cannot set location of null vertex");
		}
		m_locations.put(v, location);
	}

	@Override
	public Point getInitialLocation(VertexRef v) {
		if (v == null) {
			throw new IllegalArgumentException("Cannot get initial location of null vertex");
		}
		Vertex parent = m_graphContainer.getBaseTopology().getParent(v);
		return parent == null ? getLocation(v) : getLocation(parent);
	}
	
    @Override
    public BoundingBox getBounds() {
        Collection<? extends Vertex> vertices = m_graphContainer.getGraph().getDisplayVertices();
        if(vertices.size() > 0) {
            Collection<VertexRef> vRefs = new ArrayList<VertexRef>();
            for(Vertex v : vertices) {
                vRefs.add(v);
            }
        
            return computeBoundingBox(vRefs);
        } else {
            BoundingBox bBox = new BoundingBox();
            bBox.addPoint(new Point(0, 0));
            return bBox;
        }
    }
    
    private static BoundingBox computeBoundingBox(Layout layout, VertexRef vertRef) {
        return new BoundingBox(layout.getLocation(vertRef), 100, 100);
    }
    
    @Override
    public BoundingBox computeBoundingBox(Collection<VertexRef> vertRefs) {
        if(vertRefs != null && vertRefs.size() > 0) {
            BoundingBox boundingBox = new BoundingBox();
            for(VertexRef vertRef : vertRefs) {
                boundingBox.addBoundingbox( computeBoundingBox(this, vertRef) );
            }
            return boundingBox;
        }else {
            return getBounds();
        }
        
    }

}
