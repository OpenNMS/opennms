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

import java.util.Collection;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.jung.GridLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;
import org.opennms.netmgt.topology.persistence.api.PointEntity;

public class ManualLayoutAlgorithm implements LayoutAlgorithm {

    private final LayoutManager layoutManager;

    public ManualLayoutAlgorithm(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
	public void updateLayout(Graph graph) {
        final LayoutEntity layoutEntity = layoutManager != null ? layoutManager.loadLayout(graph) : null;
        if (layoutEntity != null) {
            // if we have a persisted layout, we apply it ...
            final Layout layout = graph.getLayout();
            final Collection<Vertex> vertices = graph.getDisplayVertices();
            for (Vertex vertex : vertices) {
                PointEntity pointEntity = layoutEntity.getPosition(vertex.getNamespace(), vertex.getId());
                layout.setLocation(vertex, new Point(pointEntity.getX(), pointEntity.getY()));
            }
        } else {
            // otherwise we apply the manual layout ...
            final Collection<Vertex> vertices = graph.getDisplayVertices();
            final Layout layout = graph.getLayout();
            final long notLayedOutCount = vertices.stream().filter(v -> {
                Point location = layout.getLocation(v);
                return location.getX() == 0 && location.getY() == 0;
            }).count();
            final long noVertexLocationCount = vertices.stream().filter(v -> {
                boolean hasNoX = v.getX() == null || v.getX().intValue() == 0;
                boolean hasNoY = v.getY() == null || v.getY().intValue() == 0;
                return hasNoX && hasNoY;
            }).count();

            // If nothing was manually layed out before, or the vertices do not have x,y coordinates assigned, we
            // manually apply the Grid Layout
            if (notLayedOutCount == vertices.size() && noVertexLocationCount == vertices.size()) {
                new GridLayoutAlgorithm().updateLayout(graph);
            } else if(noVertexLocationCount != vertices.size()) {
                // If we have at least one vertex with coordinates != (0,0), we apply them to the layout
                for (Vertex vertex : vertices) {
                    layout.setLocation(vertex, new Point(vertex.getX(), vertex.getY()));
                }
            } else {
                // Otherwise, we apply the x,y coordinates already assigned.
                // This means, the last calculated layout coordinates are re-applied.
                // At the moment no communication between ui and backend is made, when changing the coordinates manually
                // This is only done when the user explicitly saves the layout
                for (Vertex vertex : vertices) {
                    Point p = layout.getLocation(vertex);
                    layout.setLocation(vertex, p);
                }
            }
        }
	}

}
